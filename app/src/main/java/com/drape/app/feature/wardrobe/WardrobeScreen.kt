package com.drape.app.feature.wardrobe

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.FilterAltOff
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.drape.app.core.data.storage.GarmentImageStorageManager
import com.drape.app.core.designsystem.component.DrapeUiFeedbackMessage
import com.drape.app.core.designsystem.component.FeedbackSeverity
import com.drape.app.core.designsystem.component.LiquidGlassColorSwatchChip
import com.drape.app.core.designsystem.component.LiquidGlassEmptyStateCard
import com.drape.app.core.designsystem.component.LiquidGlassFeedbackBanner
import com.drape.app.core.designsystem.component.LiquidGlassFilterChip
import com.drape.app.core.designsystem.component.LiquidGlassGarmentCard
import com.drape.app.core.designsystem.component.LiquidGlassGarmentImage
import com.drape.app.core.designsystem.component.LiquidGlassPrimaryButton
import com.drape.app.core.designsystem.component.LiquidGlassSearchBar
import com.drape.app.core.designsystem.component.LiquidGlassSecondaryButton
import com.drape.app.core.designsystem.component.LiquidGlassSheetContainer
import com.drape.app.core.designsystem.component.LiquidGlassTextField
import com.drape.app.core.designsystem.component.rememberDrapeHapticController
import com.drape.app.core.designsystem.theme.DrapeThemeTokens
import com.drape.app.core.designsystem.theme.GarmentSwatchPalette
import com.drape.app.core.designsystem.theme.ResponsiveAdaptiveGrid
import com.drape.app.core.designsystem.theme.rememberDrapeResponsiveMetrics
import com.drape.app.core.model.GarmentCategory
import com.drape.app.core.model.GarmentInputValidationResult
import com.drape.app.core.model.GarmentItem
import com.drape.app.core.model.SeasonTag
import com.drape.app.core.model.WardrobeFilterCriteria
import com.drape.app.core.model.WardrobeInputValidator
import com.drape.app.core.model.WardrobeSortOption
import com.drape.app.core.model.filterAndSortGarments
import com.drape.app.feature.wardrobe.capture.GarmentCameraCaptureDialog
import kotlinx.coroutines.launch

