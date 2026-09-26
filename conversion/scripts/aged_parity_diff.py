#!/usr/bin/env python3
"""Machine-check the Aged 3.1.2 parity ledger AND the built pack contents.

Part 1 (manifest accounting) compares the frozen Aged index snapshot
(conversion/curated/aged-3.1.2-index.json) against the curated manifest
(conversion/curated/mods-manifest.json) and the explicit debt ledger
(conversion/curated/aged-missing-allowlist.json). An Aged mod is accounted
for when its Modrinth project id is keyed on a manifest entry or its pid is
listed in the allowlist as tracked debt.

Part 2 (pack realization, the part that used to be missing) checks the
*built* pack: conversion/build/resolved.json plus
conversion/build/dist/{server,client}/mods. Every accounted Aged mod must
actually ship as a jar, and every jar in those trees must be traceable to an
Aged project id, a resolver-auto-added dependency, or a Hearthwind module.
This is what catches "declared keep in the manifest but the pack lacks it".

Manifest rules enforced:
  * every entry is keyed to an Aged project id, except entries whose action
    is `drop` (documented exclusions) or `dependency` (non-Aged runtime
    libraries such as lithostitched).

Exit 1 on any unaccounted Aged mod, drop-mapped Aged mod, manifest rule
violation, declared-but-not-shipped mod, or unattributable dist jar.

Usage:
  python3 conversion/scripts/aged_parity_diff.py [--no-pack]
"""

import argparse
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SNAPSHOT = ROOT / "conversion" / "curated" / "aged-3.1.2-index.json"
MANIFEST = ROOT / "conversion" / "curated" / "mods-manifest.json"
ALLOWLIST = ROOT / "conversion" / "curated" / "aged-missing-allowlist.json"
RESOLVED = ROOT / "conversion" / "build" / "resolved.json"
DIST_SERVER = ROOT / "conversion" / "build" / "dist" / "server" / "mods"
DIST_CLIENT = ROOT / "conversion" / "build" / "dist" / "client" / "mods"
VENDORED = ROOT / "conversion" / "vendored"
CUSTOM_MODS = ROOT / "custom-mods"
# The audit report is scratch input for canonical slug/jar-name hints only;
# CI does not need it because every shipped Aged mod is keyed by project_id.
REPORT_HINTS = ROOT / ".tmp" / "k3_parity_audit.md"

ACTIONS = {"keep", "rebuild", "client-optional", "add", "dependency", "drop"}
# In-house 26.2 ports of third-party mods (mirror of build_pack.PORTED_MODULES).
PORTED_MODULES = (
    "villagesandpillages", "letsdo-vinery", "letsdo-meadow", "letsdo-bakery",
    "letsdo-candlelight", "letsdo-brewery", "letsdo-herbalbrews",
    "letsdo-farm-and-charm", "letsdo-nethervinery",
    "chipped", "dungeonz", "athena", "exposure",
    "passable-foliage", "profundis", "adventurez", "fleshz",
    "smallships",
)
# Manifest rebuild groups map to the hearthwind module that replaces them.
REBUILD_GROUP_MODULES = {
    "aged-survival": "hearthwind-survival",
    "aged-skills": "hearthwind-skills",
    "aged-primitive": "hearthwind-primitive",
    "aged-world": "hearthwind-world",
}

# Trailing version noise: -1.2.3, +fabric, _v1, -mc1.20.1, -fabric-1.20.1, ...
VERSION_SUFFIX = re.compile(
    r"(?:[-_+ ](?:v?\d[\w.+]*|fabric|quilt|forge|neoforge|universal|"
    r"mc\d+(?:\.\d+)*|1\.\d+(?:\.\d+)?))$"
)
TABLE_ROW = re.compile(
    r"^\| `(?P<jar>[^`]+)` \| `(?P<pid>[A-Za-z0-9]+)` \| "
    r"(?P<status>[A-Z]+) \| (?P<evidence>.+) \|$"
)


