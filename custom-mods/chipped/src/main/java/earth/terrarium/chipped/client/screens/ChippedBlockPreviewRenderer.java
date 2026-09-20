package earth.terrarium.chipped.client.screens;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

/**
 * 26.2 PIP renderer for the Chipped workbench preview.
 *
 * <p>Reproduces the upstream isometric presentation (Aged 3.0.7
 * {@code RenderWindowWidget}): translate to preview center, apply
 * mode/door screen offsets, scale ~20px per block, rotate
 * {@code -30deg X / 45deg Y} around the block center, then submit each
 * adjacent block. Differences from upstream are confined to the 26.2
 * plumbing:
 * <ul>
 *   <li>Lighting is {@code Lighting.Entry.ITEMS_3D} via
 *   {@code gameRenderer.lighting().setupFor(...)} instead of
 *   {@code RenderSystem.setupGui3DDiffuseLighting(SCENE_LIGHT_1/2)} +
 *   {@code Lighting.setupFor3DItems()}.</li>
 *   <li>Blocks submit through {@code FakeLevel/FakeLevelImpl}
 *   ({@code BlockModel + SubmitNodeCollector}) instead of
 *   {@code BlockRenderDispatcher + VertexConsumer}.</li>
 *   <li>Screen offsets are converted from GUI pixels to world units
 *   ({@code / scale}) because the PIP pose is already centered and scaled
 *   by {@code PictureInPictureRenderer.prepare}; upstream applied them to
 *   an unscaled {@code PoseStack} after {@code translate(getX,getY)}.</li>
 *   <li>Upstream {@code pose.scale(-20,-20,-20)} is reproduced as PIP
 *   {@code (+s,+s,-s)} plus {@code scale(-1,-1,1)} here: centered cubes
 *   look the same either way, but off-center thin doors (west 0..3/16
 *   edge) need the mirror or they land ~13px east, outside the widget.</li>
 * </ul>
 */
public class ChippedBlockPreviewRenderer extends PictureInPictureRenderer<ChippedBlockPreviewRenderState> {
    /** Upstream widget-space centering (translate 46,46) minus PIP texture center (36,36 for 72px). */
    private static final float CENTER_FUDGE_X = 10.0F;
    private static final float CENTER_FUDGE_Y = 10.0F;

    static final BlockPos DOOR_BOTTOM = BlockPos.ZERO;
    static final BlockPos DOOR_TOP = BlockPos.ZERO.above();
    private static final Set<BlockPos> DOOR_BOTTOM_POSITIONS = Set.of(DOOR_BOTTOM);
    private static final Set<BlockPos> DOOR_TOP_POSITIONS = Set.of(DOOR_TOP);

    private final FakeLevel fakeLevel = new FakeLevel();

    @Override
    public Class<ChippedBlockPreviewRenderState> getRenderStateClass() {
        return ChippedBlockPreviewRenderState.class;
    }

    @Override
    protected String getTextureLabel() {
        return "chipped_workbench_preview";
    }

    @Override
    protected float getTranslateY(int height, int guiScale) {
        return height / 2.0F;
    }

    @Override
    protected void renderToTexture(ChippedBlockPreviewRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        Minecraft.getInstance().gameRenderer.lighting().setupFor(Lighting.Entry.ITEMS_3D);

        float scale = renderState.scale();
        poseStack.translate(
            (CENTER_FUDGE_X + renderState.xOffset()) / scale,
            (CENTER_FUDGE_Y + renderState.yOffset()) / scale,
            0.0F);
        // Parity with upstream pose.scale(-20,-20,-20): PIP prepare already
        // scales (+s,+s,-s), so mirror X/Y to reach (-s,-s,-s). Without this,
        // centered cubes look fine but off-center thin geometry (doors at the
        // west 0..3/16 edge) lands ~13px too far east and vanishes outside
        // the 72px widget. Matches upstream T(46+off)*S-*T0.5*R*T-0.5 exactly.
        poseStack.scale(-1.0F, -1.0F, 1.0F);

        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.XP.rotationDegrees(-30));
        poseStack.mulPose(Axis.YP.rotationDegrees(45));
        poseStack.translate(-0.5F, -0.5F, -0.5F);

        BlockState state = renderState.state();
        if (renderState.isDoor()) {
            fakeLevel.setState(state);
            fakeLevel.setPositions(DOOR_BOTTOM_POSITIONS);
            fakeLevel.renderBlock(poseStack, submitNodeCollector);
            BlockState upper = renderState.upperState();
            if (upper != null) {
                fakeLevel.setState(upper);
                fakeLevel.setPositions(DOOR_TOP_POSITIONS);
                fakeLevel.renderBlock(poseStack, submitNodeCollector);
            }
        } else {
            fakeLevel.setState(state);
            fakeLevel.setPositions(renderState.positions());
            fakeLevel.renderBlock(poseStack, submitNodeCollector);
        }
    }
}
