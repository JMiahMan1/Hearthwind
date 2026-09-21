package net.satisfy.farm_and_charm.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.satisfy.farm_and_charm.core.block.entity.RopeKnotBlockEntity;

// 26.2: the held fence block submits through BlockModelResolver with the
// same state snapshot behavior as the old renderBatched path.
public class RopeKnotRenderer implements BlockEntityRenderer<RopeKnotBlockEntity, RopeKnotRenderer.State> {
    private final BlockModelResolver blockResolver;

    public static class State extends BlockEntityRenderState {
        public BlockState held;
    }

    public RopeKnotRenderer(BlockEntityRendererProvider.Context context) {
        this.blockResolver = context.blockModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(RopeKnotBlockEntity ropeKnotBlockEntity, State state, float partialTick,
            Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(ropeKnotBlockEntity, state, crumbling);
        if (ropeKnotBlockEntity.getLevel() == null) return;
        BlockState held = ropeKnotBlockEntity.getHeldBlock();
        if (held == null || held.isAir()) return;
        state.held = held;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        if (state.held == null || state.held.isAir()) return;

        poseStack.pushPose();
        BlockModelRenderState renderState = new BlockModelRenderState();
        blockResolver.update(renderState, state.held,
                net.minecraft.client.renderer.block.model.BlockDisplayContext.create());
        renderState.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }
}
