package dev.jmiahman.hearthwind.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class NineBottleRenderer implements StorageTypeRenderer {
    @Override
    public void render(StorageRenderState state, PoseStack matrices, SubmitNodeCollector submitNodeCollector) {
        matrices.translate(-0.13, 0.335, 0.125);
        matrices.scale(0.9f, 0.9f, 0.9f);

        for (int i = 0; i < state.itemStates.size() && i < 9; i++) {
            ItemStackRenderState itemState = state.itemStates.get(i);
            if (itemState.isEmpty()) continue;

            matrices.pushPose();
            int line = i >= 6 ? 3 : i >= 3 ? 2 : 1;
            float x = line == 1 ? -0.35f * i : line == 2 ? -0.35f * (i - 3) : -0.35f * (i - 6);
            float y = line == 1 ? 0f : line == 2 ? -0.33f : -0.66f;

            matrices.translate(x, y, 0f);
            matrices.mulPose(Axis.XN.rotationDegrees(90f));

            itemState.submit(matrices, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            matrices.popPose();
        }
    }
}
