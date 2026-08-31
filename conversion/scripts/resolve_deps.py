#!/usr/bin/env python3
"""Resolve curated manifest against Modrinth for a target Minecraft version.

Usage:
  python3 conversion/scripts/resolve_deps.py                # use targets.minecraft from build.conf.json
  python3 conversion/scripts/resolve_deps.py --mc 26.3-snapshot-9

Writes conversion/build/resolved.json and readiness-report.json.
"""

import argparse
import json
import re
import sys
import time
import urllib.request
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
CONF = ROOT / "conversion" / "build.conf.json"
MANIFEST = ROOT / "conversion" / "curated" / "mods-manifest.json"
BUILD = ROOT / "conversion" / "build"
CACHE = BUILD / ".cache"

UA = {"User-Agent": "hearthwind/0.1 (github.com/JMiahMan1/Hearthwind)"}


def http_json(url, tries=5):
    for i in range(tries):
        try:
            req = urllib.request.Request(url, headers=UA)
            with urllib.request.urlopen(req, timeout=20) as r:
                return json.load(r)
        except Exception as e:
            if i == tries - 1:
                return {"__error__": str(e)}
            time.sleep(2.5 * (i + 1))


def cached(slug):
    CACHE.mkdir(parents=True, exist_ok=True)
    p = CACHE / f"{slug}.json"
    if p.exists():
        age = time.time() - p.stat().st_mtime
        if age < 3600:
            return json.load(open(p))
    time.sleep(0.25)  # stay well under anonymous rate limits
    d = http_json(f"https://api.modrinth.com/v2/project/{slug}/version")
    if isinstance(d, list):
        json.dump(d, open(p, "w"))
        return d
    return None


def cached_project(slug_or_id):
    """Fetch project metadata (slug/title) by slug or project_id, cached."""
    CACHE.mkdir(parents=True, exist_ok=True)
    p = CACHE / f"__proj_{slug_or_id}.json"
    if p.exists():
        age = time.time() - p.stat().st_mtime
        if age < 86400:
            return json.load(open(p))
    time.sleep(0.25)
    d = http_json(f"https://api.modrinth.com/v2/project/{slug_or_id}")
    if isinstance(d, dict) and "id" in d:
        json.dump(d, open(p, "w"))
        return d
    return None


VER_RE = re.compile(r"^(\d+(\.\d+)+)")


def vkey(s):
    m = VER_RE.match(s)
    if not m:
        return (0,)
    p = [int(x) for x in m.group(1).split(".")]
    if p[0] >= 26:
        return (100 + p[0], p[1] if len(p) > 1 else 0)
    return tuple(p[:4])


def pick_version(versions, mc, loader_pref=("fabric",), allow_older=False):
    exact = [v for v in versions if mc in v.get("game_versions", [])]
    pool = exact
    if not pool and allow_older:
        pool = [
            v
            for v in versions
            if any(
                g == ".".join(mc.split(".")[:2]) or g < mc
                for g in v.get("game_versions", [])
            )
        ]
    if not pool:
        return None, "no_version_for_target"

    def rank(v):
        loaders = set(v.get("loaders", []))
        return (
            any(ld in loaders for ld in loader_pref),
            all(not ld.startswith("forge") for ld in loaders),
            v["date_published"],
        )

    pool.sort(key=rank, reverse=True)
    best = pool[0]
    files = [f for f in best["files"] if f.get("primary")] or best["files"]
    return {
        "version_id": best["id"],
        "version_number": best["version_number"],
        "game_versions": best["game_versions"],
        "loaders": best["loaders"],
        "file": files[0],
    }, ("exact" if exact else "older_fallback")


