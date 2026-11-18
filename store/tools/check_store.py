#!/usr/bin/env python3
"""Checks Play listing text limits and graphic dimensions/modes. Needs Pillow for the image part.
Usage: store/tools/check_store.py   (exit code 1 on any failure)
"""
import sys
from pathlib import Path

STORE = Path(__file__).resolve().parent.parent
LIMITS = {"title": 30, "short_description": 80, "full_description": 4000, "release_notes": 500}
bad = 0


def fail(msg):
    global bad
    bad += 1
    print("FAIL", msg)


for lang in ("uk", "en-US"):
    for name, limit in LIMITS.items():
        p = STORE / "listings" / lang / f"{name}.txt"
        if not p.exists():
            fail(f"{p} missing")
            continue
        n = len(p.read_text(encoding="utf-8").rstrip("\n"))
        print(f"{'ok  ' if n <= limit else 'FAIL'} {lang}/{name}: {n}/{limit}")
        bad += n > limit

try:
    from PIL import Image
except ImportError:
    print("Pillow missing, skipping image checks")
    sys.exit(1 if bad else 0)

for lang in ("uk", "en"):
    p = STORE / "graphics" / f"feature-graphic-{lang}-1024x500.png"
    im = Image.open(p)
    ok = im.size == (1024, 500) and im.mode == "RGB"
    print(f"{'ok  ' if ok else 'FAIL'} {p.name}: {im.size} {im.mode}")
    bad += not ok
    shots = sorted((STORE / "graphics" / "screenshots" / lang).glob("*.png"))
    if len(shots) < 4:
        fail(f"{lang}: need >= 4 screenshots, have {len(shots)}")
    for s in shots:
        im = Image.open(s)
        ok = im.size == (1080, 1920) and im.mode == "RGB" and s.stat().st_size < 8 * 1024 * 1024
        print(f"{'ok  ' if ok else 'FAIL'} {lang}/{s.name}: {im.size} {im.mode} {s.stat().st_size // 1024} KB")
        bad += not ok

icon = Image.open(STORE / "graphics" / "icon-512.png")
print(f"{'ok  ' if icon.size == (512, 512) else 'FAIL'} icon-512.png: {icon.size} {icon.mode} (Play: 32-bit PNG up to 1 MB)")
sys.exit(1 if bad else 0)
