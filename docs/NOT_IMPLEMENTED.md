# Mods not yet implemented in the pack (2026-08-29)

86 mods have no 26.2 build in the pack index today, with the reason for each.
Every mod already has a return path: kept-but-missing mods in
`docs/PATCH_PORT_STUDY.md`, previously-cut mods in `docs/DROPPED_78_STUDY.md`.
Per the no-drop policy nothing here is abandoned — this list is the
outstanding-work queue.

Implemented already (NOT on this list): all `hearthwind-*` modules
(survival/skills/jobs/primitive/world/client replacing dehydration,
environmentz, nutritionz, spoiledz, levelz, jobsaddon, partyaddon,
rpgdifficulty, earlystage, reciperemover, autotag, fabric-seasons, seasonhud,
time-and-wind, crop_growth_modifier), paxi (superseded by the native world
datapack), and the 6 YUNG mods (patched + rebuilt locally, vendored in
`conversion/vendored/`). Resolved `keep` mods (41) also ship.

Reason codes:
- **no-build** — upstream has no 26.2 build; max release shown.
- **near-miss** — upstream builds 26.1.x; auto-resolves on a future bump.
- **stalled** — upstream quiet ~6+ months; we fork-port (YUNG treatment).
- **re-locate** — Modrinth slug gone/renamed; find upstream, then re-classify.
- **de-kludge** — overlaps another mod; pick-one rule defers it until the
  winner's fate is settled.
- **adopt** — build exists; just not flipped to `keep` yet (next pack review).

## Tier A — adopt at next pack review (7 + 4 near-miss)

| Mod | Max | Reason not in pack yet |
|---|---|---|
| ExtendedDrawers | 26.2 | no-build **exists** — de-kludge: confirm ONE storage system before adding |
| scholar | 26.2 | no-build **exists** — adopt pending review |
| chalk-colorful-addon | 26.2 | no-build **exists** — adopt with kept `chalk` |
| supermartijn642configlib | 26.2 | no-build **exists** — lib; adopt with a dependent mod |
| supermartijn642corelib | 26.2 | no-build **exists** — lib; adopt with a dependent mod |
| Boids | 26.2 | no-build **exists** — adopt pending review |
| MRU | 26.3-snapshot-7 | no-build **exists** (snapshot) — adopt with a dependent mod |
| tru.e-ending | 26.1.2 | near-miss; adopt or vendor-build like YUNGs |
| birdsboids | 26.1 | near-miss; follows Boids core |
| athena | 26.1.2 | near-miss; only needed by chipped |
| arrp | 26.1.2 | near-miss; only needed by a dependent mod |

## Tier B — kept but no 26.2 build (10)

| Mod | Max (updated) | Reason |
|---|---|---|
| Kiwi | 26.1.2 (2026-08-29) | near-miss; author very active; only needed if a kept mod depends on it — dependency re-audit |
| endrem | 26.1.2 (2026-07-25) | near-miss; auto-resolve watchlist |
| medieval_buildings | 26.1.2 (2026-04-20) | near-miss; watchlist |
| modernfix | 26.1.2 (2026-08-24) | near-miss; overlap audit vs lithium before re-adding |
| the-lost-castle | 26.1.2 (2026-05-10) | near-miss; watchlist |
| antique-atlas | 1.21.1 (2026-01-05) | stalled — heavy client UI port; we fork-port; interim explorer-map surface |
| exposure | 1.21.1 (2026-06-09) | stalled — camera/photo renderer port; ping upstream first |
| herdspanic | 1.21.1 (2024-09-10) | stalled 2y — small panic-AI mixin; we fork-port |
| log-begone | 1.21.1 (2024-08-08) | stalled 2y — replaceable by log4j filter; we fork-port |
| noisium | 1.21.6 (2025-06-24) | stalled — 26.2 vanilla already optimized noise; port remaining delta only |

## Tier C — previously cut, fork-port queue (21)

