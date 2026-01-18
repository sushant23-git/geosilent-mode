package com.geosilent.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.geosilent.data.database.AppDatabase
import com.geosilent.service.actions.ActionExecutor
import com.geosilent.utils.PreferencesManager
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

/**
 * Receives geofence transition events from the system.
 * Triggers appropriate actions when user enters a zone.
 */
class GeofenceBroadcastReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "GeofenceReceiver"
    }
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        
        if (geofencingEvent == null) {
            Log.e(TAG, "GeofencingEvent is null")
            return
        }
        
        if (geofencingEvent.hasError()) {
            Log.e(TAG, "Geofencing error: ${geofencingEvent.errorCode}")
            return
        }
        
        val transitionType = geofencingEvent.geofenceTransition
        
        if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) {
            val triggeringGeofences = geofencingEvent.triggeringGeofences ?: return
            
            scope.launch {
                handleGeofenceEntry(context, triggeringGeofences)
            }
        }
    }
    
    private suspend fun handleGeofenceEntry(context: Context, geofences: List<Geofence>) {
        val preferencesManager = PreferencesManager(context)
        
        // Check if automation is globally enabled
        val automationEnabled = preferencesManager.automationEnabled.first()
        if (!automationEnabled) {
            Log.d(TAG, "Automation is disabled, skipping actions")
            return
        }
        
        val database = AppDatabase.getInstance(context)
        val zoneDao = database.zoneDao()
        val actionExecutor = ActionExecutor(context)
        
        for (geofence in geofences) {
            try {
                val zoneId = geofence.requestId.toLongOrNull() ?: continue
                val zone = zoneDao.getZoneById(zoneId)
                
                if (zone != null && zone.isEnabled) {
                    Log.d(TAG, "Entering zone: ${zone.name}")
                    
                    // Execute actions
                    actionExecutor.executeActions(zone)
                    
                    // Update last triggered timestamp
                    zoneDao.updateLastTriggered(zoneId, System.currentTimeMillis())
                    
                    // Show notification
                    showZoneEnteredNotification(context, zone.name)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling geofence entry", e)
            }
        }
    }
    
    private fun showZoneEnteredNotification(context: Context, zoneName: String) {
        // Notification is optional - could add a notification here
        Log.d(TAG, "Zone entered notification for: $zoneName")
    }
}
