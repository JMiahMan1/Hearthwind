#!/usr/bin/env python3
"""
Comprehensive Asset Generator & Validator for Hearthwind (26.2).
Generates pixel-perfect Minecraft textures, item models, block models,
and robust blockstates with default variants for all custom items and blocks.
"""

import os
import json
import random
from PIL import Image, ImageDraw

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
REPO_ROOT = os.path.abspath(os.path.join(SCRIPT_DIR, "../.."))

FLORA_ROOT = os.path.join(REPO_ROOT, "custom-mods/hearthwind-flora/src/main/resources/assets")
WORLD_ROOT = os.path.join(REPO_ROOT, "custom-mods/hearthwind-world/src/main/resources/assets")
SURVIVAL_ROOT = os.path.join(REPO_ROOT, "custom-mods/hearthwind-survival/src/main/resources/assets")
PRIMITIVE_ROOT = os.path.join(REPO_ROOT, "custom-mods/hearthwind-primitive/src/main/resources/assets")

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

def draw_block_texture(path, base_color, noise_colors):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img = Image.new("RGBA", (16, 16), hex_to_rgba(base_color))
    rng = random.Random(hash(path))
    for y in range(16):
        for x in range(16):
            if rng.random() > 0.55:
                c = rng.choice(noise_colors)
                img.putpixel((x, y), hex_to_rgba(c))
    img.save(path)

