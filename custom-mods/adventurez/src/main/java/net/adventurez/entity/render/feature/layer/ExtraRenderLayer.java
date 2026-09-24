package net.adventurez.entity.render.feature.layer;

import net.adventurez.init.RenderInit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public final class ExtraRenderLayer {
    private ExtraRenderLayer() {
    }

    public static RenderType getGlowing(Identifier identifier) {
        return RenderInit.isCanvasLoaded ? RenderTypes.entityCutout(identifier) : RenderTypes.eyes(identifier);
    }
}
