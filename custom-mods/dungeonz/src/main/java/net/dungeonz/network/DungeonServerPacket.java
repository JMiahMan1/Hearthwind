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
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.registries.BuiltInRegistries;

public class DungeonServerPacket {

    public static void init() {
        PayloadTypeRegistry.clientboundPlay().register(DungeonAdmissionPacket.PACKET_ID, DungeonAdmissionPacket.PACKET_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(DungeonInfoPacket.PACKET_ID, DungeonInfoPacket.PACKET_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(DungeonTeleportCountdownPacket.PACKET_ID, DungeonTeleportCountdownPacket.PACKET_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(DungeonSyncGatePacket.PACKET_ID, DungeonSyncGatePacket.PACKET_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(DungeonSyncScreenPacket.PACKET_ID, DungeonSyncScreenPacket.PACKET_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(DungeonOpScreenPacket.PACKET_ID, DungeonOpScreenPacket.PACKET_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(DungeonCompassScreenPacket.PACKET_ID, DungeonCompassScreenPacket.PACKET_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(DungeonPortalPacket.PACKET_ID, DungeonPortalPacket.PACKET_CODEC);

        PayloadTypeRegistry.serverboundPlay().register(DungeonDifficultyPacket.PACKET_ID, DungeonDifficultyPacket.PACKET_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(DungeonGroupPacket.PACKET_ID, DungeonGroupPacket.PACKET_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(DungeonTeleportPacket.PACKET_ID, DungeonTeleportPacket.PACKET_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(DungeonTypePacket.PACKET_ID, DungeonTypePacket.PACKET_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(DungeonCompassPacket.PACKET_ID, DungeonCompassPacket.PACKET_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(DungeonGatePacket.PACKET_ID, DungeonGatePacket.PACKET_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(DungeonDifficultyPacket.PACKET_ID, (payload, context) -> {
            BlockPos dungeonPortalPos = payload.portalBlockPos();
            context.server().execute(() -> {
                if (context.player().level().getBlockEntity(dungeonPortalPos) != null && context.player().level().getBlockEntity(dungeonPortalPos) instanceof DungeonPortalEntity dungeonPortalEntity) {

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
                        dungeonPortalEntity.setChanged();
                        writeS2CSyncScreenPacket(context.player(), dungeonPortalEntity);
                    }
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(DungeonTeleportPacket.PACKET_ID, (payload, context) -> {
            BlockPos dungeonPortalPos = payload.dungeonPortalPos();
            // Boolean isMinGroupRequired = payload.isMinGroupRequired();
            UUID uuid = payload.uuid();
            context.server().execute(() -> {
                DungeonHelper.teleportDungeon(context.player(), dungeonPortalPos, uuid);
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(DungeonGroupPacket.PACKET_ID, (payload, context) -> {
            BlockPos dungeonPortalPos = payload.portalBlockPos();
            boolean privateGroup = payload.privateGroup();
            context.server().execute(() -> {
                if (context.player().level().getBlockEntity(dungeonPortalPos) != null
                        && context.player().level().getBlockEntity(dungeonPortalPos) instanceof DungeonPortalEntity dungeonPortalEntity) {

                    if (dungeonPortalEntity.getDungeonPlayerCount() == 0) {
                        dungeonPortalEntity.setPrivateGroup(privateGroup);
                        dungeonPortalEntity.setChanged();
                    }
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(DungeonTypePacket.PACKET_ID, (payload, context) -> {
            BlockPos dungeonPortalPos = payload.portalBlockPos();
            String dungeonType = payload.dungeonType();
            String defaultDifficulty = payload.defaultDifficulty();
            context.server().execute(() -> {
                if (context.player().canUseGameMasterBlocks()) {
                    if (Dungeon.getDungeon(dungeonType) != null) {
                        Dungeon dungeon = Dungeon.getDungeon(dungeonType);
                        if (dungeon.getDifficultyList().contains(defaultDifficulty)) {
                            BlockPos pos = dungeonPortalPos;
                            if (DungeonPortalBlock.isOtherDungeonPortalBlockNearby(context.player().level(), dungeonPortalPos)) {
                                pos = DungeonPortalBlock.getMainDungeonPortalBlockPos(context.player().level(), pos);
                            }
                            if (context.player().level().getBlockEntity(pos) != null
                                    && context.player().level().getBlockEntity(pos) instanceof DungeonPortalEntity dungeonPortalEntity) {
                                dungeonPortalEntity.setDungeonType(dungeonType);
                                dungeonPortalEntity.setDifficulty(defaultDifficulty);
                                dungeonPortalEntity.setMaxGroupSize(dungeon.getMaxGroupSize());
                                dungeonPortalEntity.setMinGroupSize(dungeon.getMinGroupSize());
                                dungeonPortalEntity.setChanged();
                                context.player().sendSystemMessage(Component.nullToEmpty("Set dungeon type successfully!"));
                                return;
                            }
                        } else {
                            context.player().sendSystemMessage(Component.nullToEmpty("Failed to set dungeon type cause difficulty " + defaultDifficulty + " does not exist in type " + dungeonType + "!"));
                        }
                    } else {
                        context.player().sendSystemMessage(Component.nullToEmpty("Failed to set dungeon type cause " + dungeonType + " does not exist!"));
                    }
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(DungeonGatePacket.PACKET_ID, (payload, context) -> {
            BlockPos gatePos = payload.portalBlockPos();
            String blockId = payload.blockId();
            String particleId = payload.particleId();
            String unlockItemId = payload.unlockItemId();
            context.server().execute(() -> {
                if (context.player().canUseGameMasterBlocks()) {
                    if (context.player().level().getBlockEntity(gatePos) != null && context.player().level().getBlockEntity(gatePos) instanceof DungeonGateEntity) {
                        List<BlockPos> otherDungeonGatesPosList = DungeonGateEntity.getConnectedDungeonGatePosList(context.player().level(), gatePos);
                        for (int i = 0; i < otherDungeonGatesPosList.size(); i++) {
                            if (context.player().level().getBlockEntity(otherDungeonGatesPosList.get(i)) != null
                                    && context.player().level().getBlockEntity(otherDungeonGatesPosList.get(i)) instanceof DungeonGateEntity otherDungeonGateEntity) {
                                otherDungeonGateEntity.setBlockId(Identifier.parse(blockId));
                                otherDungeonGateEntity.setParticleEffectId(particleId);
                                otherDungeonGateEntity.setUnlockItemId(unlockItemId);
                                otherDungeonGateEntity.setChanged();
                            }
                        }
                        writeS2CSyncGatePacket(context.player(), (DungeonGateEntity) context.player().level().getBlockEntity(gatePos), otherDungeonGatesPosList);
                    }
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(DungeonCompassPacket.PACKET_ID, (payload, context) -> {
            String dungeonType = payload.dungeonType();
            context.server().execute(() -> {
                if (context.player().getMainHandItem().is(ItemInit.DUNGEON_COMPASS)
                        && InventoryHelper.hasRequiredItemStacks(context.player().getInventory(), ItemInit.getRequiredDungeonCompassCalibrationItems())) {
                    InventoryHelper.decrementRequiredItemStacks(context.player().getInventory(), ItemInit.getRequiredDungeonCompassCalibrationItems());
                    DungeonCompassItem.setCompassDungeonStructure((ServerLevel) context.player().level(), context.player().blockPosition(), context.player().getMainHandItem(), dungeonType);
                }
            });
        });
    }

    public static void writeS2CDungeonInfoPacket(ServerPlayer serverPlayerEntity, List<Integer> breakableBlockIdList, List<Integer> placeableBlockIdList, boolean allowElytra) {
        ServerPlayNetworking.send(serverPlayerEntity, new DungeonInfoPacket(breakableBlockIdList, placeableBlockIdList, allowElytra));
    }

    public static void writeS2CSyncScreenPacket(ServerPlayer serverPlayerEntity, DungeonPortalEntity dungeonPortalEntity) {
        ServerPlayNetworking.send(serverPlayerEntity, new DungeonSyncScreenPacket(dungeonPortalEntity.getBlockPos(), dungeonPortalEntity.getDifficulty()));
    }

    public static void writeS2COpenOpScreenPacket(ServerPlayer serverPlayerEntity, @Nullable DungeonPortalEntity dungeonPortalEntity, @Nullable DungeonGateEntity dungeonGateEntity) {
        BlockPos blockPos = null;
        String blockIdOrDungeonType = "";
        String particleEffectOrDifficulty = "";
        String unlockItem = "";
        if (dungeonPortalEntity != null) {
            blockPos = dungeonPortalEntity.getBlockPos();
            blockIdOrDungeonType = dungeonPortalEntity.getDungeonType();
            particleEffectOrDifficulty = dungeonPortalEntity.getDifficulty();
        }
        if (dungeonGateEntity != null) {
            blockPos = dungeonGateEntity.getBlockPos();
            blockIdOrDungeonType = BuiltInRegistries.BLOCK.getKey(dungeonGateEntity.getDisguiseBlockState().getBlock()).toString();
            particleEffectOrDifficulty = dungeonGateEntity.getParticleEffect() != null ? BuiltInRegistries.PARTICLE_TYPE.getKey(dungeonGateEntity.getParticleEffect().getType()).toString() : "";
            unlockItem = dungeonGateEntity.getUnlockItem() != null ? BuiltInRegistries.ITEM.getKey(dungeonGateEntity.getUnlockItem()).toString() : "";
        }

        ServerPlayNetworking.send(serverPlayerEntity, new DungeonOpScreenPacket(blockPos, blockIdOrDungeonType, particleEffectOrDifficulty, unlockItem));
    }

    public static void writeS2COpenCompassScreenPacket(ServerPlayer serverPlayerEntity, String dungeonType) {
        List<String> dungeonIdList = new ArrayList<String>();
        for (int i = 0; i < DungeonzMain.DUNGEONS.size(); i++) {
            dungeonIdList.add(DungeonzMain.DUNGEONS.get(i).getDungeonTypeId());
        }
        ServerPlayNetworking.send(serverPlayerEntity, new DungeonCompassScreenPacket(dungeonType, dungeonIdList));
    }

    public static void writeS2CSyncGatePacket(ServerPlayer serverPlayerEntity, DungeonGateEntity dungeonGateEntity, List<BlockPos> dungeonGatesPosList) {
        ServerPlayNetworking.send(serverPlayerEntity,
                new DungeonSyncGatePacket(new HashSet<BlockPos>(dungeonGatesPosList), BuiltInRegistries.BLOCK.getKey(dungeonGateEntity.getDisguiseBlockState().getBlock()).toString(),
                        dungeonGateEntity.getParticleEffect() != null ? BuiltInRegistries.PARTICLE_TYPE.getKey(dungeonGateEntity.getParticleEffect().getType()).toString() : "",
                        dungeonGateEntity.getUnlockItem() != null ? BuiltInRegistries.ITEM.getKey(dungeonGateEntity.getUnlockItem()).toString() : ""));
    }

    public static void writeS2CDungeonTeleportCountdown(ServerPlayer serverPlayerEntity, int countdownTicks) {
        ServerPlayNetworking.send(serverPlayerEntity, new DungeonTeleportCountdownPacket(countdownTicks));
    }

}
