package net.smitherz.screen.widget;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.smitherz.init.TagInit;
import net.smitherz.item.Gem;

public class GemSlot extends Slot {
   private boolean enabled;
   private boolean linked;

   public GemSlot(Container inventory, int index, int x, int y) {
      super(inventory, index, x, y);
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   public void setLinked(boolean linked) {
      this.linked = linked;
   }

   public boolean isLinked() {
      return this.linked;
   }

   public boolean isActive() {
      return this.enabled;
   }

   public boolean mayPickup(Player playerEntity) {
      return !this.linked;
   }

   public boolean mayPlace(ItemStack stack) {
      return stack.getItem() instanceof Gem gem && !gem.canLinkToItemStack(this.container.getItem(0))
         ? false
         : this.enabled && !this.linked && (stack.getItem() instanceof Gem || stack.is(TagInit.GEMS));
   }

   public int getMaxStackSize() {
      return 1;
   }
}
