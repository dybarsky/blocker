#!/usr/bin/env python3
"""Builds the Play Store marketing images from the raw emulator captures.

Needs Pillow (`python3 -m venv .venv && .venv/bin/pip install Pillow`).
Usage: store/tools/compose_graphics.py

Inputs : store/graphics/raw/{uk,en}/NN_name.png (1080x1920 captures, see capture_screenshots.sh)
         store/graphics/icon-512.png
Outputs: store/graphics/screenshots/{uk,en}/NN_name.png  (1080x1920, 24-bit RGB, captioned)
         store/graphics/feature-graphic-{uk,en}-1024x500.png (RGB)
"""
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parent.parent / "graphics"
FONT_BOLD = "/System/Library/Fonts/Supplemental/Arial Bold.ttf"
FONT_REG = "/System/Library/Fonts/Supplemental/Arial.ttf"

BG = (17, 19, 24)          # app background
CARD = (55, 57, 62)        # app card
TEAL = (56, 178, 190)      # icon tile teal, lightened for dark backgrounds
SALMON = (255, 255, 255)      # icon badge (white)
TEXT = (232, 234, 237)
MUTED = (160, 166, 175)

# name -> {lang: (headline, sub)}
CAPTIONS = {
    "01_masks": {
        "uk": ("Ваші маски номерів", "Додайте шаблони на кшталт +44165*"),
        "en": ("Your number masks", "Add patterns like +44165*"),
    },
    "02_editor": {
        "uk": ("Маска за секунди", "Символ * замінює будь-які цифри"),
        "en": ("Add a mask in seconds", "The * stands for any digits"),
    },
    "03_log": {
        "uk": ("Журнал заблокованих", "Зберігається лише на вашому телефоні"),
        "en": ("Blocked-call log", "Stored only on your phone"),
    },
    "04_role": {
        "uk": ("Один крок до роботи", "Зробіть додаток блокувальником викликів"),
        "en": ("One step to get started", "Set it as your call screening app"),
    },
}

FEATURE = {
    "uk": ("Блокувальник шахраїв", "Відхиляє дзвінки за вашими масками номерів", "Без інтернету. Усе на пристрої."),
    "en": ("Scam Call Blocker", "Rejects calls that match your number masks", "No internet. Everything on your device."),
}
CHIPS = ["+44165*", "+49 145 *", "0049 800*"]


def font(path, size):
    return ImageFont.truetype(path, size)


def fit(draw, text, path, size, max_w):
    while size > 20 and draw.textlength(text, font=font(path, size)) > max_w:
        size -= 2
    return font(path, size)


def centered(draw, y, text, fnt, fill, width):
    w = draw.textlength(text, font=fnt)
    draw.text(((width - w) / 2, y), text, font=fnt, fill=fill)


def rounded_mask(size, radius, bottom_square=True):
    m = Image.new("L", size, 0)
    d = ImageDraw.Draw(m)
    d.rounded_rectangle((0, 0, size[0] - 1, size[1] - 1 + (radius if bottom_square else 0)), radius, fill=255)
    return m


def screenshot(lang, name):
    raw = Image.open(ROOT / "raw" / lang / f"{name}.png").convert("RGB")
    assert raw.size == (1080, 1920), raw.size
    W, H = 1080, 1920
    canvas = Image.new("RGB", (W, H), BG)
    d = ImageDraw.Draw(canvas)
    head, sub = CAPTIONS[name][lang]
    centered(d, 90, head, fit(d, head, FONT_BOLD, 84, W - 120), TEXT, W)
    centered(d, 205, sub, fit(d, sub, FONT_REG, 46, W - 120), MUTED, W)
    # Crop the status bar (demo clock/icons are not part of the story), scale, bleed off the bottom.
    body = raw.crop((0, 110, 1080, 1920))
    scale = 0.9
    body = body.resize((round(body.width * scale), round(body.height * scale)), Image.LANCZOS)
    x, y = (W - body.width) // 2, 340
    # soft frame
    frame = Image.new("RGB", (body.width + 16, body.height + 16), (44, 48, 56))
    canvas.paste(frame, (x - 8, y - 8), rounded_mask(frame.size, 64))
    canvas.paste(body, (x, y), rounded_mask(body.size, 56))
    out = ROOT / "screenshots" / lang
    out.mkdir(parents=True, exist_ok=True)
    canvas.crop((0, 0, W, H)).save(out / f"{name}.png", "PNG")


def chip(canvas, xy, text, fnt):
    d = ImageDraw.Draw(canvas)
    w = d.textlength(text, font=fnt)
    x, y = xy
    pad_x, pad_y = 22, 12
    box = (x, y, x + w + 2 * pad_x, y + fnt.size + 2 * pad_y + 4)
    d.rounded_rectangle(box, 22, fill=CARD)
    d.text((x + pad_x, y + pad_y), text, font=fnt, fill=TEXT)
    return box[2]


def feature(lang):
    W, H = 1024, 500
    canvas = Image.new("RGB", (W, H), BG)
    # subtle teal glow behind the icon
    glow = Image.new("RGB", (W, H), BG)
    gd = ImageDraw.Draw(glow)
    gd.ellipse((-120, -60, 520, 560), fill=(22, 52, 56))
    from PIL import ImageFilter
    canvas = Image.blend(canvas, glow.filter(ImageFilter.GaussianBlur(80)), 0.9)
    icon = Image.open(ROOT / "icon-512.png").convert("RGBA").resize((300, 300), Image.LANCZOS)
    icon_mask = Image.new("L", icon.size, 0)
    ImageDraw.Draw(icon_mask).rounded_rectangle((0, 0, 299, 299), 66, fill=255)
    canvas.paste(icon.convert("RGB"), (70, 100), icon_mask)

    d = ImageDraw.Draw(canvas)
    title, tagline, privacy = FEATURE[lang]
    tx = 420
    d.text((tx, 105), title, font=fit(d, title, FONT_BOLD, 60, W - tx - 40), fill=TEXT)
    d.text((tx, 190), tagline, font=fit(d, tagline, FONT_REG, 30, W - tx - 40), fill=MUTED)
    cf = font(FONT_BOLD, 30)
    x = tx
    for c in CHIPS:
        x = chip(canvas, (x, 262), c, cf) + 14
    d.text((tx, 372), privacy, font=fit(d, privacy, FONT_REG, 28, W - tx - 40), fill=TEAL)
    canvas.save(ROOT / f"feature-graphic-{lang}-1024x500.png", "PNG")


def main():
    for lang in ("uk", "en"):
        for name in CAPTIONS:
            screenshot(lang, name)
        feature(lang)
    print("done")


if __name__ == "__main__":
    main()
