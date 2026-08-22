#!/usr/bin/env python3
"""Migrate the original Aged paxi datapack (MC 1.20.1, pack_format 15) to the
modern 26.x format conventions:

  - directory singularization introduced in 1.21 (loot_tables -> loot_table,
    recipes -> recipe, tags/items -> tags/item, ...)
  - recipe schema changes from 1.21.2 (result.item -> result.id,
    {"item": x} / {"tag": t} ingredients -> [x] / ["#t"])
  - removed loot functions (set_nbt -> set_custom_data)

Only gameplay-relevant namespaces of kept/rebuilt systems are carried over;
data belonging to cut mods is left behind (see SKIP_NAMESPACES).
"""

import json
import shutil
import sys
from pathlib import Path
from typing import Any

SRC_DEFAULT = Path("/tmp/opencode/aged-ref/overrides/config/paxi/datapacks/aged")
ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "conversion" / "datapacks" / "aged-server"

DIR_RENAMES = {
    "loot_tables": "loot_table",
    "recipes": "recipe",
    "advancements": "advancement",
    "predicates": "predicate",
    "item_modifiers": "item_modifier",
    "structures": "structure",
}
TAG_RENAMES = {
    "items": "item",
    "blocks": "block",
    "entity_types": "entity_type",
    "fluids": "fluid",
    "functions": "function",
    "game_events": "game_event",
}

SKIP_NAMESPACES = {
    # cut mods' private data
    "brewery",
    "chalk",
    "dungeonnowloading",
    "farm_and_charm",
    "fishingreal",
    "fleshz",
    "herbalbrews",
    "meadow",
    "moreculling",
    "natures_spirit",
    "smitherz",
    "vinery",
}

report: dict[str, Any] = {
    "files": 0,
    "recipe_fixed": 0,
    "loot_fixed": 0,
    "skipped_ns": [],
    "warnings": [],
    "tag_refs_made_optional": 0,
    "tag_files_scrubbed": 0,
    "worldgen_dropped": [],
}


def iter_ns_strings(node):
    """Yield every 'namespace:path' / '#namespace:path' string in a JSON tree."""
    if isinstance(node, str):
        s = node.lstrip("#")
        if ":" in s and not s.startswith(" ") and len(s) < 200:
            yield s.split(":", 1)[0], s
    elif isinstance(node, list):
        for x in node:
            yield from iter_ns_strings(x)
    elif isinstance(node, dict):
        for x in node.values():
            yield from iter_ns_strings(x)


def drop_foreign_worldgen(out_data: Path, allowed: set[str]):
    """Worldgen JSON is registry-loaded and fatal-on-error. Overrides that
    inject blocks/features from uninstalled mods must be dropped entirely so
    vanilla's own definitions load instead."""
    for p in sorted(out_data.rglob("worldgen/**/*.json")):
        rel = p.relative_to(out_data)
        if "tags" in rel.parts:
            continue  # handled by scrub_tags
        try:
            tree = json.loads(p.read_text())
        except Exception as e:  # noqa: BLE001
            report["warnings"].append(f"unparsable worldgen {rel}: {e}")
            continue
        bad = {s for ns, s in iter_ns_strings(tree) if ns not in allowed}
        if bad:
            p.unlink()
            report["worldgen_dropped"].append(
                {"file": str(rel), "refs": sorted(bad)[:6]}
            )


def installed_namespaces(mods_dir: Path) -> set[str]:
    """Derive the set of namespaces that actually exist on the server by
    reading fabric.mod.json ids from the resolved pack's jars."""
    import zipfile

    # NOTE: 'c' (convention) tags are deliberately NOT trusted as existing:
    # several were removed/renamed by 26.x and a missing required ref aborts
    # boot. Optional refs still resolve once something provides them.
    ns = {"minecraft", "aged"}
    if not mods_dir.exists():
        sys.exit(
            f"mods dir not found: {mods_dir} — run build_pack.py --server-dir first"
        )
    for jar in sorted(mods_dir.glob("*.jar")):
        try:
            with zipfile.ZipFile(jar) as z:
                fmj = json.loads(z.read("fabric.mod.json"))
            mid = fmj.get("id")
            if mid:
                ns.add(mid)
        except Exception as e:  # noqa: BLE001 - report and continue
            print(f"WARN: cannot read {jar.name}: {e}")
    return ns


