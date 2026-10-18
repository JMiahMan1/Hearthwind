package net.smitherz.screen;

import java.util.ArrayList;
import java.util.List;
import net.libz.access.ScreenHandlerAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.smitherz.init.ScreenInit;
import net.smitherz.init.TagInit;
import net.smitherz.item.Upgradeable;
import net.smitherz.util.UpgradeHelper;

public class GrinderScreenHandler extends AbstractContainerMenu implements ScreenHandlerAccess {
   private final Container result = new ResultContainer();
   final Container input = new SimpleContainer(2) {
      public void setChanged() {
         super.setChanged();
         GrinderScreenHandler.this.slotsChanged(this);
      }
   };
   private final ContainerLevelAccess context;
   private BlockPos pos;
   private final List<net.minecraft.world.item.ItemStack> unlinkedItemStacks = new ArrayList<>();

   public GrinderScreenHandler(int syncId, Inventory playerInventory, ContainerLevelAccess context) {
      super(ScreenInit.GRINDER_SCREEN_HANDLER_TYPE, syncId);
      this.context = context;
      this.addSlot(new Slot(this.input, 0, 49, 40) {
         public boolean mayPlace(ItemStack stack) {
            return net.smitherz.util.UpgradeHelper.isUpgradeable(stack);
         }
      });
      this.addSlot(new Slot(this.input, 1, 49, 19) {
         public boolean mayPlace(ItemStack stack) {
            return stack.is(TagInit.EXTRACTION_ITEMS);
         }
      });
      this.addSlot(new Slot(this.result, 2, 129, 34) {
         public boolean mayPlace(ItemStack stack) {
            return false;
         }

         public void onTake(Player player, ItemStack stack) {
            context.execute((world, pos) -> world.levelEvent(1042, pos, 0));
            GrinderScreenHandler.this.input.setItem(0, ItemStack.EMPTY);
            GrinderScreenHandler.this.input.getItem(1).shrink(1);

            for (ItemStack unlinkedItemStack : GrinderScreenHandler.this.unlinkedItemStacks) {
               playerInventory.placeItemBackInInventory(unlinkedItemStack);
            }
         }
      });

      for (int i = 0; i < 3; i++) {
         for (int j = 0; j < 9; j++) {
            this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
         }
      }

      for (int var6 = 0; var6 < 9; var6++) {
         this.addSlot(new Slot(playerInventory, var6, 8 + var6 * 18, 142));
      }

      this.context.execute((world, pos) -> this.setPos(pos));
   }

   public void slotsChanged(Container inventory) {
      super.slotsChanged(inventory);
      if (inventory == this.input) {
         this.updateResult();
      }
   }

   private void updateResult() {
      ItemStack upgradeable = this.input.getItem(0);
      this.unlinkedItemStacks.clear();
      if (!upgradeable.isEmpty()) {
         List<net.minecraft.world.item.ItemStack> resultStacks = UpgradeHelper.tryRemoveStackFromUpgradeable(upgradeable, this.input.getItem(1));
         if (!resultStacks.isEmpty()) {
            this.result.setItem(0, (ItemStack)resultStacks.get(0));
            if (resultStacks.size() > 1) {
               for (int i = 1; i < resultStacks.size(); i++) {
                  this.unlinkedItemStacks.add(resultStacks.get(i));
               }
            }
         }
      } else {
         this.result.setItem(0, ItemStack.EMPTY);
      }

      this.broadcastChanges();
   }

   public void removed(Player player) {
      super.removed(player);
      this.context.execute((world, pos) -> this.clearContainer(player, this.input));
   }

   public boolean stillValid(Player player) {
      return stillValid(this.context, player, Blocks.GRINDSTONE);
   }

   public ItemStack quickMoveStack(Player player, int slotId) {
      ItemStack itemStack = ItemStack.EMPTY;
      Slot slot = (Slot)this.slots.get(slotId);
      if (slot != null && slot.hasItem()) {
         ItemStack itemStack2 = slot.getItem();
         itemStack = itemStack2.copy();
         ItemStack itemStack3 = this.input.getItem(0);
         ItemStack itemStack4 = this.input.getItem(1);
         if (slotId == 2) {
            if (!this.moveItemStackTo(itemStack2, 3, 39, true)) {
               return ItemStack.EMPTY;
            }

            slot.onQuickCraft(itemStack2, itemStack);
         } else {
            if (slotId != 0 && slotId != 1
               ? (
                  !itemStack3.isEmpty() && !itemStack4.isEmpty()
                     ? (
                        slotId >= 3 && slotId < 30
                           ? !this.moveItemStackTo(itemStack2, 30, 39, false)
                           : slotId >= 30 && slotId < 39 && !this.moveItemStackTo(itemStack2, 3, 30, false)
                     )
                     : !this.moveItemStackTo(itemStack2, 0, 2, false)
               )
               : !this.moveItemStackTo(itemStack2, 3, 39, false)) {
               return ItemStack.EMPTY;
            }

            if (itemStack2.is(TagInit.EXTRACTION_ITEMS) && this.moveItemStackTo(itemStack2, 1, 2, false)) {
               return ItemStack.EMPTY;
            }

            if (this.input.getItem(0).isEmpty() && net.smitherz.util.UpgradeHelper.isUpgradeable(itemStack2)) {
               this.input.setItem(0, itemStack2);
               return ItemStack.EMPTY;
            }
         }

         if (itemStack2.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }

         if (itemStack2.getCount() == itemStack.getCount()) {
            return ItemStack.EMPTY;
         }

         slot.onTake(player, itemStack2);
      }

      return itemStack;
   }

   public void setPos(BlockPos pos) {
      this.pos = pos;
   }

   public BlockPos getPos() {
      return this.pos;
   }
}
