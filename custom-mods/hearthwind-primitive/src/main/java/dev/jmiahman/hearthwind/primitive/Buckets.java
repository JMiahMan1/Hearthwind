package dev.jmiahman.hearthwind.primitive;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Earlystage buckets (parity port). Wooden buckets only scoop from water
 * cauldrons and are single-use on pour; clay buckets fire into lava brick
 * buckets; brick buckets scoop cauldrons and pour, water empties back to a
 * brick bucket, lava degrades to a vanilla iron bucket (reference quirk).
 */
final class Buckets {

    private static BlockHitResult blockRay(Level level, Player player, ClipContext.Fluid fluidMode) {
        return level.clip(new ClipContext(player.getEyePosition(),
                player.getEyePosition().add(player.getViewVector(1.0f).scale(5.0)),
                ClipContext.Block.OUTLINE, fluidMode, player));
    }

    private static InteractionResult giveFilled(Level level, Player player, InteractionHand hand,
            ItemStack stack, ItemStack filled) {
        if (!level.isClientSide()) {
            if (player.hasInfiniteMaterials()) {
                player.getInventory().placeItemBackInInventory(filled);
            } else if (stack.getCount() == 1) {
                player.setItemInHand(hand, filled);
            } else {
                stack.shrink(1);
                if (!player.getInventory().add(filled)) {
                    player.drop(filled, false);
                }
            }
        }
        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }

    /** Empty wooden bucket: scoops water only from a water cauldron. */
    public static class WoodenBucketItem extends Item {
        public WoodenBucketItem(Properties props) {
            super(props);
        }

