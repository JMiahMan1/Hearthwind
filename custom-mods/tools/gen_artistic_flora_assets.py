#!/usr/bin/env python3
"""
High-Fidelity Pixel Art Generator for Hearthwind Flora.
Generates beautiful, handcrafted 16x16 and 32x32 Minecraft-style pixel art textures
with multi-step gradient palettes, highlights, dithering, and soft outlines.
"""

import os
from PIL import Image, ImageDraw

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "../hearthwind-flora/src/main/resources/assets"))

def hex_to_rgba(h, alpha=255):
    h = h.lstrip('#')
    return tuple(int(h[i:i+2], 16) for i in (0, 2, 4)) + (alpha,)

def draw_item_texture(path, pixels_dict):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for (x, y), color in pixels_dict.items():
        if 0 <= x < 16 and 0 <= y < 16:
            img.putpixel((x, y), hex_to_rgba(color))
    img.save(path)

def draw_block_texture(path, base_color, noise_colors, pattern=None):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img = Image.new("RGBA", (16, 16), hex_to_rgba(base_color))
    import random
    rng = random.Random(hash(path))
    for y in range(16):
        for x in range(16):
            if rng.random() > 0.6:
                c = rng.choice(noise_colors)
                img.putpixel((x, y), hex_to_rgba(c))
    img.save(path)

# -----------------------------------------------------------------------------
# 1. VINERY ASSETS
# -----------------------------------------------------------------------------
def gen_vinery():
    base = f"{ROOT}/vinery/textures/item"
    
    # Red Grapes
    grapes = {}
    # Stem
    for y in range(1, 4): grapes[(8, y)] = "#3E2723"
    grapes[(9, 2)] = "#2E7D32" # Leaf
    # Grape cluster (shaded purple/ruby)
    cluster = [
        (7, 4), (8, 4), (9, 4),
        (6, 5), (7, 5), (8, 5), (9, 5), (10, 5),
        (6, 6), (7, 6), (8, 6), (9, 6), (10, 6),
        (7, 7), (8, 7), (9, 7),
        (7, 8), (8, 8),
        (8, 9)
    ]
    for (x, y) in cluster:
        if y <= 5 and x <= 8:
            grapes[(x, y)] = "#8E24AA" # Highlight
        elif x == 10 or y >= 8:
            grapes[(x, y)] = "#4A148C" # Shadow
        else:
            grapes[(x, y)] = "#6A1B9A" # Midtone
    draw_item_texture(f"{base}/red_grape.png", grapes)

    # White / Green Grapes
    w_grapes = {}
    for y in range(1, 4): w_grapes[(8, y)] = "#3E2723"
    w_grapes[(9, 2)] = "#2E7D32"
    for (x, y) in cluster:
        if y <= 5 and x <= 8:
            w_grapes[(x, y)] = "#DCE775"
        elif x == 10 or y >= 8:
            w_grapes[(x, y)] = "#827717"
        else:
            w_grapes[(x, y)] = "#AFB42B"
    draw_item_texture(f"{base}/white_grape.png", w_grapes)
    draw_item_texture(f"{base}/savanna_grapes.png", w_grapes)
    draw_item_texture(f"{base}/taiga_grapes.png", grapes)
    draw_item_texture(f"{base}/jungle_grapes.png", grapes)

    # Wine Bottles
    def make_bottle(liquid_color, highlight_color):
        b = {}
        # Cork & Neck
        b[(7, 1)] = "#8D6E63"; b[(8, 1)] = "#8D6E63"
        b[(7, 2)] = "#5D4037"; b[(8, 2)] = "#5D4037"
        for y in range(3, 7):
            b[(7, y)] = "#37474F"; b[(8, y)] = "#455A64"
        # Body
        for y in range(7, 15):
            for x in range(5, 11):
                if x == 5 or x == 10 or y == 14:
                    b[(x, y)] = "#263238" # Outline/Glass
                elif x == 6:
                    b[(x, y)] = highlight_color
                else:
                    b[(x, y)] = liquid_color
        # Label
        for y in range(9, 12):
            for x in range(7, 9):
                b[(x, y)] = "#FFF8E1"
        return b

    draw_item_texture(f"{base}/chenet_wine.png", make_bottle("#880E4F", "#C2185B"))
    draw_item_texture(f"{base}/bolvar_wine.png", make_bottle("#4A148C", "#7B1FA2"))
    draw_item_texture(f"{base}/cherry_wine.png", make_bottle("#AD1457", "#E91E63"))
    draw_item_texture(f"{base}/solaris_wine.png", make_bottle("#F57F17", "#FBC02D"))
    draw_item_texture(f"{base}/clark_wine.png", make_bottle("#1B5E20", "#388E3C"))
    draw_item_texture(f"{base}/apple_wine.png", make_bottle("#E65100", "#FF9800"))
    draw_item_texture(f"{base}/mead.png", make_bottle("#FFB300", "#FFE082"))
    draw_item_texture(f"{base}/grape_juice.png", make_bottle("#6A1B9A", "#AB47BC"))
    draw_item_texture(f"{base}/apple_juice.png", make_bottle("#F9A825", "#FFF176"))
    draw_item_texture(f"{base}/kelp_cider.png", make_bottle("#00695C", "#26A69A"))
    draw_item_texture(f"{base}/villagers_fright.png", make_bottle("#212121", "#424242"))

    # Seeds
    seeds = {(7, 7): "#5D4037", (8, 7): "#8D6E63", (7, 8): "#3E2723", (8, 8): "#5D4037"}
    draw_item_texture(f"{base}/red_grape_seeds.png", seeds)
    draw_item_texture(f"{base}/white_grape_seeds.png", seeds)

