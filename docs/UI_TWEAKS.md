# UI tweaks vs Aged gallery

Source of truth: https://modrinth.com/modpack/aged/gallery
(shots saved under `.tmp/aged-ui/`: nutrition.png, level.png, job.png,
inventory.png + webp thumbs). Reference mod: corekeeper (Aged's core mod)
is an assets-only resource pack retexturing vanilla GUIs — that is the
"semi normal" chrome the user pointed at.

## What Aged's screens look like

- **Tabs**: square ITEM-ICON tabs (no text), anchored panel top-left,
  active tab raised and merged into the panel (no bottom border,
  panel-colored face), inactive tabs darker face + border, tucked 2 px
  behind the panel edge. Icons: pot = nutrition, sword = skills,
  tools = jobs, humanoid = party.
- **Jobs screen**: 2-column card grid (darker gray cards, dark border);
  each card has an item icon in a dark slot square, white job name,
  `Lv. 0` centered, segmented dark-green XP bar along the card bottom;
  header lines "Job Cooldown" / "Not Employed".
- **Skills screen**: 2-column icon + `0/30` + gray beveled [+] button
  rows; player render top-left; stats icon grid; "Level 0 / Points 2";
  vanilla-style segmented XP bar; green guide-book button top-right.
- **Nutrition screen**: label + right-aligned value `145/300` above the
  bar; segmented bar (bright fill, same-hue dark track, tick marks);
  item icon per nutrient; back arrow top-left.
- **Inventory**: 4 icon tabs merged on the inventory top-left + slim
  left button column (armor shortcuts), player preview with teal
  `Lv. 0`, 2x2 crafting, green book button.

## Tweaks applied this pass (all live-verified or gametest-covered)

1. **TabStrip rewritten to icon tabs** — square 24x22 icon tabs
   (Apple / Glass Bottle / Campfire / Iron Sword / Iron Axe), left
   anchored at panel edge, 1 px gap, active tab raised 2 px and merged
   into the panel (no bottom border), inactive tabs darker + hover
   brightening. Matches Aged's chrome.
2. **SurvivalInfoScreen (Skills/Jobs/Thirst/Temp) rebuilt** — dark
   readable text on the light panel (no white-on-gray haze), all labels
   fit within the 176 px panel, hints shortened.
3. **Jobs → card grid** — 2-column cards (79x26) with icon slot, job
   name, current job shows green face + `Lv N` + XP progress bar,
   non-current shows Join affordance; click card to join/leave
   (server-authoritative via `/job join|leave`).
4. **Skills → 2-column icon rows** (14 px icons, right-aligned Lv).
5. **NutrientsScreen** — values right-aligned to the bar edge, dark
   text scheme throughout.
6. **Crafting rock grid renderer** — placed items render as GROUND
   drops over their 3x3 compartment (scale 1.15, y +0.125 lift) so the
   player can see which compartment each item sits in; the mirrored
   slot math matches the server's hit-mapping.
7. **Sieve renderer** — sifted stack renders INSIDE the basin (below
   the lattice plate): scale 0.62, base heights 0.20/0.175/0.15/0.125
   by sieve count, item fully under the 0.875 plate.

## Known remaining gaps (epics, not this pass)

- **Inventory integration**: Aged merges the panels into the inventory
  screen (icon tabs on the inventory top-left + left button column).
  We currently use standalone screens opened via `N`/buttons; merging
  is a follow-up epic (mixins on InventoryScreen anchored to
  `leftPos/topPos` — never recompute the panel x).
- Segmented XP bars + tick marks on nutrients bar (we use plain fills).
- Skills screen extras (player render, stats grid, guide-book button).
- Blast furnace extra-slot icon needs one final in-game look.
