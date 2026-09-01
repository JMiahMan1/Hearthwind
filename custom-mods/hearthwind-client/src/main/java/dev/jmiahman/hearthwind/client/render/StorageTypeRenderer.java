package dev.jmiahman.hearthwind.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;

@FunctionalInterface
public interface StorageTypeRenderer {
    void render(StorageRenderState state, PoseStack matrices, SubmitNodeCollector submitNodeCollector);
}