| Mod | Max (updated) | Reason |
|---|---|---|
| revive | 1.21.1 (2026-07-25) | no-build; co-op value → first fork-port |
| niftycarts | 1.21.8 (2026-03-09) | no-build; near-miss versions → cheap port |
| lukis-grand-capitals | 1.21.9 (2026-01-13) | no-build; closest to portability |
| villagesandpillages | 1.21.4 (2025-06-13) | no-build |
| couplings | 1.20.1 (2023-07-14) | stalled 3y; tiny mod |
| creeperoverhaul | 1.21.1 (2025-01-04) | no-build; visuals+spawn data |
| endermanoverhaul | 1.21.1 (2026-02-22) | no-build; same pattern |
| naturalist | 1.21.1 (2026-08-16) | no-build; author active → may resolve itself |
| gardens-of-the-dead | 1.21.1 (2025-12-08) | no-build; data-driven worldgen |
| villager-transportation | 23w32a (2024-12-13) | stalled; small |
| async-locator | 1.20.2 (2023-06-24) | stalled 3y; server-side locate perf |
| another_furniture | 1.21.1 (2026-04-16) | no-build; pure content |
| medievalweapons | 1.21.1 (2026-08-21) | no-build; overlaps tiered affix plan — port or fold in |
| inmis / inmisaddon | 1.21.1 | no-build; needs trinkets API; accessory cluster pick-one |
| backslot / backslotaddon | 1.21.1 | no-build; same accessory cluster pick-one |
| trinkets | 1.21.1 (2024-07-15) | stalled 2y; API lib for accessory cluster |
| libz | 1.21.1 (2026-03-01) | no-build; follows smitherz/travelerz |
| memoryleakfix | 22w07a (2024-01-06) | stalled; likely superseded by modern vanilla+lithium — re-adopt on evidence |
| DEUF_Refabricated | 1.19.2 (2022-06-13) | stalled 4y; superseded by vanilla+lithium — re-adopt on profiling evidence |

## Tier D — previously cut, big rebuild epics

| Cluster | Mods (max) | Reason |
|---|---|---|
| Let's Do food family | letsdo-API 1.20.2, bakery/brewery/candlelight 1.20.1, farm_and_charm/meadow/vinery 1.21.1, nethervinery 1.20.6, herbalbrews*, sushi_bar*, emi-letsdo-compat* | de-kludge + no-build: 11 overlapping food mods stalled on old MC; collapse to ONE in-house food system (content migrates, nothing lost) |
| Worldgen pick-ONE | naturespirit 1.21.1, profundis 1.21.4, Grass_Overhaul 1.21.1 | no-build + overlaps Tectonic/Terralith decision — port as alternative profile only after pick |
| Structure/dungeon set | Dungeon Now Loading 1.20.1, dungeons+ 1.20.4, dungeonz 1.21.1, spirder-caves 1.20.4, desert-dungeon*, u_desert*, underground-jungle*, betterendcitiesvanilla*, mns*, mes* | no-build (most stalled ≤1.20.4) + heavy NBT sets; * = re-locate first (slug gone) |
| Content misc | adventurez 1.21.1, fleshz 1.21.1, astrocraft 1.21.4, chipped 1.21.1 (+athena 26.1.2), bbb* | no-build; adventurez/fleshz need Age-gating review; astrocraft conflicts with realism north star (kept, discuss before port effort); chipped is a huge variant set + CTM lib; bbb re-locate |
| Transport | smallships 1.21.4, ships 1.21.4, immersive_aircraft 1.21.11 | no-build + pick-ONE ship system; aircraft Age-gated behind Mechanical |
| Utility/UI | lavender 1.21.4, Pockets 1.20.1, surveyor*, villagerfix*, smarterfarmers*, extendeddrawersaddon*, OctoLib* | no-build and/or re-locate (slug gone); all small ports |
| Zone mods | smitherz 1.21.1, travelerz 1.21.1 (+libz) | no-build; overlap with hearthwind-skills smithing + jobs smither — unique pieces rebuild into our modules |

Machine-readable: `.tmp/not_implemented.json` + `.tmp/modrinth_status.json`.
