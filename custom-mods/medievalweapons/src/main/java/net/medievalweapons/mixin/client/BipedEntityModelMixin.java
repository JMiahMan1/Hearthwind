package net.medievalweapons.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.medievalweapons.init.CompatInit;
import net.medievalweapons.init.TagInit;
import net.medievalweapons.item.BigAxeItem;
import net.medievalweapons.item.LongSwordItem;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({HumanoidModel.class})
public abstract class BipedEntityModelMixin extends EntityModel<HumanoidRenderState>implements ArmedModel<HumanoidRenderState>, HeadedModel {
   @Shadow
   @Mutable
   @Final
   public ModelPart rightArm;
   @Shadow
   @Mutable
   @Final
   public ModelPart leftArm;
   @Shadow
   @Mutable
   @Final
   public ModelPart head;

   protected BipedEntityModelMixin(ModelPart root) {
      super(root);
   }

   @Inject(
      method = {"setupAnim"},
      at = {@At("TAIL")}
   )
   private void setAnglesMixin(HumanoidRenderState state, CallbackInfo info) {
      float f = state.walkAnimationPos;
      float g = state.walkAnimationSpeed;
      ItemStack mainHand = state.getMainHandItemStack();
      ItemStack offHand = state.mainArm == HumanoidArm.RIGHT ? state.leftHandItemStack : state.rightHandItemStack;
      boolean blocking = state.rightArmPose == HumanoidModel.ArmPose.BLOCK || state.leftArmPose == HumanoidModel.ArmPose.BLOCK;
      if (offHand.isEmpty() && !state.isVisuallySwimming && !state.isPassenger && mainHand != null) {
         if (!mainHand.is(TagInit.DOUBLE_HANDED_ITEMS) && !(mainHand.getItem() instanceof LongSwordItem)) {
            if (mainHand.is(TagInit.ACROSS_DOUBLE_HANDED_ITEMS) || mainHand.getItem() instanceof BigAxeItem) {
               if (!CompatInit.isBetterCombatLoaded) {
                  this.rightArm.xRot = -0.5236F + Mth.cos(f * 0.6662F) * 2.0F * g * 0.5F / 15.0F;
                  this.rightArm.yRot = 0.0F;
                  this.rightArm.zRot = 0.0F;
                  this.leftArm.xRot = -1.2217F + Mth.cos(f * 0.6662F) * 2.0F * g * 0.5F / 15.0F;
                  this.leftArm.yRot = 0.0F;
                  this.leftArm.zRot = 0.0F;
                  if (state.attackTime > 0.0F) {
                     float gx = 1.0F - state.attackTime;
                     float hx = Mth.sin(gx * (float) Math.PI);
                     float kx = this.head.xRot;
                     if (kx < 0.0F) {
                        kx = 0.25F;
                     }

                     float ix = Mth.sin(state.attackTime * (float) Math.PI) * -(kx - 0.7F) * 0.75F * 0.6F;
                     this.rightArm.xRot = (float)(this.rightArm.xRot - (hx * 1.2 + ix));
                     this.leftArm.xRot = (float)(this.leftArm.xRot - (hx * 1.2 + ix) * 1.2) * 0.75F * 1.2F;
                  }
               }

               if (blocking) {
                  if (!CompatInit.isBetterCombatLoaded) {
                     this.rightArm.xRot = -1.0234747F;
                     this.leftArm.xRot = -1.6393949F;
                  } else {
                     this.rightArm.xRot = -1.0234747F;
                     this.leftArm.xRot = -1.3393949F;
                     this.rightArm.yRot = -0.6F;
                     this.leftArm.yRot = 0.15F;
                     this.rightArm.zRot = 1.0F;
                  }
               }
            }
         } else {
            if (!CompatInit.isBetterCombatLoaded) {
               this.rightArm.xRot = -0.8727F + Mth.cos(f * 0.6662F) * 2.0F * g * 0.5F / 15.0F;
               this.rightArm.yRot = -0.5672F;
               this.rightArm.zRot = 0.0F;
               this.leftArm.xRot = -1.0472F + Mth.cos(f * 0.6662F) * 2.0F * g * 0.5F / 15.0F;
               this.leftArm.yRot = 0.829F;
               this.leftArm.zRot = -0.0436F;
               if (state.attackTime > 0.0F) {
                  float gx = 1.0F - state.attackTime;
                  float hx = Mth.sin(gx * (float) Math.PI);
                  float kx = this.head.xRot;
                  if (kx < 0.0F) {
                     kx = 0.25F;
                  }

                  float ix = Mth.sin(state.attackTime * (float) Math.PI) * -(kx - 0.7F) * 0.75F * 0.6F;
                  this.rightArm.xRot = (float)(this.rightArm.xRot - (hx * 1.2 + ix));
                  this.leftArm.xRot = (float)(this.leftArm.xRot - (hx * 1.2 + ix) * 1.2) * 0.75F;
               }
            }

            if (blocking) {
               if (!CompatInit.isBetterCombatLoaded) {
                  this.rightArm.xRot = -1.25F;
                  this.leftArm.xRot = -1.17F;
                  this.rightArm.zRot = 0.7F;
               } else {
                  this.rightArm.xRot = -1.25F;
                  this.rightArm.yRot = -0.8F;
                  this.leftArm.xRot = -1.25F;
                  this.leftArm.yRot = 0.8F;
               }
            }
         }
      }
   }
}
