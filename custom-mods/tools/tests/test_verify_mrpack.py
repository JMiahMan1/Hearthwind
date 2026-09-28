"""Self-tests for verify_mrpack.py.

The real bug this guards: a published pack was rejected by the Modrinth
App with "unknown element fabric-api" because our `dependencies` map
carried a mod id, and our verifier only checked that the map was non-empty.
These fixtures encode the failure classes a launcher cares about, so the
checker itself is tested rather than trusted.

Run: python3 custom-mods/tools/tests/test_verify_mrpack.py
"""

import hashlib
import io
import json
import sys
import unittest
import zipfile
from pathlib import Path

REPO = Path(__file__).resolve().parents[3]
TOOLS = REPO / "custom-mods" / "tools"
sys.path.insert(0, str(TOOLS))

import verify_mrpack  # noqa: E402

GOOD_JAR = b"fake-mod-jar-bytes"


def _index(**overrides):
    payload = GOOD_JAR
    entry = {
        "path": "mods/fake-mod-1.0.0.jar",
        "hashes": {
            "sha1": hashlib.sha1(payload).hexdigest(),
            "sha512": hashlib.sha512(payload).hexdigest(),
        },
        "env": {"client": "required", "server": "required"},
        "downloads": ["https://cdn.modrinth.com/data/AAAA/versions/BBBB/fake-mod-1.0.0.jar"],
        "fileSize": len(payload),
    }
    index = {
        "formatVersion": 1,
        "game": "minecraft",
        "name": "Fixture Pack",
        "versionId": "0.0.1",
        "dependencies": {"minecraft": "26.2", "fabric-loader": "0.19.5"},
        "files": [entry],
    }
    index.update(overrides)
    return index


def write_pack(path: Path, index: dict, extra_entries: dict | None = None) -> None:
    with zipfile.ZipFile(path, "w", zipfile.ZIP_DEFLATED) as zf:
        zf.writestr("modrinth.index.json", json.dumps(index, indent=2))
        for name, data in (extra_entries or {}).items():
            zf.writestr(name, data)


class VerifyMrpackTests(unittest.TestCase):
    def setUp(self):
        self.tmp = REPO / ".tmp" / "verify_mrpack_tests"
        self.tmp.mkdir(parents=True, exist_ok=True)

    def check(self, index: dict, extra: dict | None = None) -> list[str]:
        path = self.tmp / "pack.mrpack"
        write_pack(path, index, extra)
        problems: list[str] = []
        verify_mrpack.check_pack(path, problems)
        return problems

    def test_valid_pack_passes(self):
        self.assertEqual(self.check(_index()), [])

    def test_mod_id_in_dependencies_is_rejected(self):
        # The exact regression: the Modrinth App refused our packs with
        # "unknown element fabric-api".
        problems = self.check(_index(dependencies={
            "minecraft": "26.2", "fabric-loader": "0.19.5", "fabric-api": "0.161.0+26.2",
        }))
        self.assertTrue(any("fabric-api" in p for p in problems), problems)

    def test_unknown_loader_element_is_rejected(self):
        problems = self.check(_index(dependencies={"minecraft": "26.2", "some-mod": "1.0.0"}))
        self.assertTrue(problems, "an unknown element must fail the check")

    def test_missing_minecraft_dependency_is_rejected(self):
        problems = self.check(_index(dependencies={"fabric-loader": "0.19.5"}))
        self.assertTrue(problems, "minecraft must be a dependency")

    def test_malformed_sha1_is_rejected(self):
        # The bytes of an index file are not in the pack (the launcher
        # downloads them), so a static check can only validate the format;
        # --deep re-hashes the real file.
        bad = _index()
        bad["files"][0]["hashes"]["sha1"] = "0" * 39
        self.assertTrue(self.check(bad))

    def test_malformed_sha512_is_rejected(self):
        bad = _index()
        bad["files"][0]["hashes"]["sha512"] = "f" * 127
        self.assertTrue(self.check(bad))

    def test_non_positive_file_size_is_rejected(self):
        bad = _index()
        bad["files"][0]["fileSize"] = 0
        self.assertTrue(self.check(bad))

    def test_bad_env_value_is_rejected(self):
        bad = _index()
        bad["files"][0]["env"] = {"client": "true", "server": "true"}
        self.assertTrue(self.check(bad))

    def test_empty_downloads_is_rejected(self):
        bad = _index()
        bad["files"][0]["downloads"] = []
        self.assertTrue(self.check(bad))

    def test_http_download_is_rejected(self):
        bad = _index()
        bad["files"][0]["downloads"] = ["http://cdn.modrinth.com/data/AAAA/x.jar"]
        self.assertTrue(self.check(bad))

    def test_index_path_also_in_overrides_is_rejected(self):
        # A launcher cannot decide which copy wins; our pack must not ship both.
        index = _index()
        self.assertTrue(self.check(index, {"overrides/mods/fake-mod-1.0.0.jar": GOOD_JAR}))

    def test_unknown_override_folder_is_rejected(self):
        self.assertTrue(self.check(_index(), {"overrides/whatever/x.txt": "hi"}))

    def test_corrupt_override_jar_is_rejected(self):
        self.assertTrue(self.check(_index(), {"overrides/mods/broken.jar": b"not a zip at all"}))


if __name__ == "__main__":
    unittest.main(verbosity=2)
