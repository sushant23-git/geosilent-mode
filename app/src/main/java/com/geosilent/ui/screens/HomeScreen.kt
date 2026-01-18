package com.geosilent.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.geosilent.data.database.ZoneEntity
import com.geosilent.ui.theme.*
import com.geosilent.ui.viewmodels.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onAddZone: () -> Unit,
    onEditZone: (Long) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<ZoneEntity?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "My Zones",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                actions = {
                    // Automation toggle
                    Switch(
                        checked = uiState.automationEnabled,
                        onCheckedChange = { viewModel.toggleAutomation() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Primary,
                            checkedTrackColor = Primary.copy(alpha = 0.5f)
                        )
                    )
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = OnSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddZone,
                containerColor = Primary,
                contentColor = OnPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Zone")
            }
        },
        containerColor = BackgroundDark
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Primary
                )
            } else if (uiState.zones.isEmpty()) {
                EmptyZonesView(
                    modifier = Modifier.align(Alignment.Center),
                    onAddZone = onAddZone
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.zones,
                        key = { it.id }
                    ) { zone ->
                        ZoneCard(
                            zone = zone,
                            onToggle = { viewModel.toggleZoneEnabled(zone) },
                            onEdit = { onEditZone(zone.id) },
                            onDelete = { showDeleteDialog = zone }
                        )
                    }
                }
            }
        }
    }
    
    // Delete confirmation dialog
    showDeleteDialog?.let { zone ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Zone") },
            text = { Text("Are you sure you want to delete \"${zone.name}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteZone(zone)
                        showDeleteDialog = null
                    }
                ) {
                    Text("Delete", color = Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            },
            containerColor = SurfaceDark
        )
    }
}

@Composable
private fun EmptyZonesView(
    modifier: Modifier = Modifier,
    onAddZone: () -> Unit
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(SurfaceVariantDark),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = TextSecondary
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            "No Zones Yet",
            style = MaterialTheme.typography.titleLarge,
            color = OnBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            "Tap the + button to create your first zone",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onAddZone,
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Zone")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ZoneCard(
    zone: ZoneEntity,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        onClick = onEdit,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceDark
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Zone icon with status indicator
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (zone.isEnabled) GeofenceFill else SurfaceVariantDark
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = if (zone.isEnabled) Primary else TextSecondary
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Zone info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = zone.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = OnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Status badge
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (zone.isEnabled) ZoneActive else ZoneInactive)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (zone.isEnabled) "Active" else "Inactive",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    
                    Text(
                        text = " • ${zone.radius.toInt()}m radius",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                
                // Show enabled actions
                if (zone.enableDND || zone.enableSMS || zone.enableAppLaunch) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        if (zone.enableDND) {
                            ActionChip(text = "DND")
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        if (zone.enableSMS) {
                            ActionChip(text = "SMS")
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        if (zone.enableAppLaunch) {
                            ActionChip(text = "App")
                        }
                    }
                }
            }
            
            // Toggle and delete
            Column(horizontalAlignment = Alignment.End) {
                Switch(
                    checked = zone.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Primary,
                        checkedTrackColor = Primary.copy(alpha = 0.5f)
                    )
                )
                
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Error.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionChip(text: String) {
    Surface(
        color = Primary.copy(alpha = 0.2f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = Primary,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
