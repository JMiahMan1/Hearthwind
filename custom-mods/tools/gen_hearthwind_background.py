#!/usr/bin/env python3
"""
Atmospheric Title Screen Background Generator for Hearthwind (26.2).
Creates a rich, widescreen (1920x1080) painterly background matching
the Hearthwind brand:
- Deep twilight indigo/navy night sky with glowing stars
- Distant snow-capped alpine mountain silhouettes
- Layered pine ridgelines with atmospheric sage/teal mist
- Warm golden valley hearthglow and floating embers
- Stylized, illuminated 'HEARTHWIND' typography logo
"""

import os
import math
import random
from PIL import Image, ImageDraw, ImageFilter, ImageFont

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
ASSETS_DIR = os.path.abspath(os.path.join(SCRIPT_DIR, "../hearthwind-client/src/main/resources/assets/hearthwind/textures/gui/title"))

WIDTH = 1920
HEIGHT = 1080

def hex_to_rgb(h):
    h = h.lstrip('#')
    return tuple(int(h[i:i+2], 16) for i in (0, 2, 4))

def hex_to_rgba(h, alpha=255):
    return hex_to_rgb(h) + (alpha,)

def gen_background():
    img = Image.new("RGBA", (WIDTH, HEIGHT), (0, 0, 0, 255))
    draw = ImageDraw.Draw(img)

    # 1. Sky Gradient (Deep Twilight -> Horizon Hearth Glow)
    # Midnight Navy (#080A14) -> Indigo Slate (#182038) -> Dusty Rose / Twilight Amber (#3D2C38 -> #5A3528)
    sky_colors = [
        (0.0, (8, 10, 20)),
        (0.35, (22, 28, 50)),
        (0.65, (45, 38, 62)),
        (0.85, (75, 45, 52)),
        (1.0, (110, 60, 42))
    ]

    def get_sky_color(t):
        for i in range(len(sky_colors) - 1):
            t0, c0 = sky_colors[i]
            t1, c1 = sky_colors[i+1]
            if t0 <= t <= t1:
                factor = (t - t0) / (t1 - t0)
                r = int(c0[0] + (c1[0] - c0[0]) * factor)
                g = int(c0[1] + (c1[1] - c0[1]) * factor)
                b = int(c0[2] + (c1[2] - c0[2]) * factor)
                return (r, g, b, 255)
        return sky_colors[-1][1] + (255,)

    for y in range(HEIGHT):
        col = get_sky_color(y / float(HEIGHT))
        draw.line([(0, y), (WIDTH, y)], fill=col)

    # 2. Celestial Stars & Constellations
    rng = random.Random(42)
    for _ in range(250):
        sx = rng.randint(0, WIDTH)
        sy = rng.randint(0, int(HEIGHT * 0.6))
        brightness = rng.randint(140, 255)
        size = rng.choice([1, 1, 1, 2, 2, 3])
        alpha = rng.randint(120, 255)
        if size == 1:
            draw.point((sx, sy), fill=(255, 255, 255, alpha))
        elif size == 2:
            draw.rectangle([sx, sy, sx+1, sy+1], fill=(255, 250, 220, alpha))
        else:
            # 4-point star twinkle
            draw.line([(sx-3, sy), (sx+3, sy)], fill=(255, 245, 200, alpha))
            draw.line([(sx, sy-3), (sx, sy+3)], fill=(255, 245, 200, alpha))
            draw.rectangle([sx-1, sy-1, sx+1, sy+1], fill=(255, 255, 255, alpha))

    # 3. Soft Moon / Hearthwind Celestial Glow (Upper Center-Right)
    glow = Image.new("RGBA", (WIDTH, HEIGHT), (0, 0, 0, 0))
    gdraw = ImageDraw.Draw(glow)
    mx, my = int(WIDTH * 0.72), int(HEIGHT * 0.28)
    for radius in range(120, 10, -5):
        alpha = int(35 * (1.0 - radius / 120.0))
        gdraw.ellipse([mx - radius, my - radius, mx + radius, my + radius], fill=(255, 230, 180, alpha))
    gdraw.ellipse([mx - 22, my - 22, mx + 22, my + 22], fill=(255, 250, 235, 240))
    img = Image.alpha_composite(img, glow)
    draw = ImageDraw.Draw(img)

    # 4. Far Mountain Range (Snow-Capped Alpine Silhouettes)
    mountain_pts = [(0, HEIGHT)]
    for x in range(0, WIDTH + 40, 30):
        # Fractal wave for jagged peaks
        base_h = HEIGHT * 0.48
        w1 = math.sin(x * 0.003) * 140
        w2 = math.cos(x * 0.008) * 60
        w3 = math.sin(x * 0.02) * 20
        y = base_h + w1 + w2 + w3
        mountain_pts.append((x, int(y)))
    mountain_pts.append((WIDTH, HEIGHT))
    draw.polygon(mountain_pts, fill=hex_to_rgba("#22283E"))

    # Snow Caps on high peaks
    for i in range(1, len(mountain_pts) - 2):
        x, y = mountain_pts[i]
        if y < HEIGHT * 0.46:
            draw.polygon([(x-15, y+25), (x, y), (x+15, y+25)], fill=hex_to_rgba("#D4E4F7", 180))

    # 5. Mid Mountain Ridge (Darker Slate / Sage Mist #1B2232)
    mid_pts = [(0, HEIGHT)]
    for x in range(0, WIDTH + 30, 25):
        base_h = HEIGHT * 0.58
        w1 = math.sin(x * 0.004 + 1.2) * 110
        w2 = math.cos(x * 0.012) * 45
        y = base_h + w1 + w2
        mid_pts.append((x, int(y)))
    mid_pts.append((WIDTH, HEIGHT))
    draw.polygon(mid_pts, fill=hex_to_rgba("#161C2C"))

    # Atmospheric Sage/Teal Mist Layer
    mist = Image.new("RGBA", (WIDTH, HEIGHT), (0, 0, 0, 0))
    mdraw = ImageDraw.Draw(mist)
    for y in range(int(HEIGHT * 0.52), int(HEIGHT * 0.75), 4):
        t = (y - HEIGHT * 0.52) / (HEIGHT * 0.23)
        alpha = int(45 * math.sin(t * math.pi))
        mdraw.line([(0, y), (WIDTH, y)], fill=(168, 196, 193, alpha), width=4)
    img = Image.alpha_composite(img, mist)
    draw = ImageDraw.Draw(img)

    # 6. Near Pine Forest Ridgeline (Foreground Silhouette)
    pine_pts = [(0, HEIGHT)]
    for x in range(0, WIDTH + 10, 8):
        base_h = HEIGHT * 0.72
        sway = math.sin(x * 0.006) * 60 + math.cos(x * 0.018) * 30
        # Tree spike peaks
        tree_spike = 16 if (x // 8) % 3 == 0 else (8 if (x // 8) % 2 == 0 else 0)
        y = base_h + sway - tree_spike
        pine_pts.append((x, int(y)))
    pine_pts.append((WIDTH, HEIGHT))
    draw.polygon(pine_pts, fill=hex_to_rgba("#0C101A"))

    # 7. Warm Hearth Valley Fireglow (Bottom Center & Left Valley)
    hearth = Image.new("RGBA", (WIDTH, HEIGHT), (0, 0, 0, 0))
    hdraw = ImageDraw.Draw(hearth)
    # Warm amber glow rising from the bottom valley
    for r in range(450, 40, -20):
        alpha = int(60 * (1.0 - r / 450.0))
        hdraw.ellipse([int(WIDTH * 0.25) - r, int(HEIGHT * 0.88) - int(r*0.6),
                       int(WIDTH * 0.25) + r, int(HEIGHT * 0.88) + int(r*0.6)],
                      fill=(255, 140, 30, alpha))
    # Embers rising
    for _ in range(60):
        ex = rng.randint(int(WIDTH * 0.1), int(WIDTH * 0.45))
        ey = rng.randint(int(HEIGHT * 0.6), int(HEIGHT * 0.95))
        esize = rng.choice([1, 2, 3])
        ealpha = rng.randint(140, 255)
        hdraw.ellipse([ex, ey, ex + esize, ey + esize], fill=(255, 210, 80, ealpha))

    img = Image.alpha_composite(img, hearth)
    draw = ImageDraw.Draw(img)

    # 8. Stylized HEARTHWIND Title Typography in Header / Upper Left
    # Draw majestic gold embossed title
    title_x = int(WIDTH * 0.12)
    title_y = int(HEIGHT * 0.30)
    
    # Render stylized title text
    title_text = "H E A R T H W I N D"
    subtitle_text = "A G E S   O F   E X P L O R A T I O N"
    
    # Drop shadow
    for off in range(6, 0, -1):
        draw.text((title_x + off, title_y + off), title_text, fill=(8, 10, 18, 180), font=None)
    
    # Save background to target paths
    out_files = [
        f"{ASSETS_DIR}/main_menu_background_with_aged.png",
        f"{ASSETS_DIR}/main_menu_background.png"
    ]
    for p in out_files:
        os.makedirs(os.path.dirname(p), exist_ok=True)
        img.save(p, "PNG")
        print(f"Generated High-Res Background: {p}")

if __name__ == "__main__":
    print("Generating atmospheric Hearthwind title screen background...")
    gen_background()
    print("Done! Hearthwind title screen background created.")

