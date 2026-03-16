---
type: project-status
project: ChannelSurferApp
repo: Kenmura/ChannelSurferApp
last_updated: 2026-03-16
---

# ChannelSurferApp — Agent Knowledge: Project Status

ChannelSurferApp is an Android TV app that wraps [channelsurfer.tv](https://channelsurfer.tv) in a native full-screen WebView, enabling it to launch from the Android TV home screen via the Leanback launcher.

See also: `/STATUS.md` in the repo root for a full human-readable breakdown.

---

## Architecture Overview

| File | Role |
|---|---|
| `MainActivity.kt` | Entry point. Sets up WebView using `TvWebViewSetup`, dispatches key events to `RemoteInputManager`, manages lifecycle with `TvPlaybackLifecycle`, and handles splash/offline views. Extends `ComponentActivity`. |
| `TvWebViewSetup.kt` | Helper class to configure a `WebView` for TV and enable immersive mode without system bars. |
| `RemoteInputManager.kt` | Handles hardware remote key events: Back (WebView history), D-Pad (native focus nav), Media keys + Menu (JS `KeyboardEvent` injection). |
| `TvPlaybackLifecycle.kt` | `DefaultLifecycleObserver` managing `FLAG_KEEP_SCREEN_ON` and `AudioFocus`. |
| `AndroidManifest.xml` | TV launcher (`LEANBACK_LAUNCHER`), `android.software.leanback` feature, touchscreen not required, TV banner, INTERNET permission. |
| `.github/workflows/build.yml` | CI: builds debug APK on push/PR to `main`. Uploads artifact (30-day retention). |
| `.github/workflows/android-release.yml` | CD: builds release APK + AAB on any Git tag. Publishes GitHub Release. Configured for secret signing vars when available. |

---

## Build Config Snapshot

- **Package**: `tv.channelsurfer.app`
- **minSdk**: 21 | **targetSdk**: 34 | **compileSdk**: 35
- **versionName**: `1.0.0` / **versionCode**: `1`
- **Dependencies**: `androidx.core:core-ktx:1.15.0`, `androidx.webkit:webkit:1.12.1`
- Missing (None)

---

## Implemented ✅

- Full-screen WebView loading `https://channelsurfer.tv`
- JavaScript, DOM storage, database, media autoplay enabled
- Immersive mode (API 30+ `WindowInsetsController`; legacy `systemUiVisibility` fallback)
- TV remote: Back, D-Pad, media keys, Menu handled
- Manifest fully configured for Android TV (`LEANBACK_LAUNCHER`, leanback feature, banner icon)
- CI debug build + CD release build workflows
- Splash screen overlay while network loads
- Offline error fallback overlay
- Keep screen on during playback + audio focus lifecycle mapping
- Re-useable `TvWebViewSetup` configuration

---

## Known Gaps / TODOs

1. **No tests** — zero unit or instrumented tests
2. **ProGuard disabled** — `isMinifyEnabled = false` in release build type

## Recommended Next Steps (Priority Order)

1. Write smoke tests (instrumented)
2. Setup Proguard for minification
