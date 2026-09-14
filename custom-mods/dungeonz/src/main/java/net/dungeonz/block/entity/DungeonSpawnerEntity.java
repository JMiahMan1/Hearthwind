package net.dungeonz.block.entity;

import net.dungeonz.block.logic.DungeonSpawnerLogic;
import net.dungeonz.init.BlockInit;
import net.minecraft.class_1299;
import net.minecraft.class_1937;
import net.minecraft.class_1952;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2487;
import net.minecraft.class_2586;
import net.minecraft.class_2622;
import net.minecraft.class_2680;
import net.minecraft.class_3218;
import net.minecraft.class_5819;
import net.minecraft.class_7225;
import net.minecraft.class_7225.class_7874;
import net.minecraft.class_8959;
import org.jetbrains.annotations.Nullable;

public class DungeonSpawnerEntity extends class_2586 implements class_8959 {
    private final DungeonSpawnerLogic logic = new DungeonSpawnerLogic() {

        @Override
        public void sendStatus(class_1937 world, class_2338 pos, int status) {
            world.method_8427(pos, BlockInit.DUNGEON_SPAWNER, status, 0);
        }

        @Override
        public void setSpawnEntry(@Nullable class_1937 world, class_2338 pos, class_1952 spawnEntry) {
            super.setSpawnEntry(world, pos, spawnEntry);
            if (world != null) {
                class_2680 blockState = world.method_8320(pos);
                world.method_8413(pos, blockState, blockState, class_2248.field_31029);
            }
        }
    };

    public DungeonSpawnerEntity(class_2338 pos, class_2680 state) {
        super(BlockInit.DUNGEON_SPAWNER_ENTITY, pos, state);
    }

    @Override
    public void method_11014(class_2487 nbt, class_7225.class_7874 registryLookup) {
        super.method_11014(nbt, registryLookup);
        this.logic.readNbt(this.field_11863, this.field_11867, nbt);
    }

    @Override
    protected void method_11007(class_2487 nbt, class_7225.class_7874 registryLookup) {
        super.method_11007(nbt, registryLookup);
        this.logic.writeNbt(nbt);
    }

    public static void clientTick(class_1937 world, class_2338 pos, class_2680 state, DungeonSpawnerEntity blockEntity) {
        blockEntity.logic.clientTick(world, pos);
    }

    public static void serverTick(class_1937 world, class_2338 pos, class_2680 state, DungeonSpawnerEntity blockEntity) {
        blockEntity.logic.serverTick((class_3218) world, pos);
    }

    @Override
    public class_2622 method_38235() {
        return class_2622.method_38585(this);
    }

    @Override
    public class_2487 method_16887(class_7874 registryLookup) {
        class_2487 nbtCompound = this.method_38244(registryLookup);
        nbtCompound.method_10551("SpawnPotentials");
        return nbtCompound;
    }

    @Override
    public boolean method_11004(int type, int data) {
        if (this.logic.handleStatus(this.field_11863, type)) {
            return true;
        }
        return super.method_11004(type, data);
    }

    @Override
    public boolean method_11011() {
        return true;
    }

    public DungeonSpawnerLogic getLogic() {
        return this.logic;
    }

    @Override
    public void method_46408(class_1299<?> entityType, class_5819 random) {
        this.logic.setEntityId(entityType);
    }

}
