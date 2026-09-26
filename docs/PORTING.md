# Porting guide: 1.20.1/1.21.1 -> 26.x (verified fixes)

Every entry below was hit while porting the LetsDo-family mods and fixed
against the 26.2 deobfuscated jar. Check this file first when a port module
fails to compile - the fix is probably already here.

## Gone classes (do not import)
| 1.20.1 import | 26.x replacement |
|---|---|
| `net.minecraft.world.item.MilkBucketItem` | Plain `Item` + `Properties.component(DataComponents.CONSUMABLE, Consumables.MILK_BUCKET)` + `craftRemainder(BUCKET)`. Drink anim/sound, effect-clear, stats, container return all come free via `Consumable#onConsume`. See `letsdo-meadow` WoodenMilkBucket. |
| `net.minecraft.world.item.PickaxeItem` / `SwordItem` / `AxeItem` / etc. | Plain `new Item(props)` + `props.pickaxe(ToolMaterial, speed, dmg)` / `.sword(...)` / `.axe(...)` / ... `ToolMaterial` is a record. |
| `net.minecraft.world.entity.EntityType` (singular) | `net.minecraft.world.entity.EntityTypes` (plural) for constants. |
| `DirectionProperty` | Gone. Use `BlockStateProperties.HORIZONTAL_FACING` (`EnumProperty<Direction>`). |

## Changed signatures (most common errors)

- `DispensibleContainerItem.emptyContents(Player, Level, Copper, BlockHitResult)` 
  is now `emptyContents(LivingEntity, Level, Copper, BlockHitResult)`.
  Same for `playEmptySound`. `LivingEntity` import required.
- `LevelAccessor.destroyBlock(Copper, boolean)` is now
  `destroyBlock(Copper, boolean, Entity, int)` (flags; vanilla passes 512
  from bucket code paths... verify per call site with `javap`).
- `LevelAccessor.playSound` arg order is `(Entity, SoundEvent, SoundSource,
  volume, pitch)` - the entity comes FIRST.
- `Item.use` returns sealed `InteractionResult` (`SUCCESS_SERVER` /
  `CONSUME` / `FAIL` / `PASS`), not `InteractionResultHolder`.
- `Item.finishUsingItem(ItemStack, Level, LivingEntity)` - overrides that
  fail with "does not override" usually mean the superclass is wrong
  (e.g. extending the deleted `MilkBucketItem`); fix the superclass first.
- `Block.updateShape` / `useItemOn` / `canSurvive` / `rotate` / `mirror`
  signatures changed - decompile the 26.2 `Block` class and match exactly.
- Game events: call `level.gameEvent(entity, GameEvent.FLUID_PLACE, pos)`
  explicitly where vanilla bucket code does (fill AND place paths).

## Architectury traps

- `InjectedBucketItemExtension.arch$getFluid()` default body does a blind
  `checkcast BucketItem` - any `Item`-based bucket MUST override it to
  return its fluid directly or every call throws `ClassCastException`.
- Dispenser behavior needs NO registration: vanilla
  `DispenseItemBehavior$3` already handles any `DispensibleContainerItem`.

## Assets (26.x)

- Every item needs `assets/<ns>/items/<id>.json`
  (`{"model":{"type":"minecraft:model","model":"<ns>:item/<id>"}}`) -
  `models/item/` alone renders purple-black. Missing ones can be generated
  mechanically; see `.tmp/gen_itemdefs.py`.
- Cross-mod texture refs (`bakery` models -> `farm_and_charm:` textures)
  are legal - upstream shares assets through dependencies. An asset audit
  must resolve refs against ALL shipped mods, not per-module.
- Upstream 1.21.1 trees contain broken refs and orphan files too (missing
  textures, blockstates for unregistered blocks). Mirror the tree, then
  fix/supplement only what registered content needs; see `.tmp/` audit
  tooling and ATTRIBUTION.md for the full trail.
- `Item.Properties.setId(ResourceKey<Item>)` is MANDATORY before
  construction (registry-freeze crash otherwise).

