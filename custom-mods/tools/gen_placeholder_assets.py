#!/usr/bin/env python3
"""Generate placeholder item assets (models, 26.x item definitions, textures,
lang) for the aged-* custom mods. Committed output keeps the repo buildable
without running this script; rerun after adding items.

Pure-python PNG writer (zlib+struct), no PIL dependency.
"""

import json
import struct
import sys
import zlib
from pathlib import Path

MODS = {
    "aged-primitive": {
        "earlystage": {
            "rock": "#7a7a72",
            "flint": "#2b2b30",
            "flint_pickaxe": "#2b2b30",
            "flint_axe": "#2b2b30",
            "flint_shovel": "#2b2b30",
            "flint_hoe": "#2b2b30",
            "flint_sword": "#2b2b30",
            "stone_shears": "#8a8a82",
        },
        "agedaddition": {
            "coal_piece": "#1c1c1c",
            "copper_nugget": "#c46f43",
            "diamond_piece": "#4aedd9",
            "emerald_piece": "#17dd62",
            "lapis_lazuli_piece": "#2545c4",
            "nether_star_piece": "#fdfdd8",
            "netherite_scrap_piece": "#654735",
            "quartz_piece": "#eae5da",
            "raw_copper_nugget": "#9c6238",
            "raw_gold_nugget": "#d8a83c",
            "raw_iron_nugget": "#c8b39a",
        },
    },
    "hearthwind-survival": {
        "dehydration": {
            "water_bowl": "#5a7a3a",  # tainted swamp green
            "purified_water_bowl": "#7ad0f0",  # clear blue
            "hot_water_bowl": "#c45a3a",  # boiling reddish with bubbles
            "hot_purified_water_bowl": "#f0a07a",
            "cold_water_bowl": "#3a6a9a",  # cold blue
            "cold_purified_water_bowl": "#8fd3f0",  # icy clear
        },
    },
    "hearthwind-primitive": {
        "hearthwind": {
            "clay_cup": "#b89a6a",  # fired clay cup - tan
            "clay_cup_unfired": "#9a8a7a",  # unfired clay - grayish tan
            "clay_cup_water": "#5a7a3a",
            "clay_cup_purified": "#7ad0f0",
            "clay_cup_hot_water": "#c45a3a",
            "clay_cup_hot_purified": "#f0a07a",
            "clay_cup_cold_water": "#3a6a9a",
            "clay_cup_cold_purified": "#8fd3f0",
        },
        "environmentz": {
            "wolf_pelt": "#6d5a44",
            "polar_bear_fur": "#e8e8e2",
            "ice_pack": "#9fd8f0",
            "heating_stones": "#b05a28",
            "wolf_helmet": "#5a4a38",
            "wolf_chestplate": "#5a4a38",
            "wolf_leggings": "#4e4030",
            "wolf_boots": "#46382a",
            "wanderer_helmet": "#c8b48c",
            "wanderer_chestplate": "#c8b48c",
            "wanderer_leggings": "#b8a47c",
            "wanderer_boots": "#a89468",
        },
    },
}

# Equipment assets + entity textures (client visuals; server ignores them).
EQUIPMENT = {
    "aged-survival": {
        "environmentz": {
            "wolf": "#5a4a38",
            "wanderer": "#c8b48c",
        },
    },
}

HANDHELD = {
    "flint_pickaxe",
    "flint_axe",
    "flint_shovel",
    "flint_hoe",
    "flint_sword",
    "stone_shears",
}


