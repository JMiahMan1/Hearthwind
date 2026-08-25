# Project direction: from Aged fork → standalone pack

Agreed direction (2026-08-24). This document governs identity decisions
so daily work keeps serving the end state instead of deepening fork
lock-in.

## North star — Realism, earned unlocks, rising frontier, no kludge

Hearthwind = **as close to reality as we can make it, without being miserable.**
Hearthwind becomes its own pack — own name, own look, own systems — while
shipping parity with Aged (`docs/FEATURE_PARITY.md`) as the floor, not the
ceiling. Every addition is judged by four principles:

1. **Realism first.** Thirst/temperature/diet/spoilage, seasons, crop
   realism, physical crafting (knapping, rock→cobble, sieve, tanning,
   compost) and believable progression replace instant-vanilla loops.
   If vanilla does it magically, we add a cost/skill/tool.
2. **Unlock as you gather, harder as you grow.** Resources, skills, and
   jobs gate the next tier; the world pushes back. Distance mob scaling
   (`hearthwind-skills: MobScaling`) is the first instance — future
   scaling couples to **aggregate progression** (total skill levels + best
   job level + farthest biome explored), not just coordinates. The opening
   hours near spawn are forgiving; the deep wilds and later ages are
   genuinely dangerous. Reward = access, not free power.
3. **No kludge — one best implementation.** All mods must play well
   together. Remove overlap, consolidate duplicates, keep the single best
   implementation and delete the rest. If two mods do “fancy farming,” pick
   one; if two do “sieving,” keep `earlystage:sieve_drops/aged_drops.json`
   as canon and delete the imitator (`Prospecor's Bench` vs `Sieve` — we
   keep `Sieve`). Every feature gets one home in `hearthwind-*` and one
   config.
4. **Technology slowly, hard-fought.** Tech is not a creative menu — it
   unlocks in ordered **Ages** (see § Ages below) gated by skills, jobs,
   and world exploration, each with its own resources, recipes, and
   infrastructure cost. No lightweight automation before `steel` is smelted
   and `smithing 14` is earned.

We still cherry-pick the best ideas/resources from any project (Genesis
instruction toasts/age chains, Tectonic terrain, Homestead's workbenches
as design reference) and rebuild them in-house under these principles.

## Phases

### Phase A — Fork with parity (now)

- Rebuild all original systems in our own code (`hearthwind-*` mods) — done/
  in progress per parity matrix (survival + skills v1 shipped, jobs scaffold live).
- Use upstream graphics & resources where needed. Rules:
  - Track provenance: every borrowed texture/sound/model gets an entry
    in `ATTRIBUTION.md` at repo root (source project, author, license).
  - Respect licenses: original Aged resources are "courtesy of
    xR4YM0ND and contributors" (see CONVERSION.md license note); third-
    party mods keep their licenses (e.g., Genesis: no redistribution of
    itself, but ideas/add-on integration allowed; MPL-2.0 framework code
    can be consulted, not copied verbatim into MIT code without notice).
  - Borrow through thin wrappers, not hardwiring: assets live under
    `resources/upstream/<origin>/...` so a later swap is mechanical.

### Phase B — Own identity

- **Naming decision (2026-08-24): the pack is called HEARTHWIND.**
  Display names switched immediately (`Hearthwind: Survival/Skills/
  Primitive/World/Jobs`, pack name `Hearthwind Server`); internal mod ids
  migrated from `aged_*` to `hearthwind_*` on 2026-08-25 (`hearthwind_survival`,
  `hearthwind_skills`, `hearthwind_jobs`, `hearthwind_primitive`,
  `hearthwind_world`). Attachment namespaces `levelz:`, `nutritionz:`,
  `spoiledz:` deliberately still mirror originals for corpus compatibility.
- **Credits**: Hearthwind is *inspired by* the original Aged pack
  (xR4YM0ND & contributors) and by Genesis (marianyp) — both credited
  in every mod's metadata; Aged resources remain credited per
  ATTRIBUTION.md if/when adopted.

- **Naming/branding pass**: display names, `modmenu` metadata, pack
  description and world flavor text are now Hearthwind. Mod ids are stable
  as `hearthwind_*` and can be aliased if a future rename is needed.
- **Asset replacement**: replace upstream textures starting with the
  most visible surfaces (custom items, HUD art, GUIs). ATTRIBUTION.md
  shrinks toward zero or becomes permanent credits where licenses allow.
- **Namespace audit**: our data attachment/tag namespaces (`nutritionz:`, `levelz:`,
  `spoiledz:`) intentionally mirror originals for corpus compatibility;
  when parity is complete, migrate them to first-party namespaces
  (`hearthwind:*`) with a one-time world upgrade script. The pack data
  slug `aged-server` is kept for world-upgrade compatibility.

### Phase C — Divergence / best-of

Maintain a standing **borrow board** of features worth adopting — filtered
by the four principles above (realism, earned unlock, one best, slow tech):

| Idea | Source | Status | Verdict vs principles |
|---|---|---|---|
| Client animations & instruction toasts | Genesis | studied — rebuild in-house (ideas/genesis-comparison.md) | ✅ Keep — teaches gates without wiki, no overlap |
| Age/story progression gating | Genesis | rebuild via advancement-wrapping | ✅ Keep — canonical way to enforce slow-tech Ages |
| Terrain-quality rivers/coasts | Tectonic/Terralith | watchlist, next version bump | ✅ Keep — realism payoff, one worldgen choice (pick one, not both) |
| Wave surfaces | shaders/companion mod | ideas/rivers-and-waves.md Phase 3 | 🟡 Defer — visual only, companion mod |
| Prospector's Bench gravel→nuggets | Homesteads (VoxelForge) | noted 2026-08-25 | ➖ Drop — duplicate of `earlystage:sieve_drops` — keep **Sieve** as single best (one mechanic, one loot file) |
| Tanning Rack flesh→leather | Homesteads | noted | 🟡 Adopt as plain **tanning** recipe inside `hearthwind-primitive` (no new block, just 4×rotten→1 leather via datapack), avoids block overlap |
| Honey Bottling Station | Homesteads | noted | ➖ Drop — `dehydration` water bowls + `farm_and_charm` already cover fluids; no new block |
| Create (mechanical automation) | Homestead-modpack / Create Aeronautics | watchlist | 🟡 Defer to **Iron/Steel Age** (post-`smithing 18` + `builder 5`), gated, not starter — hard-fought tech, not cozy instant |