## meadow round (second wave, verified)

- `MapCodec` is invariant: a `LayeredCauldronBlock` subclass override must
  return `MapCodec<LayeredCauldronBlock>`, not `MapCodec<? extends ...>`.
  Keep the `simpleCodec(Sub::new)` field, cast in the override with
  `@SuppressWarnings("unchecked")`.
- `Block.appendHoverText` is GONE - move block tooltips into a `BlockItem`
  subclass (`CompletionistBannerItem` pattern) and register block
  `registerWithoutItem` + item `registerItem` under the same id.
- Do NOT pre-migrate `FoliagePlacer.createFoliage` to
  `LevelSimulatedReader` - 26.2 still takes `WorldGenLevel`.
- `IntProvider.codec()` takes no args now; ranged factory is
  `IntProviders.codec(min, max)` (plural).
- `SpawnerData` is a record: `type()` / `minCount()` / `maxCount()`;
  mob lists come as `Weighted<SpawnerData>`, unwrap with `.value()`.
- Fabric biome effects: `setGrassColor`/`setFoliageColor` became
  `setGrassColorOverride`/`setFoliageColorOverride` on `EffectsContext`.
- Block colors: Architectury `ColorHandlerRegistry.registerBlockColors`
  is gone - use Fabric `BlockColorRegistry` + `BlockTintsFactory`.
- `ItemInHandRenderer.renderArmWithItem` -> `submitArmWithItem`
  (takes `SubmitNodeCollector`); `getMainCamera()` -> `mainCamera()`.
- `GameRules.RULE_DOMOBSPAWNING` split up - portals gate on
  `GameRules.SPAWN_MOBS`.
- Cloth-config: `AutoConfig.getConfigScreen` -> `AutoConfigClient.getConfigScreen`.
- `BlockEntityRenderer` is now `BlockEntityRenderer<T, S extends
  BlockEntityRenderState>` with `createRenderState` / `extractRenderState`
  / `submit` (no more single `render` with PoseStack/MultiBufferSource).
- `Level.destroyBlock(pos, true)` 2-arg still resolves via the
  `LevelWriter` default method - do not "fix" these.

## nethervinery round (1.20.1 source, verified)

- `Identifier` is final (`ResourceLocation` deleted) - `extends
  ResourceLocation` helpers become static factories
  (`Identifier.fromNamespaceAndPath`).
- `InstantenousMobEffect` (sic) deleted - extend `MobEffect` with
  `isInstantaneous()`, `applyInstantaneousEffect(ServerLevel, Entity,
  Entity, LivingEntity, int, double)`, tick form `boolean
  applyEffectTick(ServerLevel, LivingEntity, int)` gated by
  `shouldApplyEffectTickThisTick`. Expiry cleanup moves to
  `onMobRemoved(ServerLevel, LivingEntity, int, Entity.RemovalReason)`.
- `MobEffectInstance` ctors / `getEffect()` are `Holder<MobEffect>`-based;
  vanilla `MobEffects.X` are already holders. `RegistrySupplier.asHolder()`
  bridges Architectury registrars.
- Respawn data: `getRespawnPosition/Dimension()` ->
  `getRespawnConfig().respawnData()` (`.dimension()/.pos()`);
  `getSharedSpawnPos()` -> `ServerLevel.getRespawnData().pos()`;
  cross-dim teleport gains `Set<Relative>` + boolean params.
  `ServerPlayer.server` is private - go through `level().getServer()`.
- Loot: `LootTableReference` -> `NestedLootTable.lootTableReference
  (ResourceKey<LootTable>)`; architectury listener is
  `(ResourceKey<LootTable>, Ctx, boolean)`; `addPool` takes the
  `LootPool.Builder` un-built; id matching via `id.identifier()`.
- `Blocks.<color>_WOOL` gone (`WOOL` is a `ColorCollection`) - use fresh
  `Properties...sound(SoundType.WOOL)`. `BlockBehaviour.getSoundType`
  is protected - use `defaultBlockState().getSoundType()`.
