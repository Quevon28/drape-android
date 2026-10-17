package com.drape.app.feature.style

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.drape.app.core.data.remote.AiOutfitGenerationResponse
import com.drape.app.core.data.remote.DrapeAiStylistEngine
import com.drape.app.core.data.remote.StylistRefinementOption
import com.drape.app.core.designsystem.component.GlassElevation
import com.drape.app.core.designsystem.component.LiquidGlassEmptyStateCard
import com.drape.app.core.designsystem.component.LiquidGlassFilterChip
import com.drape.app.core.designsystem.component.LiquidGlassGarmentImage
import com.drape.app.core.designsystem.component.LiquidGlassIconButton
import com.drape.app.core.designsystem.component.LiquidGlassPrimaryButton
import com.drape.app.core.designsystem.component.LiquidGlassSecondaryButton
import com.drape.app.core.designsystem.component.LiquidGlassSheetContainer
import com.drape.app.core.designsystem.component.LiquidGlassOutfitSkeletonGrid
import com.drape.app.core.designsystem.component.LiquidGlassStatCard
import com.drape.app.core.designsystem.component.rememberDrapeHapticController
import com.drape.app.core.designsystem.theme.ResponsiveAdaptiveGrid
import com.drape.app.core.designsystem.theme.rememberDrapeResponsiveMetrics
import com.drape.app.core.designsystem.component.LiquidGlassSurface
import com.drape.app.core.designsystem.component.LiquidGlassTextField
import com.drape.app.core.designsystem.theme.DrapeThemeTokens
import com.drape.app.core.model.DefaultWeatherPresets
import com.drape.app.core.model.GarmentCategory
import com.drape.app.core.model.GarmentItem
import com.drape.app.core.model.OccasionVibe
import com.drape.app.core.model.OutfitCombination
import com.drape.app.core.model.UserStylePreferences
import com.drape.app.core.model.WeatherConditionPreset
import kotlinx.coroutines.launch

