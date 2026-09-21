#!/usr/bin/env python3
"""Generate the showcase datapack: one function per module that setblocks
every blockstate id and summons every item id from that module's assets,
on a per-mod grid band. Running `function showcase:setup` (via RCON on a
server with all pack mods) renders every texture in-world and reports any
unknown id as a command error in the log.

Output: custom-mods/showcase/ (pack.mcmeta pack_format read from
conversion/build.conf.json).

Usage: python3 tools/gen_showcase.py
"""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
CUSTOM = ROOT / "custom-mods"
OUT = CUSTOM / "showcase"
DATA = OUT / "data" / "showcase" / "function"

MODULES = [
    "hearthwind-survival", "hearthwind-jobs", "hearthwind-primitive",
    "hearthwind-skills", "hearthwind-world", "hearthwind-client",
    "letsdo-meadow", "letsdo-bakery", "letsdo-brewery",
    "letsdo-candlelight", "letsdo-farm-and-charm", "letsdo-herbalbrews",
    "letsdo-vinery", "letsdo-nethervinery", "smallships",
    "villagesandpillages",
]

pack_format = json.load(open(ROOT / "conversion/build.conf.json"))["datapack"]["pack_format"]

# fresh output (keep README if present)
import shutil
if OUT.exists():
    shutil.rmtree(OUT)
DATA.mkdir(parents=True)

(OUT / "pack.mcmeta").write_text(json.dumps(
    {"pack": {"pack_format": pack_format,
              "description": "Hearthwind texture/mod showcase (generated)"}},
    indent=2) + "\n")

COLS = 40
CELL = 2
Y = 70
# asset ids with no registered block/item behind them (verified: zero java
# mentions in tree AND port). A function file aborts on first unknown id,
# so these must stay out until code registers them.
PRUNE_BLOCKS = {"brewery:brew_kettle",
                # codeless naturalist shellstone family (assets only, no java)
                "naturalist:chrysalis", "naturalist:cut_shellstone",
                "naturalist:shellstone", "naturalist:shellstone_bricks",
                "naturalist:shellstone_slab", "naturalist:shellstone_stairs",
                "naturalist:shellstone_wall", "naturalist:smooth_shellstone",
                "naturalist:shellstone_brick_slab",
                "naturalist:shellstone_brick_stairs",
                "naturalist:shellstone_brick_wall"}
PRUNE_ITEMS = set()
setup = []
tot_blocks = tot_items = 0
for mi, mod in enumerate(MODULES):
    assets = CUSTOM / mod / "src" / "main" / "resources" / "assets"
    if not assets.exists():
        continue
    blocks, items = [], []
    for bs in sorted(assets.rglob("blockstates/*.json")):
        rel = bs.relative_to(assets)
        ns = rel.parts[0]
        if ns == "minecraft":
            continue
        bid = f"{ns}:{bs.stem}"
        if bid in PRUNE_BLOCKS:
            continue
        blocks.append(bid)
    for idef in sorted(assets.rglob("items/*.json")):
        rel = idef.relative_to(assets)
        ns = rel.parts[0]
        if ns == "minecraft":
            continue
        try:
            d = json.loads(idef.read_text())
        except Exception:
            continue
        # only plain model defs (skip selectors/overlays we cannot summon simply)
        m = d.get("model")
        if isinstance(m, dict) and m.get("type") == "minecraft:model":
            iid = f"{ns}:{idef.stem}"
            if iid in PRUNE_ITEMS:
                continue
            items.append(iid)
    if not blocks and not items:
        continue
    z0 = mi * 80
    # platform sized to content
    rows = max(1, (len(blocks) + COLS - 1) // COLS)
    lines = [f"# showcase {mod}: {len(blocks)} blocks, {len(items)} items",
             f"fill 0 {Y-1} {z0} {COLS*CELL} {Y-1} {z0+rows*CELL+8} minecraft:stone",
             f"fill 0 {Y-1} {z0} {COLS*CELL} {Y-1} {z0+rows*CELL+8} minecraft:stone_bricks replace minecraft:stone"]
    for i, bid in enumerate(blocks):
        x, z = (i % COLS) * CELL + 1, z0 + (i // COLS) * CELL + 1
        lines.append(f"setblock {x} {Y} {z} {bid}")
    for iid in items:
        lines.append(f"summon minecraft:item 2 {Y+2} {z0+2} {{Item:{{id:\"{iid}\",count:1}}}}")
    lines.append(f"kill @e[type=minecraft:item,x=2,y={Y+2},z={z0+2},distance=..30]")
    (DATA / f"{mod}.mcfunction").write_text("\n".join(lines) + "\n")
    setup.append(f"function showcase:{mod}")
    tot_blocks += len(blocks)
    tot_items += len(items)

(DATA / "setup.mcfunction").write_text("\n".join(setup) + "\n")
print(f"showcase: {len(setup)} modules, {tot_blocks} setblocks, {tot_items} summons")
print(f"out: {OUT.relative_to(ROOT)}")
