package com.starfish_studios.another_furniture.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.starfish_studios.another_furniture.AnotherFurniture;
import com.starfish_studios.another_furniture.block.entity.ServiceBellBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class ServiceBellButtonRenderer implements BlockEntityRenderer<ServiceBellBlockEntity, ServiceBellRenderState> {
   public static final SpriteId BELL_TEXTURE = new SpriteId(TextureAtlas.LOCATION_BLOCKS, AnotherFurniture.res("block/service_bell"));
   public static ModelLayerLocation SERVICE_BELL_MODEL = new ModelLayerLocation(AnotherFurniture.res("service_bell"), "service_bell");
   private final ModelPart button;
   private final SpriteGetter sprites;

   public ServiceBellButtonRenderer(Context context) {
      this.button = context.bakeLayer(SERVICE_BELL_MODEL);
      this.sprites = context.sprites();
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      partdefinition.addOrReplaceChild(
         "button", CubeListBuilder.create().texOffs(19, 11).addBox(-9.0F, -7.0F, 7.0F, 2.0F, 1.0F, 2.0F), PartPose.ZERO
      );
      partdefinition.getChild("button")
         .addOrReplaceChild(
            "button_shaft", CubeListBuilder.create().texOffs(0, 0).addBox(-8.5F, -6.0F, 7.5F, 1.0F, 1.0F, 1.0F), PartPose.ZERO
         );
      return LayerDefinition.create(meshdefinition, 32, 32);
   }

   public ServiceBellRenderState createRenderState() {
      return new ServiceBellRenderState();
   }

   public void extractRenderState(
      ServiceBellBlockEntity blockEntity, ServiceBellRenderState state, float partialTick, Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
   ) {
      BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTick, cameraPos, crumblingOverlay);
      state.offsetY = blockEntity.pressed ? -(blockEntity.ticks / 25.0F) : 0.0F;
   }

   public void submit(ServiceBellRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
      poseStack.translate(0.0F, state.offsetY, 0.0F);
      poseStack.pushPose();
      poseStack.scale(-1.0F, -1.0F, 1.0F);
      collector.submitModelPart(
         this.button, poseStack, RenderTypes.entitySolid(TextureAtlas.LOCATION_BLOCKS), state.lightCoords, OverlayTexture.NO_OVERLAY, this.sprites.get(BELL_TEXTURE)
      );
      poseStack.popPose();
   }
}
