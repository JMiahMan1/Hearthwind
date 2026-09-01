package com.talhanation.smallships.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.talhanation.smallships.SmallShipsMod;
import com.talhanation.smallships.client.model.CannonBallModel;
import com.talhanation.smallships.client.renderer.entity.state.CannonBallRenderState;
import com.talhanation.smallships.world.entity.projectile.CannonBallEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class CannonBallRenderer extends EntityRenderer<CannonBallEntity, CannonBallRenderState> {
    private final CannonBallModel model;

    public CannonBallRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new CannonBallModel();
        this.shadowRadius = 0.25F;
    }

    @Override
    public @NotNull CannonBallRenderState createRenderState() {
        return new CannonBallRenderState();
    }

    @Override
    public void extractRenderState(CannonBallEntity entity, CannonBallRenderState state, float f) {
        super.extractRenderState(entity, state, f);
    }

    @Override
    public void submit(CannonBallRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        poseStack.scale(0.75F, 0.75F, 0.75F);
        poseStack.translate(0.0D, -1.0D, 0.0D);
        nodeCollector.submitModel(this.model, state, poseStack, getTextureLocation(null), 15728880, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF, null);
        poseStack.popPose();
        super.submit(state, poseStack, nodeCollector, cameraRenderState);
    }

    public @NotNull Identifier getTextureLocation(CannonBallEntity entity) {
        return Identifier.fromNamespaceAndPath(SmallShipsMod.MOD_ID, "textures/entity/cannon/cannon_ball.png");
    }
}
