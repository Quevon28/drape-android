package com.drape.app.core.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Keyless backend proxy endpoint for AI outfit styling.
 * No third-party LLM API keys are ever stored in the Android client; authentication and
 * model invocation are handled server-side by the proxy configured in `BuildConfig.AI_PROXY_BASE_URL`.
 */
interface DrapeAiStylistApi {
    @POST("v1/stylist/compose-outfit")
    suspend fun generateOutfit(
        @Body request: AiOutfitGenerationRequest
    ): AiOutfitGenerationResponse
}
