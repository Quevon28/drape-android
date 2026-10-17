package com.drape.app

import com.drape.app.core.data.remote.AiOutfitGenerationRequest
import com.drape.app.core.data.remote.AiOutfitGenerationResponse
import com.drape.app.core.data.remote.AiStylingHarmonyBreakdown
import com.drape.app.core.data.remote.DrapeAiStylistApi
import com.drape.app.core.data.remote.DrapeAiStylistEngine
import com.drape.app.core.data.remote.StylistEngineSource
import com.drape.app.core.model.DefaultWeatherPresets
import com.drape.app.core.model.OccasionVibe
import com.drape.app.core.model.StarterCapsuleCatalog
import com.drape.app.core.security.DrapeSecurityPolicy
import com.drape.app.core.security.ProxyEndpointValidationResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class SecurityAndOfflineResilienceTest {

    @Test
    fun securityPolicy_rejectsCleartextHttpAndEmbeddedApiKeys() {
        val cleartext = DrapeSecurityPolicy.validateProxyEndpointUrl("http://proxy.drape.app/v1")
        assertTrue(cleartext is ProxyEndpointValidationResult.RejectedInsecureEndpoint)

        val leakedOpenAiKey = DrapeSecurityPolicy.validateProxyEndpointUrl(
            "https://proxy.drape.app/v1?api_key=sk-proj-1234567890abcdefghij"
        )
        assertTrue(leakedOpenAiKey is ProxyEndpointValidationResult.RejectedInsecureEndpoint)

        val leakedGeminiKey = DrapeSecurityPolicy.validateProxyEndpointUrl(
            "https://proxy.drape.app/v1?key=AIzaSyD1234567890abcdefghijklmnop"
        )
        assertTrue(leakedGeminiKey is ProxyEndpointValidationResult.RejectedInsecureEndpoint)

        val placeholder = DrapeSecurityPolicy.validateProxyEndpointUrl(
            "https://api.drape-stylist.example.com/"
        )
        assertEquals(ProxyEndpointValidationResult.UnconfiguredPlaceholder, placeholder)

        val valid = DrapeSecurityPolicy.validateProxyEndpointUrl(
            "https://stylist-proxy.drape.app"
        )
        assertTrue(valid is ProxyEndpointValidationResult.ValidRemoteProxy)
        assertEquals(
            "https://stylist-proxy.drape.app/",
            (valid as ProxyEndpointValidationResult.ValidRemoteProxy).normalizedHttpsUrl
        )
    }

    @Test
    fun securityPolicy_blocksDirectoryTraversalAndAllowsAppPrivateSubpaths() {
        val allowedRoot = "/data/user/0/com.drape.app/files/garments"

        assertTrue(
            DrapeSecurityPolicy.isSafeAppPrivatePath(
                candidateCanonicalPath = "/data/user/0/com.drape.app/files/garments/garment_01.jpg",
                allowedRootCanonicalPath = allowedRoot
            )
        )
        assertFalse(
            DrapeSecurityPolicy.isSafeAppPrivatePath(
                candidateCanonicalPath = "/data/user/0/com.drape.app/shared_prefs/secrets.xml",
                allowedRootCanonicalPath = allowedRoot
            )
        )
        assertFalse(
            DrapeSecurityPolicy.isSafeAppPrivatePath(
                candidateCanonicalPath = "/data/user/0/com.drape.app/files/garments_other/file.jpg",
                allowedRootCanonicalPath = allowedRoot
            )
        )
    }

    @Test
    fun aiStylistEngine_gracefullyFallsBackToOnDeviceWhenNetworkThrowsIOException() = runTest {
        val failingApi = object : DrapeAiStylistApi {
            override suspend fun generateOutfit(
                request: AiOutfitGenerationRequest
            ): AiOutfitGenerationResponse {
                throw IOException("Simulated unreachable backend proxy")
            }
        }

        val engine = DrapeAiStylistEngine(
            remoteApi = failingApi,
            isRemoteEndpointConfigured = true,
            isOnlineProvider = { true }
        )

        val lockedId = "garment_trench"
        val response = engine.generateOrRefineOutfit(
            wardrobe = StarterCapsuleCatalog.InitialGarments,
            occasion = OccasionVibe.OFFICE_TAILORED,
            weather = DefaultWeatherPresets.first(),
            lockedGarmentIds = setOf(lockedId)
        )

        assertEquals(StylistEngineSource.ON_DEVICE_VISION_ENGINE, response.engineSource)
        assertTrue(response.selectedGarmentIds.isNotEmpty())
        assertTrue(lockedId in response.selectedGarmentIds)
    }

    @Test
    fun aiStylistEngine_rejectsRemoteResponseThatDropsLockedItemsOrHallucinatesIds() = runTest {
        val invalidRemoteApi = object : DrapeAiStylistApi {
            override suspend fun generateOutfit(
                request: AiOutfitGenerationRequest
            ): AiOutfitGenerationResponse {
                return AiOutfitGenerationResponse(
                    suggestedTitle = "Hallucinated Look",
                    selectedGarmentIds = listOf("fake_id_999", "fake_id_888"),
                    rationale = "Invalid IDs not in user's wardrobe",
                    harmony = AiStylingHarmonyBreakdown(90, 90, 90, 90),
                    combinationSignature = "fake_id_888|fake_id_999"
                )
            }
        }

        val engine = DrapeAiStylistEngine(
            remoteApi = invalidRemoteApi,
            isRemoteEndpointConfigured = true,
            isOnlineProvider = { true }
        )

        val lockedId = "garment_poplin_shirt"
        val response = engine.generateOrRefineOutfit(
            wardrobe = StarterCapsuleCatalog.InitialGarments,
            occasion = OccasionVibe.EVERYDAY_MINIMAL,
            weather = DefaultWeatherPresets.first(),
            lockedGarmentIds = setOf(lockedId)
        )

        // Falls back to on-device engine and guarantees only real closet IDs + locked item
        val realIds = StarterCapsuleCatalog.InitialGarments.map { it.id }.toSet()
        assertEquals(StylistEngineSource.ON_DEVICE_VISION_ENGINE, response.engineSource)
        assertTrue(response.selectedGarmentIds.all { it in realIds })
        assertTrue(lockedId in response.selectedGarmentIds)
    }
}
