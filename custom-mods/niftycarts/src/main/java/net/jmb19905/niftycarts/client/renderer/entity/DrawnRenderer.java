package net.jmb19905.niftycarts.client.renderer.entity;

import com.mojang.math.Axis;
import java.util.Map;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.jmb19905.niftycarts.client.mixin.ModelPartMixin;
import net.jmb19905.niftycarts.client.renderer.entity.model.CartModel;
import net.jmb19905.niftycarts.entity.AbstractDrawnEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.banner.BannerFlagModel;
import net.minecraft.client.model.object.banner.BannerModel;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public abstract class DrawnRenderer<T extends AbstractDrawnEntity, S extends CartRenderState, M extends CartModel<S>>
   extends net.minecraft.client.renderer.entity.EntityRenderer<T, S> {
   protected M model;
   private final ModelPart pole;
   private final ModelPart bar;
   private final BannerFlagModel flagModel;
   private final SpriteGetter materials;

   protected DrawnRenderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context renderManager, M model) {
      super(renderManager);
      this.model = model;
      this.materials = renderManager.getSprites();
      this.flagModel = model.getFlagModel();
      BannerModel bannerModel = model.getBannerModel();
      this.pole = bannerModel.getChildPart("pole");
      this.bar = bannerModel.getChildPart("bar");
   }

   @NotNull
   protected net.minecraft.world.phys.AABB getBoundingBoxForCulling(@NotNull T minecraft) {
      return super.getBoundingBoxForCulling(minecraft).inflate(0.5);
   }

   @NotNull
   public abstract S createRenderState();

   public void extractRenderState(T entity, S state, float delta) {
      super.extractRenderState(entity, state, delta);
      AbstractDrawnEntity.RenderInfo info = entity.getInfo(delta);
      state.pitch = info.getPitch();
      state.yaw = info.getYaw();
      state.wheelRotation0 = entity.getWheelRotation(0);
      state.wheelRotation1 = entity.getWheelRotation(1);
      state.wheelRotationInc0 = entity.getWheelRotationIncrement(0);
      state.wheelRotationInc1 = entity.getWheelRotationIncrement(1);
      state.timeSinceHit = entity.getTimeSinceHit();
      state.damage = entity.getDamageTaken();
      state.forward = entity.getForwardDirection();
      state.bannerColor = entity.getBannerColor();
      state.bannerPattern = entity.getBannerPattern();
      state.woodType = entity.getWoodType();
   }

   public abstract net.minecraft.resources.Identifier getTextureLocation(S var1);

   public void submit(
      S state,
      com.mojang.blaze3d.vertex.PoseStack stack,
      net.minecraft.client.renderer.SubmitNodeCollector collector,
      @NotNull net.minecraft.client.renderer.state.level.CameraRenderState cameraState
   ) {
      stack.pushPose();
      this.setupRotation(state, stack);
      this.model.setupAnim(state);
      collector.submitModel(
         this.model, state, stack, this.model.renderType(this.getTextureLocation(state)), state.lightCoords, OverlayTexture.NO_OVERLAY, -1, null, 0, null
      );
      this.submitContents(state, stack, collector);
      stack.popPose();
   }

   protected abstract void submitContents(S var1, com.mojang.blaze3d.vertex.PoseStack var2, net.minecraft.client.renderer.SubmitNodeCollector var3);

   public void setupRotation(S state, com.mojang.blaze3d.vertex.PoseStack stack) {
      stack.mulPose(Axis.YP.rotationDegrees(180.0F - state.yaw));
      float time = state.timeSinceHit - state.delta;
      if (time > 0.0F) {
         double center = 1.2;
         stack.translate(0.0, 1.2, 0.0);
         float damage = Math.max(state.damage - state.delta, 0.0F);
         float angle = Mth.sin(time) * time * damage / 60.0F;
         stack.mulPose(Axis.ZP.rotationDegrees(angle * state.forward));
         stack.translate(0.0, -1.2, 0.0);
         stack.translate(0.0, angle / 32.0F, 0.0);
      }

      stack.scale(-1.0F, -1.0F, 1.0F);
   }

   protected void submitBanner(
      S state, com.mojang.blaze3d.vertex.PoseStack stack, net.minecraft.client.renderer.SubmitNodeCollector submitNodeCollector
   ) {
      stack.pushPose();
      stack.mulPose(Axis.YP.rotationDegrees(90.0F));
      float scale = 0.6666667F;
      stack.scale(0.6666667F, 0.6666667F, 0.6666667F);
      SpriteId material = Sheets.BANNER_BASE;
      submitNodeCollector.submitModelPart(
         this.pole,
         stack,
         material.renderType(RenderTypes::entitySolid),
         state.lightCoords,
         OverlayTexture.NO_OVERLAY,
         this.materials.get(material)
      );
      submitNodeCollector.submitModelPart(
         this.bar,
         stack,
         material.renderType(RenderTypes::entitySolid),
         state.lightCoords,
         OverlayTexture.NO_OVERLAY,
         this.materials.get(material)
      );
      float k = (Math.floorMod((int)(state.x * 7.0 + state.y * 9.0 + state.z * 13.0 + state.ageInTicks), 100) + state.delta)
         / 100.0F;
      stack.translate(-0.25F, 1.125F, 0.09375F);
      BannerRenderer.submitPatterns(
         this.materials,
         stack,
         submitNodeCollector,
         state.lightCoords,
         OverlayTexture.NO_OVERLAY,
         this.flagModel,
         k,
         true,
         state.bannerColor,
         state.bannerPattern,
         null
      );
      stack.popPose();
   }

   protected void attach(
      net.minecraft.client.model.geom.ModelPart bone,
      net.minecraft.client.model.geom.ModelPart attachment,
      Consumer<com.mojang.blaze3d.vertex.PoseStack> function,
      com.mojang.blaze3d.vertex.PoseStack stack
   ) {
      stack.pushPose();
      bone.translateAndRotate(stack);
      if (bone == attachment) {
         function.accept(stack);
      } else {
         Map<String, net.minecraft.client.model.geom.ModelPart> childModels = ((ModelPartMixin)(Object)bone).getChildren();

         for (ModelPart child : childModels.values()) {
            this.attach(child, attachment, function, stack);
         }
      }

      stack.popPose();
   }
}
