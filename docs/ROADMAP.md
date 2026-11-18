# Roadmap

Supersedes the open (unchecked) items of `TODO.md`. Each item was checked against the code as of commit `2b4aba3`; where the docs and the code disagree, the code wins (see "Doc discrepancies" at the end).

Legend: effort S (under half a day), M (about a day), L (several days). Paths are relative to `app/src/main/java/dybarsky/blocker/` unless they start with `app/` or `docs/`. Tests are JUnit4 unit tests in `app/src/test` unless noted; "device" means a manual check on an emulator or phone (`adb emu gsm call +380501234567` simulates an incoming call on an emulator).

## Tier 1 - Before 1.0 release (recommended)

Theme: the call path must be cheap and safe, a bad mask must not silently block everything, and the app must be shippable.

### A1 Cached mask list; take DataStore off the call path - M, risk medium
`BlockerService.onScreenCall` runs on the main thread and does `runBlocking { store.loadNumbers().first() }`, then a second `runBlocking` for `callLog.add` (reads and rewrites up to 200 entries) per blocked call. On a cold process the first call also pays process start + DataStore file read. Telecom allows only a short window to respond, after which the call is allowed anyway; a slow read means a late verdict, not a crash, so this is a latency and main-thread-jank risk rather than a guaranteed ANR. Plan:
- App-scoped holder (`SupervisorJob` + `Dispatchers.IO`) that collects `loadNumbers()` into a `StateFlow<List<Number>?>` (null = not loaded yet), started from `BlockerApp.onCreate`.
- `screen()` matches against the cached list; if null, fall back to one bounded read (`withTimeout(~1500 ms)`, timeout = `Allow`, consistent with fail-open).
- `record()` after `respondToCall` runs on `Dispatchers.IO` (still awaited, or fire-and-forget on the app scope; a lost log line on process death is acceptable).
- Files: `service/BlockerService.kt`, `service/BlockerServiceModelImpl.kt`, `di/Injection.kt`, `di/ServiceModelFactory.kt`, `BlockerApp.kt`, new cache class (e.g. `store/NumberCache.kt`).
- Tests: `BlockerServiceModelImplTest` with a fake store (cache hit; cache cold and slow store, expect Allow on timeout; store throws, expect Allow; list changes, expect new list used). Device: block + allow after cold start (`adb shell am force-stop`), then edit a mask and call again immediately.

### A2 Incoming-calls-only guard - S, risk low (verify first)
`onScreenCall` never checks `callDetails.callDirection`. The framework documents `CallScreeningService` as invoked for incoming calls and, in some configurations, outgoing ones. If the platform delivers outgoing calls on a given device, dialing a number that matches a mask would be rejected and logged as "blocked". Check on a device/emulator whether it fires for outgoing; regardless, add `if (callDetails.callDirection != Call.Details.DIRECTION_INCOMING) { respondToCall(allow); return }` (API 29 is available at minSdk).
- Files: `service/BlockerService.kt`. Tests: the direction check is in the Android layer; extract a tiny pure function if desired, otherwise device-verify (call out to a masked number).

### A3 Role lost / unavailable handling - S-M, risk low
`RoleScreen` calls `createRequestRoleIntent` without `isRoleAvailable` (devices without a dialer/telephony stack can throw or no-op), and the role is checked only on start. If the user revokes the role in system settings, Home looks fine but nothing is blocked. Add: an `isRoleAvailable` guard with an explanatory message; on `ON_RESUME` re-check `isRoleHeld` and show a banner/route back to `Roles` if lost.
- Files: `ui/screens/role/RoleScreen.kt`, `ui/screens/main/MainScreen.kt` / `Navigation.kt`, `strings.xml` (+ `values-en`), `RolePreviews.kt`. Tests: device (revoke role in Settings > Default apps, return to app).

