package com.geosilent.di

import android.content.Context
import com.geosilent.data.database.AppDatabase
import com.geosilent.data.database.ZoneDao
import com.geosilent.data.repository.ZoneRepository
import com.geosilent.service.GeofenceManager
import com.geosilent.service.actions.ActionExecutor
import com.geosilent.utils.PreferencesManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing app-wide dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return AppDatabase.getInstance(context)
    }
    
    @Provides
    @Singleton
    fun provideZoneDao(database: AppDatabase): ZoneDao {
        return database.zoneDao()
    }
    
    @Provides
    @Singleton
    fun provideZoneRepository(zoneDao: ZoneDao): ZoneRepository {
        return ZoneRepository(zoneDao)
    }
    
    @Provides
    @Singleton
    fun providePreferencesManager(
        @ApplicationContext context: Context
    ): PreferencesManager {
        return PreferencesManager(context)
    }
    
    @Provides
    @Singleton
    fun provideGeofenceManager(
        @ApplicationContext context: Context,
        zoneRepository: ZoneRepository
    ): GeofenceManager {
        return GeofenceManager(context, zoneRepository)
    }
    
    @Provides
    @Singleton
    fun provideActionExecutor(
        @ApplicationContext context: Context
    ): ActionExecutor {
        return ActionExecutor(context)
    }
}
