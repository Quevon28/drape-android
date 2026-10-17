package com.drape.app.core.data.remote

import com.drape.app.core.model.GarmentCategory
import com.drape.app.core.model.GarmentItem
import com.drape.app.core.model.OccasionVibe
import com.drape.app.core.model.SeasonTag
import com.drape.app.core.model.StylePersonaOption
import com.drape.app.core.model.UserStylePreferences
import com.drape.app.core.model.WeatherConditionPreset
import com.drape.app.core.security.DrapeSecurityPolicy
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Real AI Outfit Stylist Engine with Keyless Backend Proxy Security & Offline Resilience.
 *
 * - Never sends device filesystem paths (`localImageUri`) or client API keys over the network.
 * - Checks `isOnlineProvider()` and `isRemoteEndpointConfigured` before attempting remote calls.
 * - Validates remote responses against the user's actual closet IDs and locked items.
 * - Seamlessly falls back to `composeWithOnDeviceEngine(...)` when offline or when the backend
 *   proxy returns an error or invalid garment IDs.
 */
class DrapeAiStylistEngine(
    private val remoteApi: DrapeAiStylistApi? = null,
    private val isRemoteEndpointConfigured: Boolean = false,
    private val isOnlineProvider: () -> Boolean = { true }
) {

    suspend fun generateOrRefineOutfit(
        wardrobe: List<GarmentItem>,
        occasion: OccasionVibe,
        weather: WeatherConditionPreset,
        lockedGarmentIds: Set<String> = emptySet(),
        currentOutfitGarmentIds: List<String> = emptyList(),
        excludedSignatures: Set<String> = emptySet(),
        refinementOption: StylistRefinementOption? = null,
        customRefinementPrompt: String? = null,
        stylePreferences: UserStylePreferences = UserStylePreferences()
    ): AiOutfitGenerationResponse {
        val signaturesById = wardrobe.associate { item ->
            item.id to GarmentPhotoVisionAnalyzer.extractSignature(item)
        }

        val canCallRemote = remoteApi != null &&
            isRemoteEndpointConfigured &&
            isOnlineProvider() &&
            wardrobe.isNotEmpty() &&
            !DrapeSecurityPolicy.containsForbiddenClientSecret(customRefinementPrompt.orEmpty())

        if (canCallRemote && remoteApi != null) {
            val remoteResult = runCatching {
                val descriptors = wardrobe.map { item ->
                    val sig = signaturesById.getValue(item.id)
                    AiGarmentDescriptor(
                        id = item.id,
                        title = item.title,
                        brand = item.brand,
                        category = item.category.name,
                        season = item.season.name,
                        swatchId = item.swatch.id,
                        swatchHex = item.swatch.hexCode,
                        fabric = item.fabric,
                        wearCount = item.wearCount,
                        isFavorite = item.isFavorite,
                        tags = item.tags,
                        visualSignature = sig
                    )
                }
                val request = AiOutfitGenerationRequest(
                    occasion = occasion.name,
                    weatherLabel = "${weather.label} (${weather.tempSummary})",
                    recommendedSeason = weather.recommendedSeason.name,
                    lockedGarmentIds = lockedGarmentIds.toList(),
                    currentOutfitGarmentIds = currentOutfitGarmentIds,
                    excludedCombinationSignatures = excludedSignatures.toList(),
                    refinementDirectiveId = refinementOption?.id,
                    customRefinementPrompt = customRefinementPrompt?.takeIf { it.isNotBlank() },
                    wardrobe = descriptors
                )
                val response = remoteApi.generateOutfit(request)
                val validClosetIds = wardrobe.map { it.id }.toSet()
                val validReturnedIds = response.selectedGarmentIds
                    .distinct()
                    .filter { it in validClosetIds }
                val preservesLocks = lockedGarmentIds.all { it in validReturnedIds }
                if (validReturnedIds.isNotEmpty() && preservesLocks) {
                    response.copy(
                        selectedGarmentIds = validReturnedIds,
                        combinationSignature = canonicalSignature(validReturnedIds),
                        engineSourceName = StylistEngineSource.REMOTE_AI_PROXY.name
                    )
                } else {
                    null
                }
            }.getOrNull()

            if (remoteResult != null) {
                return remoteResult
            }
        }

        return composeWithOnDeviceEngine(
            wardrobe = wardrobe,
            signaturesById = signaturesById,
            occasion = occasion,
            weather = weather,
            lockedGarmentIds = lockedGarmentIds,
            currentOutfitGarmentIds = currentOutfitGarmentIds,
            excludedSignatures = excludedSignatures,
            refinementOption = refinementOption,
            customRefinementPrompt = customRefinementPrompt,
            stylePreferences = stylePreferences
        )
    }

    fun composeWithOnDeviceEngine(
        wardrobe: List<GarmentItem>,
        signaturesById: Map<String, GarmentVisualSignature> = wardrobe.associate {
            it.id to GarmentPhotoVisionAnalyzer.extractSignatureSync(it)
        },
        occasion: OccasionVibe,
        weather: WeatherConditionPreset,
        lockedGarmentIds: Set<String> = emptySet(),
        currentOutfitGarmentIds: List<String> = emptyList(),
        excludedSignatures: Set<String> = emptySet(),
        refinementOption: StylistRefinementOption? = null,
        customRefinementPrompt: String? = null,
        stylePreferences: UserStylePreferences = UserStylePreferences()
    ): AiOutfitGenerationResponse {
        if (wardrobe.isEmpty()) {
            return AiOutfitGenerationResponse(
                suggestedTitle = "${occasion.displayName} Look",
                selectedGarmentIds = emptyList(),
                rationale = "Add garments to your wardrobe to generate complete outfits.",
                refinementSummary = null,
                harmony = AiStylingHarmonyBreakdown(0, 0, 0, 0),
                combinationSignature = ""
            )
        }

        val validLocked = wardrobe.filter { it.id in lockedGarmentIds }
        val lockedByCategory = validLocked.groupBy { it.category }

        val tops = lockedByCategory[GarmentCategory.TOPS]
            ?: wardrobe.filter { it.category == GarmentCategory.TOPS }
        val bottoms = lockedByCategory[GarmentCategory.BOTTOMS]
            ?: wardrobe.filter { it.category == GarmentCategory.BOTTOMS }
        val onePieces = lockedByCategory[GarmentCategory.ONE_PIECE]
            ?: wardrobe.filter { it.category == GarmentCategory.ONE_PIECE }
        val outerwearList = lockedByCategory[GarmentCategory.OUTERWEAR]
            ?: wardrobe.filter { it.category == GarmentCategory.OUTERWEAR }
        val footwearList = lockedByCategory[GarmentCategory.FOOTWEAR]
            ?: wardrobe.filter { it.category == GarmentCategory.FOOTWEAR }
        val accessoriesList = lockedByCategory[GarmentCategory.ACCESSORIES]
            ?: wardrobe.filter { it.category == GarmentCategory.ACCESSORIES }

        val wantsOuterwear = when {
            lockedByCategory.containsKey(GarmentCategory.OUTERWEAR) -> true
            refinementOption == StylistRefinementOption.LIGHTER_BREATHABLE -> false
            refinementOption == StylistRefinementOption.WARMER_LAYER -> true
            weather.recommendedSeason == SeasonTag.AUTUMN_WINTER -> true
            occasion == OccasionVibe.OFFICE_TAILORED || occasion == OccasionVibe.TRAVEL_LAYERING -> true
            else -> outerwearList.isNotEmpty() && wardrobe.size <= 4
        }

        val candidates = mutableListOf<List<GarmentItem>>()

        if (tops.isNotEmpty() && bottoms.isNotEmpty()) {
            for (top in tops) {
                for (bottom in bottoms) {
                    val outerOptions: List<GarmentItem?> = when {
                        lockedByCategory.containsKey(GarmentCategory.OUTERWEAR) -> outerwearList
                        wantsOuterwear && outerwearList.isNotEmpty() -> outerwearList + listOf(null)
                        else -> listOf(null) + outerwearList
                    }
                    val shoeOptions: List<GarmentItem?> = if (footwearList.isNotEmpty()) {
                        footwearList
                    } else {
                        listOf(null)
                    }
                    val accOptions: List<GarmentItem?> = when {
                        lockedByCategory.containsKey(GarmentCategory.ACCESSORIES) -> accessoriesList
                        accessoriesList.isNotEmpty() -> listOf(null) + accessoriesList
                        else -> listOf(null)
                    }

                    for (outer in outerOptions) {
                        for (shoe in shoeOptions) {
                            for (acc in accOptions) {
                                val combo = buildList {
                                    if (outer != null) add(outer)
                                    add(top)
                                    add(bottom)
                                    if (shoe != null) add(shoe)
                                    if (acc != null) add(acc)
                                    validLocked.forEach { lockedItem ->
                                        if (none { it.id == lockedItem.id }) {
                                            add(lockedItem)
                                        }
                                    }
                                }
                                candidates.add(combo)
                            }
                        }
                    }
                }
            }
        }

        if (onePieces.isNotEmpty() && !lockedByCategory.containsKey(GarmentCategory.TOPS) && !lockedByCategory.containsKey(GarmentCategory.BOTTOMS)) {
            for (onePiece in onePieces) {
                val shoeOptions: List<GarmentItem?> = if (footwearList.isNotEmpty()) footwearList else listOf(null)
                val outerOptions: List<GarmentItem?> = if (wantsOuterwear && outerwearList.isNotEmpty()) outerwearList else listOf(null)
                for (outer in outerOptions) {
                    for (shoe in shoeOptions) {
                        val combo = buildList {
                            if (outer != null) add(outer)
                            add(onePiece)
                            if (shoe != null) add(shoe)
                            validLocked.forEach { lockedItem ->
                                if (none { it.id == lockedItem.id }) add(lockedItem)
                            }
                        }
                        candidates.add(combo)
                    }
                }
            }
        }

        if (candidates.isEmpty()) {
            val distinctByCategory = buildList {
                addAll(validLocked)
                wardrobe.forEach { item ->
                    if (none { it.id == item.id } && none { it.category == item.category }) {
                        add(item)
                    }
                }
                if (size < 2) {
                    wardrobe.forEach { item ->
                        if (none { it.id == item.id } && size < 3) add(item)
                    }
                }
            }
            candidates.add(distinctByCategory)
        }

        val promptKeywords = customRefinementPrompt
            ?.lowercase()
            ?.split(Regex("[^a-z0-9]+"))
            ?.filter { it.length >= 3 }
            .orEmpty()

        data class ScoredCandidate(
            val items: List<GarmentItem>,
            val signature: String,
            val totalScore: Double,
            val colorScore: Int,
            val seasonScore: Int,
            val rotationScore: Int
        )

        val scoredCandidates = candidates
            .distinctBy { canonicalSignature(it.map { g -> g.id }) }
            .map { items ->
                val sig = canonicalSignature(items.map { g -> g.id })
                val breakdown = scoreCandidate(
                    items = items,
                    signaturesById = signaturesById,
                    occasion = occasion,
                    weather = weather,
                    refinementOption = refinementOption,
                    promptKeywords = promptKeywords,
                    stylePreferences = stylePreferences
                )
                ScoredCandidate(
                    items = items,
                    signature = sig,
                    totalScore = breakdown.first,
                    colorScore = breakdown.second.colorHarmonyScore,
                    seasonScore = breakdown.second.seasonMatchScore,
                    rotationScore = breakdown.second.rotationBalanceScore
                )
            }
            .sortedByDescending { it.totalScore }

        val best = scoredCandidates.firstOrNull { it.signature !in excludedSignatures }
            ?: scoredCandidates.first()

        val selectedItems = best.items
        val overallScore = ((best.colorScore * 0.40) + (best.seasonScore * 0.35) + (best.rotationScore * 0.25))
            .roundToInt()
            .coerceIn(68, 99)

        val photoBackedCount = selectedItems.count { !it.localImageUri.isNullOrBlank() }
        val lockedNames = selectedItems.filter { it.id in lockedGarmentIds }.map { it.title }

        val rationale = buildRationale(
            items = selectedItems,
            occasion = occasion,
            weather = weather,
            photoBackedCount = photoBackedCount,
            lockedNames = lockedNames
        )

        val refinementSummary = buildRefinementSummary(
            previousIds = currentOutfitGarmentIds,
            newItems = selectedItems,
            lockedIds = lockedGarmentIds,
            refinementOption = refinementOption,
            customPrompt = customRefinementPrompt
        )

        val titlePrefix = when {
            refinementOption != null -> "${occasion.displayName} Â· ${refinementOption.label.removePrefix("+ ")}"
            !customRefinementPrompt.isNullOrBlank() -> "${occasion.displayName} Â· Custom Edit"
            else -> "${occasion.displayName} Edit"
        }

        return AiOutfitGenerationResponse(
            suggestedTitle = titlePrefix,
            selectedGarmentIds = selectedItems.map { it.id },
            rationale = rationale,
            refinementSummary = refinementSummary,
            harmony = AiStylingHarmonyBreakdown(
                overallMatchScore = overallScore,
                colorHarmonyScore = best.colorScore,
                seasonMatchScore = best.seasonScore,
                rotationBalanceScore = best.rotationScore
            ),
            combinationSignature = best.signature,
            engineSourceName = StylistEngineSource.ON_DEVICE_VISION_ENGINE.name
        )
    }

    private fun scoreCandidate(
        items: List<GarmentItem>,
        signaturesById: Map<String, GarmentVisualSignature>,
        occasion: OccasionVibe,
        weather: WeatherConditionPreset,
        refinementOption: StylistRefinementOption?,
        promptKeywords: List<String>,
        stylePreferences: UserStylePreferences
    ): Pair<Double, AiStylingHarmonyBreakdown> {
        val sigs = items.mapNotNull { signaturesById[it.id] }
        val luminances = sigs.map { it.relativeLuminance }
        val maxLumDiff = if (luminances.size >= 2) {
            (luminances.maxOrNull()!! - luminances.minOrNull()!!)
        } else {
            0.35f
        }
        val avgSaturation = if (sigs.isNotEmpty()) sigs.map { it.saturationScore }.average().toFloat() else 0.2f

        val colorHarmony = when (refinementOption) {
            StylistRefinementOption.HIGH_CONTRAST -> (65f + maxLumDiff * 34f).roundToInt()
            StylistRefinementOption.MONOCHROME_TONAL -> (96f - maxLumDiff * 35f).roundToInt()
            else -> {
                val neutralBonus = if (avgSaturation < 0.35f) 12 else 4
                val contrastBalance = (1f - abs(maxLumDiff - 0.45f)).coerceIn(0f, 1f) * 20f
                (70 + neutralBonus + contrastBalance.roundToInt())
            }
        }.coerceIn(65, 99)

        val seasonMatches = items.count {
            it.season == weather.recommendedSeason || it.season == SeasonTag.ALL_SEASONS
        }
        var seasonScore = ((seasonMatches.toFloat() / items.size.coerceAtLeast(1)) * 88f + 10f)
            .roundToInt()
            .coerceIn(62, 99)

        val hasOuterwear = items.any { it.category == GarmentCategory.OUTERWEAR }
        if (refinementOption == StylistRefinementOption.WARMER_LAYER) {
            seasonScore = if (hasOuterwear) (seasonScore + 12).coerceAtMost(99) else (seasonScore - 18).coerceAtLeast(50)
        } else if (refinementOption == StylistRefinementOption.LIGHTER_BREATHABLE) {
            seasonScore = if (!hasOuterwear) (seasonScore + 10).coerceAtMost(99) else (seasonScore - 20).coerceAtLeast(50)
        }

        val avgWear = items.map { it.wearCount }.average()
        val rotationScore = (96.0 - (avgWear * 1.4)).roundToInt().coerceIn(64, 98)

        var total = colorHarmony * 0.38 + seasonScore * 0.36 + rotationScore * 0.26

        val preferredSwatchMatches = items.count { it.swatch.id in stylePreferences.preferredSwatchIds }
        total += preferredSwatchMatches * 4.5

        if (stylePreferences.prioritizeUnwornInAi) {
            total += (25.0 - avgWear).coerceAtLeast(0.0) * 0.7
        }
        if (StylePersonaOption.MONOCHROME_LAYERING in stylePreferences.selectedPersonas) {
            total += (1.0 - maxLumDiff) * 8.0
        }
        if (StylePersonaOption.NATURAL_FIBERS in stylePreferences.selectedPersonas) {
            total += items.count {
                it.fabric.contains("Wool", ignoreCase = true) ||
                    it.fabric.contains("Cotton", ignoreCase = true) ||
                    it.fabric.contains("Linen", ignoreCase = true) ||
                    it.fabric.contains("Silk", ignoreCase = true)
            } * 3.5
        }

        when (refinementOption) {
            StylistRefinementOption.WARMER_LAYER -> {
                if (hasOuterwear) total += 28.0
                total += items.count { it.season == SeasonTag.AUTUMN_WINTER } * 10.0
            }
            StylistRefinementOption.LIGHTER_BREATHABLE -> {
                if (!hasOuterwear) total += 28.0
                total += items.count {
                    it.season == SeasonTag.SPRING_SUMMER || it.season == SeasonTag.RESORT_HUMID
                } * 12.0
            }
            StylistRefinementOption.MORE_TAILORED -> {
                total += items.count { item ->
                    item.fabric.contains("Wool", ignoreCase = true) ||
                        item.fabric.contains("Poplin", ignoreCase = true) ||
                        item.fabric.contains("Calfskin", ignoreCase = true) ||
                        item.tags.any { it in listOf("tailored", "office", "pleated", "crisp") }
                } * 14.0
            }
            StylistRefinementOption.RELAXED_CASUAL -> {
                total += items.count { item ->
                    item.fabric.contains("Denim", ignoreCase = true) ||
                        item.fabric.contains("Merino", ignoreCase = true) ||
                        item.fabric.contains("Cotton", ignoreCase = true) ||
                        item.tags.any { it in listOf("casual", "knitwear", "denim") }
                } * 14.0
            }
            StylistRefinementOption.HIGH_CONTRAST -> {
                total += maxLumDiff * 40.0
            }
            StylistRefinementOption.MONOCHROME_TONAL -> {
                total += (1.0 - maxLumDiff) * 40.0
            }
            StylistRefinementOption.ROTATION_BOOST -> {
                total += (30.0 - avgWear).coerceAtLeast(0.0) * 2.2
            }
            null -> Unit
        }

        if (promptKeywords.isNotEmpty()) {
            items.forEach { item ->
                val searchable = "${item.title} ${item.brand} ${item.swatch.label} ${item.fabric} ${item.category.displayName} ${item.tags.joinToString(" ")}".lowercase()
                promptKeywords.forEach { kw ->
                    if (searchable.contains(kw)) {
                        total += 22.0
                    }
                }
            }
        }

        val categories = items.map { it.category }.toSet()
        if (GarmentCategory.TOPS in categories && GarmentCategory.BOTTOMS in categories) {
            total += 15.0
        }
        if (GarmentCategory.FOOTWEAR in categories) {
            total += 10.0
        }

        return total to AiStylingHarmonyBreakdown(
            overallMatchScore = ((colorHarmony + seasonScore + rotationScore) / 3),
            colorHarmonyScore = colorHarmony,
            seasonMatchScore = seasonScore,
            rotationBalanceScore = rotationScore
        )
    }

    private fun buildRationale(
        items: List<GarmentItem>,
        occasion: OccasionVibe,
        weather: WeatherConditionPreset,
        photoBackedCount: Int,
        lockedNames: List<String>
    ): String {
        val swatches = items.map { it.swatch.label }.distinct().joinToString(" + ")
        val leadPieces = items.take(3).joinToString(", ") { it.title }
        val lockClause = if (lockedNames.isNotEmpty()) {
            " Anchored around your locked ${lockedNames.joinToString(" & ")}."
        } else {
            ""
        }
        val photoClause = if (photoBackedCount > 0) {
            " Analyzed $photoBackedCount photographed garment(s) for tonal balance."
        } else {
            ""
        }
        return "Pairs $leadPieces in a $swatches palette tuned for ${occasion.displayName.lowercase()} and ${weather.label.lowercase()} (${weather.tempSummary}).$lockClause$photoClause"
    }

    private fun buildRefinementSummary(
        previousIds: List<String>,
        newItems: List<GarmentItem>,
        lockedIds: Set<String>,
        refinementOption: StylistRefinementOption?,
        customPrompt: String?
    ): String? {
        if (previousIds.isEmpty() && refinementOption == null && customPrompt.isNullOrBlank()) {
            return null
        }
        val newIds = newItems.map { it.id }.toSet()
        val swappedIn = newItems.filter { it.id !in previousIds }
        val keptLockedCount = newItems.count { it.id in lockedIds }

        val directiveText = when {
            !customPrompt.isNullOrBlank() && refinementOption != null ->
                "${refinementOption.label} + '${customPrompt.trim()}'"
            !customPrompt.isNullOrBlank() -> '${customPrompt.trim()}'
            refinementOption != null -> refinementOption.label
            else -> "Shuffle"
        }

        return if (swappedIn.isNotEmpty()) {
            val swappedTitles = swappedIn.joinToString(", ") { it.title }
            val lockNote = if (keptLockedCount > 0) " while keeping $keptLockedCount locked piece(s)" else ""
            "Applied $directiveText: introduced $swappedTitles$lockNote."
        } else {
            "Applied $directiveText across your current ${newIds.size}-piece silhouette."
        }
    }

    companion object {
        fun canonicalSignature(garmentIds: Collection<String>): String =
            garmentIds.sorted().joinToString("|")
    }
}

