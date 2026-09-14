package net.dungeonz.block;

import net.dungeonz.block.entity.DungeonSpawnerEntity;
import net.dungeonz.init.BlockInit;
import net.minecraft.class_1799;
import net.minecraft.class_1937;
import net.minecraft.class_2237;
import net.minecraft.class_2338;
import net.minecraft.class_2464;
import net.minecraft.class_2586;
import net.minecraft.class_2591;
import net.minecraft.class_2680;
import net.minecraft.class_3218;
import net.minecraft.class_4970;
import net.minecraft.class_5558;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

public class DungeonSpawnerBlock extends class_2237 {

    public static final MapCodec<DungeonSpawnerBlock> CODEC = DungeonSpawnerBlock.method_54094(DungeonSpawnerBlock::new);

    public DungeonSpawnerBlock(class_4970.class_2251 settings) {
        super(settings);
    }

    @Override
    public class_2586 method_10123(class_2338 pos, class_2680 state) {
        return new DungeonSpawnerEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends class_2586> class_5558<T> method_31645(class_1937 world, class_2680 state, class_2591<T> type) {
        return DungeonSpawnerBlock.method_31618(type, BlockInit.DUNGEON_SPAWNER_ENTITY, world.field_9236 ? DungeonSpawnerEntity::clientTick : DungeonSpawnerEntity::serverTick);
    }

    @Override
    public void method_9565(class_2680 state, class_3218 world, class_2338 pos, class_1799 stack, boolean dropExperience) {
        super.method_9565(state, world, pos, stack, dropExperience);
        if (dropExperience) {
            int i = 15 + world.field_9229.method_43048(15) + world.field_9229.method_43048(15);
            this.method_9583(world, pos, i);
        }
    }

    @Override
    public class_2464 method_9604(class_2680 state) {
        return class_2464.field_11458;
    }

    @Override
    protected MapCodec<? extends class_2237> method_53969() {
        return field_46280;
    }
}
