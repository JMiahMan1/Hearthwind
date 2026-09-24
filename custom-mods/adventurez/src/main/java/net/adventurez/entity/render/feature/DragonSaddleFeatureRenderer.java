package net.adventurez.entity.render.feature;

import com.mojang.blaze3d.vertex.PoseStack;
import net.adventurez.entity.render.AdventureRenderState;
import net.adventurez.entity.model.DragonModel;
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
public class DragonSaddleFeatureRenderer extends RenderLayer<AdventureRenderState, DragonModel> {
    private static final RenderType SADDLE_LAYER = RenderTypes.entityCutout(Identifier.fromNamespaceAndPath("adventurez", "textures/entity/feature/dragon_saddle.png"));

    public DragonSaddleFeatureRenderer(RenderLayerParent<AdventureRenderState, DragonModel> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, AdventureRenderState state, float yRot, float xRot) {
        if (state.hasSaddle) {
            collector.submitModel(getParentModel(), state, poseStack, SADDLE_LAYER, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
        }
    }
}
