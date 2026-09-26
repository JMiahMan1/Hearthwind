package dev.jmiahman.hearthwind.survival.hydration;

import dev.jmiahman.hearthwind.survival.PurifiedWater;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

/**
 * Port of Dehydration 1.4.1 {@code CampfireCauldronFluidStorage}: four
 * bottle-units of water, switched to purified fluid once boiled. Bottles,
 * bowls and flasks all transfer through the shared item storages.
 */
public class CampfireCauldronFluidStorage extends SingleVariantStorage<FluidVariant> {
    private static final long CAPACITY = FluidConstants.BOTTLE * 4L;

    private final Level world;
    private final BlockPos pos;
    private final BlockState state;
    private final CampfireCauldronBlockEntity campfireCauldronEntity;

    private int fluidLevel;
    private boolean updateBoiling = false;

    public CampfireCauldronFluidStorage(Level world, BlockPos pos, BlockState state,
            CampfireCauldronBlockEntity campfireCauldronEntity) {
        this.world = world;
        this.pos = pos;
        this.state = state;
        this.campfireCauldronEntity = campfireCauldronEntity;
        this.fluidLevel = state.getValue(CampfireCauldronBlock.LEVEL);
        if (this.fluidLevel > 0) {
            this.variant = campfireCauldronEntity.isBoiled
                    ? FluidVariant.of(PurifiedWater.STILL)
                    : FluidVariant.of(Fluids.WATER);
            this.amount = this.fluidLevel * FluidConstants.BOTTLE;
        }
    }

    @Override
    protected FluidVariant getBlankVariant() {
        return FluidVariant.blank();
    }

    @Override
    protected long getCapacity(FluidVariant variant) {
        return CAPACITY;
    }

    @Override
    protected boolean canExtract(FluidVariant variant) {
        return this.variant.getFluid() == variant.getFluid() && this.fluidLevel > 0;
    }

    @Override
    protected boolean canInsert(FluidVariant variant) {
        return (variant.isOf(Fluids.WATER) || variant.isOf(PurifiedWater.STILL))
                && this.fluidLevel < 4;
    }

    @Override
    protected void readSnapshot(ResourceAmount<FluidVariant> snapshot) {
        super.readSnapshot(snapshot);
        this.fluidLevel = Math.min((int) (this.amount / FluidConstants.BOTTLE), 4);
        this.updateBoiling = !this.variant.isBlank()
                && this.variant.getFluid() != PurifiedWater.STILL;
    }

    @Override
    public long insert(FluidVariant insertedVariant, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(insertedVariant, maxAmount);
        if (!this.canInsert(insertedVariant)) {
            return 0;
        }
        updateSnapshots(transaction);
        int levelIncrease = Math.min((int) (maxAmount / FluidConstants.BOTTLE), 4 - this.fluidLevel);
        if (levelIncrease <= 0) {
            return 0;
        }
        this.fluidLevel += levelIncrease;
        long insertedAmount = (long) levelIncrease * FluidConstants.BOTTLE;
        this.amount += insertedAmount;
        if (this.variant.getFluid() != PurifiedWater.STILL
                || insertedVariant.getFluid() != PurifiedWater.STILL) {
            this.updateBoiling = true;
        }
        if (this.variant.isBlank()) {
            this.variant = insertedVariant;
        } else if (this.variant.getFluid() != insertedVariant.getFluid()) {
            this.variant = this.variant.getFluid() != PurifiedWater.STILL ? insertedVariant : this.variant;
        }
        return insertedAmount;
    }

    @Override
    public long extract(FluidVariant extractedVariant, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(extractedVariant, maxAmount);
        if (!this.canExtract(extractedVariant)) {
            return 0;
        }
        updateSnapshots(transaction);
        int levelDecrease = Math.min((int) (maxAmount / FluidConstants.BOTTLE), this.fluidLevel);
        if (levelDecrease <= 0) {
            return 0;
        }
        this.fluidLevel -= levelDecrease;
        long extractedAmount = (long) levelDecrease * FluidConstants.BOTTLE;
        this.amount -= extractedAmount;
        if (this.amount <= 0 || this.fluidLevel <= 0) {
            this.fluidLevel = 0;
            this.amount = 0;
            this.variant = this.getBlankVariant();
        }
        return extractedAmount;
    }

    @Override
    protected void onFinalCommit() {
        if (this.state.getBlock() instanceof CampfireCauldronBlock campfireCauldronBlock) {
            campfireCauldronBlock.setLevel(this.world, this.pos, this.state, this.fluidLevel);
            if (this.updateBoiling) {
                this.campfireCauldronEntity.onFillingCauldron();
            }
        }
    }
}
