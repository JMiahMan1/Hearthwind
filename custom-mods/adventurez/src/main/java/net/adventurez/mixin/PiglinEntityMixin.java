package net.adventurez.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;

import net.adventurez.entity.PiglinBeastEntity;
import net.adventurez.init.ConfigInit;
import net.adventurez.init.EntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

@SuppressWarnings("deprecation")
@Mixin(Piglin.class)
public abstract class PiglinEntityMixin extends AbstractPiglin {

    public PiglinEntityMixin(EntityType<? extends AbstractPiglin> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void die(DamageSource source) {
        if (!this.level().isClientSide() && ConfigInit.CONFIG.piglin_beast_attack_piglin_spawn_chance != 0) {
            if (source.getEntity() != null && source.getEntity() instanceof Player && this.level().dimension() == Level.NETHER) {
                int spawnChanceInt = this.level().getRandom().nextInt(ConfigInit.CONFIG.piglin_beast_attack_piglin_spawn_chance) + 1;
                if (spawnChanceInt == 1 && isEntityNearby(Piglin.class, 12D, 2) && !isEntityNearby(PiglinBeastEntity.class, 40D, 1)) {
                    ServerLevel serverLevel = (ServerLevel) this.level();
                    PiglinBeastEntity beastEntity = EntityInit.PIGLIN_BEAST.create(serverLevel, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
                    if (beastEntity == null) {
                        super.die(source);
                        return;
                    }
                    int posYOfPlayer = this.blockPosition().getY();
                    for (int counter = 0; counter < 100; counter++) {
                        float randomFloat = this.level().getRandom().nextFloat() * 6.2831855F;
                        int posX = this.blockPosition().getX() + Mth.floor(Mth.cos(randomFloat) * 26.0F + this.level().getRandom().nextInt(5));
                        int posZ = this.blockPosition().getZ() + Mth.floor(Mth.sin(randomFloat) * 26.0F + this.level().getRandom().nextInt(5));
                        int posY = posYOfPlayer - 20 + this.level().getRandom().nextInt(40);
                        BlockPos spawnPos = new BlockPos(posX, posY, posZ);

                        if (this.level().hasChunksAt(spawnPos.getX() - 4, spawnPos.getY() - 4, spawnPos.getZ() - 4, spawnPos.getX() + 4, spawnPos.getY() + 4, spawnPos.getZ() + 4)
                                && SpawnPlacements.isSpawnPositionOk(EntityInit.PIGLIN_BEAST, serverLevel, spawnPos)
                                && SpawnPlacements.checkSpawnRules(EntityInit.PIGLIN_BEAST, serverLevel, EntitySpawnReason.EVENT, spawnPos, this.level().getRandom())) {
                            beastEntity.setPos(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D);
                            beastEntity.setYRot(this.level().getRandom().nextFloat() * 360.0F);
                            beastEntity.setXRot(0.0F);
                            beastEntity.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(this.blockPosition()), EntitySpawnReason.EVENT, null);
                            this.level().addFreshEntity(beastEntity);
                            beastEntity.spawnAnim();
                            break;
                        }
                    }
                }
            }
        }
        super.die(source);
    }

    private boolean isEntityNearby(Class<? extends Entity> entityClass, double distance, int count) {
        List<? extends Entity> list = this.level().getEntitiesOfClass(entityClass, this.getBoundingBox().inflate(distance), EntitySelector.NO_SPECTATORS);
        return list.size() >= count;
    }

}