def normalize(name):
    """Lowercase, drop version noise/extensions/parentheticals, unify separators."""
    if not name:
        return ""
    s = name.lower().replace("_", "-").replace(" ", "-")
    s = re.sub(r"\([^)]*\)", "", s)
    s = re.sub(r"\.jar$", "", s)
    prev = None
    while prev != s:
        prev = s
        s = VERSION_SUFFIX.sub("", s)
    return re.sub(r"[^a-z0-9]+", "-", s).strip("-")


def squash(name):
    return re.sub(r"[^a-z0-9]", "", (name or "").lower())


def load_json(path):
    with open(path, encoding="utf-8") as fh:
        return json.load(fh)


def load_report_hints(path):
    """Optional pid -> {stems, slugs} hints parsed from the audit report tables."""
    hints = {}
    if not path.is_file():
        return hints
    for line in path.read_text(encoding="utf-8").splitlines():
        m = TABLE_ROW.match(line.strip())
        if not m:
            continue
        pid = m.group("pid")
        hint = hints.setdefault(pid, {"stems": set(), "slugs": set()})
        hint["stems"].add(normalize(m.group("jar")))
        evidence = m.group("evidence")
        for slug in re.findall(r"plan 5\.2: ([\w-]+)", evidence):
            hint["slugs"].add(slug.lower())
        for slug in re.findall(r"official 26\.2 build exists \(([\w-]+)\)", evidence):
            hint["slugs"].add(slug.lower())
    return hints


def entry_tokens(entry):
    tokens = set()
    for key in ("file", "slug"):
        value = entry.get(key)
        if value:
            tokens.add(squash(normalize(value)))
    for token in entry.get("jar_tokens") or []:
        tokens.add(squash(normalize(token)))
    tokens.discard("")
    return tokens


def jar_tokens(name):
    """squashed token for a jar filename after version-noise stripping."""
    token = squash(normalize(name))
    return {token} if token else set()


def attribute_by_tokens(name, entries):
    """Return the unique manifest entry a jar filename belongs to, or None."""
    candidates = jar_tokens(name)
    if not candidates:
        return None
    matches = [e for e in entries if entry_tokens(e) & candidates]
    if len(matches) == 1:
        return matches[0]
    if len(matches) > 1:
        exact = [e for e in matches if entry_tokens(e) == candidates]
        return exact[0] if len(exact) == 1 else None
    # Prefix fallback for module names such as custom-mods/athena -> athena-ctm.
    for candidate in candidates:
        if len(candidate) < 5:
            continue
        prefix = [
            e for e in entries
            if any(
                t.startswith(candidate) or candidate.startswith(t)
                for t in entry_tokens(e)
                if len(t) >= 5
            )
        ]
        if len(prefix) == 1:
            return prefix[0]
    return None


def custom_module_jars():
    """jar name -> module dir name for locally built mod jars."""
    jars = {}
    for module_dir in sorted(CUSTOM_MODS.iterdir()):
        libs = module_dir / "build" / "libs"
        if not libs.is_dir():
            continue
        if not (module_dir.name.startswith("hearthwind-") or module_dir.name in PORTED_MODULES):
            continue
        for jar in sorted(libs.glob("*.jar")):
            if "sources" in jar.name or "javadoc" in jar.name:
                continue
            jars[jar.name] = module_dir.name
    return jars


def realization_ok(entry, module_names):
    """Is a rebuild / datapack-backed entry realized by a module or datapack?"""
    target = entry.get("realized_by")
    if target is None:
        target = REBUILD_GROUP_MODULES.get(entry.get("group"))
    if target and target.startswith("datapack:"):
        name = target.split(":", 1)[1]
        return (ROOT / "conversion" / "datapacks" / name / "pack.mcmeta").is_file()
    if target and target.startswith("module-prefix:"):
        prefix = target.split(":", 1)[1]
        return any(module.startswith(prefix) for module in module_names)
    if target:
        return target in module_names
    # No explicit target: accept a module whose name matches the entry tokens.
    tokens = entry_tokens(entry)
    return any(squash(module) in tokens for module in module_names)


