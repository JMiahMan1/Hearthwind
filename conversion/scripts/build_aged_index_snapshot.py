#!/usr/bin/env python3
"""Freeze Aged's Modrinth index into a deterministic, committed snapshot.

The Aged parity ledger (W0 in docs/RELEASE_1.0_PARITY.md) needs a stable
reference for "what Aged 3.1.2 actually shipped" that does not depend on a
scratch .mrpack file being present. This script reads a Modrinth pack and
writes every indexed artifact (mods, resource packs, shader packs) keyed by
its Modrinth project id, version id and content hashes.

Usage:
  python3 conversion/scripts/build_aged_index_snapshot.py
  python3 conversion/scripts/build_aged_index_snapshot.py --mrpack .tmp/Aged-3.1.2.mrpack
"""

import argparse
import json
import re
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DEFAULT_MRPACK = ROOT / ".tmp" / "Aged-3.1.2.mrpack"
DEFAULT_OUTPUT = ROOT / "conversion" / "curated" / "aged-3.1.2-index.json"

SOURCE = "Aged 3.1.2"
GENERATED_BY = "conversion/scripts/build_aged_index_snapshot.py"

KIND_BY_PREFIX = {
    "mods": "mod",
    "resourcepacks": "resourcepack",
    "shaderpacks": "shaderpack",
}

# .../data/<project_id>/versions/<version_id>/<filename>
DOWNLOAD_RE = re.compile(r"/data/(?P<project_id>[^/]+)/versions/(?P<version_id>[^/]+)/")


def parse_download(url):
    m = DOWNLOAD_RE.search(url or "")
    if not m:
        return None, None
    return m.group("project_id"), m.group("version_id")


def build_snapshot(mrpack):
    with zipfile.ZipFile(mrpack) as zf:
        index = json.loads(zf.read("modrinth.index.json"))

    files = []
    for entry in index.get("files", []):
        path = entry["path"]
        top = path.split("/", 1)[0]
        kind = KIND_BY_PREFIX.get(top)
        if kind is None:
            print(f"skip non-modpack file: {path}", file=sys.stderr)
            continue
        downloads = entry.get("downloads") or []
        project_id, version_id = parse_download(downloads[0] if downloads else "")
        if project_id is None:
            print(f"skip unkeyable download: {path}", file=sys.stderr)
            continue
        hashes = entry.get("hashes", {})
        files.append(
            {
                "path": path,
                "kind": kind,
                "project_id": project_id,
                "version_id": version_id,
                "filename": path.rsplit("/", 1)[-1],
                "sha1": hashes.get("sha1"),
                "sha512": hashes.get("sha512"),
            }
        )

    files.sort(key=lambda f: f["path"])
    return {"source": SOURCE, "generated_by": GENERATED_BY, "files": files}


def main():
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("--mrpack", default=str(DEFAULT_MRPACK), help="Modrinth pack to read")
    ap.add_argument("--output", default=str(DEFAULT_OUTPUT), help="snapshot JSON to write")
    args = ap.parse_args()

    mrpack = Path(args.mrpack)
    if not mrpack.is_file():
        print(f"error: mrpack not found: {mrpack}", file=sys.stderr)
        return 1

    snapshot = build_snapshot(mrpack)
    output = Path(args.output)
    output.parent.mkdir(parents=True, exist_ok=True)
    with open(output, "w", encoding="utf-8") as fh:
        json.dump(snapshot, fh, indent=2, ensure_ascii=False)
        fh.write("\n")

    kinds = {}
    for f in snapshot["files"]:
        kinds[f["kind"]] = kinds.get(f["kind"], 0) + 1
    summary = ", ".join(f"{k} {v}" for k, v in sorted(kinds.items()))
    print(f"wrote {output} ({len(snapshot['files'])} files: {summary})")
    return 0


if __name__ == "__main__":
    sys.exit(main())
