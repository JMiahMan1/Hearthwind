package net.dungeonz.util;

import net.dungeonz.DungeonzMain;
import net.dungeonz.access.ServerPlayerAccess;
import net.dungeonz.block.entity.DungeonPortalEntity;
import net.dungeonz.dungeon.Dungeon;
import net.dungeonz.dungeon.DungeonPlacementHandler;
import net.dungeonz.init.DimensionInit;
import net.dungeonz.network.DungeonServerPacket;
import net.levelz.access.LevelManagerAccess;
import net.levelz.level.LevelManager;
import net.minecraft.class_1263;
import net.minecraft.class_1277;
import net.minecraft.class_173;
import net.minecraft.class_1799;
import net.minecraft.class_181;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2586;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.class_52;
import net.minecraft.class_5321;
import net.minecraft.class_5454;
import net.minecraft.class_7923;
import net.minecraft.class_7924;
import net.minecraft.class_8567;
import net.minecraft.server.MinecraftServer;
import net.partyaddon.access.GroupManagerAccess;
import net.partyaddon.group.GroupManager;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.Map.Entry;

public class DungeonHelper {

    @Nullable
    public static Dungeon getCurrentDungeon(class_3222 playerEntity) {
        if (playerEntity != null && playerEntity.method_37908().method_27983() == DimensionInit.DUNGEON_WORLD && ((ServerPlayerAccess) playerEntity).getOldServerWorld() != null) {
            class_2586 blockEntity = ((ServerPlayerAccess) playerEntity).getOldServerWorld().method_8321(((ServerPlayerAccess) playerEntity).getDungeonPortalBlockPos());
            if (blockEntity == null) {
                return null;
            }
            if (blockEntity instanceof DungeonPortalEntity dungeonPortalEntity) {
                return dungeonPortalEntity.getDungeon();
            }
        }
        return null;
    }

    @Nullable
    public static DungeonPortalEntity getDungeonPortalEntity(class_3222 playerEntity) {
        if (((ServerPlayerAccess) playerEntity).getOldServerWorld() != null) {
            class_2586 blockEntity = ((ServerPlayerAccess) playerEntity).getOldServerWorld().method_8321(((ServerPlayerAccess) playerEntity).getDungeonPortalBlockPos());
            if (blockEntity == null) {
                return null;
            }
            if (blockEntity instanceof DungeonPortalEntity dungeonPortalEntity) {
                return dungeonPortalEntity;
            }
        }
        return null;
    }

    public static Map<String, List<class_1799>> getRequiredItemStackList(Dungeon dungeon) {
        Map<String, List<class_1799>> requiredItemStackList = new HashMap<>();
        for (Entry<String, HashMap<Integer, Integer>> entry : dungeon.getDifficultyRequiredItemCountMap().entrySet()) {
            List<class_1799> stacks = new ArrayList<>();
            for (Entry<Integer, Integer> itemIdEntry : entry.getValue().entrySet()) {
                stacks.add(new class_1799(class_7923.field_41178.method_10200(itemIdEntry.getKey()), itemIdEntry.getValue()));
            }
            requiredItemStackList.put(entry.getKey(), stacks);
        }
        return requiredItemStackList;
    }

    public static Map<String, List<class_1799>> getPossibleLootItemStackMap(Dungeon dungeon, MinecraftServer server) {
        HashMap<String, List<class_1799>> possibleLootItemStackMap = new HashMap<String, List<class_1799>>();
        for (Entry<String, String> entry : dungeon.getDifficultyBossLootTableMap().entrySet()) {
            class_52 lootTable = server.method_58576().method_58295(class_5321.method_29179(class_7924.field_50079, class_2960.method_60654(entry.getValue())));
            class_8567.class_8568 builder = new class_8567.class_8568(server.method_30002()).method_51874(class_181.field_24424,
                    server.method_3760().method_14571().get(server.method_30002().method_8409().method_43048(server.method_3760().method_14571().size())).method_19538());

            class_1263 inventory = new class_1277(27);
            lootTable.method_329(inventory, builder.method_51875(class_173.field_1179), server.method_30002().method_8409().method_43055());

            List<class_1799> itemStacks = new ArrayList<class_1799>();
            for (int i = 0; i < inventory.method_5439(); i++) {
                if (!inventory.method_5438(i).method_7960()) {
                    if (inventory.method_5438(i).method_7986()) {
                        inventory.method_5438(i).method_7974(0);
                    }
                    boolean contains = false;
                    for (class_1799 itemStack : itemStacks) {
                        if (class_1799.method_7984(itemStack, inventory.method_5438(i))) {
                            itemStack.method_7933(inventory.method_5438(i).method_7947());
                            contains = true;
                            break;
                        }
                    }
                    if (!contains) {
                        itemStacks.add(inventory.method_5438(i));
                    }
                }
            }
            possibleLootItemStackMap.put(entry.getKey(), itemStacks);
        }

        return possibleLootItemStackMap;
    }

