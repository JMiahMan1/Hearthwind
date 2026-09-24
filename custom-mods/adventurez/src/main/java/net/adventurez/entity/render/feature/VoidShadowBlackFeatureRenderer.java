package net.adventurez.entity.render.feature;

import com.mojang.blaze3d.vertex.PoseStack;
import net.adventurez.entity.VoidShadowEntity;
import net.adventurez.entity.render.AdventureRenderState;
import net.adventurez.entity.model.VoidShadowModel;
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
public class VoidShadowBlackFeatureRenderer extends RenderLayer<AdventureRenderState, VoidShadowModel> {
    private static final RenderType BLACK_LAYER = RenderTypes.entityCutout(Identifier.fromNamespaceAndPath("adventurez", "textures/entity/black_void_shadow.png"));
    private static final RenderType EYE_LAYER = RenderTypes.eyes(Identifier.fromNamespaceAndPath("adventurez", "textures/entity/feature/black_void_shadow_eyes_feature.png"));

    public VoidShadowBlackFeatureRenderer(RenderLayerParent<AdventureRenderState, VoidShadowModel> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, AdventureRenderState state, float yRot, float xRot) {
        if (state.getEntityData().get(VoidShadowEntity.HALF_LIFE_CHANGE)) {
            collector.submitModel(getParentModel(), state, poseStack, BLACK_LAYER, 15728640, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
            collector.submitModel(getParentModel(), state, poseStack, EYE_LAYER, 15728640, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
        }
    }
}
