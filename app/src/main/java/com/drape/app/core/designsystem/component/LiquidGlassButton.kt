package com.drape.app.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.drape.app.core.designsystem.theme.DrapePalette
import com.drape.app.core.designsystem.theme.DrapeThemeTokens

/**
 * Primary action pill button with warm champagne-gold refraction and tactile spring press scale.
 */
@Composable
fun LiquidGlassPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 13.dp)
) {
    val glassColors = DrapeThemeTokens.glassColors
    val motion = DrapeThemeTokens.motion
    val shape = DrapeThemeTokens.componentShapes.pill
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (enabled && !isLoading && isPressed) motion.pressedScaleFactor else 1f,
        animationSpec = motion.pressScaleSpring,
        label = "PrimaryButtonPressScale"
    )

    val backgroundBrush = remember(glassColors.isDark, enabled) {
        val alpha = if (enabled) 1f else 0.45f
        Brush.linearGradient(
            colors = listOf(
                DrapePalette.ChampagneAccent.copy(alpha = alpha),
                DrapePalette.ChampagneAccentDeep.copy(alpha = alpha)
            )
        )
    }

    val borderBrush = remember(glassColors) {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.65f),
                DrapePalette.ChampagneAccentDeep.copy(alpha = 0.35f)
            )
        )
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(brush = backgroundBrush, shape = shape)
            .border(BorderStroke(1.dp, borderBrush), shape = shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled && !isLoading,
                role = Role.Button,
                onClick = onClick
            )
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = DrapePalette.ObsidianBase
                )
            } else if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = DrapePalette.ObsidianBase,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = DrapePalette.ObsidianBase
            )
        }
    }
}

/**
 * Secondary translucent Liquid Glass pill button.
 */
@Composable
fun LiquidGlassSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    isSelected: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(horizontal = 18.dp, vertical = 12.dp)
) {
    val glassColors = DrapeThemeTokens.glassColors
    val shape = DrapeThemeTokens.componentShapes.pill

    LiquidGlassSurface(
        modifier = modifier,
        shape = shape,
        elevation = if (isSelected) GlassElevation.Highlighted else GlassElevation.Standard,
        isSelected = isSelected,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(contentPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = glassColors.accentPrimary,
                    modifier = Modifier.size(17.dp)
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = glassColors.textPrimary
            )
        }
    }
}

/**
 * Circular translucent icon button with spring pop feedback.
 */
@Composable
fun LiquidGlassIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    activeTint: Color? = null,
    size: Dp = 42.dp
) {
    val glassColors = DrapeThemeTokens.glassColors
    val shape = DrapeThemeTokens.componentShapes.pill
    val resolvedTint = when {
        isActive && activeTint != null -> activeTint
        isActive -> glassColors.accentPrimary
        else -> glassColors.textPrimary
    }

    LiquidGlassSurface(
        modifier = modifier.size(size),
        shape = shape,
        elevation = if (isActive) GlassElevation.Highlighted else GlassElevation.Subtle,
        isSelected = isActive,
        onClick = onClick
    ) {
        Box(
            modifier = Modifier. size(size),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = resolvedTint,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
