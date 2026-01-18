package com.geosilent.data.repository

import com.geosilent.data.database.ZoneDao
import com.geosilent.data.database.ZoneEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for zone data operations.
 * Acts as a single source of truth for zone data.
 */
@Singleton
class ZoneRepository @Inject constructor(
    private val zoneDao: ZoneDao
) {
    
    /**
     * Get all zones as a Flow for reactive UI.
     */
    fun getAllZones(): Flow<List<ZoneEntity>> = zoneDao.getAllZones()
    
    /**
     * Get only enabled zones as a Flow.
     */
    fun getEnabledZones(): Flow<List<ZoneEntity>> = zoneDao.getEnabledZones()
    
    /**
     * Get enabled zones synchronously (for geofencing service).
     */
    suspend fun getEnabledZonesSync(): List<ZoneEntity> = zoneDao.getEnabledZonesSync()
    
    /**
     * Get a specific zone by ID.
     */
    suspend fun getZoneById(zoneId: Long): ZoneEntity? = zoneDao.getZoneById(zoneId)
    
    /**
     * Get a zone by ID as a Flow.
     */
    fun getZoneByIdFlow(zoneId: Long): Flow<ZoneEntity?> = zoneDao.getZoneByIdFlow(zoneId)
    
    /**
     * Create a new zone.
     */
    suspend fun createZone(zone: ZoneEntity): Long = zoneDao.insertZone(zone)
    
    /**
     * Update an existing zone.
     */
    suspend fun updateZone(zone: ZoneEntity) = zoneDao.updateZone(zone)
    
    /**
     * Delete a zone.
     */
    suspend fun deleteZone(zone: ZoneEntity) = zoneDao.deleteZone(zone)
    
    /**
     * Delete a zone by ID.
     */
    suspend fun deleteZoneById(zoneId: Long) = zoneDao.deleteZoneById(zoneId)
    
    /**
     * Enable or disable a zone.
     */
    suspend fun setZoneEnabled(zoneId: Long, enabled: Boolean) = 
        zoneDao.setZoneEnabled(zoneId, enabled)
    
    /**
     * Update the last triggered timestamp.
     */
    suspend fun updateLastTriggered(zoneId: Long) = 
        zoneDao.updateLastTriggered(zoneId, System.currentTimeMillis())
    
    /**
     * Get the total number of zones.
     */
    suspend fun getZoneCount(): Int = zoneDao.getZoneCount()
}