def entry_ns(v) -> tuple[str, str]:
    """Return (id, namespace) for a tag value entry."""
    raw = v.get("id") if isinstance(v, dict) else v
    vid = str(raw or "").lstrip("#")
    ns = vid.split(":", 1)[0] if ":" in vid else "minecraft"
    return vid, ns


def scrub_tags(out_data: Path, allowed: set[str]):
    """Registry loading is fatal-on-error in modern MC: a tag referencing an
    item/entity from a mod that is not installed aborts server boot. Mark such
    refs {'required': False} so vanilla skips them until the owning mod
    (e.g. our rebuilt custom mods) provides the content. Data is preserved."""
    tag_files = sorted(out_data.rglob("tags/**/*.json"))
    for p in tag_files:
        try:
            d = json.loads(p.read_text())
        except Exception as e:  # noqa: BLE001
            report["warnings"].append(f"unparsable tag {p.name}: {e}")
            continue
        vals = d.get("values")
        if not isinstance(vals, list):
            continue
        changed = False
        out = []
        for v in vals:
            _, ns = entry_ns(v)
            required = v.get("required", True) if isinstance(v, dict) else True
            if required and ns not in allowed:
                out.append(
                    {"id": (v["id"] if isinstance(v, dict) else v), "required": False}
                )
                changed = True
                report["tag_refs_made_optional"] += 1
            else:
                out.append(v)
        if changed:
            d["values"] = out
            p.write_text(json.dumps(d, indent=1))
            report["tag_files_scrubbed"] += 1


def migrate_ingredient(x):
    """26.x ingredient/key values are flat strings: 'mod:item' or '#mod:tag'
    (the 1.21.2 transitional list-wrapped form was removed)."""
    if isinstance(x, dict):
        if "item" in x:
            return x["item"]
        if "tag" in x:
            return "#" + x["tag"]
    return x


def migrate_recipe(data):
    changed = False
    res = data.get("result")
    if isinstance(res, dict) and "item" in res:
        new = {"id": res["item"]}
        if "count" in res:
            new["count"] = res["count"]
        data["result"] = new
        changed = True
    for key in ("ingredient",):
        if key in data:
            new = migrate_ingredient(data[key])
            if new != data[key]:
                data[key] = new
                changed = True
    if isinstance(data.get("key"), dict):
        for k, v in data["key"].items():
            nv = migrate_ingredient(v)
            if nv != v:
                data["key"][k] = nv
                changed = True
    if isinstance(data.get("ingredients"), list):
        nl = [migrate_ingredient(i) for i in data["ingredients"]]
        if nl != data["ingredients"]:
            data["ingredients"] = nl
            changed = True
    return data, changed


def walk_loot(node):
    n = 0
    if isinstance(node, dict):
        fn = node.get("function")
        if fn == "minecraft:set_nbt":
            node["function"] = "minecraft:set_custom_data"
            n += 1
        for v in node.values():
            n += walk_loot(v)
    elif isinstance(node, list):
        for v in node:
            n += walk_loot(v)
    return n


def migrate_file(dst: Path, src: Path):
    rel = dst.relative_to(OUT / "data")
    parts = list(rel.parts)
    ns = parts[0]
    if ns in SKIP_NAMESPACES:
        report["skipped_ns"].append(str(rel))
        return
    # rename well-known dirs anywhere after namespace
    for i, p in enumerate(parts[1:], 1):
        if p in DIR_RENAMES:
            parts[i] = DIR_RENAMES[p]
        elif i >= 1 and parts[i - 1] == "tags" and p in TAG_RENAMES:
            parts[i] = TAG_RENAMES[p]
    dst = OUT.joinpath("data", *parts)
    dst.parent.mkdir(parents=True, exist_ok=True)

    text = src.read_text()
    try:
        data = json.loads(text)
    except json.JSONDecodeError as e:
        report["warnings"].append(f"bad json {rel}: {e}")
        shutil.copyfile(src, dst)
        return

    parent = src.parent.name
    if "recipe" in parent or parent.endswith("recipes"):
        data, ch = migrate_recipe(data)
        if ch:
            report["recipe_fixed"] += 1
    if "loot_table" in (" ".join(parts)):
        n = walk_loot(data)
        if n:
            report["loot_fixed"] += n
    if '"set_nbt"' in text and "loot" not in str(rel):
        report["warnings"].append(f"set_nbt outside loot table: {rel}")

    dst.write_text(json.dumps(data, indent=1))
    report["files"] += 1


