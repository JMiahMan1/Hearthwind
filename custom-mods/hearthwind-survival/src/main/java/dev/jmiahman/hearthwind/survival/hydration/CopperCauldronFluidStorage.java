package dev.jmiahman.hearthwind.survival.hydration;

import com.google.common.primitives.Ints;

import dev.jmiahman.hearthwind.survival.PurifiedWater;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/**
 * Port of Dehydration 1.4.1 {@code CopperCauldronFluidStorage}: bucket,
 * bottle, bowl and flask transfers for the copper cauldron family. One
 * level is one bottle (27000 droplets), so a level-3 cauldron holds exactly
 * one bucket.
 */
public class CopperCauldronFluidStorage extends SnapshotParticipant<BlockState>
        implements SingleSlotStorage<FluidVariant> {

    private final Level world;
    private final BlockPos pos;

    private BlockState lastReleasedSnapshot;

    public CopperCauldronFluidStorage(Level world, BlockPos pos) {
        this.world = world;
        this.pos = pos;
    }

    private CopperCauldronFluidContent getCurrentContent() {
        return new CopperCauldronFluidContent(this.createSnapshot().getBlock());
    }

    private void updateLevel(CopperCauldronFluidContent newContent, int level,
            TransactionContext transaction) {
        updateSnapshots(transaction);
        BlockState newState = newContent.block.defaultBlockState();
        if (newContent.levelProperty != null) {
            newState = newState.setValue(newContent.levelProperty, level);
        }
        this.world.setBlock(this.pos, newState, 0);
    }

    @Override
    public long insert(FluidVariant fluidVariant, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(fluidVariant, maxAmount);
        CopperCauldronFluidContent insertContent = new CopperCauldronFluidContent(fluidVariant.getFluid());
        int maxLevelsInserted = Ints.saturatedCast(maxAmount / insertContent.amountPerLevel);

        if (this.getAmount() == 0) {
            if (fluidVariant.isOf(Fluids.LAVA)) {
                return -1;
            }
            int levelsInserted = Math.min(maxLevelsInserted, insertContent.maxLevel);
            if (levelsInserted > 0) {
                this.updateLevel(insertContent, levelsInserted, transaction);
            }
            return (long) levelsInserted * insertContent.amountPerLevel;
        }

        CopperCauldronFluidContent currentContent = this.getCurrentContent();
        CopperCauldronFluidContent mixedContent;
        if (fluidVariant.isOf(currentContent.fluid)) {
            mixedContent = currentContent;
        } else if (fluidVariant.isOf(Fluids.WATER) || fluidVariant.isOf(PurifiedWater.STILL)) {
            mixedContent = new CopperCauldronFluidContent(Fluids.WATER);
        } else {
            return 0;
        }

        int currentLevel = currentContent.currentLevel(this.createSnapshot());
        int levelsInserted = Math.min(maxLevelsInserted, currentContent.maxLevel - currentLevel);
        if (levelsInserted > 0) {
            this.updateLevel(mixedContent, currentLevel + levelsInserted, transaction);
        }
        return (long) levelsInserted * mixedContent.amountPerLevel;
    }

    @Override
    public long extract(FluidVariant fluidVariant, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(fluidVariant, maxAmount);
        CopperCauldronFluidContent currentContent = this.getCurrentContent();
        if (fluidVariant.isOf(currentContent.fluid)) {
            int maxLevelsExtracted = Ints.saturatedCast(maxAmount / currentContent.amountPerLevel);
            int currentLevel = currentContent.currentLevel(this.createSnapshot());
            int levelsExtracted = Math.min(maxLevelsExtracted, currentLevel);
            if (levelsExtracted > 0) {
                if (levelsExtracted == currentLevel) {
                    updateSnapshots(transaction);
                    this.world.setBlock(this.pos, HydrationBlocks.COPPER_CAULDRON.defaultBlockState(), 0);
                } else {
                    this.updateLevel(currentContent, currentLevel - levelsExtracted, transaction);
                }
            }
            return (long) levelsExtracted * currentContent.amountPerLevel;
        }
        return 0;
    }

    @Override
    public boolean isResourceBlank() {
        return this.getResource().isBlank();
    }

    @Override
    public FluidVariant getResource() {
        return FluidVariant.of(this.getCurrentContent().fluid);
    }

    @Override
    public long getAmount() {
        CopperCauldronFluidContent currentContent = this.getCurrentContent();
        return (long) currentContent.currentLevel(this.createSnapshot()) * currentContent.amountPerLevel;
    }

    @Override
    public long getCapacity() {
        CopperCauldronFluidContent currentContent = this.getCurrentContent();
        return (long) currentContent.maxLevel * currentContent.amountPerLevel;
    }

    @Override
    public BlockState createSnapshot() {
        return this.world.getBlockState(this.pos);
    }

    @Override
    public void readSnapshot(BlockState savedState) {
        this.world.setBlock(this.pos, savedState, 0);
    }

    @Override
    protected void releaseSnapshot(BlockState snapshot) {
        this.lastReleasedSnapshot = snapshot;
    }

    @Override
    protected void onFinalCommit() {
        BlockState state = this.createSnapshot();
        BlockState originalState = this.lastReleasedSnapshot;
        if (originalState != state) {
            this.world.setBlock(this.pos, originalState, 0);
            this.world.setBlock(this.pos, state, 3);
        }
    }

    @Override
    public String toString() {
        return "CopperCauldronStorage[" + this.world + ", " + this.pos + "]";
    }

    private static class CopperCauldronFluidContent {
        final Block block;
        final Fluid fluid;
        final long amountPerLevel;
        final int maxLevel;
        final IntegerProperty levelProperty;

        private CopperCauldronFluidContent(Block block) {
            if (block == HydrationBlocks.COPPER_WATER_CAULDRON) {
                this.block = block;
                this.fluid = Fluids.WATER;
                this.amountPerLevel = FluidConstants.BOTTLE;
                this.maxLevel = 3;
                this.levelProperty = CopperLeveledCauldronBlock.LEVEL;
            } else if (block == HydrationBlocks.COPPER_PURIFIED_WATER_CAULDRON) {
                this.block = block;
                this.fluid = PurifiedWater.STILL;
                this.amountPerLevel = FluidConstants.BOTTLE;
                this.maxLevel = 3;
                this.levelProperty = CopperLeveledCauldronBlock.LEVEL;
            } else {
                this.block = HydrationBlocks.COPPER_CAULDRON;
                this.fluid = Fluids.EMPTY;
                this.amountPerLevel = FluidConstants.BUCKET;
                this.maxLevel = 1;
                this.levelProperty = null;
            }
        }

        private CopperCauldronFluidContent(Fluid fluid) {
            if (fluid == Fluids.WATER) {
                this.fluid = fluid;
                this.block = HydrationBlocks.COPPER_WATER_CAULDRON;
                this.amountPerLevel = FluidConstants.BOTTLE;
                this.maxLevel = 3;
                this.levelProperty = CopperLeveledCauldronBlock.LEVEL;
            } else if (fluid == PurifiedWater.STILL) {
                this.fluid = fluid;
                this.block = HydrationBlocks.COPPER_PURIFIED_WATER_CAULDRON;
                this.amountPerLevel = FluidConstants.BOTTLE;
                this.maxLevel = 3;
                this.levelProperty = CopperLeveledCauldronBlock.LEVEL;
            } else {
                this.fluid = Fluids.EMPTY;
                this.block = HydrationBlocks.COPPER_CAULDRON;
                this.amountPerLevel = FluidConstants.BUCKET;
                this.maxLevel = 1;
                this.levelProperty = null;
            }
        }

        private int currentLevel(BlockState state) {
            if (this.fluid == Fluids.EMPTY) {
                return 0;
            } else if (this.levelProperty == null) {
                return 1;
            }
            return state.getValue(this.levelProperty);
        }
    }
}
