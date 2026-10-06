#!/usr/bin/env python3
"""Turn a Yarn-era Fabric mod into readable Mojmap sources - the first step of
every W4 port.

Why this exists
---------------
MC 26.x uses Mojang's own names, so a mod already built for 26.x ports in a
couple of edits. Almost every remaining queue entry is a 1.20.1-1.21.11 Fabric
mod compiled against Yarn, so a port starts with a rename of every MC class,
field and method in the tree before the 26.2 API delta is even visible. Loom
1.17 has no `migrateMappings` task, so this rebuilds that step.

How it works
------------
A published Fabric jar is compiled against INTERMEDIARY names (not Yarn - Yarn
is only a dev-time view). None of the three mapping sets is sufficient alone:

  * Yarn's tiny v2 has columns (intermediary, named). Since 1.20.x it no longer
    carries the `official` column, so it cannot be joined to Mojang's file.
  * Mojang's client.txt maps official (obf) -> mojmap.
  * Fabric's intermediary maps official (obf) -> intermediary.

The bridge is the `official` column that intermediary and Mojang share, so the
joined table is intermediary -> mojmap. Members are keyed by
(owner, name, descriptor) with descriptors converted between namespaces -
plain string replacement gets overloads wrong in ways that still compile.

Usage
-----
  python3 custom-mods/tools/remap_to_mojmap.py <mod.jar> [--mc 1.21.11]
      [--out DIR] [--keep]

Output: <out>/<mod>-mojmap.jar and <out>/src/**/*.java (Vineflower).
Artifacts are cached under .tmp/mapwork/ so repeat runs are offline.
"""
import argparse
import hashlib
import os
import re
import subprocess
import sys
import urllib.request
import zipfile

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..'))
WORK = os.path.join(ROOT, '.tmp', 'mapwork')
PRIM = {'void': 'V', 'boolean': 'Z', 'byte': 'B', 'char': 'C', 'short': 'S',
        'int': 'I', 'long': 'J', 'float': 'F', 'double': 'D'}
UA = {'User-Agent': 'hearthwind-port-tool'}


def fetch(url, dest):
    if os.path.exists(dest) and os.path.getsize(dest) > 0:
        return dest
    os.makedirs(os.path.dirname(dest), exist_ok=True)
    print(f'  fetching {os.path.basename(dest)}')
    with urllib.request.urlopen(urllib.request.Request(url, headers=UA), timeout=120) as r, open(dest, 'wb') as f:
        f.write(r.read())
    return dest


def mojang_client_mappings_url(mc):
    import json
    manifest = json.load(urllib.request.urlopen(
        urllib.request.Request('https://launchermeta.mojang.com/mc/game/version_manifest_v2.json', headers=UA),
        timeout=60))
    entry = next((v for v in manifest['versions'] if v['id'] == mc), None)
    if not entry:
        sys.exit(f'{mc} not in the version manifest')
    meta = json.load(urllib.request.urlopen(urllib.request.Request(entry['url'], headers=UA), timeout=60))
    return meta['downloads']['client_mappings']['url']


# --- Mojang client.txt -------------------------------------------------------

def split_types(args):
    return [a.strip() for a in args.split(',') if a.strip()]


def desc_of(t, class_rev):
    t = t.strip()
    arr = 0
    while t.endswith('[]'):
        t, arr = t[:-2].strip(), arr + 1
    if not t:
        return ''
    if t in PRIM:
        d = PRIM[t]
    else:
        d = 'L' + class_rev.get(t, t.replace('.', '/')) + ';'
    return '[' * arr + d


