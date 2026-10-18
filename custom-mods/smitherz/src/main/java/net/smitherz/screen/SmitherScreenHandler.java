package net.smitherz.screen;

import java.util.List;
import net.libz.access.ScreenHandlerAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.smitherz.init.ConfigInit;
import net.smitherz.init.ItemInit;
import net.smitherz.init.ScreenInit;
import net.smitherz.init.SoundInit;
import net.smitherz.init.TagInit;
import net.smitherz.item.Gem;
import net.smitherz.item.Upgradeable;
import net.smitherz.item.component.GemComponent;
import net.smitherz.network.SmitherServerPacket;
import net.smitherz.screen.widget.GemSlot;
import net.smitherz.util.UpgradeHelper;

public class SmitherScreenHandler extends AbstractContainerMenu implements ScreenHandlerAccess {
   private final Container inventory = new SimpleContainer(ConfigInit.CONFIG.maxGemSlots + 2) {
      public void setChanged() {
         super.setChanged();
         SmitherScreenHandler.this.slotsChanged(this);
      }
   };
   private final ContainerLevelAccess context;
   private final Player player;
   private BlockPos pos;

   public SmitherScreenHandler(int syncId, Inventory playerInventory, ContainerLevelAccess context) {
      super(ScreenInit.SMITHER_SCREEN_HANDLER_TYPE, syncId);
      this.context = context;
      this.player = playerInventory.player;
      this.addSlot(new Slot(this.inventory, 0, 80, 19) {
         public boolean mayPlace(ItemStack stack) {
            return net.smitherz.util.UpgradeHelper.isUpgradeable(stack);
         }

         public void onTake(Player player, ItemStack stack) {
            if (!player.level().isClientSide()) {
               for (int i = 0; i < this.container.getContainerSize() - 2; i++) {
                  if (!SmitherScreenHandler.this.getSlot(i + 2).getItem().isEmpty()) {
                     if (((GemSlot)SmitherScreenHandler.this.getSlot(i + 2)).isLinked()) {
                        SmitherScreenHandler.this.getSlot(i + 2).setByPlayer(ItemStack.EMPTY);
                        ((GemSlot)SmitherScreenHandler.this.getSlot(i + 2)).setLinked(false);
                     } else {
                        playerInventory.placeItemBackInInventory(SmitherScreenHandler.this.getSlot(i + 2).getItem());
                     }
                  }
               }
            }

            super.onTake(player, stack);
         }

         public void setByPlayer(ItemStack stack) {
            if (!stack.isEmpty()) {
               SmitherScreenHandler.this.updateGemSlots(stack);
            }

            super.setByPlayer(stack);
         }
      });
      this.addSlot(new Slot(this.inventory, 1, 106, 19) {
         public boolean mayPlace(ItemStack stack) {
            return stack.is(TagInit.BONUS_ITEMS);
         }
      });

      for (int i = 2; i < this.inventory.getContainerSize(); i++) {
         this.addSlot(new GemSlot(this.inventory, i, 89 - this.inventory.getContainerSize() / 2 * 18 + i * 18 - 18, 43));
         ((GemSlot)this.getSlot(i)).setEnabled(false);
      }

      for (int i = 0; i < 3; i++) {
         for (int j = 0; j < 9; j++) {
            this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
         }
      }

      for (int var7 = 0; var7 < 9; var7++) {
         this.addSlot(new Slot(playerInventory, var7, 8 + var7 * 18, 142));
      }

      this.context.execute((world, pos) -> this.setPos(pos));
   }

   public void slotsChanged(Container inventory) {
      super.slotsChanged(inventory);
      if (inventory == this.inventory) {
         this.updateResult();
      }
   }

   private void updateGemSlots(ItemStack upgradeable) {
      int gemSlots = ((Upgradeable)upgradeable.getItem()).gemSlots(upgradeable);
      List<net.minecraft.world.item.ItemStack> linkedStacks = UpgradeHelper.getGemStacks(upgradeable).toList();

      for (int i = 0; i < this.inventory.getContainerSize() - 2; i++) {
         ((GemSlot)this.getSlot(i + 2)).setEnabled(i < gemSlots);
         boolean linked = i < linkedStacks.size();
         ((GemSlot)this.getSlot(i + 2)).setLinked(linked);
         if (linked) {
            this.getSlot(i + 2).setByPlayer((ItemStack)linkedStacks.get(i));
         }
      }
   }

   private void updateResult() {
      if (!this.player.level().isClientSide()) {
         boolean disableButton = true;
         boolean hasUpgradeable = this.getSlot(0).hasItem() && net.smitherz.util.UpgradeHelper.isUpgradeable(this.getSlot(0).getItem());
         if (hasUpgradeable && this.hasUnlinkedGem()) {
            disableButton = false;
         }

         if (!disableButton && !this.canLinkSameGem()) {
            disableButton = true;
         }

         SmitherServerPacket.writeS2CSmitherReadyPacket((ServerPlayer)this.player, disableButton);
      }
   }

