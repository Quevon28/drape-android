package com.drape.app.core.model

import androidx.compose.runtime.Immutable

/**
 * Deterministic input validation for garment creation, editing, and outfit composition.
 */
@Immutable
sealed interface GarmentInputValidationResult {
    data object Valid : GarmentInputValidationResult

    @Immutable
    data class Invalid(
        val field: String,
        val userMessage: String
    ) : GarmentInputValidationResult
}

object WardrobeInputValidator {

    fun validateGarmentDraft(
        title: String,
        priceText: String
    ): GarmentInputValidationResult {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isEmpty()) {
            return GarmentInputValidationResult.Invalid(
                field = "title",
                userMessage = "Please enter a garment name (e.g., Cashmere Overshirt)."
            )
        }
        if (trimmedTitle.length > 80) {
            return GarmentInputValidationResult.Invalid(
                field = "title",
                userMessage = "Garment name must be 80 characters or fewer."
            )
        }
        val cleanPrice = priceText.trim()
        if (cleanPrice.isNotEmpty()) {
            val parsed = cleanPrice.toDoubleOrNull()
            if (parsed == null || parsed < 0.0 || parsed > 100_000.0) {
                return GarmentInputValidationResult.Invalid(
                    field = "price",
                    userMessage = "Enter a valid non-negative purchase price (e.g., 180)."
                )
            }
        }
        return GarmentInputValidationResult.Valid
    }
}
