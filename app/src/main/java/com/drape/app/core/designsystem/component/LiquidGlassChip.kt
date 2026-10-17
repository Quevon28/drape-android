package com.drape.app.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.drape.app.core.designsystem.theme.DrapeThemeTokens
import com.drape.app.core.designsystem.theme.GarmentColorSwatch

/**
 * Accessible filter chip for wardrobe categories, seasons, and occasions with spring selection
 * morphing, haptic tick feedback, and TalkBack `stateDescription`.
 */
@Composable
fun LiquidGlassFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    countBadge: Int? = null,
    swatchColor: Color? = null
) {
    val glassColors = DrapeThemeTokens.glassColors
    val motion = DrapeThemeTokens.motion
    val shape = DrapeThemeTokens.componentShapes.chip
    val editorialTypography = DrapeThemeTokens.editorialTypography
    val haptics = rememberDrapeHapticController()

    val scale by animateFloatAsState(
        targetValue = if (selected) 1.02f else 1f,
        animationSpec = motion.pressScaleSpring,
        label = "FilterChipSelectionScale"
    )

    val textColor by animateColorAsState(
        targetValue = if (selected) glassColors.textPrimary else glassColors.textSecondary,
        label = "FilterChipTextColor"
    )

    LiquidGlassSurface(
        modifier = modifier
            .defaultMinSize(minHeight = 38.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .semantics {
                role = Role.Checkbox
                stateDescription = if (selected) "Selected" else "Not selected"
            },
        shape = shape,
        elevation = if (selected) GlassElevation.Highlighted else GlassElevation.Subtle,
        isSelected = selected,
        onClick = {
            haptics.performLightTick()
            onClick()
        }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            if (swatchColor != null) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(swatchColor)
                        .border(0.75.dp, glassColors.specularRimTop, CircleShape)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = textColor
            )
            if (countBadge != null) {
                AnimatedOdometerText(
                    text = countBadge.toString(),
                    style = editorialTypography.numericBadge,
                    color = if (selected) glassColors.accentPrimary else glassColors.textMuted
                )
            }
        }
    }
}

/**
 * Garment color swatch selector pill for quick wardrobe color filtering and tagging.
 */
@Composable
fun LiquidGlassColorSwatchChip(
    swatch: GarmentColorSwatch,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LiquidGlassFilterChip(
        label = swatch.label,
        selected = selected,
        onClick = onClick,
        swatchColor = swatch.color,
        modifier = modifier
    )
}
