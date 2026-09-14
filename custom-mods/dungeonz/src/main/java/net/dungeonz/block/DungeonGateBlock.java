package net.dungeonz.block;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import net.dungeonz.block.entity.DungeonGateEntity;
import net.dungeonz.init.BlockInit;
import net.dungeonz.init.ConfigInit;
import net.dungeonz.network.DungeonServerPacket;
import net.minecraft.class_10;
import net.minecraft.class_1268;
import net.minecraft.class_1657;
import net.minecraft.class_1747;
import net.minecraft.class_1799;
import net.minecraft.class_1922;
import net.minecraft.class_1937;
import net.minecraft.class_2237;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2464;
import net.minecraft.class_2586;
import net.minecraft.class_259;
import net.minecraft.class_2591;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_2689;
import net.minecraft.class_2741;
import net.minecraft.class_2746;
import net.minecraft.class_3222;
import net.minecraft.class_3726;
import net.minecraft.class_3965;
import net.minecraft.class_5558;
import net.minecraft.class_5819;
import net.minecraft.class_5945;
import net.minecraft.class_6019;
import net.minecraft.class_7923;
import net.minecraft.class_9062;

public class DungeonGateBlock extends class_2237 {

    public static class_2746 ENABLED = class_2741.field_12515;
    public static final MapCodec<DungeonGateBlock> CODEC = DungeonGateBlock.method_54094(DungeonGateBlock::new);

    public DungeonGateBlock(class_2251 settings) {
        super(settings);
        this.method_9590(this.field_10647.method_11664().method_11657(ENABLED, true));
    }

    @Override
    public class_2586 method_10123(class_2338 pos, class_2680 state) {
        return new DungeonGateEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends class_2586> class_5558<T> method_31645(class_1937 world, class_2680 state, class_2591<T> type) {
        return DungeonGateBlock.method_31618(type, BlockInit.DUNGEON_GATE_ENTITY, world.method_8608() ? null : DungeonGateEntity::serverTick);
    }

    @Override
    protected class_9062 method_55765(class_1799 stack, class_2680 state, class_1937 world, class_2338 pos, class_1657 player, class_1268 hand, class_3965 hit) {
        if (player.method_37908().method_8321(pos) != null && player.method_37908().method_8321(pos) instanceof DungeonGateEntity) {
            DungeonGateEntity dungeonGateEntity = (DungeonGateEntity) player.method_37908().method_8321(pos);
            if (player.method_7338()) {
                if (!player.method_5998(hand).method_7960() && player.method_5998(hand).method_7909() instanceof class_1747) {
                    dungeonGateEntity.setBlockId(class_7923.field_41175.method_10221(((class_1747) player.method_5998(hand).method_7909()).method_7711()));
                    dungeonGateEntity.method_5431();
                } else if (player.method_5715()) {
                    if (!world.method_8608()) {
                        DungeonServerPacket.writeS2COpenOpScreenPacket((class_3222) player, null, dungeonGateEntity);
                    }
                }
                return class_9062.method_55644(world.method_8608());
            } else if (dungeonGateEntity.getUnlockItem() != null && player.method_5998(hand).method_31574(dungeonGateEntity.getUnlockItem())) {
                if (!world.method_8608()) {
                    if (!player.method_7337()) {
                        player.method_5998(hand).method_7934(1);
                    }
                    dungeonGateEntity.unlockGate(pos);
                }
                return class_9062.method_55644(world.method_8608());
            }

        }
        return super.method_55765(stack, state, world, pos, player, hand, hit);
    }

    @Override
    public class_2464 method_9604(class_2680 state) {
        return class_2464.field_11455;
    }

    @Override
    public void method_9515(class_2689.class_2690<class_2248, class_2680> builder) {
        builder.method_11667(ENABLED);
    }

    @Override
    public void method_9496(class_2680 state, class_1937 world, class_2338 pos, class_5819 random) {
        if (state.method_11654(ENABLED) && world.method_8321(pos) != null && world.method_8321(pos) instanceof DungeonGateEntity
                && ((DungeonGateEntity) world.method_8321(pos)).getParticleEffect() != null) {
            class_5945.method_34682(world, pos, ((DungeonGateEntity) world.method_8321(pos)).getParticleEffect(), class_6019.method_35017(0, 1));
        }
    }

    @Override
    public boolean method_9579(class_2680 state, class_1922 world, class_2338 pos) {
        if (!state.method_11654(ENABLED)) {
            return true;
        }
        return super.method_9579(state, world, pos);
    }

    @Override
    protected boolean method_9516(class_2680 state, class_10 type) {
        if (state.method_11654(ENABLED)) {
            return false;
        }
        return true;
    }

    @Override
    public class_265 method_9530(class_2680 state, class_1922 world, class_2338 pos, class_3726 context) {
        if (!state.method_11654(ENABLED) && !ConfigInit.CONFIG.devMode) {
            return class_259.method_1073();
        }
        return super.method_9530(state, world, pos, context);
    }

    @Override
    public class_265 method_9549(class_2680 state, class_1922 world, class_2338 pos, class_3726 context) {
        if (!state.method_11654(ENABLED)) {
            return class_259.method_1073();
        }
        return super.method_9549(state, world, pos, context);
    }

    @Override
    protected MapCodec<? extends class_2237> method_53969() {
        return field_46280;
    }

}
