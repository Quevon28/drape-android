# DRAPE Keyless Backend AI Proxy & Security Architecture

## 1. Zero Client-Side API Keys
DRAPE (`com.drape.app`) never embeds third-party AI API keys (`GEMINI_API_KEY`, `OPENAI_API_KEY`, or `ANTHROPIC_API_KEY`) inside the Android APK, Gradle resources, or `BuildConfig`.

- **Client Configuration**: `app/build.gradle.kts` exposes only `BuildConfig.AI_PROXY_BASE_URL` (the HTTPS URL of your backend proxy).
- **Runtime Enforcement (`DrapeSecurityPolicy.kt` & `NetworkModule.kt`)**:
  - Enforces HTTPS-only (`cleartextTrafficPermitted="false"` in `res/xml/network_security_config.xml`).
  - Rejects any endpoint URL or outgoing request containing secret token patterns (`sk-`, `AIza`, `api_key=`).
  - Strips device-local filesystem paths (`localImageUri`) from outgoing JSON payloads, sending only garment metadata and compact `48x48` JPEG thumbnails (`thumbnailBase64`).

## 2. Graceful Offline & Fallback Behavior
If the user's device is offline (`DrapeConnectivityMonitor.isCurrentlyOnline() == false`), if `AI_PROXY_BASE_URL` is unconfigured, or if the remote proxy returns an error or invalid garment IDs:
- `DrapeAiStylistEngine` automatically executes `composeWithOnDeviceEngine(...)` on-device.
- Core wardrobe management, CameraX photography, Photo Picker gallery import, full outfit generation, per-item locking, non-repeating shuffles, iterative refinements, wear history logging, and DataStore preferences remain 100% functional offline.
