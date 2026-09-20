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
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;

@Environment(EnvType.CLIENT)
public class DungeonClientPacket {

    @SuppressWarnings("resource")
    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(DungeonAdmissionPacket.PACKET_ID, (payload, context) -> {
            context.client().execute(() -> {
                if (context.player().containerMenu instanceof DungeonPortalScreenHandler menu && menu.containerId == payload.containerId()) {
                    menu.setAdmission(payload);
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(DungeonInfoPacket.PACKET_ID, (payload, context) -> {
            List<Integer> breakableBlockIdList = payload.breakableBlockIdList();
            List<Integer> placeableBlockIdList = payload.placeableBlockIdList();
            boolean allowElytra = payload.allowElytra();
            context.client().execute(() -> {
                ((ClientPlayerAccess) context.player()).setClientDungeonInfo(breakableBlockIdList, placeableBlockIdList, allowElytra);
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(DungeonSyncScreenPacket.PACKET_ID, (payload, context) -> {
            BlockPos dungeonPortalPos = payload.blockPos();
            String difficulty = payload.difficulty();

            context.client().execute(() -> {
                if (context.client().level.getBlockEntity(dungeonPortalPos) != null && context.client().level.getBlockEntity(dungeonPortalPos) instanceof DungeonPortalEntity dungeonPortalEntity) {
                    dungeonPortalEntity.setDifficulty(difficulty);

                    if (context.client().gui.screen() instanceof DungeonPortalScreen dungeonPortalScreen) {
                        dungeonPortalScreen.difficultyButton.setText(Component.translatable("dungeonz.difficulty." + difficulty));
                    }
                    if (context.client().player.containerMenu instanceof DungeonPortalScreenHandler dungeonPortalScreenHandler) {
                        dungeonPortalScreenHandler.getDungeonPortalEntity().setDifficulty(difficulty);
                    }
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(DungeonOpScreenPacket.PACKET_ID, (payload, context) -> {
            BlockPos portalOrGatePos = payload.blockPos();
            String dungeonTypeOrBlockId = payload.blockIdOrDungeonType();
            String difficultyOrParticleId = payload.particleEffectOrDifficulty();
            String unlockItemId = payload.unlockItem();

            context.client().execute(() -> {
                if (context.client().level.getBlockEntity(portalOrGatePos) != null) {
                    if (context.client().level.getBlockEntity(portalOrGatePos) instanceof DungeonPortalEntity dungeonPortalEntity) {
                        dungeonPortalEntity.setDungeonType(dungeonTypeOrBlockId);
                        dungeonPortalEntity.setDifficulty(difficultyOrParticleId);
                        context.client().gui.setScreen(new DungeonPortalOpScreen(portalOrGatePos));
                    } else if (context.client().level.getBlockEntity(portalOrGatePos) instanceof DungeonGateEntity dungeonGateEntity) {
                        dungeonGateEntity.setBlockId(Identifier.parse(dungeonTypeOrBlockId));
                        dungeonGateEntity.setParticleEffectId(difficultyOrParticleId);
                        dungeonGateEntity.setUnlockItemId(unlockItemId);
                        context.client().gui.setScreen(new DungeonGateOpScreen(portalOrGatePos));
                    }
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(DungeonCompassScreenPacket.PACKET_ID, (payload, context) -> {
            String dungeonType = payload.dungeonType();
            List<String> dungeonIds = payload.dungeonIdList();
            context.client().execute(() -> {
                context.client().gui.setScreen(new DungeonCompassScreen(dungeonType, dungeonIds));
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(DungeonSyncGatePacket.PACKET_ID, (payload, context) -> {
            Set<BlockPos> dungeonGatesPosList = payload.dungeonGatesPosList();

            String blockId = payload.blockId();
            String particleId = payload.particleEffect();
            String unlockItemId = payload.unlockItem();

            context.client().execute(() -> {
                Iterator<BlockPos> iterator = dungeonGatesPosList.iterator();
                while (iterator.hasNext()) {
                    BlockPos pos = iterator.next();
                    if (context.client().level.getBlockEntity(pos) != null && context.client().level.getBlockEntity(pos) instanceof DungeonGateEntity dungeonGateEntity) {
                        dungeonGateEntity.setBlockId(Identifier.parse(blockId));
                        dungeonGateEntity.setParticleEffectId(particleId);
                        dungeonGateEntity.setUnlockItemId(unlockItemId);
                    }
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(DungeonTeleportCountdownPacket.PACKET_ID, (payload, context) -> {
            int dungeonTeleportCountdown = payload.countdownTicks();
            context.client().execute(() -> {
                ((InGameHudAccess) context.client().gui).setDungeonCountdownTicks(dungeonTeleportCountdown);
                context.player().playSound(SoundInit.DUNGEON_COUNTDOWN_EVENT, 1.0f, 1.0f);
            });
        });
    }

    public static void writeC2SChangeDifficultyPacket(Minecraft client, BlockPos portalBlockPos) {
        ClientPlayNetworking.send(new DungeonDifficultyPacket(portalBlockPos));
    }

    public static void writeC2SChangePrivateGroupPacket(Minecraft client, BlockPos portalBlockPos, boolean privateGroup) {
        ClientPlayNetworking.send(new DungeonGroupPacket(portalBlockPos, privateGroup));
    }

    public static void writeC2SDungeonTeleportPacket(Minecraft client, BlockPos portalBlockPos, @Nullable UUID requiredMinGroupUuid) {
        ClientPlayNetworking.send(new DungeonTeleportPacket(portalBlockPos, requiredMinGroupUuid != null, requiredMinGroupUuid));
    }

    public static void writeC2SSetDungeonTypePacket(Minecraft client, String dungeonType, String defaultDifficulty, BlockPos portalBlockPos) {
        ClientPlayNetworking.send(new DungeonTypePacket(portalBlockPos, dungeonType, defaultDifficulty));
    }

    public static void writeC2SSetGateBlockPacket(Minecraft client, String blockId, String particleId, String unlockItemId, BlockPos portalBlockPos) {
        ClientPlayNetworking.send(new DungeonGatePacket(portalBlockPos, blockId, particleId, unlockItemId));
    }

    public static void writeC2SSetDungeonCompassPacket(Minecraft client, String dungeonType) {
        ClientPlayNetworking.send(new DungeonCompassPacket(dungeonType));
    }
}
