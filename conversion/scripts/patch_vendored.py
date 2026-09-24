#!/usr/bin/env python3
"""Idempotent content patches for vendored jars that lack official 26.2 builds.

kiwi 26.0.20+fabric (targets minecraft >=26.1) crashes on 26.2:

  NoSuchFieldError: Minecraft does not have member field 'Screen screen'
    at snownee.kiwi.contributor.ContributorsClient.onKeyInput

Two independent guards (both required):

1. KiwiClientConfig.<clinit>: flip cosmeticScreenKeybind default true->false
   so a missing config/kiwi-client.yaml cannot enable the keybind.
2. ContributorsClient.onKeyInput: replace the leading getstatic with
   iconst_0 so ifeq always takes the return path. Kiwi rewrites
   config/kiwi-client.yaml with cosmeticScreenKeybind: true whenever the
   live field is true (any instance that ever booted unpatched), which
   would otherwise re-enable the Minecraft.screen getfield on next launch.

Also patches managed Prism instance mods/ copies so update_prism.sh and
hand deploys cannot ship unpatched bytes.

Run automatically from build_pack.py; also safe standalone:

  python3 conversion/scripts/patch_vendored.py
"""

from __future__ import annotations

import struct
import sys
import zipfile
from pathlib import Path

CLASS_PATH = "snownee/kiwi/KiwiClientConfig.class"
CONTRIBUTORS_PATH = "snownee/kiwi/contributor/ContributorsClient.class"
FIELD_NAME = "cosmeticScreenKeybind"
FIELD_OWNER = "snownee/kiwi/KiwiClientConfig"
ON_KEY_INPUT = "onKeyInput"

ICONST_0 = 0x03
ICONST_1 = 0x04
PUTSTATIC = 0xB3
GETSTATIC = 0xB2
NOP = 0x00

# JVM tags we must skip when walking the constant pool
_CP_FIXED = {
    7: 2,   # Class
    8: 2,   # String
    16: 2,  # MethodType
    19: 2,  # Module
    20: 2,  # Package
    15: 3,  # MethodHandle
    3: 4,   # Integer
    4: 4,   # Float
    9: 4,   # Fieldref
    10: 4,  # Methodref
    11: 4,  # InterfaceMethodref
    12: 4,  # NameAndType
    17: 4,  # Dynamic
    18: 4,  # InvokeDynamic
    5: 8,   # Long (takes two CP slots)
    6: 8,   # Double (takes two CP slots)
}


def _parse_cp(data: bytes) -> tuple[list[tuple[int, bytes | None]], int]:
    """Return (cp_entries, offset_after_cp). Index 0 is unused padding."""
    count = struct.unpack_from(">H", data, 8)[0]
    cp: list[tuple[int, bytes | None]] = [(0, None)]
    i = 10
    idx = 1
    while idx < count:
        tag = data[i]
        if tag == 1:  # Utf8
            ln = struct.unpack_from(">H", data, i + 1)[0]
            cp.append((tag, data[i + 3 : i + 3 + ln]))
            i += 3 + ln
        elif tag in _CP_FIXED:
            width = _CP_FIXED[tag]
            payload = data[i + 1 : i + 1 + width]
            cp.append((tag, payload))
            i += 1 + width
            if tag in (5, 6):  # long/double occupy two slots
                cp.append((0, None))
                idx += 1
        else:
            raise ValueError(f"unknown constant-pool tag {tag} at byte {i}")
        idx += 1
    return cp, i


def _utf8(cp: list, index: int) -> str:
    tag, payload = cp[index]
    if tag != 1:
        raise ValueError(f"CP#{index} is not Utf8 (tag={tag})")
    assert payload is not None
    return payload.decode("utf-8", "replace")


def _fieldref_index_for(cp: list, owner: str, name: str) -> int:
    """Resolve Fieldref CP index for owner.name."""
    # Build NameAndType -> (name, desc) and Class -> name maps first
    nat: dict[int, tuple[str, str]] = {}
    classes: dict[int, str] = {}
    for i, (tag, payload) in enumerate(cp):
        if tag == 12 and payload is not None:
            name_idx, desc_idx = struct.unpack(">HH", payload)
            nat[i] = (_utf8(cp, name_idx), _utf8(cp, desc_idx))
        elif tag == 7 and payload is not None:
            (name_idx,) = struct.unpack(">H", payload)
            classes[i] = _utf8(cp, name_idx)
    for i, (tag, payload) in enumerate(cp):
        if tag == 9 and payload is not None:
            class_idx, nat_idx = struct.unpack(">HH", payload)
            if classes.get(class_idx) == owner and nat.get(nat_idx, ("", ""))[0] == name:
                return i
    raise ValueError(f"Fieldref {owner}.{name} not found in constant pool")


