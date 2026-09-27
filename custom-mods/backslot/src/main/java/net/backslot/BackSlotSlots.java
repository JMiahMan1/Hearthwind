package net.backslot;

import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

/** Attachment-backed storage helpers shared by the menu, network, HUD and layers. */
public final class BackSlotSlots {

    private BackSlotSlots() {}

    public static AttachmentType<ItemStack> typeFor(int slot) {
        return slot == BackSlot.BELT_SLOT ? BackSlot.BELT_ITEM : BackSlot.BACK_ITEM;
    }

    public static ItemStack get(Entity entity, int slot) {
        return entity.getAttachedOrElse(typeFor(slot), ItemStack.EMPTY);
    }

    public static void set(Entity entity, int slot, ItemStack stack) {
        entity.setAttached(typeFor(slot), stack);
    }

    /**
     * Upstream allows anything in either slot for our item set (its only
     * rejections are Medieval Weapons classes, which Hearthwind does not
     * ship).
     */
    public static boolean isItemAllowed(ItemStack stack, int slot) {
        return slot == BackSlot.BACK_SLOT || slot == BackSlot.BELT_SLOT;
    }
}
