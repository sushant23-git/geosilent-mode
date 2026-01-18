package com.geosilent.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geosilent.data.database.ZoneEntity
import com.geosilent.data.repository.ZoneRepository
import com.geosilent.service.GeofenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ZoneSetupUiState(
    val name: String = "",
    val enableDND: Boolean = false,
    val enableSMS: Boolean = false,
    val smsMessage: String = "",
    val smsRecipient: String = "",
    val enableAppLaunch: Boolean = false,
    val appPackage: String = "",
    val appName: String = "",
    val isSaving: Boolean = false,
    val existingZone: ZoneEntity? = null
)

@HiltViewModel
class ZoneSetupViewModel @Inject constructor(
    private val zoneRepository: ZoneRepository,
    private val geofenceManager: GeofenceManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ZoneSetupUiState())
    val uiState: StateFlow<ZoneSetupUiState> = _uiState.asStateFlow()
    
    fun loadExistingZone(zoneId: Long?) {
        if (zoneId == null || zoneId == -1L) return
        
        viewModelScope.launch {
            val zone = zoneRepository.getZoneById(zoneId)
            if (zone != null) {
                _uiState.update {
                    it.copy(
                        name = zone.name,
                        enableDND = zone.enableDND,
                        enableSMS = zone.enableSMS,
                        smsMessage = zone.smsMessage,
                        smsRecipient = zone.smsRecipient,
                        enableAppLaunch = zone.enableAppLaunch,
                        appPackage = zone.launchAppPackage,
                        appName = zone.launchAppName,
                        existingZone = zone
                    )
                }
            }
        }
    }
    
    fun setName(name: String) {
        _uiState.update { it.copy(name = name) }
    }
    
    fun setEnableDND(enabled: Boolean) {
        _uiState.update { it.copy(enableDND = enabled) }
    }
    
    fun setEnableSMS(enabled: Boolean) {
        _uiState.update { it.copy(enableSMS = enabled) }
    }
    
    fun setSmsMessage(message: String) {
        _uiState.update { it.copy(smsMessage = message) }
    }
    
    fun setSmsRecipient(recipient: String) {
        _uiState.update { it.copy(smsRecipient = recipient) }
    }
    
    fun setEnableAppLaunch(enabled: Boolean) {
        _uiState.update { it.copy(enableAppLaunch = enabled) }
    }
    
    fun setLaunchApp(packageName: String, appName: String) {
        _uiState.update { it.copy(appPackage = packageName, appName = appName) }
    }
    
    fun saveZone(
        latitude: Double,
        longitude: Double,
        radius: Float,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            
            val state = _uiState.value
            val zone = ZoneEntity(
                id = state.existingZone?.id ?: 0,
                name = state.name.ifBlank { "Zone" },
                latitude = latitude,
                longitude = longitude,
                radius = radius,
                isEnabled = true,
                enableSilentMode = true,
                enableDND = state.enableDND,
                enableSMS = state.enableSMS,
                smsMessage = state.smsMessage,
                smsRecipient = state.smsRecipient,
                enableAppLaunch = state.enableAppLaunch,
                launchAppPackage = state.appPackage,
                launchAppName = state.appName,
                createdAt = state.existingZone?.createdAt ?: System.currentTimeMillis()
            )
            
            if (state.existingZone != null) {
                zoneRepository.updateZone(zone)
            } else {
                zoneRepository.createZone(zone)
            }
            
            geofenceManager.refreshGeofences()
            
            _uiState.update { it.copy(isSaving = false) }
            onSuccess()
        }
    }
    
    fun isValid(): Boolean {
        return _uiState.value.name.isNotBlank() || true // Allow empty name, will use default
    }
}
