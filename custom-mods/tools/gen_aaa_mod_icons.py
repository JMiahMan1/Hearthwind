#!/usr/bin/env python3
"""
AAA-Quality Mod Icon Generator for Hearthwind (26.2).
Renders at 256x256 with smooth anti-aliased geometry, multi-layer depth,
rim lighting, bevels, and vibrant colors, then Lanczos-resamples to 128x128.
"""

import os
import math
from PIL import Image, ImageDraw, ImageFilter

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
MODS_DIR = os.path.abspath(os.path.join(SCRIPT_DIR, ".."))
DESKTOP_ICONS = os.path.expanduser("~/Desktop/Hearthwind_Flora_Assets/Module_Icons")

S = 256  # Supersampled resolution

def hex_to_rgba(h, alpha=255):
    h = h.lstrip('#')
    return tuple(int(h[i:i+2], 16) for i in (0, 2, 4)) + (alpha,)

def draw_squircle_badge(bg_top, bg_bot, border_col, inner_glow_col):
    """Creates a modern rounded-squircle badge with bevels and depth."""
    img = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    margin = 16
    r = 44  # Corner radius

    # Outer drop shadow
    shadow = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    sdraw = ImageDraw.Draw(shadow)
    sdraw.rounded_rectangle([margin+2, margin+6, S-margin+2, S-margin+6], radius=r, fill=(0, 0, 0, 100))
    shadow = shadow.filter(ImageFilter.GaussianBlur(8))
    img = Image.alpha_composite(img, shadow)
    draw = ImageDraw.Draw(img)

    # Base Beveled Border
    draw.rounded_rectangle([margin, margin, S-margin, S-margin], radius=r, fill=hex_to_rgba(border_col))

    # Inner gradient body
    body = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    bdraw = ImageDraw.Draw(body)
    bw = 6
    bdraw.rounded_rectangle([margin+bw, margin+bw, S-margin-bw, S-margin-bw], radius=r-bw, fill=(255, 255, 255, 255))

    grad = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    for y in range(S):
        t = y / float(S)
        cr = int(bg_top[0] * (1-t) + bg_bot[0] * t)
        cg = int(bg_top[1] * (1-t) + bg_bot[1] * t)
        cb = int(bg_top[2] * (1-t) + bg_bot[2] * t)
        for x in range(S):
            grad.putpixel((x, y), (cr, cg, cb, 255))

    body_layer = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    body_layer.paste(grad, (0, 0), body)
    img = Image.alpha_composite(img, body_layer)
    draw = ImageDraw.Draw(img)

    # Top-edge highlight rim
    draw.arc([margin+bw, margin+bw, S-margin-bw, margin+bw+r*2], start=180, end=360, fill=hex_to_rgba("#FFFFFF", 50), width=2)

    return img

def finalize_and_save(img256, mod_name, ns_name, desktop_filename):
    """Downsamples with high-quality Lanczos filter to 128x128 and saves."""
    img128 = img256.resize((128, 128), Image.Resampling.LANCZOS)
    
    # Save in custom-mods
    p1 = f"{MODS_DIR}/{mod_name}/src/main/resources/icon.png"
    p2 = f"{MODS_DIR}/{mod_name}/src/main/resources/assets/{ns_name}/icon.png"
    os.makedirs(os.path.dirname(p1), exist_ok=True)
    os.makedirs(os.path.dirname(p2), exist_ok=True)
    img128.save(p1)
    img128.save(p2)

    # Save to Desktop Module_Icons
    if os.path.exists(DESKTOP_ICONS):
        p3 = os.path.join(DESKTOP_ICONS, desktop_filename)
        img128.save(p3)

    print(f"Saved AAA Icon: {mod_name} -> {desktop_filename}")

