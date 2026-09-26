package dev.jmiahman.hearthwind.survival.hydration;

import dev.jmiahman.hearthwind.survival.PurifiedWater;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Dehydration 1.3.6 {@code EventInit} bowl filling: sneak + right-click a
 * still water source with a {@code minecraft:bowl} in hand swaps it for a
 * {@code dehydration:water_bowl}, or {@code dehydration:purified_water_bowl}
 * when the fluid is in the {@code dehydration:purified_water} tag, and
 * consumes the source block.
 *
 * <p>Registered on both the item and block use events (the 1.3.6 mod used
 * the block event; the 1.4.1 upstream used the item event). The existing
 * {@code BareHandDrinkHandler} owns the empty-hand sip; this handler only
 * acts on bowls.
 */
public final class HydrationBowlHandler {
    private HydrationBowlHandler() {}

    public static void register() {
        UseItemCallback.EVENT.register((player, level, hand) -> tryFillBowl(player, level, hand));
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> tryFillBowl(player, level, hand));
    }

    public static InteractionResult tryFillBowl(Player player, Level level, InteractionHand hand) {
        if (player.isCreative() || player.isSpectator() || !player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.is(Items.BOWL)) {
            return InteractionResult.PASS;
        }
        BlockPos pos = findSource(player, level);
        if (pos == null) {
            return InteractionResult.PASS;
        }
        FluidState fluid = level.getFluidState(pos);
        boolean purified = fluid.is(PurifiedWater.PURIFIED_TAG);
        if (!level.mayInteract(player, pos) || !(fluid.is(FluidTags.WATER) || purified)) {
            return InteractionResult.PASS;
        }
        if (!fluid.isSource()) {
            return InteractionResult.PASS;
        }

        level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
        if (!level.isClientSide()) {
            ItemStack filled = new ItemStack(purified
                    ? HydrationItems.PURIFIED_WATER_BOWL
                    : HydrationItems.WATER_BOWL);
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, filled));
            player.awardStat(Stats.ITEM_USED.get(player.getItemInHand(hand).getItem()));
            BlockState state = level.getBlockState(pos);
            if (state.hasProperty(BlockStateProperties.WATERLOGGED)
                    && state.getValue(BlockStateProperties.WATERLOGGED)) {
                level.setBlock(pos, state.setValue(BlockStateProperties.WATERLOGGED, false), 3);
            } else {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * Upstream used a hard raycast of 4.5 blocks; mock players report a
     * zero interaction range, so clamp to at least the real range and fall
     * back to the player's own block when the fluid ray misses (standing
     * inside a source, where fluid shapes can be skipped).
     */
    private static BlockPos findSource(Player player, Level level) {
        HitResult hitResult = player.pick(Math.max(4.5, player.blockInteractionRange()), 0.0F, true);
        if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = ((BlockHitResult) hitResult).getBlockPos();
            FluidState fluid = level.getFluidState(pos);
            if (fluid.is(FluidTags.WATER) || fluid.is(PurifiedWater.PURIFIED_TAG)) {
                return pos;
            }
        }
        BlockPos own = player.blockPosition();
        FluidState ownFluid = level.getFluidState(own);
        if (ownFluid.is(FluidTags.WATER) || ownFluid.is(PurifiedWater.PURIFIED_TAG)) {
            return own;
        }
        return null;
    }
}
