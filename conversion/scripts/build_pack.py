#!/usr/bin/env python3
"""Build distributable artifacts from resolved dependency data.

Outputs (in conversion/dist and conversion/build/dist):
  - modrinth.index.json          Modrinth pack format index
  - Hearthwind-<ver>-mc<mc>.mrpack        installable server pack (server required, client optional -> vanilla join)
  - HearthwindClient-<ver>-mc<mc>.mrpack  optional client HUD pack (client required, server unsupported)
  - server/<mod>.jar             plain server mods dir (with --server-dir)
  - client/mods/<mod>.jar        plain client mods dir (hearthwind-client)

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
# Legacy DIST for back-compat (conversion/dist) + canonical build/dist
DIST = BUILD / "dist"
DIST_LEGACY = ROOT / "conversion" / "dist"
UA = {"User-Agent": "hearthwind/0.1 (github.com/JMiahMan1/Hearthwind)"}


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument(
        "--server-dir",
        action="store_true",
        help="also materialize a plain server mods directory",
    )
    args = ap.parse_args()

    conf = json.load(open(CONF))
    data = json.load(open(BUILD / "resolved.json"))
    mc = conf["targets"]["minecraft"]
    ready = [r for r in data["resolved"] if r["status"].startswith("ok")]
    print(f"Building pack for MC {mc}: {len(ready)} mods")

    files = []
    for r in ready:
        f = r["picked"]["file"]
        files.append(
            {
                "path": "mods/" + f["filename"],
                "hashes": {
                    "sha1": f["hashes"]["sha1"],
                    "sha512": f["hashes"]["sha512"],
                },
                "env": {"client": "required", "server": "required"},
                "downloads": list(
                    f["url"] if isinstance(f["url"], list) else [f["url"]]
                ),
                "fileSize": f["size"],
            }
        )

    # Server pack: server required, client optional (vanilla join supported)
    server_files = [{**f, "env": {"client": "optional", "server": "required"}} for f in files]
    # Client pack will be built by layering hearthwind-client jar on top of same file set
    # but with client required / server unsupported
    index = {
        "formatVersion": 1,
        "game": "minecraft",
        "versionId": conf["pack"]["version"],
        "name": conf["pack"]["name"],
        "summary": conf["pack"]["summary"],
        "files": server_files,
        "dependencies": {
            "minecraft": mc,
            "fabric-loader": conf["targets"]["loader_version"],
        },
    }

    DIST.mkdir(parents=True, exist_ok=True)
    DIST_LEGACY.mkdir(parents=True, exist_ok=True)
    idx_path = DIST / "modrinth.index.json"
    json.dump(index, open(idx_path, "w"), indent=2)
    # also mirror to legacy path so old docs/scripts keep working
    json.dump(index, open(DIST_LEGACY / "modrinth.index.json", "w"), indent=2)

    import zipfile

    slug = conf["pack"]["slug"]  # hearthwind
    ver = conf["pack"]["version"]
    mrpack = DIST / f"{slug.title()}-{ver}-mc{mc}.mrpack"  # Hearthwind-0.1.0-mc26.2.mrpack
    mrpack_legacy = DIST / f"HearthwindServer-{ver}-mc{mc}.mrpack"  # back-compat alias

    datapack = ROOT / "conversion" / "datapacks" / "aged-server"
    if not (datapack / "pack.mcmeta").exists():
        raise SystemExit(
            "conversion/datapacks/aged-server/pack.mcmeta missing — "
            "run conversion/scripts/migrate_datapack.py first"
        )

    with zipfile.ZipFile(mrpack, "w", zipfile.ZIP_DEFLATED) as z:
        z.write(idx_path, "modrinth.index.json")
        for p in sorted(datapack.rglob("*")):
            if p.is_file():
                z.write(
                    p,
                    "overrides/world/datapacks/aged-server/"
                    + str(p.relative_to(datapack)),
                )
        ov = ROOT / "conversion" / "overrides"
        if ov.exists():
            for p in ov.rglob("*"):
                if p.is_file():
                    z.write(p, "overrides/" + str(p.relative_to(ov)))
    # legacy alias
    shutil.copy(mrpack, mrpack_legacy)
    # also copy to conversion/dist for legacy tooling
    for p in [mrpack, mrpack_legacy, idx_path]:
        shutil.copy(p, DIST_LEGACY / p.name)
    print(
        f"Wrote {mrpack.name} ({mrpack.stat().st_size // 1024} KiB, {len(server_files)} mods) + legacy {mrpack_legacy.name}"
    )

    # ---- Client companion pack (optional HUD) ----
    # For now the client pack is a thin overlay: same index but with client
    # files marked required and a note to drop hearthwind-client jar.
    # We also generate a ready-to-unzip client mods folder.
    client_index = dict(index)
    client_index["name"] = conf["pack"]["name"] + " Client"
    client_index["files"] = [{**f, "env": {"client": "required", "server": "unsupported"}} for f in files]
    client_idx_path = DIST / "modrinth.client.index.json"
    json.dump(client_index, open(client_idx_path, "w"), indent=2)
    client_mrpack = DIST / f"{slug.title()}Client-{ver}-mc{mc}.mrpack"
    with zipfile.ZipFile(client_mrpack, "w", zipfile.ZIP_DEFLATED) as z:
        # reuse server datapack as overrides — client needs same world compat
        z.write(client_idx_path, "modrinth.index.json")
        for p in sorted(datapack.rglob("*")):
            if p.is_file():
                z.write(p, "overrides/world/datapacks/aged-server/" + str(p.relative_to(datapack)))
    shutil.copy(client_mrpack, DIST_LEGACY / client_mrpack.name)
    print(f"Wrote {client_mrpack.name} ({client_mrpack.stat().st_size // 1024} KiB) — client companion (same files, client-required)")

    if args.server_dir:
        sdir = DIST / "server"
        cdir = DIST / "client"
        (sdir / "mods").mkdir(parents=True, exist_ok=True)
        (cdir / "mods").mkdir(parents=True, exist_ok=True)
        for r in ready:
            dest = sdir / "mods" / r["picked"]["file"]["filename"]
            if (
                dest.exists()
                and hashlib.sha1(dest.read_bytes()).hexdigest()
                == r["picked"]["file"]["hashes"]["sha1"]
            ):
                continue
            url = r["picked"]["file"]["url"]
            if isinstance(url, list):
                url = url[0]
            req = urllib.request.Request(url, headers=UA)
            with (
                urllib.request.urlopen(req, timeout=120) as resp,
                open(dest, "wb") as out,
            ):
                shutil.copyfileobj(resp, out)
        # Mirror server mods to client mods for now (client can install same set
        # plus its own companion jar; actual Modrinth env filtering happens on import)
        for p in (sdir / "mods").glob("*.jar"):
            shutil.copy(p, cdir / "mods" / p.name)
        # Also copy our custom jars into the plain dirs for offline installs
        custom_jars = list((ROOT / "custom-mods").rglob("hearthwind-*/build/libs/*26.2*.jar"))
        custom_jars = [j for j in custom_jars if "sources" not in j.name]
        for j in custom_jars:
            is_client = "hearthwind-client" in str(j)
            target_dir = cdir / "mods" if is_client else sdir / "mods"
            # server gets all but client; client gets client + shared deps
            if is_client:
                shutil.copy(j, cdir / "mods" / j.name)
            else:
                shutil.copy(j, sdir / "mods" / j.name)
                # also copy server jars to client mods so client can run single-player
                shutil.copy(j, cdir / "mods" / j.name)
        wdp = sdir / "world" / "datapacks" / "aged-server"
        shutil.rmtree(wdp, ignore_errors=True)
        shutil.copytree(datapack, wdp)
        # client needs same datapack when hosting via client (singleplayer)
        wdp_c = cdir / "world" / "datapacks" / "aged-server"
        shutil.rmtree(wdp_c, ignore_errors=True)
        shutil.copytree(datapack, wdp_c)
        # Mirror to legacy location too
        sdir_legacy = DIST_LEGACY / "server"
        cdir_legacy = DIST_LEGACY / "client"
        shutil.rmtree(sdir_legacy, ignore_errors=True)
        shutil.rmtree(cdir_legacy, ignore_errors=True)
        shutil.copytree(sdir, sdir_legacy)
        shutil.copytree(cdir, cdir_legacy)
        print(f"Materialized {len(ready)} jars + {len(custom_jars)} custom into {sdir / 'mods'} (server) and {cdir / 'mods'} (client) + world datapacks")


if __name__ == "__main__":
    main()