/**
 * Style Screen — AI Outfit Stylist + Saved Lookbook with outfit favorites,
 * occasion filters, wear logging, and personal style preference integration.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StyleScreen(
    garments: List<GarmentItem>,
    outfits: List<OutfitCombination>,
    onCreateOutfit: (title: String, occasion: OccasionVibe, garmentIds: List<String>, rationale: String?, scheduleToday: Boolean) -> Unit,
    onScheduleOutfitToday: (String) -> Unit,
    onDeleteOutfit: (String) -> Unit,
    onNavigateToWardrobe: () -> Unit,
    onLoadStarterCapsule: () -> Unit,
    modifier: Modifier = Modifier,
    selectedWeather: WeatherConditionPreset = DefaultWeatherPresets.first(),
    stylePreferences: UserStylePreferences = UserStylePreferences(),
    onToggleOutfitFavorite: (String) -> Unit = {},
    onLogOutfitWear: (String) -> Unit = {},
    stylistEngine: DrapeAiStylistEngine = remember { DrapeAiStylistEngine() },
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val glassColors = DrapeThemeTokens.glassColors
    val editorialTypography = DrapeThemeTokens.editorialTypography
    val spacing = DrapeThemeTokens.spacing
    val responsive = rememberDrapeResponsiveMetrics()
    val haptics = rememberDrapeHapticController()
    val scope = rememberCoroutineScope()

    var selectedOccasion by rememberSaveable { mutableStateOf(stylePreferences.defaultOccasion) }
    var activeRefinement by rememberSaveable { mutableStateOf<StylistRefinementOption?>(null) }
    var customRefinementPrompt by rememberSaveable { mutableStateOf("") }
    var outfitTitle by rememberSaveable { mutableStateOf("") }
    var lookbookFavoritesOnly by rememberSaveable { mutableStateOf(false) }
    var isGenerating by remember { mutableStateOf(false) }

    val activeOutfitGarmentIds = remember { mutableStateListOf<String>() }
    val lockedGarmentIds = remember { mutableStateListOf<String>() }
    val seenCombinationSignatures = remember { mutableStateListOf<String>() }

    var latestAiResponse by remember { mutableStateOf<AiOutfitGenerationResponse?>(null) }
    var slotCategoryToSwap by remember { mutableStateOf<GarmentCategory?>(null) }
    var showAddExtraPieceDrawer by remember { mutableStateOf(false) }

    fun runAiStylist(
        refinement: StylistRefinementOption? = activeRefinement,
        customPrompt: String? = customRefinementPrompt,
        isShuffle: Boolean = false
    ) {
        if (garments.isEmpty()) return
        isGenerating = true
        scope.launch {
            val currentLocks = lockedGarmentIds.filter { id -> garments.any { it.id == id } }.toSet()
            val response = stylistEngine.generateOrRefineOutfit(
                wardrobe = garments,
                occasion = selectedOccasion,
                weather = selectedWeather,
                lockedGarmentIds = currentLocks,
                currentOutfitGarmentIds = activeOutfitGarmentIds.toList(),
                excludedSignatures = if (isShuffle) seenCombinationSignatures.toSet() else emptySet(),
                refinementOption = refinement,
                customRefinementPrompt = customPrompt,
                stylePreferences = stylePreferences
            )
            activeOutfitGarmentIds.clear()
            activeOutfitGarmentIds.addAll(response.selectedGarmentIds)

            if (response.combinationSignature.isNotBlank()) {
                if (response.combinationSignature in seenCombinationSignatures) {
                    seenCombinationSignatures.clear()
                }
                seenCombinationSignatures.add(response.combinationSignature)
            }
            latestAiResponse = response
            if (outfitTitle.isBlank()) {
                outfitTitle = response.suggestedTitle
            }
            isGenerating = false
        }
    }

    LaunchedEffect(garments.size, selectedOccasion, selectedWeather.id) {
        if (garments.isNotEmpty()) {
            val validIds = garments.map { it.id }.toSet()
            activeOutfitGarmentIds.removeAll { it !in validIds }
            lockedGarmentIds.removeAll { it !in validIds }
            runAiStylist(isShuffle = false)
        } else {
            activeOutfitGarmentIds.clear()
            lockedGarmentIds.clear()
            latestAiResponse = null
        }
    }

    val activeOutfitGarments = remember(garments, activeOutfitGarmentIds.toList()) {
        activeOutfitGarmentIds.mapNotNull { id -> garments.firstOrNull { it.id == id } }
    }

    val displayedOutfits = remember(outfits, lookbookFavoritesOnly) {
        if (lookbookFavoritesOnly) outfits.filter { it.isFavorite } else outfits
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Occasion & Aesthetic Vibe Selector
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "OCCASION & DIRECTION",
                    style = editorialTypography.eyebrowUpper,
                    color = glassColors.textMuted
                )
                Text(
                    text = "${selectedWeather.label} · ${selectedWeather.tempSummary}",
                    style = editorialTypography.numericBadge,
                    color = glassColors.accentPrimary
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OccasionVibe.entries.forEach { vibe ->
                    LiquidGlassFilterChip(
                        label = vibe.displayName,
                        selected = selectedOccasion == vibe,
                        onClick = {
                            selectedOccasion = vibe
                            outfitTitle = ""
                        }
                    )
                }
            }
            Text(
                text = selectedOccasion.description,
                style = MaterialTheme.typography.bodySmall,
                color = glassColors.textSecondary
            )
        }

        // Studio Composer or Empty Closet State
        if (garments.isEmpty()) {
            LiquidGlassEmptyStateCard(
                icon = Icons.Rounded.GridView,
                eyebrowBadge = "Outfit Studio Locked",
                title = "Add Garments to Unlock AI Outfit Stylist",
                description = "DRAPE builds complete outfits using your actual photographed and imported clothes. Add garments in your Wardrobe or load the starter capsule first.",
                primaryActionText = "Go to Wardrobe",
                primaryActionIcon = Icons.Rounded.Checkroom,
                onPrimaryAction = onNavigateToWardrobe,
                secondaryActionText = "Load Starter Capsule",
                secondaryActionIcon = Icons.Rounded.AutoAwesome,
                onSecondaryAction = onLoadStarterCapsule
            )
        } else {
            // Main AI Outfit Stylist Card
            LiquidGlassSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                elevation = GlassElevation.Standard
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
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
                                text = (latestAiResponse?.engineSource?.badgeLabel
                                    ?: "On-Device AI Stylist").uppercase(),
                                style = editorialTypography.eyebrowUpper,
                                color = glassColors.accentPrimary
                            )
                            Text(
                                text = latestAiResponse?.suggestedTitle ?: "${selectedOccasion.displayName} Look",
                                style = MaterialTheme.typography.headlineSmall,
                                color = glassColors.textPrimary
                            )
                            Text(
                                text = "${activeOutfitGarments.size} pieces · ${lockedGarmentIds.size} locked · Tailored to ${stylePreferences.selectedPersonas.size} style persona(s)",
                                style = MaterialTheme.typography.bodySmall,
                                color = glassColors.textSecondary
                            )
                        }

                        LiquidGlassPrimaryButton(
                            text = if (lockedGarmentIds.isEmpty()) "Shuffle" else "Shuffle Unlocked",
                            leadingIcon = Icons.Rounded.Shuffle,
                            isLoading = isGenerating,
                            onClick = {
                                outfitTitle = ""
                                runAiStylist(
                                    refinement = activeRefinement,
                                    customPrompt = customRefinementPrompt,
                                    isShuffle = true
                                )
                            }
                        )
                    }

                    latestAiResponse?.harmony?.let { harmony ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            LiquidGlassStatCard(
                                label = "Overall Match",
                                value = "${harmony.overallMatchScore}%",
                                supportingText = "Color ${harmony.colorHarmonyScore}%",
                                modifier = Modifier.weight(1f)
                            )
                            LiquidGlassStatCard(
                                label = "Season & Rotation",
                                value = "${harmony.seasonMatchScore}%",
                                supportingText = "Rotation ${harmony.rotationBalanceScore}%",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    if (isGenerating && activeOutfitGarments.isEmpty()) {
                        LiquidGlassOutfitSkeletonGrid(columns = responsive.outfitSlotColumns)
                    } else {
                        ResponsiveAdaptiveGrid(
                            items = activeOutfitGarments,
                            columns = responsive.outfitSlotColumns,
                            gridSpacing = responsive.gridSpacing
                        ) { piece ->
                            val isLocked = piece.id in lockedGarmentIds
                            StyledOutfitSlotCard(
                                garment = piece,
                                isLocked = isLocked,
                                onToggleLock = {
                                    haptics.performConfirmPop()
                                    if (isLocked) {
                                        lockedGarmentIds.remove(piece.id)
                                    } else {
                                        lockedGarmentIds.add(piece.id)
                                    }
                                },
                                onRequestSwap = {
                                    haptics.performLightTick()
                                    slotCategoryToSwap = if (slotCategoryToSwap == piece.category) {
                                        null
                                    } else {
                                        piece.category
                                    }
                                }
                            )
                        }
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (lockedGarmentIds.isNotEmpty()) {
                            LiquidGlassSecondaryButton(
                                text = "Unlock All (${lockedGarmentIds.size})",
                                leadingIcon = Icons.Rounded.LockOpen,
                                onClick = { lockedGarmentIds.clear() }
                            )
                        } else if (activeOutfitGarments.isNotEmpty()) {
                            LiquidGlassSecondaryButton(
                                text = "Lock All Pieces",
                                leadingIcon = Icons.Rounded.Lock,
                                onClick = {
                                    lockedGarmentIds.clear()
                                    lockedGarmentIds.addAll(activeOutfitGarments.map { it.id })
                                }
                            )
                        }

                        LiquidGlassSecondaryButton(
                            text = if (showAddExtraPieceDrawer) "Hide Closet Picker" else "Add / Swap Piece",
                            leadingIcon = Icons.Rounded.SwapHoriz,
                            isSelected = showAddExtraPieceDrawer || slotCategoryToSwap != null,
                            onClick = {
                                showAddExtraPieceDrawer = !showAddExtraPieceDrawer
                                if (!showAddExtraPieceDrawer) {
                                    slotCategoryToSwap = null
                                }
                            }
                        )
                    }

                    AnimatedVisibility(visible = slotCategoryToSwap != null || showAddExtraPieceDrawer) {
                        val targetCategory = slotCategoryToSwap
                        val candidateClothes = if (targetCategory != null) {
                            garments.filter { it.category == targetCategory }
                        } else {
                            garments
                        }

                        LiquidGlassSheetContainer(
                            title = if (targetCategory != null) {
                                "Swap ${targetCategory.displayName}"
                            } else {
                                "Toggle Garments in Outfit"
                            },
                            subtitle = "Tap any garment from your wardrobe to swap or layer into this look",
                            onDismiss = {
                                slotCategoryToSwap = null
                                showAddExtraPieceDrawer = false
                            }
                        ) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                candidateClothes.forEach { candidate ->
                                    val isInOutfit = candidate.id in activeOutfitGarmentIds
                                    LiquidGlassFilterChip(
                                        label = "${candidate.title} (${candidate.category.displayName})",
                                        selected = isInOutfit,
                                        swatchColor = candidate.swatch.color,
                                        onClick = {
                                            if (targetCategory != null) {
                                                val existingIdx = activeOutfitGarmentIds.indexOfFirst { id ->
                                                    garments.firstOrNull { it.id == id }?.category == targetCategory
                                                }
                                                if (existingIdx >= 0) {
                                                    val oldId = activeOutfitGarmentIds[existingIdx]
                                                    lockedGarmentIds.remove(oldId)
                                                    activeOutfitGarmentIds[existingIdx] = candidate.id
                                                } else {
                                                    activeOutfitGarmentIds.add(candidate.id)
                                                }
                                                slotCategoryToSwap = null
                                            } else {
                                                if (isInOutfit) {
                                                    if (activeOutfitGarmentIds.size > 1) {
                                                        activeOutfitGarmentIds.remove(candidate.id)
                                                        lockedGarmentIds.remove(candidate.id)
                                                    }
                                                } else {
                                                    activeOutfitGarmentIds.add(candidate.id)
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    latestAiResponse?.let { response ->
                        LiquidGlassSurface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            elevation = GlassElevation.Subtle
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "AI STYLING RATIONALE",
                                    style = editorialTypography.eyebrowUpper,
                                    color = glassColors.accentPrimary
                                )
                                Text(
                                    text = response.rationale,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = glassColors.textPrimary
                                )
                                if (!response.refinementSummary.isNullOrBlank()) {
                                    Text(
                                        text = response.refinementSummary,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = glassColors.accentPrimary
                                    )
                                }
                            }
                        }
                    }

                    // Interactive AI Refinements Section
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Tune,
                                contentDescription = null,
                                tint = glassColors.accentPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "REFINE THIS OUTFIT (KEEPS LOCKED ITEMS)",
                                style = editorialTypography.eyebrowUpper,
                                color = glassColors.textMuted
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StylistRefinementOption.entries.forEach { option ->
                                val isSelected = activeRefinement == option
                                LiquidGlassFilterChip(
                                    label = option.label,
                                    selected = isSelected,
                                    onClick = {
                                        val nextOption = if (isSelected) null else option
                                        activeRefinement = nextOption
                                        outfitTitle = ""
                                        runAiStylist(
                                            refinement = nextOption,
                                            customPrompt = customRefinementPrompt,
                                            isShuffle = false
                                        )
                                    }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            LiquidGlassTextField(
                                value = customRefinementPrompt,
                                onValueChange = { customRefinementPrompt = it },
                                label = "Custom Stylist Instruction",
                                placeholder = "e.g., Swap in navy denim or olive merino knit",
                                modifier = Modifier.weight(1f)
                            )
                            LiquidGlassSecondaryButton(
                                text = "Refine",
                                leadingIcon = Icons.Rounded.AutoAwesome,
                                onClick = {
                                    outfitTitle = ""
                                    runAiStylist(
                                        refinement = activeRefinement,
                                        customPrompt = customRefinementPrompt,
                                        isShuffle = false
                                    )
                                }
                            )
                        }
                    }

                    LiquidGlassTextField(
                        value = outfitTitle,
                        onValueChange = { outfitTitle = it },
                        label = "Outfit Title",
                        placeholder = latestAiResponse?.suggestedTitle ?: "e.g., Architectural Commute"
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LiquidGlassPrimaryButton(
                            text = "Save & Wear Today",
                            leadingIcon = Icons.Rounded.WbSunny,
                            enabled = activeOutfitGarmentIds.isNotEmpty(),
                            onClick = {
                                val resolvedTitle = outfitTitle.trim().ifBlank {
                                    latestAiResponse?.suggestedTitle
                                        ?: "${selectedOccasion.displayName} Look #${outfits.size + 1}"
                                }
                                onCreateOutfit(
                                    resolvedTitle,
                                    selectedOccasion,
                                    activeOutfitGarmentIds.toList(),
                                    latestAiResponse?.rationale,
                                    true
                                )
                                outfitTitle = ""
                            }
                        )
                        LiquidGlassSecondaryButton(
                            text = "Save to Lookbook",
                            leadingIcon = Icons.Rounded.Add,
                            onClick = {
                                if (activeOutfitGarmentIds.isNotEmpty()) {
                                    val resolvedTitle = outfitTitle.trim().ifBlank {
                                        latestAiResponse?.suggestedTitle
                                            ?: "${selectedOccasion.displayName} Look #${outfits.size + 1}"
                                    }
                                    onCreateOutfit(
                                        resolvedTitle,
                                        selectedOccasion,
                                        activeOutfitGarmentIds.toList(),
                                        latestAiResponse?.rationale,
                                        false
                                    )
                                    outfitTitle = ""
                                }
                            }
                        )
                    }
                }
            }
        }

        // Saved Lookbook Section with Favorites Filter & Wear Logging
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SAVED LOOKBOOK (${outfits.size})",
                    style = editorialTypography.eyebrowUpper,
                    color = glassColors.textMuted
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LiquidGlassFilterChip(
                        label = "All",
                        selected = !lookbookFavoritesOnly,
                        countBadge = outfits.size,
                        onClick = { lookbookFavoritesOnly = false }
                    )
                    LiquidGlassFilterChip(
                        label = "Favorites",
                        selected = lookbookFavoritesOnly,
                        countBadge = outfits.count { it.isFavorite },
                        onClick = { lookbookFavoritesOnly = !lookbookFavoritesOnly }
                    )
                }
            }

            if (displayedOutfits.isEmpty()) {
                LiquidGlassEmptyStateCard(
                    icon = if (lookbookFavoritesOnly) Icons.Rounded.FavoriteBorder else Icons.Rounded.AutoAwesome,
                    eyebrowBadge = if (lookbookFavoritesOnly) "No Favorite Outfits" else "Lookbook Empty",
                    title = if (lookbookFavoritesOnly) {
                        "No Favorited Outfits Yet"
                    } else {
                        "No Saved Outfits Yet"
                    },
                    description = if (lookbookFavoritesOnly) {
                        "Tap the heart icon on any saved outfit in your Lookbook to pin it to your Favorites."
                    } else {
                        "Use the AI Outfit Stylist above to shuffle, lock your favorite pieces, refine the vibe, and save complete outfits to your Lookbook."
                    },
                    primaryActionText = if (lookbookFavoritesOnly) {
                        "Show All Saved Outfits"
                    } else if (garments.isEmpty()) {
                        "Add Clothes First"
                    } else {
                        "Save Current AI Look"
                    },
                    primaryActionIcon = Icons.Rounded.AutoAwesome,
                    onPrimaryAction = {
                        if (lookbookFavoritesOnly) {
                            lookbookFavoritesOnly = false
                        } else if (garments.isEmpty()) {
                            onNavigateToWardrobe()
                        } else if (activeOutfitGarmentIds.isNotEmpty()) {
                            onCreateOutfit(
                                latestAiResponse?.suggestedTitle ?: "${selectedOccasion.displayName} Capsule",
                                selectedOccasion,
                                activeOutfitGarmentIds.toList(),
                                latestAiResponse?.rationale,
                                true
                            )
                        }
                    }
                )
            } else {
                displayedOutfits.forEach { outfit ->
                    val outfitPieces = garments.filter { it.id in outfit.garmentIds }
                    LiquidGlassSurface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        elevation = if (outfit.isScheduledForToday) GlassElevation.Highlighted else GlassElevation.Subtle,
                        isSelected = outfit.isScheduledForToday
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${outfit.occasion.displayName.uppercase()} · WORN ${outfit.timesWorn}×",
                                        style = editorialTypography.eyebrowUpper,
                                        color = glassColors.accentPrimary
                                    )
                                    Text(
                                        text = outfit.title,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = glassColors.textPrimary
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    LiquidGlassIconButton(
                                        icon = if (outfit.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                        contentDescription = "Favorite outfit",
                                        isActive = outfit.isFavorite,
                                        onClick = { onToggleOutfitFavorite(outfit.id) },
                                        size = 36.dp
                                    )
                                    LiquidGlassIconButton(
                                        icon = Icons.Rounded.DeleteOutline,
                                        contentDescription = "Delete outfit",
                                        onClick = { onDeleteOutfit(outfit.id) },
                                        size = 36.dp
                                    )
                                }
                            }

                            if (!outfit.aiRationale.isNullOrBlank()) {
                                Text(
                                    text = outfit.aiRationale,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = glassColors.textSecondary
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                outfitPieces.forEach { piece ->
                                    LiquidGlassSurface(
                                        shape = RoundedCornerShape(14.dp),
                                        elevation = GlassElevation.Subtle
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(22.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                            ) {
                                                LiquidGlassGarmentImage(
                                                    imageUri = piece.localImageUri,
                                                    contentDescription = piece.title,
                                                    swatchColor = piece.swatch.color,
                                                    fallbackIconSize = 14.dp
                                                )
                                            }
                                            Text(
                                                text = piece.title,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = glassColors.textPrimary
                                            )
                                        }
                                    }
                                }
                            }

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                LiquidGlassSecondaryButton(
                                    text = if (outfit.isScheduledForToday) "Scheduled Today" else "Wear Today",
                                    leadingIcon = Icons.Rounded.WbSunny,
                                    isSelected = outfit.isScheduledForToday,
                                    onClick = { onScheduleOutfitToday(outfit.id) }
                                )
                                LiquidGlassSecondaryButton(
                                    text = "Log Wear (+1)",
                                    leadingIcon = Icons.Rounded.CheckCircle,
                                    onClick = { onLogOutfitWear(outfit.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StyledOutfitSlotCard(
    garment: GarmentItem,
    isLocked: Boolean,
    onToggleLock: () -> Unit,
    onRequestSwap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glassColors = DrapeThemeTokens.glassColors
    val editorialTypography = DrapeThemeTokens.editorialTypography
    val shapes = DrapeThemeTokens.componentShapes

    LiquidGlassSurface(
        modifier = modifier.fillMaxWidth(),
        shape = shapes.garmentCard,
        elevation = if (isLocked) GlassElevation.Highlighted else GlassElevation.Subtle,
        isSelected = isLocked,
        onClick = onToggleLock
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.88f)
                    .clip(shapes.garmentInnerImage)
            ) {
                LiquidGlassGarmentImage(
                    imageUri = garment.localImageUri,
                    contentDescription = garment.title,
                    swatchColor = garment.swatch.color,
                    fallbackIconSize = 38.dp
                )

                LiquidGlassSurface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp),
                    shape = RoundedCornerShape(50),
                    elevation = GlassElevation.Floating
                ) {
                    Text(
                        text = garment.category.displayName.uppercase(),
                        style = editorialTypography.eyebrowUpper,
                        color = if (isLocked) glassColors.accentPrimary else glassColors.textPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                LiquidGlassIconButton(
                    icon = if (isLocked) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                    contentDescription = if (isLocked) "Unlock item" else "Lock item",
                    isActive = isLocked,
                    onClick = onToggleLock,
                    size = 32.dp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                )

                LiquidGlassIconButton(
                    icon = Icons.Rounded.SwapHoriz,
                    contentDescription = "Swap slot piece",
                    onClick = onRequestSwap,
                    size = 30.dp,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = garment.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = glassColors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isLocked) "Locked · ${garment.swatch.label}" else garment.swatch.label,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isLocked) glassColors.accentPrimary else glassColors.textMuted,
                        maxLines = 1
                    )
                }
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(garment.swatch.color)
                        .border(1.dp, glassColors.specularRimTop, CircleShape)
                )
            }
        }
    }
}
