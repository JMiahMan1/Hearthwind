package net.jmb19905.niftycarts.util;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;

public class ProxyItemUseContext extends UseOnContext {
   public ProxyItemUseContext(Player player, ItemStack itemstack, BlockHitResult rayTraceResultIn) {
      super(player.level(), player, InteractionHand.MAIN_HAND, itemstack, rayTraceResultIn);
   }
}
