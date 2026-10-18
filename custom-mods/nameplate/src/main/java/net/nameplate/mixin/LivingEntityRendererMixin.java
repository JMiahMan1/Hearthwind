package net.nameplate.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.nameplate.util.NameplateRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>>
   extends LivingEntityRenderer<T, S, M> {
   protected LivingEntityRendererMixin(EntityRendererProvider.Context ctx, M model, float shadowRadius) {
      super(ctx, model, shadowRadius);
   }

   @Inject(
      method = {"extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V"},
      at = {@At("TAIL")}
   )
   private void extractRenderStateMixin(LivingEntity entity, LivingEntityRenderState state, float partialTick, CallbackInfo info) {
      NameplateRender.extract(entity, state);
   }

   @Inject(
      method = {
            "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V"
      },
      at = {@At("TAIL")}
   )
   private void submitMixin(
      LivingEntityRenderState state,
      PoseStack poseStack,
      SubmitNodeCollector collector,
      CameraRenderState cameraState,
      CallbackInfo info
   ) {
      NameplateRender.renderNameplate(this, state, poseStack, collector, cameraState, this.getFont(), this.isBodyVisible((S)(Object)state));
   }
}
