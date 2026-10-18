package net.medievalweapons.entity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.medievalweapons.init.EntityInit;
import net.medievalweapons.item.JavelinItem;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
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
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class JavelinEntity extends AbstractArrow implements ItemSupplier {
   private static final net.minecraft.network.syncher.EntityDataAccessor<Byte> LOYALTY = SynchedEntityData.defineId(
      JavelinEntity.class, EntityDataSerializers.BYTE
   );
   private static final net.minecraft.network.syncher.EntityDataAccessor<Boolean> ENCHANTMENT_GLINT = SynchedEntityData.defineId(
      JavelinEntity.class, EntityDataSerializers.BOOLEAN
   );
   private ItemStack javelin;
   private final Set<UUID> piercedEntities = new HashSet<>();
   public int returnTimer;
   private boolean dealtDamage;

   public JavelinEntity(
      net.minecraft.world.entity.EntityType<? extends JavelinEntity> entityType, net.minecraft.world.level.Level world, JavelinItem item
   ) {
      super(entityType, world);
      this.javelin = new ItemStack(item);
   }

   public JavelinEntity(Level world, LivingEntity owner, ItemStack stack) {
      super(((JavelinItem)stack.getItem()).getType(), owner, world, stack, null);
      this.javelin = stack;
      this.entityData.set(LOYALTY, this.getLoyalty(stack));
      this.entityData.set(ENCHANTMENT_GLINT, stack.hasFoil());
   }

   public JavelinEntity(Level world, double x, double y, double z, ItemStack stack) {
      super(((JavelinItem)stack.getItem()).getType(), z, z, z, world, stack, stack);
      this.javelin = stack;
      this.entityData.set(LOYALTY, this.getLoyalty(stack));
      this.entityData.set(ENCHANTMENT_GLINT, stack.hasFoil());
   }

   protected void defineSynchedData(Builder builder) {
      super.defineSynchedData(builder);
      builder.define(LOYALTY, (byte)0);
      builder.define(ENCHANTMENT_GLINT, false);
   }

   protected ItemStack getPickupItem() {
      return this.javelin.copy();
   }

   @Environment(EnvType.CLIENT)
   public boolean enchantingGlint() {
      return (Boolean)this.entityData.get(ENCHANTMENT_GLINT);
   }

   protected void onHitEntity(EntityHitResult entityHitResult) {
      Entity hitEntity = entityHitResult.getEntity();
      if (!this.piercedEntities.contains(hitEntity.getUUID()) && this.piercedEntities.size() <= this.getPierceLevel()) {
         this.piercedEntities.add(hitEntity.getUUID());
         float damage = ((JavelinItem)this.javelin.getItem()).getToolMaterial().attackDamageBonus() * 2.35F;
         this.dealtDamage = true;
         Entity owner = this.getOwner();
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

         if (this.piercedEntities.size() > this.getPierceLevel()) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(-0.01, -0.1, -0.01));
         } else {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.75));
         }

         this.playSound(SoundEvents.TRIDENT_HIT, 1.0F, 1.0F);
      }
   }

   @Nullable
   protected EntityHitResult findHitEntity(Vec3 currentPosition, Vec3 nextPosition) {
      return this.dealtDamage ? null : super.findHitEntity(currentPosition, nextPosition);
   }

   public void tick() {
      if (this.inGroundTime > 4) {
         this.dealtDamage = true;
      }

      Entity entity = this.getOwner();
      byte i = (Byte)this.entityData.get(LOYALTY);
      if (i > 0 && (this.dealtDamage || this.isNoPhysics()) && entity != null) {
         if (!this.isOwnerAlive()) {
            if (!this.level().isClientSide() && this.pickup == Pickup.ALLOWED) {
               this.spawnAtLocation((ServerLevel)this.level(), this.getPickupItem(), 0.1F);
            }

            this.discard();
         } else {
            this.setNoPhysics(true);
            Vec3 vec3d = entity.getEyePosition().subtract(this.position());
            this.setPosRaw(this.getX(), this.getY() + vec3d.y * 0.015 * i, this.getZ());
            if (this.level().isClientSide()) {
               this.yOld = this.getY();
            }

            double d = 0.05 * i;
            this.setDeltaMovement(this.getDeltaMovement().scale(0.95).add(vec3d.normalize().scale(d)));
            if (this.returnTimer == 0) {
               this.playSound(SoundEvents.TRIDENT_RETURN, 10.0F, 1.0F);
            }

            this.returnTimer++;
         }
      }

      super.tick();
   }

   private boolean isOwnerAlive() {
      Entity entity = this.getOwner();
      return entity != null && entity.isAlive() ? !(entity instanceof ServerPlayer) || !entity.isSpectator() : false;
   }

   public void playerTouch(Player player) {
      Entity entity = this.getOwner();
      if (entity == null || entity.getUUID() == player.getUUID()) {
         super.playerTouch(player);
      }
   }

   public void readAdditionalSaveData(ValueInput input) {
      super.readAdditionalSaveData(input);
      this.javelin = input.read("javelin", ItemStack.CODEC).orElse(this.getDefaultPickupItem());
      this.entityData.set(ENCHANTMENT_GLINT, this.javelin.hasFoil());
      this.piercedEntities.clear();
      input.listOrEmpty("javelin_hit", UUIDUtil.CODEC).forEach(uuid -> this.piercedEntities.add(uuid));
      this.dealtDamage = input.getBooleanOr("DealtDamage", false);
      this.entityData.set(LOYALTY, this.getLoyalty(this.getPickupItemStackOrigin()));
   }

   private byte getLoyalty(ItemStack stack) {
      return this.level() instanceof ServerLevel serverWorld
         ? (byte)Mth.clamp(EnchantmentHelper.getTridentReturnToOwnerAcceleration(serverWorld, stack, this), 0, 127)
         : 0;
   }

   public void addAdditionalSaveData(ValueOutput output) {
      super.addAdditionalSaveData(output);
      if (!this.javelin.isEmpty()) {
         output.store("javelin", ItemStack.CODEC, this.javelin);
         ValueOutput.TypedOutputList<UUID> tags = output.list("javelin_hit", UUIDUtil.CODEC);

         for (UUID uuid : this.piercedEntities) {
            tags.add(uuid);
         }

         output.putBoolean("DealtDamage", this.dealtDamage);
      }
   }

   public void tickDespawn() {
      int i = (Byte)this.entityData.get(LOYALTY);
      if (this.pickup != Pickup.ALLOWED || i <= 0) {
         super.tickDespawn();
      }
   }

   public ItemStack getWeaponItem() {
      return this.javelin.copy();
   }

   @Environment(EnvType.CLIENT)
   public boolean shouldRender(double cameraX, double cameraY, double cameraZ) {
      return true;
   }

   public ItemStack getItem() {
      return this.javelin;
   }

   private DamageSource createDamageSource(Entity source, Entity attacker) {
      return attacker.damageSources().source(EntityInit.JAVELIN, source, attacker);
   }

   protected ItemStack getDefaultPickupItem() {
      return this.javelin;
   }
}
