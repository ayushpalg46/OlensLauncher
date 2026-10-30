# Olens Launcher 🚀

[![CI/CD Pipeline](https://github.com/ayushpalg46/OlensLauncher/actions/workflows/build-apk.yml/badge.svg)](https://github.com/ayushpalg46/OlensLauncher/actions/workflows/build-apk.yml)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.20-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Rust](https://img.shields.io/badge/Rust-1.98.1-black?logo=rust&logoColor=white)](https://www.rust-lang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-2024.09.00-4285F4?logo=android&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Android](https://img.shields.io/badge/Min_SDK-26%20(Android%208.0%2B)-3DDC84?logo=android&logoColor=white)](https://android.com)

A high-performance, minimalist, ergonomic Android launcher reverse-engineered for fluid single-handed use. Built exclusively in **100% pure Kotlin** (Jetpack Compose UI, Android System Services) and **pure Rust** (Native Mathematical Engine & Nucleo/FZF-style Sub-millisecond Fuzzy Indexer via JNI).

---

## ⚡ Core Highlights

- **Pure Kotlin + Rust Architecture**: Zero bloated third-party frameworks. Zero Java or C++ wrappers.
- **Trigonometric Wave Scrubber**: Vertical alphabet rail with Gaussian/cosine wave protrusion and floating thumb magnifier bubble extracted directly from Niagara Launcher bytecode analysis.
- **Sub-Millisecond Fuzzy Search**: Native Rust engine running instant acronym (`yt` ➔ *YouTube*), prefix, substring, and subsequence matching.
- **One-Handed Ergonomic Gestures**: Swipe down anywhere on the home screen to expand notifications; swipe up to open instant search.
- **Deep App Shortcuts (Pop-up Actions)**: Instant access to native static and dynamic app actions (`LauncherApps.getShortcuts`).
- **Live Status Bar Notifications**: Intercepts active notifications and displays dynamic inline preview subtitles beneath app rows.
- **Full Customization Suite**: 3rd-party icon pack support, 7 Niagara accent palettes, font family selection, custom icon shapes/sizes, and wallpaper dimming.
- **Emergency Escape Subsystem**: Guaranteed safety mechanism to immediately exit Olens Launcher and restore your stock OEM launcher (Samsung One UI, Pixel Launcher, MIUI, etc.) at any time.

---

## 🎨 Customization Engine

Olens Launcher includes a comprehensive, modular 4-tab control center:

| Category | Available Customizations |
| :--- | :--- |
| **🎨 Theme & Look** | • **Wallpaper Dimming Scrim**: Configurable dimming (`0%`, `25%`, `50%`, `75%`) for crystal-clear readability.<br>• **Pure AMOLED Black**: True `#000000` pitch-black mode for battery savings on OLED displays.<br>• **Signature Accent Palettes**: *Niagara Cyan*, *Emerald Green*, *Sunset Orange*, *Amethyst Purple*, *Crimson Red*, *Sunflower Gold*, and *Pure White*.<br>• **Font Families**: *System Default*, *Sans-Serif*, *Serif*, *Monospace*, and *Cursive*.<br>• **System Wallpaper**: One-tap trigger to open the device's native wallpaper chooser. |
| **📱 Icons & Shapes** | • **Third-Party Icon Packs**: Native XML parser for Nova, Apex, ADW, and Go Launcher icon packs (`IconPackManager`).<br>• **Adaptive Icon Masking**: *Circle*, *Rounded Square*, *Squircle*, and *Teardrop*.<br>• **Icon Sizing**: *32dp* (Compact), *40dp* (Standard), *48dp* (Large), and *56dp* (Huge).<br>• **Minimalist Mode**: Toggle to hide app labels on the home favorites list. |
| **✋ Ergonomics** | • **Scrubber Rail Placement**: Right-handed (default) or Left-handed mode with dynamic layout mirroring.<br>• **Tactile Haptic Feedback**: Differentiated tactile clicks (`CLOCK_TICK` for letters, heavy `LONG_PRESS` for `★` favorites).<br>• **Time Format**: Switch between 24-Hour (`15:45`) and 12-Hour (`3:45 PM`).<br>• **At-a-Glance Glanceables**: Live battery percentage, charging state, date, and weather indicator. |
| **🛡️ System & Escape** | • **One-Tap Emergency Escape**: Instantly exit and restore original stock launcher.<br>• **Hidden Apps Manager**: Hide sensitive apps and manage unhiding.<br>• **App Renaming**: Assign custom nicknames to any installed app.<br>• **Cache Reloading**: Fast rescan of installed applications. |

---

## 🛡️ Emergency Escape Subsystem

To guarantee you never get stuck while experimenting with Olens Launcher:

1. **One-Tap Revert Button**:
   - Available at the top of the **Settings Dialog** and inside the search drawer (typing `"escape"`, `"exit"`, or `"old"`).
2. **Stock Launcher Preference Clearing**:
   - Calls `clearPackagePreferredActivities()` on Olens Launcher so Android removes default home associations.
3. **Automatic OEM Detection**:
   - Detects and directly launches installed OEM launchers (Samsung One UI, Pixel Launcher, Xiaomi MIUI/HyperOS, OnePlus OxygenOS, Moto App Launcher).
4. **Settings Chooser Fallback**:
   - Launches Android's official `ACTION_HOME_SETTINGS` or `ACTION_MANAGE_DEFAULT_APPS_SETTINGS` dialog.

---

## 🏗️ Architecture & Codebase Map

```
OlensLauncher/
├── .agents/                               # Multi-Agent Coordination Protocol (MACP)
│   ├── ARCHITECTURE.md                    # JNI contracts, state flows, and intent routes
│   ├── COMMUNICATION.md                   # Chronological cross-agent review ledger
│   ├── LOCKS.json                         # Concurrency file locks to prevent edit collisions
│   ├── MESSAGES.jsonl                     # Real-time agent event stream
│   ├── PROTOCOL.md                        # Rules of engagement & branch hygiene
│   ├── STATUS.json                        # Global system build and health state
│   └── TASKS.json / TASKS.md              # Live task backlog & sprint tracker
├── .github/
│   └── workflows/
│       └── build-apk.yml                  # GitHub Actions CI/CD (NDK r26b + Rust + Gradle 8.7)
├── app/                                   # Android Kotlin Application
│   ├── build.gradle.kts
│   ├── src/main/
│   │   ├── AndroidManifest.xml            # Launcher intents & notification permissions
│   │   ├── kotlin/com/olens/launcher/
│   │   │   ├── MainActivity.kt            # Edge-to-edge Compose container
│   │   │   ├── OlensApplication.kt        # Global application singleton
│   │   │   ├── core/
│   │   │   │   └── NativeEngine.kt        # Kotlin JNI Gateway to libolens_core.so
│   │   │   ├── data/
│   │   │   │   ├── AppRepository.kt       # LauncherApps service & reactive state flows
│   │   │   │   ├── IconPackManager.kt     # Third-party icon pack XML parser
│   │   │   │   └── UserPreferences.kt     # Jetpack DataStore preferences schema
│   │   │   ├── model/
│   │   │   │   └── AppItem.kt             # Lightweight immutable app metadata
│   │   │   ├── receiver/
│   │   │   │   └── PackageChangesReceiver.kt # Broadcast receiver for app install/remove
│   │   │   ├── service/
│   │   │   │   └── OlensNotificationListener.kt # Intercepts notifications for inline subtitles
│   │   │   ├── ui/
│   │   │   │   ├── HomeScreen.kt          # Single-column drawer & gesture coordinator
│   │   │   │   ├── components/
│   │   │   │   │   ├── AlphabetScrubber.kt   # Wave rail with thumb magnifier bubble
│   │   │   │   │   ├── AppActionDialog.kt    # Deep shortcuts & contextual menu
│   │   │   │   │   ├── AppRow.kt             # App icon, shape mask, badge & preview
│   │   │   │   │   ├── HomeHeader.kt         # Clock ticker, date, battery & tap intents
│   │   │   │   │   ├── SearchBar.kt          # Fast search input field
│   │   │   │   │   └── SettingsDialog.kt     # 4-tab customization control center
│   │   │   │   └── theme/
│   │   │   │       └── Theme.kt              # Dynamic typography & 7 color palettes
│   │   │   └── util/
│   │   │       ├── AppShortcutsHelper.kt     # Android dynamic & static shortcut invoker
│   │   │       ├── LauncherSwitcherHelper.kt # Stock launcher recovery & escape logic
│   │   │       └── StatusBarHelper.kt        # System notifications shade expansion
│   │   └── res/
│   └── proguard-rules.pro
├── core-engine/                           # Native Rust Shared Library
│   ├── Cargo.toml                         # Zero unnecessary crates (jni only)
│   └── src/
│       └── lib.rs                         # JNI Exports: wave math & Nucleo fuzzy search
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

---

## 🤖 Multi-Agent Collaboration System (`.agents/`)

Olens Launcher is maintained through an autonomous, synchronized **Multi-Agent Coordination Protocol (MACP)** connecting **Antigravity CLI** ⚡ and **Antigravity IDE** 🎨:

- **Domain Ownership**:
  - `CLI`: Rust JNI engine, system listeners, low-level intents, CI/CD, and native unit tests.
  - `IDE`: Jetpack Compose UI/UX, typography tokens, gesture physics, and AMOLED dark mode styling.
- **Lock Registry (`.agents/LOCKS.json`)**: Prevents merge conflicts through mutual exclusion on file edits.
- **Event Bus (`.agents/MESSAGES.jsonl`)**: Threaded peer reviews, approval tokens, and delivery records.

---

## 📦 Downloading & Installing the `.apk`

Because local Android SDK and NDK installations are not required, every commit to `main` is automatically compiled and packaged using GitHub Actions:

1. Open the repository on GitHub: [`https://github.com/ayushpalg46/OlensLauncher`](https://github.com/ayushpalg46/OlensLauncher).
2. Navigate to the **Actions** tab.
3. Select the latest completed workflow run (e.g. *Build & Package Android APK*).
4. Scroll to the **Artifacts** section at the bottom.
5. Download **`OlensLauncher-APKs.zip`**, extract it on your phone, and install **`app-debug.apk`**!

---

## 🧪 Testing & Verification

### Native Rust Engine Tests:
```bash
cargo test --manifest-path core-engine/Cargo.toml
```
*Output: `2 passed, 0 failed` (`test_wave_calculation`, `test_fuzzy_score`).*

### Gradle Verification (CI Environment):
```bash
./gradlew testDebugUnitTest
```

---

## 📄 License
Crafted with ❤️ in Kotlin and Rust. Licensed under the [MIT License](LICENSE).