def parse_mojang(path):
    """obf -> mojmap classes, and (obf owner, obf name, obf desc) -> mojmap member.

    TWO passes on purpose. Member descriptors name their parameter and return
    types, and those have to be converted from mojmap back to obf to match
    Fabric's rows - which needs the class table complete. Parsing in one pass
    meant any member whose type is declared LATER in Mojang's file fell back to
    the unconverted mojmap path and silently failed to join: measured on
    connectiblechains that lost ~26% of members (66,494 of 94,916 mapped).
    """
    classes, classes_rev = {}, {}
    for line in open(path, encoding='utf-8'):
        if line.startswith('#'):
            continue
        raw = line.rstrip('\n')
        if not raw[:1].isspace():
            m = re.match(r'^(\S+) -> (\S+):$', raw)
            if m:
                classes[m.group(2)] = m.group(1)
                classes_rev[m.group(1)] = m.group(2)

    members = {}
    owner = None
    for line in open(path, encoding='utf-8'):
        if line.startswith('#'):
            continue
        raw = line.rstrip('\n')
        # Mojang indents members with SPACES, not tabs - keying on '\t' here
        # silently matched every class and no members at all.
        if not raw[:1].isspace():
            m = re.match(r'^(\S+) -> (\S+):$', raw)
            if m:
                owner = m.group(2)
            continue
        if owner is None:
            continue
        body = re.sub(r'^\d+:\d+:', '', raw.strip())
        m = re.match(r'^(.*) -> (\S+)$', body)
        if not m:
            continue
        decl, obf_name = m.group(1), m.group(2)
        if '(' in decl:
            head, argstr = decl.split('(', 1)
            args = argstr.rsplit(')', 1)[0]
            ret = head.rsplit(' ', 1)[0]
            desc = '(' + ''.join(desc_of(a, classes_rev) for a in split_types(args)) + ')' + desc_of(ret, classes_rev)
            name = head.rsplit(' ', 1)[1].split('.')[-1]
        else:
            parts = decl.rsplit(' ', 1)
            if len(parts) != 2:
                continue
            desc, name = desc_of(parts[0], classes_rev), parts[1].split('.')[-1]
        members[(owner, obf_name, desc)] = name
    return classes, members


def remap_desc(desc, class_map):
    return re.sub(r'L([^;]+);', lambda m: 'L' + class_map.get(m.group(1), m.group(1)) + ';', desc)


def build_intermediary_to_mojmap(intermediary_tiny, mojang_txt, out):
    moj_classes, moj_members = parse_mojang(mojang_txt)
    obf_to_int = {}
    for line in open(intermediary_tiny, encoding='utf-8'):
        p = line.rstrip('\n').split('\t')
        if len(p) == 3 and p[0] == 'c':
            obf_to_int[p[1]] = p[2]

    lines = ['tiny\t2\t0\tintermediary\tmojmap\n']
    stats = dict(classes=0, methods=0, fields=0)
    owner_obf = None
    for line in open(intermediary_tiny, encoding='utf-8'):
        if line.startswith('tiny'):
            continue
        p = line.rstrip('\n').split('\t')
        if p and p[0] == 'c' and len(p) == 3:
            owner_obf = p[1]
            moj = moj_classes.get(owner_obf)
            if moj:
                lines.append(f'c\t{p[2]}\t{moj}\n')
                stats['classes'] += 1
            continue
        if owner_obf is None:
            continue
        # '\tf\t<desc>\t<name>\t<target>'.split('\t') has a LEADING EMPTY
        # element, so the guard is len 5 with p[0] == '' - not len 4.
        if len(p) == 5 and p[0] == '' and p[1] in ('f', 'm'):
            kind, desc, obf_name, int_name = p[1], p[2], p[3], p[4]
            moj_name = moj_members.get((owner_obf, obf_name, desc))
            if moj_name:
                lines.append(f'\t{kind}\t{remap_desc(desc, obf_to_int)}\t{int_name}\t{moj_name}\n')
                stats['methods' if kind == 'm' else 'fields'] += 1
    open(out, 'w', encoding='utf-8').writelines(lines)
    print(f"  joined: {stats['classes']} classes, {stats['methods']} methods, {stats['fields']} fields")
    return stats



