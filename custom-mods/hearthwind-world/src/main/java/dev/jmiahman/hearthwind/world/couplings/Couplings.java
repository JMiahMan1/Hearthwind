package dev.jmiahman.hearthwind.world.couplings;

import dev.jmiahman.hearthwind.world.HearthwindWorldConfig;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 26.2 Modern port of Couplings (Aged 3.1.2 parity, Tschipp):
 * Synchronizes opening/closing of double doors, fence gates, and trapdoors.
 * Sneaking bypasses coupling to allow single-door manipulation.
 */
public final class Couplings {
    private static boolean isHandling = false;

    private Couplings() {}

    public static void register() {
        UseBlockCallback.EVENT.register(Couplings::onUseBlock);
    }

    public static InteractionResult onUseBlock(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        if (isHandling || hand != InteractionHand.MAIN_HAND || player.isCrouching()) {
            return InteractionResult.PASS;
        }

        BlockPos pos = hitResult.getBlockPos();
        BlockState state = level.getBlockState(pos);

        if (state.getBlock() instanceof DoorBlock) {
            return handleDoor(player, level, pos, state);
        } else if (state.getBlock() instanceof FenceGateBlock) {
            return handleFenceGate(player, level, pos, state);
        } else if (state.getBlock() instanceof TrapDoorBlock && HearthwindWorldConfig.get().coupleTrapdoors) {
            return handleTrapdoor(player, level, pos, state);
        }

        return InteractionResult.PASS;
    }

    private static InteractionResult handleDoor(Player player, Level level, BlockPos pos, BlockState state) {
        if (state.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER) {
            pos = pos.below();
            state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof DoorBlock)) {
                return InteractionResult.PASS;
            }
        }

        Direction facing = state.getValue(DoorBlock.FACING);
        DoorHingeSide hinge = state.getValue(DoorBlock.HINGE);
        boolean open = state.getValue(DoorBlock.OPEN);
        boolean newOpen = !open;

        // Check horizontal perpendicular neighbors for paired double door
        Direction searchDir = hinge == DoorHingeSide.LEFT ? facing.getClockWise() : facing.getCounterClockWise();
        BlockPos otherPos = pos.relative(searchDir);
        BlockState otherState = level.getBlockState(otherPos);

        if (otherState.getBlock() == state.getBlock()
                && otherState.getValue(DoorBlock.FACING) == facing
                && otherState.getValue(DoorBlock.HINGE) != hinge
                && otherState.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER
                && otherState.getValue(DoorBlock.OPEN) != newOpen) {

            // Synchronize other door
            isHandling = true;
            try {
                ((DoorBlock) otherState.getBlock()).setOpen(player, level, otherState, otherPos, newOpen);
            } finally {
                isHandling = false;
            }
        }

        return InteractionResult.PASS;
    }

    private static InteractionResult handleFenceGate(Player player, Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FenceGateBlock.FACING);
        boolean open = state.getValue(FenceGateBlock.OPEN);
        boolean newOpen = !open;

        // Check horizontal adjacent neighbors
        Direction[] dirs = { facing.getClockWise(), facing.getCounterClockWise() };
        for (Direction dir : dirs) {
            BlockPos otherPos = pos.relative(dir);
            BlockState otherState = level.getBlockState(otherPos);

            if (otherState.getBlock() instanceof FenceGateBlock
                    && otherState.getValue(FenceGateBlock.FACING).getAxis() == facing.getAxis()
                    && otherState.getValue(FenceGateBlock.OPEN) != newOpen) {

                isHandling = true;
                try {
                    BlockState updated = otherState.setValue(FenceGateBlock.OPEN, newOpen);
                    level.setBlock(otherPos, updated, 10);
                } finally {
                    isHandling = false;
                }
            }
        }

        return InteractionResult.PASS;
    }

    private static InteractionResult handleTrapdoor(Player player, Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(TrapDoorBlock.FACING);
        boolean open = state.getValue(TrapDoorBlock.OPEN);
        boolean newOpen = !open;

        Direction[] dirs = { facing.getClockWise(), facing.getCounterClockWise() };
        for (Direction dir : dirs) {
            BlockPos otherPos = pos.relative(dir);
            BlockState otherState = level.getBlockState(otherPos);

            if (otherState.getBlock() instanceof TrapDoorBlock
                    && otherState.getValue(TrapDoorBlock.OPEN) != newOpen) {

                isHandling = true;
                try {
                    BlockState updated = otherState.setValue(TrapDoorBlock.OPEN, newOpen);
                    level.setBlock(otherPos, updated, 10);
                } finally {
                    isHandling = false;
                }
            }
        }

        return InteractionResult.PASS;
    }

    public static boolean coupleDoubleDoors(Level level, BlockPos pos1, BlockPos pos2, boolean open) {
        BlockState state1 = level.getBlockState(pos1);
        BlockState state2 = level.getBlockState(pos2);
        if (state1.getBlock() instanceof DoorBlock && state2.getBlock() instanceof DoorBlock) {
            level.setBlock(pos1, state1.setValue(DoorBlock.OPEN, open), 10);
            level.setBlock(pos2, state2.setValue(DoorBlock.OPEN, open), 10);
            return true;
        }
        return false;
    }
}
