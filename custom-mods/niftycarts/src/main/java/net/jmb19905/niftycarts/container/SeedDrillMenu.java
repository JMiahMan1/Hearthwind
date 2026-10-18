package net.jmb19905.niftycarts.container;

import net.jmb19905.niftycarts.NiftyCarts;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CropBlock;
import org.jetbrains.annotations.NotNull;

public class SeedDrillMenu extends AbstractContainerMenu {
   public static final int SLOT_COUNT = 9;
   private final Container container;

   public SeedDrillMenu(int i, Inventory inv) {
      this(i, inv, new SimpleContainer(9));
   }

   public SeedDrillMenu(int i, Inventory inv, Container container) {
      this(NiftyCarts.SEED_DRILL_MENU_TYPE, i, inv, container);
   }

   protected SeedDrillMenu(
      net.minecraft.world.inventory.MenuType<?> menuType,
      int id,
      net.minecraft.world.entity.player.Inventory inventory,
      net.minecraft.world.Container container
   ) {
      super(menuType, id);
      checkContainerSize(container, 9);
      this.container = container;
      container.startOpen(inventory.player);

      for (int i = 0; i < 9; i++) {
         this.addSlot(
            new Slot(container, i, 8 + 18 * i, 28) {
               public boolean mayPlace(@NotNull ItemStack itemStack) {
                  return itemStack.is(NiftyCarts.SEED_DRILL_PLANTABLE)
                     || itemStack.getItem() instanceof BlockItem item && item.getBlock() instanceof CropBlock;
               }
            }
         );
      }

      for (int i = 0; i < 3; i++) {
         for (int j = 0; j < 9; j++) {
            this.addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
         }
      }

      for (int k = 0; k < 9; k++) {
         this.addSlot(new Slot(inventory, k, 8 + k * 18, 142));
      }
   }

   @NotNull
   public ItemStack quickMoveStack(@NotNull Player player, int index) {
      ItemStack itemstack = ItemStack.EMPTY;
      Slot slot = (Slot)this.slots.get(index);
      if (slot.hasItem()) {
         ItemStack itemstack1 = slot.getItem();
         if (index < this.container.getContainerSize()) {
            if (!this.moveItemStackTo(itemstack1, this.container.getContainerSize(), this.slots.size(), true)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo(itemstack1, 0, this.container.getContainerSize(), false)) {
            return ItemStack.EMPTY;
         }

         if (itemstack1.isEmpty()) {
            slot.set(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }
      }

      return itemstack;
   }

   public void removed(@NotNull Player player) {
      super.removed(player);
      this.container.stopOpen(player);
   }

   public Container getContainer() {
      return this.container;
   }

   public boolean stillValid(@NotNull Player player) {
      return this.container.stillValid(player);
   }
}