/**
 * Digital Wardrobe Screen (Phase 7 Polish & Performance) — Responsive multi-column grid
 * (2 columns on phones, 3 on foldables, 4 on tablets), `derivedStateOf` recomposition skipping,
 * structured input validation & feedback banners, and haptic micro-interactions.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WardrobeScreen(
    garments: List<GarmentItem>,
    onAddGarment: (
        title: String,
        brand: String,
        category: GarmentCategory,
        season: SeasonTag,
        swatchId: String,
        localImageUri: String?,
        notes: String,
        size: String,
        fabric: String,
        purchasePrice: Double?,
        tags: List<String>
    ) -> Unit,
    onUpdateGarment: (GarmentItem) -> Unit,
    onDeleteGarment: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onIncrementWear: (String) -> Unit,
    onDecrementWear: (String) -> Unit,
    onLoadStarterCapsule: () -> Unit,
    onClearWardrobe: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val glassColors = DrapeThemeTokens.glassColors
    val editorialTypography = DrapeThemeTokens.editorialTypography
    val responsive = rememberDrapeResponsiveMetrics()
    val haptics = rememberDrapeHapticController()

    val storageManager = remember(context) {
        GarmentImageStorageManager(context.applicationContext)
    }

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedCategoryName by rememberSaveable { mutableStateOf("ALL") }
    var selectedSeasonName by rememberSaveable { mutableStateOf("ALL") }
    var selectedSwatchId by rememberSaveable { mutableStateOf<String?>(null) }
    var favoritesOnly by rememberSaveable { mutableStateOf(false) }
    var sortOptionName by rememberSaveable { mutableStateOf(WardrobeSortOption.NEWEST.name) }

    var showAddGarmentSheet by rememberSaveable { mutableStateOf(false) }
    var selectedGarmentIdForDetail by rememberSaveable { mutableStateOf<String?>(null) }
    var showCameraCaptureDialog by rememberSaveable { mutableStateOf(false) }
    var photoTargetGarmentId by rememberSaveable { mutableStateOf<String?>(null) }
    var feedbackBanner by remember { mutableStateOf<DrapeUiFeedbackMessage?>(null) }

    var draftTitle by rememberSaveable { mutableStateOf("") }
    var draftBrand by rememberSaveable { mutableStateOf("") }
    var draftCategory by rememberSaveable { mutableStateOf(GarmentCategory.TOPS) }
    var draftSeason by rememberSaveable { mutableStateOf(SeasonTag.ALL_SEASONS) }
    var draftSwatchId by rememberSaveable { mutableStateOf(GarmentSwatchPalette.ObsidianBlack.id) }
    var draftSize by rememberSaveable { mutableStateOf("M") }
    var draftFabric by rememberSaveable { mutableStateOf("100% Cotton") }
    var draftPriceText by rememberSaveable { mutableStateOf("") }
    var draftTagsText by rememberSaveable { mutableStateOf("") }
    var draftNotes by rememberSaveable { mutableStateOf("") }
    var draftImageUri by rememberSaveable { mutableStateOf<String?>(null) }

    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { pickedUri: Uri? ->
        if (pickedUri != null) {
            scope.launch {
                val persistedUri = storageManager.importAndOptimizeImage(pickedUri)
                    ?: pickedUri.toString()
                val targetId = photoTargetGarmentId
                if (targetId != null) {
                    val existing = garments.firstOrNull { it.id == targetId }
                    if (existing != null) {
                        onUpdateGarment(existing.copy(localImageUri = persistedUri))
                        feedbackBanner = DrapeUiFeedbackMessage(
                            title = "Photo Updated",
                            message = "Optimized and attached gallery photo to ${existing.title}.",
                            severity = FeedbackSeverity.SUCCESS
                        )
                    }
                } else {
                    draftImageUri = persistedUri
                    showAddGarmentSheet = true
                    feedbackBanner = DrapeUiFeedbackMessage(
                        title = "Photo Imported",
                        message = "Garment image downscaled to 1440px max edge for 60 FPS scrolling.",
                        severity = FeedbackSeverity.INFO
                    )
                }
            }
        }
    }

    val filterCriteria by remember {
        derivedStateOf {
            WardrobeFilterCriteria(
                searchQuery = searchQuery,
                selectedCategory = GarmentCategory.entries.firstOrNull { it.name == selectedCategoryName },
                selectedSeason = SeasonTag.entries.firstOrNull { it.name == selectedSeasonName },
                selectedSwatchId = selectedSwatchId,
                favoritesOnly = favoritesOnly,
                sortOption = WardrobeSortOption.entries.firstOrNull { it.name == sortOptionName }
                    ?: WardrobeSortOption.NEWEST
            )
        }
    }

    val filteredGarments by remember(garments) {
        derivedStateOf {
            filterAndSortGarments(garments, filterCriteria)
        }
    }

    val selectedDetailGarment by remember(garments) {
        derivedStateOf {
            selectedGarmentIdForDetail?.let { id -> garments.firstOrNull { it.id == id } }
        }
    }

    if (showCameraCaptureDialog) {
        GarmentCameraCaptureDialog(
            onPhotoCaptured = { capturedUri ->
                showCameraCaptureDialog = false
                val targetId = photoTargetGarmentId
                if (targetId != null) {
                    val existing = garments.firstOrNull { it.id == targetId }
                    if (existing != null) {
                        onUpdateGarment(existing.copy(localImageUri = capturedUri))
                        feedbackBanner = DrapeUiFeedbackMessage(
                            title = "CameraX Capture Saved",
                            message = "Updated photo for ${existing.title}.",
                            severity = FeedbackSeverity.SUCCESS
                        )
                    }
                } else {
                    draftImageUri = capturedUri
                    showAddGarmentSheet = true
                    feedbackBanner = DrapeUiFeedbackMessage(
                        title = "Garment Photographed",
                        message = "Enter garment details below to save to your closet.",
                        severity = FeedbackSeverity.SUCCESS
                    )
                }
            },
            onDismiss = { showCameraCaptureDialog = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LiquidGlassFeedbackBanner(
            feedback = feedbackBanner,
            onDismiss = { feedbackBanner = null }
        )

        // Top Action Bar
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column {
                Text(
                    text = "${garments.size} Pieces in Closet",
                    style = MaterialTheme.typography.titleMedium,
                    color = glassColors.textPrimary
                )
                Text(
                    text = "Tap any piece to inspect, edit, or log wears",
                    style = MaterialTheme.typography.bodySmall,
                    color = glassColors.textSecondary
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LiquidGlassSecondaryButton(
                    text = "Camera",
                    leadingIcon = Icons.Rounded.CameraAlt,
                    onClick = {
                        haptics.performLightTick()
                        photoTargetGarmentId = null
                        showCameraCaptureDialog = true
                    }
                )
                LiquidGlassSecondaryButton(
                    text = "Gallery",
                    leadingIcon = Icons.Rounded.PhotoLibrary,
                    onClick = {
                        haptics.performLightTick()
                        photoTargetGarmentId = null
                        galleryPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )
                LiquidGlassPrimaryButton(
                    text = "Add",
                    leadingIcon = Icons.Rounded.Add,
                    onClick = {
                        haptics.performConfirmPop()
                        showAddGarmentSheet = !showAddGarmentSheet
                    }
                )
            }
        }

        // Selected Garment Detail & Edit Sheet
        selectedDetailGarment?.let { detailGarment ->
            GarmentDetailSheet(
                garment = detailGarment,
                onSaveEdits = { updated ->
                    onUpdateGarment(updated)
                    feedbackBanner = DrapeUiFeedbackMessage(
                        title = "Changes Saved",
                        message = "Updated ${updated.title} in your local wardrobe.",
                        severity = FeedbackSeverity.SUCCESS
                    )
                },
                onDeleteGarment = { id ->
                    val deletedTitle = detailGarment.title
                    onDeleteGarment(id)
                    selectedGarmentIdForDetail = null
                    feedbackBanner = DrapeUiFeedbackMessage(
                        title = "Garment Removed",
                        message = "Removed $deletedTitle from your wardrobe.",
                        severity = FeedbackSeverity.INFO
                    )
                },
                onToggleFavorite = onToggleFavorite,
                onIncrementWear = onIncrementWear,
                onDecrementWear = onDecrementWear,
                onRequestCameraCapture = {
                    photoTargetGarmentId = detailGarment.id
                    showCameraCaptureDialog = true
                },
                onRequestGalleryImport = {
                    photoTargetGarmentId = detailGarment.id
                    galleryPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onDismiss = { selectedGarmentIdForDetail = null }
            )
        }

        // Expandable Liquid Glass Add-Garment Drawer with Input Validation
        AnimatedVisibility(visible = showAddGarmentSheet) {
            val activeDraftSwatch = GarmentSwatchPalette.AllSwatches
                .firstOrNull { it.id == draftSwatchId }
                ?: GarmentSwatchPalette.ObsidianBlack

            LiquidGlassSheetContainer(
                title = "Catalog New Garment",
                subtitle = "Attach a CameraX photo or gallery image and set garment details",
                onDismiss = { showAddGarmentSheet = false }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.35f)
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, glassColors.specularRimTop, RoundedCornerShape(20.dp))
                ) {
                    LiquidGlassGarmentImage(
                        imageUri = draftImageUri,
                        contentDescription = draftTitle.ifBlank { "New garment preview" },
                        swatchColor = activeDraftSwatch.color,
                        fallbackIconSize = 52.dp
                    )
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LiquidGlassSecondaryButton(
                            text = if (draftImageUri == null) "Take Photo" else "Retake",
                            leadingIcon = Icons.Rounded.CameraAlt,
                            onClick = {
                                photoTargetGarmentId = null
                                showCameraCaptureDialog = true
                            }
                        )
                        LiquidGlassSecondaryButton(
                            text = "Pick from Gallery",
                            leadingIcon = Icons.Rounded.PhotoLibrary,
                            onClick = {
                                photoTargetGarmentId = null
                                galleryPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        )
                    }
                }

                LiquidGlassTextField(
                    value = draftTitle,
                    onValueChange = { draftTitle = it },
                    label = "Garment Name",
                    placeholder = "e.g., Cashmere Overshirt"
                )
                LiquidGlassTextField(
                    value = draftBrand,
                    onValueChange = { draftBrand = it },
                    label = "Brand or Label",
                    placeholder = "e.g., Lemaire"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LiquidGlassTextField(
                        value = draftSize,
                        onValueChange = { draftSize = it },
                        label = "Size",
                        placeholder = "M / 32 / 42",
                        modifier = Modifier.weight(1f)
                    )
                    LiquidGlassTextField(
                        value = draftPriceText,
                        onValueChange = { draftPriceText = it },
                        label = "Purchase Price ($)",
                        placeholder = "e.g., 220",
                        modifier = Modifier.weight(1f)
                    )
                }

                LiquidGlassTextField(
                    value = draftFabric,
                    onValueChange = { draftFabric = it },
                    label = "Fabric / Material",
                    placeholder = "e.g., 100% Organic Cotton"
                )

                Text(
                    text = "CATEGORY",
                    style = editorialTypography.eyebrowUpper,
                    color = glassColors.textMuted
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GarmentCategory.entries.forEach { cat ->
                        LiquidGlassFilterChip(
                            label = cat.displayName,
                            selected = draftCategory == cat,
                            onClick = { draftCategory = cat }
                        )
                    }
                }

                Text(
                    text = "SEASON",
                    style = editorialTypography.eyebrowUpper,
                    color = glassColors.textMuted
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SeasonTag.entries.forEach { season ->
                        LiquidGlassFilterChip(
                            label = season.displayName,
                            selected = draftSeason == season,
                            onClick = { draftSeason = season }
                        )
                    }
                }

                Text(
                    text = "COLOR SWATCH",
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
                            selected = draftSwatchId == swatch.id,
                            onClick = { draftSwatchId = swatch.id }
                        )
                    }
                }

                LiquidGlassTextField(
                    value = draftTagsText,
                    onValueChange = { draftTagsText = it },
                    label = "Style Tags (comma-separated)",
                    placeholder = "minimal, office, layering"
                )

                LiquidGlassTextField(
                    value = draftNotes,
                    onValueChange = { draftNotes = it },
                    label = "Care & Styling Notes",
                    placeholder = "Optional fit or care notes",
                    singleLine = false
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    LiquidGlassPrimaryButton(
                        text = "Save to Wardrobe",
                        leadingIcon = Icons.Rounded.Add,
                        onClick = {
                            when (val validation = WardrobeInputValidator.validateGarmentDraft(draftTitle, draftPriceText)) {
                                is GarmentInputValidationResult.Invalid -> {
                                    feedbackBanner = DrapeUiFeedbackMessage(
                                        title = "Check Input",
                                        message = validation.userMessage,
                                        severity = FeedbackSeverity.WARNING
                                    )
                                }
                                is GarmentInputValidationResult.Valid -> {
                                    haptics.performConfirmPop()
                                    val savedTitle = draftTitle.trim()
                                    onAddGarment(
                                        savedTitle,
                                        draftBrand.trim().ifBlank { "Personal Archive" },
                                        draftCategory,
                                        draftSeason,
                                        draftSwatchId,
                                        draftImageUri,
                                        draftNotes.trim(),
                                        draftSize.trim().ifBlank { "M" },
                                        draftFabric.trim().ifBlank { "Natural Blend" },
                                        draftPriceText.trim().toDoubleOrNull(),
                                        draftTagsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                    )
                                    draftTitle = ""
                                    draftBrand = ""
                                    draftPriceText = ""
                                    draftTagsText = ""
                                    draftNotes = ""
                                    draftImageUri = null
                                    showAddGarmentSheet = false
                                    feedbackBanner = DrapeUiFeedbackMessage(
                                        title = "Added to Wardrobe",
                                        message = "$savedTitle is now available in your closet and AI Outfit Stylist.",
                                        severity = FeedbackSeverity.SUCCESS
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }

        // Search Bar
        LiquidGlassSearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it }
        )

        // Category & Favorites Filter Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LiquidGlassFilterChip(
                label = "All",
                selected = selectedCategoryName == "ALL",
                countBadge = garments.size,
                onClick = { selectedCategoryName = "ALL" }
            )
            LiquidGlassFilterChip(
                label = "Favorites",
                selected = favoritesOnly,
                countBadge = garments.count { it.isFavorite },
                onClick = { favoritesOnly = !favoritesOnly }
            )
            GarmentCategory.entries.forEach { category ->
                val count = garments.count { it.category == category }
                LiquidGlassFilterChip(
                    label = category.displayName,
                    selected = selectedCategoryName == category.name,
                    countBadge = count,
                    onClick = { selectedCategoryName = category.name }
                )
            }
        }

        // Season Filter Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LiquidGlassFilterChip(
                label = "Any Season",
                selected = selectedSeasonName == "ALL",
                onClick = { selectedSeasonName = "ALL" }
            )
            SeasonTag.entries.forEach { season ->
                LiquidGlassFilterChip(
                    label = season.displayName,
                    selected = selectedSeasonName == season.name,
                    onClick = {
                        selectedSeasonName = if (selectedSeasonName == season.name) "ALL" else season.name
                    }
                )
            }
        }

        // Color Swatch Filter Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GarmentSwatchPalette.AllSwatches.forEach { swatch ->
                val isSelected = selectedSwatchId == swatch.id
                LiquidGlassColorSwatchChip(
                    swatch = swatch,
                    selected = isSelected,
                    onClick = {
                        selectedSwatchId = if (isSelected) null else swatch.id
                    }
                )
            }
        }

        // Sort Selector & Reset Filter Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SORT:",
                style = editorialTypography.eyebrowUpper,
                color = glassColors.textMuted,
                modifier = Modifier.padding(end = 4.dp)
            )
            WardrobeSortOption.entries.forEach { option ->
                LiquidGlassFilterChip(
                    label = option.label,
                    selected = sortOptionName == option.name,
                    onClick = { sortOptionName = option.name }
                )
            }
            if (filterCriteria.hasActiveFilters) {
                LiquidGlassSecondaryButton(
                    text = "Reset Filters",
                    leadingIcon = Icons.Rounded.FilterAltOff,
                    onClick = {
                        searchQuery = ""
                        selectedCategoryName = "ALL"
                        selectedSeasonName = "ALL"
                        selectedSwatchId = null
                        favoritesOnly = false
                    }
                )
            }
            if (garments.isNotEmpty()) {
                LiquidGlassSecondaryButton(
                    text = "Empty Closet",
                    leadingIcon = Icons.Rounded.DeleteSweep,
                    onClick = {
                        selectedGarmentIdForDetail = null
                        onClearWardrobe()
                    }
                )
            }
        }

        // Main Responsive Wardrobe Grid (2 columns on compact, 3 on medium/foldable, 4 on tablet)
        when {
            garments.isEmpty() -> {
                LiquidGlassEmptyStateCard(
                    icon = Icons.Rounded.Checkroom,
                    eyebrowBadge = "Empty Wardrobe",
                    title = "Your Digital Closet Awaits",
                    description = "Photograph your clothes with CameraX, import from your gallery via Android Photo Picker, or load the starter capsule to explore DRAPE.",
                    primaryActionText = "Photograph Piece",
                    primaryActionIcon = Icons.Rounded.CameraAlt,
                    onPrimaryAction = {
                        photoTargetGarmentId = null
                        showCameraCaptureDialog = true
                    },
                    secondaryActionText = "Load Starter Capsule",
                    secondaryActionIcon = Icons.Rounded.AutoAwesome,
                    onSecondaryAction = onLoadStarterCapsule
                )
            }

            filteredGarments.isEmpty() -> {
                LiquidGlassEmptyStateCard(
                    icon = Icons.Rounded.SearchOff,
                    eyebrowBadge = "Zero Matches",
                    title = "No Garments Match Your Filters",
                    description = "No pieces matched your current search, season, or color swatch filter. Reset your active filters to view all ${garments.size} garments in your closet.",
                    primaryActionText = "Reset All Filters",
                    primaryActionIcon = Icons.Rounded.FilterAltOff,
                    onPrimaryAction = {
                        searchQuery = ""
                        selectedCategoryName = "ALL"
                        selectedSeasonName = "ALL"
                        selectedSwatchId = null
                        favoritesOnly = false
                    },
                    secondaryActionText = "Add Matching Piece",
                    secondaryActionIcon = Icons.Rounded.Add,
                    onSecondaryAction = { showAddGarmentSheet = true }
                )
            }

            else -> {
                ResponsiveAdaptiveGrid(
                    items = filteredGarments,
                    columns = responsive.wardrobeGridColumns,
                    gridSpacing = responsive.gridSpacing
                ) { garment ->
                    LiquidGlassGarmentCard(
                        title = garment.title,
                        categoryLabel = "${garment.category.displayName} · ${garment.brand}",
                        seasonLabel = garment.season.displayName,
                        swatchColor = garment.swatch.color,
                        wearCount = garment.wearCount,
                        isFavorite = garment.isFavorite,
                        imageUri = garment.localImageUri,
                        isSelectedForOutfit = selectedGarmentIdForDetail == garment.id,
                        onCardClick = {
                            selectedGarmentIdForDetail = if (selectedGarmentIdForDetail == garment.id) {
                                null
                            } else {
                                garment.id
                            }
                        },
                        onFavoriteToggle = { onToggleFavorite(garment.id) }
                    )
                }
            }
        }
    }
}
