package net.adventurez.entity;

import net.minecraft.world.entity.ai.goal.target.TargetGoal;

import net.minecraft.sounds.SoundSource;
import java.util.EnumSet;

import net.adventurez.entity.nonliving.ThrownRockEntity;
import net.adventurez.entity.nonliving.VoidBulletEntity;
import net.adventurez.init.SoundInit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.Level;

public class VoidShadeEntity extends Monster {

    public VoidShadeEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new VoidShadeEntity.VoidShadeMoveControl(this);
        this.xpReward = 0;
    }

    public static AttributeSupplier.Builder createVoidShadeAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 20.0D).add(Attributes.MOVEMENT_SPEED, 0.15D)
                .add(Attributes.ATTACK_DAMAGE, 7.0D).add(Attributes.ATTACK_KNOCKBACK, 0.5D).add(Attributes.KNOCKBACK_RESISTANCE, 10.0D)
                .add(Attributes.FOLLOW_RANGE, 80.0D);
    }

    @Override
    public void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(3, new VoidShadeEntity.LookAtTargetGoal(this));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(3, new VoidShadeEntity.MoveGoal(this));
        this.goalSelector.addGoal(5, new VoidShadeEntity.ShootBulletGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource source, float amount) {
        if (this.isInvulnerableTo(serverLevel, source) || source.getEntity() instanceof ThrownRockEntity) {
            return false;
        } else {
            return super.hurtServer(serverLevel, source, amount);
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

    static class ShootBulletGoal extends Goal {
        private final VoidShadeEntity voidShadeEntity;
        public int cooldown;

        public ShootBulletGoal(VoidShadeEntity voidShadeEntity) {
            this.voidShadeEntity = voidShadeEntity;
        }

        @Override
        public boolean canUse() {
            LivingEntity livingEntity = this.voidShadeEntity.getTarget();
            return livingEntity != null && this.voidShadeEntity.hasLineOfSight(livingEntity);
        }

        @Override
        public void start() {
            this.cooldown = 0;
        }

        @Override
        public void tick() {
            LivingEntity livingEntity = this.voidShadeEntity.getTarget();
            if (livingEntity.distanceTo(this.voidShadeEntity) < 12.0D) {
                Level level = this.voidShadeEntity.level();
                ++this.cooldown;
                if (this.cooldown == 20) {
                    ((ServerLevel) level).playSound(null, this.voidShadeEntity, SoundInit.SHADOW_CAST_EVENT, SoundSource.HOSTILE, 1.0F, 1.0F);
                    Vec3 vec3d = this.voidShadeEntity.getViewVector(1.0F);
                    vec3d.add(this.voidShadeEntity.level().getRandom().nextFloat() * 0.5F - 0.25F, 0, this.voidShadeEntity.level().getRandom().nextFloat() * 0.5F - 0.25F);
                    VoidBulletEntity voidBulletEntity = new VoidBulletEntity(this.voidShadeEntity, vec3d, level);
                    this.voidShadeEntity.level().addFreshEntity(voidBulletEntity);

                    this.cooldown = -40;
                }
                if (livingEntity.distanceTo(this.voidShadeEntity) < 1.0D) {
                    this.voidShadeEntity.doHurtTarget(getServerLevel(this.voidShadeEntity), voidShadeEntity.getTarget());
                }
            } else if (this.cooldown > 0) {
                --this.cooldown;
            }
        }
    }

    static class LookAtTargetGoal extends Goal {
        private final VoidShadeEntity voidShadeEntity;

        public LookAtTargetGoal(VoidShadeEntity voidShadeEntity) {
            this.voidShadeEntity = voidShadeEntity;
            this.setFlags(EnumSet.of(Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return true;
        }

        @Override
        public void tick() {
            if (this.voidShadeEntity.getTarget() == null) {
                Vec3 vec3d = this.voidShadeEntity.getDeltaMovement();
                this.voidShadeEntity.setYRot(-((float) Mth.atan2(vec3d.x, vec3d.z)) * 57.295776F);
                this.voidShadeEntity.yBodyRot = this.voidShadeEntity.getYRot();
            } else {
                LivingEntity livingEntity = this.voidShadeEntity.getTarget();
                if (livingEntity.distanceToSqr(this.voidShadeEntity) < 4096.0D) {
                    double e = livingEntity.getX() - this.voidShadeEntity.getX();
                    double f = livingEntity.getZ() - this.voidShadeEntity.getZ();
                    this.voidShadeEntity.setYRot(-((float) Mth.atan2(e, f)) * 57.295776F);
                    this.voidShadeEntity.yBodyRot = this.voidShadeEntity.getYRot();
                }
            }

        }
    }

    static class MoveGoal extends Goal {
        private final VoidShadeEntity voidShadeEntity;

        public MoveGoal(VoidShadeEntity voidShadeEntity) {
            this.voidShadeEntity = voidShadeEntity;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.voidShadeEntity.getTarget() != null) {
                return true;
            } else
                return false;
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void start() {
            if (this.voidShadeEntity.getTarget() != null) {
                LivingEntity livingEntity = this.voidShadeEntity.getTarget();
                this.voidShadeEntity.getMoveControl().setWantedPosition(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), 1.0D);

            }
        }
    }

    static class VoidShadeMoveControl extends MoveControl {
        private final VoidShadeEntity voidShadeEntity;

        public VoidShadeMoveControl(VoidShadeEntity voidShadeEntity) {
            super(voidShadeEntity);
            this.voidShadeEntity = voidShadeEntity;
        }

        @Override
        public void tick() {
            if (this.operation == MoveControl.Operation.MOVE_TO) {
                Vec3 vec3d = new Vec3(this.wantedX - this.voidShadeEntity.getX(), this.wantedY - this.voidShadeEntity.getY(), this.wantedZ - this.voidShadeEntity.getZ());
                vec3d = vec3d.normalize();
                this.voidShadeEntity.setDeltaMovement(this.voidShadeEntity.getDeltaMovement().add(vec3d.scale(0.012D)));
                if (this.voidShadeEntity.getTarget() == null) {
                    this.setWait();
                }

            }
        }
    }

}
