# DRAPE (`com.drape.app`)

**DRAPE** is a native Android digital wardrobe and AI outfit-styling application built with **Kotlin** and **Jetpack Compose**, featuring an Apple-inspired **Liquid Glass** design system, **Room** local persistence, **CameraX** and **Android Photo Picker** garment capture, and a **keyless backend AI proxy** with full on-device offline fallback.

## Key Features

- **Liquid Glass Design System (`core/designsystem/`)**: Multi-tier translucent refraction fills, cached inner highlights (`drawWithCache`), diagonal specular borders, `tnum` tabular numerals (`AnimatedOdometerText`), tactile spring physics (`Motion.kt`), and adaptive responsive column grids (2 columns on compact phones, 3 on foldables, 4 on tablets).
- **4-Screen Navigation (`navigation/`, `feature/`)**:
  - **Today (`TodayScreen.kt`)**: Climate/weather context selector, Today's Scheduled Look card, one-tap wear logging, and recent wear history timeline.
  - **Wardrobe (`WardrobeScreen.kt`)**: Native CameraX garment capture (`GarmentCameraCaptureDialog.kt`), Android Photo Picker gallery import (`PickVisualMedia`), 1440px bitmap downscaling (`GarmentImageStorageManager.kt`), full garment detail/edit/delete sheet (`GarmentDetailSheet.kt`), search, and category/season/color swatch filtering & sorting.
  - **Style (`StyleScreen.kt`)**: AI-assisted full outfit creation using the user's actual photographed garments, per-item Lock/Unlock anchoring, non-repeating Shuffle, 7 structured refinement directives + custom natural-language instructions, and Lookbook favorites.
  - **Profile (`ProfileScreen.kt`)**: Wardrobe utilization & cost-per-wear analytics, persistent Personal Style Preferences (`UserPreferencesDataSource.kt` via Jetpack DataStore), chronological Wear History log, and dark/light Liquid Glass theme controls.
- **Security & Offline Resilience (`core/security/`, `core/network/`)**:
  - Zero third-party AI API keys embedded in the APK (`DrapeSecurityPolicy.kt`, `network_security_config.xml` with `cleartextTrafficPermitted="false"`).
  - Automatic offline fallback (`DrapeAiStylistEngine.kt` + `DrapeConnectivityMonitor.kt`) ensuring all wardrobe and outfit-styling features work 100% offline.

## Building the APK

### Option 1: GitHub Actions (Recommended)
Push this repository to GitHub. The workflow in `.github/workflows/android-build.yml` automatically:
1. Sets up Temurin JDK 17 and Gradle 8.9.
2. Runs unit tests (`gradle testDebugUnitTest`) and Android Lint (`gradle lintDebug`).
3. Builds both the **Debug APK** (`drape-debug-apk`) and the **Signed Release APK** (`drape-signed-release-apk`) and uploads them as downloadable workflow artifacts.

### Option 2: Local Build with Android SDK
Ensure `ANDROID_HOME` is set to an Android SDK installation with Platform 35, then run:
```bash
gradle testDebugUnitTest assembleDebug assembleRelease
```