   public void smith() {
      if (this.getSlot(0).hasItem() && !this.getUnlickedGem().isEmpty() && net.smitherz.util.UpgradeHelper.isUpgradeable(this.getSlot(0).getItem())) {
         boolean linkageSuccess = UpgradeHelper.tryAddStackToUpgradeable(
            this.getSlot(0).getItem(), this.getUnlickedGem(), this.getSlot(1).getItem()
         );
         this.context
            .execute(
               (world, pos) -> {
                  if (world instanceof ServerLevel serverWorld) {
                     serverWorld.playSeededSound(
                        null,
                        pos.getX(),
                        pos.getY(),
                        pos.getZ(),
                        linkageSuccess ? SoundInit.LINKAGE_SUCCESS_EVENT : SoundInit.LINKAGE_FAILURE_EVENT,
                        SoundSource.BLOCKS,
                        1.0F,
                        0.8F + 0.4F * serverWorld.getRandom().nextFloat(),
                        serverWorld.getRandom().nextLong()
                     );
                  }
               }
            );
         this.updateGemSlots(this.getSlot(0).getItem());
         if (!this.getSlot(0).hasItem()) {
            this.context
               .execute(
                  (world, pos) -> {
                     if (world instanceof ServerLevel serverWorld) {
                        serverWorld.playSeededSound(
                           null,
                           pos.getX(),
                           pos.getY(),
                           pos.getZ(),
                           SoundEvents.ITEM_BREAK,
                           SoundSource.BLOCKS,
                           1.0F,
                           1.0F,
                           serverWorld.getRandom().nextLong()
                        );
                     }
                  }
               );
         }
      }
   }

   private ItemStack getUnlickedGem() {
      for (int i = 2; i < this.inventory.getContainerSize(); i++) {
         if (!this.getSlot(i).getItem().isEmpty() && !((GemSlot)this.getSlot(i)).isLinked()) {
            return this.getSlot(i).getItem();
         }
      }

      return ItemStack.EMPTY;
   }

   public boolean canLinkSameGem() {
      if (!ConfigInit.CONFIG.canLinkSameGem && this.getSlot(0).getItem().get(ItemInit.GEMS) != null) {
         Item gem = this.getUnlickedGem().getItem();

         for (int i = 0; i < ((GemComponent)this.getSlot(0).getItem().get(ItemInit.GEMS)).gems().size(); i++) {
            if (((ItemStack)((GemComponent)this.getSlot(0).getItem().get(ItemInit.GEMS)).gems().get(i)).is(gem)) {
               return false;
            }
         }
      }

      return true;
   }

   private boolean hasUnlinkedGem() {
      for (int i = 2; i < this.inventory.getContainerSize(); i++) {
         if (((GemSlot)this.getSlot(i)).isActive() && !this.getSlot(i).getItem().isEmpty() && !((GemSlot)this.getSlot(i)).isLinked()) {
            return true;
         }
      }

      return false;
   }

   private boolean isUnlinkedGem(int slot) {
      return slot == 0 || slot == 1 ? true : this.getSlot(slot) instanceof GemSlot && !((GemSlot)this.getSlot(slot)).isLinked();
   }

   public void removed(Player player) {
      super.removed(player);
      this.context.execute((world, pos) -> this.clearContainer(player, this.inventory));
   }

   protected void clearContainer(Player player, Container inventory) {
      if (!player.isAlive() || player instanceof ServerPlayer && ((ServerPlayer)player).hasDisconnected()) {
         for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (this.isUnlinkedGem(i)) {
               player.drop(inventory.removeItemNoUpdate(i), false);
            }
         }
      } else {
         for (int ix = 0; ix < inventory.getContainerSize(); ix++) {
            Inventory playerInventory = player.getInventory();
            if (playerInventory.player instanceof ServerPlayer && this.isUnlinkedGem(ix)) {
               playerInventory.placeItemBackInInventory(inventory.removeItemNoUpdate(ix));
            }
         }
      }
   }

   public boolean stillValid(Player player) {
      return (Boolean)this.context
         .evaluate((world, pos) -> player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0, true);
   }

   public ItemStack quickMoveStack(Player player, int index) {
      ItemStack itemStack = ItemStack.EMPTY;
      Slot slot = (Slot)this.slots.get(index);
      if (slot != null && slot.hasItem()) {
         ItemStack itemStack2 = slot.getItem();
         itemStack = itemStack2.copy();
         if (index == 0) {
            if (!this.moveItemStackTo(itemStack2, 8, 39, true)) {
               return ItemStack.EMPTY;
            }

            slot.onQuickCraft(itemStack2, itemStack);
         } else if (index > 0 && index < 8) {
            if (!this.moveItemStackTo(itemStack2, 8, 39, false)) {
               return ItemStack.EMPTY;
            }
         } else if (index >= 8 && index < 44) {
            if (itemStack.is(TagInit.BONUS_ITEMS) && !this.moveItemStackTo(itemStack2, 1, 2, false)) {
               return ItemStack.EMPTY;
            }

            if (this.getSlot(0).getItem().isEmpty()) {
               if (net.smitherz.util.UpgradeHelper.isUpgradeable(itemStack) && !this.moveItemStackTo(itemStack2, 0, 1, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (itemStack.getItem() instanceof Gem && !this.insertGem(itemStack2, 2, 8)) {
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

   private boolean insertGem(ItemStack stack, int startIndex, int endIndex) {
      boolean bl = false;
      if (!stack.isEmpty()) {
         for (int i = startIndex; i < endIndex; i++) {
            Slot slot = (Slot)this.slots.get(i);
            ItemStack itemStack = slot.getItem();
            if (itemStack.isEmpty() && slot.mayPlace(stack)) {
               if (stack.getCount() > slot.getMaxStackSize()) {
                  slot.setByPlayer(stack.split(slot.getMaxStackSize()));
               } else {
                  slot.setByPlayer(stack.split(stack.getCount()));
               }

               slot.setChanged();
               bl = true;
               break;
            }
         }
      }

      return bl;
   }

   public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
      return slot.getContainerSlot() == 0
            || !this.getSlot(0).getItem().isEmpty() && UpgradeHelper.getGemSlotSize(this.getSlot(0).getItem()) >= slot.getContainerSlot()
         ? slot.container != this.inventory && super.canTakeItemForPickAll(stack, slot)
         : false;
   }

   public void setPos(BlockPos pos) {
      this.pos = pos;
   }

   public BlockPos getPos() {
      return this.pos;
   }
}
