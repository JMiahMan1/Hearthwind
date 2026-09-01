#!/usr/bin/env python3
"""Provision vanilla Minecraft client artifacts for headless client gametests.

Downloads (sha1-verified, cached) into an out dir:
  versions/26.2.json       - Mojang version json (CGT_VJSON)
  versions/26.2-client.jar - vanilla client jar (CGT_GAME_JAR)
  libraries/<artifact path>- linux-rule client libraries (CGT_LIBS_ROOT)
  assets/indexes/<id>.json - asset index (individual objects are fetched by the
                             game client itself from resources.download.minecraft.net)

The companion env vars are printed as shell exports on stdout so the caller can
`eval "$(provision_ci_client.py --out DIR)"` or source an env file.
"""

import argparse
import concurrent.futures
import hashlib
import json
import os
import pathlib
import sys
import urllib.request

MANIFEST_URL = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"
UA = "Hearthwind-CGT/0.1 (CI provisioning)"
RULE_OS = "linux"
NATIVES_CLASSIFIER = "natives-linux"


def rule_ok(rules):
    if not rules:
        return True
    allowed = False
    for r in rules:
        feats = r.get("features")
        if feats and not all(feats.values()):
            continue
        os_c = r.get("os", {})
        if os_c.get("name") not in (None, RULE_OS):
            continue
        allowed = r["action"] == "allow"
    return allowed


def sha1_of(path):
    h = hashlib.sha1()
    with open(path, "rb") as f:
        for chunk in iter(lambda: f.read(1 << 20), b""):
            h.update(chunk)
    return h.hexdigest()


def fetch(url, dest, expect_sha1=None, size_hint=0):
    dest.parent.mkdir(parents=True, exist_ok=True)
    if dest.exists() and (expect_sha1 is None or sha1_of(dest) == expect_sha1):
        return False
    if expect_sha1 is None and dest.exists() and dest.stat().st_size > 0:
        return False
    req = urllib.request.Request(url, headers={"User-Agent": UA})
    tmp = dest.with_suffix(dest.suffix + ".part")
    with urllib.request.urlopen(req, timeout=120) as resp, open(tmp, "wb") as out:
        while True:
            chunk = resp.read(1 << 20)
            if not chunk:
                break
            out.write(chunk)
    if expect_sha1:
        got = sha1_of(tmp)
        if got != expect_sha1:
            tmp.unlink(missing_ok=True)
            raise RuntimeError(f"sha1 mismatch for {url}: got {got} want {expect_sha1}")
    tmp.rename(dest)
    return True


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--out", required=True)
    ap.add_argument("--mc-version", default="26.2")
    args = ap.parse_args()
    out = pathlib.Path(args.out).resolve()
    (out / "versions").mkdir(parents=True, exist_ok=True)
    (out / "libraries").mkdir(parents=True, exist_ok=True)
    (out / "assets" / "indexes").mkdir(parents=True, exist_ok=True)

    manifest_cache = out / "version_manifest_v2.json"
    if fetch(MANIFEST_URL, manifest_cache):
        print("fetched version manifest", file=sys.stderr)
    manifest = json.loads(manifest_cache.read_text())
    entry = next((v for v in manifest["versions"] if v["id"] == args.mc_version), None)
    if entry is None:
        sys.exit(f"version {args.mc_version} not in manifest")

    vjson_path = out / "versions" / f"{args.mc_version}.json"
    fetch(entry["url"], vjson_path)
    vjson = json.loads(vjson_path.read_text())

    client = vjson["downloads"]["client"]
    game_jar = out / "versions" / f"{args.mc_version}-client.jar"
    fetch(client["url"], game_jar, client.get("sha1"), client.get("size", 0))
    print(f"client jar: {game_jar.name} ({game_jar.stat().st_size // 1024} KiB)", file=sys.stderr)

    dl_count = 0
    total = 0
    for lib in vjson.get("libraries", []):
        if not rule_ok(lib.get("rules")):
            continue
        downloads = lib.get("downloads", {})
        art = downloads.get("artifact")
        if art and art.get("url"):
            if fetch(art["url"], out / "libraries" / art["path"], art.get("sha1"), art.get("size", 0)):
                dl_count += 1
                total += art.get("size", 0)
        cls = downloads.get("classifiers", {}).get(NATIVES_CLASSIFIER)
        if cls and cls.get("url"):
            if fetch(cls["url"], out / "libraries" / cls["path"], cls.get("sha1"), cls.get("size", 0)):
                dl_count += 1
                total += cls.get("size", 0)
    print(f"libraries: {dl_count} downloaded this run", file=sys.stderr)

    ai = vjson["assetIndex"]
    asset_index = out / "assets" / "indexes" / f"{ai['id']}.json"
    fetch(ai["url"], asset_index, ai.get("sha1"))
    print(f"asset index: {ai['id']}.json ({asset_index.stat().st_size} bytes)", file=sys.stderr)

    # Download ALL asset objects: the vanilla client does not self-download
    # them (that is launcher/dev-env work) and missing objects break section
    # rendering in headless gametest runs.
    idx = json.loads(asset_index.read_text())
    base = "https://resources.download.minecraft.net"
    jobs = []
    for obj in idx.get("objects", {}).values():
        h = obj["hash"]
        jobs.append((f"{base}/{h[:2]}/{h}", out / "assets" / "objects" / h[:2] / h, h, obj.get("size", 0)))

    done = fetched = 0
    with concurrent.futures.ThreadPoolExecutor(max_workers=16) as pool:
        futures = {pool.submit(fetch, u, p, s, z): p for u, p, s, z in jobs}
        for fut in concurrent.futures.as_completed(futures):
            if fut.result():
                fetched += 1
            done += 1
    print(f"asset objects: {fetched} downloaded this run (of {len(jobs)})", file=sys.stderr)

    exports = {
        "CGT_VJSON": str(vjson_path),
        "CGT_GAME_JAR": str(game_jar),
        "CGT_LIBS_ROOT": str(out / "libraries"),
        "CGT_RULE_OS": RULE_OS,
        "CGT_NATIVES": NATIVES_CLASSIFIER,
        "CGT_ASSETS_DIR": str(out / "assets"),
    }
    print("\n".join(f'export {k}="{v}"' for k, v in exports.items()))


if __name__ == "__main__":
    main()
