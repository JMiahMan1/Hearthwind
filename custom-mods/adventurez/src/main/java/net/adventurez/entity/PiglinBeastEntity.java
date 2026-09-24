package net.adventurez.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;

import net.minecraft.world.level.storage.ValueInput;

import net.minecraft.world.level.storage.ValueOutput;

import net.minecraft.world.entity.EntitySelector;
import net.minecraft.sounds.SoundSource;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;

import net.adventurez.init.SoundInit;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;

public class PiglinBeastEntity extends Monster {
    public static final EntityDataAccessor<Float> ATTACK_TICK_VISUAL = SynchedEntityData.defineId(PiglinBeastEntity.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> LEAD_ARM = SynchedEntityData.defineId(PiglinBeastEntity.class, EntityDataSerializers.FLOAT);
    private float attackTick;
    public float armTick;
    private int makePiglinsAngry = 0;

    public PiglinBeastEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 30;
        this.setPathfindingMalus(PathType.LAVA, 8.0F);
    }

    public static AttributeSupplier.Builder createPiglinBeastAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 80.0D).add(Attributes.MOVEMENT_SPEED, 0.225D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D).add(Attributes.ATTACK_DAMAGE, 9.0D).add(Attributes.ATTACK_KNOCKBACK, 2D)
                .add(Attributes.FOLLOW_RANGE, 38.0D);
    }

    @Override
    public void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new AttackGoal());
        this.goalSelector.addGoal(4, new RandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Mob.class, 8.0F));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, WitherSkeleton.class, true));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, BlazeGuardianEntity.class, true));
        this.targetSelector.addGoal(4, (new HurtByTargetGoal(this, new Class[] { Piglin.class })));
    }

    public void customServerAiStep(ServerLevel level) {
        if (attackTick > 0F) {
            attackTick = attackTick - 0.08F;
            entityData.set(ATTACK_TICK_VISUAL, attackTick);
        }
        if (this.getTarget() instanceof Player) {
            makePiglinsAngry++;
            if (makePiglinsAngry == 600) {
                entityData.set(LEAD_ARM, 1F);
                getPiglins();
                this.level().playSound(null, this, SoundInit.PIGLINBEAST_SHOUT_EVENT, SoundSource.HOSTILE, 1.0F, 1.0F);
                this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.0D);
            }
            if (makePiglinsAngry > 600) {
                entityData.set(LEAD_ARM, entityData.get(LEAD_ARM) - 0.02F);
            }
            if (makePiglinsAngry == 650) {
                this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.23D);
                makePiglinsAngry = 0;
            }
        }
    }

    @Override
    public void addAdditionalSaveData(ValueOutput tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("AttackTick", this.attackTick);
        tag.putFloat("LeadArm", 0F);
    }

    @Override
    public void readAdditionalSaveData(ValueInput tag) {
        super.readAdditionalSaveData(tag);
        this.attackTick = tag.getFloatOr("AttackTick", 0F);
        this.armTick = tag.getFloatOr("LeadArm", 0F);
    }

    @Override
    protected void defineSynchedData(Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ATTACK_TICK_VISUAL, 0F);
        builder.define(LEAD_ARM, 0F);
    }

    @Override
    public boolean checkSpawnObstruction(LevelReader level) {
        return super.checkSpawnObstruction(level) && !this.level().containsAnyLiquid(this.getBoundingBox());
    }

    public boolean mustNotDisturb(double num) {
        return false;
    }

    @Override
    public void checkDespawn() {
        if (this.level().getDifficulty() == Difficulty.PEACEFUL) {
            this.discard();
        }

    }

    @Override
    public boolean canUsePortal(boolean allowVehicles) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundInit.PIGLINBEAST_IDLE_EVENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundInit.PIGLINBEAST_HURT_EVENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundInit.PIGLINBEAST_DEATH_EVENT;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundInit.PIGLINBEAST_WALK_EVENT, 0.5F, 1.0F);
    }

    private class AttackGoal extends MeleeAttackGoal {

        public AttackGoal() {
            super(PiglinBeastEntity.this, 1.0D, true);
        }

        @Override
        protected void checkAndPerformAttack(LivingEntity target) {
            if (canPerformAttack(target) && attackTick <= 0F) {
                this.mob.level().playSound(null, PiglinBeastEntity.this, SoundInit.PIGLINBEAST_CLUBSWING_EVENT, SoundSource.HOSTILE, 1.0F, 1.0F);
                this.resetAttackCooldown();
                this.mob.doHurtTarget(getServerLevel(this.mob), mob.getTarget());
                attackTick = 1F;
            }
        }
    }

    public void getPiglins() {
        List<Piglin> list = this.level().getEntitiesOfClass(Piglin.class, this.getBoundingBox().inflate(40D), EntitySelector.NO_SPECTATORS);
        for (int i = 0; i < list.size(); ++i) {
            angerNearbyPiglins(list.get(i));
        }
    }

    private static void angerNearbyPiglins(AbstractPiglin piglin) {
        getNearbyPiglins(piglin).forEach((abstractPiglin) -> {
            getNearestDetectedPlayer(abstractPiglin).ifPresent((playerEntity) -> {
                becomeAngryWith(abstractPiglin, playerEntity);
            });
        });
    }

    public static Optional<Player> getNearestDetectedPlayer(AbstractPiglin piglin) {
        return piglin.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER) ? piglin.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER)
                : Optional.empty();
    }

    public static void becomeAngryWith(AbstractPiglin piglin, LivingEntity target) {
        piglin.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        piglin.getBrain().setMemoryWithExpiry(MemoryModuleType.ANGRY_AT, target.getUUID(), 600L);

        if (target.getType() == EntityTypes.PLAYER && ((ServerLevel) piglin.level()).getGameRules().get(GameRules.UNIVERSAL_ANGER)) {
            piglin.getBrain().setMemoryWithExpiry(MemoryModuleType.UNIVERSAL_ANGER, true, 600L);
        }
        if (!piglin.level().isClientSide()) {
            piglin.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(piglin.getAttributeValue(Attributes.ATTACK_DAMAGE) + 3.0D);
            piglin.heal(piglin.getMaxHealth());
        }

    }

    private static List<AbstractPiglin> getNearbyPiglins(AbstractPiglin piglin) {
        return (List<AbstractPiglin>) piglin.getBrain().getMemory(MemoryModuleType.NEARBY_ADULT_PIGLINS).orElse(ImmutableList.of());
    }

}
