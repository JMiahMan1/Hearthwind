package net.nameplate.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin({MobRenderer.class})
public abstract class MobEntityRendererMixin {
   @Inject(
      method = {"shouldShowName(Lnet/minecraft/world/entity/Mob;D)Z"},
      at = {@At("HEAD")},
      cancellable = true
   )
   protected void shouldShowNameMixin(Mob mobEntity, double distance, CallbackInfoReturnable<Boolean> info) {
      info.setReturnValue(false);
   }
}
