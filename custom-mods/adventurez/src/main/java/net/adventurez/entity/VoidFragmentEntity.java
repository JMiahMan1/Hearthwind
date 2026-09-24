package net.adventurez.entity;

import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.server.level.ServerLevel;

import net.minecraft.world.level.storage.ValueInput;

import net.minecraft.world.level.storage.ValueOutput;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.EntitySelector;
import java.util.List;

import net.minecraft.tags.DamageTypeTags;
import org.jetbrains.annotations.Nullable;

import net.adventurez.entity.nonliving.ThrownRockEntity;
import net.adventurez.entity.nonliving.VoidBulletEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;

public class VoidFragmentEntity extends Monster {

    public static final EntityDataAccessor<Boolean> IS_VOID_ORB = SynchedEntityData.defineId(VoidFragmentEntity.class, EntityDataSerializers.BOOLEAN);
    public boolean isVoidOrb;
    private int deathTick;

    public VoidFragmentEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createVoidFragmentAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 10.0D).add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 10.0D);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnReason, @Nullable SpawnGroupData spawnGroupData) {
        if (spawnReason == EntitySpawnReason.SPAWN_ITEM_USE) {
            this.setVoidOrb(true);
        }

        return super.finalizeSpawn(level, difficulty, spawnReason, spawnGroupData);
    }

    @Override
    protected void defineSynchedData(Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_VOID_ORB, false);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("IsVoidOrb", this.isVoidOrb);

    }

    @Override
    public void readAdditionalSaveData(ValueInput tag) {
        super.readAdditionalSaveData(tag);
        this.isVoidOrb = tag.getBooleanOr("IsVoidOrb", false);
        this.setVoidOrb(this.isVoidOrb);
    }

    public void setVoidOrb(boolean orb) {
        this.isVoidOrb = orb;
        entityData.set(IS_VOID_ORB, orb);
        this.reapplyPosition();
        this.refreshDimensions();
    }

    @Override
    protected EntityDimensions getDefaultDimensions(Pose pose) {
        return super.getDefaultDimensions(pose).scale(1.0F, (this.isVoidOrb || this.entityData.get(IS_VOID_ORB)) ? 2.1F : 1.2F);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> data) {
        if (IS_VOID_ORB.equals(data)) {
            this.reapplyPosition();
            this.refreshDimensions();
            this.setYRot(this.yHeadRot);
            this.yBodyRot = this.yHeadRot;
        }

        super.onSyncedDataUpdated(data);
    }

    @Override
    public void tickDeath() {
        if (this.level().isClientSide()) {
            for (int i = 0; i < 10; ++i) {
                double d = this.random.nextGaussian() * 0.05D;
                double e = this.random.nextGaussian() * 0.05D;
                double f = this.random.nextGaussian() * 0.05D;
                this.level().addParticle(ParticleTypes.END_ROD, this.getRandomX(1.0D), this.getRandomY(), this.getRandomZ(1.0D), d, e, f);
            }
        }
        ++this.deathTick;
        if (this.deathTick >= 20 && !this.level().isClientSide()) {
            this.remove(Entity.RemovalReason.KILLED);
        }
    }

    @Override
    public void die(DamageSource source) {
        if (!this.level().isClientSide() && this.isVoidOrb) {
            AABB box = new AABB(this.blockPosition());
            List<VoidShadowEntity> list = this.level().getEntitiesOfClass(VoidShadowEntity.class, box.inflate(120D), EntitySelector.NO_SPECTATORS);
            for (int i = 0; i < list.size(); ++i) {
                list.get(i).hurt(this.damageSources().magic(), 40f);
            }
        }
        super.die(source);
    }

    public void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (!this.level().isClientSide() && !this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(0.8D), EntitySelector.NO_SPECTATORS).isEmpty()) {
            this.dead = true;
            this.level().explode(this, this.getX(), this.getY(), this.getZ(), this.isVoidOrb ? 4.0F : 3.0F, Level.ExplosionInteraction.NONE);
            this.discard();
        }
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource source, float amount) {
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return super.hurtServer(serverLevel, source, amount);
        } else if ((source.getEntity() != null && source.getEntity() instanceof Player && ((Player) source.getEntity()).isCreative())) {
            return super.hurtServer(serverLevel, source, amount);
        }
        if (this.isInvulnerableTo(serverLevel, source) || source.getEntity() instanceof ThrownRockEntity || (this.isVoidOrb && !(source.getEntity() instanceof VoidBulletEntity))) {
            return false;
        } else {
            return super.hurtServer(serverLevel, source, source.getEntity() instanceof VoidBulletEntity ? this.getHealth() : amount);
        }
    }

    @Override
    public void checkDespawn() {
        if (this.level().getDifficulty() == Difficulty.PEACEFUL) {
            this.discard();
        }
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    public boolean addStatusEffect(MobEffectInstance effect, Entity entity) {
        return false;
    }

    public boolean canStartRiding(Entity entity) {
        return false;
    }

    @Override
    public boolean canUsePortal(boolean allowVehicles) {
        return false;
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

}