    public static void teleportDungeon(class_3222 player, class_2338 dungeonPortalPos, @Nullable UUID requiredMinGroupUuid) {
        if (player.method_37908().method_8321(dungeonPortalPos) != null && player.method_37908().method_8321(dungeonPortalPos) instanceof DungeonPortalEntity dungeonPortalEntity) {

            if (player.method_37908().method_27983() == DimensionInit.DUNGEON_WORLD) {
                class_3218 oldWorld = ((ServerPlayerAccess) player).getOldServerWorld();
                if (oldWorld != null) {
                    player.method_5731(DungeonPlacementHandler.leave(player, oldWorld));
                    return;
                }
            } else {
                class_3218 dungeonWorld = player.method_37908().method_8503().method_3847(DimensionInit.DUNGEON_WORLD);
                if (dungeonWorld == null) {
                    player.method_7353(class_2561.method_43470("Failed to find world, was it registered?"), false);
                    return;
                }
                if (dungeonPortalEntity.getDungeon() != null) {
                    if ((dungeonPortalEntity.getDungeonPlayerCount() + dungeonPortalEntity.getDeadDungeonPlayerUUIDs().size()) < dungeonPortalEntity.getMaxGroupSize()) {

                        if (dungeonPortalEntity.isOnCooldown((int) dungeonWorld.method_8510())) {
                            player.method_7353(class_2561.method_43471("text.dungeonz.dungeon_cooldown"), false);
                            return;
                        }
                        if (dungeonPortalEntity.getDungeonPlayerCount() > 0 && dungeonPortalEntity.getPrivateGroup()) {
                            if (DungeonzMain.isPartyAddonLoaded) {
                                GroupManager groupManager = ((GroupManagerAccess) player).getGroupManager();
                                if (groupManager.getGroupPlayerIdList().isEmpty() || !groupManager.getGroupPlayerIdList().contains(dungeonPortalEntity.getDungeonPlayerUuids().get(0))) {
                                    player.method_7353(class_2561.method_43471("text.dungeonz.dungeon_private"), false);
                                    return;
                                }
                            } else {
                                player.method_7353(class_2561.method_43471("text.dungeonz.dungeon_private"), false);
                                return;
                            }
                        }
                        if (!dungeonPortalEntity.getWaitingUuids().isEmpty() && dungeonPortalEntity.getWaitingUuids().contains(player.method_5667())) {
                            player.method_7346();
                            return;
                        }
                        if (!player.method_7337()) {
                            if (DungeonHelper.getRequiredItemStackList(dungeonPortalEntity.getDungeon()).containsKey(dungeonPortalEntity.getDifficulty())) {
                                if (InventoryHelper.hasRequiredItemStacks(player.method_31548(), DungeonHelper.getRequiredItemStackList(dungeonPortalEntity.getDungeon()).get(dungeonPortalEntity.getDifficulty()))) {
                                    InventoryHelper.decrementRequiredItemStacks(player.method_31548(), DungeonHelper.getRequiredItemStackList(dungeonPortalEntity.getDungeon()).get(dungeonPortalEntity.getDifficulty()));
                                } else {
                                    player.method_7353(class_2561.method_43471("text.dungeonz.missing"), false);
                                    return;
                                }
                            }
                        }
                        if (DungeonzMain.isLevelZLoaded) {
                            LevelManager levelManager = ((LevelManagerAccess) player).getLevelManager();
                            if (levelManager.getOverallLevel() < dungeonPortalEntity.getDungeon().getRequiredLevel()) {
                                player.method_7353(class_2561.method_43469("text.dungeonz.required_level", dungeonPortalEntity.getDungeon().getRequiredLevel()), false);
                                return;
                            }
                        }
                        if (dungeonPortalEntity.getDungeonPlayerCount() <= 0 && requiredMinGroupUuid != null && dungeonPortalEntity.getMinGroupSize() > 1) {
                            dungeonPortalEntity.addWaitingUuid(requiredMinGroupUuid);
                            if (dungeonPortalEntity.getMinGroupSize() > dungeonPortalEntity.getWaitingUuids().size()) {
                                player.method_7353(class_2561.method_43469("text.dungeonz.dungeon_min_group_size", (dungeonPortalEntity.getMinGroupSize() - dungeonPortalEntity.getWaitingUuids().size())),
                                        false);
                                return;
                            } else if (dungeonPortalEntity.getdungeonTeleportCountdown() <= 0) {
                                dungeonPortalEntity.startDungeonTeleportCountdown(dungeonWorld);
                                player.method_7346();
                            }
                        } else if (dungeonPortalEntity.getDungeonPlayerCount() <= 0 && dungeonPortalEntity.getdungeonTeleportCountdown() <= 0) {
                            dungeonPortalEntity.addWaitingUuid(requiredMinGroupUuid);
                            dungeonPortalEntity.startDungeonTeleportCountdown(dungeonWorld);
                            player.method_7346();
                        } else if (dungeonPortalEntity.getdungeonTeleportCountdown() > 0) {
                            dungeonPortalEntity.addWaitingUuid(requiredMinGroupUuid);
                            player.method_7346();
                        } else if (!dungeonPortalEntity.getDeadDungeonPlayerUUIDs().contains(player.method_5667()) || dungeonPortalEntity.getDungeon().isRespawnAllowed()) {
                            teleportPlayer(player, dungeonWorld, dungeonPortalEntity, dungeonPortalPos);
                        } else {
                            player.method_7353(class_2561.method_43471("text.dungeonz.dead_player"), false);
                            player.method_7346();
                        }
                    } else {
                        player.method_7353(class_2561.method_43471("text.dungeonz.dungeon_full"), false);
                    }
                } else {
                    player.method_7353(class_2561.method_43471("text.dungeonz.dungeon_missing"), false);
                }
            }
        }
    }

