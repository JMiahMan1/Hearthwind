package net.adventurez.entity.render.feature;

import com.mojang.blaze3d.vertex.PoseStack;
import net.adventurez.entity.render.AdventureRenderState;
import net.adventurez.entity.model.EnderWhaleModel;
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
public class EnderWhaleGlowFeatureRenderer extends RenderLayer<AdventureRenderState, EnderWhaleModel> {
    private static final RenderType GLOW_LAYER = RenderTypes.entityTranslucentEmissive(Identifier.fromNamespaceAndPath("adventurez", "textures/entity/feature/ender_whale_glow_feature.png"));

    public EnderWhaleGlowFeatureRenderer(RenderLayerParent<AdventureRenderState, EnderWhaleModel> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, AdventureRenderState state, float yRot, float xRot) {
        collector.submitModel(getParentModel(), state, poseStack, GLOW_LAYER, 15728640, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
    }
}
