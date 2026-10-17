package com.drape.app.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.drape.app.core.designsystem.theme.DrapeThemeTokens

@Immutable
data class LiquidGlassNavTab<T>(
    val route: T,
    val label: String,
    val icon: ImageVector,
    val badgeCount: Int? = null
)

/**
 * Floating Apple-style Liquid Glass capsule bottom navigation bar.
 * Features a physics-driven sliding glass indicator pill that glides smoothly between active tabs.
 */
@Composable
fun <T> LiquidGlassBottomBar(
    tabs: List<LiquidGlassNavTab<T>>,
    selectedRoute: T,
    onTabSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    val glassColors = DrapeThemeTokens.glassColors
    val motion = DrapeThemeTokens.motion
    val shapes = DrapeThemeTokens.componentShapes
    val spacing = DrapeThemeTokens.spacing

    val selectedIndex = remember(tabs, selectedRoute) {
        tabs.indexOfFirst { it.route == selectedRoute }.coerceAtLeast(0)
    }

    LiquidGlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .height(spacing.floatingBarHeight),
        shape = shapes.floatingNavBar,
        elevation = GlassElevation.Floating
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp)
        ) {
            val tabCount = tabs.size.coerceAtLeast(1)
            val tabWidth = maxWidth / tabCount

            val indicatorOffset by animateDpAsState(
                targetValue = tabWidth * selectedIndex,
                animationSpec = motion.pillSlideSpring,
                label = "SlidingGlassPillOffset"
            )

            // Sliding translucent active capsule
            LiquidGlassSurface(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .width(tabWidth)
                    .fillMaxHeight(),
                shape = shapes.floatingNavIndicator,
                elevation = GlassElevation.Highlighted,
                isSelected = true
            ) {}

            // Foreground interactive tab row
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEach { tab ->
                    val isSelected = tab.route == selectedRoute
                    val interactionSource = remember { MutableInteractionSource() }

                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) glassColors.textPrimary else glassColors.textMuted,
                        label = "NavTabContentColor"
                    )

                    val iconTint by animateColorAsState(
                        targetValue = if (isSelected) glassColors.accentPrimary else glassColors.textMuted,
                        label = "NavTabIconTint"
                    )

                    val itemScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.03f else 0.96f,
                        animationSpec = motion.pressScaleSpring,
                        label = "NavTabScale"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .graphicsLayer {
                                scaleX = itemScale
                                scaleY = itemScale
                            }
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null,
                                role = Role.Tab,
                                onClick = { onTabSelected(tab.route) }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                tint = iconTint,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.labelLarge,
                                color = contentColor
                            )
                        }
                    }
                }
            }
        }
    }
}
