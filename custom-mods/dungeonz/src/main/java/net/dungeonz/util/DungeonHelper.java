package net.dungeonz.util;

import net.dungeonz.DungeonzMain;
import net.dungeonz.access.ServerPlayerAccess;
import net.dungeonz.block.entity.DungeonPortalEntity;
import net.dungeonz.dungeon.Dungeon;
import net.dungeonz.dungeon.DungeonPlacementHandler;
import net.dungeonz.init.DimensionInit;
import net.dungeonz.network.DungeonServerPacket;
import net.dungeonz.compat.HearthwindLevels;
import net.dungeonz.compat.HearthwindGroups;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.Map.Entry;

public class DungeonHelper {

    @Nullable
    public static Dungeon getCurrentDungeon(ServerPlayer playerEntity) {
        if (playerEntity != null && playerEntity.level().dimension() == DimensionInit.DUNGEON_WORLD && ((ServerPlayerAccess) playerEntity).getOldServerWorld() != null) {
            BlockEntity blockEntity = ((ServerPlayerAccess) playerEntity).getOldServerWorld().getBlockEntity(((ServerPlayerAccess) playerEntity).getDungeonPortalBlockPos());
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
    public static DungeonPortalEntity getDungeonPortalEntity(ServerPlayer playerEntity) {
        if (((ServerPlayerAccess) playerEntity).getOldServerWorld() != null) {
            BlockEntity blockEntity = ((ServerPlayerAccess) playerEntity).getOldServerWorld().getBlockEntity(((ServerPlayerAccess) playerEntity).getDungeonPortalBlockPos());
            if (blockEntity == null) {
                return null;
            }
            if (blockEntity instanceof DungeonPortalEntity dungeonPortalEntity) {
                return dungeonPortalEntity;
            }
        }
        return null;
    }

    public static Map<String, List<ItemStack>> getRequiredItemStackList(Dungeon dungeon) {
        Map<String, List<ItemStack>> requiredItemStackList = new HashMap<>();
        for (Entry<String, HashMap<Integer, Integer>> entry : dungeon.getDifficultyRequiredItemCountMap().entrySet()) {
            List<ItemStack> stacks = new ArrayList<>();
            for (Entry<Integer, Integer> itemIdEntry : entry.getValue().entrySet()) {
                stacks.add(new ItemStack(BuiltInRegistries.ITEM.byId(itemIdEntry.getKey()), itemIdEntry.getValue()));
            }
            requiredItemStackList.put(entry.getKey(), stacks);
        }
        return requiredItemStackList;
    }

    public static Map<String, List<ItemStack>> getPossibleLootItemStackMap(Dungeon dungeon, MinecraftServer server) {
        HashMap<String, List<ItemStack>> possibleLootItemStackMap = new HashMap<String, List<ItemStack>>();
        for (Entry<String, String> entry : dungeon.getDifficultyBossLootTableMap().entrySet()) {
            LootTable lootTable = server.reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, Identifier.parse(entry.getValue())));
            LootParams.Builder builder = new LootParams.Builder(server.overworld()).withParameter(LootContextParams.ORIGIN,
                    server.getPlayerList().getPlayers().get(server.overworld().getRandom().nextInt(server.getPlayerList().getPlayers().size())).position());

            Container inventory = new SimpleContainer(27);
            lootTable.fill(inventory, builder.create(LootContextParamSets.CHEST), server.overworld().getRandom().nextLong());

            List<ItemStack> itemStacks = new ArrayList<ItemStack>();
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                if (!inventory.getItem(i).isEmpty()) {
                    if (inventory.getItem(i).isDamaged()) {
                        inventory.getItem(i).setDamageValue(0);
                    }
                    boolean contains = false;
                    for (ItemStack itemStack : itemStacks) {
                        if (ItemStack.isSameItem(itemStack, inventory.getItem(i))) {
                            itemStack.grow(inventory.getItem(i).getCount());
                            contains = true;
                            break;
                        }
                    }
                    if (!contains) {
                        itemStacks.add(inventory.getItem(i));
                    }
                }
            }
            possibleLootItemStackMap.put(entry.getKey(), itemStacks);
        }

