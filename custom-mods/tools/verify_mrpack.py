#!/usr/bin/env python3
"""Deep validator for the Modrinth modpack format (mrpack) we publish.

`verify_pack.py` answers "does the pack contain the mods we ship?"; this one
answers "will a launcher be able to *load* this pack at all?", i.e. the
fabricmr modpack format rules:

* `modrinth.index.json` at the archive root with `formatVersion` 1,
  `game`, `versionId`, `name`, `files` and `dependencies`;
* every `files` entry declares `path`, `hashes.sha1` (40 hex),
  `hashes.sha512` (128 hex), `fileSize` > 0, a non-empty `downloads` list of
  https URLs and `env.client`/`env.server` in
  {required, optional, unsupported};
* `files[].path` values are unique and never collide with an `overrides/`
  entry (a launcher would install the same jar twice from two sources);
* `overrides/` only uses the top-level directories a launcher expects
  (`mods`, `config`, `resourcepacks`, `resourcepacks-disabled`, `shaderpacks`,
  `resources`, `world`, `client-overrides`, `server-overrides`) and every
  jar inside `overrides/mods` is a readable zip;
* no jar (override, or index file under `--deep`) needs a class file newer
  than the Java a launcher provisions for MC 26.2 (25 / class file 69) - a
  Java 26 mod crashes auto-provisioned launchers with
  `UnsupportedClassVersionError`;
* the archive itself is a readable zip without encrypted or absolute paths.

Usage:
    verify_mrpack.py                       # the newest dist client pack
    verify_mrpack.py PATH [PATH ...]       # specific packs
    verify_mrpack.py --all                 # the three packs for the config version
    verify_mrpack.py --release v0.1.23     # download a published release and check it
    verify_mrpack.py --deep PATH           # also download each index file and verify
                                           # its sha1/sha512/size (slow, network)

Exit codes: 0 ok, 2 problems found, 3 usage/IO error.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import re
import subprocess
import sys
import tempfile
import zipfile
from pathlib import Path

REPO = Path(__file__).resolve().parents[2]
HEX40 = re.compile(r"^[0-9a-f]{40}$")
HEX128 = re.compile(r"^[0-9a-f]{128}$")
ENV_VALUES = {"required", "optional", "unsupported"}
# Launchers resolve `dependencies` against loader elements only; anything
# else (a mod id, for instance) makes the Modrinth App refuse the pack.
LOADER_ELEMENTS = {"minecraft", "java", "fabric-loader", "quilt-loader", "forge", "neoforge"}
ALLOWED_OVERRIDE_ROOTS = {
    "mods",
    "config",
    "resourcepacks",
    "resourcepacks-disabled",
    "shaderpacks",
    "resources",
    "world",
    "client-overrides",
    "server-overrides",
    "saves",
    "kubejs",
}


# A launcher provisions the Java version Mojang ships in the 26.2 version
# manifest (Java 25), so no jar in the pack may need a newer class file than
# that.  A Java-26 (class 70) mod in overrides/ crash installs on every
# auto-provisioned launcher with UnsupportedClassVersionError.
MAX_CLASS_MAJOR = 69
JAVA_FOR_MAX_CLASS = MAX_CLASS_MAJOR - 44


def _max_class_major(jar: zipfile.ZipFile) -> int:
    """Highest class-file major version among the jar's .class entries."""
    highest = 0
    for info in jar.infolist():
        if not info.filename.endswith(".class"):
            continue
        # 8-byte header: magic 0xCAFEBABE (4 bytes) + minor (2) + major (2).
        with jar.open(info) as handle:
            if len(handle.read(8)) < 8:
                continue
            handle.seek(6)
            highest = max(highest, int.from_bytes(handle.read(2), "big"))
    return highest


def _fail(problems: list[str], message: str) -> None:
    problems.append(message)


