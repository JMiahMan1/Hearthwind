package net.dungeonz.network;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import net.dungeonz.network.packet.*;
import org.jetbrains.annotations.Nullable;

import net.dungeonz.access.ClientPlayerAccess;
import net.dungeonz.access.InGameHudAccess;
import net.dungeonz.block.entity.DungeonGateEntity;
import net.dungeonz.block.entity.DungeonPortalEntity;
import net.dungeonz.block.screen.DungeonGateOpScreen;
import net.dungeonz.block.screen.DungeonPortalOpScreen;
import net.dungeonz.block.screen.DungeonPortalScreen;
import net.dungeonz.block.screen.DungeonPortalScreenHandler;
import net.dungeonz.init.SoundInit;
import net.dungeonz.item.screen.DungeonCompassScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.class_2338;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;

@Environment(EnvType.CLIENT)
public class DungeonClientPacket {

    @SuppressWarnings("resource")
    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(DungeonInfoPacket.PACKET_ID, (payload, context) -> {
            List<Integer> breakableBlockIdList = payload.breakableBlockIdList();
            List<Integer> placeableBlockIdList = payload.placeableBlockIdList();
            boolean allowElytra = payload.allowElytra();
            context.client().execute(() -> {
                ((ClientPlayerAccess) context.player()).setClientDungeonInfo(breakableBlockIdList, placeableBlockIdList, allowElytra);
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(DungeonSyncScreenPacket.PACKET_ID, (payload, context) -> {
            class_2338 dungeonPortalPos = payload.blockPos();
            String difficulty = payload.difficulty();

            context.client().execute(() -> {
                if (context.client().field_1687.method_8321(dungeonPortalPos) != null && context.client().field_1687.method_8321(dungeonPortalPos) instanceof DungeonPortalEntity dungeonPortalEntity) {
                    dungeonPortalEntity.setDifficulty(difficulty);

                    if (context.client().field_1755 instanceof DungeonPortalScreen dungeonPortalScreen) {
                        dungeonPortalScreen.difficultyButton.setText(class_2561.method_43471("dungeonz.difficulty." + difficulty));
                    }
                    if (context.client().field_1724.field_7512 instanceof DungeonPortalScreenHandler dungeonPortalScreenHandler) {
                        dungeonPortalScreenHandler.getDungeonPortalEntity().setDifficulty(difficulty);
                    }
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(DungeonOpScreenPacket.PACKET_ID, (payload, context) -> {
            class_2338 portalOrGatePos = payload.blockPos();
            String dungeonTypeOrBlockId = payload.blockIdOrDungeonType();
            String difficultyOrParticleId = payload.particleEffectOrDifficulty();
            String unlockItemId = payload.unlockItem();

            context.client().execute(() -> {
                if (context.client().field_1687.method_8321(portalOrGatePos) != null) {
                    if (context.client().field_1687.method_8321(portalOrGatePos) instanceof DungeonPortalEntity dungeonPortalEntity) {
                        dungeonPortalEntity.setDungeonType(dungeonTypeOrBlockId);
                        dungeonPortalEntity.setDifficulty(difficultyOrParticleId);
                        context.client().method_1507(new DungeonPortalOpScreen(portalOrGatePos));
                    } else if (context.client().field_1687.method_8321(portalOrGatePos) instanceof DungeonGateEntity dungeonGateEntity) {
                        dungeonGateEntity.setBlockId(class_2960.method_60654(dungeonTypeOrBlockId));
                        dungeonGateEntity.setParticleEffectId(difficultyOrParticleId);
                        dungeonGateEntity.setUnlockItemId(unlockItemId);
                        context.client().method_1507(new DungeonGateOpScreen(portalOrGatePos));
                    }
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(DungeonCompassScreenPacket.PACKET_ID, (payload, context) -> {
            String dungeonType = payload.dungeonType();
            List<String> dungeonIds = payload.dungeonIdList();
            context.client().execute(() -> {
                context.client().method_1507(new DungeonCompassScreen(dungeonType, dungeonIds));
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(DungeonSyncGatePacket.PACKET_ID, (payload, context) -> {
            Set<class_2338> dungeonGatesPosList = payload.dungeonGatesPosList();

            String blockId = payload.blockId();
            String particleId = payload.particleEffect();
            String unlockItemId = payload.unlockItem();

            context.client().execute(() -> {
                Iterator<class_2338> iterator = dungeonGatesPosList.iterator();
                while (iterator.hasNext()) {
                    class_2338 pos = iterator.next();
                    if (context.client().field_1687.method_8321(pos) != null && context.client().field_1687.method_8321(pos) instanceof DungeonGateEntity dungeonGateEntity) {
                        dungeonGateEntity.setBlockId(class_2960.method_60654(blockId));
                        dungeonGateEntity.setParticleEffectId(particleId);
                        dungeonGateEntity.setUnlockItemId(unlockItemId);
                    }
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(DungeonTeleportCountdownPacket.PACKET_ID, (payload, context) -> {
            int dungeonTeleportCountdown = payload.countdownTicks();
            context.client().execute(() -> {
                ((InGameHudAccess) context.client().field_1705).setDungeonCountdownTicks(dungeonTeleportCountdown);
                context.player().method_5783(SoundInit.DUNGEON_COUNTDOWN_EVENT, 1.0f, 1.0f);
            });
        });
    }

    public static void writeC2SChangeDifficultyPacket(class_310 client, class_2338 portalBlockPos) {
        ClientPlayNetworking.send(new DungeonDifficultyPacket(portalBlockPos));
    }

    public static void writeC2SChangePrivateGroupPacket(class_310 client, class_2338 portalBlockPos, boolean privateGroup) {
        ClientPlayNetworking.send(new DungeonGroupPacket(portalBlockPos, privateGroup));
    }

    public static void writeC2SDungeonTeleportPacket(class_310 client, class_2338 portalBlockPos, @Nullable UUID requiredMinGroupUuid) {
        ClientPlayNetworking.send(new DungeonTeleportPacket(portalBlockPos, requiredMinGroupUuid != null, requiredMinGroupUuid));
    }

    public static void writeC2SSetDungeonTypePacket(class_310 client, String dungeonType, String defaultDifficulty, class_2338 portalBlockPos) {
        ClientPlayNetworking.send(new DungeonTypePacket(portalBlockPos, dungeonType, defaultDifficulty));
    }

    public static void writeC2SSetGateBlockPacket(class_310 client, String blockId, String particleId, String unlockItemId, class_2338 portalBlockPos) {
        ClientPlayNetworking.send(new DungeonGatePacket(portalBlockPos, blockId, particleId, unlockItemId));
    }

    public static void writeC2SSetDungeonCompassPacket(class_310 client, String dungeonType) {
        ClientPlayNetworking.send(new DungeonCompassPacket(dungeonType));
    }
}
