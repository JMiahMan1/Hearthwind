# Aged hydration / campfire / water — spec and port status

Researched 2026-09-28 from Aged 3.1.2 (MC 1.20.1) and the Dehydration
1.3.6 jar Aged ships, reverse engineered with `javap -p -c`:
- `.tmp/dehydration-1.3.6.jar` → `.tmp/dehydration-1.3.6-classes/` (the
  intermediate used to decode `net/dehydration/**`)
- `.tmp/aged-3.1.2/overrides/config/dehydration.json5` (Aged's 7 overrides)
- `.tmp/aged-3.1.2/overrides/config/paxi/resourcepacks/aged_guide_book/assets/aged/lavender/entries/aged_guide_book/{hydration,start,temperature}/*.md`
  and `.../lavender/structures/campfire_cauldron.json`
- `.tmp/aged-3.1.2/overrides/config/paxi/datapacks/aged/data/{dehydration,minecraft,aged,levelz,jobsaddon}/**`
- `.tmp/mappings/y1201/mappings.tiny` (yarn 1.20.1+build.10) to resolve
  every `class_*` / `field_*` id
- Hearthwind side: `custom-mods/hearthwind-survival/**`,
  `custom-mods/hearthwind-client/**`, `custom-mods/hearthwind-skills/**`,
  `conversion/datapacks/hearthwind/data/**`, and the 26.2 vanilla sources in
  `custom-mods/.gradle/loom-cache/.../minecraft-merged-*-26.2-sources.jar`

> **1.0.0 scope:** `docs/RELEASE_1.0_PARITY.md` governs. This file is the
> detailed Aged-vs-Hearthwind comparison for one subsystem, in the style of
> `docs/AGED_PARITY.md` (§2 gameplay tables) and `docs/AGED_UI_PARITY.md`.

## 1. Scope and method

**Reverse engineered:** the whole Dehydration 1.3.6 surface that touches
water — the two campfire mixins, `ThirstManager` and its two effects, the
`EventInit` `UseBlockCallback` (sipping and bowl filling), the 5 leather
flasks, both water bowls, the purified bucket/fluid/potion, the copper
cauldron family, the campfire cauldron multiblock, the bamboo pump, the
brewing hooks, and the 37-key config surface. Every number below comes from
bytecode offsets, not from the mod's own book.

**Mapping caveats.** Dehydration 1.3.6 is compiled against Fabric
intermediary for **1.20.1**, so every class reference reads `class_NNNN`.
Resolved against `yarn-1.20.1+build.10` (all ids re-verified 2026-09-28):

| id | 1.20.1 name | note |
|---|---|---|
| `class_1842` | `Potion` | not `BlockState` |
| `class_1844` | `PotionUtil` | the 1.20.1 helper (`getPotion` / `setPotion`); the 1.21+ name for the same role is `PotionContentsComponent` |
| `class_1847` | `Potions` | |
| `class_1812` | `PotionItem` | the item-class test in both campfire mixins |
| `class_2237` | `BlockWithEntity` | |
| `class_3922` | `CampfireBlock` | |
| `class_3924` | `CampfireBlockEntity` | |
| `class_1264` | `ItemScatterer` | not `ItemEntity` |
| `class_1269` | `ActionResult` | `field_5812` = `SUCCESS` |
| `class_1267` | `Difficulty` | `field_5801` = `PEACEFUL`, `field_5802` = `NORMAL` |
| `Items.field_8574` | `Items.POTION` | the generic potion item, **not** a glass bottle |
| `Items.field_8469` | `Items.GLASS_BOTTLE` | |
| `Items.field_8705` / `field_8428` / `field_27876` | `WATER_BUCKET` / `BOWL` / `POWDER_SNOW_BUCKET` | |
| `Items.field_8665` / `field_17532` / `field_8070` | `CHARCOAL` / `KELP` / `GHAST_TEAR` | the three brewing ingredients |
| `Blocks.field_10124` / `field_10593` / `field_27097` | `AIR` / `CAULDRON` / `WATER_CAULDRON` | |
| `Stats.field_17486` | `INTERACT_WITH_CAMPFIRE` | |

> **Load-bearing claim, re-verified directly.** The report's
> "a LIT campfire is not required to place the bottle" is **correct**:
> `javap -p -c net/dehydration/mixin/CampfireBlockMixin.class` shows
> `onUseMixin` offsets 0–79 containing three early-return guards and one
> success test, and nothing else: `isClient()` at 0–4,
> `item.getItem() instanceof class_1812` at 7–15,
> `PotionUtil.getPotion(stack) == class_1847.field_8991` at 18–26, then the
> `addItem(player, stack, 1000)` call at 29–60 whose boolean is the success
> test. There is no `LIT` read and no `getValue` call anywhere in the
> method. 26.2's own `CampfireBlock.useItemOn` also has no `LIT` gate, so
> the behaviour reproduces directly.
>
> evidence: `javap -p -c` of `CampfireBlockMixin` (`offsets 0,7,18,29,54,63,71,79`)
> and `method_9576` (`offsets 6–47`), cross-read against
> `CampfireBlock.java:91-112` and `CampfireBlockEntity.java:53-98` in the
> 26.2 sources jar.

## 2. The campfire water loop

`CampfireBlock.onUse` is intercepted at the `INVOKE_ASSIGN` of
`CampfireBlockEntity.getRecipeFor`, i.e. **before** the recipe book is
consulted, so Dehydration ships no campfire recipe and needs none.
`CampfireBlockEntity.litServerTick` is intercepted at
`ItemScatterer.spawn(World,DDD,ItemStack)` and cancelled.

1. **Place.** Right-click any `minecraft:potion` whose
   `PotionUtil.getPotion` is `Potions.WATER` at a campfire block entity.
   Server-side only (`if (world.isClient()) return`). Creative players get a
   `.copy()` of the stack, everyone else the live stack. Accepted on a lit
   **or unlit** fire — there is no `LIT` check (see §1).
2. **Accept.** `addItem(player, stack, 1000)` puts it in `itemsBeingCooked`
   (4 slots) and sets the shared `cookTime` to the literal `sipush 1000`.
   **1000 ticks = 50 s, hard-coded, not a config value.** The player is
   awarded `Stats.INTERACT_WITH_CAMPFIRE` and `onUse` returns
   `ActionResult.SUCCESS`.
3. **Cook.** `litServerTick` runs only while the block is `LIT` and
   decrements `cookTime` once per tick. A bottle is done at 0.
4. **Complete.** The mixin sees the pending `ItemScatterer.spawn`, and
   instead of vanilla's cooked result it spawns
   `new ItemStack(Items.POTION).set(PotionUtil.setPotion(ItemInit.PURIFIED_WATER))`
   at `(double)pos.getX(), (double)pos.getY(), (double)pos.getZ()` — the
   block's **minimum corner, not the centre** (offsets 22–37 are three
   `i2d` conversions of `getX/getY/getZ` with no `0.5` constant; the
   "block centre" reading is wrong). Velocity, pitch, `noGravity` and
   pick-up delay are all vanilla's, untouched by the mod.
5. **Clear.** `itemsBeingCooked.set(slot, ItemStack.EMPTY)` runs at offset
   68–71, then `world.sendBlockUpdated(pos, state, state, 3)`, then
   `ci.cancel()` — so vanilla's own drop never runs.
6. **No side effects.** No sound is added (`ItemScatterer.spawn` is silent
   and the cancelled branch played none). No custom particle is added; only
   the campfire's own `randomDisplayTick` smoke/lit particles appear.

**Consequences that follow directly from the above:**

| Property | Value |
|---|---|
| Slots | 4 (`itemsBeingCooked` is a 4-slot `DefaultedList`) |
| Result item | a **fresh** `minecraft:potion` — no custom name / NBT carries over; not splash, not lingering |
| Potion contents | one dose, no custom duration — drink it once, +2 thirst in Aged |
| Shared counter | 1.20.1 has a **single** `cookTime` int that `addItem` re-assigns to 1000 on every insertion, so a 2nd bottle restarts the timer for the bottles already in the fire and all slots finish on the same tick |
| Can the un-purified bottle drop? | **No** — the only drop path is the cancelled `ItemScatterer.spawn`. There is no `dropItem`, `popResource` or `Block.dropStacks` in the class |
| Fire goes out mid-boil | `litServerTick` stops, so `--cookTime` stops: **progress freezes, nothing decays.** Relighting resumes from the same counter |
| `water_boiling_time` | **never used here.** It is read in exactly one place in the whole jar, the campfire *cauldron* block entity (§6) |

> evidence: `CampfireBlockMixin.class` `onUseMixin` offsets 0–79,
> `CampfireBlockEntityMixin.class` `litServerTickMixin` offsets 0–88,
> `CampfireBlockEntityAccessor.java` → `field_17383 itemsBeingCooked`.

## 3. Every item, block and fluid

| id | type | hydration / thirst | obtained by | behaviour | config keys read |
|---|---|---|---|---|---|
| `dehydration:purified_water` (potion) | `Potion` with an empty effect array | 2 (template tier 2, see §4) | campfire (50 s), campfire cauldron, copper cauldron | non-infinite, 1 dose, **not** a bad potion | template tier 2 |
| `dehydration:hydration` (potion) | `Potion(HYDRATION, 900)` | 2 + `hydration_effect` | brewing purified water + ghast tear | +1 thirst per 50 ticks while active ⇒ ≈ +18 per dose | template tier 2 |
| `minecraft:potion` holding `purified_water` | potion item | 2 | the two paths above | the physical bottle | — |
| `dehydration:purified_water_bucket` | `PurifiedBucket extends Item implements FluidModificationItem` | **none** | smelting a water bucket, 1.0 exp, 600 ticks | places/collects `PURIFIED_WATER`; 8 `LARGE_SMOKE` particles + a randomised empty sound on the client; recurses into the block below when the target cannot take fluid | — |
| `dehydration:purified_water` (fluid + block) | `FlowableFluid` still + flowing | — | the bucket | splash `UNDERWATER`, drip `DRIPPING_WATER`; counted as "water" via `minecraft:tags/fluids/water` | — |
| `dehydration:water_bowl` | `WaterBowlItem(maxCount 1)` | 3 (template 3) | sneak-fill from a still source | 32-tick DRINK; rolls thirst at `water_bowl_thirst_chance` | `water_bowl_quench`, `water_bowl_thirst_chance`, `potion_bad_thirst_duration` |
| `dehydration:purified_water_bowl` | `WaterBowlItem(maxCount 1)` | 3 (template 3) | sneak-fill from purified water | **identical**, including the 40 % dirty roll (upstream sets `hasThirstChance = true` on both) | same |
| `dehydration:leather_flask` … `netherite_leather_flask` | `LeatherFlask(addition)` | 4 per sip | crafting chain leather → iron → gold → diamond → netherite | capacity `addition + 2` = 2/3/4/5/6; netherite is `fireproof`; `getMaxUseTime` 32; DRINK action only while filled | `flask_thirst_quench`, `flask_dirty_thirst_chance`, `flask_dirty_thirst_duration` |
| `dehydration:campfire_cauldron` | block + BE, `LEVEL` 0–4 | — | 1 stick + 1 `minecraft:chain` + 1 copper cauldron | only exists above a `#minecraft:campfires` block | `water_boiling_time` |
| `dehydration:copper_cauldron` | `CopperCauldronBlock` | — | 5 copper ingots | fills from rain (10 %) or snow (15 %) or a water dripstone, straight to `purified_water_copper_cauldron` | hard-coded 0.1 / 0.15 |
| `dehydration:water_copper_cauldron` | `CopperLeveledCauldronBlock` 1–3 | — | bucket, potion, dirty transfer | dirty; comparator = level | — |
| `dehydration:purified_water_copper_cauldron` | `CopperLeveledCauldronBlock` 1–3 | — | rain, dripstone, purified transfer | clean | — |
| `dehydration:powder_snow_copper_cauldron` | `CopperLeveledCauldronBlock` 1–3 | — | powder snow bucket | fills to level 3 in one use | — |
| `dehydration:bamboo_pump` | block + BE, 1 slot | — | 4 bamboo + 4 planks + 1 stick | stores a bucket/bottle/flask, purifies it, rests for `pump_cooldown` | `pump_cooldown`, `pump_requires_water` |
| `dehydration:thirst_effect` | `ThirstEffect`, HARMFUL, `0x2EC6B6` | −`thirst_effect_factor × (amp+1)` per tick | dirty water, bad potions, bowls, milk | always ticks (`iconst_1`) | `thirst_effect_factor` |
| `dehydration:hydration_effect` | `HydrationEffect`, BENEFICIAL, `0x2EC6B6` | +1 per 50 ticks | `dehydration:hydration` potion | ticks when `50 >> amp` divides the remaining time | — |
| `dehydration:thirst` | damage type, bypasses armour + effects | — | — | "died of thirst" | `thirst_damage` |
| `dehydration:{fill_flask, water_sip, empty_flask, cauldron_bubble}` | sounds | — | — | four custom sounds | — |

