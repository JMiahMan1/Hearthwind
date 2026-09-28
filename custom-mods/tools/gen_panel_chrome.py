#!/usr/bin/env python3
"""Rebuild the Hearthwind panel chrome texture from the Aged 3.1.2 reference.

Measured off `.tmp/aged-gallery/Job_Screen.png` (2845x1600, composited onto
white because the capture has a transparent page background) with the panel's
top left at image (823, 225) and 6 physical px per logical px. The Aged
LibZ-style panel is **200x215** logical pixels:

    row 0        the black top edge
    rows 1..2    the 2 px white inner ring
    rows 3..211  the flat #C6C6C6 face
    rows 212..213 the #555555 bottom shadow
    row 214      the black bottom edge

    col 0        the black left edge
    cols 1..2    the 2 px white inner ring
    cols 3..196  the face
    cols 197..198 the #555555 right shadow
    col 199      the black right edge

There is **no tab band**: the four LibZ tabs float on the 21 rows *above* the
panel (release 0.1.32 briefly believed otherwise and grew the panel to 236;
0.1.33 restored the measured 215 and the float). Aged's own
`jobs/job_background.png` region already matches these values, so this script
only writes the shared `hearthwind_panel.png` the Skills/Party panels use.

Run from the repo root:

    python3 custom-mods/tools/gen_panel_chrome.py
"""

from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
GUI = ROOT / "hearthwind-client/src/main/resources/assets/hearthwind/textures/gui"

WIDTH = 200
HEIGHT = 215
RING = 2  # white inner ring, cols 1..2 / rows 1..2
SHADOW_COLS = (197, 198)
SHADOW_ROWS = (212, 213)  # rows 212..213

BLACK = (0, 0, 0)
WHITE = (255, 255, 255)
FACE = (198, 198, 198)
SHADOW = (85, 85, 85)

# Aged's bevel steps 1 px diagonally at the corners instead of running the
# ring and the shadow straight. These ten pixels are the whole difference
# between a straight bevel and the reference: a black notch in the top-left
# corner, the ring/shadow staircase in the other three, and the single inner
# highlight pixel at (3, 3).
CORNER_PIXELS = {
    (1, 1): BLACK,
    (WIDTH - 3, 1): WHITE,
    (WIDTH - 2, 1): BLACK,
    (WIDTH - 3, 2): FACE,
    (3, 3): WHITE,
    (WIDTH - 4, HEIGHT - 4): SHADOW,
    (1, HEIGHT - 3): WHITE,
    (2, HEIGHT - 3): FACE,
    (1, HEIGHT - 2): BLACK,
    (WIDTH - 2, HEIGHT - 2): BLACK,
}


def pixel(x: int, y: int) -> tuple:
    """One chrome pixel; the face is everything that is neither ring nor edge."""
    corner = CORNER_PIXELS.get((x, y))
    if corner is not None:
        return corner
    on_edge_row = y == 0 or y == HEIGHT - 1
    on_edge_col = x == 0 or x == WIDTH - 1
    if on_edge_row or on_edge_col:
        return BLACK
    if y in SHADOW_ROWS or x in SHADOW_COLS:
        return SHADOW
    if y < RING + 1 or x < RING + 1:
        return WHITE
    return FACE


def build() -> Image.Image:
    img = Image.new("RGBA", (WIDTH, HEIGHT))
    img.putdata([pixel(x, y) for y in range(HEIGHT) for x in range(WIDTH)])
    return img


def main() -> None:
    img = build()
    out = GUI / "hearthwind_panel.png"
    img.save(out)
    print(f"wrote {out.relative_to(ROOT.parent)} ({WIDTH}x{HEIGHT})")


if __name__ == "__main__":
    main()