### A4 Mask validation, duplicates, over-broad warning - M, risk medium
Today any text is saved: letters, trailing spaces (stored untrimmed), duplicates, and `*` / `+*`, which blocks every call from a non-contact. Whitespace-only input silently closes the dialog without saving. Add a pure `MaskValidator` (domain) returning `Ok | Empty | InvalidChars | Duplicate | MatchesAll`; show it as supporting text in the editor. `MatchesAll` (normalized mask consists only of `*` and optional `+`) requires an explicit confirm, not a hard block. Trim before saving. Duplicate = equal after the matcher's `normalize` (needs to be exposed; do not duplicate the logic).
- Files: `domain/` (new `MaskValidator`, expose normalization from `MatcherImpl`), `ui/screens/edit/EditorViewModelImpl.kt`, `EditorState.kt`, `EditorScreen.kt`, `EditorPreviews.kt`, `strings.xml` x2, `NumberStore` read for duplicate check. Tests: validator table tests (including `00` and separator normalization); `EditorViewModelImplTest` for rejected save states.

### A5 Editor hint and dialog dismissal - S, risk low
Under the field: short text "`*` = any digits; without `*` the number must match exactly" plus one example (`+49 145 *`). Also: `onDismissRequest = { /* no-op */ }` means back and outside-tap do nothing, so only Cancel closes the dialog; wire it to `Close`.
- Files: `ui/screens/edit/EditorScreen.kt`, `EditorPreviews.kt`, `strings.xml` x2. Tests: preview + device.

### A6 Number-format edge cases in the matcher - S, risk low
Verify on device and cover with tests:
- `tel:` encoding: `Uri.getSchemeSpecificPart()` already returns the decoded form, so `%2B` arrives as `+` (CLAUDE.md lists this as unhandled; likely moot, confirm and update the docs). 
- Post-dial / pause suffixes (`;postd=...`, `,`, `p`, `w`) and URI parameters (`;phone-context=...`) are not stripped, so an exact-length mask never matches such a number. Strip everything from the first `;` and drop `,`/`p`/`w` suffixes in number normalization (not in masks).
- Non-`tel:` handles (`sip:user@host`): decide explicitly (recommended: allow, do not screen).
- National vs international delivery (`050…` vs `+38050…`) is a real gap for Ukrainian callers; the proper fix is B2. For 1.0, mention in the editor hint that both forms may need separate masks, and rely on the log showing the raw number the system delivered.
- Files: `domain/MatcherImpl.kt`, `service/BlockerService.kt` (scheme check), `MatcherImplTest.kt`. Tests: table-driven cases for each bullet.

### A7 DataStore failure behavior - S, risk low
- `NumberStoreImpl` and `CallLogStoreImpl` declare `Context.dataStore by dataStore(...)` as a member extension delegate, so a second instance of either class in one process throws "multiple DataStores active for the same file". It works only because `Injection` is a lazy singleton. Move the delegates to top-level properties.
- `JsonListSerializer` uses a strict `Json`; any unknown key (a newer version of the app wrote a new field, then a downgrade or a restore from backup) raises `CorruptionException`, and `ReplaceFileCorruptionHandler { emptyList() }` then silently wipes all masks. Use `Json { ignoreUnknownKeys = true }` and give any future fields defaults; optionally copy the bad file to `numbers.json.corrupt` before reset.
- Service already fails open on read errors, and logging errors are swallowed; keep that.
- Files: `store/NumberStoreImpl.kt`, `store/CallLogStoreImpl.kt`, `store/JsonListSerializer.kt`, `store/RecordSerializer.kt`. Tests: `RecordSerializerTest` (unknown field is ignored, not corruption).

### A8 Clear-log UI entry point - S, risk low
The log stores caller numbers; the user must be able to wipe it. Logic exists (`LogIntention.Clear` → `clear()` with `clearFailed` snackbar) but nothing triggers it. Add a small action on the Log card (icon button in the card header, or overflow) with a confirmation dialog (removed earlier with the delete FAB; see git history).
- Files: `ui/screens/log/LogScreen.kt`, `LogPreviews.kt`, `strings.xml` x2. Tests: existing `LogViewModelImplTest` covers clear; add a preview for the dialog; device.

