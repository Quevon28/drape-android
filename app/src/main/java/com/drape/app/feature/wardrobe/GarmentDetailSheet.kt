package com.drape.app.feature.wardrobe

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.drape.app.core.designsystem.component.GlassElevation
import com.drape.app.core.designsystem.component.LiquidGlassColorSwatchChip
import com.drape.app.core.designsystem.component.LiquidGlassFilterChip
import com.drape.app.core.designsystem.component.LiquidGlassGarmentImage
import com.drape.app.core.designsystem.component.LiquidGlassIconButton
import com.drape.app.core.designsystem.component.LiquidGlassPrimaryButton
import com.drape.app.core.designsystem.component.LiquidGlassSecondaryButton
import com.drape.app.core.designsystem.component.LiquidGlassSheetContainer
import com.drape.app.core.designsystem.component.LiquidGlassStatCard
import com.drape.app.core.designsystem.component.LiquidGlassSurface
import com.drape.app.core.designsystem.component.LiquidGlassTextField
import com.drape.app.core.designsystem.theme.DrapeThemeTokens
import com.drape.app.core.designsystem.theme.GarmentSwatchPalette
import com.drape.app.core.model.GarmentCategory
import com.drape.app.core.model.GarmentItem
import com.drape.app.core.model.SeasonTag
import java.util.Locale

