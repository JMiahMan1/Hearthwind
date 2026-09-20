package net.dungeonz.block.entity;

import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.dungeonz.block.logic.DungeonSpawnerLogic;
import net.dungeonz.init.BlockInit;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.world.level.Spawner;
import org.jetbrains.annotations.Nullable;

public class DungeonSpawnerEntity extends BlockEntity implements Spawner {
    private final DungeonSpawnerLogic logic = new DungeonSpawnerLogic() {

        @Override
        public void sendStatus(Level world, BlockPos pos, int status) {
            world.blockEvent(pos, BlockInit.DUNGEON_SPAWNER, status, 0);
        }

        @Override
        public void setSpawnEntry(@Nullable Level world, BlockPos pos, SpawnData spawnEntry) {
            super.setSpawnEntry(world, pos, spawnEntry);
            if (world != null) {
                BlockState blockState = world.getBlockState(pos);
                world.sendBlockUpdated(pos, blockState, blockState, Block.UPDATE_INVISIBLE);
            }
        }
    };

    public DungeonSpawnerEntity(BlockPos pos, BlockState state) {
        super(BlockInit.DUNGEON_SPAWNER_ENTITY, pos, state);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.logic.readNbt(this.level, this.worldPosition, input);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        this.logic.writeNbt(output);
    }

    public static void clientTick(Level world, BlockPos pos, BlockState state, DungeonSpawnerEntity blockEntity) {
        blockEntity.logic.clientTick(world, pos);
    }

    public static void serverTick(Level world, BlockPos pos, BlockState state, DungeonSpawnerEntity blockEntity) {
        blockEntity.logic.serverTick((ServerLevel) world, pos);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(Provider registryLookup) {
        CompoundTag nbtCompound = this.saveWithoutMetadata(registryLookup);
        nbtCompound.remove("SpawnPotentials");
        return nbtCompound;
    }

    @Override
    public boolean triggerEvent(int type, int data) {
        if (this.logic.handleStatus(this.level, type)) {
            return true;
        }
        return super.triggerEvent(type, data);
    }

    public DungeonSpawnerLogic getLogic() {
        return this.logic;
    }

    @Override
    public void setEntityId(EntityType<?> entityType, RandomSource random) {
        this.logic.setEntityId(entityType);
    }

}
