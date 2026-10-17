package com.drape.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

/**
 * Navigation state controller for DRAPE's 4-tab architecture (Today, Wardrobe, Style, Profile).
 * Preserves each screen's scroll position and UI state across bottom-bar tab switches.
 */
@Stable
class DrapeAppState(
    val navController: NavHostController
) {
    val topLevelDestinations: List<DrapeTopLevelDestination> = DrapeMainTabDestinations

    val currentDestination: NavDestination?
        @Composable get() = navController.currentBackStackEntryAsState().value?.destination

    val currentTopLevelDestination: DrapeTopLevelDestination
        @Composable get() {
            val route = currentDestination?.route.orEmpty()
            return when {
                route == WardrobeDestination.routeKey || route.contains("WardrobeDestination") -> WardrobeDestination
                route == StyleDestination.routeKey || route.contains("StyleDestination") -> StyleDestination
                route == ProfileDestination.routeKey || route.contains("ProfileDestination") -> ProfileDestination
                else -> TodayDestination
            }
        }

    fun navigateToTopLevelDestination(destination: DrapeTopLevelDestination) {
        navController.navigate(destination.routeKey) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }
}

@Composable
fun rememberDrapeAppState(
    navController: NavHostController = rememberNavController()
): DrapeAppState = remember(navController) {
    DrapeAppState(navController = navController)
}
