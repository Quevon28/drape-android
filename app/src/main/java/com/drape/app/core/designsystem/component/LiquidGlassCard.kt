package com.drape.app.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.drape.app.core.designsystem.theme.DrapeThemeTokens

/**
 * Reusable Liquid Glass wardrobe garment card with full TalkBack accessibility semantics,
 * tactile haptic feedback on favorite toggle, and animated odometer wear-count badges.
 */
@Composable
fun LiquidGlassGarmentCard(
    title: String,
    categoryLabel: String,
    seasonLabel: String,
    swatchColor: Color,
    wearCount: Int,
    isFavorite: Boolean,
    onCardClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier,
    imageUri: String? = null,
    isSelectedForOutfit: Boolean = false,
    thumbnailContent: (@Composable BoxScope.() -> Unit)? = null
) {
    val glassColors = DrapeThemeTokens.glassColors
    val motion = DrapeThemeTokens.motion
    val shapes = DrapeThemeTokens.componentShapes
    val spacing = DrapeThemeTokens.spacing
    val editorialTypography = DrapeThemeTokens.editorialTypography
    val haptics = rememberDrapeHapticController()

    val favoriteScale by animateFloatAsState(
        targetValue = if (isFavorite) 1.18f else 1f,
        animationSpec = motion.favoritePopSpring,
        label = "FavoriteHeartPop"
    )

    val a11yDescription = "$title, $categoryLabel, $seasonLabel, worn $wearCount times"
    val a11yState = buildString {
        append(if (isFavorite) "Favorited" else "Not favorited")
        if (isSelectedForOutfit) append(", Selected")
    }

    LiquidGlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = a11yDescription
                stateDescription = a11yState
                role = Role.Button
            },
        shape = shapes.garmentCard,
        elevation = if (isSelectedForOutfit) GlassElevation.Highlighted else GlassElevation.Standard,
        isSelected = isSelectedForOutfit,
        onClick = {
            haptics.performLightTick()
            onCardClick()
        }
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(spacing.garmentCardAspectRatio)
                    .clip(shapes.garmentInnerImage)
            ) {
                if (thumbnailContent != null) {
                    thumbnailContent()
                } else {
                    LiquidGlassGarmentImage(
                        imageUri = imageUri,
                        contentDescription = title,
                        swatchColor = swatchColor
                    )
                }

                // Top-left season / category micro-pill
                LiquidGlassSurface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    shape = RoundedCornerShape(50),
                    elevation = GlassElevation.Floating
                ) {
                    Text(
                        text = seasonLabel,
                        style = editorialTypography.eyebrowUpper,
                        color = glassColors.textPrimary,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }

                // Top-right accessible favorite spring button (48dp touch target)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(40.dp)
                        .graphicsLayer {
                            scaleX = favoriteScale
                            scaleY = favoriteScale
                        }
                        .clip(CircleShape)
                        .background(glassColors.glassFillFloatingTop)
                        .semantics {
                            contentDescription = if (isFavorite) "Remove $title from favorites" else "Add $title to favorites"
                            role = Role.Button
                        }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                haptics.performConfirmPop()
                                onFavoriteToggle()
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = null,
                        tint = if (isFavorite) glassColors.accentPrimary else glassColors.textSecondary,
                        modifier = Modifier.size(17.dp)
                    )
                }

                // Bottom-right animated tabular wear-count badge
                LiquidGlassSurface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp),
                    shape = RoundedCornerShape(50),
                    elevation = GlassElevation.Subtle
                ) {
                    AnimatedOdometerText(
                        text = "${wearCount}× worn",
                        style = editorialTypography.numericBadge,
                        color = glassColors.textSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Garment metadata footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = glassColors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = categoryLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = glassColors.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(swatchColor)
                        .border(1.dp, glassColors.specularRimTop, CircleShape)
                )
            }
        }
    }
}

/**
 * Compact Liquid Glass metric card with animated odometer numerals and merged TalkBack semantics.
 */
@Composable
fun LiquidGlassStatCard(
    label: String,
    value: String,
    supportingText: String,
    modifier: Modifier = Modifier
) {
    val glassColors = DrapeThemeTokens.glassColors
    val shapes = DrapeThemeTokens.componentShapes
    val editorialTypography = DrapeThemeTokens.editorialTypography

    LiquidGlassSurface(
        modifier = modifier.semantics(mergeDescendants = true) {
            contentDescription = "$label: $value, $supportingText"
        },
        shape = shapes.statCard,
        elevation = GlassElevation.Subtle
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label.uppercase(),
                style = editorialTypography.eyebrowUpper,
                color = glassColors.textMuted
            )
            AnimatedOdometerText(
                text = value,
                style = editorialTypography.numericMetricLarge,
                color = glassColors.textPrimary
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodySmall,
                color = glassColors.accentPrimary
            )
        }
    }
}
