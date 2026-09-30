package dev.jmiahman.hearthwind.survival.hydration;

import java.util.Map;
import java.util.function.Predicate;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Util;
import net.minecraft.world.InteractionHand;
import dev.jmiahman.hearthwind.survival.PurifiedWater;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.gameevent.GameEvent;

/**
 * Port of Dehydration 1.4.1 {@code CopperCauldronBehavior} (Aged 1.3.6 had
 * the same maps). Bucket, bottle, bowl and flask transfers are handled by
 * the Fabric fluid transfer storages registered in
 * {@link HydrationStorages}; the behavior maps cover powder snow, which is
 * not a fluid.
 */
public interface CopperCauldronBehavior {
    Map<Item, CopperCauldronBehavior> EMPTY_COPPER_CAULDRON_BEHAVIOR = createMap();
    Map<Item, CopperCauldronBehavior> WATER_COPPER_CAULDRON_BEHAVIOR = createMap();
    Map<Item, CopperCauldronBehavior> POWDER_SNOW_COPPER_CAULDRON_BEHAVIOR = createMap();
    Map<Item, CopperCauldronBehavior> PURIFIED_WATER_COPPER_CAULDRON_BEHAVIOR = createMap();

    CopperCauldronBehavior FILL_WITH_POWDER_SNOW = (state, level, pos, player, hand, stack) ->
            fillCauldron(level, pos, player, hand, stack,
                    HydrationBlocks.COPPER_POWDERED_CAULDRON.defaultBlockState()
                            .setValue(CopperLeveledCauldronBlock.LEVEL, 3),
                    SoundEvents.BUCKET_EMPTY_POWDER_SNOW);

    static Object2ObjectOpenHashMap<Item, CopperCauldronBehavior> createMap() {
        return Util.make(new Object2ObjectOpenHashMap<>(), map ->
                map.defaultReturnValue((state, level, pos, player, hand, stack) -> InteractionResult.PASS));
    }

    InteractionResult interact(BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, ItemStack stack);

    static void registerBehavior() {
        registerBucketBehavior(EMPTY_COPPER_CAULDRON_BEHAVIOR);
        registerBucketBehavior(WATER_COPPER_CAULDRON_BEHAVIOR);
        // A poured water bottle fills a copper cauldron straight to LEVEL 3 and
        // leaves a bowl behind, the same shape as the bucket fill. Aged plays
        // the bucket-empty sound for it.
        EMPTY_COPPER_CAULDRON_BEHAVIOR.put(Items.POTION, (state, level, pos, player, hand, stack) ->
                pourPotion(state, level, pos, player, hand, stack));
        POWDER_SNOW_COPPER_CAULDRON_BEHAVIOR.put(Items.BUCKET, (state, level, pos, player, hand, stack) ->
                emptyCauldron(state, level, pos, player, hand, stack,
                        new ItemStack(Items.POWDER_SNOW_BUCKET),
                        statex -> statex.getValue(CopperLeveledCauldronBlock.LEVEL) == 3,
                        SoundEvents.BUCKET_FILL_POWDER_SNOW));
        registerBucketBehavior(POWDER_SNOW_COPPER_CAULDRON_BEHAVIOR);
        registerBucketBehavior(PURIFIED_WATER_COPPER_CAULDRON_BEHAVIOR);
    }

    /**
     * The potion fill: water or purified water in, a bowl out, and the cauldron
     * jumps straight to LEVEL 3 rather than filling one step at a time. A plain
     * water pour makes it dirty, a purified one keeps it clean.
     */
    private static InteractionResult pourPotion(BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, ItemStack stack) {
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        Holder<Potion> potion = contents == null ? null : contents.potion().orElse(null);
        if (potion == null) {
            return InteractionResult.PASS;
        }
        boolean purified = potion.is(PurifiedWater.PURIFIED_POTION);
        if (!purified && !potion.is(Potions.WATER)) {
            return InteractionResult.PASS;
        }
        BlockState filled = HydrationBlocks.COPPER_PURIFIED_WATER_CAULDRON.defaultBlockState()
                .setValue(CopperLeveledCauldronBlock.LEVEL, 3);
        if (!purified) {
            filled = HydrationBlocks.COPPER_WATER_CAULDRON.defaultBlockState()
                    .setValue(CopperLeveledCauldronBlock.LEVEL, 3);
        }
        if (!level.isClientSide()) {
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.BOWL)));
            player.awardStat(Stats.FILL_CAULDRON);
            level.setBlockAndUpdate(pos, filled);
            level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
        }
        return InteractionResult.SUCCESS;
    }

    static void registerBucketBehavior(Map<Item, CopperCauldronBehavior> behavior) {
        behavior.put(Items.POWDER_SNOW_BUCKET, FILL_WITH_POWDER_SNOW);
    }

    static InteractionResult emptyCauldron(BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, ItemStack stack, ItemStack output, Predicate<BlockState> predicate,
            SoundEvent soundEvent) {
        if (!predicate.test(state)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            Item item = stack.getItem();
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, output));
            player.awardStat(Stats.USE_CAULDRON);
            player.awardStat(Stats.ITEM_USED.get(item));
            level.setBlockAndUpdate(pos, HydrationBlocks.COPPER_CAULDRON.defaultBlockState());
            level.playSound(null, pos, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
        }
        return InteractionResult.SUCCESS;
    }

    static InteractionResult fillCauldron(Level level, BlockPos pos, Player player, InteractionHand hand,
            ItemStack stack, BlockState state, SoundEvent soundEvent) {
        if (!level.isClientSide()) {
            Item item = stack.getItem();
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.BUCKET)));
            player.awardStat(Stats.FILL_CAULDRON);
            player.awardStat(Stats.ITEM_USED.get(item));
            level.setBlockAndUpdate(pos, state);
            level.playSound(null, pos, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
        }
        return InteractionResult.SUCCESS;
    }
}
