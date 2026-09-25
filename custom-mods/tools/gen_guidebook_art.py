#!/usr/bin/env python3
"""Generate the Hearthwind guidebook art (deterministic, re-runnable).

Outputs (into hearthwind-survival resources):
  assets/hearthwind/textures/gui/hearthwind_guide_book.png  - Lavender book sheet
  assets/hearthwind/textures/item/hearthwind_guide_book.png - 16x16 item icon
  assets/hearthwind/textures/gui/guide/initial/<a-z>.png    - illuminated initials
  assets/hearthwind/textures/gui/guide/divider.png          - gilt knot divider

The GUI sheet is derived from Lavender's MIT-licensed brown_book.png so every
widget keeps the exact UV layout Lavender's book.xml expects. Only the open
spread (cover + pages) is restyled: tooled oxblood leather with brass corner
caps, and aged parchment with darkened edges, fibres and foxing. The item icon
is hand-drawn pixel art.

Usage: python3 custom-mods/tools/gen_guidebook_art.py
"""

import random
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "lavender/src/main/resources/assets/lavender/textures/gui/brown_book.png"
OUT = ROOT / "hearthwind-survival/src/main/resources/assets/hearthwind/textures"

# Region of the sheet holding the open book spread (Lavender book.xml, u/v 0,0).
SPREAD_W, SPREAD_H = 266, 179

PARCHMENT_DARK = (178, 140, 88)
PARCHMENT_LIGHT = (236, 214, 170)
LEATHER_DARK = (38, 14, 11)
LEATHER_LIGHT = (116, 46, 32)
OUTLINE = (18, 8, 6)
BRASS_SHADOW = (112, 76, 26)
BRASS = (190, 142, 56)
BRASS_LIGHT = (236, 196, 108)
GILT = (156, 108, 48)
INK = (96, 62, 34)


def lum(c):
    return 0.3 * c[0] + 0.59 * c[1] + 0.11 * c[2]


def lerp(a, b, t):
    t = max(0.0, min(1.0, t))
    return tuple(round(a[i] + (b[i] - a[i]) * t) for i in range(3))


def jitter(c, rng, amount):
    d = rng.randint(-amount, amount)
    return tuple(max(0, min(255, v + d)) for v in c)


def page_mask(im):
    mask = set()
    for y in range(SPREAD_H):
        for x in range(SPREAD_W):
            p = im.getpixel((x, y))
            if p[3] > 0 and lum(p) >= 150:
                mask.add((x, y))
    return mask


def edge_distance(mask, limit=10):
    """Chebyshev-ish distance from each page pixel to the nearest non-page pixel."""
    dist = {}
    frontier = [p for p in mask
                if any((p[0] + dx, p[1] + dy) not in mask
                       for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))]
    for p in frontier:
        dist[p] = 0
    d = 0
    while frontier and d < limit:
        d += 1
        nxt = []
        for x, y in frontier:
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                q = (x + dx, y + dy)
                if q in mask and q not in dist:
                    dist[q] = d
                    nxt.append(q)
        frontier = nxt
    return dist


def restyle_spread(im):
    rng = random.Random(1337)
    pages = page_mask(im)
    dist = edge_distance(pages)
    fibre_rows = {y: rng.uniform(-2, 2) for y in range(SPREAD_H)}
    spots = [(rng.randrange(8, SPREAD_W - 8), rng.randrange(8, SPREAD_H - 8), rng.uniform(1.2, 2.6))
             for _ in range(14)]

    for y in range(SPREAD_H):
        for x in range(SPREAD_W):
            p = im.getpixel((x, y))
            if p[3] == 0:
                continue
            L = lum(p)
            if (x, y) in pages:
                t = (L - 150) / 105.0
                c = lerp(PARCHMENT_DARK, PARCHMENT_LIGHT, 0.35 + 0.65 * t)
                # darkened, handled edges
                e = dist.get((x, y), 10)
                if e < 10:
                    c = lerp(c, PARCHMENT_DARK, (10 - e) / 10.0 * 0.55)
                # horizontal paper fibres + grain
                f = fibre_rows[y]
                c = tuple(max(0, min(255, round(v + f))) for v in c)
                c = jitter(c, rng, 3)
                # foxing: faint brown age spots, kept near the margins
                for sx, sy, r in spots:
                    dd = ((x - sx) ** 2 + (y - sy) ** 2) ** 0.5
                    if dd < r and e < 18:
                        c = lerp(c, INK, 0.18 * (1 - dd / r))
                im.putpixel((x, y), c + (p[3],))
            elif L < 22:
                im.putpixel((x, y), OUTLINE + (p[3],))
            else:
                t = (L - 22) / 110.0
                c = jitter(lerp(LEATHER_DARK, LEATHER_LIGHT, t), rng, 4)
                im.putpixel((x, y), c + (p[3],))
    return pages


