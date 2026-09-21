package net.satisfy.candlelight.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.satisfy.candlelight.Candlelight;
import net.satisfy.candlelight.client.model.DinnerBellModel;
import net.satisfy.candlelight.core.block.entity.DinnerBellBlockEntity;

// 26.2: same base/button parts and ring dip as before; parts submit through
// BakedPartModel wrappers with the button offset on the PoseStack.
public class DinnerBellRenderer implements BlockEntityRenderer<DinnerBellBlockEntity, DinnerBellRenderer.State> {
    private static final Identifier BELL_TEXTURE = Candlelight.identifier("textures/entity/dinner_bell.png");

    private final BakedPartModel baseModel;
    private final BakedPartModel buttonModel;

    public static class State extends BlockEntityRenderState {
        public float yOffset;
    }

    public DinnerBellRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart root = context.bakeLayer(DinnerBellModel.LAYER_LOCATION);
        this.baseModel = new BakedPartModel(root.getChild("dinner_bell_base"));
        this.buttonModel = new BakedPartModel(root.getChild("dinner_bell_button"));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(DinnerBellBlockEntity blockEntity, State state, float partialTick, Vec3 cameraPos,
            ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(blockEntity, state, crumbling);
        state.yOffset = blockEntity.getYOffset();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        poseStack.pushPose();
        collector.submitModel(baseModel, state, poseStack, RenderTypes.entityCutout(BELL_TEXTURE),
                state.lightCoords, OverlayTexture.NO_OVERLAY, EntityRenderState.NO_OUTLINE, state.breakProgress);
        poseStack.translate(0.0f, state.yOffset, 0.0f);
        collector.submitModel(buttonModel, state, poseStack, RenderTypes.entityCutout(BELL_TEXTURE),
                state.lightCoords, OverlayTexture.NO_OVERLAY, EntityRenderState.NO_OUTLINE, state.breakProgress);
        poseStack.popPose();
    }
}
