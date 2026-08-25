# Hearthwind

A challenging medieval survival pack where realism & wisdom rule your life.
Grown from the [Aged](https://github.com/xR4YM0ND/Aged) fork - inspired by
Aged and by [Genesis](https://github.com/marianyp/Genesis) - now finding its
own way. Server-authoritative, vanilla-joinable: a vanilla 26.2 client connects with no mods; an optional client companion adds HUD.

You awaken in a wilderness where **fresh water**, **body temperature**, a
**balanced diet** and **spoiling food** matter. You **gather supplies** and
craft **flint tools** before stone is even within reach, while
**monsters grow stronger** the farther you wander from spawn. Raise your
**twelve skills**, choose a **job**, and weather the **seasons**.
*That's where your story begins.*

| | |
|---|---|
| Status | in development (Minecraft 26.2, Fabric) - [Releases](https://github.com/JMiahMan1/Hearthwind/releases) `Hearthwind-0.1.0-mc26.2.mrpack` + `HearthwindClient` companion |
| Docs | `docs/INSTALL.md` (server + client) · `docs/PLAYER_CHANGES.md` (what's changed) · `docs/FEATURE_PARITY.md` (vs Aged) · `docs/PROJECT_DIRECTION.md` (Ages & vision) · `docs/CONVERSION.md` (porting notes) |
| Testing | GHA `build-and-test.yml` - `./gradlew build` (6 modules) + 19 headless gametests on every push |

## How to Play - Thematic Breakdown

Hearthwind is **realism → earned unlock → harder frontier → one best → slow tech** (`docs/PROJECT_DIRECTION.md#north-star`). If vanilla does it instantly, Hearthwind adds a cost, a skill, or a season.

### 1 - Awaken Stranded (Age 0: Flint & Thirst)

You punch **stone for rocks**, not cobble. Three rocks → cobble, flint knapped into **flint tools** (`earlystage:flint_pickaxe/axe/shovel/hoe/sword`, `stone_shears` - `hearthwind-primitive:33K`). Repairs use `earlystage:flint_tool_repair`. Ore comes as **pieces/nuggets** (9 → 1 ingot: `agedaddition:coal_piece` … `raw_iron_nugget`) so early metal is scarce. Tanning is physical: **4 rotten flesh → 1 leather** (datapack, no extra bench).

You are thirsty from tick one. **Thirst** drains ~0.4/s, ×2 sprinting, ×0.6 per Thirst effect level (`config/hearthwind_survival.json:thirst`). Below 6/20 natural regen stops; at 0 you take 1 damage/4s. Drink via `dehydration:water_bowl` → purify to `purified_water_bowl` to avoid the effect. Warm armor (`environmentz:warm_armor`) and `dehydration:*` bowls are stacked in your inventory for later.

### 2 - Body, Diet, and Decay (Survival v1)

- **Temperature** drifts 0.05/s toward your biome target (plains ~comfort, desert +9, frozen −9). `warm` pelts pull you warm, `non_affecting` dampens, holding an `ice_pack` or `insolating_item` nudges, rain/water/underground bias, being on fire maxes. At −8 you freeze, at +7 you exhaust, at +9 you burn (`temperature: freezeHurtAt/heatHurtAt`, 4s cooldown).
- **Diet** - five tags `nutritionz:fruits/vegetables/grains/proteins/sugars`, each 0..100, decay 0.02/s. Eating tagged food refills via `Consumable#onConsume` (`nutritionz` mixin). Deficient <15 gives **mining fatigue** (fruit), **weakness** (veg/proteins), **slowness** (grains); all ≥50 → refreshed **absorption hearts**.
- **Spoilage** - `spoiledz:perishable_items` roll 0.002 per 10s per stack → `rotten_flesh`; `non_spoiling_items` (honey, teas, wines) exempt, hot biomes ×2.

All numbers are datapack/config tunable, no hardcoding. HUD bars show these on the **client companion**; vanilla sees action-bar warnings - both playable.

### 3 - Skills Gate Everything (Age-gated progression)

12 skills to 30 (`levelz:xp`, triangular `baseXpPerLevel * N`, `config/hearthwind_skills.json:12`). Farming = crops/animals, Mining = pickaxe, Stamina = shovel, Strength = melee, Archery = bow/trident, plus Health/Defense/Agility/Luck/Smithing/Alchemy/Trade passives (`hearthwind_skills:<skill>`).

Progression is **enforced**: `SkillGates` (`data/hearthwind_skills/gates` from 400-file `levelz` corpus)
- **Break:** `minecraft:stone` needs `mining 5`, `mud_bricks 1` … `obsidian 25`, `ancient_debris 27`
- **Use:** `furnace/anvil/smithing_table` → `smithing 3/1`, `brewing_stand/cauldron` → `alchemy 1-5`, `smoker/beehive/composter` → `farming 10-31`, `cartography 8 → agility`, `grindstone 15 → strength`

Fail = action-bar hint, not silent. Missing/modded blocks are simply not gated.

### 4 - Choose a Job (Horizontal progression)

`/job join <id>`, `/job leave`, `/job info` (tab-complete, `hearthwind-jobs:30K`). Eight: **fisher/miner/farmer/warrior/smither/brewer/builder/lumberjack** (definitions in `data/aged_jobs/jobs`, 4/4 gametests). One at a time, level via matching blocks/entities/items (`miner: coal 3 → copper 4 → iron 7 → diamond 20 → ancient_debris 30`; `warrior: chicken 1 → cow 2 → zombie 5 → warden 150`, etc.). Curve = `pointsPerLevel * L` (default 100, `config/hearthwind_jobs.json`). Job-restricted recipes + bonus rewards are next (reuse gate infra).

### 5 - The Frontier Pushes Back

Monsters scale with **distance from spawn** (`MobScaling.java:1`): +2 HP/+0.5 dmg per 1000 past 500 grace, cap 20 (`config/hearthwind_skills.json:mobScaling`). Future scaling also mixes **aggregate power** (total skill levels/40). Staying near spawn is forgiving; venturing for better ores is genuinely dangerous - risk/reward stays proportional by design.

### 6 - Seasons Turn (World Ages 1→5)

`hearthwind-world:5.5K` seasons-lite: `Season.fromWorldTime(gameTime, daysPerSeason)` → `SPRING/SUMMER/AUTUMN/WINTER` every 21 MC days (default). Each carries **`tempOffset`** (−3 winter … +2 summer) consumed by survival drift, and **`cropMultiplier`** (1.0 spring, 1.2 summer, 0.9 autumn, 0.4 winter) to hook into crop growth (`config/hearthwind_world.json`). Greenhouses and underground farming will be season-proof later. Water motion (river currents, ocean swell, tides → shaders) is tracked in `ideas/rivers-and-waves.md` - **not** in this release.

### 7 - Technology Slowly, Hard-Fought (Ages)

No creative skipping - tech unlocks in ordered **Ages** (`PROJECT_DIRECTION.md#ages`), each gated by skills/jobs/advancements:

| Age | Entry | Unlocks | Frontier |
|---|---|---|---|
| 0 Stranded | rocks + flint | Flint tools, campfire, tanning | baseline |
| 1 Camp | `farming 3` + `mining 1` | **Sieve** (`earlystage:sieve_drops/aged_drops.json` - single sieve), compost farmland, crude storage | seasons matter |
| 2 Copper | `mining 4`, `smithing 3` | Copper tools, watering can | +1 step |
| 3 Iron/Steel | `mining 7`, `smithing 14` | Iron → **steel** (`earlystage:steel_ingot/nugget/block` shipped) | +2 steps |
| 4 Steel & Craft | `smithing 18`, `builder 3` | Steel tools/block, `chipped` benches, Create **wind/water wheel preview** | +3 steps |
| 5 Mechanical | `smithing 20`, `builder 5` | Full **Create** automation, powered pumps | scales with player power |

Next build: **Sieve (Age 1) + Age advancements** with instruction toasts (Genesis-style), then Steel wiring, then gated Create. `tiered` random affixes land after sieve. One-best rule: single sieve, single farm system, single storage - count per need goes down.

### 8 - Server & Client are Both First-Class

- **Server** (`environment="*"`) - authoritative: thirst/diet/spoilage/temperature, XP, gates, scaling, sieve, seasons calendar, advancements. Writes `hearthwind_*.json`. Broadcasts `hearthwind:*` sync payloads; vanilla clients ignore them.
- **Client companion** (`hearthwind-client:2.2K`, `environment="client"`, `ClientModInitializer`) - **presentation only**: HUD bars, instruction toasts, water-motion preview. Never writes authority. Without it you get fully playable action-bar/chat fallbacks.

See `docs/PROJECT_DIRECTION.md#distribution-model` for payload matrix and `docs/INSTALL.md` for both install paths.

## Install - Players & Admins

**Players (no mods needed):** vanilla 26.2 → `Multiplayer → Add Server → <host>:25565`. For HUD, install optional `HearthwindClient-0.1.0-mc26.2.mrpack` via Modrinth App or drop `hearthwind-client-26.2+0.1.0.jar` into Prism `mods/` (see `docs/INSTALL.md#players-joining-a-server`).

**Server admins:**
- **Modrinth pack (recommended):** `Hearthwind-0.1.0-mc26.2.mrpack` (41 Modrinth deps + `world/datapacks/aged-server/`) → Modrinth App From file. Hand `HearthwindClient-*.mrpack` to players.
- **Plain dir (offline):** unzip `hearthwind-server-0.1.0-mc26.2.zip` (from [Releases](https://github.com/JMiahMan1/Hearthwind/releases/tag/v0.1.0) or `conversion/build/dist/server/`) - `fabric-server.jar` deps + `world/datapacks/aged-server/` + 5× `hearthwind-*` jars → `echo "eula=true" > eula.txt` → `java -Xmx3G -jar fabric-server.jar nogui` (RCON vanilla keys). Client analog is `client/mods/` (47 jars incl. `hearthwind-client`).

**Developers:**
```bash
git clone https://github.com/JMiahMan1/Hearthwind.git && cd Hearthwind
git checkout server-26.2
python3 conversion/scripts/resolve_deps.py          # writes conversion/build/resolved.json (41/57 ok)
python3 conversion/scripts/build_pack.py --server-dir  # Hearthwind-*.mrpack + server/ + client/ + legacy aliases
cd custom-mods && ./gradlew build --no-daemon --max-workers=2  # 6 modules → hearthwind-*-26.2+0.1.0.jar
bash tools/run_gametests.sh  # 19 headless green (8 survival + 7 skills + 4 jobs)
```

## Credits

- **[Aged](https://github.com/xR4YM0ND/Aged)** (xR4YM0ND & contributors) - original pack; tuning corpus and inspiration.
- **[Genesis](https://github.com/marianyp/Genesis)** (marianyp) - progression gating & instruction toast design inspiration, rebuilt in-house.

## License

MIT for our code (see LICENSE.md). Upstream assets remain courtesy of their authors - see ATTRIBUTION.md as assets are adopted.
