package net.backslot.slot;

import net.backslot.BackSlot;
import net.backslot.BackSlotSlots;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** The belt slot in the player inventory menu (Aged positions from config). */
public class BeltItemSlot extends Slot {

    public BeltItemSlot(Container container, int x, int y) {
        super(container, 0, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return BackSlotSlots.isItemAllowed(stack, BackSlot.BELT_SLOT);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
