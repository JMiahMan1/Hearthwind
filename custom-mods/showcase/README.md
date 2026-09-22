# Hearthwind texture/mod showcase

Generated datapack that renders every ported texture in-world for visual
verification (the headless complement to `.tmp/texture_audit.py`).

## Regenerate

```bash
cd custom-mods && python3 tools/gen_showcase.py
```

Scans every module's `assets/*/items/*.json` (plain-model defs only) and
`assets/*/blockstates/*.json`, skips the `minecraft` namespace and the
`PRUNE_*` codeless ids, and writes one function per module plus `setup`.

## Run (needs a server with all pack mods, e.g. dev-server)

```
forceload add 0 0 80 320        # band 1 (repeat per band below)
/function showcase:setup
forceload remove 0 0 80 320
```

Bands (x 0-80): z 0-320, z 340-660, z 680-1000. y=70, 40 columns.
Blocks land on a stone-brick platform per module band; items are summoned
then cleaned up by the trailing `kill`. Any `Unknown block/item` error in
the log is a real gap: add the id to `PRUNE_*` only if it has zero java
mentions (codeless), otherwise fix the registration/assets.

## Physical copy

`Hearthwind-Dev-Client/saves/Showcase/` holds a baked copy of this grid
(copied from dev-server after a clean `setup` run). Teleport to
`40 72 40` and walk +z through the module bands.