def frame_bounds(im, pages):
    xs, ys = [], []
    for y in range(SPREAD_H):
        for x in range(SPREAD_W):
            if im.getpixel((x, y))[3] > 0 and (x, y) not in pages:
                xs.append(x)
                ys.append(y)
    return min(xs), min(ys), max(xs), max(ys)


def gilt_tooling(im, pages, bounds):
    """Dotted gilt line tooled along the middle of the leather frame."""
    x0, y0, x1, y1 = bounds
    inset = 4
    for x in range(x0 + 12, x1 - 11, 3):
        for y in (y0 + inset, y1 - inset):
            if (x, y) not in pages and im.getpixel((x, y))[3] > 0:
                im.putpixel((x, y), GILT + (255,))
    for y in range(y0 + 12, y1 - 11, 3):
        for x in (x0 + inset, x1 - inset):
            if (x, y) not in pages and im.getpixel((x, y))[3] > 0:
                im.putpixel((x, y), GILT + (255,))


def brass_corners(im, bounds, size=11):
    x0, y0, x1, y1 = bounds
    corners = ((x0, y0, 1, 1), (x1, y0, -1, 1), (x0, y1, 1, -1), (x1, y1, -1, -1))
    for cx, cy, sx, sy in corners:
        for i in range(size):
            for j in range(size - i):
                x, y = cx + sx * i, cy + sy * j
                if im.getpixel((x, y))[3] == 0:
                    continue
                edge = i == 0 or j == 0 or i + j == size - 1
                if edge:
                    c = BRASS_SHADOW if (i + j == size - 1) else BRASS_LIGHT
                else:
                    c = BRASS
                im.putpixel((x, y), c + (255,))
        # rivet
        rx, ry = cx + sx * 3, cy + sy * 3
        im.putpixel((rx, ry), BRASS_LIGHT + (255,))
        im.putpixel((rx + sx, ry + sy), BRASS_SHADOW + (255,))


# 16x16 icon: leather-bound book, brass corners and clasp, parchment edge.
ICON_PALETTE = {
    ".": None,
    "o": OUTLINE,
    "d": LEATHER_DARK,
    "l": (84, 32, 24),
    "L": LEATHER_LIGHT,
    "b": BRASS_SHADOW,
    "B": BRASS,
    "h": BRASS_LIGHT,
    "p": PARCHMENT_DARK,
    "P": PARCHMENT_LIGHT,
    "g": GILT,
}
ICON = [
    "................",
    "..oooooooooooo..",
    ".ohBllllllllBho.",
    ".oBLLLLLLLLLLBo.",
    ".olLgggggggggLo.",
    ".olLgLLLLLLLgLoP",
    ".olLgLLhhLLLgLoP",
    ".olLgLhBBhLLgBBo",
    ".olLgLhBBhLLgbho",
    ".olLgLLhhLLLgLoP",
    ".olLgLLLLLLLgLoP",
    ".olLgggggggggLoP",
    ".oBlllllllllllBo",
    ".ohbddddddddbho.",
    "..oooooooooooo..",
    "................",
]


def draw_icon():
    icon = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(ICON):
        for x, ch in enumerate(row):
            c = ICON_PALETTE[ch]
            if c is not None:
                icon.putpixel((x, y), c + (255,))
    return icon


