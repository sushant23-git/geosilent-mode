package com.geosilent.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geosilent.data.database.ZoneEntity
import com.geosilent.data.repository.ZoneRepository
import com.geosilent.service.GeofenceManager
import com.geosilent.utils.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val zones: List<ZoneEntity> = emptyList(),
    val isLoading: Boolean = true,
    val automationEnabled: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val zoneRepository: ZoneRepository,
    private val preferencesManager: PreferencesManager,
    private val geofenceManager: GeofenceManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadZones()
        observeAutomationState()
    }
    
    private fun loadZones() {
        viewModelScope.launch {
            zoneRepository.getAllZones()
                .collect { zones ->
                    _uiState.update { it.copy(zones = zones, isLoading = false) }
                }
        }
    }
    
    private fun observeAutomationState() {
        viewModelScope.launch {
            preferencesManager.automationEnabled
                .collect { enabled ->
                    _uiState.update { it.copy(automationEnabled = enabled) }
                }
        }
    }
    
    fun toggleZoneEnabled(zone: ZoneEntity) {
        viewModelScope.launch {
            zoneRepository.setZoneEnabled(zone.id, !zone.isEnabled)
            geofenceManager.refreshGeofences()
        }
    }
    
    fun deleteZone(zone: ZoneEntity) {
        viewModelScope.launch {
            zoneRepository.deleteZone(zone)
            geofenceManager.unregisterGeofence(zone.id)
        }
    }
    
    fun toggleAutomation() {
        viewModelScope.launch {
            val currentState = _uiState.value.automationEnabled
            preferencesManager.setAutomationEnabled(!currentState)
            
            if (!currentState) {
                geofenceManager.registerAllGeofences()
            } else {
                geofenceManager.unregisterAllGeofences()
            }
        }
    }
}
