#!/usr/bin/env python3
"""
High-Resolution Mod Icon Generator for Hearthwind (26.2).
Generates beautiful, handcrafted 128x128 thematic icons with rich color grading,
smooth gradients, crisp highlights, and distinctive Hearthwind aesthetic.
"""

import os
import math
from PIL import Image, ImageDraw, ImageFilter

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
MODS_DIR = os.path.abspath(os.path.join(SCRIPT_DIR, ".."))

def hex_to_rgba(h, alpha=255):
    h = h.lstrip('#')
    return tuple(int(h[i:i+2], 16) for i in (0, 2, 4)) + (alpha,)

def create_base_canvas(bg_color_top, bg_color_bot, border_color):
    img = Image.new("RGBA", (128, 128), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    # Circular mask / background with smooth gradient
    for y in range(128):
        t = y / 127.0
        # Interpolate color
        r = int(bg_color_top[0] * (1 - t) + bg_color_bot[0] * t)
        g = int(bg_color_top[1] * (1 - t) + bg_color_bot[1] * t)
        b = int(bg_color_top[2] * (1 - t) + bg_color_bot[2] * t)
        for x in range(128):
            dx = x - 64
            dy = y - 64
            dist = math.sqrt(dx*dx + dy*dy)
            if dist <= 58:
                img.putpixel((x, y), (r, g, b, 255))
            elif dist <= 61:
                # Border
                img.putpixel((x, y), hex_to_rgba(border_color))
            elif dist <= 62:
                # Anti-alias edge
                img.putpixel((x, y), hex_to_rgba(border_color, 120))
    return img

def save_icon(img, mod_name, ns_name):
    # Save to src/main/resources/icon.png and assets/<ns>/icon.png
    paths = [
        f"{MODS_DIR}/{mod_name}/src/main/resources/icon.png",
        f"{MODS_DIR}/{mod_name}/src/main/resources/assets/{ns_name}/icon.png"
    ]
    for p in paths:
        os.makedirs(os.path.dirname(p), exist_ok=True)
        img.save(p)
        print(f"Saved: {p}")

# 1. HEARTHWIND CLIENT (Lantern & Hearthwind Breeze)
def gen_client_icon():
    img = create_base_canvas((20, 24, 40), (10, 12, 25), "#D4AF37")
    draw = ImageDraw.Draw(img)
    
    # Wind swirl behind lantern
    for r in range(25, 45):
        draw.arc([64 - r, 64 - r, 64 + r, 64 + r], start=160, end=340, fill=(180, 230, 255, 60), width=2)

    # Glowing aura
    for radius in range(26, 10, -2):
        alpha = int(255 * (1.0 - radius / 28.0) * 0.4)
        draw.ellipse([64 - radius, 66 - radius, 64 + radius, 66 + radius], fill=(255, 200, 50, alpha))

    # Hanging Chain & Top Cap
    draw.line([(64, 18), (64, 38)], fill=hex_to_rgba("#A07840"), width=3)
    draw.polygon([(48, 48), (80, 48), (64, 36)], fill=hex_to_rgba("#4E342E"), outline=hex_to_rgba("#D4AF37"), width=2)

    # Lantern Body (Glass & Metal Frame)
    draw.rectangle([48, 48, 80, 86], fill=hex_to_rgba("#FFA726", 180), outline=hex_to_rgba("#3E2723"), width=3)
    draw.rectangle([54, 52, 74, 82], fill=hex_to_rgba("#FFE082", 220))

    # Internal Hearth Flame
    draw.polygon([(64, 56), (70, 72), (64, 78), (58, 72)], fill=hex_to_rgba("#FF6D00"))
    draw.polygon([(64, 62), (68, 72), (64, 76), (60, 72)], fill=hex_to_rgba("#FFFDE7"))

    # Metal ribs & Bottom base
    draw.line([(64, 48), (64, 86)], fill=hex_to_rgba("#4E342E"), width=2)
    draw.polygon([(46, 86), (82, 86), (76, 94), (52, 94)], fill=hex_to_rgba("#4E342E"), outline=hex_to_rgba("#D4AF37"), width=2)

    # Sparkle Stars
    for (sx, sy) in [(34, 36), (96, 42), (92, 90)]:
        draw.line([(sx-4, sy), (sx+4, sy)], fill=hex_to_rgba("#FFF9C4"), width=2)
        draw.line([(sx, sy-4), (sx, sy+4)], fill=hex_to_rgba("#FFF9C4"), width=2)

    save_icon(img, "hearthwind-client", "hearthwind")

# 2. HEARTHWIND FLORA (Lush Harvest & Vineyard Wreath)
def gen_flora_icon():
    img = create_base_canvas((24, 56, 32), (12, 30, 18), "#81C784")
    draw = ImageDraw.Draw(img)

    # Braided Vine Wreath
    for angle in range(0, 360, 15):
        rad = math.radians(angle)
        cx = 64 + int(36 * math.cos(rad))
        cy = 64 + int(36 * math.sin(rad))
        col = "#2E7D32" if angle % 30 == 0 else "#388E3C"
        draw.ellipse([cx-7, cy-7, cx+7, cy+7], fill=hex_to_rgba(col))

    # Grapes cluster (center-left)
    grape_spots = [
        (48, 54), (56, 54), (64, 54),
        (52, 62), (60, 62), (68, 62),
        (56, 70), (64, 70), (60, 78)
    ]
    for (gx, gy) in grape_spots:
        draw.ellipse([gx-5, gy-5, gx+5, gy+5], fill=hex_to_rgba("#7B1FA2"), outline=hex_to_rgba("#4A148C"), width=1)
        draw.ellipse([gx-3, gy-3, gx-1, gy-1], fill=hex_to_rgba("#E1BEE7"))

    # Golden Wheat Stalk (center-right)
    draw.line([(82, 85), (74, 42)], fill=hex_to_rgba("#FBC02D"), width=3)
    for wy in range(46, 75, 7):
        draw.polygon([(75, wy), (84, wy-5), (78, wy+2)], fill=hex_to_rgba("#FFEE58"))
        draw.polygon([(73, wy+3), (65, wy-2), (71, wy+5)], fill=hex_to_rgba("#FDD835"))

    # Red Strawberries
    for (sbx, sby) in [(40, 82), (86, 78)]:
        draw.polygon([(sbx, sby-4), (sbx+6, sby+4), (sbx, sby+8), (sbx-6, sby+4)], fill=hex_to_rgba("#E53935"))
        draw.ellipse([sbx-2, sby-6, sbx+2, sby-3], fill=hex_to_rgba("#4CAF50"))

    # Edelweiss Flower (top)
    draw.ellipse([64-4, 30-4, 64+4, 30+4], fill=hex_to_rgba("#FFD54F"))
    for p in range(0, 360, 45):
        pr = math.radians(p)
        px = 64 + int(8 * math.cos(pr))
        py = 30 + int(8 * math.sin(pr))
        draw.ellipse([px-3, py-3, px+3, py+3], fill=hex_to_rgba("#FAFAFA"))

    save_icon(img, "hearthwind-flora", "hearthwind_flora")

# 3. HEARTHWIND SURVIVAL (Roaring Campfire & Leather Flask)
def gen_survival_icon():
    img = create_base_canvas((40, 20, 20), (20, 10, 10), "#FF7043")
    draw = ImageDraw.Draw(img)

    # Campfire Stone Ring
    for angle in range(0, 360, 30):
        rad = math.radians(angle)
        sx = 64 + int(32 * math.cos(rad))
        sy = 82 + int(14 * math.sin(rad))
        draw.ellipse([sx-6, sy-4, sx+6, sy+4], fill=hex_to_rgba("#616161"), outline=hex_to_rgba("#37474F"), width=1)

    # Wood logs
    draw.line([(42, 84), (86, 76)], fill=hex_to_rgba("#4E342E"), width=6)
    draw.line([(42, 76), (86, 84)], fill=hex_to_rgba("#3E2723"), width=6)

    # Fire Flames (layered embers)
    draw.polygon([(64, 30), (84, 76), (44, 76)], fill=hex_to_rgba("#D84315"))
    draw.polygon([(64, 40), (78, 74), (50, 74)], fill=hex_to_rgba("#FF8F00"))
    draw.polygon([(64, 50), (72, 74), (56, 74)], fill=hex_to_rgba("#FFEE58"))
    draw.polygon([(64, 58), (68, 72), (60, 72)], fill=hex_to_rgba("#FFFFFF"))

    # Floating Embers
    for (ex, ey) in [(50, 34), (76, 28), (60, 22), (72, 42)]:
        draw.ellipse([ex-2, ey-2, ex+2, ey+2], fill=hex_to_rgba("#FFD54F"))

    save_icon(img, "hearthwind-survival", "hearthwind_survival")

# 4. HEARTHWIND SKILLS (Ancient Grimoire & Crossed Blades)
def gen_skills_icon():
    img = create_base_canvas((24, 32, 64), (12, 16, 36), "#42A5F5")
    draw = ImageDraw.Draw(img)

    # Crossed Steel Sword & Diamond Pickaxe behind Tome
    draw.line([(32, 32), (96, 96)], fill=hex_to_rgba("#B0BEC5"), width=4) # Blade
    draw.line([(96, 32), (32, 96)], fill=hex_to_rgba("#00E5FF"), width=4) # Pickaxe handle/head

    # Open Tome / Book
    draw.polygon([(26, 80), (64, 88), (64, 48), (26, 40)], fill=hex_to_rgba("#FFF8E1")) # Left page
    draw.polygon([(102, 80), (64, 88), (64, 48), (102, 40)], fill=hex_to_rgba("#FFF3E0")) # Right page
    draw.polygon([(24, 84), (64, 92), (104, 84), (104, 42), (64, 48), (24, 42)], outline=hex_to_rgba("#5D4037"), width=3) # Leather cover

    # Page Lines / Runes
    for ly in [52, 60, 68, 76]:
        draw.line([(34, ly), (56, ly+3)], fill=hex_to_rgba("#8D6E63"), width=2)
        draw.line([(72, ly+3), (94, ly)], fill=hex_to_rgba("#8D6E63"), width=2)

    # Glowing Celestial Skill Star (Above tome)
    cx, cy = 64, 28
    draw.line([(cx-8, cy), (cx+8, cy)], fill=hex_to_rgba("#FFD54F"), width=3)
    draw.line([(cx, cy-8), (cx, cy+8)], fill=hex_to_rgba("#FFD54F"), width=3)
    draw.ellipse([cx-3, cy-3, cx+3, cy+3], fill=hex_to_rgba("#FFFFFF"))

    save_icon(img, "hearthwind-skills", "hearthwind_skills")

# 5. HEARTHWIND JOBS (Master Anvil & Golden Smithing Hammer)
def gen_jobs_icon():
    img = create_base_canvas((48, 36, 24), (24, 18, 12), "#FFB74D")
    draw = ImageDraw.Draw(img)

    # Heavy Anvil
    draw.polygon([(36, 56), (92, 56), (88, 68), (76, 72), (76, 82), (90, 88), (38, 88), (52, 82), (52, 72), (40, 68)],
                 fill=hex_to_rgba("#37474F"), outline=hex_to_rgba("#78909C"), width=2)
    # Anvil Horn
    draw.polygon([(36, 56), (22, 60), (36, 66)], fill=hex_to_rgba("#455A64"))

    # Smithing Hammer (Diagonal strike)
    draw.line([(52, 70), (88, 30)], fill=hex_to_rgba("#8D6E63"), width=4) # Wooden Handle
    # Hammer Head
    draw.polygon([(82, 24), (96, 38), (90, 44), (76, 30)], fill=hex_to_rgba("#FFD54F"), outline=hex_to_rgba("#FFA000"), width=2)

    # Impact Sparks
    for (sx, sy) in [(56, 50), (62, 44), (68, 48), (54, 42)]:
        draw.line([(sx-2, sy-2), (sx+2, sy+2)], fill=hex_to_rgba("#FFF59D"), width=2)

    save_icon(img, "hearthwind-jobs", "hearthwind_jobs")

# 6. HEARTHWIND PRIMITIVE (Knapped Flint Axe & Stone Sparks)
def gen_primitive_icon():
    img = create_base_canvas((44, 40, 36), (22, 20, 18), "#BDBDBD")
    draw = ImageDraw.Draw(img)

    # Primitive Rock Mound
    draw.ellipse([28, 70, 100, 100], fill=hex_to_rgba("#546E7A"), outline=hex_to_rgba("#37474F"), width=2)

    # Raw Copper & Flint chunks
    draw.ellipse([78, 74, 92, 88], fill=hex_to_rgba("#D84315"), outline=hex_to_rgba("#BF360C"), width=1)
    draw.ellipse([36, 76, 48, 88], fill=hex_to_rgba("#212121"), outline=hex_to_rgba("#000000"), width=1)

    # Knapped Flint Hatchet
    draw.line([(40, 85), (82, 38)], fill=hex_to_rgba("#6D4C41"), width=5) # Bound branch
    draw.line([(44, 80), (80, 42)], fill=hex_to_rgba("#8D6E63"), width=3)
    # Flint Blade tied with vine
    draw.polygon([(68, 34), (94, 28), (86, 52), (64, 46)], fill=hex_to_rgba("#263238"), outline=hex_to_rgba("#90A4AE"), width=2)
    # Leather/vine binding
    draw.line([(72, 44), (78, 38)], fill=hex_to_rgba("#D7CCC8"), width=2)
    draw.line([(70, 40), (80, 42)], fill=hex_to_rgba("#D7CCC8"), width=2)

    # Knapping sparks
    for (kx, ky) in [(60, 30), (54, 38), (66, 22)]:
        draw.ellipse([kx-2, ky-2, kx+2, ky+2], fill=hex_to_rgba("#FFD54F"))

    save_icon(img, "hearthwind-primitive", "hearthwind_primitive")

# 7. HEARTHWIND WORLD (Four Seasons Wheel)
def gen_world_icon():
    img = create_base_canvas((30, 40, 50), (15, 20, 25), "#E0E0E0")
    draw = ImageDraw.Draw(img)

    # 4 Quadrants
    # Top-Left: Spring (Pink Blossom)
    draw.pieslice([20, 20, 108, 108], start=180, end=270, fill=hex_to_rgba("#F8BBD0"))
    draw.ellipse([46-5, 46-5, 46+5, 46+5], fill=hex_to_rgba("#E91E63"))
    draw.ellipse([46-2, 46-2, 46+2, 46+2], fill=hex_to_rgba("#FFF9C4"))

    # Top-Right: Summer (Golden Sun)
    draw.pieslice([20, 20, 108, 108], start=270, end=360, fill=hex_to_rgba("#FFF59D"))
    draw.ellipse([82-6, 46-6, 82+6, 46+6], fill=hex_to_rgba("#F57F17"))

    # Bottom-Right: Autumn (Amber Leaf)
    draw.pieslice([20, 20, 108, 108], start=0, end=90, fill=hex_to_rgba("#FFE082"))
    draw.polygon([(82, 74), (88, 82), (84, 88), (76, 82)], fill=hex_to_rgba("#E65100"))

    # Bottom-Left: Winter (Ice Snowflake)
    draw.pieslice([20, 20, 108, 108], start=90, end=180, fill=hex_to_rgba("#B3E5FC"))
    draw.line([(42, 82), (50, 82)], fill=hex_to_rgba("#0288D1"), width=2)
    draw.line([(46, 78), (46, 86)], fill=hex_to_rgba("#0288D1"), width=2)

    # Outer and Cross Borders
    draw.ellipse([20, 20, 108, 108], outline=hex_to_rgba("#37474F"), width=3)
    draw.line([(20, 64), (108, 64)], fill=hex_to_rgba("#37474F"), width=2)
    draw.line([(64, 20), (64, 108)], fill=hex_to_rgba("#37474F"), width=2)
    draw.ellipse([64-8, 64-8, 64+8, 64+8], fill=hex_to_rgba("#FFFFFF"), outline=hex_to_rgba("#37474F"), width=2)

    save_icon(img, "hearthwind-world", "hearthwind_world")

if __name__ == "__main__":
    print("Generating high-resolution icons for all 7 Hearthwind mods...")
    gen_client_icon()
    gen_flora_icon()
    gen_survival_icon()
    gen_skills_icon()
    gen_jobs_icon()
    gen_primitive_icon()
    gen_world_icon()
    print("Done! All 7 mod icons generated successfully.")
