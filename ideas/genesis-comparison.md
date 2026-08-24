# Genesis / Genesis Framework — comparison & idea mining

Researched 2026-08-24 (Modrinth API + GitHub wiki/source). Conclusion up
front: **do NOT add either mod to the pack** — neither ships our balance
and the framework pulls in its own runtime. Instead we rebuild the good
parts natively in `aged-skills` / `aged-primitive` / the migrated
datapack. Ideas worth stealing are listed at the end.

## What it is

| | genesis-official | genesis-framework |
|---|---|---|
| Purpose | Opinionated vanilla progression overhaul ("Age" system, friction before The End) | Data-driven toolkit to build YOUR OWN age systems |
| License / perms | Pack use OK, add-ons OK, no redistribution of itself | MPL-2.0, pack use OK |
| 26.2 builds | **7** | **4** |
| Loader | Fabric | Fabric |

(Studied for ideas only — not added to the pack; see conclusion.)

Ages are advancement wrappers that **lock items/blocks/dimensions** until
criteria complete (e.g. "Iron Armor" requires completing a trial spawner
while wearing copper). **Instructions** are persistent tutorial toasts
(vanilla tutorial-hint style) completed by criteria. Everything is defined
in datapack JSON at `data/<ns>/genesisframework/{age,instruction}/*.json`,
criteria reuse standard advancement triggers plus custom ones like
`genesis:item_broken`. Unwanted built-in ages are removed by overwriting
with an empty file. Also: shared/co-op ages, game rules, config.

## Similarities with what we're building

1. **Levelz parity overlap**: our migrated levelz corpus encodes exactly
   this shape — 12 skills x levels gating items/blocks/entities (400
   files). An Age is just a coarser, story-flavored gate. Same mental
   model: content locked behind earned progress.
2. **Datapack-first tuning**: framework reads plain JSON from the world
   datapack — identical philosophy to our "~800 migrated tuning files
   activate unchanged". We can GENERATE age/instruction JSONs from the
   migrated corpus with a script in `conversion/scripts/`.
3. **Guidance**: their instruction toasts are the polished version of our
   overlay messages (thirst/temperature warnings). Fits Aged's "no
   external guides needed" onboarding goal.
4. **Story friction**: End access gated behind Nether -> Ocean Monument ->
   Ancient City -> Wither maps perfectly onto Aged's medieval-realism
   pacing and our primitive-start module (flint -> stone -> ... steel).

## Differences / cautions

- genesis-official hardcodes ITS balance (locks furnaces, raw ores, tool
  casts, adds items). That would fight our own balance + the tiredness
  feature would double-penalize alongside our thirst/temperature. Skip
  the mod; mine its `generated/data/genesis/genesisframework/*` JSONs as
  syntax examples.
- Framework covers milestone gates only — no XP curves, no numeric
  attribute bonuses, no mob scaling. `aged-skills` (levelz parity:
  XP -> levels -> attribute modifiers, rpgdifficulty scaling) remains
  necessary and complementary.
- Advancement-trigger criteria means gating granularity is "did event X",
  not "is skill level >= N". Numeric checks stay in aged-skills.

## Ideas to steal (all rebuilt in-house, no dependency)

1. **Advancement-wrapped gating**: vanilla advancements already give free
   persistence, UI tree, multiplayer sync, and criteria triggers
   (`inventory_changed`, `item_broken`-style via `used_item`/
   custom predicates). Our age/skill-unlock layer should wrap
   advancements instead of inventing storage — same trick as Genesis,
   implemented in `aged-primitive` with plain vanilla advancements +
   recipe-locking via datapack (`recipe/crafting/` removal + unlock on
   grant).
2. **Persistent instruction toasts**: vanilla "tutorial hints" style
   onboarding (find water -> gather flint -> first campfire) shown until
   completed — replaces our ad-hoc overlay strings. Vanilla supports
   tutorial toast steps; worst case a tiny client-optional companion or
   bossbar/actionbar sequencing server-side.
3. **Ordered vs unordered progression**: explicit parent chains with a
   `requiresParent` flag (their frames communicate it visually). Adopt
   for primitive tiers: Flint -> Stone -> Copper -> Iron -> Steel must be
   ordered; story ages unordered.
4. **Empty-file overwrite convention**: disabling upstream datapack
   content by overwriting with an empty file is cleaner than deletion
   patches in our migration scripts — worth adopting in
   `migrate_datapack.py` for cut-mod tuning files.
5. **Story friction before The End**: gate End access behind a milestone
   chain (Nether -> Ocean Monument -> Ancient City -> Wither) as vanilla
   advancements that toggle the `minecraft:end/enter` gate via the
   vanilla advancement-based player gate or a simple mixin.
6. **JSON schema shape**: their `{items, dimensions, criteria, display,
   requiresParent}` definition format is a good template for OUR
   generated unlock files when we convert the levelz corpus into
   datapack-consumable definitions.

## Watchlist entries

- Neither mod is added to the manifest; revisit only if we ever want a
  ready-made age UI instead of building one.
