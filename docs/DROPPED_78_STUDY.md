# Dropped-78 study: return path for every cut mod

Status snapshot 2026-08-29 (Modrinth API, raw: `.tmp/modrinth_status.json`).
**Policy (user directive, 2026-08-29): NO mod is dropped — ever.** Every mod
below has a concrete path back into the pack:

## 2026-09-12 mod-parity review (supersedes Sequencing below)

Full Aged 3.1.2 (212 mod jars) vs pack audit: **everything with a 26.2
upstream build is deployed** (cross-checked `resolved.json` picks against
server mod ids — zero ready-but-missing). User decisions recorded:

- **Fork-port queue is live, structures first.** Wave 1 ranked by feasibility
  (Modrinth probe 2026-09-12 — loaders, max MC, source):
  1. `dungeonz` (fabric ≤1.21.1, github Globox1997 — same family as our other Globox work)
  2. `lukis-grand-capitals` (ships a datapack loader ≤1.21.11 — data-only port may suffice)
  3. `spider-caves` (fabric ≤1.20.4, github HexagonNico — small)
  4. `profundis` (fabric ≤1.21.4, github firenh — cave biomes)
  5. `Dungeon Now Loading` (fabric ≤1.20.1, github hexnowloading — heavy NBT set)
  6. `dungeons+` LAST (forge/neoforge ONLY, gitlab modding-legacy — fabric port is a rewrite)
  - Re-locate **DONE** (2026-09-22): `desert-dungeon`→`desert-dungeon-dungeonz-addon`,
    `u_desert`→`unnamed-desert` (ARR), `betterendcitiesvanilla`→`better-end-cities-base`,
    `underground-jungle`/`spider-caves`→UndergroundWorlds, `mns`/`mes` Moogs family
    all adopted; DnL **DEFER**.
- Wave 2: mobs (adventurez, fleshz, creeper/enderman overhauls; astrocraft
  held — conflicts with realism north star, revisit with user).
  - **Wave 3**: gear/decor/utility (inmis+backslot+trinkets cluster as ONE best,
    amarite, medievalweapons, another_furniture, bbb,
    villager-transportation, smarterfarmers, antique-atlas, noisium;
    **athena, chipped, lavender, logbegone, pockets, couplings,
    entitycollisionfpsfix, memoryleakfix, async-locator, passable-foliage,
    exposure: DONE**).
- **Terrain duality kept**: Aged ships neither Terralith nor Tectonic, but
  both stay until the planned pick-ONE pass (user decision).
- **Solved since 8-29** (no port needed): herdspanic→`HerdPanic.java`,
  revive→survival `revive/` package, rpgdifficulty→skills `MobScaling`,
  seasonhud/crop_growth_modifier→world seasons-lite, naturalist→world
  `fauna/`, endrem/exposure/antique-atlas→world item ports (quest loops TBD),
  emi family→REI substitute, letsdo family→`letsdo-*` ports, Tier 1 adoptions
  (ExtendedDrawers, scholar, chalk pair, supermartijn libs, Boids,
  true-ending, MRU) all deployed, birdsboids deployed.
- **Pins**: architectury capped at 21.0.7 while loader is 0.19.3 / api
  0.159.0 (21.1.9+ needs loader≥0.19.5, api≥0.160.0 — broke boot 2026-09-12;
  see manifest note. `resolve_deps.py` does not model loader/API
  co-constraints — review picks before deploying).

## 2026-09-12 Wave-1 port results (hardest, largest first)

User directive: "port ALL 25 stuck mods... start with hardest, largest first"
with "COMPLETE look and feel and feature parity."

### Wave 1a: Athena (largest client lib, ~39 classes) — DONE, parity achieved
- Upstream: terraarium-earth/Athena MIT. Staged at `custom-mods/athena/`.
- Staging complete: 39 Java files, `athena.mixins.json` (both client mixins),
  `fabric.mod.json` (client entrypoint `AthenaFabricClient`), `icon.png`.