Divergence rule of thumb: adopt *mechanics* freely (reimplemented in our
code), adopt *assets* only with license-compatible attribution, never
redistribute another mod's jar. **Overlap rule:** if two mods solve the
same survival need, delete one and keep the best; document the choice in
`conversion/curated/mods-manifest.json` `reason`.

## Ages — how slow tech is enforced

Technology arrives in **ordered Ages**, each with entry requirements
(skills + jobs + world milestones) and its own recipes/loot. You cannot
craft the next Age without first living the previous one. No creative
skip.

| Age | Entry gate (example) | Unlocks | Frontier response |
|---|---|---|---|
| 0 — **Stranded** | spawn, `rock`+`flint` (no skill) | Flint tools, campfire, tanning (4 flesh→leather), thatch | Safe radius 500 blocks, mobs baseline |
| 1 — **Camp** | `farming 3` + `mining 1` (mud bricks) | Sieve (`earlystage:sieve`), compost farmland, crude storage, first farming | Crop season multiplier starts to matter |
| 2 — **Copper** | `mining 4` (copper ore), `smithing 3` (furnace) | Copper tools, watering can, `build:block` basics | Mobs +1 step past 1000 blocks |
| 3 — **Bronze/Iron** | `mining 7` (iron), `smithing 14` (iron gear) | Iron, steel is gated behind iron+coal smelt (`earlystage:steel_*`), stone cutter/mason | +2 steps, wilderness begins to hurt |
| 4 — **Steel & Craft** | `smithing 18` (blast furnace), `builder 3` (obsidian/strong blocks) | Steel tools/block, robust storage, `chipped` workbenches, `create` wind/water wheel **preview** (no full automation) | +3 steps, deep dark/outer biomes elite |
| 5 — **Mechanical** | `smithing 20`, `builder 5` (chipped tables), `farming 15` (season-proof greenhouse) | Full `Create` automation, `Tom's Simple Storage`, powered pump for pipes upward | Frontier scales with **player power** (total skill levels/40) + distance, capped 20 |

Advancement chain enforces Ages: completing “Smelt your first steel ingot”
unlocks the Mechanical recipe tag; breaking a steel-tier block with `mining 5`
alone does nothing. Gating reuses `SkillGates` + future `JobGates` (same
infra: data `aged_skills/gates` / `aged_jobs/gates`). Tech never appears
in JEI before its Age advancement fires — instruction toasts teach, not wikis.

**Current code maps:** survival+primitive cover Ages 0–3, jobs+skills cover
gating infra, world `Season` covers seasonal friction. Next build is
**Sieve + Age 1 advancement chain**, then **Steel Age 3→4 wiring**, then
**Create-gated Mechanical** — each with tests.

## Distribution model

Two artifacts from the same repo:

- **Hearthwind Server** (required) — current `hearthwind-*` modules
  (`survival`, `skills`, `jobs`, `primitive`, `world`); installs on
  dedicated servers; vanilla clients can join.
- **Hearthwind Client** (optional companion, Phase C) — `hearthwind-net`
  shared payload definitions + client rendering module: HUD bars
  (thirst/diet/temperature/skills/jobs), visible water motion, Genesis-style
  instruction toasts. Server broadcasts regardless; clients without it
  simply see nothing extra.

Packaging (`build_pack.py`) will emit both flavors: the Modrinth index
format supports per-side environment flags, so one resolved dependency
set produces `hearthwind-server.mrpack` and
`hearthwind-client.mrpack`.

## Practical consequences for day-to-day work

1. New gameplay code always lands in OUR modules — never patch upstream
   jars.
2. Every asset copied from anywhere gets an ATTRIBUTION.md entry at copy
   time, not later.
3. Flavor text (messages, item names) should be written as ours from the
   start (English for now) rather than copying original wording.
4. When choosing between "exact parity" and "clearly better," prefer
   better if it stays config-tunable back to parity behavior.
5. **One-best rule:** before adding any mod or block, check `mods-manifest.json`
   and the borrow board — if an existing feature already covers the need,
   delete the duplicate instead of adding it. Count blocks/items per
   survival need (storage, farming, sieving, metal) — the number should go
   down, not up.
6. **Slow-tech rule:** no new automation, transport, or storage tech ships
   without an Age gate (advancement + skill + job). If a PR adds a Create
   contraption without a gate, it is not ready.
7. **Harder-world rule:** any new unlock that makes the player stronger
   must also raise the frontier (add a scaling step, a new hostile, or a
   harsher season penalty) in the same PR, keeping risk/reward proportional.

## Immediate backlog from this decision

- [x] Naming: **Hearthwind** (display names live; ids migrate later)
- [ ] Create `resources/ATTRIBUTION.md` skeleton + record what upstream
      assets we currently ship (audit `resources/`, `configs/`).
      *(ATTRIBUTION.md exists at repo root; audit when assets arrive.)*
- [x] Genesis-style client animations added to the borrow board and
      considered when the companion client module starts.
