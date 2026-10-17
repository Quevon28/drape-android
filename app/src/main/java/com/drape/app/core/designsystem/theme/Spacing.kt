package com.drape.app.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Minimal layout spacing & spatial grid system for DRAPE.
 */
@Immutable
data class DrapeSpacing(
    val xxs: Dp = 4.dp,
    val xs: Dp = 8.dp,
    val sm: Dp = 12.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 20.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp,
    val xxxl: Dp = 48.dp,
    val screenHorizontalPadding: Dp = 20.dp,
    val screenTopPadding: Dp = 12.dp,
    val cardGridSpacing: Dp = 14.dp,
    val floatingBarHeight: Dp = 64.dp,
    val floatingBarBottomMargin: Dp = 16.dp,
    val garmentCardAspectRatio: Float = 0.78f
)
