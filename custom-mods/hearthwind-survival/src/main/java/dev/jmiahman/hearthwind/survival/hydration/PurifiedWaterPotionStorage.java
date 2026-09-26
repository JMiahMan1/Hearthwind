package dev.jmiahman.hearthwind.survival.hydration;

import dev.jmiahman.hearthwind.survival.PurifiedWater;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ExtractionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;

/**
 * Port of Dehydration 1.4.1 {@code PurifiedWaterPotionStorage}: a bottle of
 * {@code dehydration:purified_water} can be poured back into a cauldron as
 * one bottle of purified fluid, leaving a glass bottle.
 */
public class PurifiedWaterPotionStorage implements ExtractionOnlyStorage<FluidVariant>,
        SingleSlotStorage<FluidVariant> {
    private static final FluidVariant CONTAINED_FLUID = FluidVariant.of(PurifiedWater.STILL);
    private static final long CONTAINED_AMOUNT = FluidConstants.BOTTLE;

    private final ContainerItemContext context;

    private PurifiedWaterPotionStorage(ContainerItemContext context) {
        this.context = context;
    }

    public static PurifiedWaterPotionStorage find(ContainerItemContext context) {
        return isPurifiedWaterPotion(context) ? new PurifiedWaterPotionStorage(context) : null;
    }

    private static boolean isPurifiedWaterPotion(ContainerItemContext context) {
        ItemVariant variant = context.getItemVariant();
        if (!variant.isOf(Items.POTION)) {
            return false;
        }
        PotionContents contents = variant.toStack().get(DataComponents.POTION_CONTENTS);
        return contents != null && contents.is(PurifiedWater.PURIFIED_POTION);
    }

    private boolean isPurifiedWaterPotion() {
        return isPurifiedWaterPotion(this.context);
    }

    private ItemVariant mapToGlassBottle() {
        ItemStack newStack = this.context.getItemVariant().toStack();
        newStack.set(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        return ItemVariant.of(Items.GLASS_BOTTLE, newStack.getComponentsPatch());
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        if (!this.isPurifiedWaterPotion()) {
            return 0;
        }
        if (resource.equals(CONTAINED_FLUID) && maxAmount >= CONTAINED_AMOUNT) {
            if (this.context.exchange(mapToGlassBottle(), 1, transaction) == 1) {
                return CONTAINED_AMOUNT;
            }
        }
        return 0;
    }

    @Override
    public boolean isResourceBlank() {
        return this.getResource().isBlank();
    }

    @Override
    public FluidVariant getResource() {
        return this.isPurifiedWaterPotion() ? CONTAINED_FLUID : FluidVariant.blank();
    }

    @Override
    public long getAmount() {
        return this.isPurifiedWaterPotion() ? CONTAINED_AMOUNT : 0;
    }

    @Override
    public long getCapacity() {
        return this.getAmount();
    }

    @Override
    public String toString() {
        return "PurifiedWaterPotionStorage[" + this.context + "]";
    }
}
