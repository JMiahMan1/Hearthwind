package earth.terrarium.chipped.client.screens;

import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.Supplier;

public class RenderWindowWidget extends AbstractWidget {
    public static final BlockPos ORIGIN = BlockPos.ZERO;
    public static final BlockPos NORTH = BlockPos.ZERO.north();
    public static final BlockPos NORTH_UP = BlockPos.ZERO.north().above();
    public static final BlockPos SOUTH = BlockPos.ZERO.south();
    public static final BlockPos UP = BlockPos.ZERO.above();
    public static final BlockPos DOWN = BlockPos.ZERO.below();
    private static final Set<BlockPos> DOOR_POSITIONS_BOTTOM = Set.of(ORIGIN);
    private static final Set<BlockPos> DOOR_POSITIONS_TOP = Set.of(UP);

    public enum Mode {
        SINGLE_BLOCK(0, 0, ORIGIN),
        HORIZONTAL_BLOCK(0, 0, ORIGIN, NORTH, SOUTH),
        VERTICAL_BLOCK(0, 0, ORIGIN, UP, DOWN),
        TWO_BY_TWO(-7, 5, ORIGIN, NORTH, UP, NORTH_UP);

        private final int xOffset;
        private final int yOffset;
        private final Set<BlockPos> positions;

        Mode(int xOffset, int yOffset, BlockPos... positions) {
            this.xOffset = xOffset;
            this.yOffset = yOffset;
            this.positions = Set.of(positions);
        }
    }

    private final Supplier<Mode> mode;
    private final Supplier<@Nullable BlockState> state;

    public RenderWindowWidget(int x, int y, int width, int height, Supplier<Mode> mode, Supplier<BlockState> state) {
        super(x, y, width, height, CommonComponents.EMPTY);
        this.mode = mode;
        this.state = state;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        Mode mode = this.mode.get();
        BlockState state = this.state.get();
        if (state == null) return;
        boolean isDoor = state.getBlock() instanceof DoorBlock;

        // 26.2: upstream built a detached PoseStack and rendered immediately
        // via FakeLevel/BlockRenderDispatcher. GUI 3D is now deferred: submit
        // a PIP state and let ChippedBlockPreviewRenderer draw it during
        // GuiRenderer.preparePictureInPicture (same isometric + offsets).
        int xOffset;
        int yOffset;
        Set<BlockPos> positions;
        BlockState upper = null;
        if (isDoor) {
            xOffset = 5;
            yOffset = 12;
            positions = DOOR_POSITIONS_BOTTOM;
            upper = state.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER);
        } else {
            xOffset = mode.xOffset;
            yOffset = mode.yOffset;
            positions = mode.positions;
        }
        graphics.guiRenderState.addPicturesInPictureState(new ChippedBlockPreviewRenderState(
            state,
            upper,
            positions,
            xOffset,
            yOffset,
            getX(),
            getY(),
            getX() + getWidth(),
            getY() + getHeight(),
            ChippedBlockPreviewRenderState.PREVIEW_SCALE,
            graphics.scissorStack.peek()));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean inside) {
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
