package com.geosilent.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geosilent.utils.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val automationEnabled: Boolean = true,
    val defaultSmsMessage: String = "I have reached"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            combine(
                preferencesManager.automationEnabled,
                preferencesManager.defaultSmsMessage
            ) { automationEnabled, defaultMessage ->
                SettingsUiState(
                    automationEnabled = automationEnabled,
                    defaultSmsMessage = defaultMessage
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
    
    fun setAutomationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setAutomationEnabled(enabled)
        }
    }
    
    fun setDefaultSmsMessage(message: String) {
        viewModelScope.launch {
            preferencesManager.setDefaultSmsMessage(message)
        }
    }
}
