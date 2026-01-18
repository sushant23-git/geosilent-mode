package com.geosilent.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a geofence zone.
 * Each zone has a location, radius, and configurable actions.
 */
@Entity(tableName = "zones")
data class ZoneEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // Zone identification
    val name: String,
    
    // Location coordinates
    val latitude: Double,
    val longitude: Double,
    
    // Geofence radius in meters (50-500m)
    val radius: Float = 100f,
    
    // Zone state
    val isEnabled: Boolean = true,
    
    // Actions - Silent mode is always enabled
    val enableSilentMode: Boolean = true,  // Always true, mandatory
    val enableDND: Boolean = false,
    val enableSMS: Boolean = false,
    val smsMessage: String = "",
    val smsRecipient: String = "",
    val enableAppLaunch: Boolean = false,
    val launchAppPackage: String = "",
    val launchAppName: String = "",
    
    // Metadata
    val createdAt: Long = System.currentTimeMillis(),
    val lastTriggeredAt: Long? = null
)
