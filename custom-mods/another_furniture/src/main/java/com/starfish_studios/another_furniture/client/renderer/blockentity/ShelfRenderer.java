package com.starfish_studios.another_furniture.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.starfish_studios.another_furniture.block.ShelfBlock;
import com.starfish_studios.another_furniture.block.entity.ShelfBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class ShelfRenderer implements BlockEntityRenderer<ShelfBlockEntity, ShelfRenderState> {
   private final ItemModelResolver itemModelResolver;

   public ShelfRenderer(BlockEntityRendererProvider.Context context) {
      this.itemModelResolver = context.itemModelResolver();
   }

   public ShelfRenderState createRenderState() {
      return new ShelfRenderState();
   }

   public void extractRenderState(
      ShelfBlockEntity blockEntity, ShelfRenderState state, float partialTick, Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
   ) {
      BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTick, cameraPos, crumblingOverlay);
      state.facing = (Direction)blockEntity.getBlockState().getValue(ShelfBlock.FACING);
      state.items.clear();
      NonNullList<ItemStack> items = blockEntity.getItems();

      for (int j = 0; j < items.size(); j++) {
         ItemStack stack = (ItemStack)items.get(j);
         if (!stack.isEmpty()) {
            int renderCount = this.getAmount(stack.getCount());

            for (int i = 0; i < renderCount; i++) {
               ItemStackRenderState itemState = new ItemStackRenderState();
               this.itemModelResolver.updateForTopItem(itemState, stack, ItemDisplayContext.FIXED, blockEntity.getLevel(), null, 0);
               state.items.add(new ShelfRenderState.RenderItem(itemState, j, i));
            }
         }
      }
   }

   public void submit(ShelfRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
      Direction direction = state.facing;
      float rotation = -direction.toYRot() + 180.0F;
      poseStack.pushPose();
      poseStack.translate(0.5, 1.18, 0.5);
      poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

      for (ShelfRenderState.RenderItem entry : state.items) {
         int j = entry.slot();
         int i = entry.index();
         float fx = -0.10375F * (i - 1) * 0.5F % 0.09F;
         float fy = -0.04375F * (i - 1) * 0.5F % 0.09F;
         float fz = -0.05375F * (i - 1) * 0.5F % 0.09F;
         poseStack.pushPose();
         poseStack.translate(0.15 - 0.4 * (j % 2), 0.0, -0.225 + 0.4 * (j / 2));
         poseStack.translate(fx, fy, fz);
         poseStack.scale(0.375F, 0.375F, 0.375F);
         entry.model().submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
         poseStack.popPose();
      }

      poseStack.popPose();
   }

   private int getAmount(int count) {
      if (count > 48) {
         return 5;
      } else if (count > 32) {
         return 4;
      } else if (count > 16) {
         return 3;
      } else {
         return count > 1 ? 2 : 1;
      }
   }
}
