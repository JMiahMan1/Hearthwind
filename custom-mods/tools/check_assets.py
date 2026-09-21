"""Asset-completeness gate for letsdo/hearthwind ports.

Every model referenced from a blockstate and every texture referenced from a
model must exist on disk. Missing files render as purple-black checkerboards
in-game; headless gametests never catch them, so this script is the guard.

Usage: python3 tools/check_assets.py  (exit 0 = clean, 1 = missing refs)
"""
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MODS = [
    "letsdo-brewery",
    "letsdo-farm-and-charm",
    "letsdo-vinery",
    "letsdo-bakery",
    "hearthwind-primitive",
    "hearthwind-survival",
    "hearthwind-client",
]

MODEL_RE = re.compile(r'"model"\s*:\s*"([a-z0-9_]+:[a-z0-9_/]+)"')
TEX_RE = re.compile(r'"(?:layer\d+"|"[0-9a-z_]+"|particle)"\s*:\s*"([a-z0-9_]+:[a-z0-9_/]+)"')


def exists(ns_path: str, kind: str) -> bool:
    """kind is 'models' (.json) or 'textures' (.png)."""
    ns, _, path = ns_path.partition(":")
    if ns == "minecraft":
        return True
    ext = ".json" if kind == "models" else ".png"
    for mod in MODS:
        if (ROOT / mod / "src" / "main" / "resources" / "assets" / ns / kind / (path + ext)).exists():
            return True
    return False


def main() -> int:
    missing = []
    for mod in MODS:
        assets = ROOT / mod / "src" / "main" / "resources" / "assets"
        if not assets.exists():
            continue
        for nsdir in assets.iterdir():
            if not nsdir.is_dir():
                continue
            bsdir = nsdir / "blockstates"
            if bsdir.exists():
                for bs in bsdir.glob("*.json"):
                    try:
                        text = bs.read_text()
                    except OSError:
                        continue
                    for m in set(MODEL_RE.findall(text)):
                        if not exists(m, "models"):
                            missing.append(f"{nsdir.name}:{bs.stem} -> model {m}")
            mdir = nsdir / "models"
            if mdir.exists():
                idir = nsdir / "items"
                # Top-level item models only: nested files (e.g.
                # item/breathalyzer/x.json) are override parts selected by
                # the parent item's definition, not items themselves.
                for mp in mdir.glob("*.json"):
                    rel = mp.relative_to(mdir)
                    # 26.x renders items only with an item definition file;
                    # without one every ported item is a purple-black checker.
                    if rel.parts[0] == "item" and not (idir / (mp.stem + ".json")).exists():
                        missing.append(f"{nsdir.name}:{mp.stem} -> missing items/{mp.stem}.json")
                    try:
                        text = mp.read_text()
                    except OSError:
                        continue
                    # validate JSON too - broken files also render missing
                    try:
                        json.loads(text)
                    except json.JSONDecodeError as e:
                        missing.append(f"{nsdir.name}:{mp.stem} INVALID JSON: {e}")
                        continue
                    for t in set(TEX_RE.findall(text)):
                        if not exists(t, "textures"):
                            missing.append(f"{nsdir.name}:{mp.stem} -> texture {t}")
    if missing:
        print(f"ASSET CHECK FAILED: {len(missing)} missing refs")
        for m in sorted(set(missing)):
            print("  ", m)
        return 1
    print("asset check: clean (all blockstate models + model textures resolve)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
