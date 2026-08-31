#!/usr/bin/env python3
"""
Texture verification tool for Hearthwind mods.
Checks that all model JSON files reference valid textures that exist on disk,
and validates PNG dimensions (non-POT is OK for HUD sprites).

Usage:
    python3 tools/check_textures.py

Exit codes:
    0 - all checks pass
    1 - issues found (missing textures or invalid PNGs)
"""
import json
import re
import sys
from collections import Counter
from pathlib import Path

try:
    from PIL import Image
except ImportError:
    print("WARNING: Pillow not installed, skipping PNG validation", file=sys.stderr)
    Image = None

MODS = [
    "hearthwind-primitive",
    "hearthwind-survival",
    "hearthwind-skills",
    "hearthwind-jobs",
    "hearthwind-world",
    "hearthwind-client",
]

# Known HUD sprites that are intentionally non-POT (used with blitSprite)
KNOWN_NON_POT = {
    "hearthwind-client/src/main/resources/assets/hearthwind/textures/gui/sprites/hud/thirst_empty.png",
    "hearthwind-client/src/main/resources/assets/hearthwind/textures/gui/sprites/hud/thirst_half.png",
    "hearthwind-client/src/main/resources/assets/hearthwind/textures/gui/sprites/hud/thirst_full.png",
    "hearthwind-client/src/main/resources/assets/hearthwind/textures/gui/thirst_icons.png",
}


def find_models(base: Path) -> list[Path]:
    """Find all model JSON files under assets/."""
    models_dir = base / "src" / "main" / "resources" / "assets"
    if not models_dir.exists():
        return []
    return sorted(models_dir.glob("**/models/**/*.json"))


def extract_textures(node) -> set[str]:
    """Recursively extract all texture references from a model JSON."""
    textures = set()
    if isinstance(node, dict):
        if "textures" in node and isinstance(node["textures"], dict):
            for tex in node["textures"].values():
                if tex and tex != "#missing" and not tex.startswith("minecraft:textures/"):
                    textures.add(tex)
        elif "texture" in node:
            tex = node["texture"]
            if tex and tex != "#missing" and not tex.startswith("minecraft:textures/"):
                textures.add(tex)
        for v in node.values():
            textures |= extract_textures(v)
    elif isinstance(node, list):
        for item in node:
            textures |= extract_textures(item)
    return textures


def extract_hud_textures(java_dir: Path) -> set[str]:
    """Extract texture IDs from blitSprite calls in Java files."""
    ids = set()
    if not java_dir.exists():
        return ids
    pattern = re.compile(r'Identifier\.fromNamespaceAndPath\("([^"]+)",\s*"([^"]+)"\)')
    for jfile in java_dir.rglob("*.java"):
        try:
            with open(jfile) as f:
                content = f.read()
            for line in content.split('\n'):
                if 'blitSprite' in line:
                    for match in pattern.finditer(line):
                        ns, path = match.groups()
                        ids.add(f"{ns}:{path}")
        except OSError:
            continue
    return ids


def check_textures_exist(textures: set[str]) -> list[tuple[str, str]]:
    """Check that all referenced textures exist on disk in any module."""
    missing = []
    for tex in textures:
        parts = tex.split(":")
        if len(parts) == 2:
            ns, tex_path = parts
        else:
            ns = "minecraft"
            tex_path = tex
        tex_path = tex_path.replace("minecraft:textures/", "")
        found = False
        for mod in MODS:
            tex_file = Path("custom-mods") / mod / "src" / "main" / "resources" / "assets" / ns / "textures" / f"{tex_path}.png"
            if tex_file.exists():
                found = True
                break
        if not found:
            missing.append((ns, tex_path))
    return missing


def check_png_dims(pns: list[Path], root: Path) -> tuple[list[str], Counter]:
    """Validate PNG dimensions. Non-POT is OK for HUD sprites."""
    if Image is None:
        return [], Counter()
    errors = []
    dims = Counter()
    for png in pns:
        try:
            with Image.open(png) as img:
                w, h = img.size
                dims[f"{w}x{h}"] += 1
                rel = png.relative_to(root)
                if str(rel) not in KNOWN_NON_POT and (
                        w != 2 ** (w.bit_length() - 1) or h != 2 ** (h.bit_length() - 1)):
                    errors.append(f"Non-power-of-2: {rel} ({w}x{h})")
        except (OSError, ValueError) as e:
            errors.append(f"Error reading: {png.relative_to(root)} ({e})")
    return errors, dims


def main() -> int:
    root = Path(__file__).resolve().parent.parent
    models_found = 0
    all_textures: set[str] = set()
    all_pns: list[Path] = []

    for mod in MODS:
        base = root / mod
        # Models
        for model in find_models(base):
            models_found += 1
            try:
                with open(model) as f:
                    model_data = json.load(f)
                all_textures |= extract_textures(model_data)
            except (json.JSONDecodeError, OSError) as e:
                print(f"  ERROR reading model {model.relative_to(root)}: {e}", file=sys.stderr)

        # HUD sprites from Java
        java_dir = base / "src" / "main" / "java"
        hud_ids = extract_hud_textures(java_dir)
        all_textures |= hud_ids

        # PNGs
        assets_dir = base / "src" / "main" / "resources" / "assets"
        if assets_dir.exists():
            all_pns.extend(sorted(assets_dir.glob("**/*.png")))

    print(f"Models checked:  {models_found}")
    print(f"Textures ref'd:  {len(all_textures)}")
    print(f"PNGs found:      {len(all_pns)}")

    errors = 0

    # Check texture references
    missing = check_textures_exist(all_textures)
    if missing:
        print(f"\n  MISSING TEXTURES ({len(missing)}):")
        for ns, name in missing:
            print(f"    {ns}:{name}")
        errors += len(missing)

    # Check PNG dimensions
    if Image is not None:
        png_errors, dims = check_png_dims(all_pns, root)
        if png_errors:
            print(f"\n  PNG ERRORS ({len(png_errors)}):")
            for e in png_errors:
                print(f"    {e}")
            errors += len(png_errors)
        if dims:
            print("\n  Dimension distribution:")
            for dim, count in sorted(dims.items()):
                print(f"    {dim}: {count}")
    else:
        print("\n  (PNG validation skipped - install Pillow)")

    if errors:
        print(f"\nFAILED: {errors} issue(s) found")
        return 1
    print("\nOK: All textures valid")
    return 0


if __name__ == "__main__":
    sys.exit(main())
