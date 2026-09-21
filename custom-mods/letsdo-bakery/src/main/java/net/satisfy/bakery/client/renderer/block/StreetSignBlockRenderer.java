package net.satisfy.bakery.client.renderer.block;

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
import net.satisfy.bakery.core.block.StreetSignBlock;
import net.satisfy.bakery.core.block.entity.StreetSignBlockEntity;

// 26.2: same layout, truncation and glow colors as before; text submits
// through the collector instead of a buffer source.
public class StreetSignBlockRenderer implements BlockEntityRenderer<StreetSignBlockEntity, StreetSignBlockRenderer.State> {
    private final Font font;

    public static class State extends BlockEntityRenderState {
        public Direction facing = Direction.NORTH;
        public final String[] lines = new String[3];
        public boolean glow;
    }

    public StreetSignBlockRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.font();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(StreetSignBlockEntity entity, State state, float partialTick, Vec3 cameraPos,
            ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(entity, state, crumbling);
        state.facing = entity.getBlockState().getValue(StreetSignBlock.FACING);
        state.glow = entity.isGlowing();
        for (int i = 0; i < 3; i++) {
            String str = entity.getText(i).getString();
            if (str.length() > 8) str = str.substring(0, 8);
            state.lines[i] = str;
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        poseStack.pushPose();
        poseStack.translate(0.5, 1.0, 0.5);
        Direction facing = state.facing;
        float rotation = facing == Direction.NORTH ? 180f : facing == Direction.WEST ? -90f : facing == Direction.EAST ? 90f : 0f;
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotation));
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-22.5f));
        poseStack.scale(0.01f, -0.01f, 0.01f);
        float[] offsetY = {15.0f, 35.0f, 50.0f};
        for (int i = 0; i < 3; i++) {
            String str = state.lines[i];
            if (str != null && !str.isEmpty()) {
                poseStack.pushPose();
                poseStack.translate(0.0f, offsetY[i], 0.0f);
                collector.submitText(poseStack, -font.width(str) / 2f, 0,
                        Component.literal(str).getVisualOrderText(), false,
                        state.glow ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL,
                        0xE8C992, 0, state.lightCoords, 0);
                poseStack.popPose();
            }
        }
        poseStack.popPose();
    }
}