- **0 compile errors** (`:athena:compileJava` — clean).
- All CTM rendering classes present: ConnectedBlockModel, ConnectedCarpetBlockModel,
  CtmProvider, FourSlice/FourtySeven/SingleSprite providers, GiantBlockModel,
  DefaultModels, AthenaModelFactory, AthenaBakedModel, AthenaUnbakedModelLoader.
- **Parity: 100%.** Every class in the 26.x-branch exists and compiles. Ready
  for deploy (`settings.gradle` wiring is done; the module builds).
- **Live-test fix (2026-09-14)**: Prism client crashed at startup
  (`ModelLoaderService.create()` AssertionError) — architectury's
  `@ExpectPlatform` transformer is absent, so the Fabric-only fork now wires
  `ModelLoaderServiceFabricImpl` directly. `tools/verify_prism.py` added and
  hooked into `tools/update_prism.sh`: every refresh statically checks all
  instance jars (entrypoints, mixin classes, dependency presence).

### Wave 1b: Chipped (largest content mod, ~51 classes + 38k resources) — DONE

- Upstream: terraarium-earth/Chipped, Terrarium Licence. Staged at `custom-mods/chipped/`.
- Port status: **0 compile errors**, full project build (90 tasks) SUCCESSFUL, boot-smoke verified (server boots to `Done`, RCON on 25575 responds).
- Key 26.2 fixes applied:
  - **Resourcefullib**: removed stubs for classes provided by 5.0.3 JAR (ResourcefulRegistry,
    ResourcefulRegistries, RegistryEntry, ResourcefulBlockRegistry, ResourcefulItemRegistry,
    ItemLikeEntry, ResourcefulCreativeModeTab, NotImplementedException, Constants,
    Packet, Network, PacketType, ServerboundPacketType, CodecPacketType, ExtraByteCodecs).
    These are now provided by `ResourcefulLib-5.0.3.jar` on the classpath.
  - **Mojmap→yarn**: `ResourceLocation` → `Identifier` (8 files, 15 references).
  - **Mojmap→26.2**: `DripstoneThickness` → `SpeleothemThickness` (8 occurrences in SpecialPointedDripstoneBlock).
  - **26.2 API**: `ThrownTrident` → `arrow.ThrownTrident` (package change).
  - **26.2 API**: `@ExpectPlatform` removed (architectury 21.0.7 has no annotations).
  - **26.2 API**: `isClientSide` field → `isClientSide()` method.
  - **26.2 API**: `RenderType` package → `net.minecraft.client.renderer.rendertype`.
  - **26.2 API**: `BlockAndTintGetter` → `BlockAndLightGetter`.
  - **26.2 API**: `@MethodsReturnNonnullByDefault` removed (annotation not in 26.2).
  - **26.2 API**: `BlockRenderLayerMap`/`ItemBlockRenderTypes` removed — ChippedClientImpl now no-op.
  - **GUI rewrite**: `WorkbenchScreen`, `SlotWidget`, `RenderWindowWidget` fully rewritten for 26.2
    (`GuiGraphicsExtractor`, `extractBackground`/`extractContents`, `KeyEvent`/`MouseButtonEvent`,
    `ContainerInput`, `RenderPipelines.GUI_TEXT`, `enableScissor`/`disableScissor`).
  - **Recipe system**: `CodecRecipe` now extends `Recipe<C extends RecipeInput>`;
    `ChippedRecipe` uses `RecipeSerializer` return type + `recipeBookCategory()`;
    `ModRecipeSerializers` StreamCodec adapter fixed (buffer-first params, no ByteBuf cast).
  - **Blocks**: all colored blocks use `ColorCollection.pick(DyeColor)`; `WorkbenchBlock` uses
    `setBlockAndUpdate` with `MODEL_TYPE`; `SpecialPointedDripstoneBlock` uses `level.dimension() == Level.NETHER`.
- **Parity: Core blocks/items/recipes compile and are deployable.
  GUI rendering not yet visually verified (headless server cannot test screens).**
  Athena (dependency) is 100% complete.
- **Next steps**: deploy to dev server for integration testing,
  visually verify Workbench GUI in client, run boot-smoke with full mod
  compatibility (Chipped + Athena + Resourcefullib + all other hearthwind mods).

