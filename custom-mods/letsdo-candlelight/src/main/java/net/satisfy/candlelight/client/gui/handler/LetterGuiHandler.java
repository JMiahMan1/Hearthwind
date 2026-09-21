package net.satisfy.candlelight.client.gui.handler;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.satisfy.candlelight.core.registry.ObjectRegistry;
import net.satisfy.candlelight.core.registry.ScreenHandlerTypeRegistry;
import org.jetbrains.annotations.NotNull;

public class LetterGuiHandler extends AbstractContainerMenu {
    private final Container inventory;
    public static String name = "";
    private final Player player;

    public LetterGuiHandler(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, new SimpleContainer(3));
    }

    public LetterGuiHandler(int syncId, Inventory playerInventory, Container inventory) {
        super(ScreenHandlerTypeRegistry.LETTER_SCREEN_HANDLER.get(), syncId);
        checkContainerSize(inventory, 3);
        this.inventory = inventory;
        this.player = playerInventory.player;
        inventory.startOpen(this.player);

        this.addSlot(new Slot(inventory, 0, 33, 13));
        this.addSlot(new Slot(inventory, 1, 33, 50));
        this.addSlot(new OutputSlot(inventory, 2, 110, 48, this));

        for (int m = 0; m < 3; ++m) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, 84 + m * 18));
            }
        }
        for (int m = 0; m < 9; ++m) {
            this.addSlot(new Slot(playerInventory, m, 8 + m * 18, 142));
        }
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        Item a = this.inventory.getItem(0).getItem();
        Item b = this.inventory.getItem(1).getItem();

        if (((a == ObjectRegistry.LETTER_OPEN.get()) || (a == ObjectRegistry.LOVE_LETTER_OPEN.get())) && b == ObjectRegistry.NOTE_PAPER_WRITTEN.get()) {
            ItemStack stack = a == ObjectRegistry.LETTER_OPEN.get() ? new ItemStack(ObjectRegistry.LETTER_CLOSED.get()) : new ItemStack(ObjectRegistry.LOVE_LETTER_CLOSED.get());
            CustomData noteData = this.inventory.getItem(1).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            CompoundTag payload = noteData.copyTag();
            CompoundTag sealed = new CompoundTag();
            sealed.put("sealed_payload", payload);
            sealed.put("letter_title", StringTag.valueOf(name));
            sealed.put("letter_sender", StringTag.valueOf(this.player.getName().getString()));
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(sealed));
            this.inventory.setItem(2, stack);
        } else if (b == ObjectRegistry.LETTER_OPEN.get() && a == ObjectRegistry.NOTE_PAPER_WRITTEN.get()) {
            ItemStack stack = new ItemStack(ObjectRegistry.LETTER_CLOSED.get());
            CustomData noteData = this.inventory.getItem(0).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            CompoundTag payload = noteData.copyTag();
            CompoundTag sealed = new CompoundTag();
            sealed.put("sealed_payload", payload);
            sealed.put("letter_title", StringTag.valueOf(name));
            sealed.put("letter_sender", StringTag.valueOf(this.player.getName().getString()));
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(sealed));
            this.inventory.setItem(2, stack);
        } else {
            this.inventory.setItem(2, ItemStack.EMPTY);
        }
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (player instanceof ServerPlayer) {
            for (int i = 0; i < this.inventory.getContainerSize(); i++) {
                ItemStack stack = this.inventory.getItem(i);
                if (!stack.isEmpty()) {
                    if (player.isAlive() && !((ServerPlayer) player).hasDisconnected()) {
                        player.getInventory().placeItemBackInInventory(stack);
                    } else {
                        player.drop(stack, false);
                    }
                }
            }
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.inventory.stillValid(player);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot instanceof OutputSlot) return ItemStack.EMPTY;

        if (slot.hasItem()) {
            ItemStack original = slot.getItem();
            ItemStack copy = original.copy();

            if (index < this.inventory.getContainerSize()) {
                if (!this.moveItemStackTo(original, this.inventory.getContainerSize(), this.slots.size(), true))
                    return ItemStack.EMPTY;
            } else if (!this.moveItemStackTo(original, 0, this.inventory.getContainerSize(), false)) {
                return ItemStack.EMPTY;
            }

            if (original.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            return copy;
        }
        return ItemStack.EMPTY;
    }

    public static class OutputSlot extends Slot {
        private final LetterGuiHandler container;

        public OutputSlot(Container inventory, int index, int x, int y, LetterGuiHandler container) {
            super(inventory, index, x, y);
            this.container = container;
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            for (int i = 0; i <= 1; i++) {
                ItemStack input = this.container.inventory.getItem(i);
                if (!input.isEmpty()) {
                    input.shrink(1);
                    this.container.inventory.setItem(i, input.isEmpty() ? ItemStack.EMPTY : input);
                }
            }
            super.onTake(player, stack);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}