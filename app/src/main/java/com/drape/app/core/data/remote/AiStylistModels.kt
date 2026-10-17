package com.drape.app.core.data.remote

import androidx.compose.runtime.Immutable
import com.drape.app.core.model.GarmentCategory
import com.drape.app.core.model.OccasionVibe
import com.drape.app.core.model.SeasonTag
import kotlinx.serialization.Serializable

/**
 * Built-in refinement directives that users can apply to iteratively adjust an outfit
 * while keeping locked garments anchored.
 */
@Immutable
enum class StylistRefinementOption(
    val id: String,
    val label: String,
    val promptInstruction: String
) {
    WARMER_LAYER(
        id = "warmer_layer",
        label = "+ Warmer Layer",
        promptInstruction = "Add or swap in structured outerwear and autumn/winter insulating fabrics."
    ),
    LIGHTER_BREATHABLE(
        id = "lighter_breathable",
        label = "Lighter & Breathable",
        promptInstruction = "Remove heavy outerwear and prioritize breathable spring/summer cotton, linen, or silk."
    ),
    MORE_TAILORED(
        id = "more_tailored",
        label = "More Tailored",
        promptInstruction = "Elevate formality with structured tailoring, pleated trousers, poplin shirting, and leather footwear."
    ),
    RELAXED_CASUAL(
        id = "relaxed_casual",
        label = "More Relaxed",
        promptInstruction = "Shift toward off-duty comfort with knitwear, denim, and relaxed silhouettes."
    ),
    HIGH_CONTRAST(
        id = "high_contrast",
        label = "High Contrast",
        promptInstruction = "Maximize tonal luminance contrast between upper and lower garments."
    ),
    MONOCHROME_TONAL(
        id = "monochrome_tonal",
        label = "Monochrome / Tonal",
        promptInstruction = "Align garments within a tight tonal family for a cohesive monochrome column."
    ),
    ROTATION_BOOST(
        id = "rotation_boost",
        label = "Prioritize Unworn",
        promptInstruction = "Favor under-rotated garments with lower wear counts around locked anchor pieces."
    )
}

/**
 * Indicates whether the outfit was produced by the remote keyless AI proxy or
 * the on-device visual/combinatorial styling engine.
 */
@Immutable
enum class StylistEngineSource(val badgeLabel: String) {
    REMOTE_AI_PROXY("Cloud AI Stylist"),
    ON_DEVICE_VISION_ENGINE("On-Device AI Stylist (Offline Ready)")
}

/**
 * Visual color and luminance descriptor extracted from the user's actual garment photo
 * (or canonical swatch fallback when no photo file is attached).
 */
@Immutable
@Serializable
data class GarmentVisualSignature(
    val garmentId: String,
    val dominantHex: String,
    val relativeLuminance: Float,
    val warmthScore: Float,
    val saturationScore: Float,
    val hasActualPhoto: Boolean,
    val thumbnailBase64: String? = null
)

@Serializable
data class AiGarmentDescriptor(
    val id: String,
    val title: String,
    val brand: String,
    val category: String,
    val season: String,
    val swatchId: String,
    val swatchHex: String,
    val fabric: String,
    val wearCount: Int,
    val isFavorite: Boolean,
    val tags: List<String>,
    val visualSignature: GarmentVisualSignature
)

@Serializable
data class AiOutfitGenerationRequest(
    val occasion: String,
    val weatherLabel: String,
    val recommendedSeason: String,
    val lockedGarmentIds: List<String>,
    val currentOutfitGarmentIds: List<String>,
    val excludedCombinationSignatures: List<String>,
    val refinementDirectiveId: String? = null,
    val customRefinementPrompt: String? = null,
    val wardrobe: List<AiGarmentDescriptor>
)

@Immutable
@Serializable
data class AiStylingHarmonyBreakdown(
    val overallMatchScore: Int,
    val colorHarmonyScore: Int,
    val seasonMatchScore: Int,
    val rotationBalanceScore: Int
)

@Immutable
@Serializable
data class AiOutfitGenerationResponse(
    val suggestedTitle: String,
    val selectedGarmentIds: List<String>,
    val rationale: String,
    val refinementSummary: String? = null,
    val harmony: AiStylingHarmonyBreakdown,
    val combinationSignature: String,
    val engineSourceName: String = StylistEngineSource.ON_DEVICE_VISION_ENGINE.name
) {
    val engineSource: StylistEngineSource
        get() = StylistEngineSource.entries.firstOrNull { it.name == engineSourceName }
            ?: StylistEngineSource.ON_DEVICE_VISION_ENGINE
}