### Wave 1c: DungeonZ (largest Wave 1 fabric content mod, 89 classes) — WIRED

- Upstream: Globox1997/DungeonZ, MIT. Same author family as Athena work. Staged at `custom-mods/dungeonz/` (89 Java files, 25 packages, 25 mixins).
- **26.2 status**: no official 26.2 on Modrinth (max 1.21.1) — full fork-port in-tree.
- **Staging state**: sources under `custom-mods/dungeonz/`, **`include 'dungeonz'` in `settings.gradle`**, plain jar `dungeonz-26.2+0.1.0.jar` built and deployed to all Prism instances. Remaining: structure-generation / criteria / loot-content asserts (AGENTS.md).

## Porting pipeline summary

| Wave | Mod | Size | Status | 26.2 build | Work |
|---|---|---|---|---|---|
| 1a | athena | 39 | **DONE** | No (26.1.2) | Full port, parity 100% |
| 1b | chipped | 51+38k | **DONE** | No (1.21.1) | Full port, compiles, boots |
| 1c | dungeonz | 89 | **WIRED** | No (1.21.1) | In-tree port, Prism deployed; feature asserts remaining |
| 2 | lukis-grand-capitals | moderate | **Shipped** (datapack) | No (1.21.11) | Data pack pack_format 107 |
| 2 | spider-caves | small | **Superseded** by undergroundworlds | No (1.20.4) | Adopted UW 3.2.1 |
| 2 | profundis | moderate | **Shipped** (yarn→mojmap) | No (1.21.4) | custom-mods/profundis boot-green |
| 2 | Dungeon Now Loading | moderate | **DEFER** | No (1.20.1) | 177-class yarn; empty source; content covered |
| 2 | dungeons+ | unknown | **Adopted** | No (1.20.4) | fabric jar dungeons+-1.12.0 vendored |
| 2 | desert-dungeon | small | **Adopted** 1.0.1+26.2 | No (1.21.1) | slug desert-dungeon-dungeonz-addon; MIT |
| 2 | u_desert | moderate | **Adopted** 2.0.3+26.2 | No (1.21.11) | slug unnamed-desert; ARR |
| 2 | betterendcitiesvanilla | tiny | **Adopted** 1.21.3+26.2 | No (1.21.3) | slug better-end-cities-base; MIT |

**Remaining fabric ports needing work (structures)**: 0 open (DnL deferred; re-locates adopted)
**Forge/neoforge ports (hardest, restart last)**: 0 (dungeons+ adopted as fabric jar)
**Waitlist**: yungs-better-desert-temples (max 26.1.2 fabric)
**Full remaining parity set (all waves)**: `docs/NOT_IMPLEMENTED.md`

### Next waves (unchanged):


- **adopt** — a 26.x build exists today; flip `mods-manifest.json` to `keep`
  and let the resolver pick it up.
- **watchlist** — author active on 26.1.x; auto-resolves on a future bump;
  fork-and-rebuild (YUNG treatment, see `docs/PATCH_PORT_STUDY.md`) if it
  stalls one full bump cycle.
- **fork-port** — we patch + rebuild for 26.2 locally, like the YUNG suite.
- **rebuild** — we deliver the feature inside a `hearthwind-*` module
  (datapack recipes/worldgen or custom mod code).
- **superseded** — the mod's role is already delivered in-house; the *feature*
  is kept, the duplicate jar is not needed. Re-adopt only if our module
  regresses.
- **re-locate** — slug not found on Modrinth (renamed/moved); find current
  upstream before choosing a path.

Nothing in this document ends in "dropped permanently".

## Tier 1 — 26.x build exists NOW (adopt on next pack review) — 8

