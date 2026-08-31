#!/usr/bin/env python3
"""Static pack-wide feature-order cycle analyzer.

Replicates the graph that net.minecraft.world.level.biome.FeatureSorter
builds at world-gen time (consecutive placed-feature pairs per biome,
step-major, including cross-step boundaries) over ALL biome definitions
found in the pack, then reports strongly-connected components (cycles).

Modeled data sources:
- vanilla jar (--vanilla, auto-discovered from the fabric-loom cache)
- every jar in --mods-dir (recursively incl. META-INF/jars nested jars)
- world datapack --datapack (default conversion/datapacks/hearthwind);
  its data/<ns>/worldgen/biome/*.json REPLACE jar versions by (ns, name)
- lithostitched add_features worldgen_modifier JSONs (biome selectors are
  resolved via worldgen/biome tags); appended to the END of the target step

Nodes are (placed_feature_key, step_index) - the FeatureData granularity.
The shipped cycle-tolerant mixin makes known conflicts non-fatal; the
baseline file records their signatures. With --strict, any NEW cycle
fails the run.

Usage:
  python3 custom-mods/tools/check_feature_cycles.py --update-baseline
  python3 custom-mods/tools/check_feature_cycles.py --strict
"""

from __future__ import annotations

import argparse
import glob
import io
import json
import os
import sys
import zipfile
from collections import defaultdict
from pathlib import Path

STEPS = [
    "raw_generation", "lakes", "local_modifications", "underground_structures",
    "surface_structures", "strongholds", "underground_ores",
    "underground_decoration", "fluid_springs", "vegetal_decoration",
    "top_layer_modification",
]
STEP_IDX = {name: i for i, name in enumerate(STEPS)}
BIOME_RE_SUFFIX = "worldgen/biome/"
TAG_RE_SUFFIX = "tags/worldgen/biome/"
MODIFIER_SUFFIX = "lithostitched/worldgen_modifier/"


def iter_zip_layers(path: str):
    """Yield (source_label, zipfile) for a jar and every nested jar inside."""
    with zipfile.ZipFile(path) as top:
        yield path, top
        for name in top.namelist():
            if name.startswith("META-INF/jars/") and name.endswith(".jar"):
                try:
                    inner = zipfile.ZipFile(io.BytesIO(top.read(name)))
                except zipfile.BadZipFile:
                    continue
                yield f"{path}!{name}", inner


def data_suffix(entry: str) -> str | None:
    """Return the path after the last '/data/' segment, if any."""
    idx = entry.rfind("/data/")
    if idx < 0:
        idx = entry.find("data/") if entry.startswith("data/") else -1
        if idx != 0:
            return None
        return entry[len("data/"):]
    return entry[idx + len("/data/"):]


def split_ns(path: str) -> tuple[str, str] | None:
    if "/" not in path:
        return None
    ns, _, rest = path.partition("/")
    return ns, rest


class Sources:
    def __init__(self) -> None:
        self.biomes: dict[tuple[str, str], list[list[str]]] = {}
        self.tags: dict[tuple[str, str], list[str]] = {}
        self.modifiers: list[tuple[str, dict]] = []
        self.warnings = 0

    def warn(self, msg: str) -> None:
        self.warnings += 1
        print(f"  warn: {msg}")


def load_json_bytes(raw: bytes, where: str):
    try:
        return json.loads(raw)
    except (ValueError, UnicodeDecodeError):
        print(f"  warn: unparseable JSON skipped: {where}")
        return None


def scan_zip(sources: Sources, label: str, zf: zipfile.ZipFile) -> None:
    for entry in zf.namelist():
        if not entry.endswith(".json"):
            continue
        suffix = data_suffix(entry)
        if suffix is None:
            continue
        parts = split_ns(suffix)
        if parts is None:
            continue
        ns, rest = parts
        raw = zf.read(entry)
        where = f"{label}!{entry}"
        if BIOME_RE_SUFFIX in rest and rest.startswith("worldgen/biome/"):
            doc = load_json_bytes(raw, where)
            if isinstance(doc, dict) and isinstance(doc.get("features"), list):
                key = (ns, rest[len("worldgen/biome/"):-len(".json")])
                steps = []
                for step in doc["features"]:
                    refs = step if isinstance(step, list) else []
                    steps.append([r for r in refs if isinstance(r, str)])
                sources.biomes[key] = steps
        elif TAG_RE_SUFFIX in rest and rest.startswith(TAG_RE_SUFFIX):
            doc = load_json_bytes(raw, where)
            if isinstance(doc, dict) and isinstance(doc.get("values"), list):
                key = (ns, rest[len(TAG_RE_SUFFIX):-len(".json")])
                sources.tags[key] = [v for v in doc["values"] if isinstance(v, str)]
        elif MODIFIER_SUFFIX in rest and rest.endswith(".json"):
            doc = load_json_bytes(raw, where)
            if isinstance(doc, dict) and "lithostitched:add_features" == doc.get("type"):
                sources.modifiers.append((where, doc))


