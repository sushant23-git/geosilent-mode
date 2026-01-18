package com.geosilent.ui.screens

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.geosilent.ui.theme.*
import com.geosilent.ui.viewmodels.ZoneSetupViewModel
import com.geosilent.utils.PermissionHelper
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ZoneSetupScreen(
    viewModel: ZoneSetupViewModel,
    latitude: Double,
    longitude: Double,
    radius: Float,
    zoneId: Long?,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    
    val smsPermissionState = rememberPermissionState(Manifest.permission.SEND_SMS)
    
    // Load existing zone if editing
    LaunchedEffect(zoneId) {
        viewModel.loadExistingZone(zoneId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (zoneId != null) "Edit Zone" else "New Zone") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Zone name input
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.setName(it) },
                label = { Text("Zone Name") },
                placeholder = { Text("e.g., Office, Home, Library") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = SurfaceVariantDark,
                    focusedLabelColor = Primary
                )
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Location info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Location Selected",
                            style = MaterialTheme.typography.titleSmall,
                            color = OnSurface
                        )
                        Text(
                            "Lat: ${String.format("%.4f", latitude)}, Lng: ${String.format("%.4f", longitude)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Text(
                            "Radius: ${radius.toInt()} meters",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Actions section
            Text(
                "Actions on Entry",
                style = MaterialTheme.typography.titleMedium,
                color = OnBackground
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Silent mode (always on)
            ActionToggleCard(
                icon = Icons.Default.VolumeOff,
                title = "Silent Mode",
                description = "Automatically enabled for all zones",
                checked = true,
                onCheckedChange = { },
                enabled = false
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // DND mode
            ActionToggleCard(
                icon = Icons.Default.DoNotDisturb,
                title = "Do Not Disturb",
                description = "Enable Focus/DND mode",
                checked = uiState.enableDND,
                onCheckedChange = { viewModel.setEnableDND(it) },
                extraContent = if (uiState.enableDND && !PermissionHelper.hasDndPermission(context)) {
                    {
                        Button(
                            onClick = { PermissionHelper.openDndSettings(context) },
                            colors = ButtonDefaults.buttonColors(containerColor = Warning),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Grant DND Access", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                } else null
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // SMS
            ActionToggleCard(
                icon = Icons.Default.Sms,
                title = "Send SMS",
                description = "Send a message when entering zone",
                checked = uiState.enableSMS,
                onCheckedChange = { viewModel.setEnableSMS(it) },
                extraContent = if (uiState.enableSMS) {
                    {
                        Column {
                            if (!smsPermissionState.status.isGranted) {
                                Button(
                                    onClick = { smsPermissionState.launchPermissionRequest() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Warning),
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    Text("Grant SMS Permission", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            OutlinedTextField(
                                value = uiState.smsRecipient,
                                onValueChange = { viewModel.setSmsRecipient(it) },
                                label = { Text("Recipient Number") },
                                placeholder = { Text("+91 9876543210") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Primary,
                                    unfocusedBorderColor = SurfaceVariantDark
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            OutlinedTextField(
                                value = uiState.smsMessage,
                                onValueChange = { viewModel.setSmsMessage(it) },
                                label = { Text("Message") },
                                placeholder = { Text("I have reached") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Primary,
                                    unfocusedBorderColor = SurfaceVariantDark
                                )
                            )
                        }
                    }
                } else null
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // App launch
            ActionToggleCard(
                icon = Icons.Default.OpenInNew,
                title = "Launch App",
                description = "Open a specific app on entry",
                checked = uiState.enableAppLaunch,
                onCheckedChange = { viewModel.setEnableAppLaunch(it) },
                extraContent = if (uiState.enableAppLaunch) {
                    {
                        Text(
                            "App selection coming soon",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                } else null
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Save button
            Button(
                onClick = {
                    viewModel.saveZone(latitude, longitude, radius, onSave)
                },
                enabled = !uiState.isSaving,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = OnPrimary
                    )
                } else {
                    Text("Save Zone", modifier = Modifier.padding(8.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ActionToggleCard(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    extraContent: (@Composable () -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (checked) Primary.copy(alpha = 0.1f) else SurfaceDark
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (checked) Primary else TextSecondary
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleSmall,
                        color = OnSurface
                    )
                    Text(
                        description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                
                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    enabled = enabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Primary,
                        checkedTrackColor = Primary.copy(alpha = 0.5f)
                    )
                )
            }
            
            extraContent?.invoke()
        }
    }
}
