# Google Play naming research

Goal: a Play Store title that says what the app does (rejects incoming calls whose number matches user-defined masks such as `+44165*`; on-device, no network), while staying within Play metadata policy.

Play constraints applied to every candidate: title <= 30 characters, no emoji or ALL CAPS, no ranking or performance claims ("best", "#1", "top", "free"), no third-party brand names, nothing that implies capabilities the app does not have.

Honesty note: the app is **rule-based** (the user enters masks). It does not detect scams automatically and has no spam database. Titles with "Scam Call Blocker" are common search keywords but could suggest automatic detection, so the store description must state clearly that the user defines the masks. A title that includes "Mask(s)" helps set that expectation.

## English candidates (<= 30)

| # | Title | Chars | Pros | Cons |
|---|---|---|---|---|
| E1 | Scam Call Blocker – Masks | 25 | Strong keyword ("scam call blocker"); "Masks" signals the actual mechanism | Still hints at auto-detection; the en dash is fine in Play titles but harder to type |
| E2 | Shakhrai: Scam Call Blocker | 27 | Keeps the brand (matches launcher label) plus keyword; brand + descriptor is a normal Play pattern | "Shakhrai" is hard to spell or remember for English speakers; no mention of masks |
| E3 | Call Blocker by Number Mask | 27 | Most accurate description of the feature; no scam-detection implication | Very generic; "call blocker" is the most crowded keyword |
| E4 | Block Scam Calls by Prefix | 26 | Action-oriented, clear benefit | Masks support `*` anywhere, not only prefixes, so "Prefix" understates the feature; reads like an ad slogan |
| E5 | Number Mask Call Blocker | 24 | Short, accurate, mask is the differentiator | No scam keyword; awkward noun stack |

**Top English pick: E1 "Scam Call Blocker – Masks" (25).** Safer fallback if the "scam" wording is judged misleading: E3.

## Ukrainian candidates (<= 30)

| # | Title | Chars | Pros | Cons |
|---|---|---|---|---|
| U1 | Блокувальник шахраїв: маски | 27 | Direct counterpart of E1; "шахраї" is what Ukrainians search for; mentions masks | "Блокувальник шахраїв" (blocks the fraudsters) is slightly figurative, it blocks calls |
| U2 | Шахрай: блокування дзвінків | 27 | Keeps the brand "Шахрай"; "блокування дзвінків" is the highest-volume phrase | Brand word alone ("Шахрай" = fraudster) reads oddly as an app name; no masks mention |
| U3 | Блокування дзвінків за маскою | 29 | Most accurate and keyword-rich; no brand | Generic; no scam keyword |
| U4 | Блок шахраїв за маскою номера | 29 | Short verb-free phrase with both "шахраї" and mask | "Блок" is colloquial; hard to read |
| U5 | Стоп шахраям: блок дзвінків | 27 | Memorable, emotive call to action | Slogan-like; no masks mention; "Стоп" phrase may be used by others |

**Top Ukrainian pick: U1 "Блокувальник шахраїв: маски" (27).** Brand-continuity alternative: U2.

**Pair that works well together: E1 "Scam Call Blocker – Masks" + U1 "Блокувальник шахраїв: маски".** Same structure, same keywords (scam/шахраї, blocker/блокувальник, masks/маски), so the two listings read as translations of each other. Brand-forward pair: E2 + U2.

## Existing-title check (web search)

Method: WebSearch (standard mode) with generic queries only. Limitation: WebSearch cannot query the Play Store search page directly and its index of Play listings is partial. A "not found" below means "not surfaced", not "does not exist". Before publishing, search each final title manually in the Play Store (and check the Play Console, which warns on duplicate titles within your own account only).

