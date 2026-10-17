package com.drape.app.feature.profile

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.drape.app.BuildConfig
import com.drape.app.core.designsystem.component.GlassElevation
import com.drape.app.core.designsystem.component.LiquidGlassColorSwatchChip
import com.drape.app.core.designsystem.component.LiquidGlassEmptyStateCard
import com.drape.app.core.designsystem.component.LiquidGlassFilterChip
import com.drape.app.core.designsystem.component.LiquidGlassIconButton
import com.drape.app.core.designsystem.component.LiquidGlassPrimaryButton
import com.drape.app.core.designsystem.component.LiquidGlassSecondaryButton
import com.drape.app.core.designsystem.component.LiquidGlassStatCard
import com.drape.app.core.designsystem.component.LiquidGlassSurface
import com.drape.app.core.designsystem.theme.DrapeThemeTokens
import com.drape.app.core.designsystem.theme.GarmentSwatchPalette
import com.drape.app.core.model.GarmentItem
import com.drape.app.core.model.OccasionVibe
import com.drape.app.core.model.OutfitCombination
import com.drape.app.core.model.StylePersonaOption
import com.drape.app.core.model.UserStylePreferences
import com.drape.app.core.model.WearHistoryEntry

/**
 * Profile Screen (Phase 6) — Wardrobe analytics, favorites summary, persistent Personal Style
 * Preferences (Style Persona DNA, Preferred Color Swatches, Default Occasion, AI Unworn Priority),
 * chronological Wear History log, and theme appearance settings.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    garments: List<GarmentItem>,
    outfits: List<OutfitCombination>,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onNavigateToWardrobe: () -> Unit,
    onLoadStarterCapsule: () -> Unit,
    onClearWardrobe: () -> Unit,
    modifier: Modifier = Modifier,
    wearHistory: List<WearHistoryEntry> = emptyList(),
    stylePreferences: UserStylePreferences = UserStylePreferences(),
    onToggleStylePersona: (StylePersonaOption) -> Unit = {},
    onTogglePreferredSwatch: (String) -> Unit = {},
    onSelectDefaultOccasion: (OccasionVibe) -> Unit = {},
    onTogglePrioritizeUnworn: (Boolean) -> Unit = {},
    onScheduleOutfitToday: (String) -> Unit = {},
    onDeleteWearHistoryEntry: (String) -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val glassColors = DrapeThemeTokens.glassColors
    val editorialTypography = DrapeThemeTokens.editorialTypography

    val totalGarmentWears = garments.sumOf { it.wearCount }
    val mostWornPiece = garments.maxByOrNull { it.wearCount }
    val wornAtLeastOnceCount = garments.count { it.wearCount > 0 }
    val utilizationPercent = if (garments.isNotEmpty()) {
        ((wornAtLeastOnceCount.toFloat() / garments.size.toFloat()) * 100).toInt()
    } else {
        0
    }

    val favoriteGarmentsCount = garments.count { it.isFavorite }
    val favoriteOutfitsCount = outfits.count { it.isFavorite }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Wardrobe Analytics & Favorites Summary
        Text(
            text = "WARDROBE UTILIZATION & FAVORITES",
            style = editorialTypography.eyebrowUpper,
            color = glassColors.textMuted
        )

        if (garments.isEmpty() || totalGarmentWears == 0) {
            LiquidGlassEmptyStateCard(
                icon = Icons.Rounded.Insights,
                eyebrowBadge = "Analytics Pending",
                title = "Wardrobe Insights Unlock as You Dress",
                description = "Add garments and log wears on the Today or Style screens to compute closet utilization, favorites, and wear history.",
                primaryActionText = "Open Wardrobe",
                primaryActionIcon = Icons.Rounded.Checkroom,
                onPrimaryAction = onNavigateToWardrobe,
                secondaryActionText = "Load Starter Capsule",
                secondaryActionIcon = Icons.Rounded.AutoAwesome,
                onSecondaryAction = onLoadStarterCapsule
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LiquidGlassStatCard(
                    label = "Total Wears",
                    value = totalGarmentWears.toString(),
                    supportingText = "${wearHistory.size} history logs",
                    modifier = Modifier.weight(1f)
                )
                LiquidGlassStatCard(
                    label = "Utilization",
                    value = "$utilizationPercent%",
                    supportingText = "$wornAtLeastOnceCount of ${garments.size} pieces worn",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LiquidGlassStatCard(
                    label = "Fav Pieces",
                    value = favoriteGarmentsCount.toString(),
                    supportingText = "Pinned in Wardrobe",
                    modifier = Modifier.weight(1f)
                )
                LiquidGlassStatCard(
                    label = "Fav Outfits",
                    value = favoriteOutfitsCount.toString(),
                    supportingText = "Saved in Lookbook",
                    modifier = Modifier.weight(1f)
                )
            }

            if (mostWornPiece != null) {
                LiquidGlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    elevation = GlassElevation.Subtle
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "SIGNATURE STAPLE",
                            style = editorialTypography.eyebrowUpper,
                            color = glassColors.accentPrimary
                        )
                        Text(
                            text = "${mostWornPiece.title} (${mostWornPiece.wearCount}× worn)",
                            style = MaterialTheme.typography.titleMedium,
                            color = glassColors.textPrimary
                        )
                        Text(
                            text = "${mostWornPiece.brand} · ${mostWornPiece.category.displayName} · ${mostWornPiece.swatch.label}",
                            style = MaterialTheme.typography.bodySmall,
                            color = glassColors.textSecondary
                        )
                    }
                }
            }
        }

        // Persistent Personal Style Preferences Card
        LiquidGlassSurface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            elevation = GlassElevation.Standard
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "PERSONAL STYLE PREFERENCES (PERSISTED LOCALLY)",
                    style = editorialTypography.eyebrowUpper,
                    color = glassColors.accentPrimary
                )
                Text(
                    text = "Your aesthetic DNA, preferred color palette, and rotation rules directly guide the AI Outfit Stylist.",
                    style = MaterialTheme.typography.bodySmall,
                    color = glassColors.textSecondary
                )

                Text(
                    text = "STYLE PERSONA ARCHETYPES",
                    style = editorialTypography.eyebrowUpper,
                    color = glassColors.textMuted
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StylePersonaOption.entries.forEach { persona ->
                        val isSelected = persona in stylePreferences.selectedPersonas
                        LiquidGlassFilterChip(
                            label = persona.displayName,
                            selected = isSelected,
                            onClick = { onToggleStylePersona(persona) }
                        )
                    }
                }

                Text(
                    text = "PREFERRED COLOR PALETTE",
                    style = editorialTypography.eyebrowUpper,
                    color = glassColors.textMuted
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GarmentSwatchPalette.AllSwatches.forEach { swatch ->
                        LiquidGlassColorSwatchChip(
                            swatch = swatch,
                            selected = swatch.id in stylePreferences.preferredSwatchIds,
                            onClick = { onTogglePreferredSwatch(swatch.id) }
                        )
                    }
                }

                Text(
                    text = "DEFAULT OCCASION & ROTATION RULE",
                    style = editorialTypography.eyebrowUpper,
                    color = glassColors.textMuted
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OccasionVibe.entries.forEach { occasion ->
                        LiquidGlassFilterChip(
                            label = occasion.displayName,
                            selected = stylePreferences.defaultOccasion == occasion,
                            onClick = { onSelectDefaultOccasion(occasion) }
                        )
                    }
                }

                LiquidGlassFilterChip(
                    label = if (stylePreferences.prioritizeUnwornInAi) {
                        "Prioritize Unworn Pieces in AI Stylist: ON"
                    } else {
                        "Prioritize Unworn Pieces in AI Stylist: OFF"
                    },
                    selected = stylePreferences.prioritizeUnwornInAi,
                    onClick = { onTogglePrioritizeUnworn(!stylePreferences.prioritizeUnwornInAi) }
                )
            }
        }

        // Chronological Wear History Log Section
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "CHRONOLOGICAL WEAR HISTORY (${wearHistory.size})",
                style = editorialTypography.eyebrowUpper,
                color = glassColors.textMuted
            )

            if (wearHistory.isEmpty()) {
                LiquidGlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    elevation = GlassElevation.Subtle
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "No Logged Outfit Wears Yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = glassColors.textPrimary
                        )
                        Text(
                            text = "Tap 'Log Wear (+1)' on Today's Look or any saved outfit in the Style Lookbook to build your chronological wear diary.",
                            style = MaterialTheme.typography.bodySmall,
                            color = glassColors.textSecondary
                        )
                    }
                }
            } else {
                wearHistory.forEach { entry ->
                    val wornPieces = garments.filter { it.id in entry.garmentIds }
                    LiquidGlassSurface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        elevation = GlassElevation.Subtle
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
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
                                }
                                LiquidGlassIconButton(
                                    icon = Icons.Rounded.DeleteOutline,
                                    contentDescription = "Delete wear history entry",
                                    onClick = { onDeleteWearHistoryEntry(entry.id) },
                                    size = 34.dp
                                )
                            }

                            if (wornPieces.isNotEmpty()) {
                                Text(
                                    text = wornPieces.joinToString(" · ") { it.title },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = glassColors.textSecondary
                                )
                            }

                            if (entry.notes.isNotBlank()) {
                                Text(
                                    text = entry.notes,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = glassColors.textMuted
                                )
                            }

                            if (entry.outfitId != null) {
                                LiquidGlassSecondaryButton(
                                    text = "Schedule for Today",
                                    leadingIcon = Icons.Rounded.WbSunny,
                                    onClick = { onScheduleOutfitToday(entry.outfitId) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Appearance, Privacy & Keyless AI Architecture Card
        LiquidGlassSurface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            elevation = GlassElevation.Subtle
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "APPEARANCE & PRIVACY ARCHITECTURE",
                    style = editorialTypography.eyebrowUpper,
                    color = glassColors.textMuted
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isDarkTheme) "Obsidian Liquid Glass (Dark)" else "Alabaster Liquid Glass (Light)",
                            style = MaterialTheme.typography.titleMedium,
                            color = glassColors.textPrimary
                        )
                        Text(
                            text = "60 FPS hardware-accelerated translucency & spring physics",
                            style = MaterialTheme.typography.bodySmall,
                            color = glassColors.textSecondary
                        )
                    }
                    LiquidGlassSecondaryButton(
                        text = if (isDarkTheme) "Light" else "Dark",
                        leadingIcon = if (isDarkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                        onClick = onToggleTheme
                    )
                }

                Text(
                    text = "Keyless AI Proxy Endpoint: ${BuildConfig.AI_PROXY_BASE_URL} (No client API keys embedded; 100% offline Room + DataStore persistence active)",
                    style = MaterialTheme.typography.bodySmall,
                    color = glassColors.textMuted
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (garments.isEmpty()) {
                        LiquidGlassPrimaryButton(
                            text = "Restore Starter Capsule",
                            leadingIcon = Icons.Rounded.AutoAwesome,
                            onClick = onLoadStarterCapsule
                        )
                    } else {
                        LiquidGlassSecondaryButton(
                            text = "Test Empty States (Clear Closet)",
                            leadingIcon = Icons.Rounded.DeleteSweep,
                            onClick = onClearWardrobe
                        )
                    }
                }
            }
        }
    }
}