def fix_source_leftovers(src, table):
    """Rename intermediary members the remapper left behind.

    tiny-remapper is descriptor-aware and correct where it applies, but it only
    rewrites a member when the constant-pool owner matches the class the
    mapping row sits under. Calls through a super-interface whose row lives on
    the implementing class are therefore missed - measured on
    connectiblechains, where `readView.method_71441(...)` stayed intermediary
    even though the table has `method_71441 -> getString`.

    Only tokens that survive are touched, and only when the whole table agrees
    on one target name; ambiguous names are left for a human.
    """
    names = {}
    ambiguous = set()
    for line in open(table, encoding='utf-8'):
        p = line.rstrip('\n').split('\t')
        if len(p) == 5 and p[0] == '' and p[1] in ('m', 'f'):
            src_name, dst = p[3], p[4]
            if src_name in names and names[src_name] != dst:
                ambiguous.add(src_name)
            else:
                names[src_name] = dst
    for a in ambiguous:
        names.pop(a, None)

    fixed = left = 0
    for dirpath, _, files in os.walk(src):
        for f in files:
            if not f.endswith('.java'):
                continue
            path = os.path.join(dirpath, f)
            text = open(path, encoding='utf-8', errors='ignore').read()
            if 'class_' not in text and 'method_' not in text and 'field_' not in text:
                continue
            def sub(m):
                return names.get(m.group(0), m.group(0))
            pat = r'\b(?:method_\d+|field_\d+|class_\d+)\b'
            before = len(re.findall(pat, text))
            new_text = re.sub(pat, sub, text)
            after = len(re.findall(pat, new_text))
            fixed += before - after
            left += after
            if new_text != text:
                open(path, 'w', encoding='utf-8').write(new_text)
    print(f'  leftover pass: rewrote {fixed} member references, {left} unresolved')

    # Vineflower sometimes emits a package separator as '$' in a type name
    # (`net.net$minecraft.core.BlockPos`), which is not a package at all.
    # Mechanical and safe to collapse.
    mangled = 0
    for dirpath, _, files in os.walk(src):
        for f in files:
            if not f.endswith('.java'):
                continue
            path = os.path.join(dirpath, f)
            text = open(path, encoding='utf-8', errors='ignore').read()
            new_text, n = re.subn(r'net\.net\$minecraft', 'net.minecraft', text)
            if n:
                mangled += n
                open(path, 'w', encoding='utf-8').write(new_text)
    print(f'  mangled package names repaired: {mangled}')
    return left


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('jar', help='the upstream mod jar (intermediary names)')
    ap.add_argument('--mc', default='1.21.11')
    ap.add_argument('--out', default=None)
    ap.add_argument('--keep', action='store_true', help='keep the mojmap jar and sources side by side')
    args = ap.parse_args()

    jar = os.path.abspath(args.jar)
    name = os.path.splitext(os.path.basename(jar))[0]
    out = os.path.abspath(args.out or os.path.join(WORK, 'out', name))
    os.makedirs(out, exist_ok=True)

    yarn_jar = fetch(f'https://maven.fabricmc.net/net/fabricmc/yarn/{args.mc}+build.6/yarn-{args.mc}+build.6-v2.jar',
                     os.path.join(WORK, f'yarn-{args.mc}.jar'))
    inter_jar = fetch(f'https://maven.fabricmc.net/net/fabricmc/intermediary/{args.mc}/intermediary-{args.mc}-v2.jar',
                      os.path.join(WORK, f'intermediary-{args.mc}.jar'))
    moj_txt = fetch(mojang_client_mappings_url(args.mc), os.path.join(WORK, f'mojang-{args.mc}.txt'))
    remapper = fetch('https://maven.fabricmc.net/net/fabricmc/tiny-remapper/0.14.1/tiny-remapper-0.14.1-fat.jar',
                     os.path.join(WORK, 'tiny-remapper.jar'))
    decomp = fetch('https://repo1.maven.org/maven2/org/vineflower/vineflower/1.11.1/vineflower-1.11.1.jar',
                   os.path.join(WORK, 'vineflower.jar'))

    inter_tiny = os.path.join(WORK, f'intermediary-{args.mc}.tiny')
    if not os.path.exists(inter_tiny):
        with zipfile.ZipFile(inter_jar) as z:
            open(inter_tiny, 'wb').write(z.read('mappings/mappings.tiny'))

    table = os.path.join(WORK, f'intermediary-to-mojmap-{args.mc}.tiny')
    if not os.path.exists(table):
        build_intermediary_to_mojmap(inter_tiny, moj_txt, table)

    moj_jar = os.path.join(out, f'{name}-mojmap.jar')
    print(f'  remapping -> {os.path.relpath(moj_jar, ROOT)}')
    subprocess.run(['java', '-jar', remapper, jar, moj_jar, table, 'intermediary', 'mojmap'], check=True)

    src = os.path.join(out, 'src')
    print(f'  decompiling -> {os.path.relpath(src, ROOT)}')
    subprocess.run(['java', '-jar', decomp, '--silent=1', moj_jar, src], check=True)

    unresolved = fix_source_leftovers(src, table)
    java = sum(1 for dp, _, fs in os.walk(src) for f in fs if f.endswith('.java'))
    print(f'  {java} java files, {unresolved} unresolved intermediary references')
    if not args.keep:
        os.remove(moj_jar)
    print(f'  sources: {os.path.relpath(src, ROOT)}')


if __name__ == '__main__':
    main()
