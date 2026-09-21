package net.satisfy.candlelight.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.satisfy.candlelight.core.block.WallDecorationBlock;
import net.satisfy.candlelight.core.block.entity.WallDecorationBlockEntity;

// 26.2: same layout, truncation and glow color as before; text submits
// through the collector instead of a buffer source.
public class WallDecorationBlockRenderer implements BlockEntityRenderer<WallDecorationBlockEntity, WallDecorationBlockRenderer.State> {
    private final Font font;

    public static class State extends BlockEntityRenderState {
        public Direction facing = Direction.NORTH;
        public String line = "";
        public boolean glow;
    }

    public WallDecorationBlockRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.font();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(WallDecorationBlockEntity entity, State state, float partialTick, Vec3 cameraPos,
            ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(entity, state, crumbling);
        state.facing = entity.getBlockState().getValue(WallDecorationBlock.FACING);
        state.glow = entity.isGlowing();
        String str = entity.getText(0).getString();
        if (str.length() > 12) str = str.substring(0, 12);
        state.line = str;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        if (state.line == null || state.line.isEmpty()) return;
        poseStack.pushPose();
        poseStack.translate(0.5, 1.0, 0.5);
        Direction facing = state.facing;
        float rotation = facing == Direction.NORTH ? 180f : facing == Direction.WEST ? -90f : facing == Direction.EAST ? 90f : 0f;
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotation));
        poseStack.translate(0, -0.45, -0.425);
        poseStack.scale(0.01f, -0.01f, 0.01f);
        collector.submitText(poseStack, -font.width(state.line) / 2f, 0,
                Component.literal(state.line).getVisualOrderText(), false,
                state.glow ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL,
                0xFADFB0, 0, state.lightCoords, 0);
        poseStack.popPose();
    }
}
