# Repository Guidelines

## Project Structure & Module Organization

```
UberFilter/
├── app/src/main/java/com/uberfilter/
│   ├── model/          # Data classes: RideOffer, FilterCriteria, RideEvaluation
│   ├── data/           # Persistence layer (DataStore)
│   ├── domain/         # Business logic: RideEvaluator scoring engine
│   ├── service/        # AccessibilityService, OverlayManager, UberCardParser
│   ├── receiver/       # BootReceiver (auto-restart after reboot)
│   └── ui/             # SettingsViewModel, Compose theme (Color, Theme, Type)
├── app/src/test/       # JVM unit tests
├── app/src/androidTest/ # Instrumented tests (device/emulator)
└── gradle/             # Version catalog (libs.versions.toml)
```

The project follows a clean-architecture layering: **model → data/domain → service/ui**. Keep new classes in the appropriate package. Do not place business logic inside Activity, Service, or Composable functions.

## Build, Test, and Development Commands

| Command | Description |
|---|---|
| `./gradlew assembleDebug` | Build debug APK |
| `./gradlew assembleRelease` | Build release APK (minified via ProGuard) |
| `./gradlew test` | Run JVM unit tests |
| `./gradlew connectedAndroidTest` | Run instrumented tests on a connected device/emulator |
| `./gradlew lint` | Run static analysis |

Open the project in **Android Studio Hedgehog (2023.1.1) or newer**. Sync Gradle after pulling to regenerate the build cache.

## Coding Style & Naming Conventions

- **Language**: Kotlin. Target JVM 17.
- **Indentation**: 4 spaces (no tabs).
- **Class naming**: PascalCase (`UberCardParser`, `FilterCriteriaStore`).
- **Function/property naming**: camelCase (`valuePerKm`, `evaluate()`).
- **Package naming**: lowercase dot-separated (`com.uberfilter.domain`).
- **Singletons**: Use Kotlin `object` declarations (e.g., `object RideEvaluator`).
- **Models**: Prefer `data class` with named parameters for DTOs.
- **Formatting**: Rely on Android Studio's built-in Kotlin formatter (Ctrl+Alt+L / Cmd+Opt+L). No additional lint tool is configured yet — keep code consistent with existing files.

## Testing Guidelines

- **Framework**: JUnit (unit tests), Espresso/Compose testing (instrumented).
- **Unit tests** go in `app/src/test/` — no Android framework dependencies.
- **Instrumented tests** go in `app/src/androidTest/` — require a device or emulator.
- **Naming**: Match the class under test with a `Test` suffix (e.g., `RideEvaluatorTest`).
- **Coverage goal**: Cover the `domain` and `model` packages thoroughly. The `service` package (AccessibilityService, OverlayManager) is harder to unit-test — prefer manual testing or instrumented tests for those.

## Commit & Pull Request Guidelines

- **Commit messages**: Use Portuguese for consistency with the existing history. Keep the first line short and descriptive (≤ 72 characters). Examples from the repo:
  ```
  Melhoria na velocidade de apresentar o Popup na tela
  Ajuste add campo filtro receita por hora e adicionou modal mais proxima a barra top android
  ```
- **Pull requests**: Link any related issue. Include a brief summary of what changed and why. For UI/service changes, attach a screenshot or screen recording of the popup behavior.
- **Branching**: Work on feature branches off `main`. Rebase before merging to keep history linear.

## Security & Configuration Tips

- **Never commit** `local.properties` or API keys. The `.gitignore` already excludes them.
- Release builds enable **ProGuard** (`isMinifyEnabled = true`). Add any reflective or AccessibilityService-related classes to `proguard-rules.pro` if issues arise.
- The app requires system-level permissions (`SYSTEM_ALERT_WINDOW`, `BIND_ACCESSIBILITY_SERVICE`). Do not hardcode package names — keep them in configuration constants (see `service/` package for current Uber Driver package references).
