# Aged UI Parity — researched truth vs Hearthwind client

Researched 2026-09-04 from upstream 1.21 sources (behavioral parity, clean-room
reimplementation — no upstream code/assets copied):
- `Globox1997/LevelZ@1.21` `screen/LevelScreen.java` (24 KB), `SkillInfoScreen.java`,
  `SkillRestrictionScreen.java`, `screen/widget/`
- `Globox1997/LibZ@1.21` `util/DrawTabHelper.java`, `api/Tab.java`
- `xR4YM0ND/NutritionZ@1.21` `screen/NutritionScreen.java`
- `Globox1997/JobsAddon` README (job GUI: id-positioned, 14×14 textures at
  `assets/jobsaddon/textures/gui/<key>.png`, per-job maxlevel; jobs level up)
- ModDex Aged page (surface inventory: Nutrition Screen, Level Screen (K),
  per-skill screens incl. Mining with top-right button, crafting-book button,
  custom main-menu background via FancyMenu)

## 1. Level screen = the hub (no separate hub screen)

Aged `LevelScreen`: **200×215** textured panel (`skill_background.png`).
- Title `text.levelz.gui.title` with player name, centered ~x+118.
- 3D player preview top-left (`InventoryScreen.drawEntity`, 30px scale),
  rotatable with two arrow buttons beside it.
- Overall level label, skill-point label, **XP bar** (131×5) with
  current/total XP text.
- 12 skill rows, 88×20 each in 2 columns (x+8 / x+96, pitch 20 from y+87):
  16×16 skill sprite (`textures/gui/sprites/<key>.png`), `Lv.X/max` text,
  **+/- stepper buttons** at row ends (StatPacket +1). Scrollable + slider.
- Right icon rail (x+178): attributes toggle opens **82px slide-out panel**
  with live attribute values + icons, scrollable past 15 rows; crafting-gate
  and mining-gate buttons open **SkillRestrictionScreen** lists.
- Click skill icon → **SkillInfoScreen(skillId)** drill-down.
- K or E closes; LibZ tabs on top; no pause.

Hearthwind `SkillsScreen`: 200×215 Aged-style panel with player preview, six
attribute readouts, segmented XP bar, 12 skill rows, [+] controls, and shared
LibZ tabs. Clicking a skill opens the client-only `SkillInfoScreen` for its
icon, level, progress, description, and back navigation. Remaining Aged UI
parity: the attribute slide-out, scroll/slider, restriction rail, sprite icons,
and the full per-level bonus/restriction content.

## 2. SkillInfoScreen drill-down (partial)

Aged: 200×215 textured (`skill_info_background.png`), title + `Lv.X` header,
scrollable LineWidgets (10 visible): skill desc lines
(`skill.levelz.<key>.<i>`), bonus lines (`bonus.levelz.<key>.<i>`), then
per-level restriction lists (item/block/entity/enchantment icons in rows of
9). Scroll slider. E → back to LevelScreen, K → LevelScreen.

Hearthwind `SkillInfoScreen` is shipped with the Aged panel geometry and
back navigation, plus skill icon, level, progress bar, and concise description.
The remaining work is the full scrollable description/bonus/restriction list
and Aged sprite treatment.

## 3. SkillRestrictionScreen (missing)

Aged: list screen for crafting/mining/use gate maps, opened from the hub
rail buttons. Hearthwind gates are enforced + logged, but the restriction list
screen is not yet shipped.

## 4. Nutrition screen geometry

Aged `NutritionScreen`: **176×142** single-atlas panel (`nutrition_icons`);
5 rows pitch **23**: item icon (x+7,y+25) + name (x+28,y+26), **141×5 bar
below** (x+27,y+36), numeric `value / max` at x+127; bar hover zones: left
31px → negative-effect tooltip, right 31px → positive-effect tooltip; back
arrow (11×10 at 5,5) → inventory. NO self-drawn tabs (LibZ draws them on
tabbed screens only; plain screens like this have none).

Hearthwind `NutrientsScreen`: 176×**166** panel, pitch **24**, custom tab
strip, separate bar sprites. Deltas to fix: panel height, row pitch,
bar placement/width, numeric format, effect-zone tooltips, drop own tabs.

## 5. LibZ tabs (the real tab system)

24px wide, 25px step, ABOVE the panel (y-23 selected/27 tall, y-21
unselected); 14×14 texture or item-stack icon; hover tooltip with title;
click switches screen. Registered per screen class (`inventoryTabs` for
inventory-parented screens, `otherTabs` keyed by parent class); gated by
`inventoryButton` config + `shouldShow`/`canClick` per tab.

Hearthwind `TabStrip`: four always-present tabs (Inventory, Skills, Jobs, Party)
with LibZ geometry, and the inventory is rendered as the real vanilla panel.
Clicking a tab switches screens; the bag tab returns to inventory. The strip
also appears on the new skill detail screen.

## 6. Jobs

Aged/JobsAddon: jobs positioned by numeric id, 14×14 GUI textures,
per-job max levels, jobs earn LevelZ XP; the PACK allows up to 3 active
jobs. Hearthwind: single active job, vanilla item icons, no job textures,
no multi-job. (Server `/job join/leave/info` + XP hooks exist.)

## 7. Main menu background (out of scope for now)

Aged custom main menu = FancyMenu pack config + custom art. No Hearthwind
equivalent yet; tracked, not part of menu/tab parity.

## 8. 26.x color trap (applies to all of the above)

Aged sources use `0x3F3F3F` (opaque in 1.21). In 26.x that alpha is 0x00
→ invisible. All ported text MUST use `0xFF3F3F3F` form (already the house
rule; keep when rebuilding).

## Implementation phases

- P0: nutrients geometry parity (142 panel, 23 pitch, bar+value layout,
  effect-zone tooltips, drop own tab strip) + this doc: shipped.
- P1: skills hub rebuild (200×215 panel, player preview, XP bar, steppers,
  attributes slide-out, restriction buttons + screens, SkillInfo drill-down,
  scroll): hub + SkillInfo shipped; restriction rail/scroll/full content remains.
- P2: LibZ-geometry tabs + jobs multi/textures: tabs shipped; jobs parity is
  separately tracked.
- P3: main-menu background: shipped Hearthwind menu, Aged art parity remains.