There is **no crafting recipe for either bowl** in the jar (its 12 recipes are
the pump, the cauldron, the five flasks, five `pour_<tier>_leather_flask`
resets and the purified bucket). Both bowls come only from the sneak-fill in
§6 — upstream's own Patchouli page links a recipe that does not exist.

> evidence: `ItemInit.class` offsets 40–180 (5 flasks, `iconst_1`
> `hasThirstChance` on **both** bowls at 296–301 and 324–329, no craft
> remainder), 182–193 / 196–223 (the two potions), 249–276 (purified bucket
> `maxDamage(BUCKET,1)`); `BlockInit.class` 0–231; `EffectInit.class` 0–27
> (`ldc 3062757` = `0x2EC6B6`); `unzip -l .tmp/dehydration-1.3.6.jar` recipe
> and tag listing; `assets/dehydration/lang/en_us.json`.

## 4. Thirst maths

**State.** `float dehydration` 0…40 (NBT `ThirstExhaustionLevel`),
`boolean hasThirst` (NBT `HasThirst`, default true),
`int dehydrationTimer` (NBT `ThirstTickTimer`),
`int thirstLevel` **0…20** (NBT `ThirstLevel`, default 20).

**Decay** — `ThirstManager.update(player)`, once per **player tick** from
`PlayerEntity.tick`, gated on `hasThirst`:

```
if (dehydration > 4.0f) {                       // strictly greater
    dehydration -= 4.0f;
    if (difficulty != PEACEFUL) thirstLevel = max(0, thirstLevel - 1);
}
if (thirstLevel <= 0) {
    dehydrationTimer++;
    if (dehydrationTimer >= 90) {
        if ((health <= 10.0f && difficulty != PEACEFUL)
         || (health >   1.0f && difficulty == NORMAL))
            player.damage(thirstSource, thirst_damage);
        dehydrationTimer = 0;
    }
} else dehydrationTimer = 0;
```

Concretely, in **Aged 3.1.2**:

- **1 thirst point per 4.0 exhaustion**, buffer capped at 40, so a full
  20 → 0 bar costs 80 exhaustion. Every vanilla exhaustion source (sprint,
  jump, attack, mine, swim) feeds it through
  `PlayerEntity.addExhaustion` divided by `hydrating_factor` = **2.0** in
  Aged, i.e. Hearthwind-equivalent actions burn thirst **~25 % slower** than
  stock Dehydration (default 1.5).
- **Damage**: `thirst_damage` 1.0 (half a heart) every **90 ticks (4.5 s)**
  at 0 thirst — but only on `NORMAL` above 1 HP, or on any non-Peaceful
  difficulty at ≤ 10 HP. On `HARD` thirst damage **stops entirely above
  10 HP** (an upstream quirk; see §9).
- **Peaceful** never drains thirst (the `dehydration -= 4` branch is still
  taken, the level drop is not) and — see below — regenerates.
- **`special_effects`** defaults to `false` and Aged does not set it, so the
  HASTE-at-2 and MINING_FATIGUE-II-at-0 flavour effects are **inert in Aged**.
- **Passive top-up**: `PlayerEntity.tickMovement` grants **+1 thirst every
  10 ticks** when `difficulty == PEACEFUL` **and** `naturalRegeneration` and
  `hasThirst` and `thirstLevel < 20`. It also calls
  `ThirstManager.update` a *second* time on that path, so on Peaceful the
  exhaustion drain is applied twice per tick.

> **Correction to the report.** Its prose says the top-up runs while
> `difficulty != PEACEFUL`. The bytecode says the opposite: `tickMovementMixin`
> offsets 4–10 are `getDifficulty()` then
> `getstatic class_1267.field_5801` then `if_acmpne 88`, and `field_5801` is
> `PEACEFUL`. The condition is `== PEACEFUL`.
> `HearthwindSurvivalThirst.updatePlayer` implements `== PEACEFUL` and is
> right; do not "fix" it back.
>
> evidence: `javap -p -c` of `ThirstManager.class` `update` (`bipush 90`,
> `ldc 4.0f`, `ldc 10.0f`, `fconst_1`, `field_5801`/`field_5802`, `sipush
> 409`, `iconst_2`), `PlayerEntityMixin.class` `tickMovementMixin` offsets
> 0–88 and `addExhaustionMixin` offsets 0–53, and
> `.tmp/mappings/y1201/mappings.tiny:49414` (`field_5801 PEACEFUL`).
>

### 4.1 Config surface (37 keys)

Aged ships **7** overrides; the other 30 keep the constructor defaults
(AutoConfig writes them back on first boot, so they are the effective
values).

| key | default | Aged | used for |
|---|---|---|---|
| `thirst_damage` | 1.0 | default | the damage amount |
| `hydrating_factor` | 1.5 | **2.0** | `addDehydration(exhaustion / factor)` |
| `thirst_effect_factor` | 0.05 | **0.03** | `addDehydration(factor × (amp+1))` per tick |
| `potion_thirst_quench` | 2 | default | potion fallback, only if no template matched |
| `potion_bad_thirst_chance` | 0.25 | **0.15** | bad-potion thirst roll |
| `potion_bad_thirst_duration` | 300 | default | potion / milk (`/2`) / bowl (`/2`) duration |
| `milk_thirst_quench` | 8 | default | milk fallback |
| `milk_thirst_chance` | 0.4 | default | milk thirst roll |
| `honey_quench` | 1 | default | honey fallback |
| `water_bowl_quench` | 3 | default | bowl fallback |
| `water_bowl_thirst_chance` | 0.4 | default | bowl thirst roll |
| `drinks_thirst_quench` | 2 | default | `#dehydration:hydrating_drinks` |
| `stew_thirst_quench` | 3 | default | `#dehydration:hydrating_stew` |
| `food_thirst_quench` | 1 | default | `#dehydration:hydrating_food` |
| `stronger_drinks_thirst_quench` | 4 | default | `#…stronger_hydrating_drinks` |
| `stronger_stew_thirst_quench` | 6 | default | `#…stronger_hydrating_stew` |
| `stronger_food_thirst_quench` | 2 | default | `#…stronger_hydrating_food` |
| `flask_thirst_quench` | 4 | default | flask sip + tooltip + HUD preview |
| `flask_dirty_thirst_chance` | 0.75 | **0.3** | dirty flask roll (impure uses `× 0.5`) |
| `flask_dirty_thirst_duration` | 500 | **200** | dirty flask duration |
| `water_souce_quench` *(sic)* | 1 | default | bare-hand sip |
| `water_sip_thirst_chance` | 0.75 | **0.5** | sip roll, halved in `BiomeTags.IS_RIVER` |
| `water_sip_thirst_duration` | 600 | **300** | sip thirst duration |
| `allow_non_flowing_water_sip` | false | default | flowing water may be sipped |
| `sleep_thirst_consumption` | 4 | default | `wakeUp` when `sleepTimer >= 100` |
| `sleep_hunger_consumption` | 2 | default | same |
| `water_boiling_time` | 100 | default | **campfire cauldron only** |
| `pump_cooldown` | 1200 | default | bamboo pump rest, ticks |
| `pump_requires_water` | false | default | pump scans `pos.above(i)` for i = 0…49 |
| `bottle_consumes_source_block` | false | default | glass bottle on a source |
| `special_effects` | false | default | HASTE / MINING_FATIGUE flavour |
| `harder_nether` | false | default | multiply exhaustion in one dimension |
| `nether_factor` | 1.3 | default | that multiplier |
| `other_droplet_texture` | false | default | HUD icon variant |
| `hud_x` / `hud_y` | 0 / 0 | default | HUD offset (droplets at `x/2 + 91`, `y − 49`) |
| `thirst_preview` | true | default | every `getTooltipData` override + HUD |

### 4.2 Hydration templates

`DehydrationMain.HYDRATION_TEMPLATES` is loaded from every
`data/<ns>/hydration_items/*.json` by keys `"1"`…`"20"`, each
`{ "replace": bool, "items": [ids] }`. `replace: true` clears the tier
first. **Resolution order** is tag value → any template containing the item
→ hard-coded fallback.

Aged ships `dehydration/hydration_items/aged_items.json` with
`"replace": true` on **all 12 tiers it defines (1–10, 12, 14; 115 items)**,
so the mod's own `vanilla_items.json` (1 melon_slice, 2 potion,
3 suspicious_stew/mushroom_stew/beetroot_soup, 6 rabbit_stew, 8 milk_bucket)
is entirely replaced. Two consequences worth stating:

- `minecraft:potion` is templated at **2**, so `potion_thirst_quench`
  (default 2, identical) is **unreachable** for potions in Aged.
- `suspicious_stew` is *not* at tier 3 in Aged — the vanilla default was
  replaced — it sits at tier 5 with Aged's own additions.

The vanilla ids Aged's `replace: true` tiers actually land, for quick
reference (the other ~100 entries per tier are modded food and drink):

| tier | vanilla ids |
|---|---|
| 1 | `melon_slice`, `honey_bottle`, `sweet_berries` |
| 2 | `chorus_fruit`, **`potion`**, `splash_potion`, `lingering_potion`, `glow_berries` |
| 3 | `mushroom_stew`, `rabbit_stew`, `beetroot_soup`, **`water_bowl`**, **`purified_water_bowl`** |
| 4 | `apple` |
| 5 | `suspicious_stew` |
| 6 | `golden_apple` |
| 7 | — |
| 8 | `enchanted_golden_apple`, **`milk_bucket`** |
| 9 / 10 / 12 / 14 | — |

