package com.drape.app.feature.today

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.drape.app.core.designsystem.component.GlassElevation
import com.drape.app.core.designsystem.component.LiquidGlassEmptyStateCard
import com.drape.app.core.designsystem.component.LiquidGlassFilterChip
import com.drape.app.core.designsystem.component.LiquidGlassIconButton
import com.drape.app.core.designsystem.component.LiquidGlassPrimaryButton
import com.drape.app.core.designsystem.component.LiquidGlassSecondaryButton
import com.drape.app.core.designsystem.component.LiquidGlassStatCard
import com.drape.app.core.designsystem.component.LiquidGlassSurface
import com.drape.app.core.designsystem.theme.DrapeThemeTokens
import com.drape.app.core.model.DefaultWeatherPresets
import com.drape.app.core.model.GarmentItem
import com.drape.app.core.model.OutfitCombination
import com.drape.app.core.model.WearHistoryEntry
import com.drape.app.core.model.WeatherConditionPreset

/**
 * Today Screen — Daily outfit briefing, weather context selector, Today's Look showcase,
 * wear logging, recent wear history timeline, and polished empty states.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TodayScreen(
    garments: List<GarmentItem>,
    outfits: List<OutfitCombination>,
    selectedWeather: WeatherConditionPreset,
    onSelectWeather: (WeatherConditionPreset) -> Unit,
    onLogOutfitWear: (String) -> Unit,
    onClearTodayOutfit: () -> Unit,
    onAssignOutfitForToday: (String) -> Unit,
    onNavigateToWardrobe: () -> Unit,
    onNavigateToStyle: () -> Unit,
    onLoadStarterCapsule: () -> Unit,
    modifier: Modifier = Modifier,
    wearHistory: List<WearHistoryEntry> = emptyList(),
    onToggleOutfitFavorite: (String) -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val glassColors = DrapeThemeTokens.glassColors
    val editorialTypography = DrapeThemeTokens.editorialTypography

    val todayOutfit = outfits.firstOrNull { it.isScheduledForToday }
    val todayPieces = if (todayOutfit != null) {
        garments.filter { it.id in todayOutfit.garmentIds }
    } else {
        emptyList()
    }

    val weatherMatchedPieces = garments.filter {
        it.season == selectedWeather.recommendedSeason ||
            it.season.displayName == "All Seasons"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Climate & Weather Context Strip
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "CLIMATE & CONTEXT",
                style = editorialTypography.eyebrowUpper,
                color = glassColors.textMuted
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DefaultWeatherPresets.forEach { preset ->
                    LiquidGlassFilterChip(
                        label = "${preset.label} · ${preset.tempSummary}",
                        selected = preset.id == selectedWeather.id,
                        onClick = { onSelectWeather(preset) }
                    )
                }
            }
        }

        // Main Today's Look Section (or Polished Empty States)
        when {
            garments.isEmpty() -> {
                LiquidGlassEmptyStateCard(
                    icon = Icons.Rounded.Checkroom,
                    eyebrowBadge = "Step 1 · Digital Closet",
                    title = "Start Your Wardrobe to Plan Today's Look",
                    description = "Your daily styling briefing pulls directly from your own photographed garments. Add your first piece or load the starter capsule to begin.",
                    primaryActionText = "Add Garment in Wardrobe",
                    primaryActionIcon = Icons.Rounded.Add,
                    onPrimaryAction = onNavigateToWardrobe,
                    secondaryActionText = "Load Starter Capsule",
                    secondaryActionIcon = Icons.Rounded.AutoAwesome,
                    onSecondaryAction = onLoadStarterCapsule
                )
            }

            todayOutfit == null || todayPieces.isEmpty() -> {
                LiquidGlassEmptyStateCard(
                    icon = Icons.Rounded.WbSunny,
                    eyebrowBadge = selectedWeather.tempSummary,
                    title = "No Outfit Planned for Today",
                    description = "You have ${weatherMatchedPieces.size} pieces in your wardrobe suited for ${selectedWeather.label.lowercase()} conditions. Compose a look in Style Studio or pick a saved outfit.",
                    primaryActionText = "Compose in Style Studio",
                    primaryActionIcon = Icons.Rounded.AutoAwesome,
                    onPrimaryAction = onNavigateToStyle,
                    secondaryActionText = if (outfits.isNotEmpty()) "Schedule Saved Look" else "Browse Wardrobe",
                    secondaryActionIcon = Icons.Rounded.Checkroom,
                    onSecondaryAction = {
                        val firstOutfit = outfits.firstOrNull()
                        if (firstOutfit != null) {
                            onAssignOutfitForToday(firstOutfit.id)
                        } else {
                            onNavigateToWardrobe()
                        }
                    }
                )
            }

            else -> {
                // Active Today's Look Showcase Card
                LiquidGlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    elevation = GlassElevation.Highlighted,
                    isSelected = true
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = "TODAY'S SCHEDULED LOOK · ${todayOutfit.occasion.displayName.uppercase()}",
                                    style = editorialTypography.eyebrowUpper,
                                    color = glassColors.accentPrimary
                                )
                                Text(
                                    text = todayOutfit.title,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = glassColors.textPrimary
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                LiquidGlassIconButton(
                                    icon = if (todayOutfit.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                    contentDescription = "Favorite Today's Outfit",
                                    isActive = todayOutfit.isFavorite,
                                    onClick = { onToggleOutfitFavorite(todayOutfit.id) },
                                    size = 36.dp
                                )
                            }
                        }

                        if (!todayOutfit.aiRationale.isNullOrBlank()) {
                            Text(
                                text = todayOutfit.aiRationale,
                                style = MaterialTheme.typography.bodyMedium,
                                color = glassColors.textSecondary
                            )
                        }

                        // Constituent Garment Pills
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            todayPieces.forEach { piece ->
                                LiquidGlassSurface(
                                    shape = RoundedCornerShape(50),
                                    elevation = GlassElevation.Subtle
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(piece.swatch.color)
                                        )
                                        Text(
                                            text = piece.title,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = glassColors.textPrimary
                                        )
                                        Text(
                                            text = "${piece.wearCount}×",
                                            style = editorialTypography.numericBadge,
                                            color = glassColors.textMuted
                                        )
                                    }
                                }
                            }
                        }

                        // Action Footer
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            LiquidGlassPrimaryButton(
                                text = "Log Wear (+1)",
                                leadingIcon = Icons.Rounded.CheckCircle,
                                onClick = { onLogOutfitWear(todayOutfit.id) }
                            )
                            LiquidGlassSecondaryButton(
                                text = "Restyle in Studio",
                                leadingIcon = Icons.Rounded.AutoAwesome,
                                onClick = onNavigateToStyle
                            )
                            LiquidGlassSecondaryButton(
                                text = "Unschedule",
                                leadingIcon = Icons.Rounded.SwapHoriz,
                                onClick = onClearTodayOutfit
                            )
                        }
                    }
                }
            }
        }

        // Daily Wardrobe Pulse Metrics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LiquidGlassStatCard(
                label = "Weather Ready",
                value = weatherMatchedPieces.size.toString(),
                supportingText = "Matches ${selectedWeather.label}",
                modifier = Modifier.weight(1f)
            )
            LiquidGlassStatCard(
                label = "Saved Looks",
                value = outfits.size.toString(),
                supportingText = "${outfits.count { it.isFavorite }} favorited · ${wearHistory.size} logged",
                modifier = Modifier.weight(1f)
            )
        }

        // Recent Wear History Timeline
        if (wearHistory.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.History,
                        contentDescription = null,
                        tint = glassColors.accentPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "RECENT WEAR LOG (${wearHistory.size})",
                        style = editorialTypography.eyebrowUpper,
                        color = glassColors.textMuted
                    )
                }

                wearHistory.take(3).forEach { entry ->
                    LiquidGlassSurface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        elevation = GlassElevation.Subtle,
                        onClick = {
                            entry.outfitId?.let { onAssignOutfitForToday(it) }
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "${entry.dateLabel.uppercase()} · ${entry.weatherSummary.uppercase()}",
                                    style = editorialTypography.eyebrowUpper,
                                    color = glassColors.accentPrimary
                                )
                                Text(
                                    text = entry.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = glassColors.textPrimary
                                )
                                Text(
                                    text = "${entry.garmentIds.size} pieces · ${entry.occasion.displayName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = glassColors.textSecondary
                                )
                            }
                            if (entry.outfitId != null) {
                                LiquidGlassSecondaryButton(
                                    text = "Wear Again",
                                    onClick = { onAssignOutfitForToday(entry.outfitId) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
