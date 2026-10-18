package net.voidz.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.Musics;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.phys.HitResult;
import net.voidz.init.DimensionInit;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin({Minecraft.class})
public class MinecraftClientMixin {
   @Shadow
   @Nullable
   public LocalPlayer player;
   @Shadow
   @Nullable
   public MultiPlayerGameMode gameMode;
   @Shadow
   public HitResult hitResult;

   @Inject(method = {"getSituationalMusic"}, at = {@At("HEAD")}, cancellable = true)
   public void getSituationalMusicMixin(CallbackInfoReturnable<net.minecraft.sounds.Music> info) {
      if (this.player != null && this.player.level().dimension() == DimensionInit.VOID_WORLD) {
         info.setReturnValue(Musics.END_BOSS);
      }
   }

   @Inject(method = {"startUseItem"}, at = {@At("HEAD")}, cancellable = true)
   private void startUseItemMixin(CallbackInfo info) {
      if (this.player != null
         && !this.player.isCreative()
         && this.player.level().dimension() == DimensionInit.VOID_WORLD
         && this.hitResult != null
         && this.hitResult.getType() == HitResult.Type.BLOCK
         && (this.player.getMainHandItem().getItem() instanceof BlockItem
            || this.player.getOffhandItem().getItem() instanceof BlockItem)) {
         info.cancel();
      }
   }

   @Inject(
      method = {"continueAttack"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/phys/BlockHitResult;getDirection()Lnet/minecraft/core/Direction;"
      )},
      cancellable = true
   )
   private void continueAttackMixin(boolean bl, CallbackInfo info) {
      if (this.player != null && !this.player.isCreative() && this.player.level().dimension() == DimensionInit.VOID_WORLD) {
         this.gameMode.stopDestroyBlock();
         info.cancel();
      }
   }
}
