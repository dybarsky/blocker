#!/bin/bash
# Seeds sample masks and blocked-call log entries into the debug build on an EMULATOR.
# Usage: store/tools/seed_emulator.sh emulator-5554
# Refuses any serial that does not start with "emulator-" so a real phone is never touched.
set -euo pipefail

serial="${1:-}"
case "$serial" in
  emulator-*) ;;
  *) echo "Usage: $0 emulator-XXXX (only emulators are allowed)" >&2; exit 1 ;;
esac

adb="${ADB:-adb}"
pkg=dybarsky.blocker
here="$(cd "$(dirname "$0")" && pwd)"

numbers="$("${PYTHON:-python3}" - <<'PY'
import json, time
now = int(time.time() * 1000)
masks = ["+44165*", "+38050*4567", "+49 145 *", "0049 800*"]
print(json.dumps([{"id": now + i, "mask": m} for i, m in enumerate(masks)], ensure_ascii=False))
PY
)"

calls="$("${PYTHON:-python3}" - <<'PY'
import json, datetime
today = datetime.datetime.now().replace(second=0, microsecond=0)
def at(days_ago, hh, mm):
    d = (today - datetime.timedelta(days=days_ago)).replace(hour=hh, minute=mm)
    return int(d.timestamp() * 1000)
# (number as delivered by the system, matching mask, timestamp), oldest first
rows = [
    ("+380504274567", "+38050*4567", at(1, 8, 12)),
    ("+441651234567", "+44165*",     at(1, 15, 33)),
    ("+4914512345678", "+49 145 *",  at(1, 20, 17)),
    ("0049800123456", "0049 800*",   at(0, 9, 41)),
    ("+441659876543", "+44165*",     at(0, 11, 5)),
    ("+4914598765432", "+49 145 *",  at(0, 12, 20)),
]
print(json.dumps([{"number": n, "mask": m, "timestamp": t} for n, m, t in rows], ensure_ascii=False))
PY
)"

"$adb" -s "$serial" shell am force-stop "$pkg"
"$adb" -s "$serial" shell run-as "$pkg" mkdir -p files/datastore
printf '%s' "$numbers" | "$adb" -s "$serial" shell "run-as $pkg sh -c 'cat > files/datastore/numbers.json'"
printf '%s' "$calls" | "$adb" -s "$serial" shell "run-as $pkg sh -c 'cat > files/datastore/calls.json'"
echo "Seeded $serial"
