package com.drape.app.core.designsystem.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset

/**
 * Tactile spring specifications tuned for responsive 60 FPS interaction.
 * All interactive elements use interruptible physics springs rather than linear tween curves.
 */
@Immutable
data class DrapeMotion(
    val pressScaleSpring: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMedium
    ),
    val spatialBoundsSpring: SpringSpec<IntOffset> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    ),
    val morphAlphaSpring: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    ),
    val pillSlideSpring: SpringSpec<Dp> = spring(
        dampingRatio = 0.78f,
        stiffness = 380f
    ),
    val favoritePopSpring: SpringSpec<Float> = spring(
        dampingRatio = 0.52f,
        stiffness = 520f
    ),
    val sheetTransitionSpring: SpringSpec<Float> = spring(
        dampingRatio = 0.86f,
        stiffness = 400f
    ),
    val pressedScaleFactor: Float = 0.965f,
    val subtlePressedScaleFactor: Float = 0.98f,
    val iconPressedScaleFactor: Float = 0.90f
)

/**
 * GPU-accelerated tactile press modifier using `graphicsLayer` only (zero relayout cost).
 */
@Composable
fun Modifier.tactilePressScale(
    enabled: Boolean = true,
    pressedScale: Float = DrapeThemeTokens.motion.pressedScaleFactor,
    role: Role? = Role.Button,
    onClick: () -> Unit
): Modifier = composed {
    val motion = DrapeThemeTokens.motion
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (enabled && isPressed) pressedScale else 1f,
        animationSpec = motion.pressScaleSpring,
        label = "TactilePressScale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            role = role,
            onClick = onClick
        )
}
