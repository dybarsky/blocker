# Google Play Console checklist

Everything here is a draft to be checked against the live Play Console at submission time; policy pages and form wording change. Things that could not be verified are marked **(unverified)**.

Related files: `store/listings/` (texts), `store/graphics/` (images), `store/privacy-policy.md`, `store/NAMING.md`, `docs/ROADMAP.md`.

## 1. App basics

| Field | Value |
|---|---|
| App name (en-US) | Scam Call Blocker – Masks (25) |
| App name (uk) | Блокувальник шахраїв: маски (27) |
| Default language | Ukrainian (uk), add English (United States) `en-US` as a translation. The app itself is uk (default) + en. |
| App or game | App |
| Free or paid | Free (cannot be changed to paid later) |
| Package name | `dybarsky.blocker` (permanent once uploaded) |
| Category | **Tools** (recommended) |
| Tags | Pick what the console offers for call blocking / phone utilities |
| Contact email | `TODO: your public support email` |
| Website | optional, `TODO` |
| Privacy policy URL | `TODO: URL where you host store/privacy-policy.md` (required, public, not a PDF/editable doc) |

Category: Tools vs Communication. Recommend **Tools**: the app is a rule-based utility, it does not place calls or messages and is not a dialer. Communication tends to attract call-and-messaging apps with Phone/SMS permission declarations, which this app does not need. Communication would also be acceptable; do not pick Tools if the console shows a stricter review path for it **(unverified, check the category help text when choosing)**. The category can be changed later.

## 2. Store listing

Upload from `store/listings/{uk,en-US}/`: title, short description, full description. Release notes go into the release (section 9).

Graphics (`store/graphics/`):
- App icon 512x512: `icon-512.png` (do not regenerate; source `icon.svg`, script `tools/make_icon.sh`).
- Feature graphic 1024x500: `feature-graphic-uk-1024x500.png` for the uk listing, `feature-graphic-en-1024x500.png` for en-US.
- Phone screenshots (4 per language, 1080x1920, 24-bit PNG, no alpha): `screenshots/uk/*.png`, `screenshots/en/*.png`. Order: 01_masks, 02_editor, 03_log, 04_role.
- Tablet screenshots are optional; the app has no tablet-specific layout, so skip them.

Regenerate with `store/tools/capture_screenshots.sh` (emulator only) and `store/tools/compose_graphics.py`; check with `store/tools/check_store.py`.

Honesty review before publishing (Play metadata policy): the copy says the user defines the masks and that there is no automatic scam detection; it makes no ranking claims ("best", "#1") and no "free of ads" style claims; it does not promise "check who called". Keep it that way when editing. Search the final titles in the Play Store manually for conflicts (see `NAMING.md`).

## 3. Data safety form

Answer from the facts of the app:
- No `INTERNET` permission; no networking code; no third-party SDKs (no analytics, ads or crash reporting). The only manifest entries are the activity and the call-screening service. Verified from the debug APK with `aapt2 dump permissions` (only the AndroidX-internal `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`); re-check on the release AAB/APK.
- Masks (`numbers.json`) and blocked-call log (`calls.json`, which contains caller numbers, the matched mask and time) live in the app's private storage.

Recommended answers:

| Question | Answer |
|---|---|
| Does your app collect or share any of the required user data types? | **No** |
| Is all of the user data collected by your app encrypted in transit? | Not applicable if "No" above (the console skips it) |
| Do you provide a way for users to request that their data is deleted? | Not applicable if no data is collected; in the policy, say data is deleted by clearing app storage or uninstalling |

Policy basis (fetched 2026-11-18, support.google.com/googleplay/android-developer/answer/10787469): "Collect" means transmitting data from the app off the user's device; data that is "only processed locally on the user's device and not sent off device" does not need to be disclosed as collected; "sharing" means transferring collected data to a third party. This app never transmits data, so it collects and shares nothing in the sense of the form.

Auto Backup, answered honestly: `allowBackup="true"` and `numbers.json` (the masks) is included in Android Auto Backup and device transfer; `calls.json` is excluded from both (`res/xml/backup_rules.xml`, `res/xml/data_extraction_rules.xml`). Backup is performed by Android to the user's own Google account; the developer has no access to it and the app does not send it anywhere. The Play help page above does not mention Auto Backup at all **(unverified how Play reviewers treat it)**, so:
- Recommended: answer "No data collected / shared", because the developer never receives the masks, and state the backup behavior openly in the privacy policy and the listing (the full description already says it).
- If you want zero ambiguity, set `android:allowBackup="false"` (or exclude `datastore/numbers.json` too) before release. The cost: masks will not be restored on a new phone. This is a product decision; the roadmap (A10) currently prefers keeping backup.
- Masks are phone-number patterns the user typed, not contacts or calls of identified people, but a mask can be a full number, so treat them as potentially personal and keep the policy wording.

## 4. App content declarations

- **Privacy policy**: URL from section 1.
- **Ads**: No, the app contains no ads.
- **App access**: all functionality is available without login or special access. State that the app needs the user to set it as the call-screening app (system dialog) and that no account exists.
- **Target audience and content**: **18 and over** (or 13+ at most; do not select under-13 age groups). Not designed for children; do not opt into the Families program. Rationale: it deals with scam/unwanted calls and has no child-directed content. Selecting an adult-only audience avoids Families policy requirements.
- **News app**: No. **COVID-19 / health / government / financial features**: No.
- **Data safety**: section 3.
- **Advertising ID**: the app does not use it ("No"). The AAB must not contain the `AD_ID` permission; it does not (no ad SDKs).
- **Content rating (IARC questionnaire)**: category *Utility, Productivity, Communication or other* (the console wording may differ). Answers, all "No": violence, blood/gore, sexual content, nudity, profanity, controlled substances, gambling, simulated gambling, user-generated content shared between users, user-to-user communication, location sharing, digital purchases, unrestricted internet access (the app has no internet), horror, hate/discrimination. Expected result: rated for everyone (e.g. PEGI 3 / ESRB Everyone). The app does *not* offer users a way to communicate with others; it only blocks calls on the device.
- **Government apps / financial features / health apps / photo-video permissions**: not applicable.

