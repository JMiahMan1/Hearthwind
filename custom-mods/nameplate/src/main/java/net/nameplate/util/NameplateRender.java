package net.nameplate.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.nameplate.NameplateMain;
import net.nameplate.access.MobEntityAccess;
import net.nameplate.access.NameplateRenderState;

@Environment(EnvType.CLIENT)
public class NameplateRender {
   private static final Identifier ICONS = Identifier.parse("nameplate:textures/icons.png");

   public static void extract(LivingEntity entity, LivingEntityRenderState state) {
      NameplateRenderState renderState = (NameplateRenderState)(Object)state;
      if (entity instanceof Mob mob) {
         renderState.nameplateIsMob(true);
         renderState.nameplateLevel(((MobEntityAccess)mob).getMobRpgLevel());
         renderState.nameplateShowLabel(((MobEntityAccess)mob).showMobRpgLabel());
         renderState.nameplateHealth(mob.getHealth());
         renderState.nameplateMaxHealth(mob.getMaxHealth());
         renderState.nameplateVehicle(mob.isVehicle());
         renderState.nameplateName(mob.hasCustomName() ? mob.getCustomName() : mob.getName());
         boolean canSee = true;
         if (!NameplateMain.CONFIG.showNameplateIfObstructed) {
            var player = Minecraft.getInstance().player;
            canSee = player != null && player.hasLineOfSight(mob);
         }

         renderState.nameplateCanSee(canSee);
      } else {
         renderState.nameplateIsMob(false);
      }
   }

   public static void renderNameplate(
      EntityRenderer<?, ?> entityRenderer,
      LivingEntityRenderState state,
      PoseStack poseStack,
      SubmitNodeCollector collector,
      CameraRenderState cameraState,
      Font font,
      boolean bodyVisible
   ) {
      NameplateRenderState renderState = (NameplateRenderState)(Object)state;
      if (!renderState.nameplateIsMob()
         || !NameplateMain.CONFIG.showLevel
         || state.distanceToCameraSq > NameplateMain.CONFIG.squaredDistance
         || renderState.nameplateVehicle()
         || !bodyVisible
         || !renderState.nameplateShowLabel()
         || !renderState.nameplateCanSee()) {
         return;
      }

      poseStack.pushPose();
      poseStack.translate(0.0, (double)state.boundingBoxHeight + NameplateMain.CONFIG.nameplateHeight, 0.0);
      poseStack.mulPose(cameraState.orientation);
      poseStack.scale(-NameplateMain.CONFIG.nameplateSize, NameplateMain.CONFIG.nameplateSize, -0.025F);
      if (NameplateMain.CONFIG.healthBar) {
         poseStack.pushPose();
         poseStack.scale(1.5F, 1.5F, 1.0F);
         RenderType renderType = RenderTypes.entityTranslucent(ICONS);
         float health = renderState.nameplateMaxHealth() > 0.0F
            ? renderState.nameplateHealth() / renderState.nameplateMaxHealth()
            : 0.0F;
         collector.submitCustomGeometry(
            poseStack, renderType, (pose, vertexConsumer) -> barQuad(pose, vertexConsumer, state.lightCoords, -20.0F, 40.0F, 0.0F, 6.0F)
         );
         poseStack.translate(0.0, 0.0, -0.01);
         collector.submitCustomGeometry(
            poseStack, renderType, (pose, vertexConsumer) -> barQuad(pose, vertexConsumer, state.lightCoords, -20.0F, 40.0F * health, 6.0F, 12.0F)
         );
         poseStack.popPose();
         poseStack.translate(0.0, -9.0, 0.0);
      }

      String string = renderState.nameplateName().getString();
      if (NameplateMain.CONFIG.showHealth) {
         string = string
            + " "
            + Component.translatable(
                  "text.nameplate.health", Math.round(renderState.nameplateHealth()), Math.round(renderState.nameplateMaxHealth())
               )
               .getString();
      }

      String levelString = Component.translatable("text.nameplate.level", renderState.nameplateLevel()).getString();
      string = levelString + " " + Component.translatable("text.nameplate.name", string).getString();
      Component text = Component.nullToEmpty(string);
      FormattedCharSequence sequence = text.getVisualOrderText();
      float x = -font.width(text) / 2.0F;
      float opacity = Minecraft.getInstance().options.getBackgroundOpacity(NameplateMain.CONFIG.backgroundOpacity);
      int background = (int)(opacity * 255.0F) << 24;
      collector.submitText(
         poseStack, x, 0.0F, sequence, false, Font.DisplayMode.SEE_THROUGH, state.lightCoords, NameplateMain.CONFIG.nameColor, background, 0
      );
      collector.submitText(
         poseStack, x, 0.0F, sequence, false, Font.DisplayMode.NORMAL, state.lightCoords, NameplateMain.CONFIG.backgroundColor, 0, 0
      );
      poseStack.popPose();
   }

   private static void barQuad(PoseStack.Pose pose, VertexConsumer vertexConsumer, int light, float x, float width, float v0, float v1) {
      float x1 = x + width;
      float y0 = 0.0F;
      float y1 = 6.0F;
      float u1 = width / 256.0F;
      float fv0 = v0 / 256.0F;
      float fv1 = v1 / 256.0F;
      vertexConsumer.addVertex(pose, x, y0, 0.0F).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(0.0F, fv0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
      vertexConsumer.addVertex(pose, x1, y0, 0.0F).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(u1, fv0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
      vertexConsumer.addVertex(pose, x1, y1, 0.0F).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(u1, fv1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
      vertexConsumer.addVertex(pose, x, y1, 0.0F).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(0.0F, fv1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
   }
}
