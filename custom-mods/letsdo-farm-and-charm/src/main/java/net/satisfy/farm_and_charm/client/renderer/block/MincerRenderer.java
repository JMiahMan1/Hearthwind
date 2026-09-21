package net.satisfy.farm_and_charm.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.satisfy.farm_and_charm.FarmAndCharm;
import net.satisfy.farm_and_charm.client.model.MincerModel;
import net.satisfy.farm_and_charm.core.block.MincerBlock;
import net.satisfy.farm_and_charm.core.block.entity.MincerBlockEntity;
import org.joml.Vector3f;

// 26.2: renders submit baked parts through BakedPartModel wrappers.
// Same facing offsets, crank animation and texture as before.
public class MincerRenderer implements BlockEntityRenderer<MincerBlockEntity, MincerRenderer.State> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(FarmAndCharm.MOD_ID, "textures/entity/mincer.png");
    private final ModelPart mincer;
    private final ModelPart crank;
    private final BakedPartModel mincerModel;
    private final BakedPartModel crankModel;

    public static class State extends BlockEntityRenderState {
        public Direction facing = Direction.NORTH;
        public float crankAngle;
        public boolean skip;
    }

    public MincerRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart root = context.bakeLayer(MincerModel.LAYER_LOCATION);
        this.mincer = root.getChild("mincer");
        this.crank = root.getChild("crank");
        this.mincerModel = new BakedPartModel(this.mincer);
        this.crankModel = new BakedPartModel(this.crank);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(MincerBlockEntity blockEntity, State state, float partialTick, Vec3 cameraPos,
            ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(blockEntity, state, crumbling);
        Level level = blockEntity.getLevel();
        if (level == null) {
            state.skip = true;
            return;
        }
        BlockState blockState = level.getBlockState(blockEntity.getBlockPos());
        if (!(blockState.getBlock() instanceof MincerBlock)) {
            state.skip = true;
            return;
        }
        state.skip = false;
        state.facing = blockState.getValue(MincerBlock.FACING);
        state.crankAngle = blockEntity.getInterpolatedCrankAngle(partialTick);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        if (state.skip) return;

        poseStack.pushPose();

        Vector3f offset = new Vector3f();
        float rotationDegrees = 0F;

        switch (state.facing) {
            case NORTH -> { offset.set(1F, 0F, 1F); rotationDegrees = 180F; }
            case EAST  -> { offset.set(0F, 0F, 1F); rotationDegrees = 90F; }
            case SOUTH -> offset.set(0F, 0F, 0F);
            case WEST  -> { offset.set(1F, 0F, 0F); rotationDegrees = 270F; }
        }

        poseStack.translate(offset.x, offset.y, offset.z);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotationDegrees));

        collector.submitModel(mincerModel, state, poseStack, RenderTypes.entityCutout(TEXTURE),
                state.lightCoords, OverlayTexture.NO_OVERLAY, EntityRenderState.NO_OUTLINE, state.breakProgress);

        poseStack.translate(0.5F, 0.625F, 0.5F);
        poseStack.mulPose(Axis.XP.rotation(state.crankAngle));
        poseStack.translate(-0.5F, -0.625F, -0.5F);

        collector.submitModel(crankModel, state, poseStack, RenderTypes.entityCutout(TEXTURE),
                state.lightCoords, OverlayTexture.NO_OVERLAY, EntityRenderState.NO_OUTLINE, state.breakProgress);

        poseStack.popPose();
    }
}