def scan_dir(sources: Sources, root: Path) -> None:
    if not root.is_dir():
        return
    wdp = root / "data"
    for json_path in sorted(wdp.glob("*/worldgen/biome/*.json")):
        doc = load_json_bytes(json_path.read_bytes(), str(json_path))
        if isinstance(doc, dict) and isinstance(doc.get("features"), list):
            key = (json_path.parent.parent.name, json_path.stem)
            sources.biomes[key] = [
                [r for r in step if isinstance(r, str)]
                if isinstance(step, list) else []
                for step in doc["features"]
            ]
    for json_path in sorted(wdp.glob("*/tags/worldgen/biome/*.json")):
        doc = load_json_bytes(json_path.read_bytes(), str(json_path))
        if isinstance(doc, dict) and isinstance(doc.get("values"), list):
            key = (json_path.parent.name, json_path.stem)
            sources.tags[key] = [v for v in doc["values"] if isinstance(v, str)]


def resolve_tag(sources: Sources, tag: tuple[str, str], memo: dict, stack: set) -> set[str]:
    if tag in memo:
        return memo[tag]
    if tag in stack:
        return set()
    stack.add(tag)
    out: set[str] = set()
    for entry in sources.tags.get(tag, []):
        if entry.startswith("#"):
            parts = split_ns(entry[2:])
            if parts:
                out |= resolve_tag(sources, parts, memo, stack)
        else:
            out.add(entry)
    stack.discard(tag)
    memo[tag] = out
    return out


def biome_selector(sources: Sources, value, memo: dict) -> set[tuple[str, str]]:
    """Resolve a biome selector (id, #tag, or list) to biome keys."""
    if isinstance(value, str):
        value = [value]
    if not isinstance(value, list):
        return set()
    out: set[tuple[str, str]] = set()
    for entry in value:
        if not isinstance(entry, str):
            continue
        if entry.startswith("#"):
            parts = split_ns(entry[1:])
            if parts:
                for bid in resolve_tag(sources, parts, memo, set()):
                    bparts = split_ns(bid)
                    if bparts:
                        out.add(bparts)
        else:
            parts = split_ns(entry)
            if parts:
                out.add(parts)
    return out


def apply_modifiers(sources: Sources) -> None:
    memo: dict = {}
    for where, doc in sources.modifiers:
        step = STEP_IDX.get(doc.get("step", ""))
        if step is None:
            sources.warn(f"unknown step in {where}")
            continue
        feats = doc.get("features", [])
        feats = feats if isinstance(feats, list) else [feats]
        feats = [f for f in feats if isinstance(f, str)]
        targets = biome_selector(sources, doc.get("biomes"), memo)
        for biome in targets:
            steps = sources.biomes.get(biome)
            if steps is None:
                continue
            while len(steps) <= step:
                steps.append([])
            steps[step].extend(feats)


def build_graph(sources: Sources):
    edges: dict[tuple[str, int], set[tuple[str, int]]] = defaultdict(set)
    edge_src: dict[tuple[tuple[str, int], tuple[str, int]], set[str]] = defaultdict(set)
    for (ns, name), steps in sorted(sources.biomes.items()):
        label = f"{ns}:{name}"
        flat: list[tuple[str, int]] = []
        for si, refs in enumerate(steps):
            for ref in refs:
                if ref.startswith("#"):
                    sources.warn(f"{label}: tag ref in features skipped: {ref}")
                    continue
                flat.append((ref, si))
        for a, b in zip(flat, flat[1:]):
            edges[a].add(b)
            edge_src[(a, b)].add(label)
    return edges, edge_src


def tarjan_sccs(nodes, edges):
    index_of, low, on_stack = {}, {}, set()
    stack, sccs, counter = [], [], [0]

    def strongconnect(v):
        work = [(v, iter(sorted(edges.get(v, ()))))]
        index_of[v] = low[v] = counter[0]
        counter[0] += 1
        stack.append(v)
        on_stack.add(v)
        while work:
            node, it = work[-1]
            advanced = False
            for w in it:
                if w not in index_of:
                    index_of[w] = low[w] = counter[0]
                    counter[0] += 1
                    stack.append(w)
                    on_stack.add(w)
                    work.append((w, iter(sorted(edges.get(w, ())))))
                    advanced = True
                    break
                if w in on_stack:
                    low[node] = min(low[node], index_of[w])
            if advanced:
                continue
            work.pop()
            if work:
                parent = work[-1][0]
                low[parent] = min(low[parent], low[node])
            if low[node] == index_of[node]:
                comp = []
                while True:
                    w = stack.pop()
                    on_stack.discard(w)
                    comp.append(w)
                    if w == node:
                        break
                sccs.append(comp)

    for v in sorted(nodes):
        if v not in index_of:
            strongconnect(v)
    return sccs


