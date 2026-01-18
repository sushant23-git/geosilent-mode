package com.geosilent.ui.screens

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.geosilent.BuildConfig
import com.geosilent.ui.theme.*
import com.geosilent.ui.viewmodels.SettingsViewModel
import com.geosilent.utils.PermissionHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
            // Automation Section
            Text(
                "Automation",
                style = MaterialTheme.typography.titleMedium,
                color = Primary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            SettingsCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = if (uiState.automationEnabled) Success else TextSecondary
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Automation Enabled",
                            style = MaterialTheme.typography.titleSmall,
                            color = OnSurface
                        )
                        Text(
                            "Master toggle for all zones",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    
                    Switch(
                        checked = uiState.automationEnabled,
                        onCheckedChange = { viewModel.setAutomationEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Primary,
                            checkedTrackColor = Primary.copy(alpha = 0.5f)
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Defaults Section
            Text(
                "Defaults",
                style = MaterialTheme.typography.titleMedium,
                color = Primary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            SettingsCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Sms,
                            contentDescription = null,
                            tint = Primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Default SMS Message",
                            style = MaterialTheme.typography.titleSmall,
                            color = OnSurface
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = uiState.defaultSmsMessage,
                        onValueChange = { viewModel.setDefaultSmsMessage(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("I have reached") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = SurfaceVariantDark
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Battery Section
            Text(
                "Battery",
                style = MaterialTheme.typography.titleMedium,
                color = Primary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            SettingsCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.BatteryChargingFull,
                            contentDescription = null,
                            tint = Warning
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Battery Optimization",
                                style = MaterialTheme.typography.titleSmall,
                                color = OnSurface
                            )
                            Text(
                                "Disable battery optimization for reliable background operation",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = { PermissionHelper.openBatteryOptimizationSettings(context) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark)
                    ) {
                        Text("Open Battery Settings")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Permissions Section
            Text(
                "Permissions",
                style = MaterialTheme.typography.titleMedium,
                color = Primary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            SettingsCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    PermissionItem(
                        title = "Location",
                        granted = PermissionHelper.hasLocationPermission(context),
                        onClick = { PermissionHelper.openAppSettings(context) }
                    )
                    
                    HorizontalDivider(color = SurfaceVariantDark)
                    
                    PermissionItem(
                        title = "Background Location",
                        granted = PermissionHelper.hasBackgroundLocationPermission(context),
                        onClick = { PermissionHelper.openAppSettings(context) }
                    )
                    
                    HorizontalDivider(color = SurfaceVariantDark)
                    
                    PermissionItem(
                        title = "Do Not Disturb",
                        granted = PermissionHelper.hasDndPermission(context),
                        onClick = { PermissionHelper.openDndSettings(context) }
                    )
                    
                    HorizontalDivider(color = SurfaceVariantDark)
                    
                    PermissionItem(
                        title = "SMS",
                        granted = PermissionHelper.hasSmsPermission(context),
                        onClick = { PermissionHelper.openAppSettings(context) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // About Section
            Text(
                "About",
                style = MaterialTheme.typography.titleMedium,
                color = Primary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            SettingsCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        "Geo Silent Mode",
                        style = MaterialTheme.typography.titleLarge,
                        color = OnSurface
                    )
                    
                    Text(
                        "Version ${BuildConfig.VERSION_NAME}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        "Automate your phone based on location",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        content()
    }
}

@Composable
private fun PermissionItem(
    title: String,
    granted: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (granted) Icons.Default.CheckCircle else Icons.Default.Warning,
            contentDescription = null,
            tint = if (granted) Success else Warning
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            title,
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurface,
            modifier = Modifier.weight(1f)
        )
        
        if (!granted) {
            TextButton(onClick = onClick) {
                Text("Grant", color = Primary)
            }
        }
    }
}