    public static void teleportPlayer(class_3222 serverPlayerEntity, class_3218 dungeonWorld, DungeonPortalEntity dungeonPortalEntity, class_2338 dungeonPortalPos) {
        class_3222 playerEntity = (class_3222) serverPlayerEntity.method_5731(DungeonPlacementHandler.enter(serverPlayerEntity, dungeonWorld, serverPlayerEntity.method_51469(),
                dungeonPortalEntity, dungeonPortalPos, dungeonPortalEntity.getDifficulty(), dungeonPortalEntity.getDungeon().isPositiveEffectsAllowed()));

        DungeonServerPacket.writeS2CDungeonInfoPacket(playerEntity, dungeonPortalEntity.getDungeon().getBreakableBlockIdList(), dungeonPortalEntity.getDungeon().getplaceableBlockIdList(),
                dungeonPortalEntity.getDungeon().isElytraAllowed());
    }

    public static void teleportOutOfDungeon(class_3222 player) {
        class_3218 oldWorld = ((ServerPlayerAccess) player).getOldServerWorld();
        if (oldWorld != null) {
            player.method_5731(DungeonPlacementHandler.leave(player, oldWorld));
        } else {
            class_243 spawnPos = null;
            if (player.method_26280() != null) {
                spawnPos = new class_243(player.method_26280().method_10263(), player.method_26280().method_10264(), player.method_26280().method_10260());
            } else {
                // spawnPos = ServerPlayerEntity.findRespawnPosition(player.server.getWorld(player.getSpawnPointDimension()), ((ServerPlayerAccess) player).getDungeonSpawnBlockPos(), 0.0f, true,
                // true).get();
                player.method_5731(player.method_60590(true, class_5454.field_52245));
            }
            player.method_5731(new class_5454(player.field_13995.method_3847(player.method_26281()), spawnPos, new class_243(0.0D, 0.0D, 0.0D), 0.0f, 0.0f, class_5454.field_52245));
        }
    }

}
