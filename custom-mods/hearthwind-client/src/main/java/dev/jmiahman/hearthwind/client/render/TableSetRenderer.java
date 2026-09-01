package dev.jmiahman.hearthwind.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class TableSetRenderer implements StorageTypeRenderer {
    @Override
    public void render(StorageRenderState state, PoseStack matrices, SubmitNodeCollector submitNodeCollector) {
        if (state.itemStates.isEmpty()) return;
        ItemStackRenderState itemState = state.itemStates.get(0);
        if (itemState.isEmpty()) return;

        float oP = 1.0f / 16.0f;
        matrices.translate(0.0f, 0.5f + oP, 0.0f);
        matrices.scale(0.6f, 0.6f, 0.6f);
        matrices.mulPose(Axis.XP.rotationDegrees(90f));

        itemState.submit(matrices, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
    }
}