- `Platform.isForge()` -> `isNeoForge()`.
- `net.minecraft.Util` -> `net.minecraft.util.Util`;
  `net.minecraft.util.Tuple` (deleted) -> `datafixers.Pair`
  (`getFirst/getSecond`).
- `InteractionResult.sidedSuccess(b)` gone - use
  `level.isClientSide() ? SUCCESS : CONSUME` (`isClientSide` is a method).
- Block entities: `load(CompoundTag)` -> `loadAdditional(ValueInput)`
  (`getIntOr` etc.), `saveAdditional(CompoundTag)` ->
  `saveAdditional(ValueOutput)`, `getUpdateTag()` ->
  `getUpdateTag(HolderLookup.Provider)` returning
  `saveWithoutMetadata(provider)`; `BlockEntityType.Builder` gone - use
  `new BlockEntityType<>(factory, blocks)`.
- `playerWillDestroy` now returns `BlockState`; `onRemove` ->
  `affectNeighborsAfterRemoval(BlockState, ServerLevel, BlockPos,
  boolean)`; `getMaxBuildHeight()` -> `getMaxY()`.
- `use` therefore splits: no-item interactions go to
  `useWithoutItem(state, world, pos, player, hit)` (read the stack via
  `player.getMainHandItem()`), item interactions to
  `useItemOn(ItemStack, ...)`.
- `ResourceKey.location()` is gone - use `ResourceKey.identifier()`
  (both the registry key and `location().toString()` call sites).
- NetherVinery-specific: never ship `assets/vinery/*` duplicates inside
  the nether module (resource conflict + broken refs - vinery ships its
  own); lattice multipart sub-models exist in no release, so nether
  lattice blockstates collapse missing variants onto the base models.

## Data (worldgen, verified on boot)

- RECIPE INGREDIENTS ARE FLAT in 26.x: `"#": "minecraft:planks"` /
  `"#": "#minecraft:planks"`, NOT `{"item": ...}` / `{"tag": ...}` dicts.
  Same for shapeless `ingredients` arrays. Custom recipe codecs for
  RESULT stacks must use `ItemStackTemplate.MAP_CODEC` (an `ItemStack`
  codec fails at load with "does not have components yet").
  Scripted migration: `.tmp/` sweep + 962 files in the letsdo ports.
- ADVANCEMENT/loot predicate keys are NAMESPACED: `"minecraft:entity_type"`
  not `"type"`; player sub-predicates `"minecraft:type_specific/player"`.
  Entity loot slots use `"attacker"` not `"killer"`; drop
  `bonus_rolls` and `"add": false` (removed from codecs). `time_check`
  requires a `"clock": "minecraft:overworld"` field.
- `minecraft:chain` was renamed to `minecraft:iron_chain`
  (case-sensitive: do NOT touch `chainmail_*`).
- Old custom recipe-type JSONs must match the port's own codec field
  names (`extraingredient` string + `extra_count`, not a nested dict).
- Loot lives under `data/<ns>/loot_table/` (SINGULAR in 26.x - vanilla
  renamed it; a `loot_tables/` dir silently never loads). Fresh 1.20.1
  copies must be renamed.
- Vanilla TAG SPLITS must be re-expanded for Aged parity: 26.x moved
  grass out of `#minecraft:dirt` into `#minecraft:grass_blocks`, so an
  upstream tag of `[#dirt, stone]` must become `[#dirt,
  #grass_blocks, stone]` to cover the same ground (earlystage rocks went
  from everywhere to nearly nowhere without this).
- `minecraft:random_patch` configured features are GONE. Unwrap to the
  inner feature, move `tries`/`xz_spread`/`y_spread` to the placed feature
  as `count` + `random_offset` (trapezoid spreads), and append the inner
  `block_predicate_filter`s there (mirrors vanilla `patch_grass_plain`).
  Script: `.tmp/migrate_random_patch.py`.