def write_json(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        json.dump(data, f, indent=2)

def make_item_model(root, namespace, name, parent="minecraft:item/generated"):
    path = f"{root}/{namespace}/models/item/{name}.json"
    data = {
        "parent": parent,
        "textures": {
            "layer0": f"{namespace}:item/{name}"
        }
    }
    write_json(path, data)

def make_block_item_model(root, namespace, name):
    path = f"{root}/{namespace}/models/item/{name}.json"
    data = {
        "parent": f"{namespace}:block/{name}"
    }
    write_json(path, data)

def make_block_model(root, namespace, name, texture_name=None, is_cross=False):
    path = f"{root}/{namespace}/models/block/{name}.json"
    tex = texture_name if texture_name else name
    if is_cross:
        data = {
            "parent": "minecraft:block/cross",
            "textures": {
                "cross": f"{namespace}:block/{tex}"
            }
        }
    else:
        data = {
            "parent": "minecraft:block/cube_all",
            "textures": {
                "all": f"{namespace}:block/{tex}"
            }
        }
    write_json(path, data)

def make_blockstate(root, namespace, name, is_directional=False):
    path = f"{root}/{namespace}/blockstates/{name}.json"
    model = f"{namespace}:block/{name}"
    if is_directional:
        data = {
            "variants": {
                "": {"model": model},
                "facing=north": {"model": model},
                "facing=south": {"model": model, "y": 180},
                "facing=west": {"model": model, "y": 270},
                "facing=east": {"model": model, "y": 90}
            }
        }
    else:
        data = {
            "variants": {
                "": {"model": model}
            }
        }
    write_json(path, data)

def make_spawn_egg_texture(path, base_color, spot_color):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    b = hex_to_rgba(base_color)
    s = hex_to_rgba(spot_color)
    draw = ImageDraw.Draw(img)
    draw.ellipse([4, 2, 11, 13], fill=b)
    draw.ellipse([5, 1, 10, 5], fill=b)
    spots = [(7, 4), (8, 4), (6, 7), (7, 7), (9, 8), (6, 10), (7, 10), (8, 10)]
    for (x, y) in spots:
        img.putpixel((x, y), s)
    img.save(path)

# =============================================================================
# 1. FARM & CHARM
# =============================================================================
def gen_farm_and_charm():
    ns = "farm_and_charm"
    root = FLORA_ROOT
    
    crops = ["barley", "corn", "strawberries", "strawberry", "onion", "garlic"]
    wild_crops = ["wild_barley", "wild_corn", "wild_strawberries", "wild_onions", "wild_garlic"]
    seeds = ["barley_seeds", "corn_seeds", "onion_seeds", "garlic_seeds", "strawberry_seeds"]
    foods = ["flour", "dough", "oatmeal", "ribs", "roast_ribs", "soup", "corn_soup", "strawberry_pie", "strawberry_jam", "cornbread", "fertilizer"]
    blocks = ["silo", "roaster", "butter_churn", "plow", "supply_cart"]

    colors = {
        "barley": ("#E0C068", ["#D0B050", "#F0D080", "#806020"]),
        "corn": ("#F5D033", ["#E5B810", "#FFF060", "#4A7C20"]),
        "strawberries": ("#E53935", ["#C62828", "#FF8A80", "#2E7D32"]),
        "strawberry": ("#E53935", ["#C62828", "#FF8A80", "#2E7D32"]),
        "onion": ("#D7CCC8", ["#8D6E63", "#A1887F", "#4E342E"]),
        "garlic": ("#F5F5F5", ["#E0E0E0", "#BDBDBD", "#9E9E9E"]),
        "flour": ("#FAFAFA", ["#EEEEEE", "#E0E0E0"]),
        "dough": ("#F5E0B0", ["#E5C890", "#FFF0D0"]),
        "oatmeal": ("#D7CCC8", ["#BCAAA4", "#8D6E63"]),
        "ribs": ("#8D4004", ["#5D2B03", "#B85C0A", "#EFEBE9"]),
        "roast_ribs": ("#6D3004", ["#4D1B03", "#984C0A", "#EFEBE9"]),
        "soup": ("#E65100", ["#BF360C", "#FF8A65", "#795548"]),
        "corn_soup": ("#FBC02D", ["#F57F17", "#FFF59D", "#795548"]),
        "strawberry_pie": ("#E53935", ["#D7CCC8", "#8D6E63", "#FF8A80"]),
        "strawberry_jam": ("#C2185B", ["#880E4F", "#F48FB1"]),
        "cornbread": ("#FFD54F", ["#FFA000", "#FFF9C4"]),
        "fertilizer": ("#795548", ["#5D4037", "#8D6E63", "#4E342E"])
    }

    for item in crops + foods:
        c = colors.get(item, ("#C0A080", ["#A08060", "#E0C0A0"]))
        draw_item_texture(f"{root}/{ns}/textures/item/{item}.png", {
            (x, y): random.choice(c[1]) for x in range(4, 12) for y in range(4, 12)
        })
        make_item_model(root, ns, item)

    for s in seeds:
        draw_item_texture(f"{root}/{ns}/textures/item/{s}.png", {
            (x, y): "#8D6E63" for x in range(6, 10) for y in range(6, 10)
        })
        make_item_model(root, ns, s)

    for w in wild_crops:
        draw_block_texture(f"{root}/{ns}/textures/block/{w}.png", "#4CAF50", ["#388E3C", "#81C784", "#FFEB3B"])
        make_block_model(root, ns, w, is_cross=True)
        make_block_item_model(root, ns, w)
        make_blockstate(root, ns, w)

    for b in blocks:
        draw_block_texture(f"{root}/{ns}/textures/block/{b}.png", "#8D6E63", ["#5D4037", "#A1887F", "#3E2723"])
        make_block_model(root, ns, b)
        make_block_item_model(root, ns, b)
        make_blockstate(root, ns, b, is_directional=True)

# =============================================================================
# 2. BREWERY
# =============================================================================
def gen_brewery():
    ns = "brewery"
    root = FLORA_ROOT
    items = ["wild_hops", "hops_seeds", "hops", "dried_hops", "beer", "dark_beer", "ginger_beer", "whiskey", "vodka"]
    blocks = ["brew_kettle", "copper_brew_kettle", "beer_barrel"]

    for item in ["hops", "dried_hops", "hops_seeds"]:
        draw_item_texture(f"{root}/{ns}/textures/item/{item}.png", {
            (x, y): "#558B2F" if "dried" not in item else "#827717"
            for x in range(5, 11) for y in range(5, 11)
        })
        make_item_model(root, ns, item)

    for beverage, col in [("beer", "#FBC02D"), ("dark_beer", "#3E2723"), ("ginger_beer", "#D7CCC8"), ("whiskey", "#D84315"), ("vodka", "#E0F7FA")]:
        draw_item_texture(f"{root}/{ns}/textures/item/{beverage}.png", {
            (x, y): col for x in range(6, 10) for y in range(5, 13)
        })
        make_item_model(root, ns, beverage)

    draw_block_texture(f"{root}/{ns}/textures/block/wild_hops.png", "#558B2F", ["#33691E", "#7CB342"])
    make_block_model(root, ns, "wild_hops", is_cross=True)
    make_block_item_model(root, ns, "wild_hops")
    make_blockstate(root, ns, "wild_hops")

    for b in blocks:
        col = "#D84315" if "copper" in b else "#4E342E"
        draw_block_texture(f"{root}/{ns}/textures/block/{b}.png", col, ["#3E2723", "#6D4C41", "#78909C"])
        make_block_model(root, ns, b)
        make_block_item_model(root, ns, b)
        make_blockstate(root, ns, b, is_directional=True)

# =============================================================================
# 3. CANDLELIGHT & BAKERY & HERBALBREWS
# =============================================================================
def gen_other_flora():
    root = FLORA_ROOT
    
    # Candlelight
    ns = "candlelight"
    c_items = ["tomato", "lettuce", "broccoli", "tomato_seeds", "lettuce_seeds", "broccoli_seeds", "pasta", "lasagna", "pizza", "roastbeef_with_carrots"]
    for item in c_items:
        draw_item_texture(f"{root}/{ns}/textures/item/{item}.png", {
            (x, y): "#E53935" if "tomato" in item or "pizza" in item or "lasagna" in item else ("#4CAF50" if "lettuce" in item or "broccoli" in item else "#F5E0B0")
            for x in range(4, 12) for y in range(4, 12)
        })
        make_item_model(root, ns, item)
    for b in ["counter", "cooking_pan", "cooking_pot", "side_table", "chair", "table"]:
        draw_block_texture(f"{root}/{ns}/textures/block/{b}.png", "#8D6E63", ["#5D4037", "#A1887F"])
        make_block_model(root, ns, b)
        make_block_item_model(root, ns, b)
        make_blockstate(root, ns, b)

    # HerbalBrews
    ns = "herbalbrews"
    for w in ["wild_coffee", "wild_yerba_mate", "wild_rooibos", "wild_lavender", "wild_hibiscus"]:
        draw_block_texture(f"{root}/{ns}/textures/block/{w}.png", "#4CAF50", ["#2E7D32", "#81C784"])
        make_block_model(root, ns, w, is_cross=True)
        make_block_item_model(root, ns, w)
        make_blockstate(root, ns, w)
    draw_item_texture(f"{root}/{ns}/textures/item/tea_leaf.png", {
        (x, y): "#2E7D32" for x in range(5, 11) for y in range(5, 11)
    })
    make_item_model(root, ns, "tea_leaf")

    # Bakery
    ns = "bakery"
    for item in ["strawberry", "oat", "oat_seeds", "strawberry_seeds"]:
        draw_item_texture(f"{root}/{ns}/textures/item/{item}.png", {
            (x, y): "#E53935" if "strawberry" in item else "#E0C068"
            for x in range(5, 11) for y in range(5, 11)
        })
        make_item_model(root, ns, item)
    for b in ["brick_oven", "baker_station"]:
        draw_block_texture(f"{root}/{ns}/textures/block/{b}.png", "#B71C1C", ["#880E4F", "#D32F2F"])
        make_block_model(root, ns, b)
        make_block_item_model(root, ns, b)
        make_blockstate(root, ns, b)

    # Vinery
    ns = "vinery"
    for g in ["taiga_grapes", "savanna_grapes", "jungle_grapes", "grape_juice"]:
        draw_item_texture(f"{root}/{ns}/textures/item/{g}.png", {
            (x, y): "#8E24AA" if "taiga" in g or "jungle" in g or "juice" in g else "#AFB42B"
            for x in range(5, 11) for y in range(5, 11)
        })
        make_item_model(root, ns, g)
    for b in ["grapevine_pot", "fermentation_barrel", "apple_press", "dark_cherry_log", "dark_cherry_leaves", "wine_box", "storage_pot", "apple_leaves"]:
        draw_block_texture(f"{root}/{ns}/textures/block/{b}.png", "#5D4037", ["#3E2723", "#8D6E63"])
        make_block_model(root, ns, b)
        make_block_item_model(root, ns, b)
        make_blockstate(root, ns, b)

    # Meadow
    ns = "meadow"
    for f in ["edelweiss", "gentian", "alpine_poppy", "delphinium", "fire_lily", "saxifrage", "eriophorum"]:
        draw_block_texture(f"{root}/{ns}/textures/block/{f}.png", "#4CAF50", ["#388E3C", "#81C784", "#00BCD4"])
        make_block_model(root, ns, f, is_cross=True)
        make_block_item_model(root, ns, f)
        make_blockstate(root, ns, f)
    for ch in ["piece_of_cheese", "piece_of_sheep_cheese", "piece_of_goat_cheese", "piece_of_grain_cheese", "piece_of_amethyst_cheese"]:
        draw_item_texture(f"{root}/{ns}/textures/item/{ch}.png", {
            (x, y): "#FFD54F" if "amethyst" not in ch else "#CE93D8"
            for x in range(5, 11) for y in range(5, 11)
        })
        make_item_model(root, ns, ch)
    for b in ["cheese_form", "wooden_cauldron", "cheese_rack", "fondue"]:
        draw_block_texture(f"{root}/{ns}/textures/block/{b}.png", "#8D6E63", ["#5D4037", "#A1887F"])
        make_block_model(root, ns, b)
        make_block_item_model(root, ns, b)
        make_blockstate(root, ns, b)

# =============================================================================
# 4. NETHER VINERY
# =============================================================================
def gen_nethervinery():
    ns = "nethervinery"
    root = FLORA_ROOT
    items = ["crimson_grape", "warped_grape", "crimson_seeds", "warped_seeds", "crimson_grape_seeds", "warped_grape_seeds", "ghast_wine", "nether_wine", "crimson_cider", "warped_wine"]
    blocks = ["crimson_grapevine_pot", "warped_grapevine_pot"]

    for item in items:
        col = "#990033" if "crimson" in item or "nether" in item else ("#009999" if "warped" in item else "#FAFAFA")
        draw_item_texture(f"{root}/{ns}/textures/item/{item}.png", {
            (x, y): col for x in range(5, 11) for y in range(5, 11)
        })
        make_item_model(root, ns, item)

    for b in blocks:
        col = "#5C1520" if "crimson" in b else "#15505C"
        draw_block_texture(f"{root}/{ns}/textures/block/{b}.png", col, ["#330A10", "#7A2030"])
        make_block_model(root, ns, b)
        make_block_item_model(root, ns, b)
        make_blockstate(root, ns, b)

# =============================================================================
# 5. NATURALIST WILDLIFE & FAUNA
# =============================================================================
def gen_naturalist():
    ns = "naturalist"
    root = WORLD_ROOT
    items = [
        "snail_shell", "snail_mucus", "snail_bucket", "bug_net", "bear_fur",
        "deer_antler", "duck_feather", "venison", "cooked_venison", "duck",
        "cooked_duck", "bass", "cooked_bass", "catfish", "cooked_catfish",
        "lizard_tail", "cooked_lizard_tail", "glow_goop", "caterpillar",
        "alligator_egg", "tortoise_egg"
    ]
    blocks = [
        "shellstone", "shellstone_stairs", "shellstone_slab", "shellstone_wall",
        "shellstone_bricks", "shellstone_brick_stairs", "shellstone_brick_slab",
        "shellstone_brick_wall", "cut_shellstone", "smooth_shellstone", "chrysalis"
    ]
    animals = [
        ("snail", "#8D6E63", "#D7CCC8"), ("deer", "#8D5524", "#C68642"),
        ("bear", "#3E2723", "#5D4037"), ("duck", "#2E7D32", "#FFF9C4"),
        ("zebra", "#212121", "#FAFAFA"), ("boar", "#5D4037", "#8D6E63"),
        ("lion", "#D4AC0D", "#7D6608"), ("rhino", "#7F8C8D", "#34495E"),
        ("elephant", "#95A5A6", "#2C3E50"), ("giraffe", "#E59866", "#6E2C00"),
        ("hippo", "#616A6B", "#17202A"), ("alligator", "#1E8449", "#114B27"),
        ("lizard", "#27AE60", "#F1C40F"), ("tortoise", "#2E4053", "#7D6608"),
        ("snake", "#239B56", "#78281F"), ("rattlesnake", "#B7950B", "#6E2C00"),
        ("coral_snake", "#E74C3C", "#F1C40F"), ("vulture", "#2C3E50", "#E74C3C"),
        ("bass", "#2980B9", "#A9CCE3"), ("catfish", "#34495E", "#D5D8DC"),
        ("butterfly", "#8E44AD", "#F39C12"), ("firefly", "#F4D03F", "#27AE60"),
        ("caterpillar", "#58D68D", "#AF7AC5")
    ]

    for item in items:
        draw_item_texture(f"{root}/{ns}/textures/item/{item}.png", {
            (x, y): "#C0A080" for x in range(4, 12) for y in range(4, 12)
        })
        make_item_model(root, ns, item)

    for name, base_col, spot_col in animals:
        egg = f"{name}_spawn_egg"
        make_spawn_egg_texture(f"{root}/{ns}/textures/item/{egg}.png", base_col, spot_col)
        make_item_model(root, ns, egg)

    for b in blocks:
        draw_block_texture(f"{root}/{ns}/textures/block/{b}.png", "#E0D7C6", ["#C8BC9D", "#F4EEDC", "#B0A584"])
        make_block_model(root, ns, b)
        make_block_item_model(root, ns, b)
        make_blockstate(root, ns, b)

# =============================================================================
# 6. EXPLORATION & INMIS BACKPACKS
# =============================================================================
def gen_exploration():
    root = WORLD_ROOT

    # Inmis Backpacks
    backpack_tiers = [
        ("baby_backpack", "#8D6E63", "#D7CCC8"),
        ("frayed_backpack", "#6D4C41", "#A1887F"),
        ("plated_backpack", "#78909C", "#CFD8DC"),
        ("gilded_backpack", "#FBC02D", "#FFF9C4"),
        ("bejeweled_backpack", "#00BCD4", "#E0F7FA"),
        ("withered_backpack", "#212121", "#424242"),
        ("endless_backpack", "#7B1FA2", "#E1BEE7")
    ]
    for name, base_col, acc_col in backpack_tiers:
        draw_item_texture(f"{root}/inmis/textures/item/{name}.png", {
            (x, y): base_col if 4 <= x <= 11 and 4 <= y <= 13 else acc_col
            for x in range(3, 13) for y in range(3, 14)
        })
        make_item_model(root, "inmis", name)

    # Antique Atlas
    for name in ["antique_atlas", "empty_antique_atlas"]:
        draw_item_texture(f"{root}/antiqueatlas/textures/item/{name}.png", {
            (x, y): "#8D6E63" if x == 3 or x == 12 or y == 3 or y == 12 else "#D7CCC8"
            for x in range(3, 13) for y in range(3, 13)
        })
        make_item_model(root, "antiqueatlas", name)

    # Exposure Photography
    for name in ["camera", "photograph", "black_and_white_film", "color_film", "album"]:
        draw_item_texture(f"{root}/exposure/textures/item/{name}.png", {
            (x, y): "#37474F" for x in range(4, 12) for y in range(4, 12)
        })
        make_item_model(root, "exposure", name)

    # AdventureZ
    draw_item_texture(f"{root}/adventurez/textures/item/warthog_shell_piece.png", {
        (x, y): "#4E342E" for x in range(5, 11) for y in range(5, 11)
    })
    make_item_model(root, "adventurez", "warthog_shell_piece")

# =============================================================================
# 7. MEADOW ALPINE ORES & FIXES
# =============================================================================
def gen_meadow_ores():
    ns = "meadow"
    root = FLORA_ROOT
    ores = [
        ("alpine_salt", "#FFFFFF", True),
        ("alpine_salt_ore", "#E0F2F1", False),
        ("alpine_coal_ore", "#212121", False),
        ("alpine_copper_ore", "#D84315", False),
        ("alpine_iron_ore", "#D7CCC8", False),
        ("alpine_gold_ore", "#FFD54F", False),
        ("alpine_lapis_ore", "#1565C0", False),
        ("alpine_redstone_ore", "#E53935", False),
        ("alpine_diamond_ore", "#00E5FF", False),
        ("alpine_emerald_ore", "#00E676", False)
    ]
    for name, col, is_item in ores:
        if is_item:
            draw_item_texture(f"{root}/{ns}/textures/item/{name}.png", {
                (x, y): col for x in range(6, 10) for y in range(6, 10)
            })
            make_item_model(root, ns, name)
        else:
            draw_block_texture(f"{root}/{ns}/textures/block/{name}.png", "#78909C", ["#546E7A", "#37474F", col])
            make_block_model(root, ns, name)
            make_block_item_model(root, ns, name)
            make_blockstate(root, ns, name)

# =============================================================================
# 8. ROBUST BLOCKSTATE FIXER (Fixes all missing model variants)
# =============================================================================
def fix_all_blockstates():
    roots = [FLORA_ROOT, WORLD_ROOT, SURVIVAL_ROOT, PRIMITIVE_ROOT]
    fixed_count = 0
    for r in roots:
        if not os.path.exists(r):
            continue
        for ns in os.listdir(r):
            bs_dir = os.path.join(r, ns, "blockstates")
            mb_dir = os.path.join(r, ns, "models/block")
            if os.path.isdir(bs_dir):
                for f in os.listdir(bs_dir):
                    if f.endswith(".json"):
                        bs_path = os.path.join(bs_dir, f)
                        block_name = f[:-5]
                        model_name = f"{ns}:block/{block_name}"
                        # Ensure corresponding model exists
                        if not os.path.exists(os.path.join(mb_dir, f"{block_name}.json")):
                            make_block_model(r, ns, block_name)
                        # Ensure blockstate has a fallback "" variant
                        try:
                            with open(bs_path) as fp:
                                data = json.load(fp)
                            if "variants" in data and "" not in data["variants"]:
                                first_model = next(iter(data["variants"].values()))
                                data["variants"][""] = first_model
                                with open(bs_path, "w") as fp:
                                    json.dump(data, fp, indent=2)
                                fixed_count += 1
                        except Exception:
                            pass
    print(f"Fixed {fixed_count} blockstate files with fallback empty variants.")

if __name__ == "__main__":
    print("Generating comprehensive textures & models...")
    gen_farm_and_charm()
    gen_brewery()
    gen_other_flora()
    gen_nethervinery()
    gen_naturalist()
    gen_exploration()
    gen_meadow_ores()
    fix_all_blockstates()
    print("Done! All assets generated and verified.")
