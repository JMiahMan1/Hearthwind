import json
import re
import subprocess
import unittest
from collections import Counter
from pathlib import Path


ROOT = Path(__file__).resolve().parent
RESOURCES = ROOT / "src/main/resources"
JAVA = ROOT / "src/main/java/earth/terrarium/chipped/common"
STATIONS = {
    "alchemy_bench",
    "botanist_workbench",
    "carpenters_table",
    "glassblower",
    "loom_table",
    "mason_table",
    "tinkering_table",
}
COPPER = {
    "waxed_copper_block": "unaffected",
    "waxed_exposed_copper": "exposed",
    "waxed_weathered_copper": "weathered",
    "waxed_oxidized_copper": "oxidized",
}


def load(path):
    return json.loads(path.read_text())


def normalize(value):
    if isinstance(value, list):
        return [normalize(entry) for entry in value]
    if isinstance(value, dict):
        if set(value) == {"tag"}:
            return "#" + value["tag"]
        if set(value) == {"item"}:
            return value["item"]
        return {key: normalize(entry) for key, entry in value.items()}
    return value


class ResourceTests(unittest.TestCase):
    def test_recipe_schema_and_inventory(self):
        recipes = sorted(RESOURCES.glob("data/*/recipe/**/*.json"))
        types = Counter()
        for path in recipes:
            with self.subTest(path=path):
                recipe = load(path)
                types[recipe["type"]] += 1
                ingredients = recipe.get("ingredients", list(recipe.get("key", {}).values()))
                self.assertTrue(ingredients)
                for ingredient in ingredients:
                    self.assertIsInstance(ingredient, str)
                    self.assertRegex(ingredient, r"^#?[a-z0-9_.-]+:[a-z0-9_./-]+$")
                    if ingredient.startswith("#chipped:"):
                        tag = ingredient.removeprefix("#chipped:")
                        self.assertTrue(load(RESOURCES / f"data/chipped/tags/item/{tag}.json")["values"])
                if recipe["type"] == "minecraft:crafting_shaped":
                    symbols = set("".join(recipe["pattern"])) - {" "}
                    self.assertEqual(symbols, set(recipe["key"]))
                if not recipe["type"].startswith("chipped:"):
                    self.assertEqual(recipe["result"]["count"], 1)
        self.assertEqual(types, {
            "chipped:alchemy_bench": 1,
            "chipped:botanist_workbench": 1,
            "chipped:carpenters_table": 1,
            "chipped:glassblower": 1,
            "chipped:loom_table": 1,
            "chipped:mason_table": 1,
            "chipped:tinkering_table": 1,
            "minecraft:crafting_shaped": 7,
            "minecraft:crafting_shapeless": 6,
        })
        for station in STATIONS:
            self.assertEqual(load(RESOURCES / f"data/chipped/recipe/{station}.json")["type"], f"chipped:{station}")
            self.assertEqual(load(RESOURCES / f"data/minecraft/recipe/workbench/{station}.json")["result"]["id"], f"chipped:{station}")

    def test_copper_registrations_and_required_tags(self):
        blocks = (JAVA / "registry/ModBlocks.java").read_text()
        palettes = (JAVA / "palette/Palettes.java").read_text()
        palette = re.search(r"Palette COPPER_BLOCK = PaletteBuilder.create\(\)(.*?)\.build\(\)", palettes, re.S).group(1)
        variants = re.findall(r'"([^"]+)"', palette)
        self.assertEqual(len(variants), 11)
        alchemy = load(RESOURCES / "data/chipped/recipe/alchemy_bench.json")["ingredients"]
        pickaxe = load(RESOURCES / "data/minecraft/tags/block/mineable/pickaxe.json")["values"]
        for base, state in COPPER.items():
            with self.subTest(base=base):
                self.assertIn(f"{base.upper()} = createRegistry(Blocks.COPPER_BLOCK.waxed().{state}(), Palettes.COPPER_BLOCK);", blocks)
                tag = "waxed_exposed_copper_block" if state == "exposed" else base
                expected = [base] + ["chipped:" + variant.replace("%", base) for variant in variants]
                for kind in ("block", "item"):
                    self.assertEqual(load(RESOURCES / f"data/chipped/tags/{kind}/{tag}.json")["values"], expected)
                self.assertIn(f"#chipped:{tag}", alchemy)
                self.assertIn(f"#chipped:{tag}", pickaxe)
                for entry in expected[1:]:
                    name = entry.removeprefix("chipped:")
                    for asset in ("blockstates", "models/block", "models/item"):
                        self.assertTrue(load(RESOURCES / f"assets/chipped/{asset}/{name}.json"))
                    with self.subTest(item=entry):
                        definition = load(RESOURCES / f"assets/chipped/items/{name}.json")
                        self.assertEqual(definition, {
                            "model": {
                                "type": "minecraft:model",
                                "model": f"chipped:item/{name}",
                            },
                        })
                        namespace, model = definition["model"]["model"].split(":")
                        item_model = load(RESOURCES / f"assets/{namespace}/models/{model}.json")
                        self.assertEqual(item_model, {"parent": f"chipped:block/{name}"})
                        namespace, model = item_model["parent"].split(":")
                        block_model = load(RESOURCES / f"assets/{namespace}/models/{model}.json")
                        self.assertEqual(block_model, {
                            "parent": "minecraft:block/cube_all",
                            "textures": {"all": f"chipped:block/{base}/{name}"},
                        })
                        namespace, texture = block_model["textures"]["all"].split(":")
                        texture_path = RESOURCES / f"assets/{namespace}/textures/{texture}.png"
                        self.assertTrue(texture_path.read_bytes().startswith(b"\x89PNG\r\n\x1a\n"))
        self.assertIn("ModItems.createItemRegistry(registry, itemType);", blocks)

    def test_ingredient_codec_remains_native(self):
        source = (JAVA / "recipes/ChippedRecipe.java").read_text()
        self.assertIn('Ingredient.CODEC.listOf().fieldOf("ingredients")', source)

    def test_recipe_semantics_match_pre_fix_baseline(self):
        repo = ROOT.parent.parent
        baseline = "3538c52e1"
        for path in sorted(RESOURCES.glob("data/*/recipe/**/*.json")):
            with self.subTest(path=path):
                original = subprocess.check_output(
                    ["git", "show", f"{baseline}:{path.relative_to(repo).as_posix()}"],
                    cwd=repo,
                    text=True,
                )
                expected = normalize(json.loads(original))
                rel = path.relative_to(RESOURCES).as_posix()
                if rel.startswith("data/chipped/recipe/") and path.stem in STATIONS:
                    expected["type"] = f"chipped:{path.stem}"
                self.assertEqual(load(path), expected)


if __name__ == "__main__":
    unittest.main()
