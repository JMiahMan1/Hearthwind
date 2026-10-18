package net.medievalweapons.mixin.client;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.medievalweapons.init.ItemInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin({AbstractClientPlayer.class})
public abstract class AbstractClientPlayerEntityMixin extends Player {
   public AbstractClientPlayerEntityMixin(Level world, GameProfile gameProfile) {
      super(world, gameProfile);
   }

   @Inject(
      method = {"getFieldOfViewModifier"},
      at = {@At("TAIL")},
      cancellable = true
   )
   private void getFovMultiplierMixin(boolean bl, float f, CallbackInfoReturnable<Float> info) {
      Item item = this.getUseItem().getItem();
      if (this.isUsingItem() && (item == ItemInit.LONG_BOW_ITEM || item == ItemInit.RECURVE_BOW_ITEM)) {
         int i = this.getTicksUsingItem();
         float g;
         if (item == ItemInit.LONG_BOW_ITEM) {
            g = i / 60.0F;
         } else {
            g = i / 12.0F;
         }

         if (g > 1.0F) {
            g = 1.0F;
         } else {
            g *= g;
         }

         f *= 1.0F - g * 0.15F;
         info.setReturnValue(Mth.lerp(((Double)Minecraft.getInstance().options.fovEffectScale().get()).floatValue(), 1.0F, f));
      }
   }
}