def check_pack(aged_pids, entries, accounted, dependencies):
    """Part 2: realized jars vs declared manifest entries.

    Returns (failures, notes): failure strings and informational lines.
    """
    failures = []
    notes = []
    if not RESOLVED.is_file():
        return None  # caller prints the skip notice
    resolved = load_json(RESOLVED).get("resolved", [])
    res_by_jar = {}
    res_by_pid = {}
    for r in resolved:
        if not str(r.get("status", "")).startswith("ok") or not r.get("picked"):
            continue
        fname = r["picked"]["file"]["filename"]
        res_by_jar[fname] = r
        pid = r.get("project_id")
        if pid:
            res_by_pid.setdefault(pid, r)

    server_jars = {p.name for p in DIST_SERVER.glob("*.jar")} if DIST_SERVER.is_dir() else set()
    client_jars = {p.name for p in DIST_CLIENT.glob("*.jar")} if DIST_CLIENT.is_dir() else set()
    if not server_jars and not client_jars:
        return None
    vendor_jars = {p.name for p in VENDORED.glob("*.jar") if "sources" not in p.name}
    modules = custom_module_jars()
    module_names = set(modules.values())

    ownership = {}  # escaped Aged pid -> set(jar names)
    realized_deps = set()  # dependency entry file names seen in dist
    unattributed = []
    side_of = {}
    for side, jars in (("server", server_jars), ("client", client_jars)):
        for name in jars:
            side_of.setdefault(name, set()).add(side)
    for name in sorted(side_of):
        sides = "/".join(sorted(side_of[name]))
        r = res_by_jar.get(name)
        if r is not None:
            pid = r.get("project_id")
            action = str(r.get("action", ""))
            if pid in aged_pids:
                ownership.setdefault(pid, set()).add(name)
            elif action.endswith("auto-dep"):
                pass  # resolver-proven transitive dependency
            elif action == "dependency":
                realized_deps.add(r.get("file"))
            else:
                unattributed.append((sides, name, "resolved but not an Aged mod"))
            continue
        module = modules.get(name)
        entry = None
        if module is not None:
            if module.startswith("hearthwind-"):
                continue  # our own modules are always allowed
            entry = attribute_by_tokens(module, entries)
        if entry is None:
            entry = attribute_by_tokens(name, entries)
        if entry is None and name in vendor_jars:
            for e in dependencies:
                if entry_tokens(e) & jar_tokens(name):
                    entry = e
                    break
        if entry is None:
            unattributed.append((sides, name, "no manifest entry"))
            continue
        action = entry.get("action")
        if entry.get("project_id"):
            ownership.setdefault(entry["project_id"], set()).add(name)
        elif action == "dependency":
            realized_deps.add(entry.get("file"))
        elif action == "drop":
            unattributed.append((sides, name, "dropped mod still ships"))
        else:
            unattributed.append((sides, name, f"unkeyed manifest entry ({entry.get('file')})"))

    # Forward: every accounted Aged mod must be realized in the right tree.
    for pid in sorted(accounted):
        entry = accounted[pid]
        if entry.get("realized_by") or entry.get("action") == "rebuild":
            if not realization_ok(entry, module_names):
                failures.append(
                    f"{entry.get('file')} ({pid}): rebuild/datapack realization missing"
                )
            continue
        override = entry.get("local_override")
        if override:
            if override not in (server_jars | client_jars):
                failures.append(
                    f"{entry.get('file')} ({pid}): local override {override} not in dist"
                )
            continue
        r = res_by_pid.get(pid)
        client_only = bool(r and r.get("client_only")) or entry.get("action") == "client-optional"
        if r is not None:
            expected = r["picked"]["file"]["filename"]
            target = client_jars if client_only else server_jars
            if expected not in target:
                failures.append(
                    f"{entry.get('file')} ({pid}): resolved {expected} missing from "
                    f"{'client' if client_only else 'server'} dist"
                )
        else:
            owners = ownership.get(pid, set())
            if not owners:
                failures.append(
                    f"{entry.get('file')} ({pid}): no jar - upstream missing and no "
                    "vendored/custom-mods match"
                )
            else:
                target = client_jars if client_only else server_jars
                if not (owners & target):
                    failures.append(
                        f"{entry.get('file')} ({pid}): jar {sorted(owners)} not in "
                        f"{'client' if client_only else 'server'} dist"
                    )

    # Dependencies declared in the manifest must ship too.
    for entry in dependencies:
        fname = entry.get("file")
        if fname in realized_deps:
            continue
        r = next(
            (x for x in resolved if x.get("file") == fname or x.get("slug") == entry.get("slug")),
            None,
        )
        if r and r.get("picked") and r["picked"]["file"]["filename"] in (server_jars | client_jars):
            continue
        failures.append(f"dependency {fname}: declared but no jar in the pack")

    notes.append(
        f"dist jars: {len(server_jars)} server, {len(client_jars)} client; "
        f"resolved ok: {len(res_by_jar)}; modules: {len(modules)}"
    )
    for side, name, why in unattributed:
        failures.append(f"unattributed {side} jar {name}: {why}")
    return failures, notes


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--no-pack", action="store_true", help="skip built-pack realization checks")
    args = ap.parse_args()

    for path in (SNAPSHOT, MANIFEST, ALLOWLIST):
        if not path.is_file():
            print(f"error: missing {path}", file=sys.stderr)
            return 1

    snapshot = load_json(SNAPSHOT)
    manifest = load_json(MANIFEST)
    allowlist = load_json(ALLOWLIST)
    hints = load_report_hints(REPORT_HINTS)

    aged_mods = [f for f in snapshot["files"] if f["kind"] == "mod"]
    aged_pids = {f["project_id"] for f in aged_mods}
    counts = {}
    for f in snapshot["files"]:
        counts[f["kind"]] = counts.get(f["kind"], 0) + 1

    entries = manifest["mods"]
    rule_failures = []
    by_pid = {}
    unkeyed = []
    dependencies = []
    for entry in entries:
        action = entry.get("action")
        pid = entry.get("project_id")
        if action not in ACTIONS:
            rule_failures.append(f"{entry.get('file')}: unknown action {action!r}")
            continue
        if pid:
            if pid not in aged_pids:
                rule_failures.append(
                    f"{entry.get('file')}: project_id {pid} not in the Aged index"
                )
            else:
                by_pid.setdefault(pid, []).append(entry)
        elif action == "dependency":
            dependencies.append(entry)
        elif action == "drop":
            pass
        else:
            unkeyed.append(entry)
            rule_failures.append(
                f"{entry.get('file')}: unkeyed manifest entry (action {action!r}); "
                "key it to an Aged pid, mark it `dependency`, or drop it"
            )

    # Fallback tokens for entries that are not keyed yet (legacy support).
    unkeyed_tokens = {}
    for entry in unkeyed:
        for token in entry_tokens(entry):
            unkeyed_tokens.setdefault(token, []).append(entry)

    allowed = {m["project_id"]: m for m in allowlist["missing"]}

    def fallback_match(pid, filename):
        hint = hints.get(pid, {"stems": set(), "slugs": set()})
        tokens = set(hint["stems"]) | set(hint["slugs"])
        tokens.add(normalize(filename))
        for slug in hint["slugs"]:
            tokens.add(normalize(slug))
        tokens.discard("")
        for token in tokens:
            for entry in unkeyed_tokens.get(squash(token), []):
                return entry
        return None

    by_action = {}
    allowlisted = []
    unaccounted = []
    dropped = []
    accounted = {}
    for f in sorted(aged_mods, key=lambda f: f["project_id"]):
        pid = f["project_id"]
        if pid in allowed:
            allowlisted.append(f)
            continue
        matches = by_pid.get(pid)
        entry = matches[0] if matches else fallback_match(pid, f["filename"])
        if entry is None:
            unaccounted.append(f)
            continue
        if entry.get("action") == "drop":
            dropped.append((f, entry))
            continue
        accounted[pid] = entry
        action = entry.get("action", "unknown")
        by_action[action] = by_action.get(action, 0) + 1

    covered = sum(by_action.values())
    stale_drops = [e for e in entries if e.get("action") == "drop" and e.get("project_id") in aged_pids]
    stale_allow = [pid for pid in allowed if pid not in aged_pids]
    dup_keys = sorted({pid for pid in by_pid if len(by_pid[pid]) > 1})

    pack_failures = []
    pack_notes = []
    if not args.no_pack:
        result = check_pack(aged_pids, entries, accounted, dependencies)
        if result is None:
            pack_notes.append(
                "pack check skipped: build/resolved.json or dist trees missing "
                "(run resolve_deps.py + build_pack.py --server-dir)"
            )
        else:
            pack_failures, pack_notes = result

    # ---- report -----------------------------------------------------------
    keys = sorted({e.get("project_id") for e in entries if e.get("project_id")})
    print(f"{snapshot['source']} parity diff")
    print(
        f"  index: {counts.get('mod', 0)} mods, "
        f"{counts.get('resourcepack', 0)} resourcepacks, "
        f"{counts.get('shaderpack', 0)} shaderpacks "
        "(packs tracked separately in W5)"
    )
    print(
        f"  manifest: {len(entries)} entries, {len(keys)} keyed to Aged pids, "
        f"{len(dependencies)} declared dependencies"
    )
    print(f"  allowlist: {len(allowed)} known-missing pids")
    print()

    print(f"Covered by manifest: {covered}/{len(aged_mods)}")
    for action in ("keep", "rebuild", "client-optional"):
        if action in by_action:
            print(f"  {action:16} {by_action[action]}")
    for action in sorted(set(by_action) - {"keep", "rebuild", "client-optional"}):
        print(f"  {action:16} {by_action[action]}")

    print(f"Known missing (allowlisted): {len(allowlisted)}")
    buckets = {}
    for m in allowlist["missing"]:
        if m["project_id"] not in aged_pids:
            continue
        if m["reason"].startswith("W4"):
            label = "W4 port queue (plan 5.2)"
        elif m["reason"].startswith("W2"):
            label = "W2 adoption pending; official 26.2 build exists"
        else:
            label = "unplanned (no plan entry)"
        buckets[label] = buckets.get(label, 0) + 1
    for label in sorted(buckets):
        print(f"  {label:48} {buckets[label]}")

    print(f"Unaccounted: {len(unaccounted)}")
    for f in unaccounted:
        print(f"  FAIL {f['project_id']} {f['filename']}")
    for f, entry in dropped:
        print(
            f"  FAIL {f['project_id']} {f['filename']} -> manifest drop "
            f"({entry.get('file')})"
        )

    print()
    print("Pack realization:")
    for note in pack_notes:
        print(f"  {note}")
    for failure in pack_failures:
        print(f"  FAIL {failure}")

    print()
    warnings = []
    if stale_drops:
        warnings.append(
            "drop entries whose pid IS in Aged: "
            + ", ".join(f"{e.get('file')} ({e['project_id']})" for e in stale_drops)
        )
    if stale_allow:
        warnings.append(
            "allowlist pids not in Aged index (stale): " + ", ".join(stale_allow)
        )
    if dup_keys:
        warnings.append(
            "project_id keyed on more than one manifest entry: " + ", ".join(dup_keys)
        )
    if warnings:
        print(f"WARN ({len(warnings)}):")
        for w in warnings:
            print(f"  {w}")
    else:
        print("WARN: none")
    print()

    failed = bool(
        unaccounted or dropped or rule_failures or pack_failures
    )
    if rule_failures:
        print("Manifest rule violations:")
        for failure in rule_failures:
            print(f"  FAIL {failure}")
        print()
    print("RESULT: FAIL" if failed else "RESULT: PASS")
    return 1 if failed else 0


if __name__ == "__main__":
    sys.exit(main())
