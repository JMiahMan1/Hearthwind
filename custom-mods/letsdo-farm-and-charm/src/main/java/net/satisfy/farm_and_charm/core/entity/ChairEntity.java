package net.satisfy.farm_and_charm.core.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.satisfy.farm_and_charm.core.util.GeneralUtil;

public class ChairEntity extends Entity {
    public ChairEntity(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }

    @Override
    public boolean hurtServer(net.minecraft.server.level.ServerLevel serverLevel, net.minecraft.world.damagesource.DamageSource source, float amount) {
        this.discard();
        return true;
    }

    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        if (passenger instanceof Player p) {
            BlockPos pos = GeneralUtil.getPreviousPlayerPosition(p, this);
            if (pos != null) {
                this.discard();
                return new Vec3((double)pos.getX() + 0.5, (double)pos.getY(), (double)pos.getZ() + 0.5);
            }
        }

        this.discard();
        return super.getDismountLocationForPassenger(passenger);
    }

    public void remove(RemovalReason reason) {
        super.remove(reason);
        GeneralUtil.removeChairEntity(this.level(), this.blockPosition());
    }

    @Override
    protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput input) {
    }

    @Override
    protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput output) {
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity server) {
        return new ClientboundAddEntityPacket(this, server);
    }
}