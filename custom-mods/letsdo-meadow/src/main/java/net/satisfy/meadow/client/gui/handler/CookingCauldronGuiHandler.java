package net.satisfy.meadow.client.gui.handler;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.satisfy.meadow.core.registry.ObjectRegistry;
import net.satisfy.meadow.core.registry.ScreenHandlerRegistry;
import net.satisfy.meadow.core.registry.TagRegistry;
import org.jetbrains.annotations.NotNull;

public class CookingCauldronGuiHandler extends AbstractContainerMenu {
    private final Container container;
    private final ContainerData propertyDelegate;

    public CookingCauldronGuiHandler(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, new SimpleContainer(8), new SimpleContainerData(4));
    }

    public CookingCauldronGuiHandler(int syncId, Inventory playerInventory, Container container, ContainerData propertyDelegate) {
        super(ScreenHandlerRegistry.COOKING_CAULDRON_SCREEN_HANDLER.get(), syncId);
        this.container = container;
        this.propertyDelegate = propertyDelegate;
        addDataSlots(this.propertyDelegate);

        addSlot(new FurnaceResultSlot(playerInventory.player, container, 0, 107, 27));

        for (int i = 1; i <= 6; i++) {
            int x = 11 + ((i - 1) % 3) * 18;
            int y = 17 + ((i - 1) / 3) * 18;
            this.addSlot(new Slot(container, i, x, y));
        }

        addSlot(new Slot(container, 7, 134, 23) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(TagRegistry.SMALL_WATER_FILL) || stack.is(TagRegistry.LARGE_WATER_FILL);
            }
        });

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int i = 0; i < 9; i++) {
            addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    public boolean isBeingBurned() {
        return propertyDelegate.get(1) != 0;
    }

    public int getScaledProgress(int arrowWidth) {
        int progress = propertyDelegate.get(0);
        int total = propertyDelegate.get(3);
        if (total == 0) return 0;
        return progress * arrowWidth / total;
    }

    public int getFluidLevel() {
        return propertyDelegate.get(2);
    }

    public int getCookingTime() {
        return propertyDelegate.get(0);
    }

    public int getRequiredDuration() {
        return propertyDelegate.get(3);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack item = slot.getItem();
        ItemStack copy = item.copy();

        if (index == 0) { 
            if (!this.moveItemStackTo(item, 8, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(item, copy);
        } else if (index >= 1 && index <= 6) { 
            if (!this.moveItemStackTo(item, 8, this.slots.size(), false)) {
                return ItemStack.EMPTY;
            }
        } else if (index == 7) { 
            if (!this.moveItemStackTo(item, 8, this.slots.size(), false)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (this.moveItemStackTo(item, 1, 7, false)) {
            } else if (item.is(ObjectRegistry.WOODEN_WATER_BUCKET.get()) || item.is(Items.WATER_BUCKET)) {
                if (!this.moveItemStackTo(item, 7, 8, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                return ItemStack.EMPTY;
            }
        }
        if (item.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        slot.onTake(player, item);
        return copy;
    }


    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }
}
