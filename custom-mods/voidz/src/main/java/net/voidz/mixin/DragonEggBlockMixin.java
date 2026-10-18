package net.voidz.mixin;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.DragonEggBlock;
import net.voidz.init.DimensionInit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({DragonEggBlock.class})
public class DragonEggBlockMixin {
   @Inject(
      method = {"useWithoutItem"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onUseMixin(
      net.minecraft.world.level.block.state.BlockState state,
      net.minecraft.world.level.Level world,
      net.minecraft.core.BlockPos pos,
      net.minecraft.world.entity.player.Player player,
      net.minecraft.world.phys.BlockHitResult hit,
      CallbackInfoReturnable<net.minecraft.world.InteractionResult> info
   ) {
      if (!world.isClientSide() && world.dimension() == DimensionInit.VOID_WORLD) {
         world.removeBlock(pos, false);
         ItemStack itemStack = new ItemStack(Items.DRAGON_EGG);
         if (!player.getInventory().add(itemStack)) {
            player.drop(itemStack, false);
         }

         info.setReturnValue(InteractionResult.SUCCESS_SERVER);
      }
   }
}