### A9 Unchecked cast in `ServiceModelFactory` - S, risk low
`obtain` casts with `as T`. Since the service only needs one model, drop the generic factory: expose `val serviceModel: BlockerServiceModel` (or a typed `fun obtainServiceModel()`) from `Injection`, which also simplifies A1. Remove the warning at the same time.
- Files: `di/ServiceModelFactory.kt`, `di/Injection.kt`, `service/BlockerService.kt`. Tests: compile + existing tests.

### A10 Git housekeeping - S, risk low
Verified cause of the noise: the index has mixed file modes (73 tracked files are `100755`, newer ones `100644`), plus `gradlew.bat` is CRLF and there is no `.gitattributes`. Checkouts on a filesystem that writes `644` therefore show nearly every file as modified. Fix in one commit: `git update-index --chmod=-x` for everything except `gradlew`; add `.gitattributes` (`* text=auto eol=lf`, `*.bat text eol=crlf`, `*.webp *.jar binary`); decide `.idea` policy (it is fully ignored now, which is fine for a solo project). Keep `allowBackup="true"`: restoring masks on a new phone is useful, and `calls.json` is already excluded (the `datastore/calls.json` paths match where DataStore writes). 
- Files: `.gitattributes` (new), index modes. Tests: `git status` clean after a fresh clone and build.

### A11 Release readiness and Play policy - M, risk medium
- No signing config exists; set up an upload key (kept out of git) and enroll in Play App Signing. `versionCode = 1` / `versionName = "1.0"` are fine for the first upload.
- Re-run `assembleRelease` and smoke-test the R8 build (CLAUDE.md procedure) after the items above, especially anything new that is `@Serializable`.
- Call-screening role apps are a supported use case and the app requests no runtime permissions (no `READ_CALL_LOG`, no contacts), so the SMS/Call Log permission declaration should not apply; still confirm in Play Console at submission time. Expect to provide: a privacy policy URL (the app stores caller numbers locally in `calls.json`; nothing leaves the device), a Data safety form (no data collected or shared; on-device only), and an honest store listing (it silently rejects calls matching user-entered masks; it is not a spam database; calls from saved contacts and emergency numbers are not screened).
- Check the target API requirement in Play Console when submitting (currently `targetSdk = 36`).
- Reconcile with the parallel `prep/*` branches (release, icon, naming, i18n, onboarding) before merging so signing, icon and strings are not done twice.
- Files: `app/build.gradle.kts`, `docs/` (privacy policy text), store assets. Tests: release APK on a real device: role grant, block, allow, edit, log, clear.

### A12 On-device acceptance run - S, risk low
Single pass on a real phone (not only an emulator): grant role; block `+43*`; allow a contact; hidden-number call is allowed; call arrives while the app process is dead; reboot and call again; revoke role and re-check the banner (A3).

## Tier 2 - 1.1

Theme: features that need a Settings screen and richer matching.

### B0 Settings screen (prerequisite for B3, B4, B2) - M, risk low
A third destination (or a gear on the Home card) holding toggles and the country code. Stored in a small DataStore (`settings.json`) behind a `SettingsStore` interface + `Impl`, wired in `di/`. Cached in the same app-scoped holder as A1 so the service reads it from memory.
- Files: `common/components/SettingsStore.kt`, `store/`, `di/`, `ui/screens/settings/*`, `Route.kt`, `MainScreen.kt`. Tests: store serializer test, ViewModel test with a fake store, previews.

### B1 `#` single-digit wildcard - S, risk low
`#` matches exactly one character (digit). It is on the phone keypad and never occurs in `tel:` numbers. Add a branch to `MatcherImpl.matches` (`pattern[p] == '#' && text[t].isDigit()`), include it in validation (A4) and the hint (A5).
- Files: `domain/MatcherImpl.kt`, `MatcherImplTest.kt`, hint string. Tests: `+38050#######` matches 13-char numbers only; `#` combined with `*`; backtracking correctness.

