#!/usr/bin/env python3
"""Render the GitHub Release notes for a Hearthwind pack release.

The notes lead with WHAT TO DOWNLOAD and WHY, because the release ships
two .mrpack files and picking the wrong one costs a reinstall. Usage:

    python3 release_notes.py <version> [<pack-dir>]

Reads the packs that exist in ``<pack-dir>`` (default
``conversion/build/dist``) so the table can never drift from the real
assets, counts the Modrinth index entries and bundled override jars in
each, and prints the markdown to stdout.
"""

from __future__ import annotations

import json
import sys
import zipfile
from pathlib import Path

REPO = Path(__file__).resolve().parents[2]
DEFAULT_DIR = REPO / "conversion" / "build" / "dist"


def inspect(path: Path) -> tuple[int, int]:
    """Return (index entries, bundled override jars) for an mrpack."""
    with zipfile.ZipFile(path) as archive:
        index = json.loads(archive.read("modrinth.index.json"))
        overrides = [n for n in archive.namelist() if n.startswith("overrides/mods/")]
    return len(index.get("files", [])), len(overrides)


def main() -> int:
    version = sys.argv[1] if len(sys.argv) > 1 else ""
    if not version:
        print("usage: release_notes.py <version> [<pack-dir>]", file=sys.stderr)
        return 2
    pack_dir = Path(sys.argv[2]) if len(sys.argv) > 2 else DEFAULT_DIR

    conf = json.loads((REPO / "conversion" / "build.conf.json").read_text())
    mc = conf["targets"]["minecraft"]
    client = pack_dir / f"HearthwindClient-{version}-mc{mc}.mrpack"
    server = pack_dir / f"HearthwindServer-{version}-mc{mc}.mrpack"

    rows = []
    for name, path, audience, purpose in (
        (client, client, "players (singleplayer or joining a server)",
         "Everything a game client needs: the server mods plus the 26 client-only ones (HUD, menus, keybinds). "
         "This is the download for almost everyone."),
        (server, server, "dedicated-server owners only",
         "Server-side mods and the world datapack, without the client-only mods. Pick this one only if you run "
         "the game on a headless server box."),
    ):
        if not path.exists():
            continue
        files, overrides = inspect(path)
        rows.append((name.name, audience, purpose, files, overrides))

    lines = [
        f"# Hearthwind {version}",
        "",
        "## Download this file",
        "",
        f"**{rows[0][0]}** - {rows[0][1]}. {rows[0][2]}",
        "",
        f"*{rows[1][0]}* - {rows[1][1]}. {rows[1][2]}" if len(rows) > 1 else "",
        "",
        "| File | Who it is for | Modrinth index | Bundled jars |",
        "|---|---|---|---|",
    ]
    for name, audience, purpose, files, overrides in rows:
        lines.append(f"| `{name}` | {audience} | {files} | {overrides} |")
    lines += [
        "",
        "## Why there are two files",
        "",
        "`HearthwindClient-*.mrpack` is the download for almost everyone: it carries the",
        "server-side mods **and** the client-only mods (HUD, menus, keybinds) plus the",
        "world datapacks, config and resource packs, so it runs on its own for singleplayer",
        "and connects to our servers as-is.",
        "",
        "`HearthwindServer-*.mrpack` is for dedicated-server hosts only: the same",
        "server-side mods and world data, but without the client-only mods - a leaner",
        "download, and players joining that server do not receive mods they never use.",
        "Local singleplayer needs only the client pack.",
        "",
        "Both files are Modrinth `.mrpack` archives; the launcher downloads the index",
        "mods and installs the bundled jars:",
        "",
        "Prism Launcher > Add Instance > Import > pick the file > Install.",
        "",
        "Also in the archive: Aged 3.1.2's configuration, resource packs and world",
        "datapacks, so there is nothing else to download. Every release is verified",
        "before publishing (module jars present, index hashes and sizes correct,",
        "dependency map and overrides layout valid).",
    ]
    print("\n".join(line for line in lines if line is not None))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
