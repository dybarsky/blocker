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

## Error handling & testing
- Errors are **state-based** (no one-off events): `HomeState.Error` (+ Retry), `HomeState.Content.deleteFailed` (Snackbar, then `DismissError`), `EditorState.Error`, `EditorState.Content.saveFailed` (error under the field). ViewModels hold no strings; texts live in `strings.xml` (`error_*`).
- Use `runCatchingSuspend` (`common/util/Result.kt`) instead of `runCatching` in suspend code — it rethrows `CancellationException`.
- `BlockerService` fails open: null handle, storage error or any exception → call is allowed. `NumberStoreImpl` resets a corrupt `numbers.json` to an empty list (`ReplaceFileCorruptionHandler`).
- Unit tests (`app/src/test`, JUnit4 + `kotlinx-coroutines-test`): `testing/FakeNumberStore` (switches `failLoad/failSave/failDelete`), `testing/MainDispatcherRule` for `viewModelScope`. Run: `./gradlew testDebugUnitTest` (needs `JAVA_HOME` = Android Studio JBR).

## Known issues / TODOs (observed in code)
- `EditorState.Loading` and `HomeScreenLoading` render nothing.
- `BlockerService.onScreenCall` still uses `runBlocking` on the DataStore read.
- Matcher prefix/truncation semantics are only documented by tests; no number normalization.
- Most UI strings are still hard-coded Ukrainian (only `app_name` and `error_*`/`action_close` are in `strings.xml`).
- Theme is dark-only (no light scheme, no dynamic color); `Typography` overrides only `bodyLarge`.
- `isMinifyEnabled = false` for release; `allowBackup="true"` — `numbers.json` is included in backups by default.
- `di/ServiceModelFactory.kt` has an unchecked-cast warning.

## Ideas for extending
- Add unit tests for `MatcherImpl` and `BlockerServiceModelImpl` (pure Kotlin, easy; fake `NumberStore`).
- Move strings to `strings.xml` (uk + en) and add proper error/empty/loading states.
- Normalize numbers (strip spaces/dashes, handle national vs international format) before matching.
- Consider block logging/notification, import/export of masks, and a light theme.
