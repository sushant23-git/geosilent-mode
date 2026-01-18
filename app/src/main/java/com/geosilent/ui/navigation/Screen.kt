package com.geosilent.ui.navigation

/**
 * Navigation routes for the app.
 */
sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Map : Screen("map?zoneId={zoneId}") {
        fun createRoute(zoneId: Long? = null): String {
            return if (zoneId != null) "map?zoneId=$zoneId" else "map"
        }
    }
    object ZoneSetup : Screen("zone_setup/{latitude}/{longitude}/{radius}?zoneId={zoneId}") {
        fun createRoute(
            latitude: Double,
            longitude: Double,
            radius: Float,
            zoneId: Long? = null
        ): String {
            val baseRoute = "zone_setup/$latitude/$longitude/$radius"
            return if (zoneId != null) "$baseRoute?zoneId=$zoneId" else baseRoute
        }
    }
    object Settings : Screen("settings")
}
