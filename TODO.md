# TODO — upcoming tasks

Suggested order: **1 → 2 → 3** (stabilize, upgrade with tests as a safety net, then add the new feature). See `CLAUDE.md` for architecture and conventions.

## 1. Fix unfinished issues
- [x] **Error handling:** replace `// todo` / `.catch { /* todo */ }` in `HomeViewModelImpl` and `EditorViewModelImpl`; add an `Error` state and show a message in the UI.
- [x] **Loading states:** shared `LoadingIndicator` (spinner shown after 250 ms to avoid flicker) on Home and in the editor dialog.
- [x] **Editor side effect:** in `EditorScreen`, `EditorState.Saved` calls `callback.submit(Close)` during composition — move it into a `LaunchedEffect`.
- [x] **Service:** `BlockerService` now fails open (null handle, errors → allow). Still uses `runBlocking` — consider caching the mask list in memory.
- [x] **Matcher:** `*` = any sequence, separators ignored, exact match without `*` (see `CLAUDE.md`). Follow-ups below.
- [x] **Tests:** unit tests added for view models, service model, matcher, store serializer/transformer, `runCatchingSuspend` (34 tests, `./gradlew testDebugUnitTest`). Still missing: matcher semantics decision/normalization.
- [ ] **Strings:** move hard-coded Ukrainian texts into `strings.xml` (uk default + en).
- [ ] **Housekeeping:** nearly every file shows as modified in git — check `git diff --stat` (line endings / permissions?), fix and commit; decide what to track in `.idea/`; review `allowBackup` and `res/xml/backup_rules.xml`.

### Matcher follow-ups
- [ ] Treat leading `00` as `+` (`0049…` ≡ `+49…`) in mask and number.
- [ ] Single-digit wildcard `#` (it is on the phone keyboard; `?` is not): `+38050#######`.
- [ ] Hint under the editor field explaining `*` and that masks without it are exact.
- [ ] Live preview in the editor ("matches +49 145 1234567?" test field) and a warning for over-broad masks (`*`, `+*`).
- [ ] National-format masks (`0151*`) via a default country code setting.
- [ ] Reject/flag invalid masks (letters, empty after normalization) and duplicates / masks covered by an existing one.
- [ ] Optional allowlist that overrides blocks; optional "block unknown/hidden numbers".

## 2. Upgrade Gradle and libraries
Current (upgraded): Gradle 9.6.1, AGP 9.5.1, Kotlin 2.4.21, Compose BOM 2026.09.00, compileSdk 37 (targetSdk still 36).
- [x] Move AGP from RC to the latest stable 9.0.x; bump the wrapper to the Gradle version AGP requires (`./gradlew wrapper --gradle-version <x>`); update Kotlin if compatible.
- [x] Bump the remaining entries in `gradle/libs.versions.toml` (look up latest versions at that time).
- [x] Drop the manual `1.10.1` pins for `ui-tooling` / `ui-tooling-preview` so they follow the BOM.
- [x] Replace `datastore-preferences` with `datastore` (core) — only a custom serializer is used.
- [ ] Consider `isMinifyEnabled = true` for release and verify with R8.
- [ ] After each bump run `./gradlew assembleDebug testDebugUnitTest` and fix deprecations.

## 3. Add call log (blocked-call history)
- [ ] Model `BlockedCall(id, number, mask, timestamp)` in `common/models`.
- [ ] Storage: `CallLogStore` interface (`common/components`) + `CallLogStoreImpl` (`store/`) mirroring `NumberStoreImpl` — DataStore JSON file `calls.json`, DTO + transformer, cap at ~200 entries (drop oldest).
- [ ] Matcher/service model: expose which mask matched so the log can record the rule (e.g. a `findMatch` helper used by `BlockerServiceModelImpl`).
- [ ] Service: on reject, append a log entry without delaying `respondToCall`.
- [ ] DI: wire the new store in `Injection`, `ViewModelFactory.kt`, `ServiceModelFactory.kt`.
- [ ] UI: `ui/screens/log/` (`LogState`, `LogIntention`, `LogViewModel` + `Impl`, `LogScreen`) following the existing MVI pattern; add `Route.Log`, register it in `Navigation.kt`, add an entry icon on Home; actions: clear log, optional "add as mask" from an entry.
- Note: rejected calls already appear in the system call log (`skipCallLog = false`); the in-app log adds which mask matched.

## 4. Later ideas
- [ ] Light theme / dynamic color
- [ ] Import / export of masks
- [ ] Notification toggle for blocked calls
- [ ] Allowlist
- [ ] Compose previews and UI tests
