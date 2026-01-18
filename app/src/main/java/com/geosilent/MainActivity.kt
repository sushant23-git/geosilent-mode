package com.geosilent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.geosilent.service.GeofenceManager
import com.geosilent.ui.navigation.NavGraph
import com.geosilent.ui.theme.BackgroundDark
import com.geosilent.ui.theme.GeoSilentModeTheme
import com.geosilent.utils.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var preferencesManager: PreferencesManager
    
    @Inject
    lateinit var geofenceManager: GeofenceManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Register geofences on app start
        geofenceManager.registerAllGeofences()
        
        setContent {
            GeoSilentModeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BackgroundDark
                ) {
                    val navController = rememberNavController()
                    NavGraph(
                        navController = navController,
                        preferencesManager = preferencesManager
                    )
                }
            }
        }
    }
}
