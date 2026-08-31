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
    ready_server = [r for r in ready if not r.get("client_only")]
    ready_client_only = [r for r in ready if r.get("client_only")]
    print(
        f"Building pack for MC {mc}: {len(ready)} mods "
        f"({len(ready_server)} server, {len(ready_client_only)} client-only)"
    )

    def non_index_jars(mods_dir: Path, index_names: set):
        """Locally-built jars (custom mods + vendored) that the Modrinth
        index cannot provide - these must ship as mrpack overrides/mods."""
        if not mods_dir.exists():
            return []
        return [j for j in sorted(mods_dir.glob("*.jar")) if j.name not in index_names]

    def index_file(r, client_env):
        f = r["picked"]["file"]
        return {
            "path": "mods/" + f["filename"],
            "hashes": {
                "sha1": f["hashes"]["sha1"],
                "sha512": f["hashes"]["sha512"],
            },
            "env": client_env,
            "downloads": list(
                f["url"] if isinstance(f["url"], list) else [f["url"]]
            ),
            "fileSize": f["size"],
        }

    files = [index_file(r, {"client": "required", "server": "required"}) for r in ready_server]

    # Server pack: server required, client optional (vanilla join supported)
    indexed_server = {Path(f["path"]).name for f in files}
    server_files = [{**f, "env": {"client": "optional", "server": "required"}} for f in files]
    # Client pack: everything (server mods + client-only visual mods),
    # client required / server unsupported (standalone singleplayer pack)
    client_files = [index_file(r, {"client": "required", "server": "unsupported"}) for r in ready]
    indexed_client = {Path(f["path"]).name for f in client_files}
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

    datapack = ROOT / "conversion" / "datapacks" / "hearthwind"
    if not (datapack / "pack.mcmeta").exists():
        raise SystemExit(
            "conversion/datapacks/hearthwind/pack.mcmeta missing - "
            "run conversion/scripts/migrate_datapack.py first"
        )

    with zipfile.ZipFile(mrpack, "w", zipfile.ZIP_DEFLATED) as z:
        z.write(idx_path, "modrinth.index.json")
        for p in sorted(datapack.rglob("*")):
            if p.is_file():
                z.write(
                    p,
                    "overrides/world/datapacks/hearthwind/"
                    + str(p.relative_to(datapack)),
                )
        ov = ROOT / "conversion" / "overrides"
        if ov.exists():
            for p in ov.rglob("*"):
                if p.is_file():
                    z.write(p, "overrides/" + str(p.relative_to(ov)))
        local = non_index_jars(DIST / "server" / "mods", indexed_server)
        for j in local:
            z.write(j, "overrides/mods/" + j.name)
    # legacy alias
    shutil.copy(mrpack, mrpack_legacy)
    # also copy to conversion/dist for legacy tooling
    for p in [mrpack, mrpack_legacy, idx_path]:
        shutil.copy(p, DIST_LEGACY / p.name)
    print(
        f"Wrote {mrpack.name} ({mrpack.stat().st_size // 1024} KiB, {len(server_files)} mods + {len(non_index_jars(DIST / 'server' / 'mods', indexed_server))} override jars)"
    )

    # ---- Client companion pack (optional HUD) ----
    # For now the client pack is a thin overlay: same index but with client
    # files marked required and a note to drop hearthwind-client jar.
    # We also generate a ready-to-unzip client mods folder.
    # Client pack: all mods client-required (server+client-only), standalone
    client_index = dict(index)
    client_index["name"] = conf["pack"]["name"] + " Client"
    client_index["files"] = client_files
    client_idx_path = DIST / "modrinth.client.index.json"
    json.dump(client_index, open(client_idx_path, "w"), indent=2)
    client_mrpack = DIST / f"{slug.title()}Client-{ver}-mc{mc}.mrpack"
    resourcepacks = ROOT / "conversion" / "vendored" / "resourcepacks"
    with zipfile.ZipFile(client_mrpack, "w", zipfile.ZIP_DEFLATED) as z:
        # reuse server datapack as overrides - client needs same world compat
        z.write(client_idx_path, "modrinth.index.json")
        for p in sorted(datapack.rglob("*")):
            if p.is_file():
                z.write(p, "overrides/world/datapacks/hearthwind/" + str(p.relative_to(datapack)))
        if resourcepacks.is_dir():
            for p in sorted(resourcepacks.glob("*.zip")):
                z.write(p, "overrides/resourcepacks/" + p.name)
        local_c = non_index_jars(DIST / "client" / "mods", indexed_client)
        for j in local_c:
            z.write(j, "overrides/mods/" + j.name)
    shutil.copy(client_mrpack, DIST_LEGACY / client_mrpack.name)
    print(
        f"Wrote {client_mrpack.name} ({client_mrpack.stat().st_size // 1024} KiB, {len(non_index_jars(DIST / 'client' / 'mods', indexed_client))} override jars, {len(list(resourcepacks.glob('*.zip'))) if resourcepacks.is_dir() else 0} resourcepacks) - client companion (client-required)"
    )

    if args.server_dir:
        sdir = DIST / "server"
        cdir = DIST / "client"
        (sdir / "mods").mkdir(parents=True, exist_ok=True)
        (cdir / "mods").mkdir(parents=True, exist_ok=True)
        for r in ready_server:
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
        # Client-only visual mods (EMF/ETF etc.) go to the client dir ONLY
        for r in ready_client_only:
            dest = cdir / "mods" / r["picked"]["file"]["filename"]
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
        # Vendored jars (e.g. locally patched YUNG 26.2 builds) go to both dirs
        for j in sorted((ROOT / "conversion" / "vendored").glob("*.jar")):
            if "sources" in j.name or "javadoc" in j.name:
                continue
            shutil.copy(j, sdir / "mods" / j.name)
            shutil.copy(j, cdir / "mods" / j.name)
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
        wdp = sdir / "world" / "datapacks" / "hearthwind"
        shutil.rmtree(wdp, ignore_errors=True)
        shutil.copytree(datapack, wdp)
        # client needs same datapack when hosting via client (singleplayer)
        wdp_c = cdir / "world" / "datapacks" / "hearthwind"
        shutil.rmtree(wdp_c, ignore_errors=True)
        shutil.copytree(datapack, wdp_c)
        # Client resourcepacks (e.g. Fresh Animations) ship with the client dist
        if resourcepacks.is_dir():
            (cdir / "resourcepacks").mkdir(parents=True, exist_ok=True)
            for p in sorted(resourcepacks.glob("*.zip")):
                shutil.copy(p, cdir / "resourcepacks" / p.name)
        # Prune stale resolution leftovers: keep only jars the index covers
        # plus vendored/custom jars actually staged above
        vendored_names = {
            j.name
            for j in (ROOT / "conversion" / "vendored").glob("*.jar")
            if "sources" not in j.name and "javadoc" not in j.name
        }
        custom_names = {j.name for j in custom_jars}
        server_custom_names = {j.name for j in custom_jars if "hearthwind-client" not in j.name}
        for d, allowed in (
            (sdir / "mods", indexed_server | vendored_names | server_custom_names),
            (cdir / "mods", indexed_client | vendored_names | custom_names),
        ):
            for j in sorted(d.glob("*.jar")):
                if j.name not in allowed:
                    print(f"  prune stale jar: {d.name}/{j.name}")
                    j.unlink()
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