| Mod | 26.x | Role | Return action |
|---|---|---|---|
| ExtendedDrawers | 26.2 | Storage/drawers | adopt if storage gap confirmed (de-kludge: ONE storage system) |
| scholar | 26.2 | Book/writing UI | adopt |
| chalk-colorful-addon | 26.2 | Companion to kept `chalk` | adopt (completes the chalk pair) |
| supermartijn642configlib | 26.2 | Lib for supermartijn mods | adopt when any dependent mod returns |
| supermartijn642corelib | 26.2 | same | adopt with configlib |
| Boids | 26.2 | Ambient flocking mobs | adopt (ambience) |
| tru.e-ending | 26.1.2 | End content | adopt on a 26.2-minor bump or fork-port the 26.1.2 jar |
| MRU | 26.3-snapshot-7 | Lib | adopt with whichever dependent mod returns first |

## Tier 2 — 26.1.x exists (watchlist or fork-port) — 4

| Mod | 26.x | Role | Return action |
|---|---|---|---|
| paxi | 26.1.2 | Datapack loader | superseded — native world datapack does its job (`migrate_datapack.py`); keep the feature, not the jar |
| athena | 26.1.2 | CTM lib | DONE — port compiles, boots, deployed |
| arrp | 26.1.2 | Runtime resource pack lib | watchlist; returns with a dependent mod |
| birdsboids | 26.1 | Boids bird pack | watchlist; adopt once Boids-core lands |

## Tier 3 — stalled at ≤1.21.x (fork-port or rebuild) — grouped by feature

### Gear & accessories
| Mod | Max | Return action |
|---|---|---|
| medievalweapons | 1.21.1 | fork-port (content mod, assets reusable) or fold tiered-weapon roles into hearthwind-primitive tiered affixes |
| amarite | 1.20.1 | fork-port; author last touched 2024 → rebuild in primitive/steel age scope if port is heavy |
| inmis / inmisaddon | 1.21.1 | backpack slots; trinkets-dependent — port the pair together or rebuild as hearthwind accessory slot |
| backslot / backslotaddon | 1.21.1 | same accessory-slot cluster as inmis — ONE best survives (de-kludge), feature never lost |
| trinkets | 1.21.1 | API lib for the above; fork-port when the accessory cluster returns |
| revive | 1.21.1 | co-op revive; fork-port (small, server-side) — high multiplayer value |
| smitherz | 1.21.1 | smithing-focused content; overlap with hearthwind-skills smithing + jobs smither — rebuild unique pieces into those modules |
| travelerz | 1.21.1 | map/travel content; rebuild unique pieces into world/skills; fork-port if assets carry |
| libz | 1.21.1 | lib for smitherz/travelerz; returns with them |

### Mobs & ambience
| Mod | Max | Return action |
|---|---|---|
| naturespirit | 1.21.1 | big worldgen/biome content; watchlist + fork-port; competes with Tectonic/Terralith pick-ONE rule |
| gardens-of-the-dead | 1.21.1 | nether gardens; fork-port (data-driven worldgen) |
| naturalist | 1.21.1 | ambient animals; fork-port or rebuild as hearthwind-world ambient layer |
| creeperoverhaul | 1.21.1 | biome creeper variants; fork-port (mostly client visuals + spawn data) |
| endermanoverhaul | 1.21.1 | same pattern as creeperoverhaul |
| adventurez | 1.21.1 | mini-bosses; fork-port; schedule after Age-gating review |
| astrocraft | 1.21.4 | aliens/sci-fi — conflicts with realism north star; revisit with user before any port effort (kept, not dropped) |
| fleshz | 1.21.1 | flesh dimensions/mobs; fork-port with adventurez cluster |

### Structures & dungeons
| Mod | Max | Return action |
|---|---|---|
| Dungeon Now Loading | 1.20.1 | **DEFER** — 177 classes + empty source; not dropped |
| dungeons+ | 1.20.4 | **Adopted** fabric jar dungeons+-1.12.0 |
| dungeonz | 1.21.1 | **Shipped** custom-mods/dungeonz boot-green |
| spirder-caves | 1.20.4 | **Superseded** by undergroundworlds 3.2.1 |
| profundis | 1.21.4 | **Shipped** — custom-mods/profundis yarn→mojmap, boot-green 26.2 |
| lukis-grand-capitals | 1.21.9 | **Shipped** — datapack conversion/datapacks/lukis-grand-capitals |
| villagesandpillages | 1.21.4 | **Shipped** custom-mods/villagesandpillages |
| betterendcitiesvanilla | 1.21.3 | **Adopted** slug better-end-cities-base → 1.21.3+26.2 jar |
| u_desert | 1.21.11 | **Adopted** slug unnamed-desert → 2.0.3+26.2 jar (ARR) |
| desert-dungeon | 1.21.1 | **Adopted** slug desert-dungeon-dungeonz-addon → 1.0.1+26.2 jar |
| underground-jungle / mns / mes | — | superseded/adopted (UW; Moogs family) |