# 1. CLIENT (Golden Hearthwind Lantern & Wind Swirls)
def create_client_icon():
    img = draw_squircle_badge((22, 28, 48), (10, 14, 26), "#D4AF37", "#64B5F6")
    draw = ImageDraw.Draw(img)

    # Wind Wisps (Soft glowing curves)
    for off in [-4, 0, 4]:
        draw.arc([30, 70+off, 226, 190+off], start=150, end=350, fill=hex_to_rgba("#81D4FA", 80), width=4)

    # Warm Firelight Glow
    glow = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    gdraw = ImageDraw.Draw(glow)
    for radius in range(70, 15, -5):
        alpha = int(90 * (1.0 - radius / 70.0))
        gdraw.ellipse([128-radius, 140-radius, 128+radius, 140+radius], fill=(255, 180, 50, alpha))
    img = Image.alpha_composite(img, glow)
    draw = ImageDraw.Draw(img)

    # Lantern Chain & Ring
    draw.line([(128, 38), (128, 70)], fill=hex_to_rgba("#C5A059"), width=6)
    draw.ellipse([118, 56, 138, 76], outline=hex_to_rgba("#D4AF37"), width=4)

    # Roof / Cap
    draw.polygon([(86, 96), (170, 96), (128, 68)], fill=hex_to_rgba("#3E2723"), outline=hex_to_rgba("#D4AF37"), width=4)

    # Glass Chamber & Frame
    draw.rounded_rectangle([90, 96, 166, 180], radius=8, fill=hex_to_rgba("#FFA726", 190), outline=hex_to_rgba("#2E1C14"), width=5)
    draw.rectangle([102, 106, 154, 170], fill=hex_to_rgba("#FFF59D", 230))

    # Inner Hearth Flame
    draw.polygon([(128, 116), (142, 150), (128, 164), (114, 150)], fill=hex_to_rgba("#FF6D00"))
    draw.polygon([(128, 128), (136, 150), (128, 160), (120, 150)], fill=hex_to_rgba("#FFFFFF"))

    # Metal Ribs & Bottom Base
    draw.line([(128, 96), (128, 180)], fill=hex_to_rgba("#3E2723"), width=4)
    draw.polygon([(82, 180), (174, 180), (160, 196), (96, 196)], fill=hex_to_rgba("#3E2723"), outline=hex_to_rgba("#D4AF37"), width=4)

    # Twinkling Stars
    for (sx, sy, sz) in [(64, 68, 6), (196, 80, 8), (190, 180, 5), (60, 170, 5)]:
        draw.line([(sx-sz, sy), (sx+sz, sy)], fill=hex_to_rgba("#FFF9C4"), width=2)
        draw.line([(sx, sy-sz), (sx, sy+sz)], fill=hex_to_rgba("#FFF9C4"), width=2)
        draw.ellipse([sx-2, sy-2, sx+2, sy+2], fill=hex_to_rgba("#FFFFFF"))

    finalize_and_save(img, "hearthwind-client", "hearthwind", "hearthwind-client.png")

# 2. FLORA (Harvest Wreath: Grapes, Wheat, Strawberry, Edelweiss)
def create_flora_icon():
    img = draw_squircle_badge((20, 48, 28), (10, 26, 16), "#81C784", "#A5D6A7")
    draw = ImageDraw.Draw(img)

    # Vine Ring / Wreath
    for angle in range(0, 360, 12):
        rad = math.radians(angle)
        cx = 128 + int(76 * math.cos(rad))
        cy = 128 + int(76 * math.sin(rad))
        c = "#2E7D32" if angle % 24 == 0 else "#388E3C"
        draw.ellipse([cx-14, cy-14, cx+14, cy+14], fill=hex_to_rgba(c), outline=hex_to_rgba("#1B5E20"), width=2)

    # Ripe Violet Grapes Cluster (Center Left)
    grape_pts = [
        (96, 108), (112, 108), (128, 108),
        (104, 124), (120, 124), (136, 124),
        (112, 140), (128, 140), (120, 156)
    ]
    for (gx, gy) in grape_pts:
        draw.ellipse([gx-10, gy-10, gx+10, gy+10], fill=hex_to_rgba("#7B1FA2"), outline=hex_to_rgba("#4A148C"), width=2)
        draw.ellipse([gx-6, gy-6, gx-2, gy-2], fill=hex_to_rgba("#E1BEE7"))

    # Golden Wheat Stalk (Center Right)
    draw.line([(164, 170), (148, 86)], fill=hex_to_rgba("#FBC02D"), width=5)
    for wy in range(92, 155, 14):
        draw.polygon([(150, wy), (168, wy-10), (156, wy+4)], fill=hex_to_rgba("#FFEE58"), outline=hex_to_rgba("#F57F17"), width=1)
        draw.polygon([(146, wy+6), (130, wy-4), (142, wy+10)], fill=hex_to_rgba("#FDD835"), outline=hex_to_rgba("#F57F17"), width=1)

    # Strawberry (Bottom Center)
    sbx, sby = 128, 172
    draw.polygon([(sbx, sby-8), (sbx+14, sby+8), (sbx, sby+18), (sbx-14, sby+8)], fill=hex_to_rgba("#E53935"), outline=hex_to_rgba("#B71C1C"), width=2)
    draw.ellipse([sbx-6, sby-12, sbx+6, sby-6], fill=hex_to_rgba("#4CAF50"))

    # Alpine Edelweiss Flower (Top Center)
    draw.ellipse([128-8, 64-8, 128+8, 64+8], fill=hex_to_rgba("#FFD54F"))
    for p in range(0, 360, 45):
        pr = math.radians(p)
        px = 128 + int(16 * math.cos(pr))
        py = 64 + int(16 * math.sin(pr))
        draw.ellipse([px-6, py-6, px+6, py+6], fill=hex_to_rgba("#FAFAFA"), outline=hex_to_rgba("#E0E0E0"), width=1)

    finalize_and_save(img, "hearthwind-flora", "hearthwind_flora", "hearthwind-flora.png")

