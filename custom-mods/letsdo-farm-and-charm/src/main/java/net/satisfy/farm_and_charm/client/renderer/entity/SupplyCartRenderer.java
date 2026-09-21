package net.satisfy.farm_and_charm.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.satisfy.farm_and_charm.FarmAndCharm;
import net.satisfy.farm_and_charm.client.model.SupplyCartModel;
import net.satisfy.farm_and_charm.core.entity.SupplyCartEntity;

// 26.2: all motion/wobble math runs in extractRenderState (same formulas);
// submit only applies transforms and submits the model.
public class SupplyCartRenderer extends EntityRenderer<SupplyCartEntity, SupplyCartRenderer.State> {
    public static final Identifier CART_TEXTURE = FarmAndCharm.identifier("textures/entity/supply_cart.png");
    private final SupplyCartModel model;

    public static class State extends EntityRenderState {
        public float yaw;
        public float basePitch;
        public float roll;
        public float pitch;
        public float wheelRotation;
        public float wobbleYaw;
    }

    public SupplyCartRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new SupplyCartModel(context.bakeLayer(SupplyCartModel.LAYER_LOCATION));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(SupplyCartEntity cart, State state, float partialTick) {
        super.extractRenderState(cart, state, partialTick);
        float yaw = cart.getYRot();
        state.yaw = yaw;

        boolean hasPassenger = !cart.getPassengers().isEmpty();
        boolean isPulled = cart.getPulling() != null;
        boolean pulledByPlayer = cart.getPulling() instanceof Player;
        boolean pulledByHorse = isPulled && !pulledByPlayer;

        float basePitch;
        if (!pulledByHorse) {
            float base;
            if (isPulled) {
                base = 3F;
            } else if (hasPassenger) {
                base = 11F;
            } else {
                base = -11F;
            }
            basePitch = base;
        } else {
            basePitch = 0F;
        }
        state.basePitch = basePitch;

        double deltaX = cart.getX() - cart.xOld;
        double deltaZ = cart.getZ() - cart.zOld;
        boolean isMoving = isPulled && (deltaX * deltaX + deltaZ * deltaZ) > 1.0E-4D;

        float roll = 0F;
        float pitch = 0F;
        if (isMoving) {
            float speed = Mth.sqrt((float) (deltaX * deltaX + deltaZ * deltaZ));
            float wobbleStrength = Mth.clamp(speed * 16.0F, 0.0F, 1.0F);

            float time = cart.tickCount + partialTick;

            roll = Mth.sin(time * 0.9F) * 0.9F * wobbleStrength;
            pitch = Mth.sin(time * 1.4F + 1.7F) * 1.1F * wobbleStrength;
        }
        state.roll = roll;
        state.pitch = pitch;

        float clampedPartial = Mth.clamp(partialTick, 0.0F, 1.0F);
        state.wheelRotation = cart.getWheelRotation(clampedPartial);

        float wobbleYaw = 0.0F;
        int hurtTime = cart.getHurtTime();
        if (hurtTime > 0) {
            float damage = Mth.clamp(cart.getDamage() / 40.0F, 0.0F, 1.0F);
            int hurtDir = cart.getHurtDir();
            float time = hurtTime - clampedPartial;
            wobbleYaw = Mth.sin(time * 2.2F) * time * 0.004F * hurtDir * damage;
        }
        state.wobbleYaw = wobbleYaw;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        poseStack.pushPose();

        poseStack.translate(0.0D, 1.4D, 0.0D);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(state.basePitch));
        poseStack.mulPose(Axis.ZP.rotationDegrees(state.roll));
        poseStack.mulPose(Axis.XP.rotationDegrees(state.pitch));

        model.setupAnim(state);
        collector.submitModel(model, state, poseStack, RenderTypes.entityCutout(CART_TEXTURE),
                state.lightCoords, OverlayTexture.NO_OVERLAY, EntityRenderState.NO_OUTLINE, null);

        poseStack.popPose();
    }
}