def signature(scc) -> str:
    return "|".join(sorted(f"{k}@{STEPS[s]}" for k, s in scc))


def main() -> int:
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("--mods-dir", default="dev-server/mods")
    ap.add_argument("--vanilla", default=None)
    ap.add_argument("--datapack", default="conversion/datapacks/hearthwind")
    ap.add_argument("--baseline", default="custom-mods/tools/feature_cycle_baseline.txt")
    ap.add_argument("--update-baseline", action="store_true")
    ap.add_argument("--strict", action="store_true")
    args = ap.parse_args()

    sources = Sources()

    vanilla = args.vanilla
    if vanilla is None:
        pattern = os.path.expanduser(
            "~/.gradle/caches/fabric-loom/minecraftMaven/net/minecraft/"
            "minecraft-merged*/26.2/minecraft-merged*.jar")
        hits = sorted(glob.glob(pattern))
        vanilla = hits[0] if hits else None
    if vanilla and os.path.isfile(vanilla):
        for label, zf in iter_zip_layers(vanilla):
            scan_zip(sources, label, zf)
        print(f"scanned vanilla jar: {vanilla}")
    else:
        print("note: vanilla jar not found; vanilla biomes NOT analyzed")

    mods = Path(args.mods_dir)
    if mods.is_dir():
        jars = sorted(str(p) for p in mods.glob("*.jar"))
        for jar in jars:
            for label, zf in iter_zip_layers(jar):
                scan_zip(sources, label, zf)
        print(f"scanned {len(jars)} mod jars from {mods}")
    else:
        print(f"note: mods dir missing: {mods}")

    scan_dir(sources, Path(args.datapack))
    print(f"biome defs: {len(sources.biomes)}, biome tags: {len(sources.tags)}, "
          f"add_features modifiers: {len(sources.modifiers)}")

    apply_modifiers(sources)
    edges, edge_src = build_graph(sources)
    nodes = set(edges)
    for targets in edges.values():
        nodes |= targets
    print(f"graph: {len(nodes)} nodes, {sum(len(t) for t in edges.values())} edges")

    sccs = [c for c in tarjan_sccs(nodes, edges)
            if len(c) > 1 or any(v in edges.get(v, ()) for v in c)]
    print(f"cycle SCCs: {len(sccs)}")

    baseline_path = Path(args.baseline)
    known: set[str] = set()
    if baseline_path.is_file():
        for line in baseline_path.read_text().splitlines():
            line = line.strip()
            if line and not line.startswith("#"):
                known.add(line)

    new_sccs = []
    for scc in sorted(sccs, key=signature):
        sig = signature(scc)
        marker = "known" if sig in known else "NEW"
        if marker == "NEW":
            new_sccs.append(scc)
        print(f"\n[{marker}] {len(scc)} nodes: {sig}")
        for a in sorted(scc):
            for b in sorted(edges.get(a, ())):
                if b in scc:
                    srcs = sorted(edge_src.get((a, b), ()))
                    show = ", ".join(srcs[:4]) + ("..." if len(srcs) > 4 else "")
                    print(f"  {a[0]}@{STEPS[a[1]]} -> {b[0]}@{STEPS[b[1]]}"
                          f"  [{len(srcs)} biomes: {show}]")

    if args.update_baseline:
        baseline_path.parent.mkdir(parents=True, exist_ok=True)
        lines = ["# Feature-order cycle signatures known to be non-fatal",
                 "# (cycle-tolerant FeatureSorter mixin drops these back-edges).",
                 "# Regenerate: python3 custom-mods/tools/check_feature_cycles.py --update-baseline",
                 ""]
        lines += sorted(signature(s) for s in sccs)
        baseline_path.write_text("\n".join(lines) + "\n")
        print(f"\nbaseline written: {baseline_path} ({len(sccs)} signatures)")
        return 0

    if new_sccs:
        print(f"\nRESULT: {len(new_sccs)} NEW cycle SCC(s) not in baseline")
        if args.strict:
            return 1
    else:
        print("\nRESULT: no new cycles (all match baseline)")
    if sources.warnings:
        print(f"({sources.warnings} warnings)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
