package net.smitherz.tooltip;

import net.minecraft.world.inventory.tooltip.TooltipComponent;

public class SmitherTooltipData implements TooltipComponent {
   private final net.minecraft.core.NonNullList<net.minecraft.world.item.ItemStack> inventory;
   private final int gemSlotSize;

   public SmitherTooltipData(net.minecraft.core.NonNullList<net.minecraft.world.item.ItemStack> inventory, int gemSlotSize) {
      this.inventory = inventory;
      this.gemSlotSize = gemSlotSize;
   }

   public net.minecraft.core.NonNullList<net.minecraft.world.item.ItemStack> getInventory() {
      return this.inventory;
   }

   public int getGemSlotSize() {
      return this.gemSlotSize;
   }
}
