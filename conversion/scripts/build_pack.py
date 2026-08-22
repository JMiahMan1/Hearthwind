#!/usr/bin/env python3
"""Build distributable artifacts from resolved dependency data.

Outputs (in conversion/dist):
  - modrinth.index.json          Modrinth pack format index
  - AgedServer-<ver>-mc<mc>.mrpack  installable server pack
  - server/<mod>.jar             plain mods dir (with --server-dir)

Usage:
  python3 conversion/scripts/build_pack.py [--server-dir]
"""
import argparse
import hashlib
import json
import shutil
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
CONF = ROOT / "conversion" / "build.conf.json"
BUILD = ROOT / "conversion" / "build"
DIST = BUILD / "dist"
UA = {"User-Agent": "aged-server-conversion/0.1"}


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--server-dir", action="store_true",
                    help="also materialize a plain server mods directory")
    args = ap.parse_args()

    conf = json.load(open(CONF))
    data = json.load(open(BUILD / "resolved.json"))
    mc = conf["targets"]["minecraft"]
    ready = [r for r in data["resolved"] if r["status"].startswith("ok")]
    print(f"Building pack for MC {mc}: {len(ready)} mods")

    files = []
    for r in ready:
        f = r["picked"]["file"]
        files.append({
            "path": "mods/" + f["filename"],
            "hashes": {
                "sha1": f["hashes"]["sha1"],
                "sha512": f["hashes"]["sha512"],
            },
            "env": {"client": "required", "server": "required"},
            "downloads": list(f["url"] if isinstance(f["url"], list) else [f["url"]]),
            "fileSize": f["size"],
        })

    index = {
        "formatVersion": 1,
        "game": "minecraft",
        "versionId": conf["pack"]["version"],
        "name": conf["pack"]["name"],
        "summary": conf["pack"]["summary"],
        "files": files,
        "dependencies": {
            "minecraft": mc,
            "fabric-loader": conf["targets"]["loader_version"],
        },
    }

    DIST.mkdir(parents=True, exist_ok=True)
    idx_path = DIST / "modrinth.index.json"
    json.dump(index, open(idx_path, "w"), indent=2)

    import zipfile
    mrpack = DIST / f"AgedServer-{conf['pack']['version']}-mc{mc}.mrpack"
    with zipfile.ZipFile(mrpack, "w", zipfile.ZIP_DEFLATED) as z:
        z.write(idx_path, "modrinth.index.json")
        ov = ROOT / "conversion" / "overrides"
        if ov.exists():
            for p in ov.rglob("*"):
                if p.is_file():
                    z.write(p, "overrides/" + str(p.relative_to(ov)))
    print(f"Wrote {mrpack.name} ({mrpack.stat().st_size // 1024} KiB, {len(files)} mods)")

    if args.server_dir:
        sdir = DIST / "server"
        (sdir / "mods").mkdir(parents=True, exist_ok=True)
        for r in ready:
            dest = sdir / "mods" / r["picked"]["file"]["filename"]
            if dest.exists() and hashlib.sha1(dest.read_bytes()).hexdigest() == r["picked"]["file"]["hashes"]["sha1"]:
                continue
            url = r["picked"]["file"]["url"]
            if isinstance(url, list):
                url = url[0]
            req = urllib.request.Request(url, headers=UA)
            with urllib.request.urlopen(req, timeout=120) as resp, open(dest, "wb") as out:
                shutil.copyfileobj(resp, out)
        print(f"Materialized {len(ready)} jars into {sdir/'mods'}")


if __name__ == "__main__":
    main()
