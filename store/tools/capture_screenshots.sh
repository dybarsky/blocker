#!/bin/bash
# Captures raw store screenshots (uk + en) from the debug build on an EMULATOR.
# Usage: ADB=/path/to/adb store/tools/capture_screenshots.sh emulator-5554
# Prereqs: `ANDROID_SERIAL=emulator-5554 ./gradlew installDebug` done.
# Refuses any serial that does not start with "emulator-" (never run against a real phone).
# Side effects ON THE EMULATOR ONLY: wm size 1080x1920 (Play wants <= 2:1), demo-mode status bar,
# app locale, call-screening role (re-granted at the end), seeded numbers.json/calls.json.
set -euo pipefail

serial="${1:-}"
case "$serial" in
  emulator-*) ;;
  *) echo "Usage: $0 emulator-XXXX (only emulators are allowed)" >&2; exit 1 ;;
esac

adb="${ADB:-adb}"
pkg=dybarsky.blocker
role=android.app.role.CALL_SCREENING
here="$(cd "$(dirname "$0")" && pwd)"
raw="$here/../graphics/raw"

a() { "$adb" -s "$serial" "$@"; }
demo() { a shell am broadcast -a com.android.systemui.demo "$@" >/dev/null; }
shot() { a exec-out screencap -p > "$1"; echo "wrote $1"; }
# Removing the role holder leaves the package "disabled until used" on some images; pm enable fixes it.
launch() { a shell pm enable "$pkg" >/dev/null; a shell am force-stop "$pkg";a shell am start -n "$pkg/.ui.BlockerActivity" >/dev/null; sleep 3; }

a shell wm size 1080x1920
a shell settings put global sysui_demo_allowed 1
demo -e command enter
demo -e command clock -e hhmm 1010
demo -e command battery -e level 100 -e plugged false
demo -e command network -e wifi show -e level 4 -e fully true
demo -e command network -e mobile show -e datatype lte -e level 4 -e fully true
demo -e command notifications -e visible false

for lang in uk en; do
  mkdir -p "$raw/$lang"
  a shell cmd locale set-app-locales "$pkg" --locales "$lang"
  a shell pm enable "$pkg" >/dev/null
  a shell cmd role add-role-holder "$role" "$pkg" 0 >/dev/null
  "$here/seed_emulator.sh" "$serial"

  launch
  shot "$raw/$lang/01_masks.png"

  # Editor dialog: "+" FAB, type an example mask, hide the keyboard (first Back only closes the IME).
  a shell input tap 901 1489
  sleep 1.5
  a shell "input text '+49%s145%s*'"
  sleep 0.5
  a shell input keyevent KEYCODE_BACK
  sleep 1
  shot "$raw/$lang/02_editor.png"

  launch
  a shell input tap 708 1731
  sleep 1.5
  shot "$raw/$lang/03_log.png"

  # Onboarding screen: revoke the role (emulator only), relaunch.
  a shell cmd role remove-role-holder "$role" "$pkg" 0 >/dev/null
  launch
  shot "$raw/$lang/04_role.png"
done

a shell cmd role add-role-holder "$role" "$pkg" 0 >/dev/null
demo -e command exit
a shell wm size reset
echo "done"
