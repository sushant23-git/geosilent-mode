package com.geosilent.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Manages app preferences using DataStore.
 */
@Singleton
class PreferencesManager @Inject constructor(
    private val context: Context
) {
    
    private object Keys {
        val AUTOMATION_ENABLED = booleanPreferencesKey("automation_enabled")
        val DEFAULT_SMS_MESSAGE = stringPreferencesKey("default_sms_message")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }
    
    /**
     * Whether automation is globally enabled.
     */
    val automationEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[Keys.AUTOMATION_ENABLED] ?: true
        }
    
    suspend fun setAutomationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.AUTOMATION_ENABLED] = enabled
        }
    }
    
    /**
     * Default SMS message template.
     */
    val defaultSmsMessage: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[Keys.DEFAULT_SMS_MESSAGE] ?: "I have reached"
        }
    
    suspend fun setDefaultSmsMessage(message: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.DEFAULT_SMS_MESSAGE] = message
        }
    }
    
    /**
     * Whether onboarding has been completed.
     */
    val onboardingCompleted: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[Keys.ONBOARDING_COMPLETED] ?: false
        }
    
    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.ONBOARDING_COMPLETED] = completed
        }
    }
    
    /**
     * Get automation enabled state synchronously.
     */
    suspend fun isAutomationEnabled(): Boolean {
        var enabled = true
        context.dataStore.edit { preferences ->
            enabled = preferences[Keys.AUTOMATION_ENABLED] ?: true
        }
        return enabled
    }
}
