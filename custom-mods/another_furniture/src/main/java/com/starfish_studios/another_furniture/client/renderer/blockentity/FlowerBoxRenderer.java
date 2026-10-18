package com.starfish_studios.another_furniture.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.starfish_studios.another_furniture.block.FlowerBoxBlock;
import com.starfish_studios.another_furniture.block.entity.FlowerBoxBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class FlowerBoxRenderer implements BlockEntityRenderer<FlowerBoxBlockEntity, FlowerBoxRenderState> {
   private final BlockModelResolver blockModelResolver;

   public FlowerBoxRenderer(BlockEntityRendererProvider.Context context) {
      this.blockModelResolver = context.blockModelResolver();
   }

   public FlowerBoxRenderState createRenderState() {
      return new FlowerBoxRenderState();
   }

   public void extractRenderState(
      FlowerBoxBlockEntity blockEntity, FlowerBoxRenderState state, float partialTick, Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
   ) {
      BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTick, cameraPos, crumblingOverlay);
      state.facing = (Direction)blockEntity.getBlockState().getValue(FlowerBoxBlock.FACING);
      state.attached = (Boolean)blockEntity.getBlockState().getValue(FlowerBoxBlock.ATTACHED);

      for (int i = 0; i < 2; i++) {
         state.hasLower[i] = false;
         state.hasUpper[i] = false;
         Item item = blockEntity.getItemFromSlot(i);
         if (item != Items.AIR) {
            Block block = ((BlockItem)item).getBlock();
            BlockState lower = block.defaultBlockState();
            BlockState upper = null;
            if (lower.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
               lower = (BlockState)lower.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);
               upper = (BlockState)lower.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER);
            }

            this.blockModelResolver.update(state.lower[i], lower, BlockDisplayContext.create());
            state.hasLower[i] = true;
            if (upper != null) {
               this.blockModelResolver.update(state.upper[i], upper, BlockDisplayContext.create());
               state.hasUpper[i] = true;
            }
         }
      }
   }

   public void submit(FlowerBoxRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
      Direction facing = state.facing.getOpposite();
      float rotation = -facing.toYRot();
      poseStack.pushPose();
      poseStack.scale(0.7F, 0.7F, 0.7F);
      poseStack.translate(0.0F, 0.4F, 0.0F);

      for (int i = 0; i < 2; i++) {
         if (!state.hasLower[i]) {
            continue;
         }

         poseStack.pushPose();
         poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
         poseStack.translate(0.6 - 0.8 * i, 0.001 * i, 0.2);
         poseStack.translate(0.0, 0.001 * i, 0.001 * i);
         switch (state.facing) {
            case EAST:
               poseStack.translate(0.0, 0.0, -1.4);
               break;
            case WEST:
               poseStack.translate(-1.4, 0.0, 0.0);
               break;
            case SOUTH:
               poseStack.translate(-1.4, 0.0, -1.4);
         }

         if (state.attached) {
            poseStack.translate(0.0, 0.9, 0.36);
         }

         state.lower[i].submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
         if (state.hasUpper[i]) {
            poseStack.translate(0.0, 1.0, 0.0);
            state.upper[i].submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
         }

         poseStack.popPose();
      }

      poseStack.popPose();
   }
}
