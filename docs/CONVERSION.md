# Aged -> Modern Minecraft (26.2) Server Conversion

Fork of [xR4YM0ND/Aged](https://github.com/xR4YM0ND/Aged) (MIT) rebuilt as a
**server-focused** pack targeting modern Minecraft releases (`26.2`), with an easy bump
path to future releases and snapshots.

## Verdict of the feasibility study (2026-08-22)

- Target landscape: latest release `26.2` (2026-06-16), latest snapshot
  `26.3-snapshot-9` (2026-08-17).
- Loaders: **Fabric** supports both releases and snapshots.
- Custom in-house modules in `custom-mods/` provide clean, high-performance replacements for all core mechanics.

## Custom replacement mods (Fabric 26.2, multi-module Gradle):

| Mod | Replaces | Scope & Shipped Systems |
|---|---|---|
| `hearthwind-survival` | dehydration, environmentz, nutritionz, spoiledz | Thirst, temperature, 5-group diet & deficiency debuffs, food spoilage |
| `hearthwind-skills` | levelz, rpgdifficulty | 12 skills, 3-heart starting progression (+0.5 heart/level), 1,087 content gates, distance mob scaling |
| `hearthwind-jobs` | jobs-addon (8 jobs) | 8 professions, level curves, `/job` commands, Age tech gating |
| `hearthwind-primitive` | earlystage, tiered, reciperemover | Surface rock/flint mounds, knapping start, 199 Tiered affixes & reforging |
| `hearthwind-world` | fabric-seasons, seasonhud, crop_growth_modifier | 18-day seasons, temp/crop multipliers, winter snow layering, waterfowl & flora |
| `hearthwind-client` | client HUDs, tabs, main menu | 10 droplets + flask, thermometer trio, season widget, 4-tab strip, Aged main menu |

## Shipped Progression & Parity Highlights

1. **Starting 3-Heart Progression**:
   - Players start with 3 Hearts (6.0 Max HP).
   - Health skill levels up to +0.5 heart per level (10 hearts at Level 14, 18 hearts at Level 30).
2. **LevelZ 400 Gate Corpus (1,087 Gated Entries)**:
   - Full integration of mining, smithing, alchemy, crafting, item equipment, and animal husbandry gates.
3. **Visual Inventory & HUD Parity**:
   - 10 discrete teardrops + flask icon above hunger.
   - Thermometer tube + 12×12 "F" unit box + trend chevron right of hotbar.
   - Top-left 9×9 season widget with 18-day calendar.
   - 4-tab strip (Inventory, Skills, Jobs, Nutrients) on vanilla `#C6C6C6` panels.
4. **Water Dynamics & River Currents**:
   - Balanced river drift and ocean swell with directional bubble spray particles.
5. **Start Screen Art & Layout**:
   - Custom painted HearthWind dark-fantasy cabin art.
   - Left-aligned button stack with Aged hover tints (`#EEDAC3`, `#A1B8B5`, `#6AA7BA`, `#BFA8BF`, `#EB9484`).
   - Top-right icon buttons (Discord, Modrinth, Language, Accessibility).

---

## Toolchain & Verification

```bash
# Build custom mods
cd custom-mods && ./gradlew build --no-daemon --max-workers=2

# Run headless gametest suite (140/140 passed)
bash tools/run_gametests.sh

# Sync jars to Prism Launcher & live dev server
bash tools/sync_test_clients.sh
```