        return possibleLootItemStackMap;
    }

    public static void teleportDungeon(ServerPlayer player, BlockPos dungeonPortalPos, @Nullable UUID requiredMinGroupUuid) {
        if (player.level().getBlockEntity(dungeonPortalPos) != null && player.level().getBlockEntity(dungeonPortalPos) instanceof DungeonPortalEntity dungeonPortalEntity) {

            if (player.level().dimension() == DimensionInit.DUNGEON_WORLD) {
                ServerLevel oldWorld = ((ServerPlayerAccess) player).getOldServerWorld();
                if (oldWorld != null) {
                    player.teleport(DungeonPlacementHandler.leave(player, oldWorld));
                    return;
                }
            } else {
                ServerLevel dungeonWorld = player.level().getServer().getLevel(DimensionInit.DUNGEON_WORLD);
                if (dungeonWorld == null) {
                    player.sendSystemMessage(Component.literal("Failed to find world, was it registered?"));
                    return;
                }
                if (dungeonPortalEntity.getDungeon() != null) {
                    if ((dungeonPortalEntity.getDungeonPlayerCount() + dungeonPortalEntity.getDeadDungeonPlayerUUIDs().size()) < dungeonPortalEntity.getMaxGroupSize()) {

                        if (dungeonPortalEntity.isOnCooldown((int) dungeonWorld.getGameTime())) {
                            player.sendSystemMessage(Component.translatable("text.dungeonz.dungeon_cooldown"));
                            return;
                        }
                        if (dungeonPortalEntity.getDungeonPlayerCount() > 0 && dungeonPortalEntity.getPrivateGroup()) {
                            if (DungeonzMain.isPartyAddonLoaded) {
                                if (!HearthwindGroups.admits(player, dungeonPortalEntity.getDungeonPlayerUuids().get(0))) {
                                    player.sendSystemMessage(Component.translatable("text.dungeonz.dungeon_private"));
                                    return;
                                }
                            } else {
                                player.sendSystemMessage(Component.translatable("text.dungeonz.dungeon_private"));
                                return;
                            }
                        }
                        if (!dungeonPortalEntity.getWaitingUuids().isEmpty() && dungeonPortalEntity.getWaitingUuids().contains(player.getUUID())) {
                            player.closeContainer();
                            return;
                        }
                        if (!player.isCreative()) {
                            if (DungeonHelper.getRequiredItemStackList(dungeonPortalEntity.getDungeon()).containsKey(dungeonPortalEntity.getDifficulty())) {
                                if (InventoryHelper.hasRequiredItemStacks(player.getInventory(), DungeonHelper.getRequiredItemStackList(dungeonPortalEntity.getDungeon()).get(dungeonPortalEntity.getDifficulty()))) {
                                    InventoryHelper.decrementRequiredItemStacks(player.getInventory(), DungeonHelper.getRequiredItemStackList(dungeonPortalEntity.getDungeon()).get(dungeonPortalEntity.getDifficulty()));
                                } else {
                                    player.sendSystemMessage(Component.translatable("text.dungeonz.missing"));
                                    return;
                                }
                            }
                        }
                        if (!HearthwindLevels.meetsRequiredLevel(player, dungeonPortalEntity.getDungeon().getRequiredLevel())) {
                            player.sendSystemMessage(Component.translatable("text.dungeonz.required_level", dungeonPortalEntity.getDungeon().getRequiredLevel()));
                            return;
                        }
                        if (dungeonPortalEntity.getDungeonPlayerCount() <= 0 && requiredMinGroupUuid != null && dungeonPortalEntity.getMinGroupSize() > 1) {
                            dungeonPortalEntity.addWaitingUuid(requiredMinGroupUuid);
                            if (dungeonPortalEntity.getMinGroupSize() > dungeonPortalEntity.getWaitingUuids().size()) {
                                player.sendSystemMessage(Component.translatable("text.dungeonz.dungeon_min_group_size", (dungeonPortalEntity.getMinGroupSize() - dungeonPortalEntity.getWaitingUuids().size())));
                                return;
                            } else if (dungeonPortalEntity.getdungeonTeleportCountdown() <= 0) {
                                dungeonPortalEntity.startDungeonTeleportCountdown(dungeonWorld);
                                player.closeContainer();
                            }
                        } else if (dungeonPortalEntity.getDungeonPlayerCount() <= 0 && dungeonPortalEntity.getdungeonTeleportCountdown() <= 0) {
                            dungeonPortalEntity.addWaitingUuid(requiredMinGroupUuid);
                            dungeonPortalEntity.startDungeonTeleportCountdown(dungeonWorld);
                            player.closeContainer();
                        } else if (dungeonPortalEntity.getdungeonTeleportCountdown() > 0) {
                            dungeonPortalEntity.addWaitingUuid(requiredMinGroupUuid);
                            player.closeContainer();
                        } else if (!dungeonPortalEntity.getDeadDungeonPlayerUUIDs().contains(player.getUUID()) || dungeonPortalEntity.getDungeon().isRespawnAllowed()) {
                            teleportPlayer(player, dungeonWorld, dungeonPortalEntity, dungeonPortalPos);
                        } else {
                            player.sendSystemMessage(Component.translatable("text.dungeonz.dead_player"));
                            player.closeContainer();
                        }
                    } else {
                        player.sendSystemMessage(Component.translatable("text.dungeonz.dungeon_full"));
                    }
                } else {
                    player.sendSystemMessage(Component.translatable("text.dungeonz.dungeon_missing"));
                }
            }
        }
    }

    public static void teleportPlayer(ServerPlayer serverPlayerEntity, ServerLevel dungeonWorld, DungeonPortalEntity dungeonPortalEntity, BlockPos dungeonPortalPos) {
        ServerPlayer playerEntity = (ServerPlayer) serverPlayerEntity.teleport(DungeonPlacementHandler.enter(serverPlayerEntity, dungeonWorld, serverPlayerEntity.level(),
                dungeonPortalEntity, dungeonPortalPos, dungeonPortalEntity.getDifficulty(), dungeonPortalEntity.getDungeon().isPositiveEffectsAllowed()));

        DungeonServerPacket.writeS2CDungeonInfoPacket(playerEntity, dungeonPortalEntity.getDungeon().getBreakableBlockIdList(), dungeonPortalEntity.getDungeon().getplaceableBlockIdList(),
                dungeonPortalEntity.getDungeon().isElytraAllowed());
    }

    public static void teleportOutOfDungeon(ServerPlayer player) {
        ServerLevel oldWorld = ((ServerPlayerAccess) player).getOldServerWorld();
        if (oldWorld != null) {
            player.teleport(DungeonPlacementHandler.leave(player, oldWorld));
        } else {
            Vec3 spawnPos = null;
            ServerPlayer.RespawnConfig respawnConfig = player.getRespawnConfig();
            if (respawnConfig != null) {
                BlockPos respawnPos = respawnConfig.respawnData().pos();
                spawnPos = new Vec3(respawnPos.getX(), respawnPos.getY(), respawnPos.getZ());
            } else {
                // spawnPos = ServerPlayerEntity.findRespawnPosition(player.server.getWorld(player.getSpawnPointDimension()), ((ServerPlayerAccess) player).getDungeonSpawnBlockPos(), 0.0f, true,
                // true).get();
                player.teleport(player.findRespawnPositionAndUseSpawnBlock(true, TeleportTransition.DO_NOTHING));
            }
            player.teleport(new TeleportTransition(player.level().getServer().getLevel(respawnConfig.respawnData().dimension()), spawnPos, new Vec3(0.0D, 0.0D, 0.0D), 0.0f, 0.0f,
                    TeleportTransition.DO_NOTHING));
        }
    }

}
