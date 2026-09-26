package dev.jmiahman.hearthwind.survival.hydration;

import dev.jmiahman.hearthwind.survival.PurifiedWater;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

/**
 * Port of Dehydration 1.4.1 {@code BowlFluidStorage}: an empty bowl accepts
 * one bottle of water or purified water, and a filled bowl can be emptied
 * back into a cauldron.
 */
public class BowlFluidStorage extends SingleVariantStorage<FluidVariant> {
    private static final long BOWL_CAPACITY = FluidConstants.BOTTLE;

    private final ItemStack stack;
    private final ContainerItemContext context;

    public BowlFluidStorage(ItemStack stack, ContainerItemContext context) {
        this.stack = stack;
        this.context = context;
    }

    @Override
    protected FluidVariant getBlankVariant() {
        return FluidVariant.blank();
    }

    @Override
    public boolean isResourceBlank() {
        return this.stack.is(Items.BOWL);
    }

    @Override
    public FluidVariant getResource() {
        if (this.stack.is(HydrationItems.WATER_BOWL)) {
            return FluidVariant.of(Fluids.WATER);
        } else if (this.stack.is(HydrationItems.PURIFIED_WATER_BOWL)) {
            return FluidVariant.of(PurifiedWater.STILL);
        }
        return this.getBlankVariant();
    }

    @Override
    public long getAmount() {
        return (this.stack.is(HydrationItems.WATER_BOWL)
                || this.stack.is(HydrationItems.PURIFIED_WATER_BOWL)) ? BOWL_CAPACITY : 0;
    }

    @Override
    protected long getCapacity(FluidVariant variant) {
        return BOWL_CAPACITY;
    }

    @Override
    protected boolean canExtract(FluidVariant variant) {
        if (variant.isOf(Fluids.WATER)) {
            return this.stack.is(HydrationItems.WATER_BOWL)
                    || this.stack.is(HydrationItems.PURIFIED_WATER_BOWL);
        } else if (variant.isOf(PurifiedWater.STILL)) {
            return this.stack.is(HydrationItems.PURIFIED_WATER_BOWL);
        }
        return false;
    }

    @Override
    protected boolean canInsert(FluidVariant variant) {
        return this.stack.is(Items.BOWL)
                && (variant.isOf(Fluids.WATER) || variant.isOf(PurifiedWater.STILL));
    }

    @Override
    public long insert(FluidVariant insertedVariant, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(insertedVariant, maxAmount);
        if (!this.canInsert(insertedVariant) || maxAmount < BOWL_CAPACITY) {
            return 0;
        }
        ItemStack newStack = new ItemStack(insertedVariant.isOf(PurifiedWater.STILL)
                ? HydrationItems.PURIFIED_WATER_BOWL
                : HydrationItems.WATER_BOWL);
        if (this.context.exchange(ItemVariant.of(newStack), 1, transaction) == 1) {
            return BOWL_CAPACITY;
        }
        return 0;
    }

    @Override
    public long extract(FluidVariant extractedVariant, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(extractedVariant, maxAmount);
        if (!this.canExtract(extractedVariant) || maxAmount < BOWL_CAPACITY) {
            return 0;
        }
        ItemStack newStack = new ItemStack(Items.BOWL);
        if (this.context.exchange(ItemVariant.of(newStack), 1, transaction) == 1) {
            return BOWL_CAPACITY;
        }
        return 0;
    }
}