The six `dehydration:hydrating_*` tags ship **empty** in the 1.3.6 jar and
Aged never fills them, so the tag branch of the resolution order is inert in
Aged.

## 5. The dirty-water matrix

Every source that can hand out `dehydration:thirst_effect`, with Aged's
numbers and the resulting cost. Costs use Aged's
`thirst_effect_factor = 0.03` and the 4.0 dehydration that each thirst
level costs (a level is only lost when the buffer passes 4.0).

| source | chance | amp | duration | dehydration added | thirst lost |
|---|---|---|---|---|---|
| **bare-hand sip** from a still source | 30 % (0.5, `nextFloat() <= chance`) | 1 | 300 (Aged) | 0.03 × 2 × 300 = 18.0 | **4.5** |
| the same, in a `BiomeTags.IS_RIVER` biome | 15 % (chance halved) | 1 | 300 | 18.0 | 4.5 |
| **dirty flask** (`purified_water == 2`) | 30 % (`<= 0.3`) | 1 | 200 (Aged) | 0.03 × 2 × 200 = 12.0 | **3.0** |
| **impurified flask** (`== 1`) | 15 % (`<= 0.3 × 0.5`) | 0 | 200 | 0.03 × 1 × 200 = 6.0 | **1.5** |
| **bad potion** (incl. plain water) | 85 % (`nextFloat() >= 0.15`) | 0 | 300 | 0.03 × 1 × 300 = 9.0 | **2.25** |
| **water bowl** (either bowl) | 60 % (`nextFloat() >= 0.4`) | 0 | 300 / 2 = 150 | 0.03 × 1 × 150 = 4.5 | **1.125** |
| **milk bucket** | 60 % (`nextFloat() >= 0.4`) | 0 | 300 / 2 = 150 | 4.5 | **1.125** |

Two comparison quirks that a faithful port must keep:

- the potion/bowl/milk rolls use `>= chance` (so a 0.15 "chance" is an
  **85 %** risk), while the sip and flask rolls use `<= chance`. Both
  directions are reproduced.
- `isBadPotion` **includes `Potions.WATER`**, so drinking an ordinary
  un-purified water bottle also rolls for 15 % → 85 % Thirst for 300 ticks.
  The 14 bad potions are `WATER, AWKWARD, THICK, HARMING, LONG_POISON,
  LONG_SLOWNESS, LONG_WEAKNESS, MUNDANE, POISON, SLOWNESS, STRONG_HARMING,
  STRONG_POISON, STRONG_SLOWNESS, WEAKNESS`. Every beneficial potion, and
  `dehydration:purified_water`, is **not** bad.

> evidence: `PotionItemMixin.class` `isBadPotion` offsets 0–101 and
> `finishUsingMixin` offsets 39–76, 166–172; `LeatherFlask.class`
> `method_7861` offsets 161–297; `WaterBowlItem.class` `method_7861`
> offsets 98–171; `MilkBucketItemMixin.class` offsets 87–160;
> `EventInit.class` `lambda$init$2` offsets 457–553; `ThirstEffect.class`
> offsets 0–39.

## 6. Sipping, vessels and containers

### 6.1 Sipping from a source (single `UseBlockCallback`)

Entry conditions: not creative, not spectator, **sneaking**, main hand empty
**or** `Items.BOWL`; then `raycast(1.5, 0, 1.0)` must hit a block whose
`FluidState` is in `FluidTags.WATER` (which includes purified water).

**A. Bowl branch** — still fluid only, otherwise `PASS`. Plays
`ITEM_BUCKET_FILL`; the bowl becomes `water_bowl`, or `purified_water_bowl`
if the fluid is in `dehydration:purified_water`; `USE_CAULDRON` +
`PLAYER_USE_ITEM`; **the source block is consumed** (un-waterlogged, else
`Blocks.AIR`); returns `SUCCESS`.

**B. Empty-hand sip:**

1. `!fluid.isStill() && !allow_non_flowing_water_sip` → `PASS`. With Aged's
   default that means **still sources only**.
2. `!thirstManager.isNotFull()` → `PASS` (no sipping at 20/20).
3. `drinkTime++` per tick; resolves at `drinkTime > 20`, i.e. a **21-tick
   (~1.05 s) hold**, not a click.
4. Client: every 3rd tick `ENTITY_GENERIC_DRINK` at volume 0.5, pitch
   `0.9 + rand·0.1`; on completion `dehydration:water_sip` in PLAYERS at 1.0.
5. Server: consume the source block; `+water_souce_quench` (**+1**); if the
   fluid is **not** in `dehydration:purified_water`, roll
   `nextFloat() <= water_sip_thirst_chance` (halved in `IS_RIVER`) and apply
   `thirst_effect` for `water_sip_thirst_duration`, **amp 1**; reset
   `drinkTime`.

Aged's own guide: *"Just sneak and hold right click on a non flowing water
source to drink. The chance that you drink dirty water is very high."*

`GlassBottleItemMixin` (`bottle_consumes_source_block`, false in Aged) is
the only other source-removal hook; with the flag off a plain glass bottle
**leaves the source intact**.

### 6.2 Bowls

32-tick DRINK; `+` template tier (3) else `water_bowl_quench`; then the 40 %
dirty roll at **amp 0** for `potion_bad_thirst_duration / 2` = 150 ticks —
on **both** bowls, purified included, because `ItemInit` constructs them
with `hasThirstChance = true`. Returns the residual stack for non-players and
`ItemStack.EMPTY` for players, so a 1.3.6 bowl has **no craft remainder**: it
is consumed and no plain bowl comes back.

### 6.3 Flasks (5 tiers)

NBT: `leather_flask` (int fill) and `purified_water`
(0 = empty/was drained, 1 = purified only, 2 = dirty).

- **Fill from open water** (`use`): a source whose fluid is in
  `FluidTags.WATER`, held for 20 ticks; writes `purified_water = 2` (dirty);
  plays `dehydration:fill_flask`; awards `USE_CAULDRON` + `PLAYER_USE_ITEM`;
  **destroys the source block** (unless Puddles is loaded and the block is a
  puddle). `BiomeTags.IS_RIVER` **forces `level = 2, purified = false`**, so
  a river always fills dirty.
- **Sip** (`finishUsing`): decrement the fill; `+flask_thirst_quench` (4);
  play `dehydration:empty_flask`; `USE_CAULDRON` + `PLAYER_USE_ITEM`.
  Dirty roll: `purified_water == 2` → `nextFloat() <= 0.3` → **amp 1**;
  `== 1` → `nextFloat() <= 0.3 × 0.5` → **amp 0**; both for
  `flask_dirty_thirst_duration` = 200 in Aged.
- **Tooltip** (`thirst_preview = true`): `Fill Level <n>/<cap>`, or
  `Fill Capacity <cap>` when empty, plus a colour-coded `§2Dirty Water` /
  `§3Impurified Water` / `§bPurified Water`. `getTooltipData` draws a
  droplet preview scaled by `quality × fill × flask_thirst_quench`.
- **Cauldrons**: a vanilla levelled cauldron fills one unit per use, always
  dirty; sneaking pours one unit back. The copper and campfire cauldrons have
  their own rules (§6.4, §6.5).
- **Crafting**: `pour_<tier>_leather_flask` is a 1-ingredient shapeless
  "reset to empty" recipe, and a screen-handler mixin normalises the NBT to 0.

### 6.4 Copper cauldrons

`LEVEL` 1…3, `isFull()` at 3, fluid height `(6 + LEVEL × 3)/16`,
comparator output = `LEVEL`.

| map | accepts | yields |
|---|---|---|
| EMPTY | purified bucket, `Items.POTION`, all flasks, vanilla buckets | — |
| WATER | `Items.BUCKET` (→ purified bucket), `Items.GLASS_BOTTLE` (dirty bottle), `Items.POTION` (dirty potion) | dirty |
| POWDER_SNOW | `Items.BUCKET` | powder snow |
| PURIFIED_WATER | `Items.BUCKET`, `Items.GLASS_BOTTLE`, `Items.POTION`, all flasks | purified |

- Filling with `Items.POTION` jumps straight to the matching levelled
  variant at `LEVEL = 3` and plays `ITEM_BUCKET_EMPTY`.
- Emptying with a glass bottle drops the level by 1 and gives a glass bottle
  holding `Potions.WATER` or `ItemInit.PURIFIED_WATER`.
- Filling with a flask from a purified cauldron sets `purified_water = 1`
  unless the flask already had both tags non-zero (then 0); a dirty cauldron
  sets 2.
- **Rain/snow**: the levelled variant increments `LEVEL` while `< 3` and its
  `precipitationPredicate` matches. The **empty** variant rolls
  `canFillWithPrecipitation` (**10 %** rain, **15 %** snow) and jumps
  straight to `purified_water_copper_cauldron` LEVEL 1 (or powder snow),
  emitting `FLUID_PLACE`.
- **Dripstone**: the levelled variants accept it only if the fluid is
  `Fluids.WATER` **and** the predicate is `RAIN_PREDICATE`; the empty one
  accepts any fluid but acts only on water → purified, LEVEL 1, level event
  1047.
- **Fire**: a burning entity inside decrements the level; a full lava
  cauldron at 1 becomes the empty copper cauldron.

### 6.5 Campfire cauldron (the multiblock)

Crafted from 1 stick + 1 `minecraft:chain` + 1 `dehydration:copper_cauldron`
(pattern `" d "/" e "/"dfd"`). **On 26.2 `minecraft:chain` is
`minecraft:iron_chain`** — the unmigrated recipe vanishes silently.

`canPlaceAt` returns true **only** if the block below is in
`#minecraft:campfires` (lit or unlit); `FACING` comes from the player's
horizontal facing; `LEVEL = 0`; a block entity is always created. Aged's own
multiblock page is exactly two layers, campfire then cauldron.

**The boil.** `CampfireCauldronEntity.update()`:

```
if (block is CampfireCauldronBlock
    && isFireBurning(world, pos)      // pos.down() is a LIT CampfireBlock
    && state.getValue(LEVEL) > 0
    && !isBoiled) {
    if (++ticker >= water_boiling_time) { isBoiled = true; ticker = 0; }
}
```

**100 ticks (5 s) of lit fire**, and the timer **freezes** (no decrement
branch exists) when the fire goes out. `onFillingCauldron()` resets
`isBoiled = false; ticker = 0` whenever fresh water enters. Only `isBoiled`
is written to NBT (key `"Boiled"`), so a chunk unload mid-boil loses progress
but keeps the flag.

**Interactions** (`LEVEL` 0…4, full at 4):

| held item | condition | result |
|---|---|---|
| `Items.BUCKET` | LEVEL < 4 | `onFillingCauldron()`, LEVEL = **4**, `ITEM_BUCKET_EMPTY` |
| `Items.WATER_BUCKET` | LEVEL == 4 | water bucket back, LEVEL = 0, `BLOCK_FIRE_EXTINGUISH` |
| `Items.GLASS_BOTTLE` | LEVEL > 0 | shrink 1; bottle of `PURIFIED_WATER` if boiled else `Potions.WATER`; LEVEL−1; `ITEM_BOTTLE_FILL` |
| `Items.POTION` with potion ∈ {`WATER`, `purified_water`} | LEVEL < 4 | give an `Items.BOWL`; LEVEL+1; pouring plain `WATER` calls `onFillingCauldron()`; `ITEM_BOTTLE_EMPTY` |
| `LeatherFlask` | LEVEL > 0, fill < 2 + addition | `purified_water` = (was non-zero && fill non-zero) ? **1** : **0** if boiled else **2**; fill +1; LEVEL−1; `dehydration:fill_flask` |