def write_png(path: Path, rgb: tuple[int, int, int], size: int = 16, hot: bool = False, cold: bool = False):
    def chunk(tag: bytes, data: bytes) -> bytes:
        return (
            struct.pack(">I", len(data))
            + tag
            + data
            + struct.pack(">I", zlib.crc32(tag + data) & 0xFFFFFFFF)
        )

    raw = b""
    r, g, b = rgb
    for y in range(size):
        raw += b"\x00"
        for x in range(size):
            edge = x in (0, size - 1) or y in (0, size - 1)
            shade = 0 if edge else 24
            pr, pg, pb = (max(r - shade, 0), max(g - shade, 0), max(b - shade, 0))
            # Hot boiling bubbles: white specks in upper half
            if hot and not edge:
                if (x, y) in [(4, 4), (7, 5), (9, 7), (5, 9), (11, 6), (8, 10)]:
                    pr, pg, pb = (255, 255, 255)
                elif (x, y) in [(6, 6), (10, 8)]:
                    pr, pg, pb = (255, 220, 180)  # steam tint
            # Cold icy tint: lighter center
            if cold and not edge and 5 <= x <= 10 and 5 <= y <= 10:
                pr, pg, pb = (min(255, pr + 30), min(255, pg + 30), min(255, pb + 20))
            raw += bytes((pr, pg, pb))
    ihdr = struct.pack(">IIBBBBB", size, size, 8, 2, 0, 0, 0)
    path.write_bytes(
        b"\x89PNG\r\n\x1a\n"
        + chunk(b"IHDR", ihdr)
        + chunk(b"IDAT", zlib.compress(raw))
        + chunk(b"IEND", b"")
    )


def hex_rgb(s: str) -> tuple[int, int, int]:
    return (int(s[1:3], 16), int(s[3:5], 16), int(s[5:7], 16))


def main():
    root = Path(__file__).resolve().parents[1]
    total = 0
    for mod, namespaces in MODS.items():
        res = root / mod / "src" / "main" / "resources"
        lang = {}
        # hearthwind-survival still uses aged_survival lang namespace for compat
        mod_id = "aged_survival" if mod == "hearthwind-survival" else mod.replace("aged-", "aged_")
        for ns, items in namespaces.items():
            for name, color in items.items():
                tex = res / "assets" / ns / "textures" / "item" / f"{name}.png"
                tex.parent.mkdir(parents=True, exist_ok=True)
                hot = "hot_" in name
                cold = "cold_" in name
                write_png(tex, hex_rgb(color), hot=hot, cold=cold)
                model = res / "assets" / ns / "models" / "item" / f"{name}.json"
                model.parent.mkdir(parents=True, exist_ok=True)
                model.write_text(
                    json.dumps(
                        {
                            "parent": (
                                "minecraft:item/handheld"
                                if name in HANDHELD
                                else "minecraft:item/generated"
                            ),
                            "textures": {"layer0": f"{ns}:item/{name}"},
                        },
                        indent=2,
                    )
                )
                idef = res / "assets" / ns / "items" / f"{name}.json"
                idef.parent.mkdir(parents=True, exist_ok=True)
                idef.write_text(
                    json.dumps(
                        {
                            "model": {
                                "type": "minecraft:model",
                                "model": f"{ns}:item/{name}",
                            },
                        },
                        indent=2,
                    )
                )
                pretty = name.replace("_", " ").title()
                lang[f"item.{ns}.{name}"] = pretty
        (res / "assets" / mod_id / "lang").mkdir(parents=True, exist_ok=True)
        (res / "assets" / mod_id / "lang" / "en_us.json").write_text(
            json.dumps(lang, indent=2)
        )
        total += sum(len(items) for items in namespaces.values())

    eq_total = 0
    for mod, namespaces in EQUIPMENT.items():
        res = root / mod / "src" / "main" / "resources"
        for ns, assets in namespaces.items():
            for name, color in assets.items():
                eq_dir = res / "assets" / ns / "equipment"
                eq_dir.mkdir(parents=True, exist_ok=True)
                rgb = hex_rgb(color)
                (eq_dir / f"{name}.json").write_text(
                    json.dumps(
                        {
                            "layers": {
                                "humanoid": [{"texture": f"{ns}:{name}"}],
                                "humanoid_leggings": [
                                    {"texture": f"{ns}:{name}_leggings"}
                                ],
                            }
                        },
                        indent=2,
                    )
                )
                tex_dir = (
                    res
                    / "assets"
                    / ns
                    / "textures"
                    / "entity"
                    / "equipment"
                    / "humanoid"
                )
                tex_dir.mkdir(parents=True, exist_ok=True)
                write_png(tex_dir / f"{name}.png", rgb, size=64)
                write_png(tex_dir / f"{name}_leggings.png", rgb, size=64)
                eq_total += 1
    print(
        "generated placeholder assets for",
        total,
        "items",
        "+",
        eq_total,
        "equipment sets",
    )


if __name__ == "__main__":
    sys.exit(main())
