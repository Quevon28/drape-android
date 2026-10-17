package com.drape.app.core.designsystem.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Window width size classes for responsive layout adaptation across compact phones,
 * unfolded foldables, and tablets.
 */
@Immutable
enum class DrapeWindowWidthClass {
    COMPACT,
    MEDIUM,
    EXPANDED
}

/**
 * Computed responsive layout metrics for adaptive grid columns, spacing, and max content width.
 */
@Immutable
data class DrapeResponsiveLayoutMetrics(
    val widthClass: DrapeWindowWidthClass,
    val screenWidthDp: Int,
    val screenHeightDp: Int,
    val isLandscape: Boolean,
    val wardrobeGridColumns: Int,
    val outfitSlotColumns: Int,
    val horizontalPadding: Dp,
    val gridSpacing: Dp,
    val maxContentWidth: Dp
)

/**
 * Pure deterministic calculation of responsive layout metrics for any screen dimensions.
 */
fun calculateResponsiveLayoutMetrics(
    screenWidthDp: Int,
    screenHeightDp: Int
): DrapeResponsiveLayoutMetrics {
    val isLandscape = screenWidthDp > screenHeightDp
    return when {
        screenWidthDp >= 840 -> DrapeResponsiveLayoutMetrics(
            widthClass = DrapeWindowWidthClass.EXPANDED,
            screenWidthDp = screenWidthDp,
            screenHeightDp = screenHeightDp,
            isLandscape = isLandscape,
            wardrobeGridColumns = 4,
            outfitSlotColumns = 4,
            horizontalPadding = 36.dp,
            gridSpacing = 18.dp,
            maxContentWidth = 1120.dp
        )
        screenWidthDp >= 600 -> DrapeResponsiveLayoutMetrics(
            widthClass = DrapeWindowWidthClass.MEDIUM,
            screenWidthDp = screenWidthDp,
            screenHeightDp = screenHeightDp,
            isLandscape = isLandscape,
            wardrobeGridColumns = 3,
            outfitSlotColumns = 3,
            horizontalPadding = 28.dp,
            gridSpacing = 16.dp,
            maxContentWidth = 860.dp
        )
        else -> DrapeResponsiveLayoutMetrics(
            widthClass = DrapeWindowWidthClass.COMPACT,
            screenWidthDp = screenWidthDp,
            screenHeightDp = screenHeightDp,
            isLandscape = isLandscape,
            wardrobeGridColumns = if (isLandscape) 3 else 2,
            outfitSlotColumns = if (isLandscape) 3 else 2,
            horizontalPadding = 20.dp,
            gridSpacing = 14.dp,
            maxContentWidth = 600.dp
        )
    }
}

@Composable
fun rememberDrapeResponsiveMetrics(): DrapeResponsiveLayoutMetrics {
    val configuration = LocalConfiguration.current
    return remember(configuration.screenWidthDp, configuration.screenHeightDp) {
        calculateResponsiveLayoutMetrics(
            screenWidthDp = configuration.screenWidthDp,
            screenHeightDp = configuration.screenHeightDp
        )
    }
}

/**
 * Adaptive multi-column grid layout that automatically arranges items into 2, 3, or 4 columns
 * based on `DrapeResponsiveLayoutMetrics` without nesting lazy scrollables.
 */
@Composable
fun <T> ResponsiveAdaptiveGrid(
    items: List<T>,
    columns: Int,
    gridSpacing: Dp,
    modifier: Modifier = Modifier,
    itemContent: @Composable (T) -> Unit
) {
    val safeColumns = columns.coerceAtLeast(1)
    val rows = remember(items, safeColumns) { items.chunked(safeColumns) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(gridSpacing)
    ) {
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(gridSpacing)
            ) {
                rowItems.forEach { item ->
                    Box(modifier = Modifier.weight(1f)) {
                        itemContent(item)
                    }
                }
                val emptySlots = safeColumns - rowItems.size
                repeat(emptySlots) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