- `minecraft:tree` configs now REQUIRE `below_trunk_provider` (vanilla
  uses a rule_based provider guarding the
  `cannot_replace_below_tree_trunk` tag, falling back to dirt).
- Production access-wideners must declare the `official` namespace
  (`accessWidener v2 official`, like cloth-config) - loom ships our jars
  un-remapped (`named` label crashes loader 0.19 at launch with
  "Namespace (named) does not match current runtime namespace").
- `Cow.mobInteract` moved to `AbstractCow` - retarget milking mixins.
- Every `BlockBehaviour.Properties` for a registered block needs
  `.setId(...)` or construction throws `NullPointerException: Block id
  not set` at registry time (find stragglers by grepping registrations
  without `setId`).

## Client-verified texture rules (a real client finds what static checks miss)

- Un-namespaced refs (`block/chain`, `entity/bed/black`) resolve to
  VANILLA - and 26.x moved/removed many: chain block is now
  `minecraft:block/iron_chain`, `entity/bed/*` are gone (beds render as
  block models). Validate bare refs against the 26.2 texture list
  (`.tmp/vanilla_textures.txt`); always write explicit namespaces.
- Blockbench's `"texture": "#missing"` faces ship in upstream models and
  render purple. Repoint them at the model's own primary var (`#0`, else
  `#particle`). The audit flags `HASHMISSING`.
- Element `#var` refs must resolve through the model + mod-namespace
  parent chain - but only for directly rendered models (blockstate/itemdef
  targets); pure templates extended by children are exempt. The audit
  flags reachable `UNDEF-VAR`.
