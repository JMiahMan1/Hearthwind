package net.adventurez.entity.render.feature;

import com.mojang.blaze3d.vertex.PoseStack;
import net.adventurez.entity.render.AdventureRenderState;
import net.adventurez.entity.model.EnderwarthogModel;
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
public class EnderwarthogEyesFeatureRenderer extends RenderLayer<AdventureRenderState, EnderwarthogModel> {
    private static final RenderType EYE_LAYER = RenderTypes.eyes(Identifier.fromNamespaceAndPath("adventurez", "textures/entity/feature/enderwarthog_eyes_feature.png"));
    private static final RenderType RARE_LAYER = RenderTypes.entityCutout(Identifier.fromNamespaceAndPath("adventurez", "textures/entity/feature/rare_enderwarthog.png"));

    public EnderwarthogEyesFeatureRenderer(RenderLayerParent<AdventureRenderState, EnderwarthogModel> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, AdventureRenderState state, float yRot, float xRot) {
        collector.submitModel(getParentModel(), state, poseStack, EYE_LAYER, 15728640, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
        if (state.getEntityData().get(net.adventurez.entity.EnderwarthogEntity.RARE_VARIANT)) {
            collector.submitModel(getParentModel(), state, poseStack, RARE_LAYER, 20, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
        }
    }
}
