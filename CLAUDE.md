# Blocker ("Шахрай") — Project Context

Android app that rejects incoming calls whose number matches a user-defined mask list (anti-spam / anti-scam). UI language is Ukrainian; app label is "Шахрай". Personal project, single module, no backend.

## Quick facts
- Package / applicationId: `dybarsky.blocker`; Gradle root project `Blocker`, single module `:app`
- Kotlin 2.4.21, AGP 9.5.1, Gradle wrapper 9.6.1 (needs JDK 17+; no system JDK on the dev machine → CLI builds need `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"`), Java 11 source/target
- compileSdk 37 (required by current AndroidX libs), targetSdk 36, **minSdk 29** (required for `RoleManager` call-screening role)
- UI: Jetpack Compose + Material3 (BOM 2026.09.00; `ui-tooling` versions come from the BOM), Navigation Compose 2.10.2 (type-safe `@Serializable` routes), Lifecycle 2.11.0, Activity Compose 1.13.0
- Persistence: Jetpack DataStore 1.3.0 (core `datastore` artifact, used via `dataStore(...)` with a custom JSON `Serializer`) + kotlinx-serialization-json 1.12.0
- No DI framework, no Room, no Hilt, no network. Unit tests live in `app/src/test` (JUnit 4 + coroutines-test); `ExampleInstrumentedTest` is still the default template.
- Version catalog: `gradle/libs.versions.toml`
- Build: `./gradlew assembleDebug`, install: `./gradlew installDebug`. Features must be verified on a device/emulator (call screening can't be unit-tested easily).

## How it works
1. App starts in `BlockerActivity` → `MainScreen` (NavHost, start route `Roles`).
2. `RoleScreen` checks `RoleManager.isRoleHeld(ROLE_CALL_SCREENING)`. If held, it pops itself and navigates to `Home`; otherwise shows an "Активувати" button that launches `createRequestRoleIntent(ROLE_CALL_SCREENING)`; on result it re-checks.
3. `HomeScreen` ("Номери" tab) lists masks in a card (tap a mask to open an Edit/Delete dialog; swipe end-to-start also deletes); a round `+` FAB in the card's bottom-right corner adds one.
4. `EditorScreen` is a navigation **dialog** (`dialog<Route.Editor>`) with a phone-keyboard text field; Save persists, Cancel closes.
5. On an incoming call, the system binds `BlockerService` (`CallScreeningService`, permission `BIND_SCREENING_SERVICE`); it asks `BlockerServiceModel.screen(number)` for a `Verdict`, responds with reject (`rejectCall`, `disallowCall`, `skipCallLog=false`, `skipNotification=true`) or plain allow, and only **after responding** records blocked calls via `record(...)` (best effort; failures never change the outcome).

### Mask matching (`domain/MatcherImpl.kt`)
- `*` matches **any sequence of characters (including none)** anywhere in the mask: `+43*`, `+44165*`, `+49 145 *`, `+38050*4567`, `*`.
- Whitespace and `-`, `(`, `)`, `.`, `/` are ignored in both mask and number (`+49 145 *` ≡ `+49145*`).
- Without `*` the match is **exact and full-length** (`+43` does not block `+436641234567`). Empty/blank masks never match.
- Implemented as an iterative glob matcher (no regex). Number is `callDetails.handle.schemeSpecificPart` as delivered by the system.
- A leading `00` (after stripping separators) is treated as `+` in both mask and number: `0049 145 *` ≡ `+49145*`, matching `+49145123` and `0049145123`. Only the very first `00` is mapped (`+4900…` untouched; `00` alone ≡ `+`; `00*` ≡ `+*`).
- Not handled: national format (`0151…`) vs international (`+49151…`), `tel:` URL-encoding.

## Architecture
Package root `app/src/main/java/dybarsky/blocker/`:

| Package | Contents |
|---|---|
| `BlockerApp.kt` | `Application`; static `instance`; lazy `Injection` |
| `common/models` | `BlockedCall(number, mask, timestamp)`; `Number(id: Id, mask: String)`; `Number.Id` is a `@JvmInline value class` over `Long`, `Id.New = -1` |
| `common/components` | Interfaces: `NumberStore` (save/delete/`loadNumbers(): Flow<List<Number>>`), `CallLogStore` (`add`/`clear`/`loadCalls()` newest first), `Matcher` |
| `domain` | `MatcherImpl` |
| `store` | `NumberStoreImpl` (DataStore file `numbers.json`), `CallLogStoreImpl` (`calls.json`, capped at 200 entries, see `CallRecord.kt`), `JsonListSerializer` (generic JSON-list DataStore serializer; `RecordSerializer` delegates to it), `Record` (serializable DTO `{id, mask}`), `RecordSerializer` (JSON list ↔ DataStore), `RecordTransformer` (`Record.asNumber()`, `Number.asRecord()`; new id = `currentTimeMillis()`) |
| `di` | `Injection` (manual DI, lazy `store` & `matcher`), `viewModelFactory(store)` (Lifecycle `initializer`s for `HomeViewModel`, `EditorViewModel`), `serviceModelFactory(store, matcher)` |
| `service` | `BlockerService`, `BlockerServiceModel` (abstract: `screen(phone): Verdict`, `record(phone, verdict)`), `BlockerServiceModelImpl`, `Verdict` (`Allow` / `Block(mask)`) |
| `ui/common` | `Intention` marker, `Callback<E : Intention>` fun interface (`submit(event)`) |
| `ui/screens/{home,edit,log,role,main}` | Screens, ViewModels, states, intentions, navigation (`Route`, `Navigation.kt`, `MainScreen`) |
| `ui/theme` | Dark-only Material3 scheme (`Color.kt`, `Scheme.kt`, `Theme.kt`, `Type.kt`) |

### Conventions
- **MVI-ish UI**: each screen has `XState` (sealed interface), `XIntention : Intention` (sealed interface), abstract `XViewModel` exposing `stateFlow: StateFlow<XState>`, and `XViewModelImpl`. Screens come in two overloads: a stateful one (obtains ViewModel via `BlockerApp.instance.injection.viewModelFactory`, maps intentions to model calls/navigation) and a stateless one `(state, callback: Callback<Intention>, modifier)` for previews/testing.
- **Compose previews**: live in the **debug source set** (`app/src/debug/java/dybarsky/blocker/...`, same package/subfolder as the screen; `ui-tooling-preview` is `debugImplementation`, so previews are not in release and must not be referenced from `src/main`): `HomePreviews.kt`, `LogPreviews.kt`, `EditorPreviews.kt`, `RolePreviews.kt`, `ui/common/CommonPreviews.kt` for `FloatingNavBar`/`ScreenCard`+`CardFab`). They are `private`, call the stateless overloads with a no-op `Callback` and fixed sample data, and use the shared `@ScreenPreview` annotation + `PreviewFrame` (app theme + `Surface`) from `ui/common/PreviewSupport.kt`. `LogPreviews` builds timestamps relative to today because `LogScreen` uses `LocalDate.now()`. Add previews for new screens/states the same way.
- Abstract class/interface + `Impl` pairing everywhere; wiring lives only in `di/`.
- ViewModels get args via `CreationExtras` keys (e.g. `EditorViewModel.idKey`, `EditorViewModel.extras(id)`).
- The app is dark-only: `themes.xml` uses a dark parent with transparent bars and `windowLightStatusBar=false`, and `BlockerActivity` calls `enableEdgeToEdge(SystemBarStyle.dark(...))` so status/navigation bar icons are always light regardless of the system theme.
- Top-level navigation is a **centered floating pill bar used as tabs** (`ui/common/FloatingNavBar.kt`, Material 3 floating-toolbar style: selected tab = highlighted pill with icon + label, others = plain label), hosted in `MainScreen` over the `NavHost`. Tabs: `Route.Home` ("Номери") and `Route.Log` ("Журнал"); the bar is hidden on `Roles` and stays visible under the editor dialog (`controller.visibleEntries`). Switching tabs uses `popUpTo<Route.Home> { saveState }` + `restoreState`, so system back from Log returns to Home. There is **no top app bar**. Every top-level screen is built from `ui/common/ScreenCard.kt`: `ScreenContainer` (system bars, margins, room for the bar) → `ScreenCard` (the rounded card, identical size on all screens, also used for loading/error/empty states) plus overlays placed in the container (`SnackbarHost`, `CardFab` at `BottomEnd` (currently only Home uses it); lists reserve `CardFabClearance` at their end). The official `FloatingToolbar` is not in stable Material3 1.4.0, hence the hand-built bar — swap it in when available.
- Navigation is type-safe: `Route.Home`, `Route.Roles`, `Route.Log`, `Route.Editor(id: Long?)` (null id = new mask). Editor uses raw `Long?` in the route and converts to `Number.Id` in `Navigation.kt`.
- Non-Compose services reach dependencies through the `BlockerApp.instance.injection` singleton.
- User-facing strings live in `res/values/strings.xml` (**Ukrainian is the default**) with an English translation in `res/values-en/strings.xml`; composables use `stringResource(R.string.x)`, composed texts use format args (`log_mask` = `Маска: %1$s`), never concatenation. Every new string needs both files (lint `MissingTranslation` is an error). Dates in the log use the locale-aware `DateTimeFormatter.ofLocalizedDate`. `app_name` (launcher label) is "Блокувальник" / "Call Blocker" (en); the Play titles are "Блокувальник шахраїв: маски" / "Scam Call Blocker – Masks" (see `store/NAMING.md`). Languages: uk (default) + en; `res/xml/locales_config.xml` is referenced via `android:localeConfig` (Android 13+ per-app language in system settings, no in-app picker) and `androidResources { localeFilters += listOf("uk", "en") }` drops other locales' resources — update both when adding a language.

