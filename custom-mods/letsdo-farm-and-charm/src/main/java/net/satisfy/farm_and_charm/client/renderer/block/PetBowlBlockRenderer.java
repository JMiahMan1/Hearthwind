package net.satisfy.farm_and_charm.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
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
import net.satisfy.farm_and_charm.core.block.PetBowlBlock;
import net.satisfy.farm_and_charm.core.block.entity.PetBowlBlockEntity;

// 26.2: name-tag text submits through the collector (same text, color,
// scale and facing as before).
public class PetBowlBlockRenderer implements BlockEntityRenderer<PetBowlBlockEntity, PetBowlBlockRenderer.State> {
    private final Font font;

    public static class State extends BlockEntityRenderState {
        public Direction facing = Direction.NORTH;
        public String text = "";
    }

    public PetBowlBlockRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.font();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(PetBowlBlockEntity entity, State state, float partialTick, Vec3 cameraPos,
            ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(entity, state, crumbling);
        state.facing = entity.getBlockState().getValue(PetBowlBlock.FACING);
        String str = entity.getText().getString();
        if (str.length() > 6) str = str.substring(0, 6);
        state.text = str;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        poseStack.pushPose();

        poseStack.translate(0.5, 0.165, 0.5);

        float rotation = switch (state.facing) {
            case SOUTH -> 0f;
            case WEST -> -90f;
            case EAST -> 90f;
            default -> 180f;
        };
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        poseStack.translate(0.0, 0.0, 0.2505);

        poseStack.scale(0.01f, -0.01f, 0.01f);

        if (!state.text.isEmpty()) {
            collector.submitText(poseStack, -font.width(state.text) / 2f, 0,
                    Component.literal(state.text).getVisualOrderText(), false,
                    Font.DisplayMode.NORMAL, state.lightCoords, 0xD8C4A0, 0, 0);
        }

        poseStack.popPose();
    }
}
