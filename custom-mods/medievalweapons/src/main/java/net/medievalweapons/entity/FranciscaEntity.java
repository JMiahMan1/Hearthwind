package net.medievalweapons.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.medievalweapons.init.EntityInit;
import net.medievalweapons.item.FranciscaItem;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow.Pickup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.EntityHitResult;

public class FranciscaEntity extends AbstractArrow implements ItemSupplier {
   private static final net.minecraft.network.syncher.EntityDataAccessor<Boolean> ENCHANTMENT_GLINT = SynchedEntityData.defineId(
      FranciscaEntity.class, EntityDataSerializers.BOOLEAN
   );
   private ItemStack francisca;

   public FranciscaEntity(
      net.minecraft.world.entity.EntityType<? extends FranciscaEntity> entityType, net.minecraft.world.level.Level world, FranciscaItem item
   ) {
      super(entityType, world);
      this.francisca = new ItemStack(item);
   }

   public FranciscaEntity(Level world, LivingEntity owner, ItemStack stack) {
      super(((FranciscaItem)stack.getItem()).getType(), owner, world, stack, null);
      this.francisca = stack;
      this.entityData.set(ENCHANTMENT_GLINT, stack.hasFoil());
   }

   public FranciscaEntity(Level world, double x, double y, double z, ItemStack stack) {
      super(((FranciscaItem)stack.getItem()).getType(), z, z, z, world, stack, stack);
      this.francisca = stack;
      this.entityData.set(ENCHANTMENT_GLINT, stack.hasFoil());
   }

   protected void defineSynchedData(Builder builder) {
      super.defineSynchedData(builder);
      builder.define(ENCHANTMENT_GLINT, false);
   }

   protected ItemStack getPickupItem() {
      return this.francisca.copy();
   }

   @Environment(EnvType.CLIENT)
   public boolean enchantingGlint() {
      return (Boolean)this.entityData.get(ENCHANTMENT_GLINT);
   }

   protected void onHitEntity(EntityHitResult entityHitResult) {
      Entity hitEntity = entityHitResult.getEntity();
      float damage = ((FranciscaItem)this.francisca.getItem()).getToolMaterial().attackDamageBonus() * 2.3F;
      Entity owner = this.getOwner();
      if (this.level() instanceof ServerLevel serverWorld) {
         damage = EnchantmentHelper.modifyDamage(
            serverWorld, this.getWeaponItem(), hitEntity, this.createDamageSource(this, (Entity)(owner == null ? this : owner)), damage
         );
      }

      DamageSource damageSource = this.createDamageSource(this, (Entity)(owner == null ? this : owner));
      if (hitEntity.hurtOrSimulate(damageSource, damage)) {
         if (hitEntity.getType() == EntityTypes.ENDERMAN) {
            return;
         }

         if (this.level() instanceof ServerLevel serverWorld) {
            EnchantmentHelper.doPostAttackEffectsWithItemSource(serverWorld, hitEntity, damageSource, this.getWeaponItem());
         }

         if (hitEntity instanceof LivingEntity livingEntity) {
            this.doKnockback(livingEntity, damageSource);
            this.doPostHurtEffects(livingEntity);
         }
      }

      this.setDeltaMovement(this.getDeltaMovement().scale(0.75));
      this.playSound(SoundEvents.TRIDENT_HIT, 1.0F, 1.0F);
   }

   public void playerTouch(Player player) {
      Entity entity = this.getOwner();
      if (entity == null || entity.getUUID() == player.getUUID()) {
         super.playerTouch(player);
      }
   }

   public void readAdditionalSaveData(ValueInput input) {
      super.readAdditionalSaveData(input);
      this.francisca = input.read("francisca", ItemStack.CODEC).orElse(this.getDefaultPickupItem());
      this.entityData.set(ENCHANTMENT_GLINT, this.francisca.hasFoil());
   }

   public void addAdditionalSaveData(ValueOutput output) {
      super.addAdditionalSaveData(output);
      if (!this.francisca.isEmpty()) {
         output.store("francisca", ItemStack.CODEC, this.francisca);
      }
   }

   public void tickDespawn() {
      if (this.pickup != Pickup.ALLOWED) {
         super.tickDespawn();
      }
   }

   public ItemStack getWeaponItem() {
      return this.francisca.copy();
   }

   @Environment(EnvType.CLIENT)
   public boolean shouldRender(double cameraX, double cameraY, double cameraZ) {
      return true;
   }

   public ItemStack getItem() {
      return this.francisca;
   }

   public boolean inGround() {
      return this.isInGround();
   }

   private DamageSource createDamageSource(Entity source, Entity attacker) {
      return attacker.damageSources().source(EntityInit.FRANCISCA, source, attacker);
   }

   protected ItemStack getDefaultPickupItem() {
      return this.francisca;
   }
}
