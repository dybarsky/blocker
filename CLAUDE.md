# Blocker ("Шахрай") — Project Context

Android app that rejects incoming calls whose number matches a user-defined mask list (anti-spam / anti-scam). UI language is Ukrainian; app label is "Шахрай". Personal project, single module, no backend.

## Quick facts
- Package / applicationId: `dybarsky.blocker`; Gradle root project `Blocker`, single module `:app`
- Kotlin 2.3.0, AGP `9.0.0-rc03` (pre-release), Gradle wrapper in repo, Java 11 source/target
- compileSdk/targetSdk 36, **minSdk 29** (required for `RoleManager` call-screening role)
- UI: Jetpack Compose + Material3 (BOM 2026.01.00), Navigation Compose 2.9.6 (type-safe `@Serializable` routes), Lifecycle 2.10.0, Activity Compose 1.12.2
- Persistence: Jetpack DataStore (`datastore-preferences` artifact, but used via `dataStore(...)` with a custom JSON `Serializer`) + kotlinx-serialization-json 1.9.0
- No DI framework, no Room, no Hilt, no network. Tests are only the default templates (`ExampleUnitTest`, `ExampleInstrumentedTest`); JUnit 4 is the only test dependency.
- Version catalog: `gradle/libs.versions.toml`
- Build: `./gradlew assembleDebug`, install: `./gradlew installDebug`. Features must be verified on a device/emulator (call screening can't be unit-tested easily).

## How it works
1. App starts in `BlockerActivity` → `MainScreen` (NavHost, start route `Roles`).
2. `RoleScreen` checks `RoleManager.isRoleHeld(ROLE_CALL_SCREENING)`. If held, it pops itself and navigates to `Home`; otherwise shows an "Активувати" button that launches `createRequestRoleIntent(ROLE_CALL_SCREENING)`; on result it re-checks.
3. `HomeScreen` lists masks (swipe end-to-start to delete, tap to edit, FAB "Додати" to add).
4. `EditorScreen` is a navigation **dialog** (`dialog<Route.Editor>`) with a phone-keyboard text field; Save persists, Cancel closes.
5. On an incoming call, the system binds `BlockerService` (`CallScreeningService`, permission `BIND_SCREENING_SERVICE`); it asks `BlockerServiceModel.shouldBlock(number)`, and responds with reject (`rejectCall`, `disallowCall`, `skipCallLog=false`, `skipNotification=true`) or plain allow.

### Mask matching (`domain/MatcherImpl.kt`)
```kotlin
text.zip(mask).all { (c, m) -> m == '*' || c == m }
```
- `*` = any single character; everything else literal. Number is `callDetails.handle.schemeSpecificPart` (as delivered, e.g. `+380...`).
- **Gotcha:** `zip` truncates to the shorter string, so a mask shorter than the number acts as a *prefix* match (`+38050` blocks all `+38050...`), and a number shorter than the mask also matches if it's a prefix of the mask. A blank mask would match everything (the editor refuses to save blank masks — blank save just closes).
- No normalization of formats (spaces, dashes, `0xx` vs `+380xx`); the mask must match the system-provided string.

## Architecture
Package root `app/src/main/java/dybarsky/blocker/`:

| Package | Contents |
|---|---|
| `BlockerApp.kt` | `Application`; static `instance`; lazy `Injection` |
| `common/models` | `Number(id: Id, mask: String)`; `Number.Id` is a `@JvmInline value class` over `Long`, `Id.New = -1` |
| `common/components` | Interfaces: `NumberStore` (save/delete/`loadNumbers(): Flow<List<Number>>`), `Matcher` |
| `domain` | `MatcherImpl` |
| `store` | `NumberStoreImpl` (DataStore file `numbers.json`), `Record` (serializable DTO `{id, mask}`), `RecordSerializer` (JSON list ↔ DataStore), `RecordTransformer` (`Record.asNumber()`, `Number.asRecord()`; new id = `currentTimeMillis()`) |
| `di` | `Injection` (manual DI, lazy `store` & `matcher`), `viewModelFactory(store)` (Lifecycle `initializer`s for `HomeViewModel`, `EditorViewModel`), `serviceModelFactory(store, matcher)` |
| `service` | `BlockerService`, `BlockerServiceModel` (abstract), `BlockerServiceModelImpl` |
| `ui/common` | `Intention` marker, `Callback<E : Intention>` fun interface (`submit(event)`) |
| `ui/screens/{home,edit,role,main}` | Screens, ViewModels, states, intentions, navigation (`Route`, `Navigation.kt`, `MainScreen`) |
| `ui/theme` | Dark-only Material3 scheme (`Color.kt`, `Scheme.kt`, `Theme.kt`, `Type.kt`) |

### Conventions
- **MVI-ish UI**: each screen has `XState` (sealed interface), `XIntention : Intention` (sealed interface), abstract `XViewModel` exposing `stateFlow: StateFlow<XState>`, and `XViewModelImpl`. Screens come in two overloads: a stateful one (obtains ViewModel via `BlockerApp.instance.injection.viewModelFactory`, maps intentions to model calls/navigation) and a stateless one `(state, callback: Callback<Intention>, modifier)` for previews/testing.
- Abstract class/interface + `Impl` pairing everywhere; wiring lives only in `di/`.
- ViewModels get args via `CreationExtras` keys (e.g. `EditorViewModel.idKey`, `EditorViewModel.extras(id)`).
- Navigation is type-safe: `Route.Home`, `Route.Roles`, `Route.Editor(id: Long?)`. Editor uses raw `Long?` in the route and converts to `Number.Id` in `Navigation.kt`.
- Non-Compose services reach dependencies through the `BlockerApp.instance.injection` singleton.
- User-facing strings are **hard-coded Ukrainian** in composables (only `app_name` is in `strings.xml`): "Додати номер", "Редагувати номер", "Скасувати", "Зберегти", "Видалити", "Додати", "Активувати".

## Known issues / TODOs (observed in code)
- Error handling is stubbed: `// todo` / swallowed errors in `HomeViewModelImpl`, `EditorViewModelImpl` (`.catch { /* todo */ }`, `onFailure { // todo }`).
- `EditorState.Loading` and `HomeScreenLoading` render nothing.
- `BlockerService.onScreenCall` uses `runBlocking` on the DataStore read; failures in `shouldBlock` fail open (allow call).
- `BlockerService` is `exported="true"` (needed for the screening role; protected by `BIND_SCREENING_SERVICE`).
- `BlockerService` resolves its model in a field initializer via `BlockerApp.instance` (fine since the Application is created first).
- `EditorState.Saved` triggers `callback.submit(Close)` directly during composition (side effect in composition; better as `LaunchedEffect`).
- Matcher prefix/truncation semantics (see above) are undocumented/untested; no number normalization.
- Theme is dark-only (no light scheme, no dynamic color); `Typography` overrides only `bodyLarge`.
- `isMinifyEnabled = false` for release; `allowBackup="true"` — the `numbers.json` list is included in backups by default (check `res/xml/backup_rules.xml`, `data_extraction_rules.xml`).
- Git: single commit (`ab9894c 1`); most files appear as modified in the working tree (likely line-ending/permission churn or a regenerated tree), `.idea/` is partly tracked, `local.properties` exists locally.

## Ideas for extending
- Add unit tests for `MatcherImpl` and `BlockerServiceModelImpl` (pure Kotlin, easy; fake `NumberStore`).
- Move strings to `strings.xml` (uk + en) and add proper error/empty/loading states.
- Normalize numbers (strip spaces/dashes, handle national vs international format) before matching.
- Consider block logging/notification, import/export of masks, and a light theme.
