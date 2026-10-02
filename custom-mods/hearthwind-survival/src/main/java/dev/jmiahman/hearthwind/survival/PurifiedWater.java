package dev.jmiahman.hearthwind.survival;

import java.util.function.Consumer;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.cauldron.CauldronInteractions;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.fabricmc.fabric.api.registry.FabricPotionBrewingBuilder;
import dev.jmiahman.hearthwind.survival.mixin.CauldronDispatcherAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;

/**
 * Purified water (Dehydration parity, {@code dehydration} namespace):
 * a real placeable fluid + block + bucket. Sipping or drinking it never
 * inflicts thirst. Made by smelting a water bucket (datapack recipe).
 */
public final class PurifiedWater {
    /** Fluid tag covering still + flowing purified water. */
    public static final TagKey<Fluid> PURIFIED_TAG =
            TagKey.create(Registries.FLUID, id("purified_water"));

    public static StillFluid STILL;
    public static Flowing FLOWING;
    public static Block BLOCK;
    public static Item BUCKET;
    /** The {@code dehydration:purified_water} potion carried by boiled bottles. */
    public static Holder<Potion> PURIFIED_POTION;
    /** The {@code dehydration:hydration} potion, the best thirst item in Aged. */
    public static Holder<Potion> HYDRATION_POTION;

    private PurifiedWater() {}

    public static class StillFluid extends net.minecraft.world.level.material.WaterFluid {
        /**
         * Purified water displaces whatever is in the cell it flows into,
         * including vanilla water.
         *
         * <p>Reference {@code WaterFluidMixin} overrides
         * {@code FlowableFluid.spreadTo} (26.2: {@code FlowingFluid}) and, when
         * the spreading fluid is in {@code TagInit.PURIFIED_WATER}, replaces the
         * whole vanilla body with a plain
         * {@code level.setBlock(pos, <purified state>.createLegacyBlock(), 3)}.
         *
         * <p>The vanilla body is
         * {@code if (block instanceof LiquidBlockContainer) container.placeLiquid(...)
         * else { destroy if not air; setBlock }}, and
         * {@code LiquidBlock.placeLiquid} re-checks
         * {@code canBeReplacedWith} - which refuses liquid-into-liquid. So
         * without this override purified water can never take a cell vanilla
         * water already holds: the flow simply stops. The override also skips
         * {@code beforeDestroyingBlock}, which is a no-op for water anyway.
         *
         * <p>26.2 already hands us the new {@code FluidState} to place, and its
         * own last line is exactly {@code setBlock(pos, target.createLegacyBlock(), 3)},
         * so the reference collapses to that one call - no mixin needed, because
         * our fluid already extends 26.2's {@code WaterFluid}.
         */
        /**
         * Purified water has to place ITS OWN block, not vanilla water's.
         *
         * <p>26.2's {@code WaterFluid#createLegacyBlock} is
         * {@code Blocks.WATER.defaultBlockState().setValue(LEVEL, getLegacyLevel(state))}
         * - hardcoded to {@code minecraft:water}. So without this override a
         * purified fluid state written anywhere in the world became VANILLA
         * water, which is why pouring a purified bucket used to hand you normal
         * water and why the displacement test kept seeing vanilla in the cell.
         *
         * <p>Reference {@code PurifiedWaterFluid#method_15790} (createLegacyBlock)
         * is exactly this: {@code BlockInit.PURIFIED_WATER.defaultBlockState()
         * .setValue(FluidState.LEVEL, getLegacyLevel(state))}. It is the piece
         * that makes the fluid a real second fluid rather than a water clone, and
         * the reason {@code spreadTo} below can write a purified cell at all.
         */
        @Override
        public net.minecraft.world.level.block.state.BlockState createLegacyBlock(FluidState fluidState) {
            return BLOCK.defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(fluidState));
        }

