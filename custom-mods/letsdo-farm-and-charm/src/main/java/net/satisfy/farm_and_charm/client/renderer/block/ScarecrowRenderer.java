package net.satisfy.farm_and_charm.client.renderer.block;

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
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.satisfy.farm_and_charm.FarmAndCharm;
import net.satisfy.farm_and_charm.client.model.ScarecrowModel;
import net.satisfy.farm_and_charm.core.block.ScarecrowBlock;
import net.satisfy.farm_and_charm.core.block.entity.ScarecrowBlockEntity;
import org.joml.Quaternionf;

// 26.2: same sway animation, facing and dungarees texture as before,
// submitted through BakedPartModel wrappers.
public class ScarecrowRenderer implements BlockEntityRenderer<ScarecrowBlockEntity, ScarecrowRenderer.State> {

    private static final Identifier TEX_WITH = FarmAndCharm.identifier("textures/entity/scarecrow.png");
    private static final Identifier TEX_NO  = FarmAndCharm.identifier("textures/entity/scarecrow_no_dungarees.png");
    private final ModelPart scarecrow;
    private final ModelPart post;
    private final BakedPartModel scarecrowModel;
    private final BakedPartModel postModel;

    public static class State extends BlockEntityRenderState {
        public Direction facing = Direction.NORTH;
        public boolean hasDungarees = true;
        public float swayAngle;
    }

    public ScarecrowRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart root = context.bakeLayer(ScarecrowModel.LAYER_LOCATION);
        this.scarecrow = root.getChild("scarecrow");
        this.post = root.getChild("post");
        this.scarecrowModel = new BakedPartModel(this.scarecrow);
        this.postModel = new BakedPartModel(this.post);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(ScarecrowBlockEntity be, State state, float partialTick, Vec3 cameraPos,
            ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(be, state, crumbling);
        state.facing = be.getBlockState().getValue(ScarecrowBlock.FACING);
        state.hasDungarees = be.getBlockState().getValue(ScarecrowBlock.HAS_DUNGAREES);
        long t = be.getLevel() == null ? 0L : be.getLevel().getGameTime();
        state.swayAngle = (float) Math.sin(t * 0.05) * 1.5f;
    }

    @Override
    public void submit(State state, PoseStack ms, SubmitNodeCollector collector, CameraRenderState cameraState) {
        Identifier tex = state.hasDungarees ? TEX_WITH : TEX_NO;

        float rotY = -state.facing.toYRot() + 180;

        ms.pushPose();
        ms.translate(0.5, 0, 0.5);
        ms.mulPose(new Quaternionf().rotateY((float) Math.toRadians(rotY)));
        ms.mulPose(new Quaternionf().rotateX((float) Math.toRadians(state.swayAngle)));
        ms.translate(-0.5, 0, -0.5);

        collector.submitModel(scarecrowModel, state, ms, RenderTypes.entityCutout(tex),
                state.lightCoords, OverlayTexture.NO_OVERLAY, EntityRenderState.NO_OUTLINE, state.breakProgress);
        collector.submitModel(postModel, state, ms, RenderTypes.entityCutout(tex),
                state.lightCoords, OverlayTexture.NO_OVERLAY, EntityRenderState.NO_OUTLINE, state.breakProgress);
        ms.popPose();
    }
}
