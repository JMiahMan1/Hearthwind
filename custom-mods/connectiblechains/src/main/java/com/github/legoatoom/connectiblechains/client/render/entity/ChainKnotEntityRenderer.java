package com.github.legoatoom.connectiblechains.client.render.entity;

import com.github.legoatoom.connectiblechains.ConnectibleChains;
import com.github.legoatoom.connectiblechains.client.ClientInitializer;
import com.github.legoatoom.connectiblechains.client.render.entity.catenary.CatenaryRenderer;
import com.github.legoatoom.connectiblechains.client.render.entity.model.ChainKnotEntityModel;
import com.github.legoatoom.connectiblechains.client.render.entity.state.ChainKnotEntityRenderState;
import com.github.legoatoom.connectiblechains.client.render.entity.texture.ChainModelReloader;
import com.github.legoatoom.connectiblechains.entity.ChainKnotEntity;
import com.github.legoatoom.connectiblechains.entity.Chainable;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import java.util.HashSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.decoration.BlockAttachedEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class ChainKnotEntityRenderer extends net.minecraft.client.renderer.entity.EntityRenderer<ChainKnotEntity, ChainKnotEntityRenderState> {
   private final ChainKnotEntityModel model;
   private final ChainRenderer chainRenderer = new ChainRenderer();

   public ChainKnotEntityRenderer(Context context) {
      super(context);
      this.model = new ChainKnotEntityModel(context.bakeLayer(ClientInitializer.CHAIN_KNOT));
   }

   public ChainRenderer getChainRenderer() {
      return this.chainRenderer;
   }

   protected AABB getBoundingBox(ChainKnotEntity entity) {
      AABB result = entity.getBoundingBox();

      for (Chainable.ChainData chainData : new HashSet<>(entity.getChainDataSet())) {
         Entity chainHolder = entity.getChainHolder(chainData);
         if (chainHolder != null) {
            result = result.minmax(chainHolder.getBoundingBox());
         }
      }

      return result;
   }

   public ChainKnotEntityRenderState createRenderState() {
      return new ChainKnotEntityRenderState();
   }

   public void render(ChainKnotEntityRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
      matrices.pushPose();
      matrices.translate(0.0, 0.7, 0.0);
      RenderType knotRenderLayer = this.model.renderType(this.getKnotTexture(state.sourceItem));
      queue.submitModel(this.model, state, matrices, knotRenderLayer, state.lightCoords, OverlayTexture.NO_OVERLAY, -1, null, state.outlineColor, null);
      matrices.popPose();
      boolean doDebugDraw = ConnectibleChains.runtimeConfig.doDebugDraw();

      for (ChainKnotEntityRenderState.ChainData chainData : state.chainDataSet) {
         RenderType catenaryRenderLayer = doDebugDraw ? RenderTypes.LINES : RenderTypes.entityCutout(this.getChainTexture(chainData.sourceItem));
         matrices.pushPose();
         queue.submitCustomGeometry(matrices, catenaryRenderLayer, (matricesEntry, vertexConsumer) -> {
            this.renderChainLink(matricesEntry, vertexConsumer, chainData);
            if (doDebugDraw) {
               this.drawDebugVector(matricesEntry, chainData.startPos, chainData.endPos, vertexConsumer);
            }
         });
         matrices.popPose();
      }

      if (doDebugDraw) {
         matrices.pushPose();
         this.submitNameDisplay(state, matrices, queue, cameraState);
         matrices.popPose();
      }

      super.submit(state, matrices, queue, cameraState);
   }

   private void renderChainLink(Pose matricesEntry, VertexConsumer vertexConsumer, ChainKnotEntityRenderState.ChainData chainData) {
      Vec3 offset = chainData.offset;
      Vec3 startPos = chainData.startPos;
      Vec3 endPos = chainData.endPos;
      Item sourceItem = chainData.sourceItem;
      int chainedEntityBlockLight = chainData.chainedEntityBlockLight;
      int chainHolderBlockLight = chainData.chainHolderBlockLight;
      int chainedEntitySkyLight = chainData.chainedEntitySkyLight;
      int chainHolderSkyLight = chainData.chainHolderSkyLight;
      matricesEntry.translate((float)offset.x, (float)offset.y, (float)offset.z);
      Vector3f chainVec = new Vector3f(
         (float)(endPos.x - startPos.x), (float)(endPos.y - startPos.y), (float)(endPos.z - startPos.z)
      );
      float angleY = -((float)Math.atan2(chainVec.z(), chainVec.x()));
      matricesEntry.rotate(new Quaternionf().rotateXYZ(0.0F, angleY, 0.0F));
      CatenaryRenderer renderer = this.getCatenaryRenderer(sourceItem);
      if (chainData.useBaked) {
         ChainRenderer.BakeKey key = new ChainRenderer.BakeKey(startPos, endPos);
         this.chainRenderer
            .renderBaked(
               renderer,
               vertexConsumer,
               matricesEntry,
               key,
               chainVec,
               chainedEntityBlockLight,
               chainHolderBlockLight,
               chainedEntitySkyLight,
               chainHolderSkyLight
            );
      } else {
         this.chainRenderer
            .render(
               renderer, vertexConsumer, matricesEntry, chainVec, chainedEntityBlockLight, chainHolderBlockLight, chainedEntitySkyLight, chainHolderSkyLight
            );
      }
   }

   private void drawDebugVector(Pose matricesEntry, Vec3 startPos, Vec3 endPos, VertexConsumer buffer) {
      if (startPos != null) {
         Matrix4f modelMat = matricesEntry.pose();
         Vec3 vec = endPos.subtract(startPos);
         Vec3 normal = vec.normalize();
         buffer.addVertex(modelMat, 0.0F, 0.0F, 0.0F)
            .setColor(0, 255, 0, 255)
            .setLineWidth(1.0F)
            .setNormal((float)normal.x, (float)normal.y, (float)normal.z);
         buffer.addVertex(modelMat, (float)vec.x, (float)vec.y, (float)vec.z)
            .setColor(255, 0, 0, 255)
            .setLineWidth(1.0F)
            .setNormal((float)normal.x, (float)normal.y, (float)normal.z);
      }
   }

   public void updateRenderState(ChainKnotEntity entity, ChainKnotEntityRenderState state, float tickDelta) {
      super.extractRenderState(entity, state, tickDelta);
      HashSet<ChainKnotEntityRenderState.ChainData> result = new HashSet<>(entity.getChainDataSet().size());

      for (Chainable.ChainData chainData : new HashSet<>(entity.getChainDataSet())) {
         Entity chainHolder = entity.getChainHolder(chainData);
         if (chainHolder != null) {
            Vec3 offset = new Vec3(0.0, 0.3, 0.0);
            Vec3 srcPos = entity.getChainPos(tickDelta);
            Vec3 dstPos;
            if (chainHolder instanceof ChainKnotEntity chainKnotEntity) {
               dstPos = chainKnotEntity.getChainPos(tickDelta);
            } else {
               dstPos = chainHolder.getRopeHoldPosition(tickDelta);
            }

            BlockPos blockPosOfStart = BlockPos.containing(entity.getEyePosition(tickDelta));
            BlockPos blockPosOfEnd = BlockPos.containing(chainHolder.getEyePosition(tickDelta));
            Level world = entity.level();
            ChainKnotEntityRenderState.ChainData renderChainData = new ChainKnotEntityRenderState.ChainData();
            renderChainData.offset = offset;
            renderChainData.startPos = srcPos;
            renderChainData.endPos = dstPos;
            renderChainData.chainedEntityBlockLight = world.getBrightness(LightLayer.BLOCK, blockPosOfStart);
            renderChainData.chainHolderBlockLight = world.getBrightness(LightLayer.BLOCK, blockPosOfEnd);
            renderChainData.chainedEntitySkyLight = world.getBrightness(LightLayer.SKY, blockPosOfStart);
            renderChainData.chainHolderSkyLight = world.getBrightness(LightLayer.SKY, blockPosOfEnd);
            renderChainData.sourceItem = chainData.sourceItem;
            renderChainData.useBaked = chainHolder instanceof BlockAttachedEntity;
            result.add(renderChainData);
         }
      }

      state.chainDataSet = result;
      state.sourceItem = entity.getSourceItem();
      if (ConnectibleChains.runtimeConfig.doDebugDraw()) {
         state.nameTag = Component.literal("C: " + state.chainDataSet.size());
         state.nameTagAttachment = entity.getAttachments().getNullable(EntityAttachment.NAME_TAG, 0, entity.getYRot(tickDelta));
      }
   }

   private ChainModelReloader getTextureManager() {
      return ClientInitializer.getInstance().getChainTextureManager();
   }

   private Identifier getKnotTexture(Item item) {
      Identifier id = BuiltInRegistries.ITEM.getKey(item);
      return this.getTextureManager().getKnotTexture(id);
   }

   private Identifier getChainTexture(Item item) {
      Identifier id = BuiltInRegistries.ITEM.getKey(item);
      return this.getTextureManager().getChainTexture(id);
   }

   private CatenaryRenderer getCatenaryRenderer(Item item) {
      Identifier id = BuiltInRegistries.ITEM.getKey(item);
      return this.getTextureManager().getCatenaryRenderer(id);
   }
}
