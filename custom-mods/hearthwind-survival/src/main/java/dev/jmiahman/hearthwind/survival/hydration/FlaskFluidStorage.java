package dev.jmiahman.hearthwind.survival.hydration;

import java.util.HashMap;
import java.util.Map;

import dev.jmiahman.hearthwind.survival.FlaskData;
import dev.jmiahman.hearthwind.survival.FlaskItems;
import dev.jmiahman.hearthwind.survival.LeatherFlaskItem;
import dev.jmiahman.hearthwind.survival.PurifiedWater;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;

/**
 * Fabric fluid storage for the Hearthwind leather flasks on cauldrons, a
 * port of Dehydration 1.4.1 {@code LeatherFlaskFluidStorage} adapted to this
 * repo's {@code dehydration:flask_data} component.
 *
 * <p>Quality follows {@link FlaskData}: 0 purified, 1 impure, 2 dirty. The
 * cauldron-side fluids are only water (dirty) and purified water, so the
 * upstream rule applies: taking dirty water sets quality 2 when the flask
 * was empty and bumps it one step when topping up, capped at 2; purified
 * water never cleans a dirty flask.
 */
public class FlaskFluidStorage extends SingleVariantStorage<FluidVariant> {
    private static final FluidVariant DIRTY_WATER = FluidVariant.of(Fluids.WATER);
    private static final FluidVariant PURIFIED_WATER = FluidVariant.of(PurifiedWater.STILL);

    private final ContainerItemContext context;
    private final int flaskCapacity;
    private final Map<ResourceAmount<FluidVariant>, Integer> qualitySnapshots = new HashMap<>();

    private int fillLevel;
    private int qualityLevel;

    public FlaskFluidStorage(ItemStack stack, ContainerItemContext context) {
        this.context = context;
        this.flaskCapacity = stack.getItem() instanceof LeatherFlaskItem flask ? flask.capacity() : 2;
        FlaskData data = stack.get(FlaskItems.FLASK_DATA);
        if (data != null && data.fillLevel() > 0) {
            this.fillLevel = Math.min(data.fillLevel(), this.flaskCapacity);
            this.qualityLevel = data.qualityLevel();
            this.variant = this.qualityLevel > 0 ? DIRTY_WATER : PURIFIED_WATER;
            this.amount = (long) this.fillLevel * FluidConstants.BOTTLE;
        }
    }

    @Override
    protected FluidVariant getBlankVariant() {
        return FluidVariant.blank();
    }

    @Override
    protected long getCapacity(FluidVariant variant) {
        return FluidConstants.BOTTLE * (long) this.flaskCapacity;
    }

    @Override
    protected boolean canExtract(FluidVariant variant) {
        return this.variant.getFluid() == variant.getFluid() && this.fillLevel > 0;
    }

    @Override
    protected boolean canInsert(FluidVariant variant) {
        return (variant.isOf(Fluids.WATER) || variant.isOf(PurifiedWater.STILL))
                && this.fillLevel < this.flaskCapacity;
    }

    @Override
    public long insert(FluidVariant insertedVariant, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(insertedVariant, maxAmount);
        if (!this.canInsert(insertedVariant)) {
            return 0;
        }
        updateSnapshots(transaction);
        int levelIncrease = Math.min((int) (maxAmount / FluidConstants.BOTTLE),
                this.flaskCapacity - this.fillLevel);
        if (levelIncrease <= 0) {
            return 0;
        }
        if (!insertedVariant.isOf(PurifiedWater.STILL)) {
            int dirtyAddition = levelIncrease > this.fillLevel ? 2 : 1;
            this.qualityLevel = Math.min(this.qualityLevel + dirtyAddition, 2);
        }
        this.fillLevel += levelIncrease;
        long insertedAmount = (long) levelIncrease * FluidConstants.BOTTLE;
        this.amount += insertedAmount;
        this.variant = this.qualityLevel > 0 ? DIRTY_WATER : PURIFIED_WATER;
        ItemStack newStack = this.context.getItemVariant().toStack();
        FlaskItems.setFill(newStack, this.fillLevel, this.qualityLevel);
        if (this.context.exchange(ItemVariant.of(newStack), 1, transaction) == 1) {
            return insertedAmount;
        }
        return 0;
    }

    @Override
    public long extract(FluidVariant extractedVariant, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(extractedVariant, maxAmount);
        if (!this.canExtract(extractedVariant)) {
            return 0;
        }
        updateSnapshots(transaction);
        int levelDecrease = Math.min((int) (maxAmount / FluidConstants.BOTTLE), this.fillLevel);
        if (levelDecrease <= 0) {
            return 0;
        }
        this.fillLevel -= levelDecrease;
        long extractedAmount = (long) levelDecrease * FluidConstants.BOTTLE;
        this.amount -= extractedAmount;
        if (this.amount <= 0 || this.fillLevel <= 0) {
            this.fillLevel = 0;
            this.qualityLevel = 0;
            this.amount = 0;
            this.variant = this.getBlankVariant();
        }
        ItemStack newStack = this.context.getItemVariant().toStack();
        if (this.fillLevel <= 0) {
            newStack.remove(FlaskItems.FLASK_DATA);
            newStack.remove(DataComponents.CONSUMABLE);
        } else {
            FlaskItems.setFill(newStack, this.fillLevel, this.qualityLevel);
        }
        if (this.context.exchange(ItemVariant.of(newStack), 1, transaction) == 1) {
            return extractedAmount;
        }
        return 0;
    }

    @Override
    protected ResourceAmount<FluidVariant> createSnapshot() {
        ResourceAmount<FluidVariant> snapshot = super.createSnapshot();
        this.qualitySnapshots.put(snapshot, this.qualityLevel);
        return snapshot;
    }

    @Override
    protected void readSnapshot(ResourceAmount<FluidVariant> snapshot) {
        super.readSnapshot(snapshot);
        this.fillLevel = Math.min((int) (this.amount / FluidConstants.BOTTLE), this.flaskCapacity);
        Integer quality = this.qualitySnapshots.get(snapshot);
        this.qualityLevel = quality != null ? quality : 0;
    }

    @Override
    protected void releaseSnapshot(ResourceAmount<FluidVariant> snapshot) {
        this.qualitySnapshots.remove(snapshot);
        super.releaseSnapshot(snapshot);
    }
}
