package earth.terrarium.chipped.common.menus;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class WorkbenchCrafting {
    private WorkbenchCrafting() {}

    public static boolean replace(Container inventory, int selectedSlot, ItemStack selected, ItemStack output, List<ItemStack> results, boolean replaceAll) {
        if (selectedSlot < 0 || selectedSlot >= inventory.getContainerSize() || selected.isEmpty() || output.isEmpty()) return false;
        if (!ItemStack.isSameItemSameComponents(inventory.getItem(selectedSlot), selected)) return false;
        ItemStack result = results.stream().filter(candidate -> ItemStack.isSameItemSameComponents(candidate, output)).findFirst().orElse(ItemStack.EMPTY);
        if (result.isEmpty()) return false;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack current = inventory.getItem(i);
            if (i == selectedSlot || (replaceAll && ItemStack.isSameItem(current, selected))) {
                inventory.setItem(i, result.copyWithCount(current.getCount()));
            }
        }
        inventory.setChanged();
        return true;
    }
}
