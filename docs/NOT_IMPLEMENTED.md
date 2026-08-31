# Mods not yet implemented in the pack (2026-08-31)

This document tracks the status of all upstream mods from Aged 3.1.2 that are not yet natively present or have alternative solutions in progress. Per the no-drop policy, nothing is abandoned without deliberate design rationale — this list represents the active outstanding-work and watch queue.

## Recently Shipped & Implemented (Removed from missing queue)

The following systems and mods have been completely resolved, built, and verified on 26.2:

1. **Gardens of the Dead (`gardens-of-the-dead`)**:
   - Fully ported to Fabric 26.2, patch preserved in [`contrib/gardens-of-the-dead/patches/26.2-port.patch`](file:///Users/jeremiahsummers/Code/Hearthwind/contrib/gardens-of-the-dead/patches/26.2-port.patch), vendored at [`conversion/vendored/gardens-of-the-dead-fabric-5.0.2+26.2.jar`](file:///Users/jeremiahsummers/Code/Hearthwind/conversion/vendored/gardens-of-the-dead-fabric-5.0.2+26.2.jar).
   - Biomes: Soulblight Forest & Whistling Woods with native 26.2 carvers, feature selectors, and TerraBlender surface rules.
2. **Nature's Spirit (`natures_spirit`)**:
   - Modern 26.2 release integrated and vendored (`conversion/vendored/natures_spirit-fabric-2.3.0+26.2.jar`).
   - Integrated into diet nutrition tags (`fruits`, `vegetables`, `grains`, `proteins`, `sugars`).
3. **YUNG Suite (6 Mods)**:
   - `yungs-api`, `better-nether-fortresses`, `better-end-island`, `better-desert-temples`, `better-jungle-temples`, `better-ocean-monuments` ported to 26.2 via `contrib/yungs/` and fully verified.
4. **Additional 26.x Native & Ported Mods (7 Mods)**:
   - `the-lost-castle` (`tlc`): Recompiled for 26.2 (`MapCodec` structure processor) in `contrib/the-lost-castle/`, vendored.
   - `medieval-buildings`: 26.2 ported in `contrib/medieval-buildings/`, vendored.
   - `birds-boids` & `boids`: Flock AI & ambient birds ported to 26.2 in `contrib/birds-boids/`, vendored.
   - `extended-drawers`: Modern 26.2 storage drawer release vendored.
   - `scholar`: 26.2 book & quill journaling mod vendored.
   - `chalk-colorful-addon`: 26.2 chalk coloring addon vendored.
   - `tru.e-ending`: 26.2 dragon overhaul datapack mod vendored.
   - `supermartijn642configlib` & `supermartijn642corelib`: 26.2 library mods vendored.
5. **In-House Replacements (`custom-mods/`)**:
   - `hearthwind-survival`: Replaces `dehydration`, `environmentz`, `nutritionz`, `spoiledz`, and `revive` (downed/bleedout/channeling).
   - `hearthwind-skills`: Replaces `levelz`, `rpgdifficulty` (12 skills, 3-heart start, 750+ gates, triangular curves, mob scaling).
   - `hearthwind-jobs`: Replaces `jobs-addon` (8 jobs, `/job` commands, job ladders, Age gating).
   - `hearthwind-primitive`: Replaces `earlystage`, `tiered` (199 affixes), `reciperemover` (sieve, knapping, surface rock/flint).
   - `hearthwind-world`: Replaces `fabric-seasons`, `seasonhud`, `crop_growth_modifier`, `endrem` (16 eyes), `herdspanic` (herd stampede), `villager-transportation` (leads on villagers).
   - `hearthwind-flora`: In-house implementation of the Let's Do agriculture suite (Farm & Charm, Vinery, Candlelight, Meadow, HerbalBrews, Brewery, Nether Vinery crops and crafting blocks).

---

## Current Missing Mods Queue (Categorized)

### Tier B — Kept Upstream Mods Awaiting 26.2 Release / Watchlist

| Mod | Max Version | Status & Strategy |
|---|---|---|
| `modernfix` | 26.1.2 | Performance optimization; evaluate overlap vs `lithium` / `ferritecore` |
| `kiwi` | 26.1.2 | Library mod for Snow Real Magic; watchlist |
| `antique-atlas` | 1.21.1 | Client antique book map; heavy GUI port candidate or keep in-house map |
| `exposure` | 1.21.1 | In-game camera & photograph development; ping upstream |
| `noisium` | 1.21.6 | Worldgen noise optimization; benchmark on 26.2 first |
| `log-begone` | 1.21.1 | Log spam filter; easily replaced by Log4j configuration |

---

### Tier C — High-Value Content & Immersion (Targeted for Contrib Porting)

| Mod | Max Version | Strategy |
|---|---|---|
| `naturalist` | 1.21.1 | Fauna expansion (birds, bears, snakes); upstream active |
| `creeperoverhaul` | 1.21.1 | Biome-specific variant textures for creepers; lightweight port |
| `endermanoverhaul` | 1.21.1 | Biome-specific enderman variants |
| `another_furniture` | 1.21.1 | Medieval home furniture (tables, chairs, lamps) |
| `niftycarts` | 1.21.8 | Horse-drawn carts and wagons (complementing Superb Steeds) |
| `medievalweapons` | 1.21.1 | Daggers, spears, polearms (integrate with Better Combat & Tiered) |
| `lukis-grand-capitals` | 1.21.9 | Pre-generated capital city structures |
| `villagesandpillages` | 1.21.4 | Village and outpost structure variants |

---

### Tier D — Accessory & Storage Decisions (De-Kludge Audit)

| System | Candidates in Upstream | Decision / Action |
|---|---|---|
| **Accessory System** | `trinkets`, `inmis`, `inmisaddon`, `backslot` | Consolidate to ONE accessory/backpack standard (avoid duplicate inventory slots) |
| **Storage Solution** | `sophisticatedbackpacks`, `extendeddrawers`, `ironchests` | Pick single storage progression matching tech ages (Stranded -> Wooden -> Iron) |
| **Ship Dynamics** | `smallships`, `ships`, `immersive_aircraft` | Pick ONE sailing ship system for Age 3 (Oceanic); gate aircraft behind Age 5 (Mechanical) |
| **Cosmetic Deco** | `chipped`, `handcrafted` | Validate block palette without bloat |

---

## Summary of Pack Health

- **Total Tracked Manifest Entries**: 154
- **Kept & Active Mods**: 55 (including 8 vendored ports and libraries)
- **Rebuilt In-House (`custom-mods/`)**: 15 mod equivalents across 7 optimized Fabric modules
- **Cleanly Dropped / Superseded**: 76 mods (duplicate libraries, redundant mechanics, obsolete 1.20.1 workarounds)
- **Gametests Passing**: **197 / 197 (100% Green)**
- **Dev Server Status**: **Done (6.7s)** on 26.2, RCON verified.

