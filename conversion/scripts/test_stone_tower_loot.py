import contextlib
import hashlib
import io
import json
import tempfile
import unittest
from copy import deepcopy
from pathlib import Path
from unittest.mock import patch

import migrate_datapack as migrator


ROOT = Path(__file__).resolve().parents[2]
TABLE = Path("data/formationsoverworld/loot_table/stone_tower/smithing.json")
WITCH_TABLE = Path("data/formationsoverworld/loot_table/witch_tower/smithing.json")
MANDATORY_TABLES = (TABLE, WITCH_TABLE)
CURATED = ROOT / "conversion/curated/datapack-overrides"
UPSTREAM_JAR = "formationsoverworld-1.0.5a-mc1.21+.jar"
UPSTREAM_JAR_SHA256 = "bfc0369c5a5d341420712017bc5a1abf91091b517c0026be8c615d625a346bc6"
UPSTREAM_JSON_SHA256 = "73c2cdae3af0f7ef7889912ff5ef44fc29489fee8ababb0cefa6b1b82d953a61"
UPSTREAM_WITCH_JSON_SHA256 = "9f3b81d0fb1af88a0c9951483887990bb04d9d137d87dac941d94ab5354eb4ed"


class StoneTowerLootTests(unittest.TestCase):
    def test_exact_upstream_semantics_except_chain_rename(self):
        text = (CURATED / TABLE).read_text()
        self.assertNotIn('"minecraft:chain"', text)
        self.assertEqual(text.count('"minecraft:iron_chain"'), 1)
        baseline = json.loads(text.replace('"minecraft:iron_chain"', '"minecraft:chain"'))
        normalized = json.dumps(baseline, sort_keys=True, separators=(",", ":")).encode()
        self.assertEqual(hashlib.sha256(normalized).hexdigest(), UPSTREAM_JSON_SHA256)

    def test_witch_tower_upstream_semantics_except_chain_rename(self):
        text = (CURATED / WITCH_TABLE).read_text()
        self.assertNotIn('"minecraft:chain"', text)
        self.assertEqual(text.count('"minecraft:iron_chain"'), 1)
        baseline = json.loads(text.replace('"minecraft:iron_chain"', '"minecraft:chain"'))
        normalized = json.dumps(baseline, sort_keys=True, separators=(",", ":")).encode()
        self.assertEqual(hashlib.sha256(normalized).hexdigest(), UPSTREAM_WITCH_JSON_SHA256)

    def test_generated_tables_match_canonical(self):
        for table in MANDATORY_TABLES:
            with self.subTest(table=table):
                self.assertEqual(
                    (ROOT / "conversion/datapacks/hearthwind" / table).read_bytes(),
                    (CURATED / table).read_bytes(),
                )

    def test_copy_is_narrow_and_repeatable(self):
        scratch = ROOT / ".tmp"
        scratch.mkdir(exist_ok=True)
        with tempfile.TemporaryDirectory(dir=scratch) as temporary:
            out = Path(temporary)
            sentinel = out / "data/formationsoverworld/loot_table/unrelated.json"
            sentinel.parent.mkdir(parents=True)
            sentinel.write_bytes(b"preserve unrelated output")
            for _ in range(2):
                migrator.copy_mandatory_overrides(out)
                for table in MANDATORY_TABLES:
                    self.assertEqual((out / table).read_bytes(), (CURATED / table).read_bytes())
                self.assertEqual(sentinel.read_bytes(), b"preserve unrelated output")
                self.assertEqual(
                    {p.relative_to(out) for p in out.rglob("*.json")},
                    set(MANDATORY_TABLES) | {sentinel.relative_to(out)},
                )

    def test_main_restores_override_after_generic_migration(self):
        scratch = ROOT / ".tmp"
        scratch.mkdir(exist_ok=True)
        canonical = (CURATED / TABLE).read_bytes()
        original = canonical.replace(b'"minecraft:iron_chain"', b'"minecraft:chain"')
        with tempfile.TemporaryDirectory(dir=scratch) as temporary:
            root = Path(temporary)
            src = root / "source"
            out = root / "output"
            source_table = src / TABLE
            source_table.parent.mkdir(parents=True)
            source_table.write_bytes(original)
            flatten = migrator.flatten_uniform_providers

            def verify_generic_output(data):
                self.assertEqual(json.loads((out / TABLE).read_bytes()), json.loads(original))
                self.assertFalse((out / WITCH_TABLE).exists())
                flatten(data)

            with (
                patch.object(migrator, "OUT", out),
                patch.object(migrator, "report", deepcopy(migrator.report)),
                patch.object(migrator, "installed_namespaces", return_value={"minecraft"}),
                patch.object(migrator, "flatten_uniform_providers", side_effect=verify_generic_output) as generic,
                patch.object(migrator.sys, "argv", ["migrate_datapack.py", str(src)]),
                contextlib.redirect_stdout(io.StringIO()),
            ):
                for _ in range(2):
                    migrator.main()
                    self.assertEqual((out / TABLE).read_bytes(), canonical)
                    self.assertEqual(
                        (out / WITCH_TABLE).read_bytes(), (CURATED / WITCH_TABLE).read_bytes()
                    )
                    self.assertEqual(
                        sorted(p.relative_to(out) for p in (out / "data").rglob("*.json")),
                        sorted([TABLE, WITCH_TABLE]),
                    )
                self.assertEqual(generic.call_count, 2)

    def test_world_bundles_only_scoped_canonical_override(self):
        build = (ROOT / "custom-mods/build.gradle").read_text()
        block = build.split("if (project.name == 'hearthwind-world') {", 1)[1].split(
            "if (project.name == 'hearthwind-primitive') {", 1
        )[0]
        for table in MANDATORY_TABLES:
            self.assertIn(f"\t\t\t\tinclude '{table.as_posix()}'\n", block)
        self.assertIn("duplicatesStrategy = DuplicatesStrategy.FAIL", block)
        self.assertEqual(block.count("include '"), 4)


if __name__ == "__main__":
    unittest.main()
