package net.satisfy.meadow.core.util;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.satisfy.meadow.core.registry.ObjectRegistry;

import java.util.function.Predicate;

import static net.minecraft.world.level.block.LayeredCauldronBlock.LEVEL;

/**
 * 26.2: vanilla cauldron maps are data-driven dispatchers with no mod API,
 * so wooden-cauldron behavior lives in the wooden block classes'
 * {@code useItemOn} overrides plus a vanilla-interop mixin. This class holds
 * the shared fill/take helpers with the same semantics as before.
 */
@SuppressWarnings("unused")
public class WoodenCauldronBehavior {
    public static void bootStrap() {
        // No map registration needed in 26.2; behavior is implemented in
        // WoodenCauldronBlock / WoodenWaterCauldronBlock / WoodenBucketCauldronMixin.
    }

    public static InteractionResult fillBucket(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, ItemStack stack, ItemStack output, Predicate<BlockState> predicate, SoundEvent soundEvent) {
        if (!predicate.test(state)) {
            return InteractionResult.FAIL;
        }
        if (!world.isClientSide()) {
            Item item = stack.getItem();
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, output));
            player.awardStat(Stats.ITEM_USED.get(item));
            world.setBlockAndUpdate(pos, ObjectRegistry.WOODEN_CAULDRON.get().defaultBlockState());
            world.playSound(null, pos, soundEvent, SoundSource.BLOCKS, 1.0f, 1.0f);
            world.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
        }
        return (world.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME);
    }

    public static InteractionResult fillCauldron(Level world, BlockPos pos, Player player, InteractionHand hand, ItemStack stack, BlockState state, SoundEvent soundEvent, Item returnItem) {
        if (!world.isClientSide()) {
            Item item = stack.getItem();
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(returnItem)));
            player.awardStat(Stats.FILL_CAULDRON);
            player.awardStat(Stats.ITEM_USED.get(item));
            world.setBlockAndUpdate(pos, state);
            world.playSound(null, pos, soundEvent, SoundSource.BLOCKS, 1.0f, 1.0f);
            world.gameEvent(null, GameEvent.FLUID_PLACE, pos);
        }
        return (world.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME);
    }

    public static InteractionResult takeWoodenWater(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, ItemStack stack) {
        return fillBucket(state, world, pos, player, hand, stack, new ItemStack(ObjectRegistry.WOODEN_WATER_BUCKET.get()), s -> s.getValue(LEVEL) == 3, SoundEvents.BUCKET_FILL);
    }
}
