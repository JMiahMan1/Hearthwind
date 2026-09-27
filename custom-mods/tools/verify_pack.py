#!/usr/bin/env python3
"""Verify that a built or published .mrpack is complete and installable.

Catches the release bug where the packaging job ran without building the
modules first: build_pack.py then silently contributed zero
overrides/mods/*.jar, and every published pack was missing the whole
hearthwind + in-house port stack. Instance tooling could not catch it
because update_prism derives its "expected" set from the pack contents -
an absent jar is also an unexpectable jar. This checker knows what the
pack MUST contain.

Checks per pack:
  - readable zip with a valid modrinth.index.json (name, version, files)
  - every hearthwind-* module jar and every PORTED_MODULES jar present
    (hearthwind-client only in the client pack, banned from server packs)
  - at least MIN_OVERRIDE_JARS override jars (guards silent empty builds)
  - every embedded jar passes a zip integrity test (LOC-header corruption)

Usage:
  python3 custom-mods/tools/verify_pack.py                 # newest dist client pack
  python3 custom-mods/tools/verify_pack.py PACK.mrpack ...
  python3 custom-mods/tools/verify_pack.py --all           # all 3 packs for build.conf version
  python3 custom-mods/tools/verify_pack.py --release v0.1.10

Exit codes: 0 = OK, 2 = verification failure, 3 = setup error.
"""
import json
import subprocess
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DIST = ROOT / "conversion/build/dist"
BUILD_CONF = ROOT / "conversion/build.conf.json"
MIN_OVERRIDE_JARS = 40

SERVER_MODULES = (
    "hearthwind-survival",
    "hearthwind-skills",
    "hearthwind-jobs",
    "hearthwind-primitive",
    "hearthwind-world",
)
CLIENT_MODULES = ("hearthwind-client",)


def build_conf():
    conf = json.loads(BUILD_CONF.read_text())
    return conf["pack"]["version"], conf["targets"]["minecraft"]


def ported_modules():
    sys.path.insert(0, str(ROOT / "conversion/scripts"))
    import build_pack  # noqa: E402  (same-repo module)

    return tuple(build_pack.PORTED_MODULES)


def check_pack(path: Path) -> list[str]:
    """Return a list of human-readable problems ([] = complete)."""
    problems: list[str] = []
    if not path.is_file():
        return [f"{path}: file not found"]
    try:
        zf = zipfile.ZipFile(path)
    except zipfile.BadZipFile as exc:
        return [f"{path.name}: not a zip: {exc}"]

    names = zf.namelist()
    if "modrinth.index.json" not in names:
        return [f"{path.name}: no modrinth.index.json"]
    try:
        index = json.loads(zf.read("modrinth.index.json"))
    except json.JSONDecodeError as exc:
        return [f"{path.name}: bad modrinth.index.json: {exc}"]
    for key in ("name", "versionId", "files"):
        if key not in index:
            problems.append(f"{path.name}: index missing {key!r}")

    jars = sorted(n for n in names if n.startswith("overrides/mods/") and n.endswith(".jar"))
    if len(jars) < MIN_OVERRIDE_JARS:
        problems.append(
            f"{path.name}: only {len(jars)} override jars (expected >= {MIN_OVERRIDE_JARS})"
            " - were the modules built before packaging?")

    stems = [Path(n).name for n in jars]
    is_client = "client" in path.name.lower()

    for mod in SERVER_MODULES + ported_modules():
        if not any(s.startswith(mod + "-") for s in stems):
            problems.append(f"{path.name}: missing module jar {mod}-*")

    for mod in CLIENT_MODULES:
        present = any(s.startswith(mod + "-") for s in stems)
        if is_client and not present:
            problems.append(f"{path.name}: client pack missing {mod}-*")
        if not is_client and present:
            problems.append(f"{path.name}: server pack must not ship {mod}-*")

    for n in jars:
        try:
            if zipfile.ZipFile(zf.open(n)).testzip() is not None:
                problems.append(f"{path.name}: corrupt jar {Path(n).name}")
        except zipfile.BadZipFile:
            problems.append(f"{path.name}: corrupt jar {Path(n).name} (bad zip)")
    return problems


def newest_dist_client() -> Path:
    packs = list(DIST.glob("HearthwindClient-*-mc*.mrpack"))
    if not packs:
        sys.exit("error: no dist client pack; run build_pack.py --server-dir first")

    def version_key(p: Path) -> tuple[int, ...]:
        # "HearthwindClient-0.1.10-mc26.2.mrpack" -> (0, 1, 10)
        stem = p.name.split("-", 1)[1]
        ver = stem.split("-", 1)[0]
        return tuple(int(part) for part in ver.split(".") if part.isdigit())

    return max(packs, key=version_key)


def dist_packs_for_current_version() -> list[Path]:
    version, mc = build_conf()
    packs = [DIST / f"Hearthwind-{version}-mc{mc}.mrpack",
             DIST / f"HearthwindClient-{version}-mc{mc}.mrpack",
             DIST / f"HearthwindServer-{version}-mc{mc}.mrpack"]
    return packs


def release_packs(tag: str) -> list[Path]:
    out = ROOT / ".tmp/packs" / tag
    out.mkdir(parents=True, exist_ok=True)
    result = subprocess.run(
        ["gh", "release", "download", tag, "-D", str(out), "--clobber",
         "--pattern", "*.mrpack"],
        capture_output=True, text=True)
    if result.returncode != 0:
        sys.exit(f"error: gh release download {tag} failed: {result.stderr.strip()}")
    packs = sorted(out.glob("*.mrpack"))
    if not packs:
        sys.exit(f"error: release {tag} has no .mrpack assets")
    return packs


def main(argv: list[str]) -> int:
    args = argv[1:]
    if "--release" in args:
        i = args.index("--release")
        try:
            tag = args[i + 1]
        except IndexError:
            sys.exit("error: --release needs a tag")
        packs = release_packs(tag)
    elif "--all" in args:
        packs = dist_packs_for_current_version()
    elif args:
        packs = [Path(a) for a in args]
    else:
        packs = [newest_dist_client()]

    problems: list[str] = []
    for pack in packs:
        found = check_pack(pack)
        if found:
            problems.extend(found)
        else:
            size = pack.stat().st_size / 1048576
            with zipfile.ZipFile(pack) as zf:
                jars = [n for n in zf.namelist()
                        if n.startswith("overrides/mods/") and n.endswith(".jar")]
            print(f"verify_pack: OK {pack.name} ({size:.0f} MiB, "
                  f"{len(jars)} override jars, modules complete)")

    if problems:
        for p in problems:
            print(f"verify_pack: FAIL {p}")
        print(f"verify_pack: {len(problems)} problem(s) across {len(packs)} pack(s)")
        return 2
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
