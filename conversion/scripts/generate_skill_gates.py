#!/usr/bin/env python3
"""Generate skill-gate definitions for the aged-skills mod from the
migrated levelz corpus.

Reads conversion/datapacks/aged-server/data/levelz/**.json and emits one
JSON file per skill into custom-mods/aged-skills resources:

    data/aged_skills/gates/<skill>.json
      { "break": [{"level": N, "blocks": ["minecraft:x", ...]}, ...],
        "use":   [{"level": N, "blocks": [...]}, ...] }

- "break": blocks that cannot be MINED below the skill level
  (source: mining/NN.json -> skill "mining").
- "use": functional blocks that cannot be USED below the skill level
  (source: block/*.json with a real skill field).

Placeholder ids ("minecraft:custom_block", namespaced ids of cut mods)
are kept verbatim - the runtime simply never resolves them, mirroring the
datapack's {"required": false} convention. Idempotent output, sorted.
"""
import collections
import glob
import json
import os
import sys

# <repo>/conversion/scripts/<this file> -> <repo>
ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
CORPUS = os.path.join(ROOT, "conversion/datapacks/aged-server/data/levelz")
OUT = os.path.join(
    ROOT,
    "custom-mods/aged-skills/src/main/resources/data/aged_skills/gates")


def main() -> int:
    gates = collections.defaultdict(
        lambda: {"break": collections.defaultdict(set),
                 "use": collections.defaultdict(set)})

    for path in sorted(glob.glob(f"{CORPUS}/mining/*.json")):
        d = json.load(open(path))
        level = d.get("level")
        blocks = d.get("block") or []
        if isinstance(blocks, str):  # corpus has scalar ids in places
            blocks = [blocks]
        if not isinstance(level, int) or not blocks:
            continue
        gates["mining"]["break"][level].update(blocks)

    for path in sorted(glob.glob(f"{CORPUS}/block/*.json")):
        d = json.load(open(path))
        level = d.get("level")
        skill = d.get("skill")
        blocks = d.get("block") or []
        if isinstance(blocks, str):
            blocks = [blocks]
        if not isinstance(level, int) or not skill or not blocks:
            continue
        gates[skill]["use"][level].update(blocks)

    os.makedirs(OUT, exist_ok=True)
    written = []
    for skill in sorted(gates):
        payload = {
            kind: [
                {"level": lv, "blocks": sorted(entries)}
                for lv, entries in sorted(gates[skill][kind].items())
            ]
            for kind in ("break", "use")
        }
        out_path = os.path.join(OUT, f"{skill}.json")
        with open(out_path, "w") as fh:
            json.dump(payload, fh, indent=1, sort_keys=True)
            fh.write("\n")
        n_break = sum(len(e["blocks"]) for e in payload["break"])
        n_use = sum(len(e["blocks"]) for e in payload["use"])
        written.append(f"{skill}: {n_break} break / {n_use} use")
        print(out_path.replace(ROOT + "/", ""), "->", written[-1])
    return 0


if __name__ == "__main__":
    sys.exit(main())