def flatten_uniform_providers(out_data: Path):
    """26.x removed the 'value' wrapper from minecraft:uniform int providers:
    {'type':'minecraft:uniform','value':{min,max}} -> flat min/max keys."""
    fixed = 0

    def fix(node):
        nonlocal fixed
        if isinstance(node, dict):
            if node.get("type") == "minecraft:uniform" and isinstance(
                node.get("value"), dict
            ):
                inner = node.pop("value")
                node.update(inner)
                fixed += 1
            for v in node.values():
                fix(v)
        elif isinstance(node, list):
            for x in node:
                fix(x)

    for p in sorted(out_data.rglob("*.json")):
        try:
            tree = json.loads(p.read_text())
        except Exception:  # noqa: BLE001
            continue
        before = fixed
        fix(tree)
        if fixed > before:
            p.write_text(json.dumps(tree, indent=1))
    report["uniform_fixed"] = fixed


def main():
    src = SRC_DEFAULT
    if len(sys.argv) > 1:
        src = Path(sys.argv[1])
    if not src.exists():
        sys.exit(f"source datapack not found: {src}")
    if OUT.exists():
        shutil.rmtree(OUT)
    (OUT / "data").mkdir(parents=True)
    conf = json.load(open(ROOT / "conversion" / "build.conf.json"))
    fmt = conf.get("datapack", {}).get("pack_format")
    if not fmt:
        sys.exit(
            "datapack.pack_format missing in conversion/build.conf.json — "
            "set it from the target server jar's version.json "
            "(pack_version.data_major)"
        )
    mc = conf["targets"]["minecraft"]
    fmt_minor = conf.get("datapack", {}).get("pack_format_minor", 0)
    (OUT / "pack.mcmeta").write_text(
        json.dumps(
            {
                "pack": {
                    "pack_format": fmt,
                    # mandatory once pack_format > 81
                    "min_format": [fmt, 0],
                    "max_format": [fmt, fmt_minor],
                    "description": f"Aged server datapack (migrated from paxi bundle) for MC {mc}",
                }
            },
            indent=2,
        )
    )
    total = 0
    for f in sorted(src.rglob("*.json")):
        rel = f.relative_to(src / "data")
        migrate_file(OUT / "data" / rel, f)
        total += 1
    allowed = installed_namespaces(
        ROOT / "conversion" / "build" / "dist" / "server" / "mods"
    )
    scrub_tags(OUT / "data", allowed)
    drop_foreign_worldgen(OUT / "data", allowed)
    flatten_uniform_providers(OUT / "data")
    print(
        f"scanned {total} files | written {report['files']} | "
        f"recipes migrated {report['recipe_fixed']} | "
        f"loot functions fixed {report['loot_fixed']}"
    )
    print(f"skipped files (cut-mod namespaces): {len(report['skipped_ns'])}")
    print(
        f"tag scrub: {report['tag_files_scrubbed']} files, "
        f"{report['tag_refs_made_optional']} refs -> required:false "
        f"(allowed namespaces: {len(allowed)})"
    )
    print(f"worldgen overrides dropped: {len(report['worldgen_dropped'])}")
    print(f"uniform providers flattened: {report['uniform_fixed']}")
    for w in report["warnings"][:10]:
        print("WARN:", w)
    json.dump(report, open(OUT.parent / "migration-report.json", "w"), indent=1)


if __name__ == "__main__":
    main()
