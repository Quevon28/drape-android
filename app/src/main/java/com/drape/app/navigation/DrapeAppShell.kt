package com.drape.app.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.drape.app.R
import com.drape.app.core.designsystem.component.LiquidGlassBackdrop
import com.drape.app.core.designsystem.component.LiquidGlassBanner
import com.drape.app.core.designsystem.component.LiquidGlassBottomBar
import com.drape.app.core.designsystem.component.LiquidGlassIconButton
import com.drape.app.core.designsystem.component.LiquidGlassNavTab
import com.drape.app.core.designsystem.component.LiquidGlassTopBar
import com.drape.app.core.designsystem.component.rememberDrapeHapticController
import com.drape.app.core.designsystem.theme.DrapeThemeTokens
import com.drape.app.core.designsystem.theme.rememberDrapeResponsiveMetrics
import com.drape.app.core.model.DrapeWardrobeStateHolder

/**
 * Root application shell hosting `DrapeNavHost` across the 4 main DRAPE destinations
 * (Today, Wardrobe, Style, Profile) with adaptive responsive width constraints,
 * tactile haptics, and a floating Liquid Glass bottom navigation bar.
 */
@Composable
fun DrapeAppShell(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = true,
    onToggleTheme: () -> Unit = {},
    navController: NavHostController = rememberNavController(),
    wardrobeState: DrapeWardrobeStateHolder = remember { DrapeWardrobeStateHolder() }
) {
    val spacing = DrapeThemeTokens.spacing
    val responsive = rememberDrapeResponsiveMetrics()
    val haptics = rememberDrapeHapticController()
    val appState = rememberDrapeAppState(navController = navController)
    val activeTab = appState.currentTopLevelDestination

    val navTabs = remember {
        listOf(
            LiquidGlassNavTab(
                route = TodayDestination as DrapeTopLevelDestination,
                label = TodayDestination.label,
                icon = Icons.Rounded.WbSunny
            ),
            LiquidGlassNavTab(
                route = WardrobeDestination as DrapeTopLevelDestination,
                label = WardrobeDestination.label,
                icon = Icons.Rounded.Checkroom
            ),
            LiquidGlassNavTab(
                route = StyleDestination as DrapeTopLevelDestination,
                label = StyleDestination.label,
                icon = Icons.Rounded.AutoAwesome
            ),
            LiquidGlassNavTab(
                route = ProfileDestination as DrapeTopLevelDestination,
                label = ProfileDestination.label,
                icon = Icons.Rounded.PersonOutline
            )
        )
    }

    val eyebrowText = when (activeTab) {
        TodayDestination -> stringResource(R.string.today_eyebrow)
        WardrobeDestination -> stringResource(R.string.wardrobe_eyebrow)
        StyleDestination -> stringResource(R.string.style_eyebrow)
        ProfileDestination -> stringResource(R.string.profile_eyebrow)
        else -> stringResource(R.string.design_system_status_ready)
    }

    val screenBottomInset = spacing.floatingBarHeight + spacing.floatingBarBottomMargin + 28.dp

    LiquidGlassBackdrop(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = responsive.maxContentWidth)
                    .padding(
                        start = responsive.horizontalPadding,
                        end = responsive.horizontalPadding,
                        top = spacing.screenTopPadding
                    ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Pinned Editorial Top Bar
                LiquidGlassTopBar(
                    title = stringResource(R.string.app_name),
                    subtitle = activeTab.label,
                    eyebrow = eyebrowText,
                    actions = {
                        LiquidGlassIconButton(
                            icon = if (isDarkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                            contentDescription = stringResource(R.string.action_toggle_theme),
                            isActive = !isDarkTheme,
                            onClick = {
                                haptics.performLightTick()
                                onToggleTheme()
                            }
                        )
                    }
                )

                // Offline-first architecture status banner
                LiquidGlassBanner(
                    message = stringResource(R.string.offline_mode_notice),
                    badgeLabel = stringResource(R.string.foundation_offline_badge)
                )

                // Dedicated NavHost where each screen owns its scroll container
                DrapeNavHost(
                    appState = appState,
                    wardrobeState = wardrobeState,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = onToggleTheme,
                    contentPadding = PaddingValues(bottom = screenBottomInset),
                    modifier = Modifier.weight(1f)
                )
            }

            // Floating 4-tab Liquid Glass bottom navigation capsule
            LiquidGlassBottomBar(
                tabs = navTabs,
                selectedRoute = activeTab,
                onTabSelected = { destination ->
                    haptics.performLightTick()
                    appState.navigateToTopLevelDestination(destination)
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .widthIn(max = 560.dp)
                    .fillMaxWidth()
                    .padding(
                        horizontal = responsive.horizontalPadding,
                        vertical = spacing.floatingBarBottomMargin
                    )
            )
        }
    }
}
