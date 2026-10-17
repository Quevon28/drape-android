package com.drape.app.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.drape.app.core.designsystem.theme.DrapeThemeTokens

/**
 * Elevation tiers controlling glass translucency, inner refraction, and specular rim intensity.
 */
@Immutable
enum class GlassElevation {
    Subtle,
    Standard,
    Floating,
    Highlighted
}

/**
 * Ambient atmospheric canvas backdrop that renders soft radial light fields behind Liquid Glass layers.
 * Uses `drawWithCache` so gradients are allocated only when canvas dimensions or theme change.
 */
@Composable
fun LiquidGlassBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val glassColors = DrapeThemeTokens.glassColors
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(glassColors.canvasBackground)
            .drawWithCache {
                val primaryGlow = Brush.radialGradient(
                    colors = listOf(glassColors.ambientGlowPrimary, glassColors.canvasBackground),
                    center = Offset(size.width * 0.18f, size.height * 0.14f),
                    radius = size.minDimension * 0.78f
                )
                val secondaryGlow = Brush.radialGradient(
                    colors = listOf(glassColors.ambientGlowSecondary, glassColors.canvasBackground),
                    center = Offset(size.width * 0.86f, size.height * 0.82f),
                    radius = size.minDimension * 0.68f
                )
                onDrawBehind {
                    drawCircle(
                        brush = primaryGlow,
                        center = Offset(size.width * 0.18f, size.height * 0.14f),
                        radius = size.minDimension * 0.78f
                    )
                    drawCircle(
                        brush = secondaryGlow,
                        center = Offset(size.width * 0.86f, size.height * 0.82f),
                        radius = size.minDimension * 0.68f
                    )
                }
            },
        content = content
    )
}

/**
 * Apple-inspired Liquid Glass container.
 * Uses GPU-friendly multi-stop translucent fills, a top inner refraction sheen,
 * a diagonal specular rim highlight, and `graphicsLayer` spring scale feedback to sustain 60 FPS.
 */
@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    elevation: GlassElevation = GlassElevation.Standard,
    isSelected: Boolean = false,
    borderWidth: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val glassColors = DrapeThemeTokens.glassColors
    val motion = DrapeThemeTokens.motion
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = when {
            onClick != null && isPressed -> motion.pressedScaleFactor
            else -> 1f
        },
        animationSpec = motion.pressScaleSpring,
        label = "LiquidGlassPressScale"
    )

    val animatedBorderWidth by animateDpAsState(
        targetValue = if (isSelected) 1.5.dp else borderWidth,
        animationSpec = springForDp(),
        label = "LiquidGlassBorderWidth"
    )

    val topRimColor by animateColorAsState(
        targetValue = if (isSelected || elevation == GlassElevation.Highlighted) {
            glassColors.specularRimFocus
        } else {
            glassColors.specularRimTop
        },
        label = "LiquidGlassTopRimColor"
    )

    val fillColors = remember(glassColors, elevation, isSelected) {
        when {
            isSelected || elevation == GlassElevation.Highlighted -> listOf(
                glassColors.glassFillElevated,
                glassColors.glassFillTop
            )
            elevation == GlassElevation.Subtle -> listOf(
                glassColors.glassFillSubtleTop,
                glassColors.glassFillSubtleBottom
            )
            elevation == GlassElevation.Floating -> listOf(
                glassColors.glassFillFloatingTop,
                glassColors.glassFillFloatingBottom
            )
            else -> listOf(
                glassColors.glassFillTop,
                glassColors.glassFillBottom
            )
        }
    }

    val fillBrush = remember(fillColors) {
        Brush.verticalGradient(colors = fillColors)
    }

    val specularBorderBrush = remember(topRimColor, glassColors.specularRimBottom) {
        Brush.linearGradient(
            colors = listOf(
                topRimColor,
                glassColors.specularRimBottom
            )
        )
    }

    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(brush = fillBrush, shape = shape)
            .drawWithCache {
                val sheenHeight = (size.height * 0.35f).coerceAtLeast(1f)
                val innerRefractionBrush = Brush.verticalGradient(
                    colors = listOf(
                        glassColors.innerRefractionHighlight,
                        glassColors.innerRefractionHighlight.copy(alpha = 0f)
                    ),
                    startY = 0f,
                    endY = sheenHeight
                )
                onDrawBehind {
                    drawRect(
                        brush = innerRefractionBrush,
                        size = size.copy(height = sheenHeight)
                    )
                }
            }
            .then(
                if (animatedBorderWidth > 0.dp) {
                    Modifier.border(BorderStroke(animatedBorderWidth, specularBorderBrush), shape = shape)
                } else {
                    Modifier
                }
            )
            .then(clickableModifier),
        content = content
    )
}

@Composable
private fun springForDp() = androidx.compose.animation.core.spring<Dp>(
    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
    stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
)