def _find_method_code(
    data: bytes, cp: list, cp_end: int, method_name: str
) -> tuple[int, int]:
    i = cp_end
    _access, _this, _super = struct.unpack_from(">HHH", data, i)
    i += 6
    ifaces = struct.unpack_from(">H", data, i)[0]
    i += 2 + 2 * ifaces
    n_fields = struct.unpack_from(">H", data, i)[0]
    i += 2
    for _ in range(n_fields):
        i += 6
        n_attrs = struct.unpack_from(">H", data, i)[0]
        i += 2
        for _ in range(n_attrs):
            i += 2
            ln = struct.unpack_from(">I", data, i)[0]
            i += 4 + ln
    n_methods = struct.unpack_from(">H", data, i)[0]
    i += 2
    code_attr = "Code"
    for _ in range(n_methods):
        _acc, name_idx, _desc = struct.unpack_from(">HHH", data, i)
        i += 6
        found_name = _utf8(cp, name_idx)
        n_attrs = struct.unpack_from(">H", data, i)[0]
        i += 2
        for _ in range(n_attrs):
            attr_name_idx = struct.unpack_from(">H", data, i)[0]
            attr_len = struct.unpack_from(">I", data, i + 2)[0]
            attr_body = i + 6
            if found_name == method_name and _utf8(cp, attr_name_idx) == code_attr:
                code_len = struct.unpack_from(">I", data, attr_body + 4)[0]
                code_off = attr_body + 8
                return code_off, code_len
            i = attr_body + attr_len
    raise ValueError(f"method {method_name} Code attribute not found")


def _find_clinit_with_cp(data: bytes, cp: list, cp_end: int) -> tuple[int, int]:
    return _find_method_code(data, cp, cp_end, "<clinit>")


def patch_contributors_client(class_bytes: bytes) -> tuple[bytes, str]:
    """Force onKeyInput to take the false-branch of cosmeticScreenKeybind.

    onKeyInput starts with getstatic cosmeticScreenKeybind; ifeq <return>.
    Kiwi rewrites kiwi-client.yaml with cosmeticScreenKeybind: true on any
    install that ever booted unpatched, which re-enables the Minecraft.screen
    getfield (removed in 26.2). Replace the 3-byte getstatic with
    iconst_0 + nop + nop so ifeq always jumps to return. Stack maps for the
    fall-through path remain valid (verifier still sees both successors).
    """
    cp, cp_end = _parse_cp(class_bytes)
    try:
        code_off, code_len = _find_method_code(class_bytes, cp, cp_end, ON_KEY_INPUT)
    except ValueError:
        return class_bytes, "not-found"
    if code_len < 3:
        return class_bytes, "not-found"
    code = bytearray(class_bytes[code_off : code_off + code_len])
    if code[0] == ICONST_0 and code[1] == NOP and code[2] == NOP:
        return class_bytes, "already"
    if code[0] != GETSTATIC:
        return class_bytes, "not-found"
    code[0] = ICONST_0
    code[1] = NOP
    code[2] = NOP
    out = bytearray(class_bytes)
    out[code_off : code_off + code_len] = code
    return bytes(out), "patched"


def patch_kiwi_client_config(class_bytes: bytes) -> tuple[bytes, str]:
    """Return (patched_bytes, status) where status is
    'patched' | 'already' | 'not-found'.
    """
    cp, cp_end = _parse_cp(class_bytes)
    try:
        fieldref = _fieldref_index_for(cp, FIELD_OWNER, FIELD_NAME)
    except ValueError:
        return class_bytes, "not-found"

    code_off, code_len = _find_clinit_with_cp(class_bytes, cp, cp_end)
    code = bytearray(class_bytes[code_off : code_off + code_len])

    needle_put = bytes([PUTSTATIC, (fieldref >> 8) & 0xFF, fieldref & 0xFF])
    pos = 0
    hits = []
    while True:
        j = code.find(needle_put, pos)
        if j < 0:
            break
        hits.append(j)
        pos = j + 1
    if not hits:
        return class_bytes, "not-found"

    changed = False
    for j in hits:
        if j == 0:
            continue
        prev = code[j - 1]
        if prev == ICONST_1:
            code[j - 1] = ICONST_0
            changed = True
        # already ICONST_0 -> leave alone (idempotent)

    if not changed:
        # If every hit already has iconst_0 (or no preceding iconst), decide:
        # any iconst_0 immediately before a putstatic means already patched.
        already = any(j > 0 and code[j - 1] == ICONST_0 for j in hits)
        return class_bytes, "already" if already else "not-found"

    out = bytearray(class_bytes)
    out[code_off : code_off + code_len] = code
    return bytes(out), "patched"


