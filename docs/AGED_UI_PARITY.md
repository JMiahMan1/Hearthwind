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
Re-measured off the 1.21 captures on 2026-09-28 (0.1.33): the panel is 200x215
rows - row 0 the black top edge, rows 1-2 the 2 px white inner ring, rows 3-211
the flat `#C6C6C6` face, rows 212-213 the `#555555` bottom shadow and row 214 the
black outer edge; columns likewise black / white / face / `#555555` / black with
a 1 px corner staircase (`job_background.png` in the JobsAddon gallery capture is
the same chrome). **The 20 px black tab band 0.1.32 shipped was a misreading of
the floating tab strip as a band** - see §5. Content coordinates are
panel-relative, row 0 at the panel top.
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
attribute readouts, segmented XP bar, 12 skill rows, [+] controls, shared
LibZ tabs floating above the panel, and the right icon rail: an attributes toggle
that opens the slide-out attribute panel (live values, icons) plus mining and
crafting gate buttons that open the restriction lists. Clicking a skill opens
the client-only `SkillInfoScreen`. Remaining Aged UI parity: sprite icons
(ours are item stand-ins for GPL art), hub scroll/slider, and the full
per-level bonus content text.

## 2. SkillInfoScreen drill-down (partial)

Aged: 200×236 textured (`skill_info_background.png`), title + `Lv.X` header,
scrollable LineWidgets (10 visible): skill desc lines
(`skill.levelz.<key>.<i>`), bonus lines (`bonus.levelz.<key>.<i>`), then
per-level restriction lists (item/block/entity/enchantment icons in rows of
9). Scroll slider. E → back to LevelScreen, K → LevelScreen.

Hearthwind `SkillInfoScreen` ships the Aged panel geometry, skill icon, level,
progress bar, description, per-skill bonus lines read from the live skills
config, and the per-level unlock lists (gate icons grouped by level with
overflow counts and tooltips). Remaining: the full scrollable LineWidgets
list and Aged sprite treatment.

## 3. SkillRestrictionScreen (shipped)

Aged: list screen for crafting/mining/use gate maps, opened from the hub
rail buttons. Hearthwind `SkillRestrictionScreen` ships the panel, title and
entry count, eight scrollable rows with target icon, name (tooltip with the
full requirement), the requiring skill and level, a slider and Back; mining
and crafting open from the hub rail, while item and creature gates surface in
the skill drill-down unlock lists. Data comes from the live server gate
snapshot, falling back to the local gate files when disconnected.

## 4. Nutrition screen geometry

Aged `NutritionScreen`: **176×142** single-atlas panel (`nutrition_icons`);
5 rows pitch **23**: item icon (x+7,y+25) + name (x+28,y+26), **141×5 bar
below** (x+27,y+36), numeric `value / max` at x+127; bar hover zones: left
31px → negative-effect tooltip, right 31px → positive-effect tooltip; back
arrow (11×10 at 5,5) → inventory. NO self-drawn tabs (LibZ draws them on
tabbed screens only; plain screens like this have none).

Hearthwind `NutrientsScreen`: ✅ **pixel-exact.** It blits the same
176×**142** region of the same atlas, title at y+7, rows at y+25/y+26/y+36
with a 23 px pitch, the 141×5 bar at x+27, and the `%s/%s` value at x+127.
Verified twice, not assumed: the reference capture
(`.tmp/aged-gallery/Nutrition_Screen.png`, 2845×1600) measures S=6.0 with
the panel at (895, 375) and the five 30 px bar bands on logical rows
36/59/82/105/128, and the row-1 value column's first dark pixel is at
exactly logical x=127. (This section previously claimed a 176×166 panel
with a 24 px pitch and "deltas to fix" - that was stale, left over from
before the screen was rebuilt against the real 1.0.11 source.)

## 5. LibZ tabs (the real tab system)

24px wide, 25px step, ABOVE the panel (y-23 selected/27 tall, y-21
unselected); 14×14 texture or item-stack icon; hover tooltip with title;
click switches screen. Registered per screen class (`inventoryTabs` for
inventory-parented screens, `otherTabs` keyed by parent class); gated by
`inventoryButton` config + `shouldShow`/`canClick` per tab.

The measured gallery captures confirm it on every panel: the strip **floats on
the 21 rows above the panel** (every tab top is exactly `panelY - 21`, the
selected tab is only taller so it merges into the panel's top edge, and the
empty area to the right of the tabs is the page background, not a band). The
inventory capture floats the same way. So one placement ships
(`TabStrip.draw` / `TabStrip.clicked`, restTop = panelY-21, raised 2 px) and
`ScreensTourGameTests` asserts the floating hit boxes: a click in the strip
above the panel picks a tab, a click on the panel face does not. 0.1.32 briefly
shipped a second, "banded" placement on a 200×236 panel - both were the same
misreading of the capture, corrected in 0.1.33.

Hearthwind `TabStrip`: four always-present tabs (Inventory, Skills, Jobs, Party)
with LibZ geometry floating above the panel, and the inventory is rendered as
the real vanilla panel. Clicking a tab switches screens; the bag tab returns to
inventory. The strip also appears on the new skill detail screen.

## 6. Jobs

Aged/JobsAddon: jobs positioned by numeric id, 14×14 GUI textures,
per-job max levels, jobs earn LevelZ XP; the PACK allows up to 3 active
jobs. The measured `Job_Screen.png` capture (re-measured 0.1.33 against the
200×215 panel) puts the title on panel row 7, "Job Cooldown: MM:SS" on row 20,
the employed line on row 33, and eight
91×38 cards in two columns from row 47 with a 95/41 pitch, a 16×16
icon at (+4,+4), the name at (+23,+4), "Lv. N" at (+35,+19) and an 81×5 XP bar
flush with the card bottom.
Order: Lumberjack/Miner, Farmer/Warrior, Builder/Smither, Fisher/Brewer.

Hearthwind: same panel geometry, the same 91×38 cards, pitch, icon slot and
XP bar, and the same job order. Up to 3 active jobs, and the employed line
now names every job ("Employed Jobs: Miner, Lumberjack, Fisher") instead of
truncating. Still open: Aged's 14×14 per-job GUI textures (we use vanilla
item icons). (Server `/job join/leave/info` + XP hooks exist.)

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
  scroll): hub, attributes slide-out, restriction rail + screens and the
  SkillInfo drill-down are shipped; hub scroll/slider and the full bonus text
  remain.
- P2: LibZ-geometry tabs + jobs multi/textures: tabs shipped (floating above
  every panel, the 200×215 chrome re-measured 0.1.33); multi-job shipped, the
  14×14 job textures remain.
- P3: main-menu background: shipped Hearthwind menu, Aged art parity remains.
