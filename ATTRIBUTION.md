# Asset attribution

Every non-original asset shipped or referenced by this project is
recorded here at copy time (see docs/PROJECT_DIRECTION.md Phase A rules).

| Asset path | Origin project / author | License | Replacement plan |
|---|---|---|---|
| `custom-mods/hearthwind-primitive/src/main/resources/assets/earlystage/textures/` (block/flint, block/steel_block, item/rock, item/flint, item/flint_* tools (5), item/stone_shears, item/steel_ingot, item/steel_nugget) | [earlystage](https://modrinth.com/mod/earlystage) (Globox_Z) — verbatim copies from earlystage-1.1.1.jar | MIT | Keep (identity assets; tweak only if art pass upgrades them) |
| `custom-mods/hearthwind-client/src/main/resources/assets/hearthwind/textures/gui/sprites/nutrition/` (panel, arrow, arrow_hover, bar_bg_*, bar_fill_*) | [NutritionZ](https://github.com/xR4YM0ND/NutritionZ) (xR4YM0ND) — pixel-exact crops from `icons.png` (verified against atlas) | MIT | Keep (Aged parity UI) |
| `custom-mods/hearthwind-client/src/main/resources/assets/hearthwind/textures/gui/tab_sheet.png` | [LibZ](https://github.com/Globox1997/LibZ) (Globox_Z) — verbatim `textures/gui/icons.png` (inventory tab strip variants) | MIT | Keep (Aged-parity tab look) |
| `custom-mods/hearthwind-survival/src/main/resources/assets/dehydration/` (item textures `*_leather_flask.png` + `*_empty_leather_flask.png`, 10 files) | [Dehydration](https://modrinth.com/mod/dehydration) — verbatim copies from the reference source tree | GPL-3.0 (mod) — our survival-module code that renders them is ours; the GPL applies to these copied textures | Replace with our own flask art in the real-art pass |
| `custom-mods/hearthwind-primitive/src/main/resources/assets/hearthwind/textures/item/clay_cup*.png` (8 variants) | Silhouette `cupinv.png` from [Beverage](https://github.com/minetest-mods/beverage) (Mahmut Elmas) — recolored to fired/unfired clay, cavity filled, thin ellipse liquid drawn on top | MIT | Replace with our own hand-drawn mug in the real-art pass |

Own art (no attribution owed): `hud/thirst_{empty,half,full}.png` (droplet
SHAPE art), `hud/thermometer_*.png`, `hud/job_*.png`, `hud/temp_*.png`;
rock/flint block *models* are earlystage's (MIT) but render vanilla
`block/stone` texture.
