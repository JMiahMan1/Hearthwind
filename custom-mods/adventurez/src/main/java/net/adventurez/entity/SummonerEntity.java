package net.adventurez.entity;

import net.minecraft.world.entity.ai.goal.PanicGoal;

import net.minecraft.world.entity.ai.goal.target.TargetGoal;

import net.minecraft.world.level.storage.ValueInput;

import net.minecraft.world.level.storage.ValueOutput;

import net.minecraft.world.entity.EntitySelector;
import net.minecraft.sounds.SoundSource;
import java.util.List;

import net.adventurez.init.EntityInit;
import net.adventurez.init.SoundInit;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
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
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.ServerLevelAccessor;

public class SummonerEntity extends SpellCastingEntity {
    public static final EntityDataAccessor<Boolean> INVULNERABLE_SHIELD = SynchedEntityData.defineId(SummonerEntity.class, EntityDataSerializers.BOOLEAN);
    private int invulnerableMagicTick;
    private boolean gotShotByABow = false;

    public SummonerEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createSummonerAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 55.0D).add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D).add(Attributes.ATTACK_KNOCKBACK, 2.2D).add(Attributes.FOLLOW_RANGE, 35.0D);
    }

    @Override
    public void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LookAtTargetGoalNecro());
        this.goalSelector.addGoal(2, new NormalAttack(this, 1.0D, false));
        this.goalSelector.addGoal(2, new PanicGoal(this, 0.7D));
        this.goalSelector.addGoal(4, new SummonPuppetGoal());
        this.goalSelector.addGoal(5, new ThunderboltSpellGoal());
        this.goalSelector.addGoal(6, new InvulnerableSpellGoal());
        this.goalSelector.addGoal(7, new TeleportSpellGoal());
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 7.0F, 1.0F));
        this.goalSelector.addGoal(9, new WaterAvoidingRandomStrollGoal(this, 0.9D));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
        this.targetSelector.addGoal(1, (new HurtByTargetGoal(this, new Class[] { SkeletonVanguardEntity.class })));
        this.targetSelector.addGoal(2, (new HurtByTargetGoal(this, new Class[] { Zombie.class })));
        this.targetSelector.addGoal(3, (new HurtByTargetGoal(this, new Class[] { Skeleton.class })));
        this.targetSelector.addGoal(4, (new NearestAttackableTargetGoal<>(this, Player.class, true)).setUnseenMemoryTicks(300));
    }

    public static boolean canSpawn(EntityType<SummonerEntity> type, ServerLevelAccessor level, EntitySpawnReason spawnReason, BlockPos pos, RandomSource random) {
        return (Monster.checkMonsterSpawnRules(type, level, spawnReason, pos, random) && level.canSeeSky(pos) && level.getLevel().getWeatherData().isRaining()) || spawnReason == EntitySpawnReason.SPAWNER;
    }

    @Override
    public void addAdditionalSaveData(ValueOutput tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("InvulnerableMagicTick", this.invulnerableMagicTick);
    }

    @Override
    public void readAdditionalSaveData(ValueInput tag) {
        super.readAdditionalSaveData(tag);
        this.invulnerableMagicTick = tag.getIntOr("InvulnerableMagicTick", 0);
    }

    @Override
    protected void defineSynchedData(Builder builder) {
        super.defineSynchedData(builder);
        builder.define(INVULNERABLE_SHIELD, false);
    }

    public void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (entityData.get(INVULNERABLE_SHIELD)) {
            this.setInvulnerable(true);
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.0D);
            this.invulnerableMagicTick--;
            if (this.invulnerableMagicTick < 0) {
                this.setInvulnerable(false);
                this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.3D);
                entityData.set(INVULNERABLE_SHIELD, false);
            }
        }

    }

    public int getLimitPerChunk() {
        return 1;
    }

    @Override
    public boolean canFreeze() {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WITHER_SKELETON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SKELETON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SKELETON_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.WITHER_SKELETON_STEP, 0.5F, 0.7F);
    }

    @Override
    public SoundEvent getCastSpellSound() {
        return SoundEvents.EVOKER_CAST_SPELL;
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource source, float amount) {
        int chance = 0;
        if (source.is(DamageTypeTags.IS_LIGHTNING)) {
            return false;
        }
        if (source.is(DamageTypeTags.IS_PROJECTILE)) {
            chance = this.level().getRandom().nextInt(2);
            this.gotShotByABow = true;
        }
        if (!source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) &&chance == 1) {
            this.level().playSound(null, this, SoundInit.MAGIC_SHIELD_HIT_EVENT, SoundSource.HOSTILE, 1.0F, 1.0F);
            if (source.getEntity() != null && source.getEntity() instanceof Arrow) {
                if (!this.level().isClientSide()) {
                    source.getEntity().discard();
                }
            }
            return false;
        } else {
            return !this.isInvulnerableTo(serverLevel, source) && super.hurtServer(serverLevel, source, amount);
        }
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
            LivingEntity attacker = SummonerEntity.this.getTarget();
            if (super.canUse() && attacker != null && attacker.getMainHandItem().getItem() instanceof ProjectileWeaponItem && SummonerEntity.this.distanceToSqr(attacker) > 12
                    && SummonerEntity.this.gotShotByABow) {
                return true;
            } else
                return false;
        }

        @Override
        public void castSpell() {
            LivingEntity attacker = SummonerEntity.this.getTarget();
            if (attacker != null) {
                ServerLevel serverWorld = (ServerLevel) attacker.level();
                double posX = attacker.getX() + SummonerEntity.this.level().getRandom().nextInt(3);
                double posY = attacker.getY();
                double posZ = attacker.getZ() + SummonerEntity.this.level().getRandom().nextInt(3);
                BlockPos pos = BlockPos.containing(posX, posY, posZ);
                LightningBolt lightningEntity = (LightningBolt) EntityTypes.LIGHTNING_BOLT.create(attacker.level(), net.minecraft.world.entity.EntitySpawnReason.COMMAND);
                lightningEntity.setPos(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D); lightningEntity.setYRot(0.0F); lightningEntity.setXRot(0.0F);
                serverWorld.addFreshEntity(lightningEntity);
            }
            gotShotByABow = false;
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

    private class TeleportSpellGoal extends SpellCastingEntity.CastSpellGoal {
        private TeleportSpellGoal() {
            super();
        }

        @Override
        protected int getInitialCooldown() {
            return 200;
        }

        @Override
        public int getSpellTicks() {
            return 40;
        }

        @Override
        public int startTimeDelay() {
            return 60;
        }

        @Override
        public boolean canUse() {
            if (!super.canUse()) {
                return false;
            } else
                return true;
        }

        @SuppressWarnings("deprecation")
        @Override
        public void castSpell() {
            for (int counter = 0; counter < 100; counter++) {
                float randomFloat = SummonerEntity.this.level().getRandom().nextFloat() * 6.2831855F;
                int posX = SummonerEntity.this.blockPosition().getX() + Mth.floor(Mth.cos(randomFloat) * 8.0F + SummonerEntity.this.level().getRandom().nextInt(12));
                int posZ = SummonerEntity.this.blockPosition().getZ() + Mth.floor(Mth.sin(randomFloat) * 8.0F + SummonerEntity.this.level().getRandom().nextInt(12));
                int posY = SummonerEntity.this.level().getHeight(Heightmap.Types.WORLD_SURFACE, posX, posZ);
                BlockPos teleportPos = new BlockPos(posX, posY, posZ);
                if (SummonerEntity.this.level().isLoaded(teleportPos)
                        && SpawnPlacements.checkSpawnRules(EntityInit.SUMMONER, (ServerLevel) SummonerEntity.this.level(), EntitySpawnReason.EVENT, teleportPos, SummonerEntity.this.getRandom())) {
                    SummonerEntity.this.teleportTo(teleportPos.getX(), teleportPos.getY(), teleportPos.getZ());
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
            return SpellCastingEntity.Spell.TELEPORT;
        }
    }

    private class InvulnerableSpellGoal extends SpellCastingEntity.CastSpellGoal {
        private InvulnerableSpellGoal() {
            super();
        }

        @Override
        protected int getInitialCooldown() {
            return 420;
        }

        @Override
        public int getSpellTicks() {
            return 40;
        }

        @Override
        public int startTimeDelay() {
            return 60;
        }

        @Override
        public boolean canUse() {
            if (!super.canUse() || (SummonerEntity.this.getTarget() != null && SummonerEntity.this.distanceToSqr(SummonerEntity.this.getTarget()) > 7D)) {
                return false;
            } else
                return true;
        }

        @Override
        public void tick() {
            --this.spellCooldown;
            if (this.spellCooldown == 0) {
                this.castSpell();
                SummonerEntity.this.playSound(SoundInit.SPELL_CAST_SHIELD_EVENT, 1.0F, 1.0F);
            }

        }

        @Override
        public void castSpell() {
            entityData.set(INVULNERABLE_SHIELD, true);
            SummonerEntity.this.invulnerableMagicTick = 160;
            for (int i = 0; i < 60; ++i) {
                ((ServerLevel) SummonerEntity.this.level()).sendParticles(ParticleTypes.END_ROD, SummonerEntity.this.getRandomX(1.5D), SummonerEntity.this.getRandomY(),
                        SummonerEntity.this.getRandomZ(1.5D), 0, 0.0D, 0.0D, 0.0D, 0.0D);
            }
            if (SummonerEntity.this.getTarget() != null) {
                SummonerEntity.this.getTarget().addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 1, false, false));
            }

        }

        @Override
        public SoundEvent getSoundPrepare() {
            return SoundEvents.EVOKER_PREPARE_SUMMON;
        }

        @Override
        public SpellCastingEntity.Spell getSpell() {
            return SpellCastingEntity.Spell.SHIELD;
        }
    }

    private class SummonPuppetGoal extends SpellCastingEntity.CastSpellGoal {
        private final TargetingConditions PUPPET_PREDICATE = TargetingConditions.forCombat().range(24.0D).ignoreLineOfSight();

        private SummonPuppetGoal() {
            super();

        }

        @Override
        public boolean canUse() {
            if (!super.canUse() || tooManySlaves()) {
                return false;
            } else {
                int i = SummonerEntity.this.level().getEntitiesOfClass(WitherPuppetEntity.class,
                        SummonerEntity.this.getBoundingBox().inflate(16.0D),
                        puppet -> this.PUPPET_PREDICATE.test((ServerLevel) SummonerEntity.this.level(), SummonerEntity.this, puppet)).size();
                return SummonerEntity.this.getRandom().nextInt(8) + 1 > i;
            }
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
            ServerLevel serverWorld = (ServerLevel) SummonerEntity.this.level();
            int spellCount = 0;
            for (int i = 0; i < 20; ++i) {
                BlockPos blockPos = SummonerEntity.this.blockPosition().offset(-2 + serverWorld.getRandom().nextInt(5), serverWorld.getRandom().nextInt(3), -2 + serverWorld.getRandom().nextInt(5));
                if (SpawnPlacements.checkSpawnRules(EntityInit.SKELETON_VANGUARD, serverWorld, EntitySpawnReason.EVENT, blockPos, serverWorld.getRandom())) {
                    spellCount++;
                    if (SummonerEntity.this.getHealth() <= 40.0F || SummonerEntity.this.level().getDefaultClockTime() % 24000L < 13000L) {
                        SkeletonVanguardEntity skeletonVanguardEntity = EntityInit.SKELETON_VANGUARD.create(serverWorld, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
                        skeletonVanguardEntity.setPos(blockPos.getX() + 0.5D, blockPos.getY(), blockPos.getZ() + 0.5D); skeletonVanguardEntity.setYRot(serverWorld.getRandom().nextFloat() * 360F); skeletonVanguardEntity.setXRot(0.0F);
                        skeletonVanguardEntity.finalizeSpawn(serverWorld, serverWorld.getCurrentDifficultyAt(blockPos), EntitySpawnReason.EVENT, null);
                        serverWorld.addFreshEntity(skeletonVanguardEntity);
                    } else {
                        Zombie zombieEntity = EntityTypes.ZOMBIE.create(serverWorld, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
                        zombieEntity.setPos(blockPos.getX() + 0.5D, blockPos.getY(), blockPos.getZ() + 0.5D); zombieEntity.setYRot(serverWorld.getRandom().nextFloat() * 360F); zombieEntity.setXRot(0.0F);
                        zombieEntity.finalizeSpawn(serverWorld, serverWorld.getCurrentDifficultyAt(blockPos), EntitySpawnReason.EVENT, null);
                        serverWorld.addFreshEntity(zombieEntity);
                        int skeletonChance = serverWorld.getRandom().nextInt(8);
                        if (skeletonChance == 0) {
                            Skeleton skeletonEntity = EntityTypes.SKELETON.create(serverWorld, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
                            skeletonEntity.setPos(blockPos.getX() + 0.5D, blockPos.getY(), blockPos.getZ() + 0.5D); skeletonEntity.setYRot(serverWorld.getRandom().nextFloat() * 360F); skeletonEntity.setXRot(0.0F);
                            skeletonEntity.finalizeSpawn(serverWorld, serverWorld.getCurrentDifficultyAt(blockPos), EntitySpawnReason.EVENT, null);
                            if (SummonerEntity.this.gotShotByABow) {
                                skeletonEntity.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
                            }
                            serverWorld.addFreshEntity(skeletonEntity);
                        }
                    }
                }
                if (spellCount >= 3)
                    break;
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

    private class LookAtTargetGoalNecro extends SpellCastingEntity.LookAtTargetGoal {
        private LookAtTargetGoalNecro() {
            super();
        }

        @Override
        public void tick() {
            if (SummonerEntity.this.getTarget() != null) {
                SummonerEntity.this.getLookControl().setLookAt(SummonerEntity.this.getTarget(), (float) SummonerEntity.this.getMaxHeadYRot(), (float) SummonerEntity.this.getMaxHeadXRot());
            }

        }
    }

    private boolean tooManySlaves() {
        List<LivingEntity> list = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(40D), EntitySelector.NO_SPECTATORS);
        if (!list.isEmpty()) {
            int vanguards = 0;
            int othermobs = 0;
            for (int i = 0; i < list.size(); i++) {
                LivingEntity entity = (LivingEntity) list.get(i);
                if (entity.getType() == EntityInit.SKELETON_VANGUARD) {
                    vanguards++;
                } else if (entity.getType() == EntityTypes.ZOMBIE || entity.getType() == EntityTypes.SKELETON) {
                    othermobs++;
                }
                if (vanguards >= 5 || othermobs >= 7) {
                    return true;
                }
            }
        }
        return false;
    }

    private class NormalAttack extends MeleeAttackGoal {
        private final SummonerEntity summonerEntity;

        public NormalAttack(PathfinderMob mob, double speed, boolean pauseWhenMobIdle) {
            super(mob, speed, pauseWhenMobIdle);
            this.summonerEntity = (SummonerEntity) mob;
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
                return !(livingEntity instanceof Player player) || !livingEntity.isSpectator() && !player.isCreative();
            }
        }

    }

}
