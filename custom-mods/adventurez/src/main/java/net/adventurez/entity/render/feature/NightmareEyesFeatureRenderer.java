package net.adventurez.entity.render.feature;

import com.mojang.blaze3d.vertex.PoseStack;
import net.adventurez.entity.NightmareEntity;
import net.adventurez.entity.render.feature.layer.ExtraRenderLayer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.animal.equine.HorseModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.EquineRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;

@Environment(EnvType.CLIENT)
public class NightmareEyesFeatureRenderer extends RenderLayer<EquineRenderState, HorseModel> {
    private static final RenderType EYE_LAYER = ExtraRenderLayer.getGlowing(net.minecraft.resources.Identifier.fromNamespaceAndPath("adventurez", "textures/entity/feature/nightmare_eyes_feature.png"));

    public NightmareEyesFeatureRenderer(RenderLayerParent<EquineRenderState, HorseModel> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, EquineRenderState state, float yRot, float xRot) {
        collector.submitModel(getParentModel(), state, poseStack, EYE_LAYER, 15728640, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
    }
}