        @Override
        public InteractionResult use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            HitResult hit = net.minecraft.world.item.BucketItem.getPlayerPOVHitResult(
                    level, player, ClipContext.Fluid.SOURCE_ONLY);
            if (hit.getType() != HitResult.Type.BLOCK) {
                return InteractionResult.PASS;
            }
            BlockPos pos = ((BlockHitResult) hit).getBlockPos();
            BlockState state = level.getBlockState(pos);
            if (state.is(Blocks.WATER_CAULDRON)) {
                player.awardStat(Stats.ITEM_USED.get(this));
                level.playSound(null, pos, SoundEvents.BUCKET_FILL,
                        net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);
                if (!level.isClientSide()) {
                    level.setBlock(pos, Blocks.CAULDRON.defaultBlockState(), 3);
                }
                return giveFilled(level, player, hand, stack,
                        new ItemStack(HearthwindPrimitiveItems.WATER_WOODEN_BUCKET));
            }
            return InteractionResult.FAIL;
        }
    }

    /** Filled wooden bucket: single use - pouring destroys the container. */
    public static class WaterWoodenBucketItem extends Item {
        public WaterWoodenBucketItem(Properties props) {
            super(props);
        }

        @Override
        public InteractionResult use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            HitResult hit = player.pick(5.0, 0.0f, false);
            if (hit.getType() != HitResult.Type.BLOCK) {
                return InteractionResult.PASS;
            }
            BlockPos pos = ((BlockHitResult) hit).getBlockPos()
                    .relative(((BlockHitResult) hit).getDirection());
            if (!level.mayInteract(player, pos)) {
                return InteractionResult.FAIL;
            }
            BlockState state = level.getBlockState(pos);
            if (state.is(Blocks.CAULDRON)) {
                if (!level.isClientSide()) {
                    level.setBlock(pos, Blocks.WATER_CAULDRON.defaultBlockState(), 3);
                    if (!player.hasInfiniteMaterials()) {
                        stack.shrink(1);
                    }
                }
                return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
            }
            if (state.isAir() || state.canBeReplaced()) {
                if (!level.isClientSide()) {
                    level.setBlock(pos, Fluids.WATER.defaultFluidState().createLegacyBlock(), 3);
                    if (!player.hasInfiniteMaterials()) {
                        stack.shrink(1);
                    }
                }
                level.playSound(player, pos, net.minecraft.sounds.SoundEvents.BUCKET_EMPTY,
                        net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);
                return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
            }
            return InteractionResult.FAIL;
        }
    }

    /** Clay bucket: dipping it into lava fires it into a lava brick bucket. */
    public static class ClayBucketItem extends Item {
        public ClayBucketItem(Properties props) {
            super(props);
        }

        @Override
        public InteractionResult use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            HitResult hit = net.minecraft.world.item.BucketItem.getPlayerPOVHitResult(
                    level, player, ClipContext.Fluid.SOURCE_ONLY);
            if (hit.getType() != HitResult.Type.BLOCK) {
                return InteractionResult.PASS;
            }
            BlockPos pos = ((BlockHitResult) hit).getBlockPos();
            BlockState state = level.getBlockState(pos);
            if (!state.getFluidState().isEmpty()
                    && state.getFluidState().is(net.minecraft.tags.FluidTags.LAVA)) {
                level.playSound(null, pos, net.minecraft.sounds.SoundEvents.FIRE_EXTINGUISH,
                        net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);
                if (!level.isClientSide()) {
                    if (stack.getCount() == 1) {
                        player.setItemInHand(hand, new ItemStack(HearthwindPrimitiveItems.LAVA_BRICK_BUCKET));
                    } else {
                        stack.shrink(1);
                        ItemStack fired = new ItemStack(HearthwindPrimitiveItems.LAVA_BRICK_BUCKET);
                        if (!player.getInventory().add(fired)) {
                            player.drop(fired, false);
                        }
                    }
                    level.setBlock(pos, state.getBlock() instanceof CauldronBlock
                            ? Blocks.CAULDRON.defaultBlockState()
                            : Blocks.AIR.defaultBlockState(), 3);
                }
                return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
            }
            return InteractionResult.FAIL;
        }
    }

    /** Brick buckets: empty scoops water/lava cauldrons; filled pour. */
    public static class BrickBucketItem extends Item {
        private final net.minecraft.world.level.material.Fluid fluid;

        public BrickBucketItem(net.minecraft.world.level.material.Fluid fluid, Properties props) {
            super(props);
            this.fluid = fluid;
        }

        @Override
        public InteractionResult use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (this.fluid == net.minecraft.world.level.material.Fluids.EMPTY) {
                BlockHitResult hit = level.clip(new ClipContext(player.getEyePosition(),
                        player.getEyePosition().add(player.getViewVector(1.0f).scale(5.0)),
                        ClipContext.Block.OUTLINE, ClipContext.Fluid.SOURCE_ONLY, player));
                if (hit.getType() != HitResult.Type.BLOCK) {
                    return InteractionResult.PASS;
                }
                BlockPos pos = hit.getBlockPos();
                BlockState state = level.getBlockState(pos);
                boolean isWaterCauldron = state.is(Blocks.WATER_CAULDRON);
                boolean isLavaCauldron = state.is(Blocks.LAVA_CAULDRON);
                boolean isWaterSource = !state.getFluidState().isEmpty()
                        && state.getFluidState().is(net.minecraft.tags.FluidTags.WATER)
                        && state.getFluidState().isSource();
                boolean isLavaSource = !state.getFluidState().isEmpty()
                        && state.getFluidState().is(net.minecraft.tags.FluidTags.LAVA)
                        && state.getFluidState().isSource();

                if (isWaterCauldron || isLavaCauldron || isWaterSource || isLavaSource) {
                    boolean lava = isLavaCauldron || isLavaSource;
                    level.playSound(null, pos,
                            lava ? net.minecraft.sounds.SoundEvents.BUCKET_FILL_LAVA
                                    : net.minecraft.sounds.SoundEvents.BUCKET_FILL,
                            net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);
                    ItemStack filled = new ItemStack(lava
                            ? HearthwindPrimitiveItems.LAVA_BRICK_BUCKET
                            : HearthwindPrimitiveItems.WATER_BRICK_BUCKET);
                    if (!level.isClientSide()) {
                        if (isWaterCauldron || isLavaCauldron) {
                            level.setBlock(pos, Blocks.CAULDRON.defaultBlockState(), 3);
                        } else {
                            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                        }
                    }
                    return giveFilled(level, player, hand, stack, filled);
                }
                return InteractionResult.FAIL;
            }

            BlockHitResult hit = level.clip(new ClipContext(player.getEyePosition(),
                    player.getEyePosition().add(player.getViewVector(1.0f).scale(5.0)),
                    ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
            BlockState clickedState = level.getBlockState(hit.getBlockPos());
            BlockPos target = this.fluid == net.minecraft.world.level.material.Fluids.WATER
                            && clickedState.getBlock() instanceof CauldronBlock
                                    ? hit.getBlockPos()
                                    : hit.getBlockPos().relative(hit.getDirection());
            BlockState targetState = level.getBlockState(target);
            if (!level.mayInteract(player, target)) {
                return InteractionResult.FAIL;
            }
            boolean lava = this.fluid.is(net.minecraft.tags.FluidTags.LAVA);
            if (targetState.is(Blocks.CAULDRON) && !lava) {
                if (!level.isClientSide()) {
                    level.setBlock(target, Blocks.WATER_CAULDRON.defaultBlockState(), 3);
                }
                return emptied(level, player, hand, stack, false);
            }
            if (targetState.getBlock() instanceof CauldronBlock) {
                return InteractionResult.FAIL;
            }
            if (targetState.isAir() || targetState.canBeReplaced()) {
                if (!level.isClientSide()) {
                    level.setBlock(target, this.fluid.defaultFluidState().createLegacyBlock(), 3);
                }
                level.playSound(player, target,
                        lava ? net.minecraft.sounds.SoundEvents.BUCKET_EMPTY_LAVA
                                : net.minecraft.sounds.SoundEvents.BUCKET_EMPTY,
                        net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);
                return emptied(level, player, hand, stack, lava);
            }
            return InteractionResult.FAIL;
        }

        /** Water empties back to a brick bucket; lava degrades to iron (parity). */
        private InteractionResult emptied(Level level, Player player, InteractionHand hand,
                ItemStack stack, boolean lava) {
            if (!player.hasInfiniteMaterials()) {
                stack.shrink(1);
                ItemStack back = new ItemStack(lava
                        ? net.minecraft.world.item.Items.BUCKET
                        : HearthwindPrimitiveItems.BRICK_BUCKET);
                if (stack.isEmpty()) {
                    player.setItemInHand(hand, back);
                } else if (!player.getInventory().add(back)) {
                    player.drop(back, false);
                }
            }
            return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
        }
    }
}
