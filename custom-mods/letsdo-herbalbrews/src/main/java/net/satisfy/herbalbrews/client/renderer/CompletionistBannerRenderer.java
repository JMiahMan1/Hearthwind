package net.satisfy.herbalbrews.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
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
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.satisfy.herbalbrews.HerbalBrews;
import net.satisfy.herbalbrews.core.blocks.CompletionistBannerBlock;
import net.satisfy.herbalbrews.core.blocks.CompletionistWallBannerBlock;
import net.satisfy.herbalbrews.core.blocks.entity.CompletionistBannerEntity;

// 26.2: same poses, waving flag and texture as before; parts submit through
// BakedPartModel wrappers, per-frame flag wave applied to the live part.
public class CompletionistBannerRenderer implements BlockEntityRenderer<CompletionistBannerEntity, CompletionistBannerRenderer.State> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath(HerbalBrews.MOD_ID, "banner"), "main");

    public static final String FLAG = "flag";
    private static final String POLE = "pole";
    private static final String BAR = "bar";
    private final ModelPart flag;
    private final ModelPart pole;
    private final ModelPart bar;
    private final BakedPartModel flagModel;
    private final BakedPartModel poleModel;
    private final BakedPartModel barModel;

    public static class State extends BlockEntityRenderState {
        public Identifier texture;
        public boolean inInventory;
        public boolean wall;
        public float rotation;
        public long time;
        public float partialTick;
        public boolean poleVisible = true;
        public float flagXRot;
        public float flagY;
    }

    public CompletionistBannerRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart modelPart = context.bakeLayer(LAYER_LOCATION);
        this.flag = modelPart.getChild(FLAG);
        this.pole = modelPart.getChild(POLE);
        this.bar = modelPart.getChild(BAR);
        this.flagModel = new BakedPartModel(this.flag);
        this.poleModel = new BakedPartModel(this.pole);
        this.barModel = new BakedPartModel(this.bar);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild(FLAG, CubeListBuilder.create().texOffs(0, 0).addBox(-10.0F, 0.0F, -1.0F, 20.0F, 40.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -44.0F, -1.0F, -0.0349F, 0.0F, 0.0F));
        partDefinition.addOrReplaceChild(POLE, CubeListBuilder.create().texOffs(44, 0).addBox(-1.0f, -30.0f, -1.0f, 2.0f, 42.0f, 2.0f), PartPose.ZERO);
        partDefinition.addOrReplaceChild(BAR, CubeListBuilder.create().texOffs(0, 42).addBox(-10.0f, -32.0f, -1.0f, 20.0f, 2.0f, 2.0f), PartPose.ZERO);
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(CompletionistBannerEntity banner, State state, float partialTick, Vec3 cameraPos,
            ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(banner, state, crumbling);
        state.partialTick = partialTick;
        state.inInventory = banner.getLevel() == null;
        state.texture = ((CompletionistBannerBlock) banner.getBlockState().getBlock()).getRenderTexture();
        if (state.inInventory) {
            state.time = 0L;
            state.wall = false;
            state.rotation = 0f;
            state.poleVisible = true;
        } else {
            state.time = banner.getLevel().getGameTime();
            BlockState blockState = banner.getBlockState();
            state.texture = ((CompletionistBannerBlock) blockState.getBlock()).getRenderTexture();
            if (!(blockState.getBlock() instanceof CompletionistWallBannerBlock)) {
                state.wall = false;
                // Same property instances as CompletionistBannerBlock.ROTATION /
                // CompletionistWallBannerBlock.FACING, read via vanilla to avoid
                // depending on those (out-of-scope) classes' ported field types.
                state.rotation = (float) (-blockState.getValue(BlockStateProperties.ROTATION_16) * 360) / 16.0f;
                state.poleVisible = true;
            } else {
                state.wall = true;
                state.rotation = -blockState.getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot() + 180.0f;
                state.poleVisible = false;
            }
        }
        float k = ((float) Math.floorMod(banner.getBlockPos().getX() * 7L + banner.getBlockPos().getY() * 9L + banner.getBlockPos().getZ() * 13L + state.time, 100L) + partialTick) / 100.0f;
        state.flagXRot = (-0.0125f + 0.01f * Mth.cos((float) Math.PI * 2 * k)) * (float) Math.PI;
        state.flagY = -32.0f;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        float scale = 0.66f;
        poseStack.pushPose();
        if (state.inInventory) {
            poseStack.translate(0.5, 0.5, 0.5);
        } else if (!state.wall) {
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(state.rotation));
        } else {
            poseStack.translate(0.5, -0.1666666716337204, 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(state.rotation));
            poseStack.translate(0.0, -0.3125, -0.4375);
        }
        poseStack.pushPose();
        poseStack.scale(scale, -scale, -scale);
        this.pole.visible = state.poleVisible;
        collector.submitModel(poleModel, state, poseStack, RenderTypes.entitySolid(state.texture),
                state.lightCoords, OverlayTexture.NO_OVERLAY, EntityRenderState.NO_OUTLINE, state.breakProgress);
        collector.submitModel(barModel, state, poseStack, RenderTypes.entitySolid(state.texture),
                state.lightCoords, OverlayTexture.NO_OVERLAY, EntityRenderState.NO_OUTLINE, state.breakProgress);
        this.flag.xRot = state.flagXRot;
        this.flag.y = state.flagY;
        collector.submitModel(flagModel, state, poseStack, RenderTypes.entitySolid(state.texture),
                state.lightCoords, OverlayTexture.NO_OVERLAY, EntityRenderState.NO_OUTLINE, state.breakProgress);
        poseStack.popPose();
        poseStack.popPose();
    }
}
