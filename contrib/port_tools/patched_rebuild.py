#!/usr/bin/env python3
"""Rebuild a vendored contrib jar from upstream + our 26.2 patch.

Pipeline: fetch the pinned upstream jar, unpack it into a git repo, apply
contrib/<mod>/patches/26.2-port.patch (may be --binary: it can carry
compiled-class payloads), repack deterministically.

Usage:
  patched_rebuild.py URL OUT_JAR PATCH_FILE
  patched_rebuild.py URL OUT_JAR --no-patch   (metadata-only rename, e.g. true-ending)

The output jar uses fixed timestamps and sorted entries so rebuilds are
byte-reproducible for identical inputs.
"""
import argparse
import hashlib
import io
import os
import subprocess
import sys
import tempfile
import urllib.request
import zipfile

CACHE_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), ".upstream-cache")

ZIP_DATE = (2026, 1, 1, 0, 0, 0)


def fetch(url: str) -> str:
    os.makedirs(CACHE_DIR, exist_ok=True)
    dest = os.path.join(CACHE_DIR, hashlib.sha1(url.encode()).hexdigest() + "-" + os.path.basename(url))
    if os.path.exists(dest):
        print(f"cached: {dest}")
        return dest
    req = urllib.request.Request(url, headers={"User-Agent": "hearthwind-contrib/0.1"})
    with urllib.request.urlopen(req) as r, open(dest, "wb") as f:
        f.write(r.read())
    print(f"downloaded: {dest}")
    return dest


def run(cmd, cwd=None):
    r = subprocess.run(cmd, cwd=cwd, capture_output=True, text=True)
    if r.returncode != 0:
        sys.stderr.write(r.stdout + "\n" + r.stderr + "\n")
        raise SystemExit(f"command failed: {' '.join(cmd)}")
    return r.stdout


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("url")
    ap.add_argument("out_jar")
    ap.add_argument("patch", nargs="?")
    ap.add_argument("--no-patch", action="store_true", help="repack upstream unchanged")
    args = ap.parse_args()
    if not args.no_patch and not args.patch:
        ap.error("need PATCH_FILE or --no-patch")

    upstream = fetch(args.url)

    with tempfile.TemporaryDirectory(prefix="contrib262-") as tmp:
        tree = os.path.join(tmp, "tree")
        with zipfile.ZipFile(upstream) as z:
            z.extractall(tree)
        run(["git", "init", "-q"], cwd=tree)
        run(["git", "add", "-A", "-f"], cwd=tree)
        run(["git", "-c", "user.email=port@hearthwind", "-c", "user.name=port",
             "commit", "-qm", "upstream"], cwd=tree)
        if not args.no_patch:
            patch = os.path.abspath(args.patch)
            run(["git", "apply", "--binary", "--whitespace=nowarn", patch], cwd=tree)

        entries = []
        for root, dirs, files in os.walk(tree):
            dirs[:] = [d for d in dirs if d != ".git"]
            for name in files:
                full = os.path.join(root, name)
                rel = os.path.relpath(full, tree).replace(os.sep, "/")
                entries.append((rel, full))
        entries.sort()
        out = os.path.abspath(args.out_jar)
        os.makedirs(os.path.dirname(out), exist_ok=True)
        with zipfile.ZipFile(out, "w", zipfile.ZIP_DEFLATED) as z:
            for rel, full in entries:
                info = zipfile.ZipInfo(rel, date_time=ZIP_DATE)
                info.compress_type = zipfile.ZIP_DEFLATED
                info.external_attr = 0o644 << 16
                z.writestr(info, open(full, "rb").read())
        print(f"built: {out} ({os.path.getsize(out)} bytes, {len(entries)} entries)")


if __name__ == "__main__":
    main()
