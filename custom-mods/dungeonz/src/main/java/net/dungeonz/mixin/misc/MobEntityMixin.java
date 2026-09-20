package net.dungeonz.mixin.misc;

import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dungeonz.access.BossEntityAccess;
import net.dungeonz.block.entity.DungeonPortalEntity;
import net.dungeonz.init.BlockInit;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;

@Mixin(Mob.class)
public abstract class MobEntityMixin extends LivingEntity implements BossEntityAccess {

    @Unique
    private boolean isDungeonBossEntity = false;
    @Unique
    private BlockPos portalPos = new BlockPos(0, 0, 0);
    @Unique
    private String worldRegistryKey = "";

    public MobEntityMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void writeCustomDataToNbtMixin(ValueOutput output, CallbackInfo info) {
        if (this.isDungeonBossEntity) {
            output.putBoolean("IsDungeonBossEntity", this.isDungeonBossEntity);
            output.putString("WorldRegistryKey", this.worldRegistryKey);
            output.putInt("PortalPosX", this.portalPos.getX());
            output.putInt("PortalPosY", this.portalPos.getY());
            output.putInt("PortalPosZ", this.portalPos.getZ());
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void readCustomDataFromNbtMixin(ValueInput input, CallbackInfo info) {
        if (input.getBooleanOr("IsDungeonBossEntity", false)) {
            this.isDungeonBossEntity = input.getBooleanOr("IsDungeonBossEntity", false);
            this.worldRegistryKey = input.getStringOr("WorldRegistryKey", "");
            this.portalPos = new BlockPos(input.getIntOr("PortalPosX", 0), input.getIntOr("PortalPosY", 0), input.getIntOr("PortalPosZ", 0));
        }
    }

    @Override
    public void die(DamageSource damageSource) {
        if (!this.level().isClientSide() && this.isDungeonBossEntity) {
            ServerLevel nonDungeonWorld = level().getServer().getLevel(ResourceKey.create(Registries.DIMENSION, Identifier.parse(this.worldRegistryKey)));

            if (nonDungeonWorld != null && nonDungeonWorld.getBlockEntity(this.portalPos) != null && nonDungeonWorld.getBlockEntity(this.portalPos) instanceof DungeonPortalEntity) {
                ((DungeonPortalEntity) nonDungeonWorld.getBlockEntity(this.portalPos)).finishDungeon((ServerLevel) this.level(), this.blockPosition());
            } else {
                this.level().setBlockAndUpdate(this.blockPosition(), BlockInit.DUNGEON_PORTAL.defaultBlockState());
            }

        }
        super.die(damageSource);

    }

    @Override
    public void setBoss(BlockPos portalPos, String worldRegistryKey) {
        this.isDungeonBossEntity = true;
        this.portalPos = portalPos;
        this.worldRegistryKey = worldRegistryKey;
    }

}
