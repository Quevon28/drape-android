package com.drape.app.core.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.drape.app.core.designsystem.theme.DrapeThemeTokens

/**
 * GPU-cached Liquid Glass shimmer skeleton for loading garment photos and AI outfit suggestions.
 */
@Composable
fun LiquidGlassShimmerBox(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium
) {
    val glassColors = DrapeThemeTokens.glassColors
    val transition = rememberInfiniteTransition(label = "LiquidGlassShimmer")
    val progress by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1350, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShimmerOffsetProgress"
    )

    Box(
        modifier = modifier
            .clip(shape)
            .drawWithCache {
                val width = size.width.coerceAtLeast(1f)
                val height = size.height.coerceAtLeast(1f)
                onDrawBehind {
                    val startX = progress * width
                    val brush = Brush.linearGradient(
                        colors = listOf(
                            glassColors.shimmerBase,
                            glassColors.shimmerHighlight,
                            glassColors.shimmerBase
                        ),
                        start = Offset(startX, 0f),
                        end = Offset(startX + width * 0.8f, height)
                    )
                    drawRect(brush = brush)
                }
            }
    )
}

/**
 * Skeleton card placeholder displayed while AI outfits or garment photos are loading.
 */
@Composable
fun LiquidGlassGarmentCardSkeleton(
    modifier: Modifier = Modifier
) {
    val shapes = DrapeThemeTokens.componentShapes
    val spacing = DrapeThemeTokens.spacing

    LiquidGlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Loading garment preview" },
        shape = shapes.garmentCard,
        elevation = GlassElevation.Subtle
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LiquidGlassShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(spacing.garmentCardAspectRatio),
                shape = shapes.garmentInnerImage
            )
            LiquidGlassShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(14.dp),
                shape = RoundedCornerShape(6.dp)
            )
            LiquidGlassShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.45f)
                    .height(11.dp),
                shape = RoundedCornerShape(6.dp)
            )
        }
    }
}

/**
 * Multi-column skeleton grid displayed during AI outfit generation.
 */
@Composable
fun LiquidGlassOutfitSkeletonGrid(
    columns: Int = 2,
    modifier: Modifier = Modifier
) {
    val spacing = DrapeThemeTokens.spacing
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing.cardGridSpacing)
    ) {
        repeat(columns.coerceAtLeast(2)) {
            LiquidGlassGarmentCardSkeleton(modifier = Modifier.weight(1f))
        }
    }
}