def check_pack(path: Path, problems: list[str]) -> dict:
    """Validate one .mrpack; appends human readable problems."""
    try:
        archive = zipfile.ZipFile(path)
    except zipfile.BadZipFile as exc:
        _fail(problems, f"{path.name}: not a readable zip ({exc})")
        return {}
    with archive:
        broken = archive.testzip()
        if broken is not None:
            _fail(problems, f"{path.name}: corrupt entry {broken}")
        names = archive.namelist()
        for name in names:
            if name.startswith("/") or "\\" in name or ".." in name.split("/"):
                _fail(problems, f"{path.name}: unsafe archive path {name}")
        if "modrinth.index.json" not in names:
            _fail(problems, f"{path.name}: modrinth.index.json missing")
            return {}
        try:
            index = json.loads(archive.read("modrinth.index.json"))
        except json.JSONDecodeError as exc:
            _fail(problems, f"{path.name}: modrinth.index.json is not valid JSON ({exc})")
            return {}

        if index.get("formatVersion") != 1:
            _fail(problems, f"{path.name}: formatVersion must be 1, got {index.get('formatVersion')!r}")
        if index.get("game") != "minecraft":
            _fail(problems, f"{path.name}: game must be 'minecraft', got {index.get('game')!r}")
        for key in ("name", "versionId"):
            if not isinstance(index.get(key), str) or not index[key].strip():
                _fail(problems, f"{path.name}: index {key} must be a non-empty string")

        dependencies = index.get("dependencies")
        if not isinstance(dependencies, dict) or not dependencies:
            _fail(problems, f"{path.name}: dependencies must be a non-empty object")
        else:
            for key, value in dependencies.items():
                if not isinstance(value, str) or not value.strip():
                    _fail(problems, f"{path.name}: dependency {key} has an invalid version {value!r}")
            if "minecraft" not in dependencies:
                _fail(problems, f"{path.name}: dependencies are missing 'minecraft'")
            # Launchers validate this map against the known loader elements;
            # a mod id here makes the Modrinth App refuse the whole pack.
            for key in dependencies:
                if key not in LOADER_ELEMENTS:
                    _fail(problems, f"{path.name}: dependency '{key}' is not a launcher element "
                                    f"(allowed: {', '.join(sorted(LOADER_ELEMENTS))})")

        files = index.get("files")
        if not isinstance(files, list) or not files:
            _fail(problems, f"{path.name}: index has no files")
            files = []
        overrides = {n for n in names if n.startswith("overrides/")}
        seen_paths: set[str] = set()
        for entry in files:
            if not isinstance(entry, dict):
                _fail(problems, f"{path.name}: file entry is not an object: {entry!r}")
                continue
            jar = entry.get("path", "?")
            if not isinstance(jar, str) or not jar.startswith("mods/") or not jar.endswith(".jar"):
                _fail(problems, f"{path.name}: file path must be a mods/*.jar entry, got {jar!r}")
            if jar in seen_paths:
                _fail(problems, f"{path.name}: duplicate file path {jar}")
            seen_paths.add(jar)
            if f"overrides/{jar}" in overrides:
                _fail(problems, f"{path.name}: {jar} is both an index file and an override")
            hashes = entry.get("hashes")
            if not isinstance(hashes, dict):
                _fail(problems, f"{path.name}: {jar} has no hashes")
            else:
                if not isinstance(hashes.get("sha1"), str) or not HEX40.match(hashes.get("sha1", "")):
                    _fail(problems, f"{path.name}: {jar} has a malformed sha1")
                if not isinstance(hashes.get("sha512"), str) or not HEX128.match(hashes.get("sha512", "")):
                    _fail(problems, f"{path.name}: {jar} has a malformed sha512")
            size = entry.get("fileSize")
            if not isinstance(size, int) or size <= 0:
                _fail(problems, f"{path.name}: {jar} has an invalid fileSize {size!r}")
            downloads = entry.get("downloads")
            if not isinstance(downloads, list) or not downloads:
                _fail(problems, f"{path.name}: {jar} has no downloads")
            else:
                for url in downloads:
                    if not isinstance(url, str) or not url.startswith("https://"):
                        _fail(problems, f"{path.name}: {jar} has a non-https download {url!r}")
            env = entry.get("env")
            if not isinstance(env, dict):
                _fail(problems, f"{path.name}: {jar} has no env object")
            else:
                for side in ("client", "server"):
                    if env.get(side) not in ENV_VALUES:
                        _fail(problems,
                              f"{path.name}: {jar} env.{side}={env.get(side)!r} is not one of {sorted(ENV_VALUES)}")

        for name in overrides:
            parts = name.split("/")
            if len(parts) < 2 or parts[1] == "":
                _fail(problems, f"{path.name}: odd override entry {name}")
            elif parts[1] not in ALLOWED_OVERRIDE_ROOTS:
                _fail(problems, f"{path.name}: override folder {parts[1]!r} is not launcher-expected")
        for name in sorted(n for n in names if n.startswith("overrides/mods/") and n.endswith(".jar")):
            try:
                with zipfile.ZipFile(__import__("io").BytesIO(archive.read(name))) as jar:
                    if jar.testzip() is not None:
                        _fail(problems, f"{path.name}: override jar {name} is corrupt")
                    major = _max_class_major(jar)
                    if major > MAX_CLASS_MAJOR:
                        _fail(problems,
                              f"{path.name}: override jar {name} needs class file {major} "
                              f"(Java {major - 44}), but a launcher provisions Java {JAVA_FOR_MAX_CLASS} for "
                              f"MC 26.2 (class file {MAX_CLASS_MAJOR}); it would crash with "
                              "UnsupportedClassVersionError")
            except zipfile.BadZipFile as exc:
                _fail(problems, f"{path.name}: override jar {name} is not a zip ({exc})")
        return index


