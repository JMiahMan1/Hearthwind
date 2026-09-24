package net.adventurez.entity.render.feature;

import com.mojang.blaze3d.vertex.PoseStack;
import net.adventurez.entity.BlackstoneGolemEntity;
import net.adventurez.entity.render.AdventureRenderState;
import net.adventurez.entity.model.BlackstoneGolemModel;
import net.adventurez.init.RenderInit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class StoneGolemLavaFeatureRenderer extends RenderLayer<AdventureRenderState, BlackstoneGolemModel> {
    private static final RenderType LAVA_LAYER = RenderTypes.entityTranslucentEmissive(Identifier.fromNamespaceAndPath("adventurez", "textures/entity/feature/lava_feature_golem.png"));
    private static final RenderType FLOWING_LAVA_LAYER = RenderTypes.entityCutout(Identifier.fromNamespaceAndPath("adventurez", "textures/entity/feature/lava_feature_golem.png"));

    public StoneGolemLavaFeatureRenderer(RenderLayerParent<AdventureRenderState, BlackstoneGolemModel> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, AdventureRenderState state, float yRot, float xRot) {
        if (state.getEntityData().get(BlackstoneGolemEntity.HALF_LIFE_CHANGE)) {
            return;
        }
        int lavaFlow = state.getEntityData().get(BlackstoneGolemEntity.LAVA_TEXTURE);
        RenderType layer = LAVA_LAYER;
        if (lavaFlow < 240 && !RenderInit.isCanvasLoaded && !RenderInit.isSodiumLoaded) {
            layer = FLOWING_LAVA_LAYER;
        } else {
            lavaFlow = Integer.MAX_VALUE;
        }
        collector.submitModel(getParentModel(), state, poseStack, layer, lavaFlow, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
    }
}
