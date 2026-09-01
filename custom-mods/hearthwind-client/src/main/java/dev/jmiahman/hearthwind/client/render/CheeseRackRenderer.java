package dev.jmiahman.hearthwind.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class CheeseRackRenderer implements StorageTypeRenderer {
    @Override
    public void render(StorageRenderState state, PoseStack matrices, SubmitNodeCollector submitNodeCollector) {
        matrices.translate(-0.5f, 0.05f, -0.5f);
        if (!state.itemStates.isEmpty()) {
            ItemStackRenderState item1 = state.itemStates.get(0);
            if (!item1.isEmpty()) {
                item1.submit(matrices, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            }
        }
        if (state.itemStates.size() > 1) {
            ItemStackRenderState item2 = state.itemStates.get(1);
            if (!item2.isEmpty()) {
                matrices.translate(0f, 0.4f, 0f);
                item2.submit(matrices, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            }
        }
    }
}
