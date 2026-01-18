package com.geosilent.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Zone operations.
 * Uses Flow for reactive UI updates.
 */
@Dao
interface ZoneDao {
    
    @Query("SELECT * FROM zones ORDER BY createdAt DESC")
    fun getAllZones(): Flow<List<ZoneEntity>>
    
    @Query("SELECT * FROM zones WHERE isEnabled = 1")
    fun getEnabledZones(): Flow<List<ZoneEntity>>
    
    @Query("SELECT * FROM zones WHERE isEnabled = 1")
    suspend fun getEnabledZonesSync(): List<ZoneEntity>
    
    @Query("SELECT * FROM zones WHERE id = :zoneId")
    suspend fun getZoneById(zoneId: Long): ZoneEntity?
    
    @Query("SELECT * FROM zones WHERE id = :zoneId")
    fun getZoneByIdFlow(zoneId: Long): Flow<ZoneEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertZone(zone: ZoneEntity): Long
    
    @Update
    suspend fun updateZone(zone: ZoneEntity)
    
    @Delete
    suspend fun deleteZone(zone: ZoneEntity)
    
    @Query("DELETE FROM zones WHERE id = :zoneId")
    suspend fun deleteZoneById(zoneId: Long)
    
    @Query("UPDATE zones SET isEnabled = :enabled WHERE id = :zoneId")
    suspend fun setZoneEnabled(zoneId: Long, enabled: Boolean)
    
    @Query("UPDATE zones SET lastTriggeredAt = :timestamp WHERE id = :zoneId")
    suspend fun updateLastTriggered(zoneId: Long, timestamp: Long)
    
    @Query("SELECT COUNT(*) FROM zones")
    suspend fun getZoneCount(): Int
}
