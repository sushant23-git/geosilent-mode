package com.geosilent.service.actions

import android.content.Context
import android.media.AudioManager
import android.app.NotificationManager
import android.os.Build
import android.telephony.SmsManager
import android.content.Intent
import android.content.pm.PackageManager
import com.geosilent.data.database.ZoneEntity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Executes configured actions when a zone is entered.
 */
@Singleton
class ActionExecutor @Inject constructor(
    private val context: Context
) {
    
    private val audioManager: AudioManager by lazy {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
    
    private val notificationManager: NotificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
    
    /**
     * Execute all enabled actions for a zone.
     */
    fun executeActions(zone: ZoneEntity) {
        // Silent mode is always executed
        enableSilentMode()
        
        // Optional actions
        if (zone.enableDND) {
            enableDoNotDisturb()
        }
        
        if (zone.enableSMS && zone.smsRecipient.isNotBlank()) {
            sendSms(zone.smsRecipient, zone.smsMessage.ifBlank { "I have reached" })
        }
        
        if (zone.enableAppLaunch && zone.launchAppPackage.isNotBlank()) {
            launchApp(zone.launchAppPackage)
        }
    }
    
    /**
     * Enable silent mode (ringer mode silent).
     */
    private fun enableSilentMode() {
        try {
            if (notificationManager.isNotificationPolicyAccessGranted) {
                audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Enable Do Not Disturb mode.
     */
    private fun enableDoNotDisturb() {
        try {
            if (notificationManager.isNotificationPolicyAccessGranted) {
                notificationManager.setInterruptionFilter(
                    NotificationManager.INTERRUPTION_FILTER_PRIORITY
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Send an SMS to the specified recipient.
     */
    private fun sendSms(recipient: String, message: String) {
        try {
            if (context.checkSelfPermission(android.Manifest.permission.SEND_SMS) 
                == PackageManager.PERMISSION_GRANTED) {
                val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    context.getSystemService(SmsManager::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    SmsManager.getDefault()
                }
                smsManager?.sendTextMessage(recipient, null, message, null, null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Launch the specified app.
     */
    private fun launchApp(packageName: String) {
        try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Restore normal ringer mode.
     */
    fun restoreNormalMode() {
        try {
            if (notificationManager.isNotificationPolicyAccessGranted) {
                audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                notificationManager.setInterruptionFilter(
                    NotificationManager.INTERRUPTION_FILTER_ALL
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