# 3. SURVIVAL (Roaring Campfire & Leather Traveler Flask)
def create_survival_icon():
    img = draw_squircle_badge((48, 22, 18), (24, 10, 8), "#FF7043", "#FFAB91")
    draw = ImageDraw.Draw(img)

    # Campfire Stone Ring
    for angle in range(0, 360, 30):
        rad = math.radians(angle)
        sx = 128 + int(64 * math.cos(rad))
        sy = 168 + int(28 * math.sin(rad))
        draw.ellipse([sx-12, sy-8, sx+12, sy+8], fill=hex_to_rgba("#616161"), outline=hex_to_rgba("#37474F"), width=2)

    # Burning Timber Logs
    draw.line([(84, 172), (172, 156)], fill=hex_to_rgba("#4E342E"), width=12)
    draw.line([(84, 156), (172, 172)], fill=hex_to_rgba("#3E2723"), width=12)

    # Multi-Layer Flame Core
    draw.polygon([(128, 68), (168, 156), (88, 156)], fill=hex_to_rgba("#D84315"))
    draw.polygon([(128, 88), (156, 152), (100, 152)], fill=hex_to_rgba("#FF8F00"))
    draw.polygon([(128, 108), (144, 152), (112, 152)], fill=hex_to_rgba("#FFEE58"))
    draw.polygon([(128, 124), (136, 148), (120, 148)], fill=hex_to_rgba("#FFFFFF"))

    # Floating Embers
    for (ex, ey, er) in [(100, 76, 4), (152, 64, 5), (120, 52, 3), (144, 92, 4), (110, 100, 3)]:
        draw.ellipse([ex-er, ey-er, ex+er, ey+er], fill=hex_to_rgba("#FFD54F"))

    finalize_and_save(img, "hearthwind-survival", "hearthwind_survival", "hearthwind-survival.png")

