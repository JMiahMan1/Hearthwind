package earth.terrarium.chipped.client.screens;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * 26.2 render-state for the Chipped workbench block preview.
 *
 * <p>Upstream rendered directly into {@code GuiGraphics} with a
 * {@code PoseStack} (BlockRenderDispatcher immediate path). In 26.2 all GUI
 * 3D is deferred: {@code RenderWindowWidget.extractWidgetRenderState}
 * submits this state via
 * {@code GuiGraphicsExtractor.guiRenderState.addPicturesInPictureState},
 * and {@link ChippedBlockPreviewRenderer} draws it to an offscreen texture
 * during {@code GuiRenderer.preparePictureInPicture}.
 *
 * <p>Fields mirror the upstream widget inputs: base {@code state} (lower
 * half for doors), optional {@code upperState} (non-null only for doors),
 * block {@code positions} for the current {@code Mode}, plus the
 * screen-pixel {@code xOffset/yOffset} that upstream applied before scaling
 * ({@code Mode} offsets, or {@code 5,12} for doors).
 */
public record ChippedBlockPreviewRenderState(
    BlockState state,
    @Nullable BlockState upperState,
    Set<BlockPos> positions,
    int xOffset,
    int yOffset,
    int x0,
    int y0,
    int x1,
    int y1,
    float scale,
    @Nullable ScreenRectangle scissorArea,
    @Nullable ScreenRectangle bounds
) implements PictureInPictureRenderState {
    /** GUI pixels per world unit; matches upstream {@code pose.scale(-20,-20,-20)}. */
    public static final float PREVIEW_SCALE = 20.0F;

    public ChippedBlockPreviewRenderState(
        BlockState state,
        @Nullable BlockState upperState,
        Set<BlockPos> positions,
        int xOffset,
        int yOffset,
        int x0,
        int y0,
        int x1,
        int y1,
        float scale,
        @Nullable ScreenRectangle scissorArea
    ) {
        this(state, upperState, positions, xOffset, yOffset, x0, y0, x1, y1, scale, scissorArea,
            PictureInPictureRenderState.getBounds(x0, y0, x1, y1, scissorArea));
    }

    public boolean isDoor() {
        return upperState != null;
    }
}
