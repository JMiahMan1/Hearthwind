#!/usr/bin/env python3
"""Machine-check the Aged 3.1.2 parity ledger.

Compares the frozen Aged index snapshot (conversion/curated/aged-3.1.2-index.json)
against the curated manifest (conversion/curated/mods-manifest.json) and the
explicit debt ledger (conversion/curated/aged-missing-allowlist.json).

An Aged mod is accounted for when either:
  * its Modrinth project id is keyed on a manifest entry (project_id), or the
    entry matches by canonical slug / normalized filename, or
  * its project id is listed in the allowlist as known, tracked debt.

Exit 1 when an Aged mod is unaccounted for or maps to a manifest `drop`.
Non-Aged manifest entries (our own modules, extras) and stale drop entries are
reported as warnings only.

Usage:
  python3 conversion/scripts/aged_parity_diff.py
"""

import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SNAPSHOT = ROOT / "conversion" / "curated" / "aged-3.1.2-index.json"
MANIFEST = ROOT / "conversion" / "curated" / "mods-manifest.json"
ALLOWLIST = ROOT / "conversion" / "curated" / "aged-missing-allowlist.json"
# The audit report is scratch input for canonical slug/jar-name hints only;
# CI does not need it because every shipped Aged mod is keyed by project_id.
REPORT_HINTS = ROOT / ".tmp" / "k3_parity_audit.md"

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


def main():
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
    by_pid = {}
    for entry in entries:
        pid = entry.get("project_id")
        if pid:
            by_pid.setdefault(pid, []).append(entry)

    # Fallback tokens for entries that are not keyed yet.
    unkeyed_tokens = {}
    for entry in entries:
        if entry.get("project_id"):
            continue
        tokens = {normalize(entry.get("file", "")), normalize(entry.get("slug", ""))}
        tokens.discard("")
        for token in tokens:
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
            for entry in unkeyed_tokens.get(token, []):
                return entry
        return None

    by_action = {}
    allowlisted = []
    unaccounted = []
    dropped = []
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
        action = entry.get("action", "unknown")
        by_action[action] = by_action.get(action, 0) + 1

    covered = sum(by_action.values())
    extras = [
        e for e in entries
        if e.get("project_id") and e["project_id"] not in aged_pids
    ]
    stale_drops = [
        e for e in entries
        if e.get("action") == "drop" and e.get("project_id") in aged_pids
    ]
    stale_allow = [pid for pid in allowed if pid not in aged_pids]
    dup_keys = sorted(
        {pid for pid in by_pid if len(by_pid[pid]) > 1}
    )

    # ---- report -----------------------------------------------------------
    keys = sorted({e.get("project_id") for e in entries if e.get("project_id")})
    print(f"{snapshot['source']} parity diff")
    print(
        f"  index: {counts.get('mod', 0)} mods, "
        f"{counts.get('resourcepack', 0)} resourcepacks, "
        f"{counts.get('shaderpack', 0)} shaderpacks "
        "(packs tracked separately in W5)"
    )
    print(f"  manifest: {len(entries)} entries, {len(keys)} keyed to Aged pids")
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
    warnings = []
    if extras:
        warnings.append(
            "manifest entries not in Aged (extras): "
            + ", ".join(f"{e.get('file')} ({e['project_id']})" for e in extras)
        )
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

    failed = bool(unaccounted or dropped)
    print("RESULT: FAIL" if failed else "RESULT: PASS")
    return 1 if failed else 0


if __name__ == "__main__":
    sys.exit(main())
