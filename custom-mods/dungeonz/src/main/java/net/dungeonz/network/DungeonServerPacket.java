package net.dungeonz.network;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import net.dungeonz.block.DungeonPortalBlock;
import org.jetbrains.annotations.Nullable;

import net.dungeonz.DungeonzMain;
import net.dungeonz.block.entity.DungeonGateEntity;
import net.dungeonz.block.entity.DungeonPortalEntity;
import net.dungeonz.dungeon.Dungeon;
import net.dungeonz.init.ItemInit;
import net.dungeonz.item.DungeonCompassItem;
import net.dungeonz.network.packet.*;
import net.dungeonz.util.DungeonHelper;
import net.dungeonz.util.InventoryHelper;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.class_2338;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.class_7923;

public class DungeonServerPacket {

    public static void init() {
        PayloadTypeRegistry.playS2C().register(DungeonInfoPacket.PACKET_ID, DungeonInfoPacket.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(DungeonTeleportCountdownPacket.PACKET_ID, DungeonTeleportCountdownPacket.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(DungeonSyncGatePacket.PACKET_ID, DungeonSyncGatePacket.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(DungeonSyncScreenPacket.PACKET_ID, DungeonSyncScreenPacket.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(DungeonOpScreenPacket.PACKET_ID, DungeonOpScreenPacket.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(DungeonCompassScreenPacket.PACKET_ID, DungeonCompassScreenPacket.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(DungeonPortalPacket.PACKET_ID, DungeonPortalPacket.PACKET_CODEC);

        PayloadTypeRegistry.playC2S().register(DungeonDifficultyPacket.PACKET_ID, DungeonDifficultyPacket.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(DungeonGroupPacket.PACKET_ID, DungeonGroupPacket.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(DungeonTeleportPacket.PACKET_ID, DungeonTeleportPacket.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(DungeonTypePacket.PACKET_ID, DungeonTypePacket.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(DungeonCompassPacket.PACKET_ID, DungeonCompassPacket.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(DungeonGatePacket.PACKET_ID, DungeonGatePacket.PACKET_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(DungeonDifficultyPacket.PACKET_ID, (payload, context) -> {
            class_2338 dungeonPortalPos = payload.portalBlockPos();
            context.server().execute(() -> {
                if (context.player().method_37908().method_8321(dungeonPortalPos) != null && context.player().method_37908().method_8321(dungeonPortalPos) instanceof DungeonPortalEntity dungeonPortalEntity) {

                    if (dungeonPortalEntity.getDungeonPlayerCount() == 0) {
                        List<String> difficulties = dungeonPortalEntity.getDungeon().getDifficultyList();
                        if (dungeonPortalEntity.getDifficulty().equals("")) {
                            dungeonPortalEntity.setDifficulty(difficulties.get(0));
                        } else {
                            int index = difficulties.indexOf(dungeonPortalEntity.getDifficulty()) + 1;
                            if (index >= difficulties.size()) {
                                index = 0;
                            }
                            dungeonPortalEntity.setDifficulty(difficulties.get(index));
                        }
                        dungeonPortalEntity.method_5431();
                        writeS2CSyncScreenPacket(context.player(), dungeonPortalEntity);
                    }
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(DungeonTeleportPacket.PACKET_ID, (payload, context) -> {
            class_2338 dungeonPortalPos = payload.dungeonPortalPos();
            // Boolean isMinGroupRequired = payload.isMinGroupRequired();
            UUID uuid = payload.uuid();
            context.server().execute(() -> {
                DungeonHelper.teleportDungeon(context.player(), dungeonPortalPos, uuid);
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(DungeonGroupPacket.PACKET_ID, (payload, context) -> {
            class_2338 dungeonPortalPos = payload.portalBlockPos();
            boolean privateGroup = payload.privateGroup();
            context.server().execute(() -> {
                if (context.player().method_37908().method_8321(dungeonPortalPos) != null
                        && context.player().method_37908().method_8321(dungeonPortalPos) instanceof DungeonPortalEntity dungeonPortalEntity) {

                    if (dungeonPortalEntity.getDungeonPlayerCount() == 0) {
                        dungeonPortalEntity.setPrivateGroup(privateGroup);
                        dungeonPortalEntity.method_5431();
                    }
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(DungeonTypePacket.PACKET_ID, (payload, context) -> {
            class_2338 dungeonPortalPos = payload.portalBlockPos();
            String dungeonType = payload.dungeonType();
            String defaultDifficulty = payload.defaultDifficulty();
            context.server().execute(() -> {
                if (context.player().method_7338()) {
                    if (Dungeon.getDungeon(dungeonType) != null) {
                        Dungeon dungeon = Dungeon.getDungeon(dungeonType);
                        if (dungeon.getDifficultyList().contains(defaultDifficulty)) {
                            class_2338 pos = dungeonPortalPos;
                            if (DungeonPortalBlock.isOtherDungeonPortalBlockNearby(context.player().method_37908(), dungeonPortalPos)) {
                                pos = DungeonPortalBlock.getMainDungeonPortalBlockPos(context.player().method_37908(), pos);
                            }
                            if (context.player().method_37908().method_8321(pos) != null
                                    && context.player().method_37908().method_8321(pos) instanceof DungeonPortalEntity dungeonPortalEntity) {
                                dungeonPortalEntity.setDungeonType(dungeonType);
                                dungeonPortalEntity.setDifficulty(defaultDifficulty);
                                dungeonPortalEntity.setMaxGroupSize(dungeon.getMaxGroupSize());
                                dungeonPortalEntity.setMinGroupSize(dungeon.getMinGroupSize());
                                dungeonPortalEntity.method_5431();
                                context.player().method_7353(class_2561.method_30163("Set dungeon type successfully!"), false);
                                return;
                            }
                        } else {
                            context.player().method_7353(class_2561.method_30163("Failed to set dungeon type cause difficulty " + defaultDifficulty + " does not exist in type " + dungeonType + "!"), false);
                        }
                    } else {
                        context.player().method_7353(class_2561.method_30163("Failed to set dungeon type cause " + dungeonType + " does not exist!"), false);
                    }
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(DungeonGatePacket.PACKET_ID, (payload, context) -> {
            class_2338 gatePos = payload.portalBlockPos();
            String blockId = payload.blockId();
            String particleId = payload.particleId();
            String unlockItemId = payload.unlockItemId();
            context.server().execute(() -> {
                if (context.player().method_7338()) {
                    if (context.player().method_37908().method_8321(gatePos) != null && context.player().method_37908().method_8321(gatePos) instanceof DungeonGateEntity) {
                        List<class_2338> otherDungeonGatesPosList = DungeonGateEntity.getConnectedDungeonGatePosList(context.player().method_37908(), gatePos);
                        for (int i = 0; i < otherDungeonGatesPosList.size(); i++) {
                            if (context.player().method_37908().method_8321(otherDungeonGatesPosList.get(i)) != null
                                    && context.player().method_37908().method_8321(otherDungeonGatesPosList.get(i)) instanceof DungeonGateEntity otherDungeonGateEntity) {
                                otherDungeonGateEntity.setBlockId(class_2960.method_60654(blockId));
                                otherDungeonGateEntity.setParticleEffectId(particleId);
                                otherDungeonGateEntity.setUnlockItemId(unlockItemId);
                                otherDungeonGateEntity.method_5431();
                            }
                        }
                        writeS2CSyncGatePacket(context.player(), (DungeonGateEntity) context.player().method_37908().method_8321(gatePos), otherDungeonGatesPosList);
                    }
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(DungeonCompassPacket.PACKET_ID, (payload, context) -> {
            String dungeonType = payload.dungeonType();
            context.server().execute(() -> {
                if (context.player().method_6047().method_31574(ItemInit.DUNGEON_COMPASS)
                        && InventoryHelper.hasRequiredItemStacks(context.player().method_31548(), ItemInit.REQUIRED_DUNGEON_COMPASS_CALIBRATION_ITEMS)) {
                    InventoryHelper.decrementRequiredItemStacks(context.player().method_31548(), ItemInit.REQUIRED_DUNGEON_COMPASS_CALIBRATION_ITEMS);
                    DungeonCompassItem.setCompassDungeonStructure((class_3218) context.player().method_37908(), context.player().method_24515(), context.player().method_6047(), dungeonType);
                }
            });
        });
    }

    public static void writeS2CDungeonInfoPacket(class_3222 serverPlayerEntity, List<Integer> breakableBlockIdList, List<Integer> placeableBlockIdList, boolean allowElytra) {
        ServerPlayNetworking.send(serverPlayerEntity, new DungeonInfoPacket(breakableBlockIdList, placeableBlockIdList, allowElytra));
    }

    public static void writeS2CSyncScreenPacket(class_3222 serverPlayerEntity, DungeonPortalEntity dungeonPortalEntity) {
        ServerPlayNetworking.send(serverPlayerEntity, new DungeonSyncScreenPacket(dungeonPortalEntity.method_11016(), dungeonPortalEntity.getDifficulty()));
    }

    public static void writeS2COpenOpScreenPacket(class_3222 serverPlayerEntity, @Nullable DungeonPortalEntity dungeonPortalEntity, @Nullable DungeonGateEntity dungeonGateEntity) {
        class_2338 blockPos = null;
        String blockIdOrDungeonType = "";
        String particleEffectOrDifficulty = "";
        String unlockItem = "";
        if (dungeonPortalEntity != null) {
            blockPos = dungeonPortalEntity.method_11016();
            blockIdOrDungeonType = dungeonPortalEntity.getDungeonType();
            particleEffectOrDifficulty = dungeonPortalEntity.getDifficulty();
        }
        if (dungeonGateEntity != null) {
            blockPos = dungeonGateEntity.method_11016();
            blockIdOrDungeonType = class_7923.field_41175.method_10221(dungeonGateEntity.getBlockState().method_26204()).toString();
            particleEffectOrDifficulty = dungeonGateEntity.getParticleEffect() != null ? class_7923.field_41180.method_10221(dungeonGateEntity.getParticleEffect().method_10295()).toString() : "";
            unlockItem = dungeonGateEntity.getUnlockItem() != null ? class_7923.field_41178.method_10221(dungeonGateEntity.getUnlockItem()).toString() : "";
        }

        ServerPlayNetworking.send(serverPlayerEntity, new DungeonOpScreenPacket(blockPos, blockIdOrDungeonType, particleEffectOrDifficulty, unlockItem));
    }

    public static void writeS2COpenCompassScreenPacket(class_3222 serverPlayerEntity, String dungeonType) {
        List<String> dungeonIdList = new ArrayList<String>();
        for (int i = 0; i < DungeonzMain.DUNGEONS.size(); i++) {
            dungeonIdList.add(DungeonzMain.DUNGEONS.get(i).getDungeonTypeId());
        }
        ServerPlayNetworking.send(serverPlayerEntity, new DungeonCompassScreenPacket(dungeonType, dungeonIdList));
    }

    public static void writeS2CSyncGatePacket(class_3222 serverPlayerEntity, DungeonGateEntity dungeonGateEntity, List<class_2338> dungeonGatesPosList) {
        ServerPlayNetworking.send(serverPlayerEntity,
                new DungeonSyncGatePacket(new HashSet<class_2338>(dungeonGatesPosList), class_7923.field_41175.method_10221(dungeonGateEntity.getBlockState().method_26204()).toString(),
                        dungeonGateEntity.getParticleEffect() != null ? class_7923.field_41180.method_10221(dungeonGateEntity.getParticleEffect().method_10295()).toString() : "",
                        dungeonGateEntity.getUnlockItem() != null ? class_7923.field_41178.method_10221(dungeonGateEntity.getUnlockItem()).toString() : ""));
    }

    public static void writeS2CDungeonTeleportCountdown(class_3222 serverPlayerEntity, int countdownTicks) {
        ServerPlayNetworking.send(serverPlayerEntity, new DungeonTeleportCountdownPacket(countdownTicks));
    }

}
