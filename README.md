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

You are thirsty from tick one. **Thirst** is `dehydration:hydration` 0..20, synced `hearthwind:thirst` to the client HUD (10 blue droplets 8px apart, 10px above hunger; wobbles when ≤6 - vanilla clients get action-bar warnings). Drain is `baseDrainPerSecond 0.025` (~13 min to empty, ×2 sprinting, +0.05 per `dehydration:thirst` level, hot biomes ×2), configured in `config/hearthwind_survival.json:thirst`. Warnings: 12 yellow "getting thirsty", 6 gold "dehydrated!", 3 red "dying of thirst!". Below 6 natural health regen stops; at 0 you take 1 damage every 4s.

Water is deliberately easy on day 0, hard to keep pure later:

- **Day 0 in 30 seconds:** punch trees → craft `minecraft:bowl` (3 planks) → right-click any **water source** with bowl → `dehydration:water_bowl` (+6 hydration, 50% chance of `dehydration:thirst` 15s). No bucket, no iron.
- **Truly stranded:** right-click water with **empty hand** → tiny dirty sip (+1 hydration, 90% thirst 20s, 0.6 exhaustion, 3s cooldown, hint "barely helps" - deliberately tedious, craft a bowl).
- **Clean water:** `water_bowl` → `purified_water_bowl` on a **campfire** (campfire_cooking) → same +6 but no effect. Campfire is 3 sticks + 1 coal/charcoal + 3 logs - also day-0.
- **Hot water scalds:** water scooped from a **heated cauldron** (water_cauldron over lit campfire/fire) or from a **hot biome at noon** (desert ≥1.5 base, target ≥7) comes as `hot_water_bowl` / `hot_purified_water_bowl` (red name, boiling bubbles texture, "Steaming hot!" tooltip, `CustomData hearthwind:hot_until` 30s). Drinking while hot deals **2 damage (1 heart) + 1s fire + thirst** and only +3 hydration; wait 30s and it cools to the normal bowl (no burn). Indication is red tint, tooltip, `FIRE_EXTINGUISH` hiss and overlay "Ouch! Still scalding hot! (Xs left)". Purified hot still scalds.
- **Cold water cools overheating:** water from **snowy biomes (base ≤0.15), winter, or with `ice_pack` in inventory** comes as `cold_water_bowl` / `cold_purified_water_bowl` (aqua name, icy texture). Drinking gives **-1.5 temp immediate + 60s "cooled" window (-1.5 target, dampens heat)** vs normal water's **-0.7 + 30s**. Hot water gives **no cooling**. This is the "cooldown for overheating when you drink water unless it's hot" — cold is best in desert, hot makes it worse.
- **First week loop:** secure a water source near spawn, keep 4-6 bowls, purify in batches on the campfire while you build flint tools and thatch. Store water in a `water_cauldron` (3 levels, 6 per bowl) — but a **heated cauldron** gives hot water (needs cooling), and any open water left 2 days (7 days sealed in bottle/canteen) goes stale (future `spoiledz` hook). Warm armor (`environmentz:warm_armor`) and bowls stack for later but water stays the daily chore until you have a base.

### 2 - Body, Diet, and Decay (Survival v1)

- **Temperature** `environmentz:temperature` -10..10, drifts 0.05/s (0.1 per 2s tick) toward `targetFor(biome)` = `(baseTemp-0.6)*6.5` clamped -9..9 (plains 0.8→1.3, desert 2.0→9, frozen -0.7→-8.5). `warm` armor pulls target up (+1.2/piece +0.3/piece²), `non_affecting` dampens 8%/piece, `ice_pack`/`insolating` bias ±1.5, rain -1, in water -2, on fire 9.5, Y>128 +1, Y<0 -1.5 (`HearthwindSurvivalTemperature:104`). At **-8** freeze 1 dmg/4s (`freeze`), **+7** heat exhaustion `+0.02` hunger per 2s, **+9** burn 1 dmg/4s (`hotFloor`) — both 4s cooldown. Warnings: 6 "very cold/hot", 9 "Extreme temperature! Find shelter!" (`temperature: driftPerSecond, freezeHurtAt, heatExhaustAt, heatHurtAt`). Extreme heat (desert noon, target 9) hits in ~90s from 0, so midday desert forces shade/water/ice.
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