### B2 National-format masks via default country code - M, risk medium
Masks like `050*` or `0151*` cannot match `+38050…` today. Setting: default country calling code (default derived from the SIM/network country through a small built-in table; no libphonenumber unless more is needed). Normalization rewrites a single leading `0` (national trunk prefix) to `+<code>` in both mask and number; trunk-prefix rules differ per country (the US has none), so make this opt-in via the setting and document the limits.
- Files: `domain/MatcherImpl.kt` (inject normalizer/config), `SettingsStore`, settings UI. Tests: large table in `MatcherImplTest` (with/without setting, `00` and `+` interplay, masks starting `0*`). Risk: false positives from unexpected trunk prefixes.

### B3 Block hidden / private numbers - S-M, risk medium
Currently a null or blank handle is always allowed (`BlockerServiceModelImpl.screen`). Add a setting "block hidden numbers". Detect via a null/blank handle or `callDetails.handlePresentation == PRESENTATION_RESTRICTED`/`UNKNOWN` (more reliable than a null handle). `BlockedCall.number` is non-null, so the log needs a placeholder state (a nullable number rendered via a string resource) and `Verdict.Block` needs a reason (mask vs hidden). Surfaced risk: hidden numbers include legitimate callers (hospitals, some banks); default off, with a clear description.
- Files: `service/BlockerService.kt`, `BlockerServiceModelImpl.kt`, `Verdict`, `common/models/BlockedCall.kt`, `store/CallRecord.kt` (nullable with default for back-compat), `LogScreen.kt`. Tests: service model (setting on/off, null/blank number), `CallRecordTest`.

### B4 Blocked-call notification toggle - M, risk medium
The service already suppresses the system notification (`skipNotification = true`). Optional own notification "Blocked: +43…" behind a Settings toggle. Needs `POST_NOTIFICATIONS` (Android 13+; requested only when the user turns the toggle on), a channel, and a tap action opening the Log tab. Do not put numbers on the lock screen (use `VISIBILITY_PRIVATE` with a public version).
- Files: new `service/Notifier.kt`, manifest, `SettingsStore`, settings UI. Tests: notifier behind an interface; service model test with a fake notifier; device for permission flow.

### B5 "Unblock this mask" from a log entry - S-M, risk low
Long-press/tap an entry for an action that deletes the mask that blocked it. `BlockedCall` stores the mask string, not the id, so resolve by normalized mask equality in the number store and delete all equal entries; if nothing matches (already deleted) show a neutral message. Consider the inverse shortcut ("add this number as a mask") at the same time.
- Files: `ui/screens/log/*` (state, intention, ViewModel, screen, previews), `common/components/NumberStore.kt` usage, `di/ViewModelFactory.kt`. Tests: `LogViewModelImplTest` with `FakeNumberStore`.

### B6 Live "test a number" field in the editor - M, risk low
Optional field "does `+49 145 1234567` match?" under the mask, using the real matcher, to build trust in the glob semantics. Complements the hint (A5) and the match-all warning (A4).
- Files: `ui/screens/edit/*`, `Matcher` injected into the editor ViewModel via `di/ViewModelFactory.kt`. Tests: ViewModel test (state updates as the test text changes).

## Tier 3 - Later

