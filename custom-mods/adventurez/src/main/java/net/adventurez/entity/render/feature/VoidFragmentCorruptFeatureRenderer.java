package net.adventurez.entity.render.feature;

import com.mojang.blaze3d.vertex.PoseStack;
import net.adventurez.entity.render.AdventureRenderState;
import net.adventurez.entity.model.VoidFragmentModel;
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
public class VoidFragmentCorruptFeatureRenderer extends RenderLayer<AdventureRenderState, VoidFragmentModel> {
    private static final RenderType CORRUPT_LAYER = RenderTypes.entityTranslucentEmissive(Identifier.fromNamespaceAndPath("adventurez", "textures/entity/feature/void_fragment_corrupt_feature.png"));

    public VoidFragmentCorruptFeatureRenderer(RenderLayerParent<AdventureRenderState, VoidFragmentModel> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, AdventureRenderState state, float yRot, float xRot) {
        if (state.getEntityData().get(net.adventurez.entity.VoidFragmentEntity.IS_VOID_ORB)) {
            collector.submitModel(getParentModel(), state, poseStack, CORRUPT_LAYER, 15728640, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
        }
    }
}
