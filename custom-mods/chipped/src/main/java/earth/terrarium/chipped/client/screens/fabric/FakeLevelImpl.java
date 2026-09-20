package earth.terrarium.chipped.client.screens.fabric;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 26.2 fabric implementation of the upstream {@code FakeLevelImpl.renderBatched}.
 *
 * <p>Upstream (Aged 3.0.7) delegated to
 * {@code BlockRenderDispatcher.renderBatched(state, pos, level, poseStack,
 * consumer, checkSides, random)}. That dispatcher is gone in 26.2; blocks
 * submit through {@code BlockModelSet.get(state).update(renderState, ...)}
 * then {@code BlockModelRenderState.submit(poseStack, collector, light,
 * overlay, outline)} — the same path block-entity renderers use (see
 * {@code DungeonGateRenderer}, {@code ClientUtil.renderBlock}).
 *
 * <p>Preview uses full-bright GUI light ({@code 15728880}, matching
 * {@code GuiEntityRenderer} / {@code OversizedItemRenderer} /
 * {@code GuiBookModelRenderer}) and {@code ITEMS_3D} entry lighting set up
 * by {@code ChippedBlockPreviewRenderer}. The {@code level} parameter is
 * retained for API parity (future tint-in-world); the current
 * {@code BlockModel} path resolves static tints from
 * {@code BlockColors.getTintSources}, matching block-item rendering.
 */
public class FakeLevelImpl {
    /** Full-bright packed light used by vanilla PIP renderers for GUI 3D. */
    public static final int PREVIEW_LIGHT = 15728880;

    public static void renderBatched(BlockPos pos, BlockState state, BlockAndLightGetter level, PoseStack poseStack, SubmitNodeCollector collector, RandomSource random) {
        Minecraft mc = Minecraft.getInstance();
        var blockModel = mc.getModelManager().getBlockModelSet().get(state);
        BlockModelRenderState renderState = new BlockModelRenderState();
        long seed = state.getSeed(pos);
        blockModel.update(renderState, state, BlockDisplayContext.create(), seed);
        renderState.submit(poseStack, collector, PREVIEW_LIGHT, OverlayTexture.NO_OVERLAY, 0);
    }
}