# --- Manuscript ornaments -------------------------------------------------
VERMILION = (142, 36, 22)
VERMILION_DARK = (92, 20, 12)
LAPIS = (35, 64, 122)
LAPIS_DARK = (20, 36, 76)

# 7x9 book-hand capitals (1 = ink). Drawn at 2x inside the initial tile.
GLYPHS = {
    "A": ["..###..", ".##.##.", "##...##", "##...##", "#######", "##...##", "##...##", "##...##", "##...##"],
    "B": ["######.", "##...##", "##...##", "######.", "##...##", "##...##", "##...##", "##...##", "######."],
    "C": [".#####.", "##...##", "##.....", "##.....", "##.....", "##.....", "##.....", "##...##", ".#####."],
    "D": ["#####..", "##..##.", "##...##", "##...##", "##...##", "##...##", "##...##", "##..##.", "#####.."],
    "E": ["#######", "##.....", "##.....", "#####..", "##.....", "##.....", "##.....", "##.....", "#######"],
    "F": ["#######", "##.....", "##.....", "#####..", "##.....", "##.....", "##.....", "##.....", "##....."],
    "G": [".#####.", "##...##", "##.....", "##.....", "##.####", "##...##", "##...##", "##...##", ".######"],
    "H": ["##...##", "##...##", "##...##", "#######", "##...##", "##...##", "##...##", "##...##", "##...##"],
    "I": ["#######", "..###..", "..###..", "..###..", "..###..", "..###..", "..###..", "..###..", "#######"],
    "J": ["..#####", "....##.", "....##.", "....##.", "....##.", "....##.", "##..##.", "##..##.", ".####.."],
    "K": ["##...##", "##..##.", "##.##..", "####...", "###....", "####...", "##.##..", "##..##.", "##...##"],
    "L": ["##.....", "##.....", "##.....", "##.....", "##.....", "##.....", "##.....", "##.....", "#######"],
    "M": ["##...##", "###.###", "#######", "##.#.##", "##...##", "##...##", "##...##", "##...##", "##...##"],
    "N": ["##...##", "###..##", "####.##", "##.####", "##..###", "##...##", "##...##", "##...##", "##...##"],
    "O": [".#####.", "##...##", "##...##", "##...##", "##...##", "##...##", "##...##", "##...##", ".#####."],
    "P": ["######.", "##...##", "##...##", "##...##", "######.", "##.....", "##.....", "##.....", "##....."],
    "Q": [".#####.", "##...##", "##...##", "##...##", "##...##", "##.#.##", "##..##.", ".####.#", "......#"],
    "R": ["######.", "##...##", "##...##", "##...##", "######.", "####...", "##.##..", "##..##.", "##...##"],
    "S": [".#####.", "##...##", "##.....", ".###...", "...###.", ".....##", ".....##", "##...##", ".#####."],
    "T": ["#######", "..###..", "..###..", "..###..", "..###..", "..###..", "..###..", "..###..", "..###.."],
    "U": ["##...##", "##...##", "##...##", "##...##", "##...##", "##...##", "##...##", "##...##", ".#####."],
    "V": ["##...##", "##...##", "##...##", "##...##", "##...##", ".##.##.", ".##.##.", "..###..", "...#..."],
    "W": ["##...##", "##...##", "##...##", "##...##", "##.#.##", "#######", "###.###", "##...##", "#.....#"],
    "X": ["##...##", "##...##", ".##.##.", "..###..", "...#...", "..###..", ".##.##.", "##...##", "##...##"],
    "Y": ["##...##", "##...##", ".##.##.", "..###..", "..###..", "..###..", "..###..", "..###..", "..###.."],
    "Z": ["#######", ".....##", "....##.", "...##..", "..##...", ".##....", "##.....", "##.....", "#######"],
}