# 4. SKILLS (Ancient Grimoire, Crossed Steel Sword & Celestial Star)
def create_skills_icon():
    img = draw_squircle_badge((20, 28, 56), (10, 14, 30), "#42A5F5", "#90CAF9")
    draw = ImageDraw.Draw(img)

    # Crossed Broadsword & Pickaxe behind Tome
    # Broadsword (Diagonal Top-Left to Bottom-Right)
    draw.line([(64, 64), (192, 192)], fill=hex_to_rgba("#CFD8DC"), width=8)
    draw.line([(64, 64), (192, 192)], fill=hex_to_rgba("#ECEFF1"), width=4)
    # Pickaxe (Diagonal Top-Right to Bottom-Left)
    draw.line([(192, 64), (64, 192)], fill=hex_to_rgba("#00E5FF"), width=8)

    # Open Grimoire / Tome
    draw.polygon([(52, 160), (128, 176), (128, 96), (52, 80)], fill=hex_to_rgba("#FFF8E1")) # Left Page
    draw.polygon([(204, 160), (128, 176), (128, 96), (204, 80)], fill=hex_to_rgba("#FFF3E0")) # Right Page
    draw.polygon([(48, 168), (128, 184), (208, 168), (208, 84), (128, 96), (48, 84)], outline=hex_to_rgba("#5D4037"), width=5)

    # Runes & Text
    for ly in [104, 120, 136, 152]:
        draw.line([(68, ly), (112, ly+6)], fill=hex_to_rgba("#8D6E63"), width=3)
        draw.line([(144, ly+6), (188, ly)], fill=hex_to_rgba("#8D6E63"), width=3)

    # Glowing Celestial Skill Star
    cx, cy = 128, 56
    draw.line([(cx-16, cy), (cx+16, cy)], fill=hex_to_rgba("#FFD54F"), width=5)
    draw.line([(cx, cy-16), (cx, cy+16)], fill=hex_to_rgba("#FFD54F"), width=5)
    draw.line([(cx-11, cy-11), (cx+11, cy+11)], fill=hex_to_rgba("#FFF59D"), width=3)
    draw.line([(cx-11, cy+11), (cx+11, cy-11)], fill=hex_to_rgba("#FFF59D"), width=3)
    draw.ellipse([cx-5, cy-5, cx+5, cy+5], fill=hex_to_rgba("#FFFFFF"))

    finalize_and_save(img, "hearthwind-skills", "hearthwind_skills", "hearthwind-skills.png")

# 5. JOBS (Master Blacksmith Anvil & Striking Gold Hammer)
def create_jobs_icon():
    img = draw_squircle_badge((44, 32, 24), (22, 16, 12), "#FFB74D", "#FFE082")
    draw = ImageDraw.Draw(img)

    # Master Anvil
    draw.polygon([(72, 112), (184, 112), (176, 136), (152, 144), (152, 164), (180, 176), (76, 176), (104, 164), (104, 144), (80, 136)],
                 fill=hex_to_rgba("#37474F"), outline=hex_to_rgba("#78909C"), width=4)
    # Anvil Horn
    draw.polygon([(72, 112), (44, 120), (72, 132)], fill=hex_to_rgba("#455A64"), outline=hex_to_rgba("#78909C"), width=2)
    # Anvil Face Highlight
    draw.line([(72, 114), (184, 114)], fill=hex_to_rgba("#B0BEC5"), width=3)

    # Smithing Hammer
    draw.line([(104, 140), (176, 60)], fill=hex_to_rgba("#8D6E63"), width=8) # Wooden shaft
    # Golden Hammer Head
    draw.polygon([(164, 48), (192, 76), (180, 88), (152, 60)], fill=hex_to_rgba("#FFD54F"), outline=hex_to_rgba("#FFA000"), width=3)

    # Impact Sparks
    for (sx, sy, sz) in [(112, 100, 5), (124, 88, 6), (136, 96, 4), (108, 84, 4), (144, 80, 5)]:
        draw.line([(sx-sz, sy-sz), (sx+sz, sy+sz)], fill=hex_to_rgba("#FFF59D"), width=3)
        draw.line([(sx+sz, sy-sz), (sx-sz, sy+sz)], fill=hex_to_rgba("#FFF59D"), width=3)

    finalize_and_save(img, "hearthwind-jobs", "hearthwind_jobs", "hearthwind-jobs.png")

