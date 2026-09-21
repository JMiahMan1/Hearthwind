package net.satisfy.meadow.core.entity;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.boat.ChestBoat;
import net.minecraft.world.level.Level;
import net.satisfy.meadow.core.registry.EntityTypeRegistry;
import net.satisfy.meadow.core.registry.ObjectRegistry;
import org.jetbrains.annotations.NotNull;

public class PineChestBoatEntity extends ChestBoat {

    private static final EntityDataAccessor<Integer> WOOD_TYPE = SynchedEntityData.defineId(PineChestBoatEntity.class, EntityDataSerializers.INT);

    public PineChestBoatEntity(EntityType<? extends ChestBoat> entityType, Level level) {
        super(entityType, level, ObjectRegistry.PINE_CHEST_BOAT);
    }

    public PineChestBoatEntity(Level level, double x, double y, double z) {
        this(EntityTypeRegistry.PINE_CHEST_BOAT.get(), level);
        this.setPos(x, y, z);
        this.xo = x;
        this.yo = y;
        this.zo = z;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(WOOD_TYPE, PineBoatEntity.Type.PINE.ordinal());
    }

    @Override
    protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setWoodType(PineBoatEntity.Type.byName(input.read("Type", com.mojang.serialization.Codec.STRING).orElse("pine")));
    }

    @Override
    protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("Type", this.getWoodType().getName());
    }

    public PineBoatEntity.Type getWoodType() {
        return PineBoatEntity.Type.byId(this.entityData.get(WOOD_TYPE));
    }

    public void setWoodType(PineBoatEntity.Type type) {
        this.entityData.set(WOOD_TYPE, type.ordinal());
    }

    public PineBoatEntity.Type getModVariant() {
        return PineBoatEntity.Type.byId(this.entityData.get(WOOD_TYPE));
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entity) {
        return new ClientboundAddEntityPacket(this, entity);
    }
}
