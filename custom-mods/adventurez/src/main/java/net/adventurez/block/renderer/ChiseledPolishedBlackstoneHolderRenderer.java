package net.adventurez.block.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.adventurez.block.entity.ChiseledPolishedBlackstoneHolderEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class ChiseledPolishedBlackstoneHolderRenderer implements BlockEntityRenderer<ChiseledPolishedBlackstoneHolderEntity, ChiseledPolishedBlackstoneHolderRenderer.State> {
    private final ItemModelResolver itemModelResolver;

    public ChiseledPolishedBlackstoneHolderRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(ChiseledPolishedBlackstoneHolderEntity blockEntity, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        if (blockEntity.isEmpty()) {
            state.item.clear();
        } else {
            this.itemModelResolver.updateForTopItem(state.item, blockEntity.getItem(0), ItemDisplayContext.GROUND, blockEntity.getLevel(), null, (int) blockEntity.getBlockPos().asLong());
        }
        float time = blockEntity.getLevel().getGameTime() + partialTicks;
        state.bob = (float) Math.sin(time / 8.0D) / 4.5F;
        state.rotation = time * 4.0F;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.item.isEmpty()) {
            poseStack.pushPose();
            double offset = state.bob;
            poseStack.translate(0.5D, 1.3D + offset, 0.5D);
            poseStack.mulPose(Axis.YP.rotationDegrees(state.rotation));
            state.item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }

    public static class State extends BlockEntityRenderState {
        private final ItemStackRenderState item = new ItemStackRenderState();
        private float bob;
        private float rotation;

        public boolean isEmpty() {
            return this.item.isEmpty();
        }
    }
}
