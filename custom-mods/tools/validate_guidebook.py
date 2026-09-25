#!/usr/bin/env python3
"""Validate the Hearthwind guidebook against what the pack actually ships.

The book must never promise something the game does not do. This catches the
mechanical half of that promise:
  - every <recipe;id> exists (vanilla, pack datapacks or a shipped mod jar)
    and is not stripped by RecipeRemovals
  - every <item;id> / <block;id> and every entry/category icon exists
  - every ^hearthwind:entry link resolves to an entry file
  - every entry's category exists
  - every drop-cap letter has its illuminated initial texture
  - no unknown {macro} is left in the text

Prose facts (numbers, behaviour) are checked by review against the code; see
docs/GUIDEBOOK_PARITY.md.

Usage: python3 custom-mods/tools/validate_guidebook.py   (exit 1 on problems)
"""

import glob
import json
import re
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
BOOK = ROOT / "custom-mods/hearthwind-survival/src/main/resources/assets/hearthwind"
LAV = BOOK / "lavender"
BOOK_ID = "hearthwind_guide_book"


def jar_sources():
    jars = set(glob.glob(str(ROOT / "conversion/build/dist/client/mods/*.jar")))
    jars |= set(glob.glob(str(ROOT / "conversion/vendored/*.jar")))
    jars |= {j for j in glob.glob(str(ROOT / "custom-mods/*/build/libs/*26.2*.jar")) if "sources" not in j}
    jars |= set(glob.glob(str(ROOT / "custom-mods/.gradle/loom-cache/minecraftMaven/net/minecraft/minecraft-merged-*/*/minecraft-merged-*.jar")))
    return sorted(jars)


RECIPE_RE = re.compile(r"data/([^/]+)/recipes?/(.+)\.json$")
ITEM_RE = re.compile(r"assets/([^/]+)/items/(.+)\.json$")


def collect_ids():
    recipes, items = set(), set()
    for j in jar_sources():
        try:
            names = zipfile.ZipFile(j).namelist()
        except zipfile.BadZipFile:
            continue
        for n in names:
            m = RECIPE_RE.match(n)
            if m:
                recipes.add(f"{m.group(1)}:{m.group(2)}")
            m = ITEM_RE.match(n)
            if m:
                items.add(f"{m.group(1)}:{m.group(2)}")
    for tree in list(ROOT.glob("conversion/datapacks/*/data")) + list(ROOT.glob("custom-mods/*/src/main/resources/data")):
        for f in tree.glob("*/recipe/**/*.json"):
            rel = f.relative_to(tree)
            recipes.add(f"{rel.parts[0]}:{'/'.join(rel.parts[2:])[:-5]}")
    for tree in ROOT.glob("custom-mods/*/src/main/resources/assets"):
        for f in tree.glob("*/items/**/*.json"):
            rel = f.relative_to(tree)
            items.add(f"{rel.parts[0]}:{'/'.join(rel.parts[2:])[:-5]}")
    return recipes, items


def removed_recipes():
    out = set()
    for f in ROOT.glob("custom-mods/hearthwind-primitive/src/main/resources/data/earlystage/recipe_removals/*.json"):
        data = json.load(open(f))
        for group in data.values() if isinstance(data, dict) else [data]:
            if isinstance(group, list):
                out.update(x for x in group if isinstance(x, str))
            elif isinstance(group, dict):
                for v in group.values():
                    if isinstance(v, list):
                        out.update(x for x in v if isinstance(x, str))
    return out


def front_matter(text):
    m = re.match(r"\s*```json\s*(\{.*?\})\s*```", text, re.S)
    return (json.loads(m.group(1)), text[m.end():]) if m else (None, text)


def main():
    recipes, items = collect_ids()
    removed = removed_recipes()
    macros = set(json.load(open(LAV / f"books/{BOOK_ID}.json")).get("macros", {}))
    entries = {f"hearthwind:{p.relative_to(LAV / 'entries' / BOOK_ID).as_posix()[:-3]}": p
               for p in (LAV / "entries" / BOOK_ID).rglob("*.md")}
    categories = {f"hearthwind:{p.stem}": p for p in (LAV / "categories" / BOOK_ID).glob("*.md")}
    initials = {p.stem for p in (BOOK / "textures/gui/guide/initial").glob("*.png")}
    problems = []

    def check_item(where, iid, kind):
        if iid not in items:
            problems.append(f"{where}: unknown {kind} '{iid}'")

    for cid, path in categories.items():
        fm, _ = front_matter(path.read_text())
        if not fm:
            problems.append(f"{path.name}: missing front matter")
        elif "icon" in fm:
            check_item(f"category {cid}", fm["icon"], "icon")

    for eid, path in entries.items():
        text = path.read_text()
        fm, body = front_matter(text)
        where = f"entry {eid}"
        if fm is None:
            problems.append(f"{where}: missing front matter")
            continue
        if "category" in fm and fm["category"] not in categories:
            problems.append(f"{where}: unknown category {fm['category']}")
        if "icon" in fm:
            check_item(where, fm["icon"], "icon")
        for rid in re.findall(r"<recipe;([^>]+)>", body):
            if rid not in recipes:
                problems.append(f"{where}: recipe '{rid}' does not exist")
            elif rid in removed:
                problems.append(f"{where}: recipe '{rid}' is removed by RecipeRemovals")
        for kind, iid in re.findall(r"<(item|block);([^>{]+)", body):
            check_item(where, iid, kind)
        for link in re.findall(r"\]\(\^([^)]+)\)", body):
            if link not in entries:
                problems.append(f"{where}: link to missing entry '{link}'")
        for letter in re.findall(r"drop-cap@hearthwind:guide\|letter=([a-z])", body):
            if letter not in initials:
                problems.append(f"{where}: no initial texture for '{letter}'")
        for tok in re.findall(r"\{([a-z_]+)\}", body):
            known_formatting = {"black", "dark_blue", "dark_green", "dark_aqua", "dark_red", "dark_purple",
                                "gold", "gray", "dark_gray", "blue", "green", "aqua", "red", "light_purple",
                                "yellow", "white"}
            if "{" + tok + "}" not in macros and tok not in known_formatting:
                problems.append(f"{where}: unknown macro or colour '{{{tok}}}'")

    print(f"guidebook: {len(categories)} categories, {len(entries)} entries, "
          f"{len(recipes)} known recipes, {len(items)} known items")
    for p in problems:
        print("  PROBLEM:", p)
    if problems:
        print(f"guidebook: {len(problems)} problem(s)")
        return 1
    print("guidebook: OK")
    return 0


if __name__ == "__main__":
    sys.exit(main())
