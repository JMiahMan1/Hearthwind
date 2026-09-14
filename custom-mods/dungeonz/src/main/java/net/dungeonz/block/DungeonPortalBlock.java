package net.dungeonz.block;

import java.util.Iterator;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import net.dungeonz.DungeonzMain;
import net.dungeonz.block.entity.DungeonPortalEntity;
import net.dungeonz.init.BlockInit;
import net.dungeonz.network.DungeonServerPacket;
import net.dungeonz.util.DungeonHelper;
import net.minecraft.class_1269;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1922;
import net.minecraft.class_1936;
import net.minecraft.class_1937;
import net.minecraft.class_2237;
import net.minecraft.class_2338;
import net.minecraft.class_2402;
import net.minecraft.class_2464;
import net.minecraft.class_2586;
import net.minecraft.class_2591;
import net.minecraft.class_2680;
import net.minecraft.class_3222;
import net.minecraft.class_3610;
import net.minecraft.class_3611;
import net.minecraft.class_3965;
import net.minecraft.class_5558;
import net.partyaddon.access.GroupManagerAccess;
import net.partyaddon.network.PartyAddonServerPacket;

public class DungeonPortalBlock extends class_2237 implements class_2402 {

    public static final MapCodec<DungeonPortalBlock> field_46280 = DungeonPortalBlock.method_54094(DungeonPortalBlock::new);

    public DungeonPortalBlock(class_2251 settings) {
        super(settings);
    }

    @Override
    public class_2586 method_10123(class_2338 pos, class_2680 state) {
        return new DungeonPortalEntity(pos, state);
    }

    @Override
    public class_2464 method_9604(class_2680 state) {
        return class_2464.field_11456;
    }

    @Override
    public class_1269 method_55766(class_2680 state, class_1937 world, class_2338 pos, class_1657 player, class_3965 hit) {
        if (player.method_37908().method_8321(pos) != null && player.method_37908().method_8321(pos) instanceof DungeonPortalEntity dungeonPortalEntity) {
            if (isOtherDungeonPortalBlockNearby(world, pos)) {
                dungeonPortalEntity = getMainDungeonPortalEntity(world, pos);
                pos = getMainDungeonPortalBlockPos(world, pos);
            }
            if (player.method_7338() && (dungeonPortalEntity.getDungeon() == null || player.method_5715())) {
                if (!world.method_8608()) {
                    DungeonServerPacket.writeS2COpenOpScreenPacket((class_3222) player, dungeonPortalEntity, null);
                }
                return class_1269.method_29236(world.method_8608());
            } else if (dungeonPortalEntity.getDungeon() != null) {
                if (!world.method_8608()) {
                    if (DungeonzMain.isPartyAddonLoaded) {
                        PartyAddonServerPacket.writeS2CSyncGroupManagerPacket((class_3222) player, ((GroupManagerAccess) player).getGroupManager());
                    }
                    player.method_17355(state.method_26196(world, pos));
                }
                return class_1269.method_29236(world.method_8608());
            }
        }
        return super.method_55766(state, world, pos, player, hit);
    }

    @Override
    public void method_9548(class_2680 state, class_1937 world, class_2338 pos, class_1297 entity) {
        if (!world.method_8608() && !entity.method_5765() && !entity.method_5782() && entity.method_5822(false) && entity instanceof class_3222) {
            if (!entity.method_30230()) {
                if (isOtherDungeonPortalBlockNearby(world, pos)) {
                    pos = getMainDungeonPortalBlockPos(world, pos);
                }
                DungeonHelper.teleportDungeon((class_3222) entity, pos, entity.method_5667());
                entity.method_30229();
            }
        }
    }

    @Override
    protected boolean method_22358(class_2680 state, class_3611 fluid) {
        return false;
    }

    @Override
    @Nullable
    public <T extends class_2586> class_5558<T> method_31645(class_1937 world, class_2680 state, class_2591<T> type) {
        return DungeonGateBlock.method_31618(type, BlockInit.DUNGEON_PORTAL_ENTITY, world.method_8608() ? DungeonPortalEntity::clientTick : DungeonPortalEntity::serverTick);
    }

    public static boolean isOtherDungeonPortalBlockNearby(class_1937 world, class_2338 pos) {
        for (class_2338 checkPos : class_2338.method_25996(pos, 1, 1, 1)) {
            if (checkPos.equals(pos)) {
                continue;
            }
            if (world.method_8320(checkPos).method_27852(BlockInit.DUNGEON_PORTAL)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public static class_2338 getMainDungeonPortalBlockPos(class_1937 world, class_2338 pos) {
        class_2338 checkPos = new class_2338(pos);
        for (int i = 1; i < 30; i++) {
            if (world.method_8320(checkPos.method_10089(1)).method_27852(BlockInit.DUNGEON_PORTAL)) {
                checkPos = checkPos.method_10089(1);
            } else {
                break;
            }
        }
        for (int i = 1; i < 30; i++) {
            if (world.method_8320(checkPos.method_10077(1)).method_27852(BlockInit.DUNGEON_PORTAL)) {
                checkPos = checkPos.method_10077(1);
            } else {
                break;
            }
        }
        for (int i = 1; i < 30; i++) {
            if (world.method_8320(checkPos.method_10087(1)).method_27852(BlockInit.DUNGEON_PORTAL)) {
                checkPos = checkPos.method_10087(1);
            } else {
                break;
            }
        }
        return world.method_8321(checkPos) instanceof DungeonPortalEntity dungeonPortalEntity ? dungeonPortalEntity.method_11016() : null;
    }

    @Nullable
    public static DungeonPortalEntity getMainDungeonPortalEntity(class_1937 world, class_2338 pos) {
        if (getMainDungeonPortalBlockPos(world, pos) != null) {
            return (DungeonPortalEntity) world.method_8321(getMainDungeonPortalBlockPos(world, pos));
        }
        return null;
    }

    @Override
    protected MapCodec<? extends class_2237> method_53969() {
        return field_46280;
    }

    // Used for not getting removed by water
    @Override
    public boolean method_10310(@Nullable class_1657 player, class_1922 world, class_2338 pos, class_2680 state, class_3611 fluid) {
        return false;
    }

    @Override
    public boolean method_10311(class_1936 world, class_2338 pos, class_2680 state, class_3610 fluidState) {
        return false;
    }
}
