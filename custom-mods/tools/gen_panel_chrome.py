#!/usr/bin/env python3
"""Rebuild the Hearthwind panel chrome textures from the Aged 3.1.2 reference.

Aged's LibZ-style panel (the Jobs and Level screens in
`.tmp/aged-gallery/Job_Screen.png`) is 200x236 logical pixels:

    rows 0..19    the black tab band the four LibZ tabs sit in
    rows 20..21   the 2 px white inner ring
    rows 22..230  the flat #C6C6C6 face
    rows 231..232 the #555555 bottom shadow
    rows 233..235 the black outer edge

    cols 0..1     the 2 px white left ring
    cols 2..194   the face
    cols 195..197 the #555555 right shadow (face rows only)
    cols 198..199 the black outer edge

The 20 px band is why the panels are 21 rows taller than the face-only
crop this script replaces: every content coordinate in the screens is
measured from the FACE, and the band only holds the tabs.

Run from the repo root:

    python3 custom-mods/tools/gen_panel_chrome.py
"""

from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
GUI = ROOT / "hearthwind-client/src/main/resources/assets/hearthwind/textures/gui"

WIDTH = 200
HEIGHT = 236
BAND = 20  # black tab band, rows 0..19
RING = 2  # white inner ring
FACE_ROWS = (22, 230)
SHADOW_ROWS = (231, 232)
RING_COLS = 2
SHADOW_COLS = (195, 197)
EDGE_COL = 198

BLACK = (0, 0, 0)
WHITE = (255, 255, 255)
FACE = (198, 198, 198)
SHADOW = (85, 85, 85)


def pixel(x: int, y: int) -> tuple:
    if y > SHADOW_ROWS[1]:
        # The black outer edge closes the bottom; the left ring stops with the
        # face, exactly as the Aged capture does.
        return BLACK
    if y >= SHADOW_ROWS[0] and y <= SHADOW_ROWS[1]:
        if x < RING_COLS:
            return WHITE
        if x < EDGE_COL:
            return SHADOW
        return BLACK
    if y >= FACE_ROWS[0] and y <= FACE_ROWS[1]:
        if x < RING_COLS:
            return WHITE
        if x >= SHADOW_COLS[0] and x < EDGE_COL:
            return SHADOW
        if x >= EDGE_COL:
            return BLACK
        return FACE
    if y < BAND:
        # The tab band is a flat black field; only the left ring stays white.
        return WHITE if x < RING_COLS else BLACK
    # The white ring under the band.
    return WHITE if x < EDGE_COL else BLACK


def build() -> Image.Image:
    img = Image.new("RGBA", (WIDTH, HEIGHT))
    img.putdata([pixel(x, y) for y in range(HEIGHT) for x in range(WIDTH)])
    return img


def main() -> None:
    img = build()
    for name in ("hearthwind_panel.png", "jobs/job_background.png"):
        out = GUI / name
        out.parent.mkdir(parents=True, exist_ok=True)
        img.save(out)
        print(f"wrote {out.relative_to(ROOT.parent)} ({WIDTH}x{HEIGHT})")


if __name__ == "__main__":
    main()
