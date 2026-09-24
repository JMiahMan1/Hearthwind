package net.adventurez.entity.render.feature;

import com.mojang.math.Axis;
import com.mojang.blaze3d.vertex.PoseStack;
import net.adventurez.entity.render.AdventureRenderState;
import net.adventurez.entity.model.OrcModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;

@Environment(EnvType.CLIENT)
public class OrcInventoryFeatureRenderer extends RenderLayer<AdventureRenderState, OrcModel> {
    public OrcInventoryFeatureRenderer(RenderLayerParent<AdventureRenderState, OrcModel> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, AdventureRenderState state, float yRot, float xRot) {
        if (!state.inventoryItem.isEmpty()) {
            poseStack.pushPose();
            ModelPart torso = getParentModel().getTorso();
            torso.translateAndRotate(poseStack);
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            poseStack.translate(-0.295, -0.3, 0.0);
            state.inventoryItem.submit(poseStack, collector, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
            poseStack.popPose();
        }
    }
}
