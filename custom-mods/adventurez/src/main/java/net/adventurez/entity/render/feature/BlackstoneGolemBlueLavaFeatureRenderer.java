package net.adventurez.entity.render.feature;

import com.mojang.blaze3d.vertex.PoseStack;
import net.adventurez.entity.render.AdventureRenderState;
import net.adventurez.entity.model.BlackstoneGolemModel;
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
public class BlackstoneGolemBlueLavaFeatureRenderer extends RenderLayer<AdventureRenderState, BlackstoneGolemModel> {
    private static final RenderType BLUE_LAVA_LAYER = RenderTypes.entityTranslucentEmissive(Identifier.fromNamespaceAndPath("adventurez", "textures/entity/feature/blue_lava_feature_golem.png"));

    public BlackstoneGolemBlueLavaFeatureRenderer(RenderLayerParent<AdventureRenderState, BlackstoneGolemModel> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, AdventureRenderState state, float yRot, float xRot) {
        if (state.getEntityData().get(net.adventurez.entity.BlackstoneGolemEntity.HALF_LIFE_CHANGE)) {
            collector.submitModel(getParentModel(), state, poseStack, BLUE_LAVA_LAYER, 15728640, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
        }
    }
}