def required_dep_project_ids(version_list):
    """Collect project_ids of required dependencies across a project's versions.

    We only need deps of the version we picked, but the caller passes the
    picked version object directly.
    """
    out = []
    for d in version_list.get("dependencies", []) or []:
        if d.get("dependency_type") == "required" and d.get("project_id"):
            out.append(d["project_id"])
    return out


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--mc", help="override target MC version")
    args = ap.parse_args()

    conf = json.load(open(CONF))
    mc = args.mc or conf["targets"]["minecraft"]
    allow_older = conf["policy"]["allow_older_builds_if_exact_missing"]
    print(
        f"Target MC: {mc} | loader: {conf['targets']['loader']} {conf['targets']['loader_version']}"
    )

    man = json.load(open(MANIFEST))
    wanted = [m for m in man["mods"] if m["action"] in ("keep", "add", "client-optional")]
    print(f"Resolving {len(wanted)} keep/add/client-optional entries...")

    results = []
    lock = [0]

    def resolve_one(rec):
        """Fill rec with pick info for its slug. Returns True on success."""
        slug = rec.get("slug")
        if not slug:
            rec["status"] = "no_slug"
            return False
        vers = cached(slug)
        if vers is None:
            rec["status"] = "api_error"
            return False
        picked, how = pick_version(vers, mc, allow_older=allow_older)
        if not picked:
            # report what the project DOES support to aid triage
            gvs = sorted({g for v in vers for g in v.get("game_versions", [])})
            stable = [g for g in gvs if "-" not in g]
            rec["status"] = "missing_for_target"
            rec["project_max_stable"] = max(stable, key=vkey) if stable else None
            rec["project_max_any"] = (
                max(gvs, key=lambda s: ("snapshot" not in s, vkey(s))) if gvs else None
            )
            return False
        rec["status"] = f"ok:{how}"
        rec["picked"] = picked
        # remember the full picked version so we can read its dependency list
        vid = picked.get("version_id")
        rec["_picked_version_obj"] = next((v for v in vers if v.get("id") == vid), None)
        return True

    def work(m):
        rec = dict(m)
        rec.update({"target_mc": mc, "status": None, "picked": None})
        if m["action"] == "client-optional":
            rec["client_only"] = True
        resolve_one(rec)
        return rec

    with ThreadPoolExecutor(max_workers=6) as ex:
        for rec in ex.map(work, wanted):
            mark = {"ok:exact": "+", "ok:older_fallback": "~"}.get(rec["status"], "-")
            extra = ""
            if rec["status"] == "missing_for_target":
                extra = f"(max stable {rec.get('project_max_stable')})"
            print(
                f"  [{mark}] {rec['file'][:44]:44} {rec['slug'][:28]:28} {rec['status']}{extra}"
            )
            results.append(rec)
            lock[0] += 1
            if lock[0] % 10 == 0:
                _save(results, mc)

    # --- Phase 2: recursively auto-add REQUIRED transitive dependencies ---
    # Modrinth metadata sometimes omits these from search/project listings, but
    # every version object declares its own dependencies[]. Walking them makes
    # bumps self-healing: new parent mods pull in whatever they now require.
    known_slugs = {r["slug"] for r in results if r.get("slug")}
    known_pids = set()
    for pid_cache in CACHE.glob("__proj_*.json"):
        try:
            known_pids.add(json.load(open(pid_cache))["id"])
        except Exception:
            pass
    dep_queue = []  # (parent_file, parent_slug, project_id)
    for r in results:
        obj = r.pop("_picked_version_obj", None)
        if obj:
            for pid in required_dep_project_ids(obj):
                dep_queue.append((r["file"], r.get("slug"), pid))

    auto_added = []
    seen_pids = set()
    passes = 0
    while dep_queue and passes < 5:
        passes += 1
        nxt = []
        batch, dep_queue = dep_queue, []
        for parent_file, parent_slug, pid in batch:
            if pid in seen_pids:
                continue
            seen_pids.add(pid)
            proj = cached_project(pid)
            slug = proj["slug"] if proj else None
            if slug and slug in known_slugs:
                continue  # already in pack
            rec = {
                "file": slug or pid,
                "slug": slug,
                "action": "add:auto-dep",
                "auto_added_by": [f"{parent_file} ({parent_slug})"],
                "target_mc": mc,
                "status": None,
                "picked": None,
            }
            ok = resolve_one(rec) if slug else False
            if not ok and not slug:
                rec["status"] = "dep_unresolvable"
            mark = "+" if rec["status"].startswith("ok") else "-"
            print(
                f"  [{mark}] (dep of {parent_slug}) {rec['file'][:36]:36} "
                f"{(slug or '?')[:28]:28} {rec['status']}"
            )
            results.append(rec)
            if slug:
                known_slugs.add(slug)
            if rec["status"].startswith("ok"):
                auto_added.append(rec)
                obj = rec.pop("_picked_version_obj", None)
                if obj:
                    for dpid in required_dep_project_ids(obj):
                        nxt.append((rec["file"], slug, dpid))
            else:
                rec.pop("_picked_version_obj", None)
        dep_queue.extend(nxt)

    for r in results:
        r.pop("_picked_version_obj", None)

    if auto_added:
        print(f"\nAuto-added {len(auto_added)} transitive dependency mod(s):")
        for r in auto_added:
            print(f"  + {r['slug']} <- {', '.join(r['auto_added_by'])}")

    _save(results, mc)

    ok = sum(1 for r in results if r["status"].startswith("ok"))
    missing = [r for r in results if r["status"] == "missing_for_target"]
    baddeps = [
        r
        for r in results
        if not r["status"].startswith("ok") and r.get("action") == "add:auto-dep"
    ]
    print(
        f"\nREADY {ok}/{len(results)} | MISSING {len(missing)} | BAD DEPS {len(baddeps)}"
    )
    if missing:
        print("\nMissing for target:")
        for r in sorted(missing, key=lambda x: x["file"]):
            print(f"  {r['file'][:40]:40} max-stable={r.get('project_max_stable')}")
    sys.exit(0 if ok == len(results) else 1)


def _save(results, mc):
    BUILD.mkdir(parents=True, exist_ok=True)
    json.dump(
        {"target_mc": mc, "resolved": results},
        open(BUILD / "resolved.json", "w"),
        indent=1,
    )
    report = {
        "target_mc": mc,
        "ready": sum(1 for r in results if r["status"].startswith("ok")),
        "total": len(results),
        "auto_added_deps": [
            {"file": r["file"], "slug": r.get("slug"), "by": r.get("auto_added_by")}
            for r in results
            if r.get("action") == "add:auto-dep"
            and str(r.get("status", "")).startswith("ok")
        ],
        "missing": [
            {
                "file": r["file"],
                "slug": r.get("slug"),
                "project_max_stable": r.get("project_max_stable"),
                "project_max_any": r.get("project_max_any"),
            }
            for r in results
            if r["status"] == "missing_for_target"
        ],
        "bad_deps": [
            {
                "file": r["file"],
                "slug": r.get("slug"),
                "by": r.get("auto_added_by"),
                "status": r["status"],
            }
            for r in results
            if r.get("action") == "add:auto-dep"
            and not str(r.get("status", "")).startswith("ok")
        ],
    }
    json.dump(report, open(BUILD / "readiness-report.json", "w"), indent=1)


if __name__ == "__main__":
    main()
