package net.medievalweapons.mixin;

import com.mojang.authlib.GameProfile;
import net.medievalweapons.access.PlayerAccess;
import net.medievalweapons.item.BigAxeItem;
import net.medievalweapons.item.DaggerItem;
import net.medievalweapons.item.FranciscaItem;
import net.medievalweapons.item.HealingStaffItem;
import net.medievalweapons.item.HeroStaffItem;
import net.medievalweapons.item.HeroSwordItem;
import net.medievalweapons.item.JavelinItem;
import net.medievalweapons.item.LanceItem;
import net.medievalweapons.item.LongSwordItem;
import net.medievalweapons.item.MaceItem;
import net.medievalweapons.item.NinjatoItem;
import net.medievalweapons.item.RapierItem;
import net.medievalweapons.item.SickleItem;
import net.medievalweapons.item.SmallAxeItem;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Player.class})
public abstract class PlayerEntityMixin extends LivingEntity implements PlayerAccess {
   @Unique
   private int lastAttackedOffhandTicks;

   public PlayerEntityMixin(Level world, GameProfile profile) {
      super(EntityTypes.PLAYER, world);
   }

   @Inject(
      method = {"Lnet/minecraft/world/entity/player/Player;tick()V"},
      at = {@At(
         value = "FIELD",
         target = "Lnet/minecraft/world/entity/player/Player;attackStrengthTicker:I",
         ordinal = 0
      )}
   )
   private void tickMixin(CallbackInfo info) {
      this.lastAttackedOffhandTicks++;
   }

   private void offhandAttack(Entity target) {
      if (target.isAttackable()) {
         if (!target.skipAttackInteraction(this)) {
            target.invulnerableTime = 0;
            ItemStack itemStack = this.getOffhandItem();
            DamageSource damageSource = this.createAttackSource(itemStack);
            float f = (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
            float h = this.getAttackCooldownProgressOffhand(0.5F);
            float g = h * (this.getEnchantedDamage(target, f, damageSource) - f);
            f *= this.baseDamageScaleFactor();
            this.resetLastAttackedOffhandTicks();
            if (f > 0.0F || g > 0.0F) {
               boolean strong = h > 0.9F;
               boolean sprintKnock = false;
               if (this.isSprinting() && strong) {
                  this.playServerSideSound(SoundEvents.PLAYER_ATTACK_KNOCKBACK);
                  sprintKnock = true;
               }

               f += itemStack.getItem().getAttackDamageBonus(target, f, damageSource);
               boolean crit = strong && this.canCriticalAttack(target);
               if (crit) {
                  f *= 1.5F;
               }

               float total = f + g;
               boolean sweep = strong
                  && !crit
                  && !sprintKnock
                  && this.onGround()
                  && this.getKnownMovement().horizontalDistanceSqr() < Mth.square((double)this.getSpeed() * 2.5)
                  && (itemStack.is(ItemTags.SWORDS) || isWeaponItem(itemStack));
               float healthBefore = 0.0F;
               if (target instanceof LivingEntity livingEntity) {
                  healthBefore = livingEntity.getHealth();
               }

               Vec3 motion = target.getDeltaMovement();
               boolean hurt = target.hurtOrSimulate(damageSource, total);
               if (hurt) {
                  this.causeExtraKnockback(
                     target, this.getKnockback(target, damageSource) + (sprintKnock ? 0.5F : 0.0F), motion, damageSource, total, true
                  );
                  if (sweep) {
                     this.doSweepAttack(target, f, damageSource, h);
                  }

                  this.attackVisualEffects(target, crit, sweep, strong, false, g);
                  this.setLastHurtMob(target);
                  this.itemAttackInteraction(target, itemStack, damageSource, true);
                  this.damageStatsAndHearts(target, healthBefore);
                  ((Player)(Object)this).causeFoodExhaustion(0.1F);
                  target.invulnerableTime = 0;
               } else {
                  this.playServerSideSound(SoundEvents.PLAYER_ATTACK_NODAMAGE);
               }

               this.postPiercingAttack();
            }
         }
      }
   }

   @Unique
   private static boolean isWeaponItem(ItemStack stack) {
      return stack.getItem() instanceof DaggerItem
         || stack.getItem() instanceof SmallAxeItem
         || stack.getItem() instanceof BigAxeItem
         || stack.getItem() instanceof LongSwordItem
         || stack.getItem() instanceof LanceItem
         || stack.getItem() instanceof HealingStaffItem
         || stack.getItem() instanceof MaceItem
         || stack.getItem() instanceof NinjatoItem
         || stack.getItem() instanceof SickleItem
         || stack.getItem() instanceof RapierItem
         || stack.getItem() instanceof HeroSwordItem
         || stack.getItem() instanceof HeroStaffItem
         || stack.getItem() instanceof FranciscaItem
         || stack.getItem() instanceof JavelinItem;
   }

   @Shadow
   public float getCurrentItemAttackStrengthDelay() {
      return 0.0F;
   }

   @Override
   public void doOffhandAttack(Entity entity) {
      this.offhandAttack(entity);
   }

   @Override
   public float getAttackCooldownProgressOffhand(float baseTime) {
      return Mth.clamp((this.lastAttackedOffhandTicks + baseTime) / this.getCurrentItemAttackStrengthDelay(), 0.0F, 1.0F);
   }

   @Override
   public void resetLastAttackedOffhandTicks() {
      this.lastAttackedOffhandTicks = 0;
   }

   @Shadow
   protected float getEnchantedDamage(Entity target, float baseDamage, DamageSource damageSource) {
      return baseDamage;
   }

   @Shadow
   private DamageSource createAttackSource(ItemStack itemStack) {
      throw new AssertionError();
   }

   @Shadow
   private void playServerSideSound(SoundEvent soundEvent) {
      throw new AssertionError();
   }

   @Shadow
   private boolean canCriticalAttack(Entity target) {
      throw new AssertionError();
   }

   @Shadow
   private void doSweepAttack(Entity target, float damage, DamageSource damageSource, float attackStrength) {
      throw new AssertionError();
   }

   @Shadow
   private void attackVisualEffects(Entity target, boolean crit, boolean sweep, boolean strong, boolean bl, float bonus) {
      throw new AssertionError();
   }

   @Shadow
   private void itemAttackInteraction(Entity target, ItemStack itemStack, DamageSource damageSource, boolean bl) {
      throw new AssertionError();
   }

   @Shadow
   private void damageStatsAndHearts(Entity target, float healthBefore) {
      throw new AssertionError();
   }

   @Shadow
   private float baseDamageScaleFactor() {
      throw new AssertionError();
   }
}