        /**
         * Test seam: {@code spreadTo} is protected, and the gametests live in
         * another package. Returns true so a caller can tell the write happened.
         */
        public boolean spreadToForTest(net.minecraft.world.level.LevelAccessor level,
                net.minecraft.core.BlockPos pos,
                net.minecraft.world.level.block.state.BlockState state,
                net.minecraft.core.Direction direction,
                FluidState target) {
            spreadTo(level, pos, state, direction, target);
            return true;
        }

        @Override
        protected void spreadTo(net.minecraft.world.level.LevelAccessor level,
                net.minecraft.core.BlockPos pos,
                net.minecraft.world.level.block.state.BlockState state,
                net.minecraft.core.Direction direction,
                FluidState target) {
            level.setBlock(pos, target.createLegacyBlock(), 3);
        }

        @Override
        public Fluid getFlowing() {
            return FLOWING;
        }

        @Override
        public Fluid getSource() {
            return STILL;
        }

        @Override
        public Item getBucket() {
            return BUCKET;
        }

        @Override
        public boolean isSame(Fluid fluid) {
            return fluid == STILL || fluid == FLOWING;
        }

        @Override
        public int getAmount(FluidState state) {
            return 8;
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }
    }

    public static class Flowing extends StillFluid {
        @Override
        protected void createFluidStateDefinition(
                StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(net.minecraft.world.level.material.FlowingFluid.LEVEL);
        }

        @Override
        public int getAmount(FluidState state) {
            return state.getValue(net.minecraft.world.level.material.FlowingFluid.LEVEL);
        }

        @Override
        public boolean isSource(FluidState state) {
            return false;
        }
    }

    public static void registerAll(Consumer<String> log) {
        STILL = Registry.register(BuiltInRegistries.FLUID, id("purified_water"), new StillFluid());
        FLOWING = Registry.register(BuiltInRegistries.FLUID, id("purified_flowing_water"), new Flowing());
        BLOCK = Registry.register(BuiltInRegistries.BLOCK, id("purified_water"),
                new LiquidBlock(STILL, BlockBehaviour.Properties.of().mapColor(MapColor.WATER).replaceable()
                        .noCollision().strength(100.0F).pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)
                        .noLootTable().setId(blockKey())));
        BUCKET = Registry.register(BuiltInRegistries.ITEM, id("purified_water_bucket"),
                new BucketItem(STILL, new Item.Properties().stacksTo(1).craftRemainder(net.minecraft.world.item.Items.BUCKET)
                        .setId(itemKey())));
        PURIFIED_POTION = Registry.registerForHolder(BuiltInRegistries.POTION, id("purified_water"),
                new Potion("purified_water"));
        HYDRATION_POTION = Registry.registerForHolder(BuiltInRegistries.POTION, id("hydration"),
                new Potion("hydration", new MobEffectInstance(HydrationMobEffect.HOLDER,
                        HydrationMobEffect.POTION_DURATION_TICKS)));
        log.accept("purified water fluid/block/bucket/potion registered");
    }

    /**
     * Upstream {@code BrewingRecipeRegistryMixin} adds exactly three mixes to
     * the vanilla brewing map, all keyed on ingredients vanilla never uses, so
     * nothing vanilla is overridden:
     *
     * <pre>
     * water        + charcoal  -> purified water
     * water        + kelp      -> purified water
     * purified     + ghast tear-> hydration
     * </pre>
     *
     * The reference injects at the tail of {@code registerDefaults}; 26.2 has a
     * real Fabric hook for this ({@code FabricPotionBrewingBuilder.BUILD}), so
     * the mixes go in with no mixin.
     */
    public static void registerBrewing() {
        FabricPotionBrewingBuilder.BUILD.register(builder -> {
            builder.registerPotionRecipe(Potions.WATER, Ingredient.of(Items.CHARCOAL), PURIFIED_POTION);
            builder.registerPotionRecipe(Potions.WATER, Ingredient.of(Items.KELP), PURIFIED_POTION);
            builder.registerPotionRecipe(PURIFIED_POTION, Ingredient.of(Items.GHAST_TEAR), HYDRATION_POTION);
        });
    }

    /**
     * The purified bucket fills a VANILLA cauldron, exactly like a water bucket.
     *
     * <p>Reference {@code CauldronBehaviorMixin} is a one-liner injected at the
     * tail of {@code CauldronBehaviors.registerBucketBehavior(Map)}:
     * {@code map.put(ItemInit.PURIFIED_BUCKET, CauldronBehaviors.FILL_WITH_WATER)}
     * - the purified bucket reuses vanilla's water-bucket row verbatim, so the
     * cauldron ends up as {@code minecraft:water_cauldron} at LEVEL 3. (Aged's
     * guide even says a purified bucket has "no good use" - the cauldron is it.)
     *
     * <p>26.2 renamed the map to
     * {@code CauldronInteraction.Dispatcher} and split it by what the cauldron
     * currently holds: {@code CauldronBlock} passes {@link
     * net.minecraft.core.cauldron.CauldronInteractions#EMPTY} to its super and
     * {@code AbstractCauldronBlock.useItemOn} dispatches through it, so our row
     * goes on {@code EMPTY} - the same slot vanilla's
     * {@code addDefaultInteractions} puts {@code Items.WATER_BUCKET} in.
     * Vanilla's own handler ({@code fillWaterInteraction}) is private, so this
     * mirrors it: refund a water bucket, fill to LEVEL 3, BUCKET_EMPTY sound.
     *
     * <p>Registered on server start rather than in {@code registerAll} because
     * {@code CauldronInteractions} is bootstrapped during Minecraft's own
     * bootstrap; {@code addDefaultInteractions} only ever {@code put}s and never
     * clears, so our row survives whatever order the two run in.
     */
    public static void registerCauldron() {
        ((CauldronDispatcherAccessor) (Object) CauldronInteractions.EMPTY).hearthwind$put(BUCKET,
                (state, level, pos, player, hand, itemInHand) -> {
            if (!level.isClientSide()) {
                player.setItemInHand(hand, ItemUtils.createFilledResult(itemInHand, player,
                        new ItemStack(Items.WATER_BUCKET)));
                player.awardStat(Stats.USE_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(itemInHand.getItem()));
                level.setBlockAndUpdate(pos, Blocks.WATER_CAULDRON.defaultBlockState()
                        .setValue(LayeredCauldronBlock.LEVEL, 3));
                level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.SUCCESS;
                });
    }
    /**
     * Reference Dehydration {@code PotionItemMixin}: a "bad" potion rolls
     * {@code nextFloat() >= potion_bad_thirst_chance} (Aged 0.15 = 85%
     * Thirst risk) for the thirst effect. Kept as a thin wrapper so older
     * callers keep working; the canonical path is
     * {@link ThirstHelper#hydratePlayer}.
     */
    public static void applyPotionThirst(ServerPlayer player, ItemStack stack) {
        if (!stack.is(Items.POTION)) {
            return;
        }
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents == null) {
            return;
        }
        Potion potion = contents.potion().map(Holder::value).orElse(Potions.WATER.value());
        if (!isBadPotion(potion)) {
            return;
        }
        HearthwindSurvivalConfig.Flask cfg = HearthwindSurvivalConfig.get().flask;
        if (player.getRandom().nextFloat() >= cfg.potionBadThirstChance) {
            player.addEffect(new MobEffectInstance(ThirstMobEffect.HOLDER,
                    cfg.potionBadThirstDuration, 0, false, false, true));
        }
    }

    /** Potions the reference treats as unsafe drinking water (exact list). */
    public static boolean isBadPotion(PotionContents contents) {
        return isBadPotion(contents.potion().map(Holder::value)
                .orElse(Potions.WATER.value()));
    }

    public static boolean isBadPotion(Potion potion) {
        return ThirstHelper.isBadPotion(potion);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("dehydration", path);
    }

    private static ResourceKey<Item> itemKey() {
        return ResourceKey.create(Registries.ITEM, id("purified_water_bucket"));
    }

    private static ResourceKey<Block> blockKey() {
        return ResourceKey.create(Registries.BLOCK, id("purified_water"));
    }
}
