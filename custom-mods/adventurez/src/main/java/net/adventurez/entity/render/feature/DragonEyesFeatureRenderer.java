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
public class DragonEyesFeatureRenderer extends RenderLayer<AdventureRenderState, DragonModel> {
    private static final RenderType EYE_LAYER = RenderTypes.eyes(Identifier.fromNamespaceAndPath("adventurez", "textures/entity/feature/dragon_eyes_feature.png"));
    private static final RenderType FRIENDLY_EYE_LAYER = RenderTypes.eyes(Identifier.fromNamespaceAndPath("adventurez", "textures/entity/feature/friendly_dragon_eyes_feature.png"));
    private static final RenderType FRIENDLY_ORANGE_EYE_LAYER = RenderTypes.eyes(Identifier.fromNamespaceAndPath("adventurez", "textures/entity/feature/friendly_orange_dragon_eyes_feature.png"));

    public DragonEyesFeatureRenderer(RenderLayerParent<AdventureRenderState, DragonModel> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, AdventureRenderState state, float yRot, float xRot) {
        RenderType layer = EYE_LAYER;
        if (state.tamed) {
            layer = state.otherEyes ? FRIENDLY_ORANGE_EYE_LAYER : FRIENDLY_EYE_LAYER;
        }
        collector.submitModel(getParentModel(), state, poseStack, layer, 15728640, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
    }
}
