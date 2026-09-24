package net.adventurez.entity.nonliving;

import net.minecraft.sounds.SoundSource;
import net.adventurez.init.EntityInit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;

public class FireBreathEntity extends AbstractHurtingProjectile {

    private int removeTicker;

    public FireBreathEntity(EntityType<? extends AbstractHurtingProjectile> entityType, Level level) {
        super(entityType, level);
    }

    @Environment(EnvType.CLIENT)
    public FireBreathEntity(Level level, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        this(EntityInit.FIRE_BREATH, level);
        this.setPos(x, y, z); this.setYRot(this.getYRot()); this.setXRot(this.getXRot());
        this.shoot(velocityX, velocityY, velocityZ, 1.0F, 0.0F);
    }

    public FireBreathEntity(Level level, LivingEntity owner, Vec3 velocity) {
        super(EntityInit.FIRE_BREATH, owner, velocity, level);
        Vec3 newVec3d = velocity.normalize().add(this.random.nextGaussian() * 0.1D, -this.random.nextDouble() * 0.1D, this.random.nextGaussian() * 0.1D);
        this.setDeltaMovement(newVec3d);
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.HOSTILE;
    }

    @Override
    protected ParticleOptions getTrailParticle() {
        return new BlockParticleOption(ParticleTypes.BLOCK, Blocks.AIR.defaultBlockState());
    }

    @Override
    protected float getInertia() {
        return 0.9F;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            for (int i = 0; i < 10; i++) {
                double d = (double) this.level().getRandom().nextGaussian() * 0.01D;
                double e = (double) this.level().getRandom().nextGaussian() * 0.01D;
                double f = (double) this.level().getRandom().nextGaussian() * 0.01D;
                this.level().addParticle(ParticleTypes.FLAME, this.getRandomX(1.0D), this.getRandomY(), this.getRandomZ(1.0D), d, e, f);
            }
        } else {
            removeTicker++;
            if (removeTicker >= 80)
                this.discard();
        }
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Environment(EnvType.CLIENT)
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 16384.0D;
    }

    @Override
    public float getLightLevelDependentMagicValue() {
        return 1.0F;
    }

    @Override
    public void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        Entity entity = this.getOwner();
        Entity hittedEntity = entityHitResult.getEntity();
        if (!this.level().isClientSide() && entity != null && hittedEntity instanceof LivingEntity) {
            hittedEntity.igniteForTicks(8);
            hittedEntity.hurt(createDamageSource(this), 3.0F);
            this.discard();
        }

    }

    private DamageSource createDamageSource(Entity entity) {
        return entity.damageSources().source(EntityInit.FIRE_BREATH_KEY, entity);
    }

    @Override
    public void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        if (!this.level().isClientSide()) {
            Entity entity = this.getOwner();
            if (entity == null || !(entity instanceof Mob) || ((ServerLevel) this.level()).getGameRules().get(GameRules.MOB_GRIEFING)) {
                BlockPos blockPos = blockHitResult.getBlockPos().relative(blockHitResult.getDirection());
                if (this.level().getBlockState(blockPos).isAir()) {
                    this.level().setBlock(blockPos, BaseFireBlock.getState(this.level(), blockPos), Block.UPDATE_ALL);
                }
            }
            this.discard();
        }
    }

}