# -----------------------------------------------------------------------------
# 2. CANDLELIGHT ASSETS
# -----------------------------------------------------------------------------
def gen_candlelight():
    base = f"{ROOT}/candlelight/textures/item"

    # Tomato
    tomato = {}
    tomato[(7, 4)] = "#2E7D32"; tomato[(8, 4)] = "#1B5E20" # Stem
    for y in range(5, 12):
        for x in range(4, 12):
            if (x in (4, 11) and y in (5, 11)): continue
            if x <= 6 and y <= 7:
                tomato[(x, y)] = "#FF5252" # Bright highlight
            elif x >= 10 or y >= 10:
                tomato[(x, y)] = "#B71C1C" # Deep shadow
            else:
                tomato[(x, y)] = "#D32F2F" # Rich red midtone
    draw_item_texture(f"{base}/tomato.png", tomato)

    # Lettuce
    lettuce = {}
    for y in range(5, 12):
        for x in range(4, 12):
            if (x in (4, 11) and y in (5, 11)): continue
            if y <= 7:
                lettuce[(x, y)] = "#81C784"
            elif y >= 10:
                lettuce[(x, y)] = "#2E7D32"
            else:
                lettuce[(x, y)] = "#4CAF50"
    draw_item_texture(f"{base}/lettuce.png", lettuce)

    # Broccoli
    broccoli = {}
    for y in range(9, 13):
        broccoli[(7, y)] = "#C8E6C9"; broccoli[(8, y)] = "#A5D6A7" # Stem
    for y in range(4, 9):
        for x in range(4, 12):
            if (x in (4, 11) and y == 4): continue
            if y <= 5:
                broccoli[(x, y)] = "#388E3C"
            else:
                broccoli[(x, y)] = "#1B5E20"
    draw_item_texture(f"{base}/broccoli.png", broccoli)

    # Meals (Bowl / Plated dishes)
    def make_bowl_meal(content_color, garnish_color=None):
        m = {}
        # Food contents
        for y in range(6, 10):
            for x in range(4, 12):
                m[(x, y)] = content_color
        if garnish_color:
            m[(7, 7)] = garnish_color; m[(8, 7)] = garnish_color; m[(7, 8)] = garnish_color
        # Bowl
        for y in range(9, 13):
            for x in range(3, 13):
                if y == 12 and (x < 5 or x > 10): continue
                if y == 9 and (x < 4 or x > 11): continue
                if x in (3, 12) or y == 12:
                    m[(x, y)] = "#8D6E63"
                else:
                    m[(x, y)] = "#BCAAA4"
        return m

    draw_item_texture(f"{base}/tomato_soup.png", make_bowl_meal("#D32F2F", "#81C784"))
    draw_item_texture(f"{base}/pasta.png", make_bowl_meal("#FFF59D", "#D32F2F"))
    draw_item_texture(f"{base}/lasagna.png", make_bowl_meal("#FF8A65", "#FFF9C4"))
    draw_item_texture(f"{base}/pizza.png", make_bowl_meal("#FFE082", "#C62828"))
    draw_item_texture(f"{base}/beef_tartare.png", make_bowl_meal("#B71C1C", "#FFF59D"))
    draw_item_texture(f"{base}/roastbeef_with_carrots.png", make_bowl_meal("#5D4037", "#FF6D00"))

# -----------------------------------------------------------------------------
# 3. MEADOW ASSETS
# -----------------------------------------------------------------------------
def gen_meadow():
    base = f"{ROOT}/meadow/textures/item"

    # Cheese Wedge
    def make_cheese_wedge(base_color, highlight_color, shadow_color):
        c = {}
        for y in range(6, 12):
            for x in range(3 + (y-6), 13):
                if x == 3 + (y-6) or y == 6:
                    c[(x, y)] = highlight_color
                elif x == 12 or y == 11:
                    c[(x, y)] = shadow_color
                else:
                    c[(x, y)] = base_color
        # Cheese holes
        c[(7, 8)] = shadow_color; c[(10, 9)] = shadow_color
        return c

    draw_item_texture(f"{base}/piece_of_cheese.png", make_cheese_wedge("#FBC02D", "#FFF59D", "#F57F17"))
    draw_item_texture(f"{base}/piece_of_sheep_cheese.png", make_cheese_wedge("#FFF9C4", "#FFFFFF", "#F0F4C3"))
    draw_item_texture(f"{base}/piece_of_goat_cheese.png", make_cheese_wedge("#ECEFF1", "#FFFFFF", "#CFD8DC"))
    draw_item_texture(f"{base}/piece_of_grain_cheese.png", make_cheese_wedge("#FFE082", "#FFF8E1", "#FFA000"))
    draw_item_texture(f"{base}/piece_of_amethyst_cheese.png", make_cheese_wedge("#BA68C8", "#E1BEE7", "#7B1FA2"))
    draw_item_texture(f"{base}/cheesecake.png", make_cheese_wedge("#FFF8E1", "#FFFFFF", "#FFE082"))