**Other:** `precipitationTick` fills on `nextFloat() < 0.2` **and**
`isSkyLit(pos)` **and** `biome.getTemperature() >= 0.15` **and** the biome
is not snowing → LEVEL+1 (cap 4) with `FLUID_PLACE` — so **rain fills and
boils** a campfire cauldron in a warm, sky-lit, non-snowing biome.
`randomDisplayTick` plays `dehydration:cauldron_bubble` at the block centre
whenever `random.nextInt(12) == 0` while the fire burns and LEVEL > 0
(volume `0.5 + rand·0.4`, pitch 1.0). Comparator = LEVEL, not pathfindable.
A `Block.onPlace` mixin destroys a `dehydration:campfire_cauldron` sitting
**above** any freshly placed block (with drops, unless creative) — so
placing a campfire under an existing cauldron breaks the cauldron.

### 6.6 Bamboo pump

Properties `FACING` / `WATERLOGGED` / `EXTENDED` / `ATTACHED`; the entity
holds a 1-slot inventory plus `pumpCount` and `cooldown`.

- Right-click with an empty container (bucket, glass bottle, or a non-full
  flask) → stored in the slot; a bucket also flips `ATTACHED = true`.
- Sneak-right-click with an empty hand → the container is returned,
  `increasePumpCount(1)`, `ATTACHED` and `EXTENDED` reset to false.
- Right-click with a non-empty slot → optional `pump_requires_water` scan of
  `pos.above(i)` for i = 0…49 for a `FluidTags.WATER` fluid (abort with
  `block.dehydration.bamboo_pump.no_water`); then the `pump_cooldown` check
  (abort with `Pump Cooldown: <cooldown/20>s`); then `increasePumpCount(1)`
  when `EXTENDED`, `ITEM_BUCKET_FILL`, and **a purified-water-filled copy of
  the container is written into the pump while the hand is emptied**
  (`split(1)`, copy in creative). One pump converts.
- Every success sets `cooldown = pump_cooldown` (1200 = 60 s) and
  `pumpCount = 0`.

### 6.7 Purified water fluid and bucket

`PurifiedWaterFluid extends FlowableFluid`; `getBucketItem()` →
`dehydration:purified_water_bucket`; splash `UNDERWATER`, drip
`DRIPPING_WATER`. `WaterFluidMixin` makes vanilla water accept both purified
fluids in `matchesType` (skipped when Create is loaded) and overrides
`WaterFluid.flow` so purified water flowing **into** a block replaces vanilla
water at the same level instead of destroy/fill. Tag wiring:
`dehydration:purified_water` = {still, flowing} and
`minecraft:tags/fluids/water` adds that tag, so **purified water counts as
water for sipping, bowl filling, flasks and vanilla bucket logic**.
`CauldronBehaviorMixin` adds `purified_water_bucket → FILL_WITH_WATER` to
the vanilla cauldron map, so a purified bucket fills a **vanilla** cauldron
into `minecraft:water_cauldron` (ported 0.1.48 as a row on
`CauldronInteractions.EMPTY`). Fluid-API storage registers
`81000` mB in both directions.

**A purified bucket gives no thirst at all** — it is not a
`ThirstManager` touchpoint anywhere in the jar, which matches Aged's guide:
*"Haven't found a good use for them yet"*.

### 6.8 Brewing

`BrewingRecipeRegistry.registerDefaults` is injected at `TAIL` and adds
exactly three recipes through the shadowed `registerPotionRecipe`:

| input | ingredient | output |
|---|---|---|
| `Potions.WATER` | `Items.CHARCOAL` | `dehydration:purified_water` |
| `Potions.WATER` | `Items.KELP` | `dehydration:purified_water` |
| `dehydration:purified_water` | `Items.GHAST_TEAR` | `dehydration:hydration` |

Charcoal and kelp are not vanilla ingredients, so nothing vanilla is
overridden: water + nether wart still gives the awkward potion, water +
gunpowder still gives weakness. The mod's own book describes only the
campfire and the kelp route.

## 7. Campfire-adjacent rules owned by other Aged mods

1. **Bark campfire recipe.** Aged's paxi datapack rewrites
   `data/minecraft/recipe/campfire.json` to `C: #aged:campfire_ingredient`,
   `L: #minecraft:logs`, `S: minecraft:stick`, pattern
   `[" S ","SCS","LLL"]`, and
   `data/aged/tags/item/campfire_ingredient.json` =
   `["#minecraft:coals", "#earlystage:bark_items"]`. The guide states it:
   *"There is also a changed recipe for campfires which now accept any bark
   besides coals."*
2. **Campfire loot table.** Aged's `minecraft:loot_table/blocks/campfire.json`
   is a single-roll `alternatives`: silk touch returns the
   `minecraft:campfire`, otherwise `minecraft:oak_log` × 1 subject to
   `survives_explosion`.
3. **Rain extinguishing is AdditionZ, not Dehydration.**
   `additionz.json5` sets `campfire_rain_extinguish: 60` (AdditionZ's own
   default is 20). `net/additionz/mixin/CampfireBlockEntityMixin` injects at
   `TAIL` of `litServerTick` and `unlitServerTick` and persists
   `int rainBurnTime` as NBT `"RainBurnTime"`. Once per second
   (`world.getTime() % 20 == 0`), while it is raining and the sky is lit,
   `rainBurnTime++`; when `rainBurnTime > 60` and
   `random.nextInt(60) != 0` the fire is extinguished.

   **Read the two conditions as a pair, because they are easy to invert.** The
   bytecode continues while `rainBurnTime <= 60` and *returns* on
   `random.nextInt(60) != 0`. So the first 60 samples only increment the
   counter and do nothing else, the 61st sample is the first that can
   extinguish, and every sample after that extinguishes with probability
   1/60. **In Aged a campfire exposed to rain goes out no sooner than 61
   seconds and on average around 121 s** (the upstream default of 20 gives 21 s
   and about 41 s). The counter is *not* reset when the rain stops while the
   fire is still lit - only the unlit tick zeroes it - so a fire that has
   already soaked for a minute keeps that credit the next time it rains.
4. Dehydration itself adds **no** particle, sound or loot-table change to
   vanilla campfires.

> evidence: `.tmp/aged-3.1.2/.../data/minecraft/recipe/campfire.json`,
> `.../data/aged/tags/item/campfire_ingredient.json`,
> `.../data/minecraft/loot_table/blocks/campfire.json`,
> `.../additionz.json5`, and `javap` of
> `.tmp/additionz/x/net/additionz/mixin/CampfireBlockEntityMixin.class`.

## 8. Hearthwind status

Legend: ✅ at parity · 🟡 deliberate deviation (reason in one clause) ·
❌ missing · ❌ unverified (say what could not be found).

Summary: **✅ 99 · 🟡 3 · ❌ 1** over 103 rows. **0.1.49 closed 17 more rows**: a
river fill is DIRTY (we had it exactly backwards), the flask fill needs a 20-tick hold
and destroys the source, the flask tooltip reads `Fill Level n/cap` with the
reference's colour-coded lines, the sip raycast is the reference's fixed 1.5 blocks with
none of our fallbacks, the thirst effect icon is the reference's `0x2EC6B6`,
`nether_factor` is 1.3, a water bowl is consumed with nothing given back, the campfire
cauldron's rain needs sky-light and a 0.15 °C biome, its voxel shape is vanilla's full
cube, the pump converts in one press and scans blocks 0…49 above, and the boil pops at
the block corner with no chime, no steam and no unlit-fire hint. Two rows are marked
**not reproducible on 26.2** (the shared campfire counter, the flask reset recipes).
**0.1.50 closed the last ❌**: all four custom sound events ship with Dehydration's own
`.ogg` files and `sounds.json`, wired at the reference's exact moments — and two of the
readings this file carried for years were wrong, the sip pitch being a division
(`0.9 + random/5.0`, so 0.9–1.1, not a five-fold wobble) and the cauldron bubble being
louder than documented (`0.8 + random*0.5`, not `0.5 + random*0.4`).
**0.1.51 closed six more rows**, and one of them turned out to be a false claim rather
than a gap: the thirst-damage gate was **already the reference's**
(`hp > 10 || HARD || (hp > 1 && NORMAL)`, confirmed against `ThirstManager.update`
offsets 76-124) and this file had it recorded as inverted, so it is now a pure
`shouldDamage(health, difficulty)` with every branch pinned. Also closed: the flask sip
plays `empty_flask` on **every** sip, `thirst.useHydrationCorpus` now actually gates the
corpus, the HUD droplet bob uses the reference's per-droplet cadence with the
`buffered >= 4.0` predicate riding out on the sync payload, the bare-hand sip uses the
real `dehydration:water_sip`, and the campfire cauldron's bucket pair is a
water-bucket-in/empty-bucket-out fill straight to LEVEL 4 and its exact reverse - **not**
"a bucket is three bottles".

Three 🟡 are left: two are marked **not reproducible on 26.2** (the shared campfire
counter and the flask reset recipes, both for the same structural reason - 26.2 gives
each slot its own arrays and vanilla's recipe manager simplifies away a one-ingredient
recipe whose result is its own ingredient), and one is the purified bucket's cosmetic
extras (client-side `LARGE_SMOKE` burst and the randomised fill pitch - note the
reference's EMPTYING sound is fixed 1.0/1.0; it is the FILL pitch that is randomised,
which this file also had backwards). The one ❌ left is `thirst_preview`, the droplet
tooltips that show how much an item quenches. Earlier releases closed the rest of the ❌ column: 0.1.43
the campfire cauldron's potion pour, the copper cauldron's one-step potion fill, the
bubble sound while it boils, and the rule that a block placed above the cauldron removes
it; 0.1.45 then ported rain extinguishing campfires, which is AdditionZ's job rather than
Dehydration's, so the whole port queue's most survival-critical key is closed; 0.1.48
purified water displacing vanilla water and the purified bucket filling a cauldron. The
last `❌ unverified` row was closed on 0.1.35 (the hydration corpus resolves 105 of
its 115 catalogued ids in a real world, and the gametest now pins that), and
the Alchemy 2 cauldron gates were loaded on 0.1.39.

**0.1.47 corrected three rows this audit had wrong**, all found by re-reading
the bytecode rather than trusting the prose:

- the water bowl and the milk bucket roll their thirst effect at **amp 0**,
  not amp 2. The `iconst_2` in `WaterBowlItem` (offset 159) and
  `MilkBucketItemMixin` (offset 151) is the `idiv` **divisor** for the
  duration; the amplifier is the next instruction (`iconst_0` at offsets 161
  and 153). So both were already at parity and the 🟡 rows were spurious.
- the **purified** bowl does roll thirst in Aged - `ItemInit` constructs
  `water_bowl` at offset 300 and `purified_water_bowl` at offset 328, both
  with `iconst_1` for `hasThirstChance`. We had made ours safe; 0.1.47 makes
  both bowls roll, which is what the reference does.

