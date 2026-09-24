package net.adventurez.block.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.adventurez.block.PiglinFlag;
import net.adventurez.block.entity.PiglinFlagEntity;
import net.adventurez.init.RenderInit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class PiglinFlagRenderer implements BlockEntityRenderer<PiglinFlagEntity, PiglinFlagRenderer.State> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("adventurez", "textures/block/piglin_flag.png");
    private final ModelPart holder;
    private final ModelPart flag;

    public PiglinFlagRenderer(BlockEntityRendererProvider.Context context) {
        this.holder = context.bakeLayer(RenderInit.PIGLIN_FLAG_LAYER).getChild("holder");
        this.flag = this.holder.getChild("flag");
    }

    public PiglinFlagRenderer(ModelPart root) {
        this.holder = root.getChild("holder");
        this.flag = this.holder.getChild("flag");
    }

    public static LayerDefinition getMeshDefinition() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition root = modelData.getRoot();
        PartDefinition holder = root.addOrReplaceChild("holder", CubeListBuilder.create().texOffs(0, 42).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 11.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(10, 20).addBox(-2.0F, 40.0F, -2.0F, 4.0F, 4.0F, 18.0F, new CubeDeformation(0.0F)).texOffs(36, 0).addBox(-1.0F, 11.0F, -1.0F, 2.0F, 29.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -20.0F, 0.0F));
        holder.addOrReplaceChild("flag", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -24.0F, -6.6F, 0.0F, 24.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 40.0F, 8.0F));
        return LayerDefinition.create(modelData, 64, 64);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(PiglinFlagEntity blockEntity, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.blockState = blockEntity.getBlockState();
        state.animation = (float) ((blockEntity.getBlockPos().getX() * 7L + blockEntity.getBlockPos().getY() * 9L + blockEntity.getBlockPos().getZ() * 13L + blockEntity.getLevel().getGameTime()) % 100L) / 100.0F;
        this.flag.zRot = Mth.cos(6.2831855F * state.animation) / 4.0F;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        BlockState blockState = state.blockState;
        if (blockState.getBlock() instanceof PiglinFlag) {
            Direction direction = blockState.getValue(HorizontalDirectionalBlock.FACING);
            if (direction == Direction.NORTH) {
                poseStack.translate(1.0D, 0.0D, 1.0D);
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            } else if (direction == Direction.EAST) {
                poseStack.translate(0.0D, 0.0D, 1.0D);
                poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            } else if (direction == Direction.WEST) {
                poseStack.translate(1.0D, 0.0D, 0.0D);
                poseStack.mulPose(Axis.YP.rotationDegrees(270.0F));
            }
        }
        poseStack.translate(0.5D, 1.25D, 0.5D);
        collector.submitModelPart(this.holder, poseStack, RenderTypes.entityCutout(TEXTURE), state.lightCoords, OverlayTexture.NO_OVERLAY, null);
        poseStack.popPose();
    }

    public static class State extends BlockEntityRenderState {
        private BlockState blockState;
        private float animation;
    }
}
