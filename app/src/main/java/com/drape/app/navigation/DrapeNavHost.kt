package com.drape.app.navigation

import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.drape.app.core.model.DrapeWardrobeStateHolder
import com.drape.app.feature.profile.ProfileScreen
import com.drape.app.feature.style.StyleScreen
import com.drape.app.feature.today.TodayScreen
import com.drape.app.feature.wardrobe.WardrobeScreen

/**
 * Dedicated Jetpack Navigation Compose `NavHost` connecting the Today, Wardrobe, Style,
 * and Profile screens with tactile spring transitions and shared state.
 */
@Composable
fun DrapeNavHost(
    appState: DrapeAppState,
    wardrobeState: DrapeWardrobeStateHolder,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = appState.navController,
        startDestination = TodayDestination.routeKey,
        modifier = modifier,
        enterTransition = { fadeIn(animationSpec = spring()) },
        exitTransition = { fadeOut(animationSpec = spring()) },
        popEnterTransition = { fadeIn(animationSpec = spring()) },
        popExitTransition = { fadeOut(animationSpec = spring()) }
    ) {
        composable(route = TodayDestination.routeKey) {
            TodayScreen(
                garments = wardrobeState.garments,
                outfits = wardrobeState.outfits,
                wearHistory = wardrobeState.wearHistory,
                selectedWeather = wardrobeState.selectedWeather,
                onSelectWeather = wardrobeState::selectWeather,
                onLogOutfitWear = { outfitId -> wardrobeState.logOutfitWear(outfitId) },
                onToggleOutfitFavorite = wardrobeState::toggleOutfitFavorite,
                onClearTodayOutfit = wardrobeState::clearTodayOutfit,
                onAssignOutfitForToday = wardrobeState::scheduleOutfitForToday,
                onNavigateToWardrobe = {
                    appState.navigateToTopLevelDestination(WardrobeDestination)
                },
                onNavigateToStyle = {
                    appState.navigateToTopLevelDestination(StyleDestination)
                },
                onLoadStarterCapsule = wardrobeState::loadStarterCapsule,
                contentPadding = contentPadding
            )
        }

        composable(route = WardrobeDestination.routeKey) {
            WardrobeScreen(
                garments = wardrobeState.garments,
                onAddGarment = { title, brand, category, season, swatchId, localImageUri, notes, size, fabric, purchasePrice, tags ->
                    wardrobeState.addGarment(
                        title = title,
                        brand = brand,
                        category = category,
                        season = season,
                        swatchId = swatchId,
                        localImageUri = localImageUri,
                        notes = notes,
                        size = size,
                        fabric = fabric,
                        purchasePrice = purchasePrice,
                        tags = tags
                    )
                },
                onUpdateGarment = { updated ->
                    wardrobeState.updateGarment(updated)
                },
                onDeleteGarment = wardrobeState::removeGarment,
                onToggleFavorite = wardrobeState::toggleFavorite,
                onIncrementWear = wardrobeState::incrementGarmentWear,
                onDecrementWear = wardrobeState::decrementGarmentWear,
                onLoadStarterCapsule = wardrobeState::loadStarterCapsule,
                onClearWardrobe = wardrobeState::clearAll,
                contentPadding = contentPadding
            )
        }

        composable(route = StyleDestination.routeKey) {
            StyleScreen(
                garments = wardrobeState.garments,
                outfits = wardrobeState.outfits,
                selectedWeather = wardrobeState.selectedWeather,
                stylePreferences = wardrobeState.stylePreferences,
                onCreateOutfit = { title, occasion, garmentIds, rationale, scheduleToday ->
                    wardrobeState.createOutfit(
                        title = title,
                        occasion = occasion,
                        garmentIds = garmentIds,
                        rationale = rationale,
                        scheduleToday = scheduleToday
                    )
                    if (scheduleToday) {
                        appState.navigateToTopLevelDestination(TodayDestination)
                    }
                },
                onScheduleOutfitToday = { outfitId ->
                    wardrobeState.scheduleOutfitForToday(outfitId)
                    appState.navigateToTopLevelDestination(TodayDestination)
                },
                onToggleOutfitFavorite = wardrobeState::toggleOutfitFavorite,
                onLogOutfitWear = { outfitId -> wardrobeState.logOutfitWear(outfitId) },
                onDeleteOutfit = wardrobeState::deleteOutfit,
                onNavigateToWardrobe = {
                    appState.navigateToTopLevelDestination(WardrobeDestination)
                },
                onLoadStarterCapsule = wardrobeState::loadStarterCapsule,
                contentPadding = contentPadding
            )
        }

        composable(route = ProfileDestination.routeKey) {
            ProfileScreen(
                garments = wardrobeState.garments,
                outfits = wardrobeState.outfits,
                wearHistory = wardrobeState.wearHistory,
                stylePreferences = wardrobeState.stylePreferences,
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
                onToggleStylePersona = wardrobeState::toggleStylePersona,
                onTogglePreferredSwatch = wardrobeState::togglePreferredSwatch,
                onSelectDefaultOccasion = wardrobeState::setDefaultOccasion,
                onTogglePrioritizeUnworn = wardrobeState::setPrioritizeUnwornInAi,
                onScheduleOutfitToday = { outfitId ->
                    wardrobeState.scheduleOutfitForToday(outfitId)
                    appState.navigateToTopLevelDestination(TodayDestination)
                },
                onDeleteWearHistoryEntry = wardrobeState::deleteWearHistoryEntry,
                onNavigateToWardrobe = {
                    appState.navigateToTopLevelDestination(WardrobeDestination)
                },
                onLoadStarterCapsule = wardrobeState::loadStarterCapsule,
                onClearWardrobe = wardrobeState::clearAll,
                contentPadding = contentPadding
            )
        }
    }
}
