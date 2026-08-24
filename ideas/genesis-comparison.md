# Genesis / Genesis Framework — comparison & adoption notes

Researched 2026-08-24 (Modrinth API + GitHub wiki/source). Conclusion up
front: **adopt `genesis-framework` as a keep-mod; do not adopt
`genesis-official` wholesale; mirror its data model in our datapack.**

## What it is

| | genesis-official | genesis-framework |
|---|---|---|
| Purpose | Opinionated vanilla progression overhaul ("Age" system, friction before The End) | Data-driven toolkit to build YOUR OWN age systems |
| License / perms | Pack use OK, add-ons OK, no redistribution of itself | MPL-2.0, pack use OK |
| 26.2 builds | **7** | **4** |
| Loader | Fabric | Fabric |

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

## Adoption plan

1. **Manifest**: add `genesis-framework` as `keep`, group `aged-world`/
   integration; rerun resolver to pull transitive deps on 26.2.
2. **aged-primitive / story ages**: define our age tree in the migrated
   datapack under `data/aged_server/genesisframework/age/`
   (Primitive/Flint -> Stone -> Copper -> Iron -> Steel -> Nether -> End),
   locking smithing-tier items already specced in
   `data/levelz/smithing/*.json`.
3. **Onboarding instructions**: replace ad-hoc overlay text with
   instruction JSONs (find water, gather flint, first campfire, ...).
4. **Disable official ages** we don't want by empty-file overwrites if we
   ever ship genesis-official-compatible packs; not needed when using
   only the framework.
5. **aged-skills stays**: XP->levels->attributes + mob scaling; expose a
   bridge later so ages can require "skill X >= level Y" via a custom
   criterion if the framework supports mod-provided triggers
   (`Mod Development` wiki page — TODO check).
6. **Verify interplay**: boot test with framework jar present; confirm no
   conflicts with our attachments/mixins; check their config/game rules
   for disabling any built-in survival features.

## Watchlist entries

- `genesis-framework`: keep-candidate NOW (4 builds on 26.2).
- `genesis-official`: reference-only; revisit only if we ever want its
  item additions (raw ore/cast system) wholesale.
