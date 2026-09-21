package net.satisfy.farm_and_charm.fabric.core.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.satisfy.farm_and_charm.client.renderer.entity.SaturationOverlayRenderer;
import net.satisfy.farm_and_charm.core.registry.ObjectRegistry;
import net.satisfy.farm_and_charm.core.util.SaturationTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// 26.2: LivingEntityRenderer renders via extractRenderState/submit. The
// entity is captured during extraction so the overlay can run after submit
// with the same conditions as before (farm animal, targeted crosshair,
// dungarees worn).
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {

    @Unique
    private LivingEntity farmAndCharm$capturedEntity;

    @Shadow
    protected EntityModel<?> model;

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("HEAD"))
    private void farmAndCharm$captureEntity(LivingEntity entity, net.minecraft.client.renderer.entity.state.LivingEntityRenderState state, float partialTick, CallbackInfo ci) {
        farmAndCharm$capturedEntity = entity;
    }

    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/EntityModel;setupAnim(Ljava/lang/Object;)V", shift = At.Shift.AFTER))
    private void farmAndCharm$lockArmsWhilePulling(net.minecraft.client.renderer.entity.state.LivingEntityRenderState state, PoseStack poseStack,
            SubmitNodeCollector collector, CameraRenderState cameraState, CallbackInfo ci) {
        LivingEntity entity = farmAndCharm$capturedEntity;
        if (entity == null || !(this.model instanceof HumanoidModel<?> humanoid)) {
            return;
        }
        if (entity.level() == null || !entity.level().isClientSide()) {
            return;
        }
        boolean isPulling = false;
        for (net.satisfy.farm_and_charm.core.entity.AbstractCartEntity cart : entity.level().getEntitiesOfClass(
                net.satisfy.farm_and_charm.core.entity.AbstractCartEntity.class,
                entity.getBoundingBox().inflate(64.0D))) {
            if (cart.getPulling() == entity) {
                isPulling = true;
                break;
            }
        }
        if (!isPulling) {
            return;
        }
        humanoid.leftArm.xRot = 0.0F;
        humanoid.leftArm.yRot = 0.0F;
        humanoid.leftArm.zRot = 0.0F;
        humanoid.rightArm.xRot = 0.0F;
        humanoid.rightArm.yRot = 0.0F;
        humanoid.rightArm.zRot = 0.0F;
    }

    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At("TAIL"))
    private void farmAndCharm$renderSaturationOverlay(net.minecraft.client.renderer.entity.state.LivingEntityRenderState state, PoseStack poseStack,
            SubmitNodeCollector collector, CameraRenderState cameraState, CallbackInfo ci) {
        LivingEntity entity = farmAndCharm$capturedEntity;
        farmAndCharm$capturedEntity = null;
        if (!(entity instanceof Animal animal)) return;

        EntityType<?> type = animal.getType();
        if (!(type == EntityTypes.COW || type == EntityTypes.PIG || type == EntityTypes.SHEEP || type == EntityTypes.CHICKEN)) return;

        if (!(animal instanceof SaturationTracker.SaturatedAnimal saturated)) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.crosshairPickEntity != animal || mc.player == null) return;

        if (mc.player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.LEGS).getItem() == ObjectRegistry.DUNGAREES.get()) {
                SaturationTracker tracker = saturated.farm_and_charm$getSaturationTracker();
                poseStack.pushPose();
                poseStack.translate(state.x, state.y, state.z);
                SaturationOverlayRenderer.submit(poseStack, collector, animal, tracker.level(), tracker.foodCounter());
                poseStack.popPose();
        }
    }
}
