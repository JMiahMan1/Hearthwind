package net.jmb19905.niftycarts.entity;

import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DummyLivingEntity extends LivingEntity {
   protected DummyLivingEntity(
      net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.LivingEntity> type, net.minecraft.world.level.Level world
   ) {
      super(type, world);
      this.setSilent(true);
      this.setNoGravity(true);
      this.setInvulnerable(true);
      this.setInvisible(true);
   }

   protected void defineSynchedData(@NotNull Builder builder) {
      super.defineSynchedData(builder);
   }

   @NotNull
   public ItemStack getItemBySlot(@NotNull EquipmentSlot slotIn) {
      return ItemStack.EMPTY;
   }

   public void setItemSlot(@NotNull EquipmentSlot slotIn, @NotNull ItemStack stack) {
   }

   @NotNull
   public HumanoidArm getMainArm() {
      return HumanoidArm.RIGHT;
   }

   public boolean ignoreExplosion(@NotNull Explosion explosion) {
      return true;
   }

   @NotNull
   public PushReaction getPistonPushReaction() {
      return PushReaction.IGNORE;
   }

   public boolean isEffectiveAi() {
      return false;
   }

   public boolean isPushable() {
      return false;
   }

   public void rideTick() {
      super.rideTick();
   }

   public boolean isAttackable() {
      return false;
   }

   public boolean isIgnoringBlockTriggers() {
      return true;
   }

   public boolean isPickable() {
      return false;
   }

   public boolean attackable() {
      return false;
   }

   public boolean canAttackType(@NotNull net.minecraft.world.entity.EntityType<?> type) {
      return false;
   }

   public boolean canAttack(@NotNull LivingEntity living) {
      return false;
   }

   public boolean isAffectedByPotions() {
      return false;
   }

   public void kill(@NotNull ServerLevel serverLevel) {
      this.discard();
   }

   public void push(@NotNull Entity p_21294_) {
   }

   protected void pushEntities() {
   }

   protected void doWaterSplashEffect() {
   }

   public boolean addEffect(@NotNull MobEffectInstance effect, @Nullable Entity entity) {
      return false;
   }

   protected void updateEffectVisibility() {
      super.updateEffectVisibility();
   }

   protected void updateInvisibilityStatus() {
      this.setInvisible(true);
   }
}
