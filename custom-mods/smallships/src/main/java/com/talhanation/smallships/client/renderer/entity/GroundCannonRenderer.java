package com.talhanation.smallships.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.talhanation.smallships.SmallShipsMod;
import com.talhanation.smallships.client.model.CannonModel;
import com.talhanation.smallships.client.renderer.entity.state.GroundCannonRenderState;
import com.talhanation.smallships.world.entity.cannon.Cannon;
import com.talhanation.smallships.world.entity.cannon.GroundCannonEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class GroundCannonRenderer extends EntityRenderer<GroundCannonEntity, GroundCannonRenderState> {
    private final CannonModel model = new CannonModel();

    public GroundCannonRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public @NotNull GroundCannonRenderState createRenderState() {
        return new GroundCannonRenderState();
    }

    @Override
    public void extractRenderState(GroundCannonEntity entity, GroundCannonRenderState state, float f) {
        super.extractRenderState(entity, state, f);
        state.textureLocation = this.getTextureLocation(entity);
        state.cannon = entity.getCannon();
        state.partialTicks = f;
    }

    public @NotNull Identifier getTextureLocation(GroundCannonEntity entity) {
        return Identifier.fromNamespaceAndPath(SmallShipsMod.MOD_ID, "textures/entity/cannon/ship_cannon.png");
    }

    @Override
    public void submit(GroundCannonRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        poseStack.scale(-1.3F, -1.3F, 1.3F);

        Cannon cannon = state.cannon;
        if (cannon != null) {
            float lerpYaw = -(cannon.getPrevYaw() + (cannon.getYaw() - cannon.getPrevYaw()) * state.partialTicks);
            poseStack.mulPose(Axis.YP.rotationDegrees(lerpYaw));
        }

        poseStack.pushPose();
        poseStack.mulPose(Axis.YN.rotationDegrees(180));
        poseStack.scale(0.6F, 0.6F, 0.6F);
        poseStack.translate(0, -1.5, 0);

        if (cannon != null) {
            float pitch = cannon.getPrevPitch() + state.partialTicks * (cannon.getPitch() - cannon.getPrevPitch());
            model.setLaufPitch(pitch);
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        net.minecraft.client.model.Model rawModel = this.model;
        nodeCollector.order(0).submitModel(rawModel, state, poseStack, state.textureLocation, 15728880, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF, null);
        poseStack.popPose();

        poseStack.popPose();
        super.submit(state, poseStack, nodeCollector, cameraRenderState);
    }
}