/**
 * Interactive Liquid Glass Garment Detail, Editor & Deletion sheet.
 * Displays full garment metadata, real photo or swatch silhouette, tabular cost-per-wear,
 * wear count controls, inline metadata editing, photo replacement, and safe two-step deletion.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GarmentDetailSheet(
    garment: GarmentItem,
    onSaveEdits: (GarmentItem) -> Unit,
    onDeleteGarment: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onIncrementWear: (String) -> Unit,
    onDecrementWear: (String) -> Unit,
    onRequestCameraCapture: () -> Unit,
    onRequestGalleryImport: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glassColors = DrapeThemeTokens.glassColors
    val editorialTypography = DrapeThemeTokens.editorialTypography

    var isEditing by rememberSaveable(garment.id) { mutableStateOf(false) }
    var confirmDelete by rememberSaveable(garment.id) { mutableStateOf(false) }

    // Editable fields initialized from the selected garment
    var editTitle by rememberSaveable(garment.id) { mutableStateOf(garment.title) }
    var editBrand by rememberSaveable(garment.id) { mutableStateOf(garment.brand) }
    var editCategory by rememberSaveable(garment.id) { mutableStateOf(garment.category) }
    var editSeason by rememberSaveable(garment.id) { mutableStateOf(garment.season) }
    var editSwatchId by rememberSaveable(garment.id) { mutableStateOf(garment.swatch.id) }
    var editSize by rememberSaveable(garment.id) { mutableStateOf(garment.size) }
    var editFabric by rememberSaveable(garment.id) { mutableStateOf(garment.fabric) }
    var editPriceText by rememberSaveable(garment.id) {
        mutableStateOf(garment.purchasePrice?.let { String.format(Locale.US, "%.0f", it) }.orEmpty())
    }
    var editTagsText by rememberSaveable(garment.id) {
        mutableStateOf(garment.tags.joinToString(", "))
    }
    var editNotes by rememberSaveable(garment.id) { mutableStateOf(garment.notes) }

    LiquidGlassSheetContainer(
        title = if (isEditing) "Edit Garment" else garment.title,
        subtitle = if (isEditing) {
            "Update category, color swatch, fabric, or photo"
        } else {
            "${garment.brand} · ${garment.category.displayName} · Size ${garment.size}"
        },
        onDismiss = onDismiss,
        modifier = modifier
    ) {
        // Garment Hero Photo Viewport + Camera / Gallery Controls
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.15f)
                .clip(RoundedCornerShape(22.dp))
                .border(1.dp, glassColors.specularRimTop, RoundedCornerShape(22.dp))
        ) {
            LiquidGlassGarmentImage(
                imageUri = garment.localImageUri,
                contentDescription = garment.title,
                swatchColor = garment.swatch.color,
                fallbackIconSize = 64.dp
            )

            // Top-left season & color swatch pill
            LiquidGlassSurface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp),
                shape = RoundedCornerShape(50),
                elevation = GlassElevation.Floating
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(garment.swatch.color)
                    )
                    Text(
                        text = "${garment.swatch.label} · ${garment.season.displayName}",
                        style = editorialTypography.eyebrowUpper,
                        color = glassColors.textPrimary
                    )
                }
            }

            // Top-right favorite toggle
            LiquidGlassIconButton(
                icon = if (garment.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                contentDescription = "Favorite garment",
                isActive = garment.isFavorite,
                onClick = { onToggleFavorite(garment.id) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            )

            // Bottom floating CameraX & Photo Picker bar
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LiquidGlassSecondaryButton(
                    text = if (garment.localImageUri == null) "Photograph" else "Retake Photo",
                    leadingIcon = Icons.Rounded.CameraAlt,
                    onClick = onRequestCameraCapture
                )
                LiquidGlassSecondaryButton(
                    text = "Gallery Import",
                    leadingIcon = Icons.Rounded.PhotoLibrary,
                    onClick = onRequestGalleryImport
                )
            }
        }

        if (!isEditing) {
            // VIEW MODE: Metrics, Wear Controls, Metadata, Notes, Edit & Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LiquidGlassStatCard(
                    label = "Times Worn",
                    value = "${garment.wearCount}×",
                    supportingText = "Tap +1 when worn",
                    modifier = Modifier.weight(1f)
                )
                val cpwFormatted = garment.costPerWear?.let {
                    String.format(Locale.US, "$%.2f", it)
                } ?: "—"
                val priceSubtitle = garment.purchasePrice?.let {
                    String.format(Locale.US, "Price $%.0f", it)
                } ?: "Add price in Edit"
                LiquidGlassStatCard(
                    label = "Cost / Wear",
                    value = cpwFormatted,
                    supportingText = priceSubtitle,
                    modifier = Modifier.weight(1f)
                )
            }

            // Wear count increment / decrement bar
            LiquidGlassSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                elevation = GlassElevation.Subtle
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "WEAR LOGGING",
                            style = editorialTypography.eyebrowUpper,
                            color = glassColors.textMuted
                        )
                        Text(
                            text = "${garment.fabric} · Size ${garment.size}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = glassColors.textPrimary
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        LiquidGlassIconButton(
                            icon = Icons.Rounded.Remove,
                            contentDescription = "Decrement wear count",
                            onClick = { onDecrementWear(garment.id) },
                            size = 36.dp
                        )
                        LiquidGlassIconButton(
                            icon = Icons.Rounded.Add,
                            contentDescription = "Increment wear count",
                            isActive = true,
                            onClick = { onIncrementWear(garment.id) },
                            size = 36.dp
                        )
                    }
                }
            }

            if (garment.tags.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    garment.tags.forEach { tag ->
                        LiquidGlassSurface(
                            shape = RoundedCornerShape(50),
                            elevation = GlassElevation.Subtle
                        ) {
                            Text(
                                text = "#$tag",
                                style = MaterialTheme.typography.labelMedium,
                                color = glassColors.accentPrimary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            if (garment.notes.isNotBlank()) {
                Text(
                    text = garment.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = glassColors.textSecondary
                )
            }

            // Edit & Delete Action Row
            AnimatedVisibility(visible = !confirmDelete) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LiquidGlassSecondaryButton(
                        text = "Delete Piece",
                        leadingIcon = Icons.Rounded.DeleteOutline,
                        onClick = { confirmDelete = true }
                    )
                    LiquidGlassPrimaryButton(
                        text = "Edit Details",
                        leadingIcon = Icons.Rounded.Edit,
                        onClick = { isEditing = true }
                    )
                }
            }

            // Two-Step Safe Delete Confirmation
            AnimatedVisibility(visible = confirmDelete) {
                LiquidGlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    elevation = GlassElevation.Highlighted
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Remove ${garment.title} from wardrobe?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = glassColors.statusDanger,
                            modifier = Modifier.weight(1f)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            LiquidGlassSecondaryButton(
                                text = "Cancel",
                                onClick = { confirmDelete = false }
                            )
                            LiquidGlassPrimaryButton(
                                text = "Confirm Delete",
                                leadingIcon = Icons.Rounded.DeleteOutline,
                                onClick = {
                                    onDeleteGarment(garment.id)
                                    onDismiss()
                                }
                            )
                        }
                    }
                }
            }
        } else {
            // EDIT MODE: Full Garment Form
            LiquidGlassTextField(
                value = editTitle,
                onValueChange = { editTitle = it },
                label = "Garment Name",
                placeholder = "e.g., Structured Wool Trench"
            )
            LiquidGlassTextField(
                value = editBrand,
                onValueChange = { editBrand = it },
                label = "Brand / Label",
                placeholder = "e.g., Atelier Line"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LiquidGlassTextField(
                    value = editSize,
                    onValueChange = { editSize = it },
                    label = "Size",
                    placeholder = "M / 32 / 42",
                    modifier = Modifier.weight(1f)
                )
                LiquidGlassTextField(
                    value = editPriceText,
                    onValueChange = { editPriceText = it },
                    label = "Price (USD)",
                    placeholder = "e.g., 240",
                    modifier = Modifier.weight(1f)
                )
            }

            LiquidGlassTextField(
                value = editFabric,
                onValueChange = { editFabric = it },
                label = "Material / Fabric",
                placeholder = "e.g., 100% Merino Wool"
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
                        selected = editCategory == cat,
                        onClick = { editCategory = cat }
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
                        selected = editSeason == season,
                        onClick = { editSeason = season }
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
                        selected = editSwatchId == swatch.id,
                        onClick = { editSwatchId = swatch.id }
                    )
                }
            }

            LiquidGlassTextField(
                value = editTagsText,
                onValueChange = { editTagsText = it },
                label = "Style Tags (comma-separated)",
                placeholder = "tailored, minimal, commute"
            )

            LiquidGlassTextField(
                value = editNotes,
                onValueChange = { editNotes = it },
                label = "Styling & Care Notes",
                placeholder = "Fit notes, care instructions, or favorite pairings",
                singleLine = false
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LiquidGlassSecondaryButton(
                    text = "Cancel",
                    onClick = { isEditing = false }
                )
                LiquidGlassPrimaryButton(
                    text = "Save Changes",
                    leadingIcon = Icons.Rounded.Check,
                    enabled = editTitle.isNotBlank(),
                    onClick = {
                        val resolvedSwatch = GarmentSwatchPalette.AllSwatches
                            .firstOrNull { it.id == editSwatchId }
                            ?: garment.swatch
                        val parsedPrice = editPriceText.trim().toDoubleOrNull()
                        val parsedTags = editTagsText.split(",")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }

                        onSaveEdits(
                            garment.copy(
                                title = editTitle.trim(),
                                brand = editBrand.trim().ifBlank { "Personal Archive" },
                                category = editCategory,
                                season = editSeason,
                                swatch = resolvedSwatch,
                                size = editSize.trim().ifBlank { "M" },
                                fabric = editFabric.trim().ifBlank { "Natural Blend" },
                                purchasePrice = parsedPrice,
                                tags = parsedTags,
                                notes = editNotes.trim()
                            )
                        )
                        isEditing = false
                    }
                )
            }
        }
    }
}
