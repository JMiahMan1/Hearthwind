package net.adventurez.entity.render.feature;

import com.mojang.blaze3d.vertex.PoseStack;
import net.adventurez.entity.render.AdventureRenderState;
import net.adventurez.entity.model.SummonerModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;

@Environment(EnvType.CLIENT)
public class SummonerEntityShieldFeatureRenderer extends RenderLayer<AdventureRenderState, SummonerModel> {
    private static final RenderType SHIELD_LAYER = RenderTypes.entityGlint();

    public SummonerEntityShieldFeatureRenderer(RenderLayerParent<AdventureRenderState, SummonerModel> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, AdventureRenderState state, float yRot, float xRot) {
        if (state.getEntityData().get(net.adventurez.entity.SummonerEntity.INVULNERABLE_SHIELD)) {
            collector.submitModel(getParentModel(), state, poseStack, SHIELD_LAYER, 15728640, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
        }
    }
}
