package earth.terrarium.chipped.client.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import earth.terrarium.chipped.client.screens.fabric.FakeLevelImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * 26.2 port of upstream FakeLevel (Aged 3.0.7).
 *
 * <p>Upstream implemented {@code BlockAndTintGetter} with
 * {@code getShade() -> 1}, {@code getLightEngine() -> throw}, and
 * {@code getBlockTint()} delegating to the real client level. Rendering went
 * through {@code BlockRenderDispatcher + ItemBlockRenderTypes +
 * RenderSystem.setupGui3DDiffuseLighting} with a direct
 * {@code PoseStack + VertexConsumer} path.
 *
 * <p>26.2 changes:
 * <ul>
 *   <li>{@code BlockRenderDispatcher} / {@code ItemBlockRenderTypes} /
 *   {@code setupGui3DDiffuseLighting} are gone. GUI 3D goes through the
 *   {@code GuiGraphicsExtractor} render-state model and
 *   picture-in-picture ({@code PictureInPictureRenderer}) with
 *   {@code SubmitNodeCollector + BlockModel} submits.</li>
 *   <li>{@code BlockAndTintGetter} now lives in
 *   {@code net.minecraft.client.renderer.block} and requires
 *   {@code cardinalLighting()} plus {@code getBlockTint()}.
 *   {@code BlockAndLightGetter.getBrightness()} defaults delegate to
 *   {@code getLightEngine()}, so this level returns
 *   {@code LevelLightEngine.EMPTY} (never null) and keeps full-bright 15
 *   overrides for the preview.</li>
 *   <li>Lighting uses {@code Lighting.Entry.ITEMS_3D} via
 *   {@code gameRenderer.lighting().setupFor(...)} (see
 *   {@code ChippedBlockPreviewRenderer}); the old
 *   {@code SCENE_LIGHT_1/2} vectors are kept as documentation of the
 *   upstream GUI diffuse setup.</li>
 * </ul>
 */
public class FakeLevel implements BlockAndTintGetter {
    @Nullable
    private BlockState state;
    @Nullable
    private Set<BlockPos> positions;

    /** Upstream GUI diffuse directions (pre-26.2 RenderSystem.setupGui3DDiffuseLighting args). Kept for reference. */
    public static final org.joml.Vector3f SCENE_LIGHT_1 = new org.joml.Vector3f(1, 0, 1);
    public static final org.joml.Vector3f SCENE_LIGHT_2 = new org.joml.Vector3f(-1, 1, -1);

    @Override
    public CardinalLighting cardinalLighting() {
        return CardinalLighting.DEFAULT;
    }

    @Override
    public int getBlockTint(BlockPos blockPos, ColorResolver colorResolver) {
        var level = Minecraft.getInstance().level;
        if (level != null) {
            return level.getBlockTint(blockPos, colorResolver);
        }
        return -1;
    }

    @Override
    public int getBrightness(LightLayer lightType, BlockPos blockPos) {
        return 15;
    }

    @Override
    public int getRawBrightness(BlockPos blockPos, int amount) {
        return 15;
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        return null;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return (state != null && positions != null && positions.contains(pos)) ? state : Blocks.AIR.defaultBlockState();
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return Blocks.AIR.defaultBlockState().getFluidState();
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public int getMinY() {
        return 0;
    }

    public void setState(BlockState state) {
        this.state = state;
    }

    public void setPositions(Set<BlockPos> positions) {
        this.positions = positions;
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return LevelLightEngine.EMPTY;
    }

    /**
     * 26.2 replacement for the old immediate
     * {@code renderBlock(PoseStack)} (BlockRenderDispatcher + BufferSource).
     * Iterates {@link #positions} and submits each block through
     * {@link FakeLevelImpl} using the 26.2 {@code BlockModel} path with full
     * bright light. Must be called from a PIP
     * {@code renderToTexture(PoseStack, SubmitNodeCollector)} context, not
     * from GUI extract with a detached PoseStack.
     */
    public void renderBlock(PoseStack poseStack, SubmitNodeCollector collector) {
        if (state == null || positions == null) return;
        // BlockModel path seeds per-pos via state.getSeed(pos); random is
        // retained for upstream signature parity only.
        RandomSource random = RandomSource.create();
        BlockState current = state;
        for (BlockPos pos : positions) {
            poseStack.pushPose();
            poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
            FakeLevelImpl.renderBatched(pos, current, this, poseStack, collector, random);
            poseStack.popPose();
        }
    }

    /**
     * 26.2 replacement for the old static
     * {@code renderBatched(..., VertexConsumer, boolean, RandomSource, RenderType)}.
     * The {@code checkSides} cull flag is obsolete: the PIP
     * {@code submitBlockModel} path renders all model parts and relies on
     * depth testing for adjacent preview blocks (interior faces are hidden).
     */
    public static void renderBatched(BlockPos pos, BlockState state, BlockAndLightGetter level, PoseStack poseStack, SubmitNodeCollector collector, boolean checkSides, RandomSource random) {
        FakeLevelImpl.renderBatched(pos, state, level, poseStack, collector, random);
    }
}
