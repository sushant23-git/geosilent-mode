package com.geosilent.ui.viewmodels

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geosilent.data.database.ZoneEntity
import com.geosilent.data.repository.ZoneRepository
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MapUiState(
    val selectedLocation: LatLng? = null,
    val radius: Float = 100f,
    val currentLocation: LatLng? = null,
    val existingZone: ZoneEntity? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val zoneRepository: ZoneRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()
    
    companion object {
        const val MIN_RADIUS = 50f
        const val MAX_RADIUS = 500f
        const val DEFAULT_RADIUS = 100f
    }
    
    fun loadExistingZone(zoneId: Long?) {
        if (zoneId == null || zoneId == -1L) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val zone = zoneRepository.getZoneById(zoneId)
            if (zone != null) {
                _uiState.update {
                    it.copy(
                        selectedLocation = LatLng(zone.latitude, zone.longitude),
                        radius = zone.radius,
                        existingZone = zone,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    
    fun setSelectedLocation(latLng: LatLng) {
        _uiState.update { it.copy(selectedLocation = latLng) }
    }
    
    fun setRadius(radius: Float) {
        val clampedRadius = radius.coerceIn(MIN_RADIUS, MAX_RADIUS)
        _uiState.update { it.copy(radius = clampedRadius) }
    }
    
    fun setCurrentLocation(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        _uiState.update { it.copy(currentLocation = latLng) }
        
        // Auto-select current location if no location is selected
        if (_uiState.value.selectedLocation == null) {
            setSelectedLocation(latLng)
        }
    }
    
    fun useCurrentLocationAsSelected() {
        _uiState.value.currentLocation?.let { currentLocation ->
            setSelectedLocation(currentLocation)
        }
    }
}