## Call log
- Blocked calls (number, matched mask, time) are stored in `calls.json` (newest first, max 200) and shown on `LogScreen` ("Журнал" tab), grouped by local calendar day with sticky headers (Сьогодні / Вчора / date; grouping logic in `ui/screens/log/LogGrouping.kt`). Rows show number + time, with the mask in smaller text below. Clearing exists in the logic (`LogIntention.Clear` → `LogViewModel.clear()` → `CallLogStore.clear()`, with a `clearFailed` snackbar) but has **no UI entry point right now**. Allowed calls are not logged.
- `calls.json` is excluded from Auto Backup and device transfer (`res/xml/backup_rules.xml`, `data_extraction_rules.xml`, referenced from the manifest) because it contains caller numbers.

## Error handling & testing
- Loading: `HomeState.Loading` / `EditorState.Loading` render `ui/common/LoadingIndicator` (spinner appears after 250 ms so fast local loads don't flash).
- Errors are **state-based** (no one-off events): `HomeState.Error` (+ Retry), `HomeState.Content.deleteFailed` (Snackbar, then `DismissError`), `EditorState.Error`, `EditorState.Content.saveFailed` (error under the field). ViewModels hold no strings; texts live in `strings.xml` (`error_*`).
- Use `runCatchingSuspend` (`common/util/Result.kt`) instead of `runCatching` in suspend code — it rethrows `CancellationException`.
- `BlockerService` fails open: null handle, storage error or any exception → call is allowed. `NumberStoreImpl` resets a corrupt `numbers.json` to an empty list (`ReplaceFileCorruptionHandler`).
- Unit tests (`app/src/test`, JUnit4 + `kotlinx-coroutines-test`): `testing/FakeNumberStore` (switches `failLoad/failSave/failDelete`), `testing/MainDispatcherRule` for `viewModelScope`. Run: `./gradlew testDebugUnitTest` (needs `JAVA_HOME` = Android Studio JBR).

## Release build (R8)
- `release` has `isMinifyEnabled = true` + `isShrinkResources = true`. No custom keep rules are needed: kotlinx-serialization, Navigation (type-safe routes), DataStore and AndroidX ship consumer rules, and the manifest keeps `BlockerService`. `app/proguard-rules.pro` is intentionally empty of rules. Verified: `assembleRelease` clean, and the release APK (re-signed with the debug key) loads masks, call log and the editor dialog on an emulator. Re-verify after adding new `@Serializable` types or reflection.
- Signing: `signingConfigs.release` reads the gitignored `keystore.properties` at the repo root (`storeFile`, `storePassword`, `keyAlias`, `keyPassword`; see `keystore.properties.example`). Without the file, `assembleRelease`/`bundleRelease` produce unsigned artifacts. Upload to Play: `./gradlew bundleRelease` → `app/build/outputs/bundle/release/app-release.aab` (`mapping.txt` in `app/build/outputs/mapping/release/`). Never commit keystores.
- Version: `versionCode = 1`, `versionName = "1.0.0"` in `defaultConfig`; bump both for each upload. `dependenciesInfo` is disabled. Play requires targetSdk ≥ 36 for new apps/updates from 2026-08-31 (36 satisfies it).
- Launcher icon: adaptive vector (`drawable/ic_launcher_{background,foreground,monochrome}.xml`, themed-icon layer); the Play 512px icon is rendered from `store/graphics/icon.svg` via `store/tools/make_icon.sh`.
- Store content, naming research: `store/`; prioritized issues and features: `docs/ROADMAP.md` (supersedes open items in `TODO.md`).

## Known issues / TODOs (observed in code)
- `BlockerService.onScreenCall` still uses `runBlocking` on the DataStore read.
- Matcher does not map national ↔ international formats (see TODO).
- Only uk + en exist; no in-app language picker (system per-app language only). `title_activity_main` is unused.
- Theme is dark-only (no light scheme, no dynamic color); `Typography` overrides only `bodyLarge`.
- `allowBackup="true"` — `numbers.json` is included in backups by default.
- `di/ServiceModelFactory.kt` has an unchecked-cast warning.

## Ideas for extending
- Add unit tests for `MatcherImpl` and `BlockerServiceModelImpl` (pure Kotlin, easy; fake `NumberStore`).
- Add more locales (uk + en done; per-app language enabled via `locales_config.xml`).
- Normalize numbers (strip spaces/dashes, handle national vs international format) before matching.
- Consider block logging/notification, import/export of masks, and a light theme.
