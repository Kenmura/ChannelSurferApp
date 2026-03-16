# ChannelSurferApp — Project Status

> **Last updated**: 2026-03-16  
> **Status**: Active development — core Android TV wrapper is functional, several roadmap items still outstanding.

---

## What Is This?

An Android TV app that wraps [channelsurfer.tv](https://channelsurfer.tv) in a full-screen native WebView, making it launchable from the standard TV home screen via the Leanback / LEANBACK_LAUNCHER intent.

---

## Project Structure

```
ChannelSurferApp/
├── app/
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── kotlin/tv/channelsurfer/app/
│       │   ├── MainActivity.kt           # Entry point
│       │   ├── TvWebViewSetup.kt         # WebView config helper
│       │   ├── RemoteInputManager.kt     # TV remote key handler
│       │   └── TvPlaybackLifecycle.kt    # Screen-wake + audio focus lifecycle
│       └── res/
│           ├── drawable/  (app_icon, tv_banner, tv_splash_background)
│           ├── layout/activity_main.xml
│           └── values/  (strings, styles)
├── .github/workflows/
│   ├── build.yml                         # CI: debug APK on push/PR to main
│   └── android-release.yml               # CD: signed release APK+AAB on tag push
└── build.gradle.kts / settings.gradle.kts
```

---

## Build Config

| Property | Value |
|---|---|
| Language | Kotlin |
| Package | `tv.channelsurfer.app` |
| `minSdk` | 21 (Android 5.0 Lollipop) |
| `targetSdk` | 34 |
| `compileSdk` | 35 |
| `versionCode` | 1 |
| `versionName` | `1.0.0` |
| JVM target | 17 |
| Key dependencies | `androidx.core:core-ktx:1.15.0`, `androidx.webkit:webkit:1.12.1` |

---

## Implemented Features

### ✅ Core WebView (`MainActivity.kt` + `TvWebViewSetup.kt`)
- Loads `https://channelsurfer.tv` in a full-screen `WebView`
- JavaScript, DOM storage, database storage enabled
- `mediaPlaybackRequiresUserGesture = false` (essential for TV auto-play)
- Wide viewport mode; zoom controls disabled
- Scrollbars hidden for clean TV experience
- URL state saved/restored across activity restarts via `onSaveInstanceState`
- `WebChromeClient` set for HTML5 video support

### ✅ Immersive Mode (`TvWebViewSetup.configureImmersiveMode`)
- API 30+: uses `WindowInsetsController` to hide status + nav bars with swipe-to-show-transient behaviour
- API < 30: uses deprecated `systemUiVisibility` flags for backward compatibility

### ✅ TV Remote Input (`RemoteInputManager.kt`)
- **Back button**: navigates WebView history; falls back to system exit
- **D-Pad (Up/Down/Left/Right/Center) + Enter**: delegated to native WebView focus navigation
- **Media keys** (Play, Pause, PlayPause, Stop) + **Menu**: injected as `KeyboardEvent` JavaScript events dispatched on `document`
- Clean interface: `MainActivity` forwards `dispatchKeyEvent` → `RemoteInputManager.handleKeyEvent()`

### ✅ Screen Wake + Audio Focus (`TvPlaybackLifecycle.kt`)
- Implements `DefaultLifecycleObserver` (requires `lifecycle-runtime` or similar to wire up)
- `onStart` → adds `FLAG_KEEP_SCREEN_ON`, requests `AUDIOFOCUS_GAIN`
- `onStop` → clears `FLAG_KEEP_SCREEN_ON`, abandons audio focus
- API-safe: uses `AudioFocusRequest.Builder` on API 26+, deprecated path below
- **⚠️ Integration gap**: `TvPlaybackLifecycle` is NOT currently wired into `MainActivity`. It was created as a standalone class but `MainActivity` doesn't extend `AppCompatActivity` / `LifecycleOwner` and does not register it. Screen-wake is handled separately inline in `MainActivity` (`FLAG_KEEP_SCREEN_ON` added in `onCreate`).

### ✅ Android TV Launcher Integration (`AndroidManifest.xml`)
- `LEANBACK_LAUNCHER` intent category declared → app appears on TV home screen
- `android.software.leanback` feature declared (`required=true`)
- `android.hardware.touchscreen` declared `required=false`
- TV banner: `@drawable/tv_banner` (vector drawable placeholder)
- App icon: `@drawable/app_icon` (vector drawable placeholder)
- Theme: `Theme.ChannelSurfer` — full-screen, no action bar, black splash background

### ✅ CI/CD (`build.yml` + `android-release.yml`)
- **build.yml**: triggers on push/PR to `main`; builds debug APK; uploads as GitHub Actions artifact (30-day retention)
- **android-release.yml**: triggers on any Git tag push; builds release APK + AAB; publishes GitHub Release with auto-generated notes
- **⚠️ Signing is stubbed**: keystore decoding step is commented out. Secrets (`KEYSTORE_BASE64`, `KEY_ALIAS`, `KEY_PASSWORD`, `STORE_PASSWORD`) need to be added to GitHub repo settings before release builds are signed

---

## Known Gaps / Outstanding Roadmap

| Item | Status | Notes |
|---|---|---|
| Signed release builds | ❌ Not done | Keystore signing stub exists in `android-release.yml`; secrets not yet configured |
| GitHub Releases with APK downloads | ⚠️ Partial | Workflow exists but untested without signing |
| Custom splash/loading screen | ❌ Not done | `tv_splash_background.xml` drawable exists; no loading overlay in WebView |
| Remote-friendly overlay controls | ❌ Not done | No on-screen UI overlay implemented |
| Better error/offline handling | ❌ Not done | `WebViewClient` currently allows all URLs; no error page or offline fallback |
| `TvPlaybackLifecycle` wired up | ❌ Not done | Class exists but not registered as a lifecycle observer in `MainActivity` |
| `TvWebViewSetup` class used | ❌ Not done | Class exists but `MainActivity` duplicates the setup logic inline instead of calling it |
| Proguard / minification | ❌ Not done | `isMinifyEnabled = false` in release build type |
| Tests | ❌ None | No unit or instrumented tests exist |
| `lifecycleOwner` dependency | ⚠️ Possible missing | `TvPlaybackLifecycle` imports `androidx.lifecycle` but `app/build.gradle.kts` only lists `core-ktx` and `webkit` — may need `lifecycle-runtime-ktx` added |

---

## Recommended Next Steps (Priority Order)

1. **Wire up `TvPlaybackLifecycle`** — either convert `MainActivity` to `AppCompatActivity` or manually call `onStart`/`onStop` hooks, and add `lifecycle-runtime-ktx` dependency to `app/build.gradle.kts`
2. **Deduplicate WebView setup** — have `MainActivity` delegate to `TvWebViewSetup.configureWebView()` instead of its own inline copy
3. **Configure signing** — add keystore secrets to GitHub, uncomment the keystore step in `android-release.yml`, and enable ProGuard
4. **Add offline/error handling** — implement a `WebViewClient.onReceivedError` fallback page
5. **Add a loading overlay** — show a TV-friendly splash while the first page loads
6. **Write basic smoke tests** — at minimum, an instrumented test that launches `MainActivity` and checks the WebView loads
