# Project direction: from Aged fork → standalone pack

Agreed direction (2026-08-24). This document governs identity decisions
so daily work keeps serving the end state instead of deepening fork
lock-in.

## North star

Aged Server becomes **its own pack** — own name, own look, own content
directions — while shipping feature parity with the original Aged pack
as the baseline (`docs/FEATURE_PARITY.md`). Parity is the floor, not the
ceiling: we cherry-pick the best ideas/resources from any project
(Genesis client animations, Tectonic terrain, …) and add our own systems
where they fit the medieval-realism identity.

## Phases

### Phase A — Fork with parity (now)

- Rebuild all original systems in our own code (`aged-*` mods) — done/
  in progress per parity matrix.
- Use upstream graphics & resources where needed. Rules:
  - Track provenance: every borrowed texture/sound/model gets an entry
    in `resources/ATTRIBUTION.md` (source project, author, license).
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
  Primitive/World`, pack name `Hearthwind Server`); internal mod ids
  stay `aged_*` for now and migrate to first-party ids during the
  namespace audit below.
- **Credits**: Hearthwind is *inspired by* the original Aged pack
  (xR4YM0ND & contributors) and by Genesis (marianyp) — both credited
  in every mod's metadata; Aged resources remain credited per
  ATTRIBUTION.md if/when adopted.

- **Naming/branding pass**: pick final name/logo; mod ids stay stable
  (`aged_skills` etc. can be aliased), but display names, `modmenu`
  metadata, pack description, and world flavor text switch to ours.
- **Asset replacement**: replace upstream textures starting with the
  most visible surfaces (custom items, HUD art, GUIs). ATTRIBUTION.md
  shrinks toward zero or becomes permanent credits where licenses allow.
- **Namespace audit**: our data namespaces (`nutritionz:`, `levelz:`,
  `spoiledz:`) intentionally mirror originals for corpus compatibility;
  when parity is complete, migrate attachments/tags to first-party
  namespaces (`aged:*`) with a one-time world upgrade script.

### Phase C — Divergence / best-of

Maintain a standing **borrow board** of features worth adopting:

| Idea | Source | Status |
|---|---|---|
| Client animations & instruction toasts | Genesis | studied — rebuild in-house (ideas/genesis-comparison.md) |
| Age/story progression gating | Genesis | rebuild via advancement-wrapping |
| Terrain-quality rivers/coasts | Tectonic/Terralith | watchlist, next version bump |
| Wave surfaces | shaders/companion mod | ideas/rivers-and-waves.md Phase 3 |
| … | … | add as discovered |

Divergence rule of thumb: adopt *mechanics* freely (reimplemented in our
code), adopt *assets* only with license-compatible attribution, never
redistribute another mod's jar.

## Practical consequences for day-to-day work

1. New gameplay code always lands in OUR modules — never patch upstream
   jars.
2. Every asset copied from anywhere gets an ATTRIBUTION.md entry at copy
   time, not later.
3. Flavor text (messages, item names) should be written as ours from the
   start (English for now) rather than copying original wording.
4. When choosing between "exact parity" and "clearly better," prefer
   better if it stays config-tunable back to parity behavior.

## Immediate backlog from this decision

- [x] Naming: **Hearthwind** (display names live; ids migrate later)
- [ ] Create `resources/ATTRIBUTION.md` skeleton + record what upstream
      assets we currently ship (audit `resources/`, `configs/`).
      *(ATTRIBUTION.md exists at repo root; audit when assets arrive.)*
- [x] Genesis-style client animations added to the borrow board and
      considered when the companion client module starts.
