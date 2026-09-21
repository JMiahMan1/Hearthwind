package net.satisfy.vinery.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
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
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.satisfy.vinery.core.Vinery;
import net.satisfy.vinery.core.block.entity.DarkCherrySignBlockEntity;

import java.util.List;

// 26.2: same poses, textures and text layout as before; board submits
// through a BakedPartModel, text through the collector.
@Environment(EnvType.CLIENT)
public class DarkCherrySignRenderer<T extends DarkCherrySignBlockEntity, S extends DarkCherrySignRenderer.State> implements BlockEntityRenderer<T, S> {
    private static final int OUTLINE_RENDER_DISTANCE = Mth.square(16);
    private static final Vec3 TEXT_OFFSET = new Vec3(0.0, 0.3333333432674408, 0.046666666865348816);
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Vinery.MOD_ID, "textures/entity/signs/dark_cherry.png");
    public static final net.minecraft.client.model.geom.ModelLayerLocation LAYER_LOCATION =
            new net.minecraft.client.model.geom.ModelLayerLocation(net.satisfy.vinery.core.Vinery.identifier("dark_cherry_sign"), "main");
    protected final Font font;
    private final ModelPart stick;
    private final BakedPartModel boardModel;

    public static class State extends BlockEntityRenderState {
        public boolean standing = true;
        public float yRotation;
        public boolean stickVisible = true;
        public FormattedCharSequence[] front = new FormattedCharSequence[0];
        public FormattedCharSequence[] back = new FormattedCharSequence[0];
        public int frontColor;
        public int backColor;
        public boolean frontGlow;
        public boolean backGlow;
        public int frontOutline;
        public int backOutline;
        public int lineHeight = 10;
        public boolean frontHasOutline;
        public boolean backHasOutline;
    }

    public DarkCherrySignRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart root = context.bakeLayer(LAYER_LOCATION);
        this.stick = root.getChild("stick");
        this.boardModel = new BakedPartModel(root);
        this.font = context.font();
    }

    public static LayerDefinition createSignLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("sign", CubeListBuilder.create().texOffs(0, 0).addBox(-12.0F, -14.0F, -1.0F, 24.0F, 12.0F, 2.0F), PartPose.ZERO);
        partDefinition.addOrReplaceChild("stick", CubeListBuilder.create().texOffs(0, 14).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 14.0F, 2.0F), PartPose.ZERO);
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Override
    @SuppressWarnings("unchecked")
    public S createRenderState() {
        return (S) new State();
    }

    protected void fillText(State state, boolean front, SignText signText, int maxWidth, int lineHeight, BlockPosHolder pos) {
        FormattedCharSequence[] lines = signText.getRenderMessages(false,
                component -> {
                    List<FormattedCharSequence> list = font.split(component, maxWidth);
                    return list.isEmpty() ? FormattedCharSequence.EMPTY : list.get(0);
                });
        int color = signText.getColor().getTextColor();
        boolean glow = signText.hasGlowingText();
        boolean outline = glow && isOutlineVisible(pos.pos(), color);
        if (front) {
            state.front = lines;
            state.frontGlow = glow;
            state.frontHasOutline = outline;
            state.frontColor = glow ? color : ARGB.scaleRGB(color, 0.4F);
            state.frontOutline = outline ? ARGB.scaleRGB(color, 0.4F) : 0;
            state.lineHeight = lineHeight;
        } else {
            state.back = lines;
            state.backGlow = glow;
            state.backHasOutline = outline;
            state.backColor = glow ? color : ARGB.scaleRGB(color, 0.4F);
            state.backOutline = outline ? ARGB.scaleRGB(color, 0.4F) : 0;
        }
    }

    @Override
    public void extractRenderState(T entity, S state, float partialTick, net.minecraft.world.phys.Vec3 cameraPos,
            ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(entity, state, crumbling);
        BlockState blockState = entity.getBlockState();
        state.standing = blockState.getBlock() instanceof StandingSignBlock;
        if (blockState.getBlock() instanceof SignBlock signBlock) {
            state.yRotation = -signBlock.getYRotationDegrees(blockState);
        }
        state.stickVisible = state.standing;
        fillText(state, true, entity.getFrontText(), entity.getMaxTextLineWidth(), entity.getTextLineHeight(), new BlockPosHolder(entity.getBlockPos()));
        fillText(state, false, entity.getBackText(), entity.getMaxTextLineWidth(), entity.getTextLineHeight(), new BlockPosHolder(entity.getBlockPos()));
    }

    @Override
    public void submit(S state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.75F * 0.6666667F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yRotation));
        if (!state.standing) {
            poseStack.translate(0.0F, -0.3125F, -0.4375F);
        }
        poseStack.pushPose();
        poseStack.scale(0.6666667F, -0.6666667F, -0.6666667F);
        this.stick.visible = state.stickVisible;
        collector.submitModel(boardModel, state, poseStack, RenderTypes.entityCutout(TEXTURE),
                state.lightCoords, OverlayTexture.NO_OVERLAY, EntityRenderState.NO_OUTLINE, state.breakProgress);
        poseStack.popPose();
        submitLines(state, true, poseStack, collector);
        submitLines(state, false, poseStack, collector);
        poseStack.popPose();
    }

    protected void submitLines(S state, boolean front, PoseStack poseStack, SubmitNodeCollector collector) {
        FormattedCharSequence[] lines = front ? state.front : state.back;
        if (lines.length == 0) return;
        poseStack.pushPose();
        if (!front) poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        float scale = 0.015625F * 0.6666667F;
        poseStack.translate(TEXT_OFFSET.x, TEXT_OFFSET.y, TEXT_OFFSET.z);
        poseStack.scale(scale, -scale, scale);
        int color = front ? state.frontColor : state.backColor;
        int light = state.lightCoords;
        int outline = front ? state.frontOutline : state.backOutline;
        boolean glow = front ? state.frontGlow : state.backGlow;
        int m = 4 * state.lineHeight / 2;
        for (int p = 0; p < 4 && p < lines.length; p++) {
            float x = (float) (-font.width(lines[p]) / 2);
            collector.submitText(poseStack, x, (float) (p * state.lineHeight - m), lines[p], false,
                    glow ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.POLYGON_OFFSET,
                    color, 0, glow ? 0xF000F0 : light, outline);
        }
        poseStack.popPose();
    }

    protected boolean isOutlineVisible(net.minecraft.core.BlockPos blockPos, int textColor) {
        if (textColor == DyeColor.BLACK.getTextColor()) {
            return true;
        }
        var minecraft = net.minecraft.client.Minecraft.getInstance();
        var cameraEntity = minecraft.getCameraEntity();
        return cameraEntity != null && cameraEntity.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(blockPos)) < OUTLINE_RENDER_DISTANCE;
    }

    record BlockPosHolder(net.minecraft.core.BlockPos pos) {
    }
}