def draw_initial(letter, index):
    size = 22
    tile = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    ground, ground_dark = (LAPIS, LAPIS_DARK) if index % 2 == 0 else (VERMILION, VERMILION_DARK)
    for y in range(size):
        for x in range(size):
            if x in (0, size - 1) or y in (0, size - 1):
                c = OUTLINE
            elif x in (1, size - 2) or y in (1, size - 2):
                c = BRASS_LIGHT if (x + y) % 2 == 0 else BRASS
            else:
                # subtle diagonal diaper pattern on the ground
                c = ground_dark if (x + y) % 4 == 0 else ground
            tile.putpixel((x, y), c + (255,))
    # gilt filigree dots in the four inner corners
    for cx, cy in ((3, 3), (size - 4, 3), (3, size - 4), (size - 4, size - 4)):
        tile.putpixel((cx, cy), BRASS_LIGHT + (255,))
    glyph = GLYPHS[letter]
    ox, oy = (size - 14) // 2, (size - 18) // 2
    for gy, row in enumerate(glyph):
        for gx, ch in enumerate(row):
            if ch != "#":
                continue
            for dy in (0, 1):
                for dx in (0, 1):
                    x, y = ox + gx * 2 + dx, oy + gy * 2 + dy
                    # drop shadow then parchment letter
                    if x + 1 < size - 2 and y + 1 < size - 2:
                        tile.putpixel((x + 1, y + 1), OUTLINE + (255,))
    for gy, row in enumerate(glyph):
        for gx, ch in enumerate(row):
            if ch != "#":
                continue
            for dy in (0, 1):
                for dx in (0, 1):
                    x, y = ox + gx * 2 + dx, oy + gy * 2 + dy
                    tile.putpixel((x, y), (PARCHMENT_LIGHT if dy == 0 else PARCHMENT_DARK) + (255,))
    return tile


def draw_divider(width=100):
    """Gilt rule with a central lapis lozenge and vermilion terminals."""
    h = 7
    div = Image.new("RGBA", (width, h), (0, 0, 0, 0))
    mid = width // 2
    for x in range(6, width - 6):
        div.putpixel((x, 3), GILT + (255,))
        if (x - mid) % 4 == 0:
            div.putpixel((x, 2), BRASS_LIGHT + (255,))
            div.putpixel((x, 4), BRASS_SHADOW + (255,))
    for dx in range(-3, 4):
        for dy in range(-3, 4):
            if abs(dx) + abs(dy) <= 3:
                edge = abs(dx) + abs(dy) == 3
                div.putpixel((mid + dx, 3 + dy), (OUTLINE if edge else LAPIS) + (255,))
    div.putpixel((mid, 3), BRASS_LIGHT + (255,))
    for tx in (3, width - 4):
        for dx in range(-2, 3):
            for dy in range(-2, 3):
                if abs(dx) + abs(dy) <= 2:
                    div.putpixel((tx + dx, 3 + dy), (VERMILION_DARK if abs(dx) + abs(dy) == 2 else VERMILION) + (255,))
    return div


def gild_title_rule(sheet):
    """Lavender's page-title rule lives at u54 v180, 109x3 on the sheet."""
    for x in range(54, 54 + 109):
        for y in range(180, 183):
            if sheet.getpixel((x, y))[3] > 0:
                sheet.putpixel((x, y), (BRASS_LIGHT if y == 180 else GILT if y == 181 else BRASS_SHADOW) + (255,))


def main():
    sheet = Image.open(SRC).convert("RGBA")
    pages = restyle_spread(sheet)
    bounds = frame_bounds(sheet, pages)
    gilt_tooling(sheet, pages, bounds)
    brass_corners(sheet, bounds)
    gild_title_rule(sheet)
    (OUT / "gui").mkdir(parents=True, exist_ok=True)
    (OUT / "item").mkdir(parents=True, exist_ok=True)
    sheet.save(OUT / "gui/hearthwind_guide_book.png")
    draw_icon().save(OUT / "item/hearthwind_guide_book.png")
    guide = OUT / "gui/guide"
    (guide / "initial").mkdir(parents=True, exist_ok=True)
    for i, letter in enumerate(sorted(GLYPHS)):
        draw_initial(letter, i).save(guide / "initial" / f"{letter.lower()}.png")
    draw_divider().save(guide / "divider.png")
    print("wrote", OUT / "gui/hearthwind_guide_book.png", "and item icon; frame", bounds)


if __name__ == "__main__":
    main()
