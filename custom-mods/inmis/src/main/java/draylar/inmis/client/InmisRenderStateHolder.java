package draylar.inmis.client;

import net.minecraft.world.item.ItemStack;

/**
 * Carried on {@code AvatarRenderState} so the backpack layer can read the
 * chest item without going through the armor-equipment filter (backpacks
 * deliberately have no equipment asset: an asset would hide capes and make
 * the armor layer try to draw them).
 */
public interface InmisRenderStateHolder {
    ItemStack inmis$chestItem();

    void inmis$setChestItem(ItemStack stack);
}