# 6. PRIMITIVE (Knapped Flint Axe & Granite Stone)
def create_primitive_icon():
    img = draw_squircle_badge((40, 36, 32), (20, 18, 16), "#BDBDBD", "#EEEEEE")
    draw = ImageDraw.Draw(img)

    # Stone Mound
    draw.ellipse([56, 140, 200, 200], fill=hex_to_rgba("#546E7A"), outline=hex_to_rgba("#37474F"), width=4)

    # Raw Copper Chunk & Flint Nugget
    draw.ellipse([156, 148, 184, 176], fill=hex_to_rgba("#D84315"), outline=hex_to_rgba("#BF360C"), width=2)
    draw.ellipse([72, 152, 96, 176], fill=hex_to_rgba("#212121"), outline=hex_to_rgba("#000000"), width=2)

    # Knapped Flint Hatchet
    draw.line([(80, 170), (164, 76)], fill=hex_to_rgba("#6D4C41"), width=10) # Branch
    draw.line([(88, 160), (160, 84)], fill=hex_to_rgba("#8D6E63"), width=6)
    # Flint Blade
    draw.polygon([(136, 68), (188, 56), (172, 104), (128, 92)], fill=hex_to_rgba("#263238"), outline=hex_to_rgba("#90A4AE"), width=3)
    # Leather wrap binding
    draw.line([(144, 88), (156, 76)], fill=hex_to_rgba("#D7CCC8"), width=4)
    draw.line([(140, 80), (160, 84)], fill=hex_to_rgba("#D7CCC8"), width=4)

    # Knapping sparks
    for (kx, ky) in [(120, 60), (108, 76), (132, 44)]:
        draw.ellipse([kx-4, ky-4, kx+4, ky+4], fill=hex_to_rgba("#FFD54F"))

    finalize_and_save(img, "hearthwind-primitive", "hearthwind_primitive", "hearthwind-primitive.png")

# 7. WORLD (4-Season Celestial Wheel)
def create_world_icon():
    img = draw_squircle_badge((28, 36, 46), (14, 18, 24), "#E0E0E0", "#FFFFFF")
    draw = ImageDraw.Draw(img)

    # Outer Season Compass Circle
    bbox = [40, 40, 216, 216]
    
    # Spring (Top-Left: Pink Blossom)
    draw.pieslice(bbox, start=180, end=270, fill=hex_to_rgba("#F8BBD0"))
    draw.ellipse([92-10, 92-10, 92+10, 92+10], fill=hex_to_rgba("#E91E63"))
    draw.ellipse([92-4, 92-4, 92+4, 92+4], fill=hex_to_rgba("#FFF9C4"))

    # Summer (Top-Right: Amber Sun)
    draw.pieslice(bbox, start=270, end=360, fill=hex_to_rgba("#FFF59D"))
    draw.ellipse([164-12, 92-12, 164+12, 92+12], fill=hex_to_rgba("#F57F17"))
    for a in range(0, 360, 45):
        ar = math.radians(a)
        draw.line([(164+int(16*math.cos(ar)), 92+int(16*math.sin(ar))),
                   (164+int(22*math.cos(ar)), 92+int(22*math.sin(ar)))], fill=hex_to_rgba("#F57F17"), width=3)

    # Autumn (Bottom-Right: Golden Maple Leaf)
    draw.pieslice(bbox, start=0, end=90, fill=hex_to_rgba("#FFE082"))
    draw.polygon([(164, 148), (176, 164), (168, 176), (152, 164)], fill=hex_to_rgba("#E65100"))

    # Winter (Bottom-Left: Crystalline Snowflake)
    draw.pieslice(bbox, start=90, end=180, fill=hex_to_rgba("#B3E5FC"))
    draw.line([(84, 164), (100, 164)], fill=hex_to_rgba("#0288D1"), width=4)
    draw.line([(92, 156), (92, 172)], fill=hex_to_rgba("#0288D1"), width=4)

    # Cross & Outer Divider Frames
    draw.ellipse(bbox, outline=hex_to_rgba("#37474F"), width=5)
    draw.line([(40, 128), (216, 128)], fill=hex_to_rgba("#37474F"), width=4)
    draw.line([(128, 40), (128, 216)], fill=hex_to_rgba("#37474F"), width=4)
    draw.ellipse([128-16, 128-16, 128+16, 128+16], fill=hex_to_rgba("#FFFFFF"), outline=hex_to_rgba("#37474F"), width=4)

    finalize_and_save(img, "hearthwind-world", "hearthwind_world", "hearthwind-world.png")

if __name__ == "__main__":
    print("Generating AAA Mod Icons...")
    create_client_icon()
    create_flora_icon()
    create_survival_icon()
    create_skills_icon()
    create_jobs_icon()
    create_primitive_icon()
    create_world_icon()
    print("Complete!")