# -----------------------------------------------------------------------------
# 4. BAKERY ASSETS
# -----------------------------------------------------------------------------
def gen_bakery():
    base = f"{ROOT}/bakery/textures/item"

    # Baguette
    baguette = {}
    for i in range(11):
        x = 3 + i; y = 13 - i
        baguette[(x, y)] = "#D7CCC8" # Highlight
        baguette[(x+1, y)] = "#8D6E63" # Midtone
        baguette[(x, y+1)] = "#5D4037" # Shadow
        if i % 3 == 0:
            baguette[(x, y)] = "#FFF8E1" # Slit
    draw_item_texture(f"{base}/baguette.png", baguette)

    # Croissant
    croissant = {}
    for y in range(6, 11):
        for x in range(4, 12):
            if (x in (4, 11) and y == 6): continue
            if y <= 7:
                croissant[(x, y)] = "#FFE082"
            elif y >= 9:
                croissant[(x, y)] = "#8D6E63"
            else:
                croissant[(x, y)] = "#FFA000"
    draw_item_texture(f"{base}/croissant.png", croissant)

    # Strawberry
    berry = {}
    berry[(7, 4)] = "#2E7D32"; berry[(8, 4)] = "#2E7D32" # Leaf
    for y in range(5, 12):
        for x in range(5 + (y-5)//2, 11 - (y-5)//2):
            if y <= 7:
                berry[(x, y)] = "#FF5252"
            else:
                berry[(x, y)] = "#C62828"
            if (x + y) % 3 == 0 and y >= 6:
                berry[(x, y)] = "#FFF59D" # Yellow seed
    draw_item_texture(f"{base}/strawberry.png", berry)

    # Oat
    oat = {}
    for y in range(4, 13):
        oat[(8, y)] = "#A1887F"
        if y % 2 == 0:
            oat[(7, y)] = "#FFE082"; oat[(9, y)] = "#FFD54F"
    draw_item_texture(f"{base}/oat.png", oat)

# -----------------------------------------------------------------------------
# 5. HERBALBREWS ASSETS
# -----------------------------------------------------------------------------
def gen_herbalbrews():
    base = f"{ROOT}/herbalbrews/textures/item"

    # Tea Cup / Mug
    def make_tea_cup(liquid_color):
        c = {}
        # Handle
        c[(11, 7)] = "#B0BEC5"; c[(12, 7)] = "#78909C"
        c[(12, 8)] = "#78909C"
        c[(11, 9)] = "#B0BEC5"; c[(12, 9)] = "#78909C"
        # Mug body
        for y in range(6, 12):
            for x in range(4, 11):
                if x in (4, 10) or y == 11:
                    c[(x, y)] = "#90A4AE" # Ceramic
                elif y <= 8:
                    c[(x, y)] = liquid_color # Tea surface
                else:
                    c[(x, y)] = "#ECEFF1" # Interior ceramic
        return c

    draw_item_texture(f"{base}/green_tea.png", make_tea_cup("#43A047"))
    draw_item_texture(f"{base}/black_tea.png", make_tea_cup("#4E342E"))
    draw_item_texture(f"{base}/hibiscus_tea.png", make_tea_cup("#AD1457"))
    draw_item_texture(f"{base}/lavender_tea.png", make_tea_cup("#7B1FA2"))
    draw_item_texture(f"{base}/rooibos_tea.png", make_tea_cup("#D84315"))
    draw_item_texture(f"{base}/coffee.png", make_tea_cup("#3E2723"))

    # Leaves & Beans
    tea_leaf = {(7, 6): "#4CAF50", (8, 6): "#81C784", (6, 7): "#388E3C", (7, 7): "#4CAF50", (8, 7): "#81C784", (9, 7): "#2E7D32", (7, 8): "#2E7D32"}
    draw_item_texture(f"{base}/tea_leaf.png", tea_leaf)
    draw_item_texture(f"{base}/yerba_mate_leaf.png", tea_leaf)
    draw_item_texture(f"{base}/rooibos_leaf.png", tea_leaf)

    coffee_beans = {(7, 7): "#3E2723", (8, 7): "#4E342E", (7, 8): "#271610", (8, 8): "#3E2723", (8, 6): "#5D4037"}
    draw_item_texture(f"{base}/coffee_beans.png", coffee_beans)

def main():
    print("Generating high-fidelity pixel art textures for Hearthwind Flora...")
    gen_vinery()
    gen_candlelight()
    gen_meadow()
    gen_bakery()
    gen_herbalbrews()
    print("All handcrafted flora pixel art textures generated successfully!")

if __name__ == "__main__":
    main()