| Item | Effort | Risk | Notes / files | Test approach |
|---|---|---|---|---|
| Allowlist (overrides blocks) | M | medium | Second list `AllowStore`, second Home section or tab; `screen()` checks allow first. Footgun if misordered; needs its own validator and clear UI wording. Touches `service/`, `store/`, `ui/screens/home`. | Service model tests for precedence; validator tests |
| Import / export of masks | M | low | Storage Access Framework `CreateDocument`/`OpenDocument`, JSON via the existing `Record` serializer, merge with duplicate rule (A4). Never export `calls.json`. | Round-trip serializer test; device for the picker |
| Light theme / dynamic color | M | low | `ui/theme/*` (currently dark-only), `themes.xml`, and the `enableEdgeToEdge(SystemBarStyle.dark(...))` assumption in `BlockerActivity` (system bar icons must follow the scheme). | Previews in both themes; screenshot check on device |
| Compose UI tests | M-L | low | Stateless overloads already exist, so `createComposeRule` tests are cheap: Home list/empty/error, Editor validation states, Log grouping. Needs `androidTestImplementation` for `ui-test-junit4` (BOM-managed) and CI or a local emulator. Replace `ExampleInstrumentedTest`. | Instrumented (`connectedAndroidTest`) |
| Extra locales / per-app language | S per locale + M picker | low | New `values-xx/strings.xml`; lint `MissingTranslation` is an error so every string needs all locales. Per-app picker requires `res/xml/locales_config.xml` (does not exist yet, despite the CLAUDE.md mention) + manifest `android:localeConfig`. | Lint + pseudolocale run |
| STIR/SHAKEN "verification failed" option | S | medium | `callDetails.callerNumberVerificationStatus` (API 30+, so guard on SDK) as an opt-in setting next to hidden-number blocking. Carrier support is uneven; test coverage on emulator is limited. | Service model test with a fake status; real-device only |
| Log retention by age, per-entry delete, export | S-M | low | `CallLogStoreImpl` (cap is 200 entries today), `LogScreen`. | Store/ViewModel unit tests |
| Add national-format conversion for logging display | S | low | Display-only formatting through `PhoneNumberUtils.formatNumber`. | Preview |

## Recommended order

1. A10 Git housekeeping (clean diffs for everything after it)
2. A9 `ServiceModelFactory` cleanup, then A1 cached list (+ A2 direction guard in the same service change)
3. A7 DataStore hardening (before any new stored fields are added)
4. A6 matcher edge cases, then A4 validation and A5 editor hint/dismissal (all editor/matcher work together)
5. A3 role handling, A8 clear-log entry point
6. A11 release readiness, A12 device acceptance run, tag 1.0
7. Release 1.1: B0 settings, B1 `#`, B3 hidden numbers, B2 country code, B5 unblock from log, B6 test field, B4 notification
8. Later tier as wanted; Compose UI tests are best added just before the allowlist (the first feature that adds a second list and screen)

## Definition of done for 1.0

- The call path reads masks from memory; no unbounded `runBlocking` on the main thread; any failure or timeout allows the call.
- Only incoming calls are screened (verified on a device).
- A mask cannot be saved if empty, invalid or duplicate; match-all masks need explicit confirmation; the editor explains `*` and exact matching.
- Number-format cases (`+`, `00`, separators, `;`/post-dial suffixes, `tel:`) have unit tests; national-format limitation is documented in the UI hint.
- Losing the call-screening role is visible in the app; unavailable role does not crash.
- A strict or newer-version `numbers.json` cannot silently wipe the user's masks; DataStore delegates are singletons.
- The user can clear the log from the UI.
- `./gradlew assembleDebug testDebugUnitTest` and `assembleRelease` pass; the release APK passed the on-device run (A12); `git status` is clean on a fresh checkout.
- Signing, privacy policy, Data safety form and an honest store listing are ready; Play Console requirements re-checked at submission.

## Doc discrepancies found (to fix in the next CLAUDE.md update)

- CLAUDE.md says `tel:` URL-encoding is not handled; `Uri.getSchemeSpecificPart()` returns decoded text, so this is probably a non-issue (confirm per A6).
- CLAUDE.md mentions `locales_config.xml`; the file does not exist.
- TODO.md "Housekeeping" blames line endings; the actual cause is mixed file modes (see A10).
- TODO.md says the service "still uses `runBlocking`" for the read only; it also uses `runBlocking` for the log write.
