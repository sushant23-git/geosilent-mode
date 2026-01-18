package com.geosilent.ui.screens

import androidx.lifecycle.ViewModel
import com.geosilent.utils.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    val preferencesManager: PreferencesManager
) : ViewModel()
