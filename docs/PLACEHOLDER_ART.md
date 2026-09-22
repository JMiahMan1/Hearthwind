# Placeholder art tracker

Solid-color generated PNGs (from `custom-mods/tools/gen_placeholder_assets.py`)
plus port-introduced reuse. Everything here needs real pixel art before it
can be called done. Generated files are committed; rerun the script after
adding items, then replace PNGs in place (models/lang stay valid).

(Note: `aged-primitive` / `aged-survival` keys used to live in the generator
from the fork days; no such modules exist, so they were pruned.)

## hearthwind-survival (`dehydration`)

| Item | Should depict |
|---|---|
| `water_bowl`, `purified_water_bowl` | Wooden bowl, murky vs clear water |
| `hot_water_bowl`, `hot_purified_water_bowl` | Same + steam/bubbles |
| `cold_water_bowl`, `cold_purified_water_bowl` | Same + ice tint |

## hearthwind-primitive (`hearthwind`, `environmentz`)

| Item | Should depict |
|---|---|
| `clay_cup*` (8 variants: unfired/fired × water states) | Clay cup, color shifts by content |
| `wolf_pelt`, `polar_bear_fur`, `ice_pack`, `heating_stones` | Pelts, ice pack, warm stones |
| `wolf_*_armor` (helmet/chestplate/leggings/boots) | Fur armor set |
| `wanderer_*_armor` | Traveler cloth set |

## letsdo ports (introduced by the 26.2 ports)

| Item | Status | Should depict |
|---|---|---|
| `vinery:honey_cordial` | **Reuses `apple_juice.png`** | Golden honey drink in wine bottle |
| `brewery:root_beer`, `small_beer`, `kvass`, `coffee` (block + item) | **Reuse `beer_wheat.png`** (mug w/ amber contents) | Medieval NA drinks; root beer dark sassafras brown |

## Upcoming (brewery port)

| Item | Status | Should depict |
|---|---|---|
| `brewery:sassafras_root` (name TBD) | New crop/loot item | Reddish-brown root cluster |
| `brewery:honey_cordial`? (if shared) | Decide: reuse vinery's or brewery-local | Same golden drink |

## Notes

- Purified water (`dehydration:purified_water`) reuses the vanilla water
  texture client-side; it wants its own translucent tint (Dehydration uses
  light blue 3708358) once client fluid rendering is wired.

- Equipment/armor placeholder visuals are server-side only; client armor
  models (`Winemaker*`, wolf/wanderer sets) also want real geometry/textures.
- Rule: never ship a new item without EITHER real art or an entry here.
