# Olens Launcher 🚀

A high-performance, minimalist, ergonomic Android launcher crafted exclusively in **Kotlin** (UI, Jetpack Compose, System Integrations) and **Rust** (Native Core, Fuzzy Indexing, Wave Scrubber Physics via JNI).

---

## ⚡ Core Philosophy & Architecture

Olens Launcher is engineered for maximum speed, ergonomics, and single-handed usability:

- **100% Kotlin & Rust**: Zero bloated third-party dependencies or legacy code.
- **Rust Native Engine (`core-engine/`)**:
  - **Fuzzy Search Algorithm**: Sub-millisecond acronym, prefix, subsequence scoring, and intelligent ranking.
  - **Wave Scrubber Physics**: Gaussian/cosine polynomial wave protrusion mathematics executed at native speed.
- **Ergonomic Wave Alphabet Scrubber**: Silky-smooth vertical touch strip with curved wave distortion and tactile haptic feedback.
- **At-a-Glance Home Header**: Minimalist clock, formatted calendar date, live battery percentage, and charging indicators.
- **Curated Favorites**: Quick access to your daily drivers without cluttering the screen.
- **Fast App Drawer & Search**: Instant keyboard trigger and search-as-you-type.
- **Power Features**:
  - Long-press contextual actions: Favorite, Hide App, App Info, Uninstall.
  - Hidden apps privacy management.
  - Edge-to-edge transparent system bars for seamless wallpaper immersion.
  - Notification listener integration.

---

## 🏗️ Project Structure

```
OlensLauncher/
├── .github/
│   └── workflows/
│       └── build-apk.yml       # GitHub Actions CI/CD to build .apk
├── app/                        # Android application in Kotlin
│   ├── build.gradle.kts
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── kotlin/com/olens/launcher/
│   │   │   ├── MainActivity.kt
│   │   │   ├── OlensApplication.kt
│   │   │   ├── core/NativeEngine.kt
│   │   │   ├── data/AppRepository.kt
│   │   │   ├── data/UserPreferences.kt
│   │   │   ├── model/AppItem.kt
│   │   │   ├── receiver/PackageChangesReceiver.kt
│   │   │   ├── service/OlensNotificationListener.kt
│   │   │   └── ui/
│   │   │       ├── HomeScreen.kt
│   │   │       ├── components/AlphabetScrubber.kt
│   │   │       ├── components/AppActionDialog.kt
│   │   │       ├── components/AppRow.kt
│   │   │       ├── components/HomeHeader.kt
│   │   │       └── components/SearchBar.kt
│   │   └── res/
├── core-engine/                # High-performance Rust Crate
│   ├── Cargo.toml
│   └── src/
│       └── lib.rs              # JNI Exports, Fuzzy Search, Wave Math
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── README.md
```

---

## 🤖 CI/CD Automated APK Builds (GitHub Actions)

Since local Android SDK is not required, GitHub Actions handles the entire pipeline:

1. **Push your commits** to `main` (or trigger `workflow_dispatch` manually).
2. The workflow automatically:
   - Sets up OpenJDK 17 and Android SDK/NDK.
   - Installs Rust and Android target triples (`aarch64-linux-android`, `armv7-linux-androideabi`, `x86_64-linux-android`).
   - Compiles `libolens_core.so` using `cargo-ndk`.
   - Assembles `app-debug.apk` and `app-release.apk`.
   - Uploads APKs under **Actions > Artifacts**.
3. **Downloading the APK**:
   - Go to your repository on GitHub: `https://github.com/ayushpalg46/OlensLauncher`
   - Click the **Actions** tab.
   - Click the latest workflow run.
   - Download the **OlensLauncher-APKs** zip containing the ready-to-install `.apk` files!
