package net.jmb19905.niftycarts.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.jmb19905.niftycarts.entity.PostilionEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class PostilionRenderer
   extends net.minecraft.client.renderer.entity.EntityRenderer<PostilionEntity, net.minecraft.client.renderer.entity.state.EntityRenderState> {
   public PostilionRenderer(Context manager) {
      super(manager);
   }

   @NotNull
   public EntityRenderState createRenderState() {
      return new EntityRenderState();
   }

   public void submit(
      EntityRenderState renderState, @NotNull PoseStack poseStack, @NotNull SubmitNodeCollector nodeCollector, @NotNull CameraRenderState cameraRenderState
   ) {
      super.submit(renderState, poseStack, nodeCollector, cameraRenderState);
   }

   protected boolean shouldShowName(PostilionEntity entity, double d) {
      return false;
   }
}
