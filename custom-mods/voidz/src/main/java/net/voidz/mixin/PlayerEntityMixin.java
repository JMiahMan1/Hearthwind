package net.voidz.mixin;

import net.minecraft.world.entity.player.Player;
import net.voidz.init.DimensionInit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Player.class})
public class PlayerEntityMixin {
   @Inject(
      method = {"mayUseItemAt"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void canPlaceOnMixin(
      net.minecraft.core.BlockPos pos,
      net.minecraft.core.Direction facing,
      net.minecraft.world.item.ItemStack stack,
      CallbackInfoReturnable<Boolean> info
   ) {
      Player playerEntity = (Player)(Object)this;
      if (playerEntity != null && !playerEntity.isCreative() && playerEntity.level().dimension() == DimensionInit.VOID_WORLD) {
         info.setReturnValue(false);
      }
   }
}
