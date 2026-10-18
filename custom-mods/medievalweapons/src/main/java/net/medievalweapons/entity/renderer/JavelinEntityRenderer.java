package net.medievalweapons.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.medievalweapons.entity.JavelinEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;

@Environment(EnvType.CLIENT)
public class JavelinEntityRenderer extends EntityRenderer<JavelinEntity, JavelinRenderState> {
   private final ItemModelResolver itemModelResolver;

   public JavelinEntityRenderer(Context context) {
      super(context);
      this.itemModelResolver = context.getItemModelResolver();
   }

   @Override
   public JavelinRenderState createRenderState() {
      return new JavelinRenderState();
   }

   @Override
   public void extractRenderState(JavelinEntity javelin_Entity, JavelinRenderState state, float partialTick) {
      super.extractRenderState(javelin_Entity, state, partialTick);
      state.xRot = Mth.lerp(partialTick, javelin_Entity.xRotO, javelin_Entity.getXRot());
      state.yRot = Mth.lerp(partialTick, javelin_Entity.yRotO, javelin_Entity.getYRot());
      this.itemModelResolver.updateForNonLiving(state.item, javelin_Entity.getItem(), ItemDisplayContext.GUI, javelin_Entity);
   }

   @Override
   public void submit(JavelinRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
      poseStack.pushPose();
      poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot - 90.0F));
      poseStack.mulPose(Axis.ZP.rotationDegrees(state.xRot + -45.0F));
      poseStack.translate(-0.3, -0.15, 0.0);
      state.item.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
      poseStack.popPose();
      super.submit(state, poseStack, submitNodeCollector, cameraRenderState);
   }
}