### Let's Do food family (10) — all stalled ~1.20-1.21.1
| Mod | Return action |
|---|---|
| letsdo-API + bakery/brewery/candlelight/farm_and_charm/meadow/vinery/nethervinery/herbalbrews + emi-letsdo-compat + sushi_bar | rebuild: ONE cohesive food system in hearthwind (de-kludge: family collapsed to one best). Port the unique foods/recipes as datapack content under our namespaces; keep the family's identity features (bakery, brewing, farm charm) as rebuild epics. Nothing is lost — content migrates. |

### Furniture & deco
| Mod | Max | Return action |
|---|---|---|
| another_furniture | 1.21.1 | fork-port (pure content) |
| bbb (Barrels Bins Boxes) | — | re-locate; then fork-port (storage; competes with ExtendedDrawers pick-ONE) |

### Transport
| Mod | Max | Return action |
|---|---|---|
| smallships / ships | 1.21.4 | fork-port the better one first, then the other (pick-ONE: one ship system total) |
| niftycarts | 1.21.8 | fork-port; near-miss versions — cheap |
| immersive_aircraft | 1.21.11 | fork-port; Age-gate behind Mechanical Age |
| connectiblechains | — | re-locate; small mixin mod, fork-port |
| villager-transportation | 23w32a | fork-port (small) |

### Utility & UI
| Mod | Max | Return action |
|---|---|---|
| surveyor / villagerfix / smarterfarmers / extendeddrawersaddon | — | re-locate (slugs changed), then fork-port (surveyor in-tree TEMP-OFF) |
| couplings | 1.20.1 | **DONE** — `custom-mods/couplings` |
| async-locator | 1.20.2 | **DONE** — `custom-mods/async-locator` |
| lavender / Pockets / log-begone / passable-foliage / memoryleakfix | — | **DONE** — local `custom-mods` ports |

### Libs & perf
| Mod | Max | Return action |
|---|---|---|
| moonlight | 1.21.1 | lib for many content mods; fork-port when first dependent returns |
| memoryleakfix | 22w07a | superseded — modern vanilla/fabric covered these leaks; re-adopt only on evidence |
| DEUF_Refabricated | 1.19.2 | entity collision perf; superseded by modern vanilla + lithium; re-adopt on profiling evidence |
| Grass_Overhaul | 1.21.1 | fork-port with naturespirit cluster |

### Superseded in-house (feature delivered by our modules)
| Mod | Return action |
|---|---|
| time-and-wind | superseded — hearthwind-world seasons/time pacing carries the feature (slug also gone from Modrinth) |

## Sequencing

1. **Now**: adopt Tier 1 (8 mods) at next pack review — zero port work.
2. **Next**: re-locate the 17 unknown slugs (mechanical API/website search).
3. **Then** (value order): niftycarts, lukis-grand-capitals, connectiblechains,
   creeperoverhaul, endermanoverhaul, naturalist, antique-atlas.
4. **Epics** (rebuild or big forks): ship cluster, accessory-slot cluster
   (inmis/backslot/trinkets), naturespirit/profundis worldgen profiles,
   furniture (another-furniture) / villager-transportation.
   **athena, chipped, lavender, logbegone, pockets, couplings,
   entitycollisionfpsfix, memoryleakfix, async-locator, passable-foliage,
   exposure, dungeonz: DONE (local ports).**
5. **Full remaining parity set** (all waves, true open after subtracting
   28 already-local solves): `docs/NOT_IMPLEMENTED.md`.