| Candidate | Findings |
|---|---|
| E1 Scam Call Blocker – Masks | No listing titled exactly "Scam Call Blocker" or with "– Masks" surfaced. The "X: Spam Call Blocker" pattern is extremely crowded (Truecaller, Robokiller, CallStop, Oblio One, Mr. Number, RealCall.AI, Stop Spam Call Blocker and many "Call Blocker: ..." clones), so the title is similar in style but not identical to any of them. Confusion risk: low for the exact title, but the keyword space is saturated. Two of those (CallStop, Oblio One) also block by prefix or number range, so they are direct competitors. |
| E2 Shakhrai: Scam Call Blocker | Nothing surfaced for "Shakhrai". Identical-title risk: low (inconclusive for the transliteration). |
| E3 Call Blocker by Number Mask | Similar generic titles exist ("Call Blocker - Block Numbers", "Calls Blacklist - Call Blocker", "Call Control. Call Blocker", "Prefix Blocker", "Call Prefix Filter"). None identical. Closest in meaning: Prefix Blocker and Call Prefix Filter (prefix/wildcard blocking). Risk: low, high keyword overlap. |
| E4 Block Scam Calls by Prefix | No identical title surfaced; "Prefix Blocker" and "Call Prefix Filter" are semantically close. |
| E5 Number Mask Call Blocker | No Play app with this name surfaced. Only an open-source GitHub project (CallGuardR, blocks by "number masks") and a Call Control help article on "block similar numbers (mask)". Not a Play title conflict. |
| U1 Блокувальник шахраїв: маски | Inconclusive. Ukrainian-language searches returned only news and review articles plus Oblio One, F-Droid apps and iOS listings; no Play app with this title surfaced. |
| U2 Шахрай: блокування дзвінків | Searched "Шахрай блокувальник дзвінків Google Play": no app named "Шахрай" surfaced. Inconclusive (Play Store itself not queryable). |
| U3 Блокування дзвінків за маскою | Inconclusive; no match surfaced. |
| U4 Блок шахраїв за маскою номера | Inconclusive; no match surfaced. |
| U5 Стоп шахраям: блок дзвінків | Searched "Стоп шахраям" (exact phrase): no app surfaced. Only an unrelated Russian "Стоп Спам" app (RuStore). Inconclusive for Play. |

Other findings worth knowing:
- Many competitors use the "<Brand>: Spam Call Blocker" pattern. Using "Scam" instead of "Spam" differentiates slightly, but "spam" has more search volume. Consider "Spam" in the short description or full description instead.
- In 2026 ESET reported 28 fake Play apps ("CallPhantom") that pretended to show other people's call logs. Avoid wording like "check who called", "call log of any number" or similar in metadata so the listing is not confused with those.
- Trademark: all candidates avoid brand names. "Call Blocker", "Spam Call Blocker" and "Number Mask" are descriptive terms, not marks, as far as the searches show (not a legal clearance).

## Suggested short descriptions (<= 80)

Short descriptions for the top picks:

- English (E1), 79 chars: `Reject calls from numbers matching your masks like +44165*. Private, on-device.`
- Ukrainian (U1), 72 chars: `Відхиляє дзвінки з номерів за вашими масками, як +44165*. Без інтернету.`

Both only claim what the app does: masks are user-defined, matching happens on the device, no network. Avoid "100% protection", "stops all scams" and similar guarantees.

## Notes on identifiers and launcher label

- **applicationId stays `dybarsky.blocker`.** It cannot be changed after publishing, and it is not shown to users. Do not touch it. A name change only affects `app_name`.
- The **launcher label** is `app_name` in `app/src/main/res/values/strings.xml` (uk, currently "Шахрай") and `app/src/main/res/values-en/strings.xml` (en, currently "Shakhrai"). Not changed by this task.
- The **Play title and the launcher label can differ.** The Play title is set in Play Console per locale (<= 30), the launcher label is what shows under the icon and is truncated at roughly 12 characters on many launchers.
- Suggested launcher labels (<= 12 chars):
  - Recommended: keep the brand, "Шахрай" (6) and "Shakhrai" (8). Short, distinctive, and still consistent with the U2/E2 brand-forward pair.
  - Descriptive alternative: "Блокувальник" (12) and "Call Blocker" (12). Clearer on the home screen but generic, and easy to confuse with other call-blocker apps installed on the same device.
- If the E1/U1 Play titles are used, the launcher label ("Шахрай"/"Shakhrai") does not need to change; the store icon and title carry the descriptive part.