- Every stitched model needs a `particle` texture (vanilla logs "Missing
  texture references: particle" otherwise) - including element-bearing
  item models and orphan models nobody references. Copy the primary
  texture path; window-pane inventory parents need it too (children
  inherit it). The audit flags missing `NO-PARTICLE`.
- Container suites consume `conversion/build/dist/*/mods` jars: refresh
  them from `build/libs` after every build or the suite silently tests
  stale code (this masked real failures once). `run_client_gametests.sh`
  now overlays all fresh local jars on top regardless.
- The ultimate check is a real client boot: it logs `Missing textures in
  model X:` (file gone) and `Missing texture references in model X:`
  (var/`#missing`/vanilla renames). Grep the client log after every
  asset pass.

## HUD rules (26.x)

- Aged 3.1.2 ships **CombatRoll 1.3.3** (not Aged 1.3.3 - that is the
  mod's version) with `config/combatroll/hud_config.json` and
  `field_1342/field_1343` offset keys. Our 26.2 **CombatRoll 3.0.1** moved
  the path to `config/combat_roll/` (underscore): `hud_config.json`
  (rollWidget origin+offset x/y) and `client.json5`
  (keybindingLabelPosition etc.); writing Aged's `config/combatroll/`
  files into 3.0.1 is silently ignored. With Aged's offset x=124.68 and
  the key label LEFT, the label box overlaps EnvironmentZ's thermometer
  at +95; set the label RIGHT (tiny documented deviation) or the widget
  stays clear.
- Aged 3.1.2 inventory tab strip (LibZ 1.0.3, MIT) - verified from the
  released jars, NOT the GitHub branches:
  * Inventory tabs, sorted by preferedPos: LevelZ bag (0, always),
    LevelZ skills/sword (1, always), JobsAddon jobs/hammer (2, always),
    PartyAddon party/face (3, always). No age gating.
  * The ONLY conditional inventory tab is inmisaddon BackpackTab:
    `shouldShow` = a non-empty equipped backpack (appears only then).
    Hearthwind ships no Inmis (manifest: replaced by the bundle rework),
    so four tabs is the correct strip for us.
  * SmitherZ (grinder/grindstone/smither/smithing) and Tiered
    (anvil/reforge) tabs are `registerOtherTab` on the smithing-style
    screens - never on the inventory.
  * LibZ geometry: 25 px pitch, unselected y-21 (first 25 px, others 21),
    selected y-23 (27 px); sheet u = 24/0 (first normal/selected),
    72/48 (others); item icon at +4/-17 or 14x14 texture at +5/-16.
- Gametest mock players are ALWAYS creative:
  `GameTestHelper.makeMockServerPlayerInLevel()` returns a subclass whose
  `gameMode()` hardcodes `GameType.CREATIVE`, so `setGameMode(SURVIVAL)`
  cannot change `isCreative()`. Tests of creative-guarded gameplay logic
  need a package-private test entry that skips the guard (e.g.
  `NutritionEffects.applyForTest`).
- `GuiGraphicsExtractor.blitSprite` has NO uv-subrect form (only full
  x/y/w/h, +float/int variants) - partial fills (mercury, intensity
  splits) must be pre-rendered as discrete sprite steps, not clipped at
  draw time. `blit(pipeline, Identifier, ...)` takes raw textures, not
  stitched sprites.
- Upstream EnvironmentZ HUD geometry (body icon -7/-52, thermometer
  -140/-32, arrow -150/-32, hide on invulnerable) is the layout spec;
  body/foreground art must be original (GPL sheet not copied).

## Nutrition/diet port (26.2, verified against the merged jar)

- 1.20.1 `HungerManager.update(PlayerEntity)` is now
  `FoodData.tick(ServerPlayer)`. NutritionZ's decay injection
  (`Math.max(foodLevel - 1, 0)`, the only `Math.max(II)I` in the method)
  ports as `@At(value = "INVOKE", target = "Ljava/lang/Math;max(II)I",
  ordinal = 0)`.
- `AttributeModifier` is now a record `(Identifier id, double amount,
  Operation operation)` - the 1.20.1 `(String name, float, Operation)`
  constructor is gone, and ids must be stable because removal is by id.
- `AttributeModifier.Operation` serialized names changed:
  `ADDITION` -> `ADD_VALUE`, `MULTIPLY_BASE` -> `ADD_MULTIPLIED_BASE`,
  `MULTIPLY_TOTAL` -> `ADD_MULTIPLIED_TOTAL`. Map the old names when
  reading 1.20.1 datapacks.
- Attribute registry keys dropped the `generic.` prefix in 26.2
  (`minecraft:generic.attack_speed` -> `minecraft:attack_speed`). Accept
  both ids when loading 1.20.1 JSON.
- `AttributeMap.addTransientAttributeModifiers/removeAttributeModifiers`
  take `Multimap<Holder<Attribute>, AttributeModifier>` and `add` removes
  any same-id modifier first, so re-applying on a 1s cadence is safe.
- Raw-texture sub-rect blits: `GuiGraphicsExtractor.blit(RenderPipeline,
  Identifier, x, y, float u, float v, w, h, texW, texH, color)` (the
  11-arg overload) is the replacement for 1.20.1
  `DrawContext.drawTexture(id, x, y, u, v, w, h)`; pass the texture's
  real pixel size (e.g. 256, 256) as texW/texH.
- `InputConstants.isKeyDown(Window, int)` takes a `Window` in 26.2, not
  a GLFW handle.
- `AbstractContainerScreen`'s hovered-slot field is `hoveredSlot`
  (Yarn `focusedSlot`).
- Fabric persistent attachments: if ONE attachment's codec fails to
  decode, the WHOLE `fabric:attachments` map fails and every attachment
  on the entity is silently dropped. When changing an attachment's shape,
  keep the old shape decodable with `Codec.either(newCodec, legacyCodec)`
  and encode `Either.left(new)`.

## Workflow per module

1. `javap` the 26.2 deobf jar for every vanilla superclass/interface the
   module touches - never guess signatures.
2. Fix compile errors file by file, testing with
   `:module:compileJava` (host build only).
3. `bash tools/update_prism.sh` after each milestone so the Prism
   physical-test installs (Full/Minimal/Dev-Client) stay current.
