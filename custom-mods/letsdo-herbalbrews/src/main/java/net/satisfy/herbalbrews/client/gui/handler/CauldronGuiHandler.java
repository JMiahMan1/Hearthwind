package net.satisfy.herbalbrews.client.gui.handler;

import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.satisfy.herbalbrews.core.registry.ObjectRegistry;
import net.satisfy.herbalbrews.core.registry.MenuTypeRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class CauldronGuiHandler extends AbstractContainerMenu {
    private final Container container;
    private final ContainerData data;

    public CauldronGuiHandler(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, new SimpleContainer(5), new SimpleContainerData(2));
    }

    public CauldronGuiHandler(int syncId, Inventory playerInventory, Container container, ContainerData data) {
        super(MenuTypeRegistry.CAULDRON_SCREEN_HANDLER.get(), syncId);
        this.container = container;
        this.data = data;
        addDataSlots(this.data);
        addSlot(new PotionSlot(container, 0, 57, 16));
        addSlot(new PotionSlot(container, 1, 79, 22));
        addSlot(new PotionSlot(container, 2, 101, 16));
        addSlot(new Slot(container, 3, 79, 58) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        addSlot(new FuelSlot(container, 4, 148, 42));
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int i = 0; i < 9; i++) {
            addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    public int getScaledProgress(int maxHeight) {
        int progress = data.get(0);
        int total = data.get(1);
        if (progress == 0 || total == 0) {
            return 0;
        }
        return progress * maxHeight / total;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = (Slot)this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemStack2 = slot.getItem();
            itemStack = itemStack2.copy();
            if ((index < 0 || index > 2) && index != 3 && index != 4) {
                if (FuelSlot.mayPlaceItem(itemStack)) {
                    if (this.moveItemStackTo(itemStack2, 4, 5, false) && !this.moveItemStackTo(itemStack2, 3, 4, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (PotionSlot.mayPlaceItem(itemStack)) {
                    if (!this.moveItemStackTo(itemStack2, 0, 3, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= 5 && index < 32) {
                    if (!this.moveItemStackTo(itemStack2, 32, 41, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= 32 && index < 41) {
                    if (!this.moveItemStackTo(itemStack2, 5, 32, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(itemStack2, 5, 41, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(itemStack2, 5, 41, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(itemStack2, itemStack);
            }

            if (itemStack2.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemStack2.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemStack);
        }

        return itemStack;
    }

    public int getCookingTime() {
        return data.get(0);
    }

    public int getRequiredDuration() {
        return data.get(1);
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    private static class PotionSlot extends Slot{

        public PotionSlot(Container container, int i, int j, int k) {
            super(container, i, j, k);
        }

        public boolean mayPlace(ItemStack itemStack) {
            return mayPlaceItem(itemStack);
        }

        public int getMaxStackSize() {
            return 1;
        }

        public void onTake(Player player, ItemStack itemStack) {
            Optional<Holder<Potion>> optional = itemStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion();
            if (optional.isPresent() && player instanceof ServerPlayer serverPlayer) {
                CriteriaTriggers.BREWED_POTION.trigger(serverPlayer, optional.get());
            }

            super.onTake(player, itemStack);
        }

        public static boolean mayPlaceItem(ItemStack itemStack) {
            return itemStack.getItem() instanceof PotionItem || itemStack.is(Items.POTION) || itemStack.is(Items.SPLASH_POTION) || itemStack.is(Items.LINGERING_POTION) || itemStack.is(Items.GLASS_BOTTLE);
        }
    }

    private static class FuelSlot extends Slot {

        public FuelSlot(Container container, int i, int j, int k) {
            super(container, i, j, k);
        }

        public boolean mayPlace(ItemStack itemStack) {
            return mayPlaceItem(itemStack);
        }

        public static boolean mayPlaceItem(ItemStack itemStack) {
            return itemStack.is(ObjectRegistry.HERBAL_INFUSION.get());
        }
    }
}
