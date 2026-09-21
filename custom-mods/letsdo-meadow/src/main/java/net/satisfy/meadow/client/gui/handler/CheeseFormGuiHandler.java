package net.satisfy.meadow.client.gui.handler;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.satisfy.meadow.core.registry.ScreenHandlerRegistry;
import org.jetbrains.annotations.NotNull;

public class CheeseFormGuiHandler extends AbstractContainerMenu {

    private final ContainerData propertyDelegate;

    public CheeseFormGuiHandler(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, new SimpleContainer(3), new SimpleContainerData(2));
    }

    public CheeseFormGuiHandler(int syncId, Inventory playerInventory, Container inventory, ContainerData propertyDelegate) {
        super(ScreenHandlerRegistry.CHEESE_FORM_SCREEN_HANDLER.get(), syncId);
        this.propertyDelegate = propertyDelegate;
        addDataSlots(propertyDelegate);
        buildBlockEntityContainer(playerInventory, inventory);
        buildPlayerContainer(playerInventory);
    }

    private void buildBlockEntityContainer(Inventory playerInventory, Container inventory) {
        this.addSlot(new FurnaceResultSlot(playerInventory.player, inventory, 0, 123, 34));
        this.addSlot(new Slot(inventory, 1, 33, 33));
        this.addSlot(new Slot(inventory, 2, 51, 33));
    }

    private void buildPlayerContainer(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    public int getScaledXProgress() {
        final int progress = this.propertyDelegate.get(0);
        final int totalProgress = this.propertyDelegate.get(1);
        if (progress <= 0 || totalProgress <= 0) {
            return 0;
        }
        return progress * 24 / totalProgress;
    }

    public int getScaledYProgress() {
        final int progress = this.propertyDelegate.get(0);
        final int totalProgress = this.propertyDelegate.get(1);
        if (progress <= 0 || totalProgress <= 0) {
            return 0;
        }
        return progress * 25 / totalProgress;
    }

    public int getCookingTime() {
        return propertyDelegate.get(0);
    }

    public int getRequiredDuration() {
        return propertyDelegate.get(1);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack originalStack = stack.copy();
        if (index == 0) {
            if (!this.moveItemStackTo(stack, 3, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, originalStack);
        } else if (index >= 3 && index < this.slots.size()) {
            if (!this.moveItemStackTo(stack, 1, 3, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index >= 1 && index < 3) {
            if (!this.moveItemStackTo(stack, 3, this.slots.size(), false)) {
                return ItemStack.EMPTY;
            }
        }
        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == originalStack.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return originalStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
