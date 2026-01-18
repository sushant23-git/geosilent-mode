package com.geosilent.service

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.geosilent.data.database.ZoneEntity
import com.geosilent.data.repository.ZoneRepository
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages geofence registration and updates.
 * Uses Google Play Services Geofencing API for reliable background detection.
 */
@Singleton
class GeofenceManager @Inject constructor(
    private val context: Context,
    private val zoneRepository: ZoneRepository
) {
    
    companion object {
        private const val TAG = "GeofenceManager"
        private const val GEOFENCE_EXPIRATION = Geofence.NEVER_EXPIRE
        private const val LOITERING_DELAY_MS = 30000 // 30 seconds
    }
    
    private val geofencingClient: GeofencingClient by lazy {
        LocationServices.getGeofencingClient(context)
    }
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }
    
    /**
     * Register all enabled zones as geofences.
     */
    fun registerAllGeofences() {
        scope.launch {
            try {
                val zones = zoneRepository.getEnabledZonesSync()
                if (zones.isEmpty()) {
                    Log.d(TAG, "No enabled zones to register")
                    return@launch
                }
                
                registerGeofences(zones)
            } catch (e: Exception) {
                Log.e(TAG, "Error registering geofences", e)
            }
        }
    }
    
    /**
     * Register specific zones as geofences.
     */
    private fun registerGeofences(zones: List<ZoneEntity>) {
        if (!hasLocationPermission()) {
            Log.w(TAG, "Location permission not granted")
            return
        }
        
        val geofences = zones.map { zone ->
            Geofence.Builder()
                .setRequestId(zone.id.toString())
                .setCircularRegion(zone.latitude, zone.longitude, zone.radius)
                .setExpirationDuration(GEOFENCE_EXPIRATION)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .setLoiteringDelay(LOITERING_DELAY_MS)
                .build()
        }
        
        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofences(geofences)
            .build()
        
        try {
            geofencingClient.addGeofences(request, geofencePendingIntent)
                .addOnSuccessListener {
                    Log.d(TAG, "Registered ${zones.size} geofences")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to register geofences", e)
                }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException when registering geofences", e)
        }
    }
    
    /**
     * Unregister all geofences.
     */
    fun unregisterAllGeofences() {
        geofencingClient.removeGeofences(geofencePendingIntent)
            .addOnSuccessListener {
                Log.d(TAG, "All geofences removed")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to remove geofences", e)
            }
    }
    
    /**
     * Unregister a specific zone's geofence.
     */
    fun unregisterGeofence(zoneId: Long) {
        geofencingClient.removeGeofences(listOf(zoneId.toString()))
            .addOnSuccessListener {
                Log.d(TAG, "Geofence $zoneId removed")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to remove geofence $zoneId", e)
            }
    }
    
    /**
     * Refresh geofences - removes all and re-registers enabled zones.
     */
    fun refreshGeofences() {
        unregisterAllGeofences()
        registerAllGeofences()
    }
    
    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}
