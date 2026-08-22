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

UA = {"User-Agent": "aged-server-conversion/0.1 (github.com/JMiahMan1/Aged)"}


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
        pool = [v for v in versions
                if any(g == ".".join(mc.split(".")[:2]) or g < mc
                       for g in v.get("game_versions", []))]
    if not pool:
        return None, "no_version_for_target"
    def rank(v):
        loaders = set(v.get("loaders", []))
        return (
            any(l in loaders for l in loader_pref),
            all(not l.startswith("forge") for l in loaders),
            v["date_published"],
        )
    pool.sort(key=rank, reverse=True)
    best = pool[0]
    files = [f for f in best["files"] if f.get("primary")] or best["files"]
    return {"version_number": best["version_number"],
            "game_versions": best["game_versions"],
            "loaders": best["loaders"],
            "file": files[0]}, ("exact" if exact else "older_fallback")


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--mc", help="override target MC version")
    args = ap.parse_args()

    conf = json.load(open(CONF))
    mc = args.mc or conf["targets"]["minecraft"]
    allow_older = conf["policy"]["allow_older_builds_if_exact_missing"]
    print(f"Target MC: {mc} | loader: {conf['targets']['loader']} {conf['targets']['loader_version']}")

    man = json.load(open(MANIFEST))
    wanted = [m for m in man["mods"] if m["action"] in ("keep", "add")]
    print(f"Resolving {len(wanted)} keep/add entries...")

    results = []
    lock = [0]

    def work(m):
        slug = m.get("slug")
        rec = dict(m)
        rec.update({"target_mc": mc, "status": None, "picked": None})
        if not slug:
            rec["status"] = "no_slug"
            return rec
        vers = cached(slug)
        if vers is None:
            rec["status"] = "api_error"
            return rec
        picked, how = pick_version(vers, mc, allow_older=allow_older)
        if not picked:
            # report what the project DOES support to aid triage
            gvs = sorted({g for v in vers for g in v.get("game_versions", [])})
            stable = [g for g in gvs if "-" not in g]
            rec["status"] = "missing_for_target"
            rec["project_max_stable"] = max(stable, key=vkey) if stable else None
            rec["project_max_any"] = max(gvs, key=lambda s: ("snapshot" not in s, vkey(s))) if gvs else None
            return rec
        rec["status"] = f"ok:{how}"
        rec["picked"] = picked
        return rec

    with ThreadPoolExecutor(max_workers=6) as ex:
        for rec in ex.map(work, wanted):
            mark = {"ok:exact": "+", "ok:older_fallback": "~"}.get(rec["status"], "-")
            extra = ""
            if rec["status"] == "missing_for_target":
                extra = f"(max stable {rec.get('project_max_stable')})"
            print(f"  [{mark}] {rec['file'][:44]:44} {rec['slug'][:28]:28} {rec['status']}{extra}")
            results.append(rec)
            lock[0] += 1
            if lock[0] % 10 == 0:
                _save(results, mc)

    _save(results, mc)

    ok = sum(1 for r in results if r["status"].startswith("ok"))
    missing = [r for r in results if r["status"] == "missing_for_target"]
    print(f"\nREADY {ok}/{len(results)} | MISSING {len(missing)}")
    if missing:
        print("\nMissing for target:")
        for r in sorted(missing, key=lambda x: x["file"]):
            print(f"  {r['file'][:40]:40} max-stable={r.get('project_max_stable')}")
    sys.exit(0 if ok == len(results) else 1)


def _save(results, mc):
    BUILD.mkdir(parents=True, exist_ok=True)
    json.dump({"target_mc": mc, "resolved": results},
              open(BUILD / "resolved.json", "w"), indent=1)
    report = {
        "target_mc": mc,
        "ready": sum(1 for r in results if r["status"].startswith("ok")),
        "total": len(results),
        "missing": [{"file": r["file"], "slug": r.get("slug"),
                     "project_max_stable": r.get("project_max_stable"),
                     "project_max_any": r.get("project_max_any")}
                    for r in results if r["status"] == "missing_for_target"],
    }
    json.dump(report, open(BUILD / "readiness-report.json", "w"), indent=1)


if __name__ == "__main__":
    main()
