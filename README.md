# ChannelSurferApp

An Android TV app for viewing [channelsurfer.tv](https://channelsurfer.tv) — watch YouTube like it's cable TV, right from your smart TV.

## Features

- 🖥️ **Full-screen WebView** — loads channelsurfer.tv in a native Android TV wrapper
- 🎮 **D-pad navigation** — navigate the site using your TV remote (arrows, OK, back)
- 🎬 **Media key support** — play/pause controls forwarded to the web player
- 📺 **TV launcher integration** — appears in the Android TV home screen with a banner

## Building

### Via GitHub Actions (recommended)
Push to `main` or open a PR — the CI workflow will build a debug APK and upload it as an artifact.

### Locally (requires Android SDK)
```bash
./gradlew assembleDebug
```

The APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

## Installing on Android TV

1. Download the APK from the GitHub Actions build artifacts
2. Sideload onto your Android TV:
   ```bash
   adb install app-debug.apk
   ```
3. Find "Channel Surfer" in your TV's app list

## Tech Stack

- **Language**: Kotlin
- **Target**: Android TV (API 21+, targeting API 34)
- **UI**: WebView with full-screen layout
- **CI/CD**: GitHub Actions

## Roadmap

- [ ] Signed release builds
- [ ] GitHub Releases with APK downloads
- [ ] Custom splash/loading screen
- [ ] Remote-friendly overlay controls
- [ ] Better error handling for offline scenarios
