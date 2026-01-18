package com.geosilent.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.MotionEvent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.geosilent.ui.theme.*
import com.geosilent.ui.viewmodels.MapViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel,
    zoneId: Long?,
    onLocationConfirmed: (Double, Double, Float) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    
    // Initialize osmdroid configuration
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = context.packageName
    }
    
    // Load existing zone if editing
    LaunchedEffect(zoneId) {
        viewModel.loadExistingZone(zoneId)
    }
    
    // Get current location
    LaunchedEffect(locationPermissionState.status.isGranted) {
        if (locationPermissionState.status.isGranted) {
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                @SuppressLint("MissingPermission")
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let { viewModel.setCurrentLocation(it) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Location") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark
                )
            )
        },
        containerColor = BackgroundDark
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (!locationPermissionState.status.isGranted) {
                // Permission request view
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.LocationOff,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Location Permission Required",
                        style = MaterialTheme.typography.titleLarge,
                        color = OnBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "To select a location on the map, please grant location permission.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { locationPermissionState.launchPermissionRequest() },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Text("Grant Permission")
                    }
                }
            } else {
                // OpenStreetMap view
                val selectedLocation = uiState.selectedLocation
                val currentLocation = uiState.currentLocation
                val radius = uiState.radius
                
                AndroidView(
                    factory = { ctx ->
                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            controller.setZoom(16.0)
                            
                            // Set initial position
                            val initialPoint = when {
                                selectedLocation != null -> GeoPoint(selectedLocation.latitude, selectedLocation.longitude)
                                currentLocation != null -> GeoPoint(currentLocation.latitude, currentLocation.longitude)
                                else -> GeoPoint(28.6139, 77.2090) // Default to Delhi
                            }
                            controller.setCenter(initialPoint)
                            
                            // Handle map clicks
                            setOnTouchListener { view, event ->
                                if (event.action == MotionEvent.ACTION_UP) {
                                    val projection = projection
                                    val geoPoint = projection.fromPixels(event.x.toInt(), event.y.toInt()) as GeoPoint
                                    viewModel.setSelectedLocation(
                                        com.google.android.gms.maps.model.LatLng(
                                            geoPoint.latitude,
                                            geoPoint.longitude
                                        )
                                    )
                                }
                                false
                            }
                        }
                    },
                    update = { mapView ->
                        // Clear existing overlays
                        mapView.overlays.clear()
                        
                        // Add marker and circle for selected location
                        selectedLocation?.let { location ->
                            val geoPoint = GeoPoint(location.latitude, location.longitude)
                            
                            // Add marker
                            val marker = Marker(mapView).apply {
                                position = geoPoint
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                title = "Selected Location"
                            }
                            mapView.overlays.add(marker)
                            
                            // Add circle for radius
                            val circle = Polygon(mapView).apply {
                                points = Polygon.pointsAsCircle(geoPoint, radius.toDouble())
                                fillPaint.color = Color.argb(51, 99, 102, 241) // Primary with alpha
                                outlinePaint.color = Color.rgb(99, 102, 241) // Primary
                                outlinePaint.strokeWidth = 3f
                            }
                            mapView.overlays.add(circle)
                        }
                        
                        mapView.invalidate()
                    },
                    modifier = Modifier.fillMaxSize()
                )
                
                // My Location button
                FloatingActionButton(
                    onClick = { viewModel.useCurrentLocationAsSelected() },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    containerColor = SurfaceDark,
                    contentColor = Primary
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "My Location")
                }
                
                // Bottom controls
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            "Tap on the map to select a location",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Radius slider
                        Text(
                            "Radius: ${uiState.radius.toInt()} meters",
                            style = MaterialTheme.typography.titleSmall,
                            color = OnSurface
                        )
                        
                        Slider(
                            value = uiState.radius,
                            onValueChange = { viewModel.setRadius(it) },
                            valueRange = 50f..500f,
                            steps = 8,
                            colors = SliderDefaults.colors(
                                thumbColor = Primary,
                                activeTrackColor = Primary
                            )
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("50m", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Text("500m", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = {
                                uiState.selectedLocation?.let { location ->
                                    onLocationConfirmed(
                                        location.latitude,
                                        location.longitude,
                                        uiState.radius
                                    )
                                }
                            },
                            enabled = uiState.selectedLocation != null,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Confirm Location", modifier = Modifier.padding(8.dp))
                        }
                    }
                }
            }
        }
    }
}
