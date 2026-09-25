package dev.jmiahman.hearthwind.survival;

import java.util.function.Consumer;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Block;
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

    private PurifiedWater() {}

    public static class StillFluid extends net.minecraft.world.level.material.WaterFluid {
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
        log.accept("purified water fluid/block/bucket/potion registered");
    }

    /**
     * Reference Dehydration {@code PotionItemMixin}: drinking a "bad" potion
     * (water, awkward, mundane, thick) rolls the configured chance of the
     * thirst effect. Hydration itself comes from the corpus hook.
     */
    public static void applyPotionThirst(ServerPlayer player, ItemStack stack) {
        if (!stack.is(Items.POTION)) {
            return;
        }
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents == null || !isBadPotion(contents)) {
            return;
        }
        HearthwindSurvivalConfig.Flask cfg = HearthwindSurvivalConfig.get().flask;
        if (player.getRandom().nextFloat() < cfg.potionBadThirstChance) {
            player.addEffect(new MobEffectInstance(ThirstMobEffect.HOLDER, cfg.potionBadThirstDuration, 0));
        }
    }

    /** Potions the reference treats as unsafe drinking water. */
    public static boolean isBadPotion(PotionContents contents) {
        if (!contents.customEffects().isEmpty()) {
            return false;
        }
        return contents.potion()
                .map(holder -> holder.value() == Potions.WATER.value()
                        || holder.value() == Potions.AWKWARD.value()
                        || holder.value() == Potions.MUNDANE.value()
                        || holder.value() == Potions.THICK.value())
                .orElse(false);
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
