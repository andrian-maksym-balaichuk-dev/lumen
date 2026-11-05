# Development

## Requirements

- Android Studio, or a JDK 17+ with the Android SDK (API 37).
- The build uses a JDK 17 toolchain. If none is installed, Gradle downloads one automatically.

## Adding a module

1. Create the folder, e.g. `core/network/`, with a `build.gradle.kts` that applies one
   convention plugin:

   ```kotlin
   plugins {
       alias(libs.plugins.lumen.android.library)
   }

   android {
       namespace = "sk.mpage.lumen.core.network"
   }
   ```

2. Add `include(":core:network")` to `settings.gradle.kts`.
3. Add new dependencies to `gradle/libs.versions.toml`. Versions are never written in module
   build files, and only stable versions are allowed.

Sources go in `src/main/kotlin/sk/mpage/lumen/<core|feature>/<name>/` and tests in
`src/test/kotlin/...`.

## Code style

- Formatting: ktlint (`ktlint_official` style, max line length 120, trailing commas), applied by
  Spotless and configured in `.editorconfig`.
- Library modules keep declarations `internal` unless another module uses them. `core:common`,
  `core:model` and `core:domain` use Kotlin explicit API mode.
- User-facing text lives in `strings.xml`, in English (`values/`) and Slovak (`values-sk/`).
- Time is read through an injected clock, never `System.currentTimeMillis()` or `Instant.now()`.
- Coroutine cancellation is never swallowed; `runCatching` is not used around suspend calls.
- Naming: implementations are named by what makes them different
  (`OfflineFirstCategoryRepository`, not `CategoryRepositoryImpl`); use cases are verb + noun +
  `UseCase`; tests are backtick sentences describing behaviour.

## Tests

| Level | Where | Tools |
|---|---|---|
| Pure unit | `core:common`, `core:model`, `core:domain` | JUnit 4, kotlin.test, coroutines-test |
| Data | `core:network`, `core:database`, `core:datastore`, `core:data` | MockWebServer, in-memory Room on Robolectric, temporary DataStore |
| ViewModel | `feature:*` | Fakes from `core:testing`, Turbine, virtual time |
| Compose UI | `feature:*`, `core:designsystem` | Compose UI test on Robolectric |
| Worker | `core:work` | WorkManager test helpers |