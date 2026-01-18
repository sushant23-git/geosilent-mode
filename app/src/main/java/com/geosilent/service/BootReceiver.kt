package com.geosilent.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.geosilent.data.database.AppDatabase
import com.geosilent.data.repository.ZoneRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Receiver for device boot events.
 * Re-registers geofences after device restart.
 */
class BootReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "BootReceiver"
    }
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Device booted, re-registering geofences")
            
            scope.launch {
                try {
                    val database = AppDatabase.getInstance(context)
                    val repository = ZoneRepository(database.zoneDao())
                    val geofenceManager = GeofenceManager(context, repository)
                    
                    geofenceManager.registerAllGeofences()
                } catch (e: Exception) {
                    Log.e(TAG, "Error re-registering geofences on boot", e)
                }
            }
        }
    }
}
