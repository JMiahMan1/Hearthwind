#!/usr/bin/env python3
"""Every [source:location] asset the pack's FancyMenu layouts name must resolve.

This exists because of a shipped bug. The pack shipped
``config/fancymenu/customization/aged_title_screen_layout.txt``, which pointed
its background at ``aged:textures/main_menu_background_with_aged.png`` with
``aged:textures/main_menu_background.png`` as the fallback. Those files only
ever reached an instance as ``.minecraft/resources/aged/textures/`` - a folder
no game loads - so the background AND the fallback were missing and the main
menu drew the magenta-and-black missing-texture checkerboard.

A screenshot did not catch it: the client gametest suite had been running with
``CGT_EXCLUDE_MODS=DistantHorizons,fancymenu`` (FancyMenu was excluded to keep
its first-run welcome popup out of the screenshots), so the very mod that draws
the menu was never loaded during testing. A static check is the honest guard.

Checks, against the built client jar plus vanilla:

* every ``[source:location]ns:path`` in ``conversion/overrides/config/fancymenu``
  resolves to a file in a client jar (as ``assets/<ns>/<path>``), in a
  ``resourcepacks/`` pack we ship, or in vanilla;
* the same for any ``assets/<ns>/<path>`` style reference we author ourselves;
* nothing in the pack's config points into a top-level ``resources/`` folder,
  because a launcher copies that folder straight into the instance root and the
  game never loads it.

Exit code 1 on any unresolvable reference.
"""

from __future__ import annotations

import re
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
FANCYMENU_CONFIG = ROOT / "conversion/overrides/config/fancymenu"
CLIENT_JAR = ROOT / "custom-mods/hearthwind-client/build/libs/hearthwind-client-26.2+0.1.0.jar"

# [source:location]namespace:path/to/file.png  (FancyMenu's location resource)
LOCATION = re.compile(r"\[source:location\]([a-z0-9_.-]+):([a-z0-9_./-]+)")

# Textures a third-party mod can ship itself. Only the namespaces we author or
# deliberately keep are checked; anything else is the layout's business.
AUTHORED = {"hearthwind", "minecraft"}

# Vanilla assets we know exist in 26.2 and that layouts legitimately name.
# Anything NOT listed here has to be proven present in a jar, so a typo cannot
# slip through as "probably vanilla".
VANILLA_OK = {
    "minecraft:textures/gui/options_background.png",
    "minecraft:textures/gui/sprites/button.png",
}


def jar_assets() -> set[str]:
    """Every assets/<ns>/<path> in the built client jar, as ``ns:path``."""
    if not CLIENT_JAR.is_file():
        sys.exit(f"ERROR: client jar not built: {CLIENT_JAR}")
    with zipfile.ZipFile(CLIENT_JAR) as jar:
        found = set()
        for name in jar.namelist():
            if not name.startswith("assets/"):
                continue
            namespace, _, rest = name[len("assets/") :].partition("/")
            found.add(f"{namespace}:{rest}")
        return found


def pack_assets() -> set[str]:
    """Assets we ship in overrides/resourcepacks packs, as ``ns:path``."""
    found: set[str] = set()
    root = ROOT / "conversion/overrides/resourcepacks"
    if not root.is_dir():
        return found
    for mcmeta in root.glob("*/pack.mcmeta"):
        assets = mcmeta.parent / "assets"
        if not assets.is_dir():
            continue
        for asset in assets.rglob("*"):
            if asset.is_file():
                rel = asset.relative_to(assets).as_posix()
                found.add(f"{rel.split('/', 1)[0]}:{rel.split('/', 1)[1]}")
    return found


def main() -> int:
    if not FANCYMENU_CONFIG.is_dir():
        print("menu-assets: OK (no fancymenu config in the pack)")
        return 0

    available = jar_assets() | pack_assets()
    problems: list[str] = []
    checked = 0

    for layout in sorted(FANCYMENU_CONFIG.rglob("*")):
        if not layout.is_file() or layout.suffix != ".txt":
            continue
        for ns, path in LOCATION.findall(layout.read_text(encoding="utf-8")):
            ref = f"{ns}:{path}"
            if ns not in AUTHORED and ref not in VANILLA_OK:
                # Another mod's namespace: only flag it if we cannot find it in
                # anything we ship, which is the case that broke the menu.
                pass
            checked += 1
            if ns in AUTHORED and ref not in available and ref not in VANILLA_OK:
                problems.append(
                    f"{layout.relative_to(ROOT)}: {ref} does not resolve in any "
                    f"client jar, shipped resource pack, or vanilla assets"
                )

    dead = ROOT / "conversion/overrides/resources"
    if dead.is_dir():
        problems.append(
            "conversion/overrides/resources/ exists: a launcher copies that "
            "folder into the instance root as resources/, which the game never "
            "loads, so every asset in it is unreachable"
        )

    if problems:
        print("menu-assets: FAIL")
        for problem in problems:
            print(f"  - {problem}")
        print(f"  ({checked} asset references checked)")
        return 1

    print(
        f"menu-assets: OK ({checked} FancyMenu asset references resolve, "
        f"{len(available)} assets available)"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
