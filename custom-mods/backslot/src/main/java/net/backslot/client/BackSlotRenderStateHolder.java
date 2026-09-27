package net.backslot.client;

import net.minecraft.world.item.ItemStack;

/** Carried on {@code AvatarRenderState} so the slot layers can read the items. */
public interface BackSlotRenderStateHolder {

    ItemStack backslot$backItem();

    void backslot$setBackItem(ItemStack stack);

    ItemStack backslot$beltItem();

    void backslot$setBeltItem(ItemStack stack);
}
