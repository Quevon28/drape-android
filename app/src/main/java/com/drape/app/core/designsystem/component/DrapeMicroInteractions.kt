package com.drape.app.core.designsystem.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle

/**
 * Tactile haptic feedback controller for micro-interactions (locking outfit items,
 * favoriting garments, logging wears, and shuffling combinations).
 */
@Immutable
class DrapeHapticController(
    private val hapticFeedback: HapticFeedback?
) {
    fun performLightTick() {
        hapticFeedback?.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    fun performConfirmPop() {
        hapticFeedback?.performHapticFeedback(HapticFeedbackType.LongPress)
    }
}

@Composable
fun rememberDrapeHapticController(): DrapeHapticController {
    val haptics = LocalHapticFeedback.current
    return remember(haptics) { DrapeHapticController(haptics) }
}

/**
 * Animated odometer-style tabular numeral text that smoothly slides vertically when numbers change.
 */
@Composable
fun AnimatedOdometerText(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = text,
        transitionSpec = {
            (slideInVertically(animationSpec = spring()) { height -> height / 2 } +
                fadeIn(animationSpec = spring())) togetherWith
                (slideOutVertically(animationSpec = spring()) { height -> -height / 2 } +
                    fadeOut(animationSpec = spring()))
        },
        label = "AnimatedOdometerText",
        modifier = modifier
    ) { targetValue ->
        Text(
            text = targetValue,
            style = style,
            color = color
        )
    }
}