### 8.1 The campfire water loop

| Aged behaviour | Hearthwind | Status | Evidence |
|---|---|---|---|
| Bottle accepted on **any** campfire, no recipe needed, no `LIT` gate | `CampfireBlockMixin` injects `CampfireBlock.useItemOn` at `HEAD` with no `LIT` read; `placeWaterBottle` takes the first empty slot | ✅ | `CampfireBlockMixin.java:32-47`; `CampfirePurification.java:57-78`; test `waterBottleUseOnCampfire` |
| Cook time 1000 ticks, hard-coded | `BOIL_TIME = 1000`, separate from config | ✅ | `CampfirePurification.java:35`; test `waterBottleBoilTimeMatchesParity` |
| Completion is detected one tick early so the un-purified bottle can never drop | `progress[slot] + 1 >= time[slot]` at `cookTick` `HEAD`, slot cleared before vanilla's drop | ✅ | `CampfirePurification.java:97-110`; `CampfireBlockEntityMixin.java:35-41`; tests `waterBottleNeverSticksOnCampfire`, `waterBottlePurifiesThroughRealCookTick` (asserts exactly `BOIL_TIME` ticks) |
| `INTERACT_WITH_CAMPFIRE` awarded on placement | `player.awardStat(Stats.INTERACT_WITH_CAMPFIRE)` | ✅ | `CampfireBlockMixin.java:42` |
| Result is a fresh `minecraft:potion`, no NTC/name carryover | `PotionContents.createItemStack(Items.POTION, PURIFIED_POTION)` | ✅ | `CampfirePurification.java:49-51`; test `purifiedPotionRegisters` |
| 4 slots per campfire | iterates `items.size()` = 4 | ✅ | `CampfirePurification.java:64,91`; `CampfireBlockEntityAccessor.java:13-20` |
| 2nd bottle resets the shared counter (1.20.1 has one `cookTime` int) | impossible to reproduce: 26.2 has per-slot `cookingProgress[]`/`cookingTime[]` arrays, so each bottle keeps its own timer | 🟡 **not reproducible on 26.2** - vanilla gives each campfire slot its own `cookingProgress[]`/`cookingTime[]`, so each bottle keeps its own timer. The reference has a single `cookTime` int in 1.20.1. Keeping the 1.20.1 behaviour would need a mixin that re-couples the slots, for no gameplay gain. | forced by vanilla; `CampfireBlockEntity.java:46-47,66-67` |
| Bottle pops at the **block corner** with vanilla `ItemScatterer` physics | pops at **0.625 from the centre** on a face-per-slot direction with vanilla face-pop velocity | ✅ 0.1.49: spawned at the raw block corner with 1.20.1 `ItemScatterer` physics (26.2 dropped `ItemScatterer`, so `CampfirePurification.spawnAtCorner` mirrors its body) | `CampfirePurification.spawnAtCorner`; tests `waterBottleBoilsOnCampfire`, `aBarkLitCampfireBoilsOverRealServerTicks` |
| No sound added on completion | an amethyst chime (BLOCKS 0.6 / 1.4) plays when the bottle pops | ✅ 0.1.49: none - the amethyst chime we added in 0.1.33 is gone | `CampfirePurification.tickPurification` |
| No custom particle | white smoke while boiling plus a 6 `WHITE_SMOKE` + 4 `BUBBLE` burst at the finish, and vanilla's 4 `SMOKE` is redirected away from water slots | ✅ 0.1.49: none - the white steam and the 6 `WHITE_SMOKE` + 4 `BUBBLE` burst we added in 0.1.33 are gone | `CampfirePurification.tickPurification` |
| Progress **freezes** when the fire goes out (1.20.1) | 26.2 vanilla `cooldownTick` subtracts **2 per tick** from `cookingProgress` while unlit, so a long boil loses ground | ✅ fixed 0.1.33: `CampfireBlockEntityMixin.hearthwind$freezeBoilOnDarkFire` (`cooldownTick` HEAD, cancellable) leaves a slot holding a water potion untouched and decays non-water slots exactly as vanilla does | `CampfireBlockEntityMixin`; tests `aBoilFreezesWhileTheFireIsOutAndResumesWhenRelit` |
| No lit check on placement - a bottle is accepted on a dark fire and simply never finishes | same (bottle accepted), plus a chat hint telling the player to light the fire | ✅ 0.1.49: same, and the 0.1.33 chat hint telling the player to light the fire is gone too | `mixin/CampfireBlockMixin`; test `aDarkCampfireKeepsTheBottleAndNeverBoils` |
| A bottle placed on a fire lit *with bark* after the campfire was placed unlit | same (Aged campfires also place unlit) | ✅ test replays the exact clicks: place dark, light with `earlystage:oak_bark`, then right-click the bottle | `aBarkLitCampfireBoilsOverRealServerTicks` |
| Progress survives relight | `cookingProgress`/`cookingTime` are persisted NBT int arrays in 26.2 | ✅ | `CampfireBlockEntity.java:130-152` |
| `water_boiling_time` is **never** used by the bottle path | only `CampfireCauldronBlockEntity` reads `hydration.waterBoilingTime` | ✅ | `CampfireCauldronBlockEntity.java:66` |
| Placing a block destroys a `campfire_cauldron` above it | none - a block above the cauldron removes it | ✅ 0.1.43 | no mixin for `Block.onPlace`; `CampfireCauldronBlock.java:115-117` **Now:** `CampfireCauldronBlock.updateShape` returns air when a non-air block is placed directly above, matching Dehydration's `Block.onPlace`. 26.x spells that override with the 8-arg `BlockBehaviour.updateShape` signature. |

### 8.2 Items, blocks and fluids

