package com.geosilent.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.geosilent.ui.screens.*
import com.geosilent.utils.PreferencesManager
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Main navigation graph for the app.
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    preferencesManager: PreferencesManager
) {
    val onboardingCompleted by preferencesManager.onboardingCompleted.collectAsState(initial = true)
    
    val startDestination = if (onboardingCompleted) Screen.Home.route else Screen.Onboarding.route
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Onboarding
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Home
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = hiltViewModel(),
                onAddZone = {
                    navController.navigate(Screen.Map.createRoute())
                },
                onEditZone = { zoneId ->
                    navController.navigate(Screen.Map.createRoute(zoneId))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        
        // Map
        composable(
            route = Screen.Map.route,
            arguments = listOf(
                navArgument("zoneId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val zoneId = backStackEntry.arguments?.getLong("zoneId") ?: -1L
            MapScreen(
                viewModel = hiltViewModel(),
                zoneId = if (zoneId == -1L) null else zoneId,
                onLocationConfirmed = { lat, lng, radius ->
                    navController.navigate(
                        Screen.ZoneSetup.createRoute(
                            latitude = lat,
                            longitude = lng,
                            radius = radius,
                            zoneId = if (zoneId == -1L) null else zoneId
                        )
                    )
                },
                onBack = { navController.popBackStack() }
            )
        }
        
        // Zone Setup
        composable(
            route = Screen.ZoneSetup.route,
            arguments = listOf(
                navArgument("latitude") { type = NavType.FloatType },
                navArgument("longitude") { type = NavType.FloatType },
                navArgument("radius") { type = NavType.FloatType },
                navArgument("zoneId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val latitude = backStackEntry.arguments?.getFloat("latitude")?.toDouble() ?: 0.0
            val longitude = backStackEntry.arguments?.getFloat("longitude")?.toDouble() ?: 0.0
            val radius = backStackEntry.arguments?.getFloat("radius") ?: 100f
            val zoneId = backStackEntry.arguments?.getLong("zoneId") ?: -1L
            
            ZoneSetupScreen(
                viewModel = hiltViewModel(),
                latitude = latitude,
                longitude = longitude,
                radius = radius,
                zoneId = if (zoneId == -1L) null else zoneId,
                onSave = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
        
        // Settings
        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = hiltViewModel(),
                onBack = { navController.popBackStack() }
            )
        }
    }
}
