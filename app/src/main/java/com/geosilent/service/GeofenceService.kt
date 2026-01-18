package com.geosilent.service

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.geosilent.GeoSilentApp
import com.geosilent.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Foreground service for geofence monitoring.
 * Required for reliable background location on Android 10+.
 */
@AndroidEntryPoint
class GeofenceService : Service() {
    
    companion object {
        private const val NOTIFICATION_ID = 1001
    }
    
    @Inject
    lateinit var geofenceManager: GeofenceManager
    
    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, createNotification())
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        geofenceManager.registerAllGeofences()
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        geofenceManager.unregisterAllGeofences()
    }
    
    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, GeoSilentApp.NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("Monitoring zones in background")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }
}