## 5. Call screening role and permissions

- The app uses `RoleManager` with `ROLE_CALL_SCREENING` (user confirms through the system role dialog) and a `CallScreeningService` protected by `BIND_SCREENING_SERVICE`. It declares **no** `READ_CALL_LOG`, `READ_PHONE_STATE`, `READ_CONTACTS`, `ANSWER_PHONE_CALLS`, `PROCESS_OUTGOING_CALLS`, SMS or other runtime/dangerous permissions. The number is read from `Call.Details.getHandle()` handed to the service by the system.
- Play policy check (fetched 2026-11-18, https://support.google.com/googleplay/android-developer/answer/9047303, "Use of SMS or Call Log permission groups"): the policy governs apps that request Call Log / SMS permissions. It says such apps must be the default SMS/Phone/Assistant handler or qualify for a listed exception (which includes caller ID, spam detection and blocking, with `READ_CALL_LOG` etc.), and must submit a Permissions Declaration Form in Play Console. The page does **not** mention the call-screening role or `CallScreeningService`. Because the app requests none of those permissions, the Permissions Declaration Form should not apply, and the Play Console should not show a declaration prompt for it. **(unverified)** If the console does show a Call Log/SMS declaration or a "default handler" prompt at submission, answer that the app uses only the call-screening role and no Call Log/SMS permission, and do not add the permissions.
- Do not add `READ_CALL_LOG`, `READ_CONTACTS` or phone permissions later without re-reading the policy: it would trigger the declaration and review.
- Listing wording must explain the role request (it does: "Why the app asks to be your call screening app"). Keep the in-app text `role_purpose` consistent with the store text.

## 6. Pre-launch blockers worth finishing first (docs/ROADMAP.md, Tier 1 "Before 1.0")

Most relevant for a first public release, in suggested order:
1. **A10** Git housekeeping (clean tree for the release tag).
2. **A9 + A1 (+ A2)** Cached mask list off the call path, incoming-direction guard, drop the unchecked cast. A slow DataStore read on the main thread during the call is the main real-world risk of blocking or missing calls.
3. **A7** DataStore hardening: strict JSON means a newer or restored `numbers.json` with an unknown field would silently wipe all masks (relevant because masks are in backups).
4. **A6, A4, A5** Matcher edge cases, mask validation (match-all `*` warning, duplicates, trimming) and the editor hint explaining `*`; the listing promises these semantics, so the hint should be in the app.
5. **A3** Role lost / unavailable handling (otherwise Home looks fine while nothing is blocked).
6. **A8** Clear-log button in the UI (the log contains caller numbers; the privacy policy says users can delete data. Today the only way is clearing app storage).
7. **A11, A12** Release readiness and a real-phone acceptance run (role grant, block, allow, cold-start call, reboot, role revoked).

If you ship without A8, the privacy policy's "how to delete" section must keep only the "clear storage / uninstall" route (it does).

## 7. Signing and build

1. Create an upload keystore (keep it and passwords out of git and back it up). Copy `keystore.properties.example` to `keystore.properties` and fill `storeFile`, `storePassword`, `keyAlias`, `keyPassword`.
2. Enrol in **Play App Signing** when creating the app (default for new apps; Google holds the app signing key, you keep the upload key).
3. Bump `versionCode`/`versionName` in `app/build.gradle.kts` for every upload (currently 1 / `1.0.0`).
4. `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew bundleRelease`
5. Output: `app/build/outputs/bundle/release/app-release.aab`. R8 mapping: `app/build/outputs/mapping/release/mapping.txt`.
6. Upload the AAB; upload `mapping.txt` for the release (App bundle explorer or the release's deobfuscation file field) so crash/ANR traces are readable.
7. Target API: `targetSdk = 36`; Play requires 36 or higher for new apps and updates from 2026-08-31 (per `CLAUDE.md`; re-check the console message). minSdk 29.
8. Test the **release** build (R8 on) on a real phone first: role grant, block, allow, edit, log.

## 8. Testing tracks and review

1. Create the app, fill in sections 1-5 (Dashboard > "Set up your app" tasks).
2. Upload the first AAB to **Internal testing**, add yourself and a few testers by email, install through the opt-in link.
3. Review the **pre-launch report** (automated device testing) for crashes and accessibility findings; the app has no login, so the crawler should reach every screen except those needing the role.
4. Personal developer accounts created after Nov 2023 must run **closed testing with at least 12 testers for 14 days** before applying for production access **(unverified for 2026, check the current requirement in the console)**.
5. Promote to Production with a staged rollout (for example 20%), then increase.
6. Review typically takes from hours to several days; the first review of a new app can take longer.

## 9. Release notes

Paste from `store/listings/{uk,en-US}/release_notes.txt` into the release, wrapped as:

```
<en-US>
...contents of listings/en-US/release_notes.txt...
</en-US>
<uk>
...contents of listings/uk/release_notes.txt...
</uk>
```

## 10. Placeholders to fill

- Contact email, website (optional), privacy policy URL.
- Developer name/address shown on the listing (Play shows the address for paid/organization accounts; check your account type).
- Data safety, content rating and target audience forms (answers above) must be entered by you in the console.
- Hosting for `store/privacy-policy.md`.