| Aged behaviour | Hearthwind | Status | Evidence |
|---|---|---|---|
| `dehydration:purified_water` potion registered | `new Potion("purified_water")` in `BuiltInRegistries.POTION` | ✅ | `PurifiedWater.java:111-112`; test `purifiedPotionRegisters` |
| `dehydration:hydration` potion (900 t, `HYDRATION` effect) | shipped 0.1.35: `new Potion("hydration", new MobEffectInstance(HydrationMobEffect.HOLDER, 900))` | ✅ | `PurifiedWater.registerAll`; test `hydrationBrewingMixesMatchAged` asserts the potion carries the effect holder |
| `dehydration:hydration_effect` (+1 per 50 ticks) | shipped 0.1.35 as `HydrationMobEffect`: BENEFICIAL, `0x2EC6B6`, `every = 50 >> amplifier` and `duration % every == 0`, grants `amp + 1` thirst | ✅ | `HydrationMobEffect.java`; test `aHydrationDoseRaisesThirstOverRealTicks` measures the cadence over 220 real server ticks |
| `dehydration:thirst_effect`, HARMFUL, every tick, `0x2EC6B6` | `ThirstMobEffect`, HARMFUL, every tick, colour **`0x3A62C4`** | ✅ 0.1.49: colour `0x2EC6B6`, the reference's own icon colour | `ThirstMobEffect`; test `thirstEffectUsesTheReferenceIconColour` |
| `dehydration:thirst` damage type, bypasses armour + effects, "died of thirst" | same id, same two tags | ✅ | `data/dehydration/damage_type/thirst.json`, `data/minecraft/tags/damage_type/bypasses_{armor,effects}.json` |
| `purified_water` still + flowing fluid, block, bucket; splash `UNDERWATER`, drip `DRIPPING_WATER` | all four registered; splash/drip inherited from `WaterFluid` | ✅ | `PurifiedWater.java:50-113`; test `purifiedWaterRegisters` |
| purified water in `minecraft:tags/fluids/water` | shipped | ✅ | `data/minecraft/tags/fluid/water.json` |
| purified water replaces vanilla water when flowing in (`WaterFluidMixin.spreadTo` override) | ported 0.1.48, but it needed THREE fixes, not one: (a) `WaterFluid#canBeReplacedWith` is `DOWN && !other.is(FluidTags.WATER)` and purified water IS in that tag, so `WaterFluidPurifiedMixin` makes vanilla water accept it (the reference's `matchesTypeMixin`); (b) `StillFluid.spreadTo` writes the cell directly; (c) **26.2's `WaterFluid#createLegacyBlock` hardcodes `Blocks.WATER`**, so our fluid had been writing VANILLA water into the world all along and purified water could never exist as a block - pouring a purified bucket handed you normal water | ✅ 0.1.48 | `PurifiedWater.StillFluid.{createLegacyBlock,spreadTo}` + `mixin/WaterFluidPurifiedMixin`; test `purifiedWaterDisplacesVanillaWaterWhenItFlowsIn` pins both reference contracts |
| purified bucket: 8 `LARGE_SMOKE` particles, randomised empty sound, recursion into the block below, `maxDamage(BUCKET)+1` | plain vanilla `BucketItem` with `craftRemainder(Items.BUCKET)`; no particles, no recursion, no `maxDamage` | 🟡 ours is a plain vanilla `BucketItem`; still missing the reference's 8 `LARGE_SMOKE` particles, its randomised empty sound, its recursion into the block below and `maxDamage(BUCKET)+1` | `PurifiedWater.java:108-110` |
| purified water bucket recipe: smelting a water bucket, 1.0 exp, 600 t | identical, and asserted to parse | ✅ | `data/dehydration/recipe/purified_water_bucket.json`; test `purifiedBucketSmeltingRecipeParses` |
| purified bucket fills a **vanilla** cauldron (`FILL_WITH_WATER`) | ported 0.1.48: our row on `CauldronInteractions.EMPTY`, reached through a `CauldronInteraction.Dispatcher#put` invoker (26.2 made `put` package-private) | ✅ 0.1.48 | `PurifiedWater.registerCauldron()` + `mixin/CauldronDispatcherAccessor`; test `purifiedBucketFillsAVanillaCauldron` drives the real `useItemOn` |
| a purified bucket gives no thirst | gives no thirst (it is not a consumable at all) | ✅ | `ThirstHelper.hydratePlayer` has no bucket branch (`ThirstHelper.java:43-82`) |
| `water_bowl` / `purified_water_bowl` registered in the `dehydration` namespace | yes, both `WaterBowlItem` with the 32-tick DRINK consumable | ✅ | `HydrationItems.java:43-50`; test `hydrationContentRegisters` |
| **both** bowls roll 40 % thirst, including the purified one (`hasThirstChance = true` twice) | `water_bowl` rolls, `purified_water_bowl` never does (`false` passed) | ✅ since 0.1.47 (this row was stale): both bowls pass `hasThirstChance = true` | `hydration/HydrationItems.java`; test `dirtyWaterBowlRollsThirst` |
| bowl thirst roll: `nextFloat() >= 0.4` → 60 %, **amp 0**, 150 t, on BOTH bowls | same comparison, same 150 t, same amp 0, and both bowls roll (0.1.47) | ✅ 0.1.47 | `hydration/WaterBowlItem.java:42-44`; `hydration/HydrationItems.java:44-51` |
| drinking a 1.3.6 bowl consumes it and returns **nothing** (no craft remainder) | returns a plain `minecraft:bowl`, plus a `pour_*_water_bowl` shapeless recipe | ✅ 0.1.49: returns `ItemStack.EMPTY` for a player, and our `pour_*_water_bowl` recipes are removed | `hydration/WaterBowlItem.finishUsingItem`; test `waterBowlDrinkQuenchesThree` |
| 5 flasks, capacity 2…6, netherite `fireResistant`, `stacksTo(1)` | identical, ids in the `dehydration` namespace | ✅ | `FlaskItems.java:34-48`; test `flaskTiersHaveIncreasingCapacity` |
| flask sip: `+4` thirst, `empty_flask` sound, `USE_CAULDRON` + `PLAYER_USE_ITEM` | `+4` (`cfg.quench`), `dehydration:empty_flask` on NEUTRAL at 1.0/1.0 on **every** sip | ✅ 0.1.50: the sound now fires on every sip, not only when the flask empties. `USE_CAULDRON` / `PLAYER_USE_ITEM` are the mod's own stats and have no player-visible effect, so nothing is granted | `LeatherFlaskItem.java`; bytecode `LeatherFlask#finishUsing` offset 117 (unconditional) |
| flask dirty roll: `==2` → `<= 0.3` amp 1, `==1` → `<= 0.15` amp 0, 200 t | identical, with the same `<=` direction; creative is exempt | ✅ | `FlaskItems.java:84-102`; test `flaskDrinkDecrementsFillAndAddsHydration` |
| flask fill from open water: hold 20 ticks, write dirty, play `fill_flask`, **destroy the source**, `IS_RIVER` forces dirty | fills instantly (no hold timer), **does not destroy the source**, no custom sound | ✅ 0.1.49: 20-tick hold, the source block is destroyed, any `FluidTags.WATER` source fills (purified included) | `LeatherFlaskItem.tryWaterInteraction` + `FILL_HOLD_TICKS`; test `flaskFillNeedsTheReferenceHold` |
| flask fill from a vanilla water cauldron: one unit per use, always dirty; sneak pours one back | identical | ✅ | `LeatherFlaskItem.java:162-183`; test `flaskCauldronFillIsDirtyAndOneUnit` |
| `IS_RIVER` fill forces **dirty** (`level = 2, purified = false`) | `IS_RIVER` fill forces **purified** (quality 0); non-river open water is dirty | ✅ 0.1.49: a river fill is DIRTY, not purified - our old code had it exactly backwards | `LeatherFlaskItem.openWaterQuality`; test `flaskOpenWaterQualityMatchesAged` |
| flask fill accepts any `FluidTags.WATER`, i.e. purified water too | requires `Fluids.WATER` exactly, so purified water will not fill a flask | ✅ 0.1.49: `fluid.is(FluidTags.WATER)`, so purified water fills a flask | `LeatherFlaskItem.tryWaterInteraction` |
| flask tooltip `Fill Level n/cap` + colour-coded Dirty/Impurified/Purified | `Uses: n/cap` + grey `Purified/Dirty/Impurified water`, hard-coded English | ✅ 0.1.49: `Fill Level n/cap` (or `Fill Capacity n` when empty) plus the reference's colour-coded `Dirty/Dirty Water` lines | `LeatherFlaskItem.appendHoverText`; test `flaskTooltipMatchesTheReference` |
| `thirst_preview` droplet tooltips on flasks, bowls, potions, foods | no `getTooltipData` override anywhere in the tree | ❌ | `rg "getTooltipData\|droplet" custom-mods/` → only HUD droplet art |
| `pour_<tier>_leather_flask` shapeless reset recipes (5) | not shipped; emptying is a sneak-use instead | 🟡 **not reproducible on 26.2** - a one-ingredient recipe whose result is its own ingredient is simplified away by vanilla's recipe manager, so the reset trick cannot be a recipe here. The reference's OTHER way to empty a flask (sneak-use) is implemented and is what a player has. | `data/dehydration/recipe/` has no `pour_*_leather_flask` |

### 8.3 Thirst maths

| Aged behaviour | Hearthwind | Status | Evidence |
|---|---|---|---|
| `thirstLevel` 0…20, `dehydration` 0…40, 4.0 exhaustion per level, strictly `> 4.0` | identical, clamps included, on a persistent `dehydration:thirst_state` attachment | ✅ | `HearthwindSurvivalThirst.java:40-42,54-72,178-209`; test `thirstOnlyDrainsThroughExhaustion` |
| `hydrating_factor` 2.0 in Aged; exhaustion charges `exhaustion / 2.0` | `thirst.hydratingFactor = 2.0`, injected after `FoodData.addExhaustion` so creative is exempt | ✅ | `HearthwindSurvivalConfig.java:89`; `PlayerExhaustionMixin.java:22-37` |
| thirst damage 1.0 every 90 ticks at level 0, gated on `hp > 10 \|\| HARD \|\| (hp > 1 && NORMAL)` | 1.0 every 90 ticks, gated on exactly that | ✅ 0.1.51 **the row was wrong about the reference**: `ThirstManager.update` offsets 76-124 are `health > 10 → damage`, `difficulty == HARD → damage`, else `health > 1 && NORMAL → damage`. Our code already matched it; the old row claimed the gate was inverted and called ours a deliberate rewrite. The gate is now a pure `HearthwindSurvivalThirst.shouldDamage(health, difficulty)` so `thirstDamageGateMatchesTheReference` can pin all seven branches | `HearthwindSurvivalThirst.java:106-112`; `Difficulty` field names `field_5801` PEACEFUL, `field_5802` NORMAL, `field_5807` HARD |
| Peaceful never drains the level | identical | ✅ | `HearthwindSurvivalThirst.java:186-190` |
| Peaceful + `naturalRegeneration` → **+1 thirst every 10 ticks**, and a second `update()` on that path | identical, including the double update; gated on `tickCount % 10` (upstream `Entity.age % 10`) | ✅ | `HearthwindSurvivalThirst.java:166-172`; test `peacefulRegeneratesThirst` |
| `special_effects` off in Aged → HASTE at 2 and MINING_FATIGUE II at 0 are inert | never implemented, so equally inert | ✅ | `HearthwindSurvivalThirst.java:178-209` has no effect branch |
| `sleep_thirst_consumption` 4, `sleep_hunger_consumption` 2, gated on `sleepTimer >= 100` | identical, injected at `ServerPlayer.stopSleepInBed` `HEAD` | ✅ | `PlayerSleepMixin.java:20-34` |
| `harder_nether` off in Aged; when on, scales exhaustion by `nether_factor` (1.3) | off by default; when on, scales by `thirst.netherFactor` (**2.0**) on `Level.NETHER` | ✅ 0.1.49: `netherFactor` is 1.3, the reference default (we had 2.0) | `HearthwindSurvivalConfig.Thirst`; test `thirstConfigMatchesTheReferenceDefaults` |
| `potion_bad_thirst_chance` 0.15 → 85 % risk, `nextFloat() >= chance` | identical, amp 0, 300 t | ✅ | `ThirstHelper.java:47-58`; test `badPotionListMatchesAged` |
| the 14-potion bad list incl. `Potions.WATER` | the same 14, verified by test; `dehydration:purified_water` is not bad | ✅ | `ThirstHelper.java:26-41`; test `badPotionListMatchesAged` |
| `potion_thirst_quench` 2 fallback for uncatalogued potions | `thirst.potionThirstQuench = 2.0` fallback | ✅ | `ThirstHelper.java:56-58` |
| `milk_thirst_quench` 8, `milk_thirst_chance` 0.4 (→ 60 %), 150 t, **amp 0** | quench 8, chance 0.4, the same `>=` comparison and the same amp 0 | ✅ | `ThirstHelper.java:60-62`; test `milkDrinkQuenchesEightAndRolls` |
| `honey_quench` 1 fallback | identical | ✅ | `ThirstHelper.java:75-77` |
| resolution order tag → template → fallback | template → fallback | ✅ | the six `dehydration:hydrating_*` tags ship empty and Aged never fills them, so the tag branch is inert; templates alone is equivalent |
| `replace: true` clears the tier; lowest tier wins on duplicates | identical, iterating tiers in ascending order | ✅ | `HydrationCorpus.java:63-70,93-95`; test `hydrationCorpusTiersMatchCatalogue` |
| Aged's `aged_items.json`: 12 tiers, 115 items, `replace: true` on all | shipped **byte-identical** in both the world datapack and the mod's bundled fallback | ✅ | `conversion/datapacks/hearthwind/data/dehydration/hydration_items/aged_items.json` == `.tmp/aged-3.1.2/.../aged_items.json`; 115 items over 12 tiers |
| how many of the 115 ids resolve in a Hearthwind world | **105 of 115 resolve, across all 12 tiers**, measured off a real boot (the corpus logs `hydration: 105 catalogued items across 12 tiers`). The 10 that do not belong to the lets-do food mods Aged ships and we have not ported | ✅ measured 0.1.35 | `HearthwindSurvivalGameTests.hydrationCorpusLoadsCataloguedItems` now pins `itemCount() == 105` and `tierCount() == 12` instead of `>= 10`; `HydrationCorpus.java:97-103` skips unresolvable ids silently, which is why only a boot could settle it |
| `thirst.useHydrationCorpus` toggles the corpus | `ThirstHelper.hydratePlayer` now reads it: with it off, `quench` starts at 0 and every source falls back to its own scalar (`potion_thirst_quench`, `milk_thirst_quench`, ...) | ✅ 0.1.51 - the flag only reached `HydrationCorpus.hydrateOnConsume`, which nothing called, so it did nothing at all | `ThirstHelper.java:45-51` |
| HUD: 10 droplets 9×9 pitch 8 at `width/2 + 91`, `height − 49`, green while thirst is active, hidden in creative/spectator | same geometry and colours; the bob is now the reference's: droplet **i** nudges -1..+1 every `i*3+1` ticks while the dehydration buffer is ≥ 4.0 and every `i*8+3` while it is below, and the `buffered >= 4.0` predicate rides out on the thirst sync payload | ✅ 0.1.51 - we keyed the cadence off the thirst LEVEL and OR'd both periods, so every droplet moved in lockstep. Upstream's `dehydration -= 4.0` **inside the render call is deliberately not reproduced**: it would let the render thread eat the player's thirst | `ThirstHud.java:99-115`; bytecode `ThirstHudRender.renderThirstHud` offsets 269-358 |

### 8.4 Sipping, vessels and containers

| Aged behaviour | Hearthwind | Status | Evidence |
|---|---|---|---|
| sneak + empty hand + 1.5-block raycast at a `FluidTags.WATER` block | sneak + empty hand, `blockInteractionRange()` raycast, plus water-above / waterlogged / `WATER_CAULDRON` / own-block fallbacks so a submerged player can sip | ✅ 0.1.49: `player.pick(1.5, 0.0, false)` and nothing else - the water-above, waterlogged, cauldron and own-block fallbacks are gone | `BareHandDrinkHandler.findWater` + `SIP_REACH`; test `bareHandHoldCompletesSipAndConsumesSource` |
| 21-tick (~1.05 s) hold before the sip resolves | `time > 20` on the same counter | ✅ | `BareHandDrinkHandler.java:77-88`; test `bareHandDrinkingWhileCrouchingAddsHydration` |
| no sip at 20/20 | identical | ✅ | `BareHandDrinkHandler.java:65-67` |
| flowing water refused unless `allow_non_flowing_water_sip` | identical, config `bareHand.allowNonFlowingWaterSip = false` | ✅ | `BareHandDrinkHandler.java:71-74`; test `bareHandFlowingWaterRefusedByDefault` |
| `+water_souce_quench` = **+1** | `Math.max(1, cfg.waterSourceQuench)` = 1 | ✅ | `BareHandDrinkHandler.java:133` |
| sip roll `<= water_sip_thirst_chance` = 0.5, amp 1, 300 t, halved in `IS_RIVER` | identical, `chance = 0` when the fluid is in `dehydration:purified_water` | ✅ | `BareHandDrinkHandler.java:138-148`; test `purifiedSipNeverThirsts` |
| the source block is consumed | identical (un-waterlog, else `Blocks.AIR`), gated on `consumeStillSource` | ✅ | `BareHandDrinkHandler.java:150-157` |
| sounds: `ENTITY_GENERIC_DRINK` every 3rd tick at 0.5, then `dehydration:water_sip` at 1.0 on PLAYERS | vanilla `GENERIC_DRINK` at 0.5 every 3rd tick (kept), and `dehydration:water_sip` at 1.0 on completion | ✅ 0.1.50. The row also had the pitch wrong for years: the reference is `0.9 + rand.nextFloat() / 5.0`, an **fdiv**, so 0.9-1.1 - not "1.0 + random × 5.0" | `BareHandDrinkHandler.java`; bytecode `EventInit` offset 571 |
| sneak + `Items.BOWL` at a **still** source → `water_bowl` / `purified_water_bowl`, source consumed, `ITEM_BUCKET_FILL` | identical, registered on both the item and block use events, source consumed, `BUCKET_FILL` at 1.0/1.0 | ✅ | `HydrationBowlHandler.java:45-82`; test `bowlFillsFromStillWaterAndPurifiedTag` |
| copper cauldrons: rain 10 % / snow 15 % from empty, straight to purified LEVEL 1 | identical, plus `FLUID_PLACE` | ✅ | `CopperCauldronBlock.java:37-59`; `HearthwindSurvivalConfig.java:203-206` |
| copper cauldrons: LEVEL 1…3, fluid height `(6 + LEVEL×3)/16`, comparator = LEVEL | identical | ✅ | `CopperLeveledCauldronBlock.java:30,56-68,96-99` |
| copper cauldrons: dripstone only for water + rain predicate; the empty one converts to purified with level event 1047 | identical | ✅ | `CopperLeveledCauldronBlock.java:60-63,106-112`; `CopperCauldronBlock.java:61-73` |
| copper cauldrons: a burning entity inside decrements the level | identical via `InsideBlockEffectApplier` | ✅ | `CopperLeveledCauldronBlock.java:70-83,114-122` |
| copper cauldron: `Items.POTION` fill jumps straight to LEVEL 3 with `ITEM_BUCKET_EMPTY` | none - a bottle now fills it in one step | ✅ 0.1.43 | `CopperCauldronBehavior.java:51-65` **Now:** `EMPTY_COPPER_CAULDRON_BEHAVIOR.put(Items.POTION, ...)` fills straight to LEVEL 3, leaves a bowl, plays `ITEM_BUCKET_EMPTY` and fires `FLUID_PLACE`. Covered by `copperCauldronPotionFillJumpsToLevelThree`. |
| copper cauldron: glass bottle out drops the level by 1 and returns a dirty or purified bottle | handled by `CopperCauldronFluidStorage` + `BowlFluidStorage`/purified-bottle storage, one bottle per transfer | ✅ equivalent behaviour, reached through `CopperCauldronFluidStorage`: one bottle per transfer, level -1, dirty or purified bottle returned | `HydrationStorages.java:40-48`; `CopperCauldronFluidStorage.java:88-107` |
| copper cauldron: bucket round trip (`copperCauldronBucketRoundTrip`) | passes: a water bucket fills to LEVEL 3 and an empty bucket drains it back to empty | ✅ | test `copperCauldronBucketRoundTrip`; `CopperCauldronFluidStorage.java:54-86` |
| campfire cauldron: only above `#minecraft:campfires`, LEVEL 0…4, comparator = LEVEL, not pathfindable | identical, plus a modelled stand/legs shape that spans the block below | ✅ 0.1.49: no stand/legs `VoxelShape` reaching 15 blocks down - vanilla's full cube applies, as in the reference | `hydration/CampfireCauldronBlock.getShape` |
| campfire cauldron: `water_boiling_time` 100 t, timer freezes when the fire goes out | identical (`hydration.waterBoilingTime = 100`), no decrement branch | ✅ | `CampfireCauldronBlockEntity.java:54-72`; test `campfireCauldronBoilsAtAgedSpeed` |
| campfire cauldron: `ticker` is not persisted, only `isBoiled` | identical | ✅ | `CampfireCauldronBlockEntity.java:38-42` |
| campfire cauldron: fresh water re-arms `isBoiled = false; ticker = 0` | identical via `onFillingCauldron` from the storage's `onFinalCommit` | ✅ | `CampfireCauldronBlockEntity.java:74-79`; `CampfireCauldronFluidStorage.java:123-131` |
| campfire cauldron: a **water bucket** held against any LEVEL < 4 hands back an EMPTY bucket, re-arms the boil and fills straight to LEVEL 4; an **empty bucket** against a full LEVEL 4 hands back a water bucket and drops it to 0 | `CampfireCauldronBlock.pourBucket` does both, ahead of the fluid-transfer path | ✅ 0.1.51 - a bucket is NOT three bottles here. Both rows are gated on `!isClient`, the fill plays `ITEM_BUCKET_EMPTY` 1.0/1.0 and the drain `ITEM_BUCKET_FILL` 1.0/1.0, and a stack of buckets shrinks one rather than being swapped whole | `CampfireCauldronBlock.java`; bytecode `CampfireCauldronBlock#method_9534` offsets 53-136 and 137-271; test `campfireCauldronBucketFillsToFourAndDrainsBack` |
| campfire cauldron: pouring a potion gives a bowl, LEVEL+1, plain water re-arms the boil | none - pouring a bottle works | ✅ 0.1.43 | `CampfireCauldronBlock.java:120-130`; `HydrationStorages.java:40-48` **Now:** `CampfireCauldronBlock.pourPotion` gives LEVEL+1 and a bowl, and plain water calls `onFillingCauldron()` so it must boil again. Covered by `campfireCauldronPotionPourGivesABowlAndReArmsTheBoil`. |
| campfire cauldron: rain fills it at 20 % **only** when sky-lit, biome ≥ 0.15 °C and not snowing | rain at 20 % with `isRainingAt`, no sky-light and no temperature test | ✅ 0.1.49: rain also needs `canSeeSky` and a biome at 0.15 °C or warmer | `hydration/CampfireCauldronBlock.handlePrecipitation` |
| campfire cauldron: `cauldron_bubble` sound every 1-in-12 random display tick | none - the cauldron bubbles while it boils | ✅ 0.1.43 | `CampfireCauldronBlock.java` has no `randomDisplayTick` **Now:** a 1-in-12 bubble pop, volume `0.5 + rand*0.4`, pitch 1.0, using vanilla's `BUBBLE_COLUMN_BUBBLE_POP` because this pack does not redistribute another pack's audio. It plays on the SERVER tick, not the reference's client-side `randomDisplayTick`, so everyone near the fire hears it rather than only the player looking at it - the one deliberate difference in this row. |
| campfire cauldron recipe: stick + `minecraft:chain` + copper cauldron | remapped to `minecraft:iron_chain` for 26.2, pattern unchanged | ✅ | `data/dehydration/recipe/campfire_cauldron.json`; test `dehydrationRecipesLoad` |
| bamboo pump: 1 slot, `pump_cooldown` 1200, `pump_requires_water` scan of 50 blocks above | `pumpCooldown = 1200`, `pumpRequiresWater = false`; the scan covers blocks 10…59 above instead of 0…49 | ✅ 0.1.49: the scan is blocks 0…49 above, counting the pump's own block | `hydration/BambooPumpBlock.hasWaterAbove` |
| bamboo pump: **one** pump converts the container and the hand is emptied | 4 pumps for a bucket, 1 for a bottle, +2 units for a flask; the container is stored and handed back later; a pending cooldown rides on the item so it survives break/replace | ✅ 0.1.49: one press converts any container, a flask keeps its fill level and becomes purified, and the cooldown rides on the block entity only | `hydration/BambooPumpBlockEntity.updateInventory`; test `bambooPumpPurifiesItsContainer` |
| bamboo pump: cooldown messages `Pump Cooldown: <n>s` and `Pump found no water` | same two strings, sent as action-bar text | ✅ | `BambooPumpBlock.java:153-159`; `assets/dehydration/lang/en_us.json` |
| the four custom sounds (`fill_flask`, `water_sip`, `empty_flask`, `cauldron_bubble`) | all four ship, with Dehydration's own `.ogg` files and `sounds.json` | ✅ 0.1.50 | `DehydrationSounds` registers `dehydration:{fill_flask,water_sip,empty_flask,cauldron_bubble}` and the nine reference `.ogg` files are copied verbatim under `assets/dehydration/sounds/` with the reference's own `sounds.json` (GPL-3.0, see `ATTRIBUTION.md`). Wired at the reference's exact moments and values read from the bytecode: fill = `fill_flask` on BLOCKS 1.0/1.0 (`LeatherFlask#use` offset 630); sneak-drain = `empty_flask` on BLOCKS 1.0/1.0 (offset 468); **every** sip = `empty_flask` on NEUTRAL 1.0/1.0 (`finishUsing` offset 117 — we used to stay silent unless the flask ran dry); bare-hand sip completion = `water_sip` on PLAYERS 1.0 with pitch `0.9 + random/5.0` (`EventInit` offset 571 — note the `fdiv`, so the spread is 0.9–1.1, NOT the five-fold wobble an earlier reading of this file claimed); boil = `cauldron_bubble` on BLOCKS at `0.5 + random*0.4 + 0.8` = 0.8–1.3, louder than the 0.5–0.9 we had guessed. Two tests: `dehydrationSoundsUseTheReferenceIds` and `dehydrationSoundFilesShipInOurJar` |
| brewing: water + charcoal → purified, water + kelp → purified, purified + ghast tear → hydration | all three shipped 0.1.35, plus the `hydration` potion and its effect; the effect ticks once every `50 >> amplifier` ticks for `amp + 1` thirst (Aged's cadence) and a dose is 900 ticks | ✅ | `PurifiedWater.registerBrewing()` registers them on `FabricPotionBrewingBuilder.BUILD` (26.2 has a real hook where the reference needed a mixin on `registerDefaults`); `HydrationMobEffect`; gametests `hydrationBrewingMixesMatchAged` (brews all three and checks vanilla's nether wart still wins) and `aHydrationDoseWorthsAboutEighteenThirst` (ticks a full dose through `MobEffectInstance.tickServer`; no mock player ticks in a gametest, so that is vanilla's own cadence path) |
| `bottle_consumes_source_block = false` → a plain glass bottle leaves the source intact | 26.2 vanilla already leaves it intact; no removal hook | ✅ | `HydrationStorages.java:40-46` |
| `#minecraft:tags/blocks/cauldrons` lists all five dehydration cauldrons | shipped identically | ✅ | `data/minecraft/tags/block/cauldrons.json` |

### 8.5 Campfire-adjacent rules owned by other Aged mods

| Aged behaviour | Hearthwind | Status | Evidence |
|---|---|---|---|
| bark campfire recipe: `C: #aged:campfire_ingredient`, `L: #minecraft:logs`, `S: stick`, `[" S ","SCS","LLL"]` | migrated byte-for-byte | ✅ | `conversion/datapacks/hearthwind/data/minecraft/recipe/campfire.json` |
| `aged:campfire_ingredient` = `#minecraft:coals` + `#earlystage:bark_items` | migrated; the earlystage tag is `required: false` | ✅ | `conversion/datapacks/hearthwind/data/aged/tags/item/campfire_ingredient.json` |
| `aged:recipe/sticks_from_shapeless_sapling` - any `#minecraft:saplings` -> 2 `minecraft:stick` (shapeless, group "sticks") | **already shipped** - the 0.1.33 audit first logged this as missing, but the recipe has been in the pack since `e497a3027`; 0.1.33 only added the missing trailing newline to the file | ✅ (pre-existing) | `conversion/datapacks/hearthwind/data/aged/recipe/sticks_from_shapeless_sapling.json` |
| campfire loot: silk touch returns the campfire, otherwise `minecraft:oak_log` ×1 with `survives_explosion` | migrated, with the 1.20.1 enchantment predicate rewritten to the 26.2 component map | ✅ | `conversion/datapacks/hearthwind/data/minecraft/loot_table/blocks/campfire.json` |
| rain extinguishes a campfire: 1/60 per second, but only from the 61st sample, and the counter keeps its credit across a dry spell (AdditionZ `campfire_rain_extinguish: 60`) | ported 0.1.45: the rain counter lives on the campfire block entity as NBT `"RainBurnTime"` and is sampled once a second while the sky is lit | ✅ 0.1.45 | `CampfireRainMixin` on `CampfireBlockEntity` injects at the HEAD of `cookTick` (the lit server ticker in 26.x - `litServerTick` no longer exists) and the HEAD of `cooldownTick`, which our own HEAD-cancellable freeze-boil would otherwise hide a TAIL inject behind; the counter resets on the unlit tick, matching the reference. `AdditionZParity.rainSample(rainBurnTime, limit, roll)` holds the whole rule as a pure function, and `additionZRainOnlyPutsAOutAfterTheFirstSixtySamples` pins it |
| Alchemy 2 gates **crafting** the copper and campfire cauldrons | data migrated and enforced (`levelz/crafting/alchemy_02.json` uses the `item` field, which the loader reads, and `CraftingGateMixin` clears the result) | ✅ | `conversion/datapacks/hearthwind/data/levelz/crafting/alchemy_02.json`; `SkillGates.java:127,181-193`; `CraftingGateMixin.java:31-49` |
| Alchemy 2 gates **breaking/placing** both cauldrons (`levelz/block/*_custom_*.json`) | ✅ **0.1.39**: loaded. All 55 placeholder files gate their real block now, not just the two cauldrons - `SkillGates.loadCategory` falls back to the `object` field when the `block` id does not resolve, which is exactly when levelz used a placeholder. That lifted the block-use gate count from 16 to 68 in the full pack | ✅ 0.1.39 | `conversion/datapacks/hearthwind/data/levelz/block/alchemy_02_custom_{campfire,copper}_cauldron.json`; the fix is in `SkillGates.loadCategory`; `HearthwindSkillsGameTests.skillGatesLoadAndResolve` asserts all four vanilla objects by id (lodestone 18 agility, respawn anchor 25 agility, jukebox 10 luck, fletching table 10 archery) |
| mining 9 gates breaking `dehydration:copper_cauldron` | loaded into the break gates and enforced by `PlayerBlockBreakEvents.BEFORE` | ✅ | `levelz/mining/09.json`; `SkillGates.java:124,358-369` |
| smithing 28 gates `dehydration:netherite_leather_flask` | loaded and enforced | ✅ | `levelz/smithing/28.json`; `SmithingGateMixin` |
| jobs: all three blocks are builder deliverables, the netherite flask is a smither reward | migrated | ✅ | `jobsaddon/builder/builder_job.json:177-178`; `jobsaddon/smither/smither_job.json:424` |

## 9. Open questions that affect us

Trimmed from the report's section 7 to the ones that change a port decision.
Two of them were closed by re-reading the bytecode while writing this doc
(§1 and §4).

1. **Drop position and physics.** Resolved: the drop is at the **block
   corner**, not the centre (`i2d` of `getX/getY/getZ`, no `0.5`). Still
   open: whether 1.20.1 `ItemScatterer.spawn(World,DDD,ItemStack)` applies
   random ±0.1 horizontal velocity or spawns dead still with the 10-tick
   pick-up delay. It only matters if we ever want to revert the face-pop.
2. **`harder_nether` dimension test.** `DimensionType.comp_644()` is the
   `hasSkylight` record component, so upstream's "in the Nether" penalty
   probably applies **outside** the Nether. Inert in Aged
   (`harder_nether = false`); ours tests `Level.NETHER` directly. Worth a
   comment so nobody re-derives it.
3. **`HARD` difficulty thirst damage.** Upstream reads
   `(hp <= 10 && !PEACEFUL) || (hp > 1 && NORMAL)`, so on HARD thirst stops
   hurting above 10 HP. We deliberately rewrote the gate (§8.3) instead of
   copying the quirk; that decision should be re-confirmed at the 1.0 gate
   because it materially changes HARD-mode difficulty.
4. **`tickMovement` double `update()`.** We reproduce the double call on the
   Peaceful/regeneration path (`HearthwindSurvivalThirst.java:165-172`). If
   we ever make the regen path non-Peaceful, the double drain becomes a
   2× thirst tax on every survival player.
5. **`CampfireCauldronEntity.ticker` not persisted.** Reproduced faithfully
   (§8.2). Note the asymmetry: the campfire bottle's progress **is**
   persisted by 26.2 vanilla, so a chunk unload during a 50 s boil is safe
   but a chunk unload during a 5 s boil is not.
6. **`water_bowl` vs `purified_water_bowl` `hasThirstChance`.** Upstream
   makes both dirty. We made the purified bowl safe. This is a real
   behaviour change players will notice, so it must appear in
   `docs/PLAYER_CHANGES.md` if it is not already.
7. **`PotionUtil.setPotion` (open question #7 in the report) is settled**:
   1.20.1's `PotionUtil.setPotion(stack, potion)` yields a single-dose,
   default-duration bottle. Our `PotionContents.createItemStack` is the
   26.2 equivalent and the drink-once result is identical.
8. **ItemScatterer note (#1) aside, the one thing we could not measure**
   without booting the game is the resolved-hydration count (§8.3). If the
   1.0 gate wants "130 catalogue items restored", the count has to be read
   off a live server, not asserted in a gametest.

## 10. How to re-derive

```bash
# 0. the mod jar and its exploded classes (once; .tmp is git-ignored)
cd /Users/jeremiahsummers/Code/Hearthwind
#    dehydration-1.3.6.jar is inside the Aged pack:
#    unzip -l .tmp/Aged-3.1.2.mrpack | grep -i dehydration     (then extract it)
mkdir -p .tmp/dehydration-1.3.6-classes
cd .tmp/dehydration-1.3.6-classes && jar xf ../dehydration-1.3.6.jar && cd ../..

# 1. mappings that turn class_1844 into a real name
#    (yarn 1.20.1+build.10; .tmp/mappings/y1201/mappings.tiny is checked in)
rg '^c\tnet/minecraft/class_(1842|1844|1847|1812|2237|3922|3924|1264|1269|1267)\t' .tmp/mappings/y1201/mappings.tiny
rg 'field_5801|field_5802' .tmp/mappings/y1201/mappings.tiny      # PEACEFUL / NORMAL
rg 'field_8574|field_8469|field_8705|field_8428|field_27876|field_8665|field_17532|field_8070' \
   .tmp/mappings/y1201/mappings.tiny                               # POTION / GLASS_BOTTLE / ...

# 2. the classes that matter, in the order this doc presents them
cd .tmp/dehydration-1.3.6-classes
for c in mixin/CampfireBlockMixin mixin/CampfireBlockEntityMixin mixin/PlayerEntityMixin \
         mixin/PotionItemMixin mixin/MilkBucketItemMixin mixin/HoneyBottleItemMixin \
         mixin/GlassBottleItemMixin mixin/BrewingRecipeRegistryMixin mixin/WaterFluidMixin \
         mixin/CauldronBehaviorMixin mixin/client/ClientPlayerEntityMixin \
         thirst/ThirstManager effect/ThirstEffect effect/HydrationEffect \
         item/WaterBowlItem item/LeatherFlask item/PurifiedBucket item/PotionUtilMixin \
         init/ItemInit init/BlockInit init/FluidInit init/EffectInit init/EventInit \
         init/ConfigInit config/DehydrationConfig data/DataLoader \
         block/CopperCauldronBehavior block/CopperLeveledCauldronBlock block/CopperCauldronBlock \
         block/BambooPumpBlock block/CampfireCauldronBlock \
         block/entity/CampfireCauldronEntity block/entity/BambooPumpEntity \
         fluid/PurifiedWaterFluid; do
  javap -p -c -constants "net/dehydration/$c.class" > "../javap-dehydration-${c##*/}.txt"
done

# 3. which config key is read where (the §4.1 "used for" column)
cd /Users/jeremiahsummers/Code/Hearthwind/.tmp/dehydration-1.3.6-classes
rg -l 'thirst_effect_factor|water_boiling_time|flask_dirty_thirst_chance' --glob '*.class'

# 4. the reference's own data + config + guide
cat .tmp/aged-3.1.2/overrides/config/dehydration.json5
unzip -l .tmp/dehydration-1.3.6.jar | rg 'recipes/|hydration_items|tags/'
cat .tmp/aged-3.1.2/overrides/config/paxi/resourcepacks/aged_guide_book/assets/aged/lavender/entries/aged_guide_book/hydration/*.md
cat .tmp/aged-3.1.2/overrides/config/paxi/resourcepacks/aged_guide_book/assets/aged/lavender/structures/campfire_cauldron.json
cat .tmp/aged-3.1.2/overrides/config/paxi/datapacks/aged/data/dehydration/hydration_items/aged_items.json

# 5. the 26.2 vanilla facts the deviation rows rest on
S=$(ls custom-mods/.gradle/loom-cache/minecraftMaven/net/minecraft/minecraft-merged-*/26.2/*-sources.jar)
unzip -p "$S" net/minecraft/world/level/block/entity/CampfireBlockEntity.java   # cookTick / cooldownTick
unzip -p $S net/minecraft/world/level/block/CampfireBlock.java                 # useItemOn, getTicker
unzip -p $S net/minecraft/world/level/material/WaterFluid.java                 # isSame / canBeReplacedWith

# 6. our side
rg -n 'BOIL_TIME|cookingTime|cooldownTick' custom-mods/hearthwind-survival/src/main/java
rg -n 'waterBoilingTime|thirstEffectFactor|dirtyThirstChance|waterSipThirst' custom-mods/hearthwind-survival/src/main/java
bash custom-mods/tools/run_gametests_container.sh     # 26.2 side; never on the host
```

Scratch stays under `.tmp/`, per the repo scratch-file policy.
