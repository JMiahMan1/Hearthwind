# Mods status and roadmap from Aged 3.1.2 to Hearthwind (26.2)

> **1.0.0 scope (2026-09-24):** `docs/RELEASE_1.0_PARITY.md` governs 1.0.0 and wins wherever this file disagrees. Its open set was computed from the manifest, which misses 83 Aged mods by slug. The authoritative 212-mod ledger is in the plan's section 5.

**Policy: NO mod is ever dropped.** Every mod from Aged 3.1.2 is preserved through native rebuilds in `custom-mods/`, standalone 26.2 module ports, or active upstream 26.2 releases.

Snapshot 2026-09-22: resolver `ready=57/138`, `missing=66` — but **28 of those 66 are already solved locally** (vendored jar or `custom-mods` port) and only look "missing" because Modrinth still has no 26.2 file. True open set for complete parity is listed below.

## Recently Shipped & Implemented (100% Verified on 26.2)

1. **Small Ships (`small-ships` / `ships`)**:
   - Standalone 26.2 Fabric port in `custom-mods/smallships`.
   - Fully functional Cogs, Brigs, Galleys, and Drakkars with shipboard cannons, mountable swivel guns, dyeable canvas sails, and container holds.
2. **Villages and Pillages (`villagesandpillages`)**:
   - Standalone 26.2 Fabric port in `custom-mods/villagesandpillages`.
   - Swamp Witch Villages with custom Jigsaw placement, structure processors, villager cages, and loot tables.
3. **Gardens of the Dead (`gardens-of-the-dead`)**:
   - Fully ported to Fabric 26.2 in `contrib/gardens-of-the-dead/`, vendored.
   - Soulblight Forest & Whistling Woods with native carvers, feature selectors, and TerraBlender surface rules.
4. **Nature's Spirit (`natures_spirit`)**:
   - Modern 26.2 release integrated and vendored. 60+ biomes, 18 wood sets, Kaolin clay, wild edibles integrated with 5-group nutrition.
5. **YUNG Suite (6 Mods)**:
   - `yungs-api`, `better-nether-fortresses`, `better-end-island`, `better-desert-temples`, `better-jungle-temples`, `better-ocean-monuments` on 26.2 (vendored local builds).
6. **Additional 26.x Native & Ported Mods**:
   - `the-lost-castle` (`tlc`), `medieval-buildings`, `birds-boids` & `boids`, `extended-drawers`, `scholar`, `chalk-colorful-addon`, `true-ending`, `supermartijn642configlib` & `supermartijn642corelib`, `kiwi`, `arrp`.
7. **custom-mods 26.2 ports (2026-09-22, all boot-smoked + in Prism)**:
   - `lavender` (+ `owo-lib`), `logbegone`, `pockets`, `couplings`, `entitycollisionfpsfix`, `memoryleakfix`, `async-locator`, `passable-foliage`.
   - Also: `athena`, `chipped`, `exposure`, `dungeonz`.
8. **In-House Replacements (`custom-mods/`)**:
   - `hearthwind-survival`: Replaces `dehydration`, `environmentz`, `nutritionz`, `spoiledz`, and `revive` (teardrop thirst HUD, dirty water effect, authentic glass thermometer, 5 nutrient groups, spoilage, downed/revive).
   - `hearthwind-skills`: Replaces `levelz`, `rpgdifficulty` (12 skills, 3-heart start, 676+ gates, triangular curves, mob scaling).
   - `hearthwind-jobs`: Replaces `jobs-addon` (8 jobs, `/job` commands, job ladders, Age gating).
   - `hearthwind-primitive`: Replaces `earlystage`, `tiered`, `reciperemover` (sieve, knapping, surface rock/flint, equipment affixes).
   - `hearthwind-world`: Replaces `fabric-seasons`, `seasonhud`, `crop_growth_modifier`, `endrem`, `herdspanic`, `villager-transportation` (seasons, crop multipliers, 16 ancient eyes under `endrem:`, `HerdPanic`).
   - `letsdo-*`: Let's Do agriculture suite (Farm & Charm, Vinery, Candlelight, Meadow, HerbalBrews, Brewery, Nether Vinery).

---

## Remaining for complete Aged parity (true open set)

Resolver `missing` minus already-solved local ports/vendored jars/in-house rebuilds. Grouped by return path (see `docs/DROPPED_78_STUDY.md`, `docs/PATCH_PORT_STUDY.md`).

### Fork-port queue (structures first — user wave order)

