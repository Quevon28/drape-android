package com.drape.app.core.security

import androidx.compose.runtime.Immutable
import java.io.File

/**
 * Security validation result for the keyless backend AI proxy endpoint.
 */
@Immutable
sealed interface ProxyEndpointValidationResult {
    data class ValidRemoteProxy(val normalizedHttpsUrl: String) : ProxyEndpointValidationResult
    data object UnconfiguredPlaceholder : ProxyEndpointValidationResult
    data class RejectedInsecureEndpoint(val reason: String) : ProxyEndpointValidationResult
}

/**
 * Central security policy enforcing:
 * 1. Zero embedded AI provider API keys in client URLs, headers, or payloads.
 * 2. Strict HTTPS-only transport for remote proxy calls.
 * 3. Canonical path-traversal protection for app-private garment image storage.
 * 4. Removal of device-internal file paths before transmitting garment descriptors to the proxy.
 */
object DrapeSecurityPolicy {

    private val FORBIDDEN_SECRET_PATTERNS = listOf(
        Regex("sk-[a-zA-Z0-9_-]{12,}"),
        Regex("AIza[0-9A-Za-z_-]{20,}"),
        Regex("(?i)(api_key|apikey|client_secret|access_token)\s*[:=]"),
        Regex("(?i)Bearer\s+sk-")
    )

    fun containsForbiddenClientSecret(input: String): Boolean {
        if (input.isBlank()) return false
        return FORBIDDEN_SECRET_PATTERNS.any { it.containsMatchIn(input) }
    }

    fun validateProxyEndpointUrl(rawUrl: String): ProxyEndpointValidationResult {
        val trimmed = rawUrl.trim()
        if (trimmed.isEmpty()) {
            return ProxyEndpointValidationResult.UnconfiguredPlaceholder
        }
        if (containsForbiddenClientSecret(trimmed)) {
            return ProxyEndpointValidationResult.RejectedInsecureEndpoint(
                "Proxy URL must not contain embedded API keys or secret tokens."
            )
        }
        if (!trimmed.startsWith("https://", ignoreCase = true)) {
            return ProxyEndpointValidationResult.RejectedInsecureEndpoint(
                "Cleartext HTTP is prohibited; AI proxy URL must use https://."
            )
        }
        if (trimmed.contains("example.com", ignoreCase = true) ||
            trimmed.contains("localhost", ignoreCase = true)
        ) {
            return ProxyEndpointValidationResult.UnconfiguredPlaceholder
        }
        val normalized = if (trimmed.endsWith("/")) trimmed else "$trimmed/"
        return ProxyEndpointValidationResult.ValidRemoteProxy(normalized)
    }

    /**
     * Verifies that a target file path resolves strictly inside the allowed app-private directory,
     * blocking `../` path traversal or external storage deletion.
     */
    fun isSafeAppPrivatePath(
        candidateCanonicalPath: String,
        allowedRootCanonicalPath: String
    ): Boolean {
        val normalizedRoot = allowedRootCanonicalPath.trimEnd(File.separatorChar) + File.separatorChar
        return candidateCanonicalPath.startsWith(normalizedRoot)
    }

    /**
     * Sanitizes a filename to prevent directory traversal or shell/URI injection.
     */
    fun sanitizeFileName(rawName: String): String {
        val cleaned = rawName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
            .trim('.')
        return cleaned.ifBlank { "garment_photo.jpg" }
    }
}
