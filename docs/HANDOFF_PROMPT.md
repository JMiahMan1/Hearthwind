# Hearthwind parity handoff prompt

You are taking over Hearthwind (`/Users/jeremiahsummers/Code/Hearthwind`, branch `master`): a
server-focused rebuild of the Aged 3.1.2 modpack on Minecraft 26.2. **Read `AGENTS.md` first** - it
is binding (build commands, container test harnesses, the 26.2 API crib, hard-won traps, scratch
policy). Then `docs/RELEASE_1.0_PARITY.md` (the 1.0 plan and ledger), `docs/GUIDEBOOK_PARITY.md`,
`docs/AGED_UI_PARITY.md`, `docs/PLAYER_CHANGES.md`, `docs/VERSIONING.md`.

## Standing order

Exact Aged parity. **Nothing is ever "deferred": if Aged 3.1.2 ships it, Hearthwind implements it.**
Docs must not park anything as deferred/pending. Ship continuously - every batch that passes the
gates cuts the next patch release.

## State at handoff

- v0.1.22 is published and asset-verified; `8d8483242` adds the skills-info scroll page that missed
  the 0.1.22 cut. Cut 0.1.23 from that tree.
- Reference pack: `.tmp/Aged-3.1.2.mrpack` (212 mods; extracted index at
  `conversion/curated/aged-3.1.2-index.json`). Parity ledger: `conversion/curated/mods-manifest.json`
  and `aged-missing-allowlist.json`; gate: `python3 conversion/scripts/aged_parity_diff.py` (PASS).

## Release pipeline (docs/VERSIONING.md)

1. `cd custom-mods && ./gradlew build --no-daemon --max-workers=2 -q` (never from the repo root).
2. Bump `conversion/build.conf.json` `pack.version`.
3. `python3 conversion/scripts/resolve_deps.py --allow-missing` then
   `python3 conversion/scripts/build_pack.py --server-dir`.
4. `python3 custom-mods/tools/verify_pack.py --all` (all three packs must print OK).
5. `bash custom-mods/tools/update_prism.sh` (refuses while a game is open - never force past it).
6. `python3 conversion/scripts/aged_parity_diff.py` and `python3 custom-mods/tools/validate_models.py`.
7. Tests: `bash custom-mods/tools/run_gametests_container.sh` (server suite, filters allowed);
   `CGT_EXCLUDE_MODS=DistantHorizons,fancymenu bash custom-mods/tools/run_client_gametests_container.sh`
   (client tour + screenshots; every new screen/page lands with a screenshot).
8. Commit, **`git log --oneline -3` to verify**, push `master`, tag `vX`, push the tag.
9. `gh run watch <id> --exit-status > .tmp/logs/ci.log 2>&1; echo WATCH_EXIT=$?`; if the documented
   fastutil/dungeonz mock-join flake appears, `gh run rerun <id> --failed` (clears on rerun).
10. `python3 custom-mods/tools/verify_pack.py --release vX` - the published packs must contain every
    module (51/52/51 override jars) and be uncorrupted before calling the release done.

## No-deferral work list (all of it must land)

- **Bamboo pump** (last open water item): clean-room block + block entity + renderer in
  `hearthwind-survival` with our own art (upstream Dehydration 1.3.6 is GPLv3 - do not copy assets).
  Bytecode dumps: `.tmp/bp_block.txt`, `.tmp/bp_entity.txt`. Rules: first use attaches; sneaking with
  an empty hand hands the stored container back; bucket/bottle/leather flask inserts; `EXTENDED`
  toggles; when extended each use pumps (`increasePumpCount(1)`); bucket needs `pumpCount > 3` ->
  purified bucket, glass bottle -> purified-water potion, flask -> `fillFlask(stack, 2)`; the
  cooldown ticks down every tick and travels in the item's `"Cooldown"` NBT across break/place;
  messages `block.dehydration.bamboo_pump`, `.cooldown` ("Pump Cooldown: %ss"), `.no_water`
  ("Pump found no water"); config `pump_cooldown` / `pump_requires_water`; recipe
  `dehydration:bamboo_pump`; gametests for the fill, cooldown and no-water paths.
- **BackSlot Addon** (`.tmp/addons/bsa`): shield-on-back/double-sword/lantern transforms and light,
  config + `belt_lantern_items` tag.
- **Inmis Addon** (`.tmp/addons/ia`): 3D backpack models, GUI skins, Trinkets backpack slot.
- **Job XP**: anvil/smithing output, brewing potion ladder, builder placing.
- **End Remastered**: own `ancient_portal_frame` block + portal predicate + acquisition
  (`ERTrades`/loot) with `endrem.json` (`CAN_REMOVE_EYE`).
- **Nameplate**: ship it disabled exactly like Aged (`nameplate.json` `showLevel=false`).
- **pour_*_leather_flask** recipes; **BackSlot nuances** (damaged-item take guard,
  `changeSlotArrangement`, offhand/putAside/dropHolding swap semantics, sounds, slot glyphs, mending
  repair, entity tracking, death drops); **Inmis dye tinting** for the frayed backpack.
- **RPGDifficulty remainder**: protection/speed caps, random values, `extraXp`, `dropMoreLoot`,
  special zombies, `dynamicBossModification`/`bossMaxFactor`.
- Check `dungeonnowloading` in the Aged index; if Aged ships it, port it. Then the W4 queue (tier A
  first), W5 resource packs/shaders, W6 release gate.

## Traps that already cost time

- Selective `git add`s hid three files from the 0.1.22 pack - always `git status` before cutting and
  `verify_pack --release` after publishing.
- The client-gametest API waits only 10 s for its dedicated server (flaky on a loaded host, fine on
  CI). `CGT_EXCLUDE_MODS=DistantHorizons` avoids the DH teardown deadlock; add `fancymenu` because
  its first-run popup dirties fresh test dirs.
- Never deploy jars while a Prism instance is running; `gradlew` only from `custom-mods/`; all
  scratch under `.tmp/`.
