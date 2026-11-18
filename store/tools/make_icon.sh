#!/bin/bash
# Renders store/graphics/icon.svg to store/graphics/icon-512.png (512x512, full-bleed square).
# icon.svg is hand-maintained and mirrors the Android vector drawables
# (app/src/main/res/drawable/ic_launcher_{background,foreground}.xml); edit them together.
#
# Uses the first available renderer: rsvg-convert, ImageMagick, or macOS Quick Look (qlmanage).
# Usage: store/tools/make_icon.sh
set -euo pipefail

root="$(cd "$(dirname "$0")/.." && pwd)"
svg="$root/graphics/icon.svg"
png="$root/graphics/icon-512.png"

if command -v rsvg-convert >/dev/null; then
  rsvg-convert -w 512 -h 512 "$svg" -o "$png"
elif command -v magick >/dev/null; then
  magick -background none -density 384 "$svg" -resize 512x512 "$png"
elif command -v qlmanage >/dev/null; then
  tmp="$(mktemp -d)"
  qlmanage -t -s 512 -o "$tmp" "$svg" >/dev/null
  mv "$tmp/icon.svg.png" "$png"
  rmdir "$tmp"
else
  echo "Need rsvg-convert, ImageMagick (magick) or macOS qlmanage" >&2
  exit 1
fi

echo "Wrote $png"