def patch_kiwi_jar(path: Path) -> str:
    """Patch one jar in place. Returns 'patched'|'already'|'skip'.

    Patches both KiwiClientConfig.<clinit> (default false) and
    ContributorsClient.onKeyInput (ignore config; always early-return).
    """
    if not path.is_file() or path.suffix != ".jar":
        return "skip"
    try:
        with zipfile.ZipFile(path, "r") as z:
            names = set(z.namelist())
            if CLASS_PATH not in names:
                return "skip"
            replacements: dict[str, bytes] = {}
            statuses: list[str] = []

            original = z.read(CLASS_PATH)
            patched, status = patch_kiwi_client_config(original)
            if status == "not-found":
                return "skip"
            if status == "patched":
                replacements[CLASS_PATH] = patched
            statuses.append(status)

            if CONTRIBUTORS_PATH in names:
                c_orig = z.read(CONTRIBUTORS_PATH)
                c_patched, c_status = patch_contributors_client(c_orig)
                if c_status == "patched":
                    replacements[CONTRIBUTORS_PATH] = c_patched
                statuses.append(c_status)

            if not replacements:
                return "already"

            items = []
            for info in z.infolist():
                data = replacements.get(info.filename) or z.read(info.filename)
                items.append((info, data))
    except zipfile.BadZipFile:
        return "skip"

    tmp = path.with_suffix(path.suffix + ".tmp")
    with zipfile.ZipFile(tmp, "w", zipfile.ZIP_DEFLATED) as out:
        for info, data in items:
            # Fresh ZipInfo avoids duplicate-name / zip64 quirks on rewrite
            ni = zipfile.ZipInfo(info.filename, date_time=info.date_time)
            ni.compress_type = info.compress_type
            ni.external_attr = info.external_attr
            ni.create_system = info.create_system
            out.writestr(ni, data)
    tmp.replace(path)
    return "patched"


def _kiwi_candidate_roots(root: Path) -> list[Path]:
    roots = [
        root / "vendored",
        root / "build" / "dist" / "server" / "mods",
        root / "build" / "dist" / "client" / "mods",
        root / "dist" / "server" / "mods",
        root / "dist" / "client" / "mods",
    ]
    # Prism test instances so a bare update_prism / hand-copy cannot ship
    # an unpatched ContributorsClient again.
    prism = Path.home() / "Library" / "Application Support" / "PrismLauncher" / "instances"
    for inst in ("Hearthwind-Full", "Hearthwind-Minimal", "Hearthwind-Dev-Client"):
        mods = prism / inst / "minecraft" / "mods"
        if mods.is_dir():
            roots.append(mods)
    return [r for r in roots if r.is_dir()]


def patch_all(conversion_root: Path | None = None) -> dict[str, int]:
    """Patch every kiwi jar under conversion/ and managed Prism instances."""
    if conversion_root is None:
        conversion_root = Path(__file__).resolve().parents[1]
    counts = {"patched": 0, "already": 0, "skip": 0}
    seen: set[Path] = set()
    for root_dir in _kiwi_candidate_roots(conversion_root):
        for jar in sorted(root_dir.glob("kiwi*.jar")):
            key = jar.resolve()
            if key in seen:
                continue
            seen.add(key)
            status = patch_kiwi_jar(jar)
            counts[status] = counts.get(status, 0) + 1
            if status == "patched":
                label = (
                    str(jar.relative_to(conversion_root))
                    if jar.is_relative_to(conversion_root)
                    else str(jar)
                )
                print(f"  patch_vendored: patched {label}")
    return counts


def main(argv: list[str]) -> int:
    root = Path(argv[1]).resolve() if len(argv) > 1 else None
    counts = patch_all(root)
    total = counts["patched"] + counts["already"]
    print(
        f"patch_vendored: {counts['patched']} patched, "
        f"{counts['already']} already ok, {counts['skip']} skipped"
    )
    # Fail loudly if a kiwi jar exists but could not be found/patched at all
    # when we expected the class — patch_all only counts skip for non-kiwi.
    return 0 if total > 0 else 1


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))
