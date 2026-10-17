package com.drape.app.core.designsystem.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.drape.app.core.designsystem.theme.DrapeThemeTokens

/**
 * Polished Apple-inspired Liquid Glass empty state card.
 * Features a translucent double-ring icon medallion with subtle ambient float animation,
 * editorial typography, optional hint badge, and tactile primary/secondary actions.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LiquidGlassEmptyStateCard(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    eyebrowBadge: String? = null,
    primaryActionText: String? = null,
    primaryActionIcon: ImageVector? = null,
    onPrimaryAction: (() -> Unit)? = null,
    secondaryActionText: String? = null,
    secondaryActionIcon: ImageVector? = null,
    onSecondaryAction: (() -> Unit)? = null
) {
    val glassColors = DrapeThemeTokens.glassColors
    val editorialTypography = DrapeThemeTokens.editorialTypography

    val infiniteTransition = rememberInfiniteTransition(label = "EmptyStateMedallionPulse")
    val medallionScale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "MedallionBreathScale"
    )

    LiquidGlassSurface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        elevation = GlassElevation.Standard
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Double-ring Liquid Glass icon medallion
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .graphicsLayer {
                        scaleX = medallionScale
                        scaleY = medallionScale
                    }
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                glassColors.accentPrimary.copy(alpha = 0.22f),
                                glassColors.glassFillSubtleTop
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                glassColors.specularRimFocus,
                                glassColors.specularRimBottom
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = glassColors.accentPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }

            if (!eyebrowBadge.isNullOrBlank()) {
                LiquidGlassSurface(
                    shape = RoundedCornerShape(50),
                    elevation = GlassElevation.Subtle
                ) {
                    Text(
                        text = eyebrowBadge.uppercase(),
                        style = editorialTypography.eyebrowUpper,
                        color = glassColors.accentPrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = glassColors.textPrimary,
                textAlign = TextAlign.Center
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = glassColors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            if ((primaryActionText != null && onPrimaryAction != null) ||
                (secondaryActionText != null && onSecondaryAction != null)
            ) {
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (primaryActionText != null && onPrimaryAction != null) {
                        LiquidGlassPrimaryButton(
                            text = primaryActionText,
                            leadingIcon = primaryActionIcon,
                            onClick = onPrimaryAction
                        )
                    }
                    if (secondaryActionText != null && onSecondaryAction != null) {
                        LiquidGlassSecondaryButton(
                            text = secondaryActionText,
                            leadingIcon = secondaryActionIcon,
                            onClick = onSecondaryAction
                        )
                    }
                }
            }
        }
    }
}