def deep_check(path: Path, index: dict, problems: list[str]) -> None:
    """Download every index file and verify its hashes and size."""
    for entry in index.get("files", []):
        url = entry["downloads"][0]
        jar = entry["path"]
        with tempfile.NamedTemporaryFile(suffix=".jar", delete=False) as tmp:
            target = Path(tmp.name)
        try:
            subprocess.run(["curl", "-sL", "--fail", "-o", str(target), url], check=True)
            data = target.read_bytes()
        except subprocess.CalledProcessError:
            _fail(problems, f"{path.name}: {jar} could not be downloaded from {url}")
            continue
        finally:
            target.unlink(missing_ok=True)
        if len(data) != entry["fileSize"]:
            _fail(problems, f"{path.name}: {jar} downloaded size {len(data)} != {entry['fileSize']}")
        if hashlib.sha1(data).hexdigest() != entry["hashes"]["sha1"]:
            _fail(problems, f"{path.name}: {jar} sha1 mismatch")
        if hashlib.sha512(data).hexdigest() != entry["hashes"]["sha512"]:
            _fail(problems, f"{path.name}: {jar} sha512 mismatch")
        try:
            with zipfile.ZipFile(__import__("io").BytesIO(data)) as downloaded:
                major = _max_class_major(downloaded)
            if major > MAX_CLASS_MAJOR:
                _fail(problems,
                      f"{path.name}: index jar {jar} needs class file {major} (Java {major - 44}), "
                      f"but a launcher provisions Java {JAVA_FOR_MAX_CLASS} for MC 26.2; it would crash "
                      "with UnsupportedClassVersionError")
        except zipfile.BadZipFile as exc:
            _fail(problems, f"{path.name}: index jar {jar} is not a zip ({exc})")


def dist_packs(only_client: bool = False) -> list[Path]:
    dist = REPO / "conversion" / "build" / "dist"
    config = json.loads((REPO / "conversion" / "build.conf.json").read_text())
    # build.conf keeps the version under pack.version and the game id under
    # targets.minecraft; the pack file names embed both.
    version = config["pack"]["version"]
    mc = config["targets"]["minecraft"]
    patterns = [f"HearthwindServer-{version}-mc{mc}.mrpack", f"HearthwindClient-{version}-mc{mc}.mrpack"]
    if only_client:
        patterns = [f"HearthwindClient-{version}-mc{mc}.mrpack"]
    found = [dist / p for p in patterns if (dist / p).exists()]
    if not found:
        found = sorted(dist.glob("*.mrpack"))
    return found


def download_release(tag: str) -> list[Path]:
    target = REPO / ".tmp" / "packs" / tag
    target.mkdir(parents=True, exist_ok=True)
    subprocess.run(
        ["gh", "release", "download", tag, "-D", str(target), "--clobber", "--pattern", "*.mrpack"],
        check=True,
    )
    return sorted(target.glob("*.mrpack"))


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("packs", nargs="*", type=Path)
    parser.add_argument("--all", action="store_true", help="check the three packs of the current version")
    parser.add_argument("--release", metavar="TAG", help="download a published release and check it")
    parser.add_argument("--deep", action="store_true", help="also download every index file and verify hashes")
    args = parser.parse_args()

    if args.release:
        packs = download_release(args.release)
    elif args.all:
        packs = dist_packs()
    elif args.packs:
        packs = list(args.packs)
    else:
        newest = sorted((REPO / "conversion" / "build" / "dist").glob("HearthwindClient-*.mrpack"))
        packs = [newest[-1]] if newest else []
    if not packs:
        print("verify_mrpack: no packs found", file=sys.stderr)
        return 3

    problems: list[str] = []
    counts: list[tuple[Path, int]] = []
    for pack in packs:
        index = check_pack(pack, problems)
        counts.append((pack, len((index or {}).get("files", []))))
        if args.deep and index:
            deep_check(pack, index, problems)
    if problems:
        print(f"verify_mrpack: FAIL ({len(problems)} problems)")
        for problem in problems:
            print(f"  - {problem}")
        return 2
    for pack, count in counts:
        print(f"verify_mrpack: OK {pack.name} ({pack.stat().st_size // (1024 * 1024)} MiB, "
              f"{count} index files)"
              + (" [deep verified]" if args.deep else ""))
    return 0


if __name__ == "__main__":
    sys.exit(main())
