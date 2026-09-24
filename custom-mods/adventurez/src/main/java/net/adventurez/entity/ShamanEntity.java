package net.adventurez.entity;

import net.minecraft.world.entity.ai.goal.PanicGoal;

import net.minecraft.world.entity.ai.goal.target.TargetGoal;

import net.minecraft.world.entity.EntitySelector;
import java.util.List;

import net.adventurez.init.SoundInit;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.spider.CaveSpider;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public class ShamanEntity extends SpellCastingEntity {

    public ShamanEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createShamanAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 40.0D).add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D).add(Attributes.ATTACK_KNOCKBACK, 1.0D).add(Attributes.FOLLOW_RANGE, 35.0D);
    }

    @Override
    public void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LookAtTargetGoalShaman());
        this.goalSelector.addGoal(2, new NormalAttack(this, 1.0D, false));
        this.goalSelector.addGoal(2, new PanicGoal(this, 0.7D));
        this.goalSelector.addGoal(4, new SummonCompanionsGoal());
        this.goalSelector.addGoal(5, new ThunderboltSpellGoal());
        this.goalSelector.addGoal(7, new EffectSpellGoal());
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 7.0F, 1.0F));
        this.goalSelector.addGoal(9, new WaterAvoidingRandomStrollGoal(this, 0.9D));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
        this.targetSelector.addGoal(1, (new HurtByTargetGoal(this, new Class[] { Witch.class })));
        this.targetSelector.addGoal(2, (new HurtByTargetGoal(this, new Class[] { Zombie.class })));
        this.targetSelector.addGoal(3, (new HurtByTargetGoal(this, new Class[] { Skeleton.class })));
        this.targetSelector.addGoal(3, (new HurtByTargetGoal(this, new Class[] { Spider.class })));
        this.targetSelector.addGoal(3, (new HurtByTargetGoal(this, new Class[] { CaveSpider.class })));
        this.targetSelector.addGoal(4, (new NearestAttackableTargetGoal<>(this, Player.class, true)).setUnseenMemoryTicks(300));
    }

    public static boolean canSpawn(EntityType<ShamanEntity> type, ServerLevelAccessor level, EntitySpawnReason spawnReason, BlockPos pos, RandomSource random) {
        return (Monster.checkMonsterSpawnRules(type, level, spawnReason, pos, random) && level.canSeeSky(pos)) || spawnReason == EntitySpawnReason.SPAWNER;

    }

    public int getLimitPerChunk() {
        return 1;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundInit.SHAMAN_IDLE_EVENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundInit.SHAMAN_HURT_EVENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundInit.SHAMAN_DEATH_EVENT;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundInit.SHAMAN_WALK_EVENT, 0.5F, 1.0F);
    }

    @Override
    public SoundEvent getCastSpellSound() {
        return SoundEvents.EVOKER_CAST_SPELL;
    }

    private boolean tooManyCompanions() {
        List<LivingEntity> list = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(40D), EntitySelector.NO_SPECTATORS);
        if (!list.isEmpty()) {
            int spiders = 0;
            int othermobs = 0;
            for (int i = 0; i < list.size(); i++) {
                LivingEntity entity = (LivingEntity) list.get(i);
                if (entity.getType() == EntityTypes.CAVE_SPIDER) {
                    spiders++;
                } else if (entity.getType() == EntityTypes.ZOMBIE || entity.getType() == EntityTypes.SKELETON || entity.getType() == EntityTypes.SPIDER) {
                    othermobs++;
                }
                if (spiders >= 2 || othermobs >= 4) {
                    return true;
                }
            }
        }
        return false;
    }

    private class ThunderboltSpellGoal extends SpellCastingEntity.CastSpellGoal {
        private ThunderboltSpellGoal() {
            super();
        }

        @Override
        protected int getInitialCooldown() {
            return 200;
        }

        @Override
        public int getSpellTicks() {
            return 20;
        }

        @Override
        public int startTimeDelay() {
            return 20;
        }

        @Override
        public boolean canUse() {
            LivingEntity attacker = ShamanEntity.this.getTarget();
            if (super.canUse() && attacker != null && attacker.getMainHandItem().getItem() instanceof ProjectileWeaponItem && ShamanEntity.this.distanceToSqr(attacker) > 12
                    && ShamanEntity.this.level().isRaining()) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        public void castSpell() {
            LivingEntity attacker = ShamanEntity.this.getTarget();
            if (attacker != null) {
                ServerLevel serverWorld = (ServerLevel) attacker.level();
                double posX = attacker.getX() + ShamanEntity.this.level().getRandom().nextInt(3);
                double posY = attacker.getY();
                double posZ = attacker.getZ() + ShamanEntity.this.level().getRandom().nextInt(3);
                BlockPos pos = BlockPos.containing(posX, posY, posZ);
                LightningBolt lightningEntity = (LightningBolt) EntityTypes.LIGHTNING_BOLT.create(attacker.level(), net.minecraft.world.entity.EntitySpawnReason.COMMAND);
                lightningEntity.setPos(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D); lightningEntity.setYRot(0.0F); lightningEntity.setXRot(0.0F);
                serverWorld.addFreshEntity(lightningEntity);
            }
        }

        @Override
        public SoundEvent getSoundPrepare() {
            return SoundEvents.EVOKER_PREPARE_SUMMON;
        }

        @Override
        public SpellCastingEntity.Spell getSpell() {
            return SpellCastingEntity.Spell.THUNDERBOLT;
        }
    }

    private class EffectSpellGoal extends SpellCastingEntity.CastSpellGoal {
        private EffectSpellGoal() {
            super();
        }

        @Override
        protected int getInitialCooldown() {
            return 200;
        }

        @Override
        public int getSpellTicks() {
            return 60;
        }

        @Override
        public int startTimeDelay() {
            return 10;
        }

        @Override
        public boolean canUse() {
            if (!super.canUse()) {
                return false;
            } else
                return true;
        }

        @Override
        public void castSpell() {
            LivingEntity livingEntity = ShamanEntity.this.getTarget();
            if (livingEntity != null) {
                Holder<MobEffect> statusEffect;
                if (ShamanEntity.this.getRandom().nextFloat() <= 0.5F) {
                    statusEffect = MobEffects.WEAKNESS;
                } else {
                    statusEffect = MobEffects.POISON;
                }
                livingEntity.addEffect(new MobEffectInstance(statusEffect, 120 + ShamanEntity.this.getRandom().nextInt(80), 1, false, false, true));
            }

        }

        @Override
        public SoundEvent getSoundPrepare() {
            return SoundEvents.EVOKER_PREPARE_SUMMON;
        }

        @Override
        public SpellCastingEntity.Spell getSpell() {
            return SpellCastingEntity.Spell.WITHERING;
        }
    }

    private class SummonCompanionsGoal extends SpellCastingEntity.CastSpellGoal {
        private SummonCompanionsGoal() {
            super();
        }

        @Override
        public boolean canUse() {
            if (!super.canUse() || tooManyCompanions()) {
                return false;
            } else
                return true;
        }

        @Override
        public int getSpellTicks() {
            return 140;
        }

        @Override
        public int startTimeDelay() {
            return 340;
        }

        @Override
        public void castSpell() {
            ServerLevel serverWorld = (ServerLevel) ShamanEntity.this.level();
            int spellCount = 0;
            for (int i = 0; i < 20; ++i) {
                BlockPos blockPos = ShamanEntity.this.blockPosition().offset(-3 + ShamanEntity.this.getRandom().nextInt(6), ShamanEntity.this.getRandom().nextInt(3),
                        -3 + ShamanEntity.this.getRandom().nextInt(6));
                if (SpawnPlacements.checkSpawnRules(EntityTypes.ZOMBIE, serverWorld, EntitySpawnReason.EVENT, blockPos, serverWorld.getRandom())) {
                    spellCount++;
                    if (ShamanEntity.this.getRandom().nextFloat() < 0.6F) {
                        CaveSpider caveSpider = EntityTypes.CAVE_SPIDER.create(serverWorld, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
                        caveSpider.setPos(blockPos.getX() + 0.5D, blockPos.getY(), blockPos.getZ() + 0.5D); caveSpider.setYRot(serverWorld.getRandom().nextFloat() * 360F); caveSpider.setXRot(0.0F);
                        caveSpider.finalizeSpawn(serverWorld, serverWorld.getCurrentDifficultyAt(blockPos), EntitySpawnReason.EVENT, null);
                        serverWorld.addFreshEntity(caveSpider);
                    } else {
                        Mob mobEntity;
                        int randomInt = ShamanEntity.this.getRandom().nextInt(3);
                        if (randomInt == 0) {
                            mobEntity = EntityTypes.ZOMBIE.create(serverWorld, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
                        } else if (randomInt == 1) {
                            mobEntity = EntityTypes.SKELETON.create(serverWorld, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
                        } else {
                            mobEntity = EntityTypes.SPIDER.create(serverWorld, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
                        }
                        mobEntity.setPos(blockPos.getX() + 0.5D, blockPos.getY(), blockPos.getZ() + 0.5D); mobEntity.setYRot(serverWorld.getRandom().nextFloat() * 360F); mobEntity.setXRot(0.0F);
                        mobEntity.finalizeSpawn(serverWorld, serverWorld.getCurrentDifficultyAt(blockPos), EntitySpawnReason.EVENT, null);
                        serverWorld.addFreshEntity(mobEntity);
                    }
                }
                if (spellCount >= 3) {
                    break;
                }
            }

        }

        @Override
        public SoundEvent getSoundPrepare() {
            return SoundEvents.EVOKER_PREPARE_SUMMON;
        }

        @Override
        public SpellCastingEntity.Spell getSpell() {
            return SpellCastingEntity.Spell.SUMMON_PUPPET;
        }
    }

    private class LookAtTargetGoalShaman extends SpellCastingEntity.LookAtTargetGoal {
        private LookAtTargetGoalShaman() {
            super();
        }

        @Override
        public void tick() {
            if (ShamanEntity.this.getTarget() != null) {
                ShamanEntity.this.getLookControl().setLookAt(ShamanEntity.this.getTarget(), (float) ShamanEntity.this.getMaxHeadYRot(), (float) ShamanEntity.this.getMaxHeadXRot());
            }

        }
    }

    private class NormalAttack extends MeleeAttackGoal {
        private final ShamanEntity summonerEntity;

        public NormalAttack(PathfinderMob mob, double speed, boolean pauseWhenMobIdle) {
            super(mob, speed, pauseWhenMobIdle);
            this.summonerEntity = (ShamanEntity) mob;
        }

        @Override
        public boolean canUse() {
            LivingEntity livingEntity = this.summonerEntity.getTarget();
            return livingEntity != null && livingEntity.isAlive() && this.summonerEntity.canAttack(livingEntity) && this.summonerEntity.distanceToSqr(livingEntity) < 7.0D && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity livingEntity = this.mob.getTarget();
            if (livingEntity == null || this.summonerEntity.distanceToSqr(livingEntity) > 5D) {
                return false;
            } else if (!livingEntity.isAlive()) {
                return false;
            } else if (this.mob.getNavigation().getTargetPos() == null || this.mob.distanceToSqr(livingEntity) > 25.0D) {
                return false;
            } else {
                return !(livingEntity instanceof Player) || !livingEntity.isSpectator() && !((Player) livingEntity).isCreative();
            }
        }

    }

}
