package net.medievalweapons.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.medievalweapons.entity.FranciscaEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;

@Environment(EnvType.CLIENT)
public class FranciscaEntityRenderer extends EntityRenderer<FranciscaEntity, FranciscaRenderState> {
   private final ItemModelResolver itemModelResolver;

   public FranciscaEntityRenderer(Context context) {
      super(context);
      this.itemModelResolver = context.getItemModelResolver();
   }

   @Override
   public FranciscaRenderState createRenderState() {
      return new FranciscaRenderState();
   }

   @Override
   public void extractRenderState(FranciscaEntity francisca_Entity, FranciscaRenderState state, float partialTick) {
      super.extractRenderState(francisca_Entity, state, partialTick);
      state.xRot = Mth.lerp(partialTick, francisca_Entity.xRotO, francisca_Entity.getXRot());
      state.yRot = Mth.lerp(partialTick, francisca_Entity.yRotO, francisca_Entity.getYRot());
      state.age = getAge(francisca_Entity);
      this.itemModelResolver.updateForNonLiving(state.item, francisca_Entity.getItem(), ItemDisplayContext.GROUND, francisca_Entity);
   }

   @Override
   public void submit(FranciscaRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
      poseStack.pushPose();
      poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot - 90.0F));
      poseStack.mulPose(Axis.ZP.rotationDegrees(state.xRot + 90.0F * state.age));
      poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
      poseStack.translate(0.0, -0.2, 0.0);
      state.item.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
      poseStack.popPose();
      super.submit(state, poseStack, submitNodeCollector, cameraRenderState);
   }

   private float getAge(FranciscaEntity francisca_Entity) {
      return !francisca_Entity.inGround() ? francisca_Entity.tickCount : 1.0F;
   }
}
