package net.satisfy.meadow.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.satisfy.meadow.Meadow;
import net.satisfy.meadow.core.block.entity.PineHangingSignBlockEntity;

// 26.2: same poses, chains and text layout as before; board submits
// through a BakedPartModel, text through the collector.
@Environment(EnvType.CLIENT)
public class PineHangingSignRenderer extends PineSignRenderer<PineHangingSignBlockEntity, PineHangingSignRenderer.HangingState> {
    private static final Vec3 TEXT_OFFSET = new Vec3(0.0, -0.3199999928474426, 0.0729999989271164);
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Meadow.MOD_ID, "textures/entity/signs/hanging/pine.png");
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Meadow.identifier("pine_hanging_sign"), "main");

    private final ModelPart plank;
    private final ModelPart normalChains;
    private final ModelPart vChains;
    private final BakedPartModel boardModel;

    public static class HangingState extends PineSignRenderer.State {
        public boolean ceiling = true;
        public boolean attached;
    }

    public PineHangingSignRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        ModelPart root = context.bakeLayer(LAYER_LOCATION);
        this.plank = root.getChild("plank");
        this.normalChains = root.getChild("normalChains");
        this.vChains = root.getChild("vChains");
        this.boardModel = new BakedPartModel(root);
    }

    public static LayerDefinition createHangingSignLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("board", CubeListBuilder.create().texOffs(0, 12).addBox(-7.0F, 0.0F, -1.0F, 14.0F, 10.0F, 2.0F), PartPose.ZERO);
        partDefinition.addOrReplaceChild("plank", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -6.0F, -2.0F, 16.0F, 2.0F, 4.0F), PartPose.ZERO);
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("normalChains", CubeListBuilder.create(), PartPose.ZERO);
        partDefinition2.addOrReplaceChild("chainL1", CubeListBuilder.create().texOffs(0, 6).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 6.0F, 0.0F), PartPose.offsetAndRotation(-5.0F, -6.0F, 0.0F, 0.0F, -0.7853982F, 0.0F));
        partDefinition2.addOrReplaceChild("chainL2", CubeListBuilder.create().texOffs(6, 6).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 6.0F, 0.0F), PartPose.offsetAndRotation(-5.0F, -6.0F, 0.0F, 0.0F, 0.7853982F, 0.0F));
        partDefinition2.addOrReplaceChild("chainR1", CubeListBuilder.create().texOffs(0, 6).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 6.0F, 0.0F), PartPose.offsetAndRotation(5.0F, -6.0F, 0.0F, 0.0F, -0.7853982F, 0.0F));
        partDefinition2.addOrReplaceChild("chainR2", CubeListBuilder.create().texOffs(6, 6).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 6.0F, 0.0F), PartPose.offsetAndRotation(5.0F, -6.0F, 0.0F, 0.0F, 0.7853982F, 0.0F));
        partDefinition.addOrReplaceChild("vChains", CubeListBuilder.create().texOffs(14, 6).addBox(-6.0F, -6.0F, 0.0F, 12.0F, 6.0F, 0.0F), PartPose.ZERO);
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Override
    public HangingState createRenderState() {
        return new HangingState();
    }


    @Override
    public void extractRenderState(PineHangingSignBlockEntity entity, HangingState state, float partialTick, net.minecraft.world.phys.Vec3 cameraPos,
            ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(entity, state, crumbling);
        BlockState blockState = entity.getBlockState();
        boolean ceiling = blockState.getBlock() instanceof CeilingHangingSignBlock;
        state.ceiling = ceiling;
        if (blockState.getBlock() instanceof SignBlock signBlock) {
            state.yRotation = -signBlock.getYRotationDegrees(blockState);
        }
        boolean attached = !ceiling && blockState.getValue(BlockStateProperties.ATTACHED);
        state.attached = attached;
        this.plank.visible = !ceiling;
        this.vChains.visible = !ceiling && attached;
        this.normalChains.visible = ceiling || !attached;
        state.lineHeight = entity.getTextLineHeight();
        fillText(state, true, entity.getFrontText(), entity.getMaxTextLineWidth(), entity.getTextLineHeight(),
                new BlockPosHolder(entity.getBlockPos()));
        fillText(state, false, entity.getBackText(), entity.getMaxTextLineWidth(), entity.getTextLineHeight(),
                new BlockPosHolder(entity.getBlockPos()));
    }

    @Override
    public void submit(HangingState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.9375, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yRotation));
        poseStack.translate(0.0F, -0.3125F, 0.0F);
        collector.submitModel(boardModel, state, poseStack, RenderTypes.entityCutout(TEXTURE),
                state.lightCoords, OverlayTexture.NO_OVERLAY, EntityRenderState.NO_OUTLINE, state.breakProgress);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.5, 0.9375, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yRotation));
        poseStack.translate(0.0F, -0.3125F, 0.0F);
        poseStack.translate(TEXT_OFFSET.x, TEXT_OFFSET.y, TEXT_OFFSET.z);
        float scale = 0.015625F * 0.9F;
        poseStack.scale(scale, -scale, scale);
        submitHangingLines(state, true, poseStack, collector);
        submitHangingLines(state, false, poseStack, collector);
        poseStack.popPose();
    }

    private void submitHangingLines(HangingState state, boolean front, PoseStack poseStack, SubmitNodeCollector collector) {
        net.minecraft.util.FormattedCharSequence[] lines = front ? state.front : state.back;
        if (lines.length == 0) return;
        poseStack.pushPose();
        if (!front) poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        int color = front ? state.frontColor : state.backColor;
        int light = state.lightCoords;
        int outline = front ? state.frontOutline : state.backOutline;
        boolean glow = front ? state.frontGlow : state.backGlow;
        int m = 4 * state.lineHeight / 2;
        for (int p = 0; p < 4 && p < lines.length; p++) {
            float x = (float) (-font.width(lines[p]) / 2);
            collector.submitText(poseStack, x, (float) (p * state.lineHeight - m), lines[p], false,
                    glow ? net.minecraft.client.gui.Font.DisplayMode.SEE_THROUGH : net.minecraft.client.gui.Font.DisplayMode.POLYGON_OFFSET,
                    color, 0, glow ? 0xF000F0 : light, outline);
        }
        poseStack.popPose();
    }
}