| Wave | Mod | Upstream max | Notes |
|---|---|---|---|
| ✅ | `lukis-grand-capitals` | 1.21.11 | **SHIPPED** — datapack under `conversion/datapacks/`, pack_format 107 |
| ✅ | `spider-caves` | 1.20.4 | **SUPERSEDED** by adopted `undergroundworlds` 3.2.1 |
| ✅ | `profundis` | 1.21.4 | **SHIPPED** — full yarn→mojmap port `custom-mods/profundis`, boot-green 218 mods |
| ✅ | `dungeons-plus` | 26.1.2 | **ADOPTED** fabric jar `dungeons+-1.12.0.jar` vendored |
| ✅ | `underground-jungle` | 1.20.1 | **SUPERSEDED** by adopted `undergroundworlds` 3.2.1 |
| ✅ | `mns` / `mes` | 26.2 fabric | **ADOPTED** MoogsNether + MoogsEnd + StructureLib vendored |
| ✅ | `desert-dungeon` | 1.21.1 | **ADOPTED** slug `desert-dungeon-dungeonz-addon` → `desert-dungeon-1.0.1+26.2.jar` (data addon for our dungeonz) |
| ✅ | `u_desert` | 1.21.11 | **ADOPTED** slug `unnamed-desert` → `unnamed-desert-2.0.3+26.2.jar` (**ARR** — redistribute carefully) |
| ✅ | `betterendcitiesvanilla` | 1.21.3 | **ADOPTED** slug `better-end-cities-base` → `betterendcities-vanilla-1.21.3+26.2.jar` (end city NBT overrides) |
| ⏸ | `dungeon-now-loading` | 1.20.1 | **DEFER** — 177-class yarn port + empty source; content already covered by UW/Dungeons+/Moogs/Profundis |

### Wave 2 — mobs & adventure

| Mod | Upstream max | Notes |
|---|---|---|
| `adventurez` | 1.21.1 | mini-bosses after Age-gating review |
| `fleshz` | 1.21.1 | flesh dimension cluster with adventurez |
| `creeper-overhaul` | 1.21.1 | biome creeper variants |
| `enderman-overhaul` | 1.21.1 | same pattern |
| `astrocraft` | 1.21.4 | **held** — conflicts with realism north star; revisit with user before port effort |

### Wave 3 — gear / decor / utility

| Cluster | Mods | Notes |
|---|---|---|
| Accessory slots | `trinkets`, `inmis`+`inmisaddon`, `backslot`+`backslotaddon` | port as **ONE best** (de-kludge) |
| Weapons / smith | `medievalweapons`, `amarite`, `smitherz`+`libz`, `travelerz` | smitherz/travelerz may rebuild into skills/jobs instead |
| Furniture | `another-furniture`, `grass-overhaul` | pure content / ambience |
| Transport | `niftycarts`, `villager-transportation` | cheap near-miss ports |
| Exploration | `antique-atlas-4` | interim: vanilla maps + cartographer |
| Utility | `surveyor` | **in-tree TEMP-OFF** (`settings.gradle`): 26.1→26.2 nudge WIP (~324 compile errors) |
| Perf / libs | `modernfix`, `noisium`, `moonlight` | watchlist / re-baseline on 26.2 vanilla noise |

### Client-optional (server parity N/A)

`emi`, `emi-loot`, `emi-ores`, `emi-enchanting`, `emiffect`, `emitrades` — ship in the client bundle when EMI publishes 26.2 (or a REI substitute). Never required on the server.

### Not counted as open (already delivered)

- **Local ports / vendored**: profundis, lukis-grand-capitals, undergroundworlds, dungeons+, MoogsNether/End/StructureLib, ForgeConfigAPIPort, desert-dungeon, unnamed-desert, betterendcities-vanilla, lavender, logbegone, pockets, couplings, entitycollisionfpsfix, memoryleakfix, async-locator, passable-foliage, athena, chipped, exposure, dungeonz, smallships, villagesandpillages, YUNG×6, gardens-of-the-dead, natures-spirit, tlc, medieval-buildings, true-ending, birds-boids, kiwi, arrp.
- **In-house rebuilds**: endrem eyes (`hearthwind-world` `endrem/`), herdspanic (`HerdPanic`), villager-transportation (listed as world scope; verify feature depth before calling parity complete), plus the full rebuild groups in `mods-manifest.json`.

---

## Active watchlist (not in the open-port count above)

- Upstream may publish 26.2 mid-bump: re-run `python3 conversion/scripts/resolve_deps.py` after each MC bump and review `conversion/build/readiness-report.json`.
- `astrocraft` stays held until a user decision on the realism rule.
- Ambience/sound stack (`AGED_PARITY.md` §2): AmbientEnvironment, DripSounds, PresenceFootsteps, Sound Physics, ImmersiveThunder, Euphonium — separate "feel" epic, not in the fork-port table until someone owns it.

## Pack Health

- **Gametests**: **296 / 296 (container, green)** as of commit `ba4677096`.
- **Dev Server Status**: **Done** on 26.2, RCON verified; structures boot-green (profundis + adopted structure suite).
- **Structures wave**: closed except DNL **DEFER** (177-class, empty source) + `yungs-better-desert-temples` waitlist (max 26.1.2). Re-locate candidates adopted: desert-dungeon / unnamed-desert (ARR) / better-end-cities-base.
- **Prism**: Full / Minimal / Dev-Client refreshed with all finished ports including profundis + structure adopts (verify_prism OK).
