package net.dungeonz.dungeon;

import net.dungeonz.DungeonzMain;
import net.dungeonz.access.BossEntityAccess;
import net.dungeonz.access.ServerPlayerAccess;
import net.dungeonz.block.DungeonGateBlock;
import net.dungeonz.block.entity.DungeonGateEntity;
import net.dungeonz.block.entity.DungeonPortalEntity;
import net.dungeonz.block.entity.DungeonSpawnerEntity;
import net.dungeonz.init.BlockInit;
import net.dungeonz.init.TagInit;
import net.dungeonz.util.InventoryHelper;
import net.dungeonz.util.PropertyUtil;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1308;
import net.minecraft.class_1309;
import net.minecraft.class_1542;
import net.minecraft.class_1676;
import net.minecraft.class_1923;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2378;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2487;
import net.minecraft.class_2680;
import net.minecraft.class_2741;
import net.minecraft.class_2794;
import net.minecraft.class_2960;
import net.minecraft.class_3195;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.class_3230;
import net.minecraft.class_3341;
import net.minecraft.class_3443;
import net.minecraft.class_3485;
import net.minecraft.class_3730;
import net.minecraft.class_3778;
import net.minecraft.class_3785;
import net.minecraft.class_3790;
import net.minecraft.class_4076;
import net.minecraft.class_5134;
import net.minecraft.class_5138;
import net.minecraft.class_5321;
import net.minecraft.class_5434;
import net.minecraft.class_5454;
import net.minecraft.class_5688;
import net.minecraft.class_5819;
import net.minecraft.class_6626;
import net.minecraft.class_6880;
import net.minecraft.class_7923;
import net.minecraft.class_7924;
import net.minecraft.class_8891;
import net.minecraft.entity.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.*;
import net.rpgdifficulty.api.MobStrengthener;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.Map.Entry;

public class DungeonPlacementHandler {

    public static class_5454 enter(class_3222 serverPlayerEntity, class_3218 dungeonWorld, class_3218 oldWorld, DungeonPortalEntity portalEntity, class_2338 portalPos, String difficulty,
                                       boolean positiveEffects) {
        class_2338 playerBlockPos = serverPlayerEntity.method_24515().method_25503();

        if (oldWorld.method_8320(playerBlockPos).method_27852(BlockInit.DUNGEON_PORTAL) || oldWorld.method_8320(playerBlockPos.method_10074()).method_27852(BlockInit.DUNGEON_PORTAL)) {
            if (oldWorld.method_8320(playerBlockPos).method_27852(BlockInit.DUNGEON_PORTAL)) {
                playerBlockPos = playerBlockPos.method_10084();
            }
            for (int i = 0; i < 4; i++) {
                if (oldWorld.method_8320(playerBlockPos.method_10079(class_2350.method_10139(i), 1).method_10086(0)).method_26215()
                        && oldWorld.method_8320(playerBlockPos.method_10079(class_2350.method_10139(i), 1).method_10086(1)).method_26215()) {
                    playerBlockPos = playerBlockPos.method_10079(class_2350.method_10139(i), 1);
                    break;
                }
                if (i == 3) {
                    class_243 vec3d = serverPlayerEntity.method_60590(true, class_5454.field_52245).comp_2821();
                    playerBlockPos = class_2338.method_49637(vec3d.method_10216(), vec3d.method_10214(), vec3d.method_10215());
                }
            }
        }
        ((ServerPlayerAccess) serverPlayerEntity).setDungeonInfo(oldWorld, portalPos, playerBlockPos);
        if (!positiveEffects) {
            serverPlayerEntity.method_6012();
        }

        portalEntity.joinDungeon(serverPlayerEntity.method_5667());

        return new class_5454(dungeonWorld, class_243.method_24954(new class_2338(0, 0, 0).method_10069(portalPos.method_10263() * 16, 100, portalPos.method_10260() * 16)).method_1031(0.5, 0, 0.5), class_243.field_1353, 0, 0, class_5454.field_52245);
    }

    public static class_5454 leave(class_3222 serverPlayerEntity, class_3218 serverWorld) {
        if (serverWorld.method_8321(((ServerPlayerAccess) serverPlayerEntity).getDungeonPortalBlockPos()) != null) {
            ((DungeonPortalEntity) serverWorld.method_8321(((ServerPlayerAccess) serverPlayerEntity).getDungeonPortalBlockPos())).leaveDungeon(serverPlayerEntity.method_5667());
        }
        return new class_5454(serverWorld, class_243.method_24954(((ServerPlayerAccess) serverPlayerEntity).getDungeonSpawnBlockPos()).method_1031(0.5, 0, 0.5), class_243.field_1353, serverWorld.field_9229.method_43057() * 360F, 0,
                class_5454.field_52245);
    }

    public static void generateDungeonStructure(class_3218 world, class_2338 pos, DungeonPortalEntity portalEntity) {
        class_2378<class_3785> registry = world.method_30349().method_30530(class_7924.field_41249);

        class_6880<class_3785> registryEntry = registry.method_40290(class_5321.method_29179(class_7924.field_41249, portalEntity.getDungeon().getStructurePoolId()));
        generate(world, portalEntity, portalEntity.getDungeon(), registryEntry, class_2960.method_60654("dungeonz:spawn"), 64, pos, false);
    }

    private static boolean generate(class_3218 world, DungeonPortalEntity portalEntity, Dungeon dungeon, class_6880<class_3785> structurePool, class_2960 id, int size, class_2338 pos,
                                    boolean keepJigsaws) {
        class_2794 chunkGenerator = world.method_14178().method_12129();
        class_3485 structureTemplateManager = world.method_14183();
        class_5138 structureAccessor = world.method_27056();
        class_5819 random = world.method_8409();
        class_3195.class_7149 context = new class_3195.class_7149(world.method_30349(), chunkGenerator, chunkGenerator.method_12098(), world.method_14178().method_41248(),
                structureTemplateManager, world.method_8412(), new class_1923(pos), world, registryEntry -> true);

        Optional<class_3195.class_7150> optional = class_3778.method_30419(context, structurePool, Optional.of(id), size, pos, false, Optional.empty(), 512,
                class_8891.field_46826, class_5434.field_51911, class_5434.field_52235);
        if (optional.isPresent()) {
            HashMap<Integer, ArrayList<class_2338>> blockIdPosMap = new HashMap<Integer, ArrayList<class_2338>>();
            ArrayList<class_2338> chestPosList = new ArrayList<class_2338>();
            ArrayList<class_2338> exitPosList = new ArrayList<class_2338>();
            ArrayList<class_2338> gatePosList = new ArrayList<class_2338>();
            Map<class_2338, Integer> movingBlockMap = new HashMap<>();
            Map<class_2338, DungeonPortalEntity.Powered> poweredBlockMap = new HashMap<>();
            HashMap<class_2338, Integer> spawnerPosEntityIdMap = new HashMap<class_2338, Integer>();
            class_2248 exitBlock = class_7923.field_41175.method_10200(dungeon.getExitBlockId());
            class_2248 bossLootBlock = class_7923.field_41175.method_10200(dungeon.getBossLootBlockId());

            class_6626 structurePiecesCollector = optional.get().method_44019();
            for (class_3443 structurePiece : structurePiecesCollector.method_38714().comp_132()) {
                if (!(structurePiece instanceof class_3790 poolStructurePiece)) {
                    continue;
                }
                poolStructurePiece.method_27236(world, structureAccessor, chunkGenerator, random, class_3341.method_14665(), pos, keepJigsaws);

                portalEntity.addDungeonEdge(poolStructurePiece.method_14935().method_35415(), poolStructurePiece.method_14935().method_35416(), poolStructurePiece.method_14935().method_35417());
                portalEntity.addDungeonEdge(poolStructurePiece.method_14935().method_35418(), poolStructurePiece.method_14935().method_35419(), poolStructurePiece.method_14935().method_35420());
                for (int i = poolStructurePiece.method_14935().method_35415(); i <= poolStructurePiece.method_14935().method_35418(); i++) {
                    for (int u = poolStructurePiece.method_14935().method_35416(); u <= poolStructurePiece.method_14935().method_35419(); u++) {
                        for (int o = poolStructurePiece.method_14935().method_35417(); o <= poolStructurePiece.method_14935().method_35420(); o++) {
                            class_2338 checkPos = new class_2338(i, u, o);
                            class_2680 state = world.method_8320(checkPos);
                            if (!state.method_26215()) {
                                int blockId = class_7923.field_41175.method_10206(state.method_26204());
                                if (dungeon.containsBlockId(blockId)) {
                                    if (!blockIdPosMap.containsKey(blockId)) {
                                        ArrayList<class_2338> newList = new ArrayList<class_2338>();
                                        newList.add(checkPos);
                                        blockIdPosMap.put(blockId, newList);
                                    } else {
                                        blockIdPosMap.get(blockId).add(checkPos);
                                    }
                                } else if (dungeon.getBossBlockId() == blockId) {
                                    portalEntity.setBossBlockPos(checkPos);
                                } else if (state.method_27852(class_2246.field_10034) || state.method_27852(class_2246.field_16328) || state.method_27852(class_2246.field_10380)) {
                                    chestPosList.add(checkPos);
                                } else if (state.method_27852(exitBlock)) {
                                    exitPosList.add(checkPos);
                                } else if (state.method_27852(bossLootBlock)) {
                                    portalEntity.setBossLootBlockPos(checkPos);
                                } else if (state.method_27852(BlockInit.DUNGEON_SPAWNER)) {
                                    spawnerPosEntityIdMap.put(checkPos, ((DungeonSpawnerEntity) world.method_8321(checkPos)).getLogic().getEntityId());
                                } else if (state.method_27852(BlockInit.DUNGEON_GATE)) {
                                    gatePosList.add(checkPos);
                                    if (world.method_8321(checkPos) != null && ((DungeonGateEntity) world.method_8321(checkPos)).getUnlockItem() == null) {
                                        DungeonGateEntity dungeonGateEntity = (DungeonGateEntity) world.method_8321(checkPos);
                                        dungeonGateEntity.addDungeonEdge(poolStructurePiece.method_14935().method_35415(), poolStructurePiece.method_14935().method_35416(),
                                                poolStructurePiece.method_14935().method_35417());
                                        dungeonGateEntity.addDungeonEdge(poolStructurePiece.method_14935().method_35418(), poolStructurePiece.method_14935().method_35419(),
                                                poolStructurePiece.method_14935().method_35420());
                                        dungeonGateEntity.method_5431();
                                    }
                                } else if (state.method_26204() instanceof class_5688) {
                                    movingBlockMap.put(checkPos, blockId);
                                } else if (state.method_28498(class_2741.field_12484) && !state.method_27852(class_2246.field_10282)) {
                                    poweredBlockMap.put(checkPos, new DungeonPortalEntity.Powered(blockId, state.method_11654(class_2741.field_12484), PropertyUtil.getHorizontalFacing(state), PropertyUtil.getBlockFacing(state)));
                                }
                            }
                        }
                    }
                }
            }
            portalEntity.setChestPosList(chestPosList);
            portalEntity.setExitPosList(exitPosList);
            portalEntity.setMovingBlockMap(movingBlockMap);
            portalEntity.setPoweredBlockMap(poweredBlockMap);
            portalEntity.setBlockMap(blockIdPosMap);
            portalEntity.setSpawnerPosEntityIdMap(spawnerPosEntityIdMap);
            portalEntity.setGatePosList(gatePosList);
            portalEntity.method_5431();
            return true;
        }
        return false;
    }

    public static void prepareDungeon(class_3218 dungeonWorld, DungeonPortalEntity portalEntity) {
        List<class_1923> chunkPosList = new ArrayList<>();
        for (int i = 0; i < portalEntity.getDungeonEdgeList().size() / 6; i++) {
            int x1 = portalEntity.getDungeonEdgeList().get(6 * i);
            int z1 = portalEntity.getDungeonEdgeList().get(2 + 6 * i);

            int x2 = portalEntity.getDungeonEdgeList().get(3 + 6 * i);
            int z2 = portalEntity.getDungeonEdgeList().get(5 + 6 * i);

            int xCount = Math.abs(x1 - x2) / 16 + ((x1 - x2) % 16 == 0 ? 0 : 1);
            int zCount = Math.abs(z1 - z2) / 16 + ((z1 - z2) % 16 == 0 ? 0 : 1);
            for (int u = 0; u < xCount; u++) {
                for (int o = 0; o < zCount; o++) {
                    class_1923 chunkPos = new class_1923(class_4076.method_18675(x1 + 16 * u), class_4076.method_18675(z1 + 16 * o));
                    if (!chunkPosList.contains(chunkPos)) {
                        chunkPosList.add(chunkPos);
                    }
                }
            }
        }
        for (class_1923 chunkPos : chunkPosList) {
            dungeonWorld.method_14178().method_17297(class_3230.field_19280, chunkPos, 1, chunkPos.method_8323());
        }
    }

    public static void refreshDungeon(MinecraftServer server, class_3218 world, DungeonPortalEntity portalEntity, Dungeon dungeon, String difficulty) {

        // Could be tested with create = true
        // world.getChunkManager().threadedAnvilChunkStorage.getChunk(holder, requiredStatus).thenApply(either -> {
        // // This block will be executed when the CompletableFuture completes
        // // either contains the result of the getChunk method
        // return either.map(chunk -> {
        // // Do something with the Chunk
        // System.out.println("Got chunk: " + chunk);
        // return chunk;
        // }, unloaded -> {
        // // Handle the Unloaded case
        // System.out.println("Chunk is unloaded");
        // return null;
        // });
        // }).thenAccept(result -> {
        // // This block will be executed after the thenApply block
        // System.out.println("Completed processing of chunk");
        // });

        // Refresh mobs
        for (int u = 0; u < portalEntity.getDungeonEdgeList().size() / 6; u++) {
            List<class_1297> entities = world.method_8335(null,
                    new class_238(portalEntity.getDungeonEdgeList().get(6 * u), portalEntity.getDungeonEdgeList().get(1 + 6 * u), portalEntity.getDungeonEdgeList().get(2 + 6 * u),
                            portalEntity.getDungeonEdgeList().get(3 + 6 * u), portalEntity.getDungeonEdgeList().get(4 + 6 * u), portalEntity.getDungeonEdgeList().get(5 + 6 * u)));

            for (class_1297 entity : entities) {
                if (!(entity instanceof class_3222)
                        && (entity instanceof class_1309 || entity instanceof class_1542 || entity instanceof class_1676)) {
                    entity.method_31472();
                }
            }
        }
        portalEntity.setDifficulty(difficulty);

        for (Entry<Integer, ArrayList<class_2338>> entry : portalEntity.getBlockMap().entrySet()) {
            for (int i = 0; i < entry.getValue().size(); i++) {
                if (dungeon.getBlockIdBlockReplacementMap().get(entry.getKey()) != -1) {
                    if (dungeon.getBlockIdBlockReplacementMap().get(entry.getKey()) == 0) {
                        world.method_8650(entry.getValue().get(i), false);
                    } else {
                        world.method_8652(entry.getValue().get(i), class_7923.field_41175.method_10200(dungeon.getBlockIdBlockReplacementMap().get(entry.getKey())).method_9564(), 3);
                    }
                }
                // dungeon.getBlockIdEntitySpawnChanceMap().containsKey(blockId) &&
                if (world.method_8409().method_43057() <= dungeon.getBlockIdEntitySpawnChanceMap().get(entry.getKey()).get(difficulty)) {
                    class_1308 mobEntity = createMob(world, dungeon.getBlockIdEntityMap().get(entry.getKey()).get(world.method_8409().method_43048(dungeon.getBlockIdEntityMap().get(entry.getKey()).size())),
                            null);
                    // hopefully initialize doesn't lead to problems
                    mobEntity.method_5943(world, world.method_8404(entry.getValue().get(i)), class_3730.field_16474, null);
                    mobEntity.method_5971();
                    strengthenMob(mobEntity, dungeon, difficulty, false);
                    dungeon.getBlockIdBlockReplacementMap().get(entry.getKey());
                    mobEntity.method_5725(entry.getValue().get(i), 360f * world.method_8409().method_43057(), 0.0f);
                    world.method_8649(mobEntity);
                }
            }
        }

        // Refresh boss
        class_1308 bossEntity = createMob(world, dungeon.getBossEntityType(), dungeon.getBossNbtCompound());
        bossEntity.method_5943(world, world.method_8404(portalEntity.getBossBlockPos()), class_3730.field_16474, null);
        bossEntity.method_5971();
        ((BossEntityAccess) bossEntity).setBoss(portalEntity.method_11016(), portalEntity.method_10997().method_27983().method_29177().toString());
        strengthenMob(bossEntity, dungeon, difficulty, true);

        if (dungeon.getBlockIdBlockReplacementMap().get(dungeon.getBossBlockId()) != -1) {
            if (dungeon.getBlockIdBlockReplacementMap().get(dungeon.getBossBlockId()) == 0) {
                world.method_8650(portalEntity.getBossBlockPos(), false);
            } else {
                world.method_8652(portalEntity.getBossBlockPos(), class_7923.field_41175.method_10200(dungeon.getBlockIdBlockReplacementMap().get(dungeon.getBossBlockId())).method_9564(), 3);
            }
        }
        bossEntity.method_5725(portalEntity.getBossBlockPos(), 360f * world.method_8409().method_43057(), 0.0f);
        world.method_8649(bossEntity);

        // Refresh chests
        for (int i = 0; i < portalEntity.getChestPosList().size(); i++) {
            String lootTableString = dungeon.getDifficultyLootTableIdMap().get(difficulty).get(world.method_8409().method_43048(dungeon.getDifficultyLootTableIdMap().get(difficulty).size()));
            InventoryHelper.fillInventoryWithLoot(server, world, portalEntity.getChestPosList().get(i), lootTableString);
        }
        // Refresh exit
        for (int i = 0; i < portalEntity.getExitPosList().size(); i++) {
            world.method_8652(portalEntity.getExitPosList().get(i),
                    dungeon.getBlockIdBlockReplacementMap().containsKey(dungeon.getExitBlockId()) && dungeon.getBlockIdBlockReplacementMap().get(dungeon.getExitBlockId()) != -1
                            ? class_7923.field_41175.method_10200(dungeon.getBlockIdBlockReplacementMap().get(dungeon.getExitBlockId())).method_9564()
                            : class_7923.field_41175.method_10200(dungeon.getExitBlockId()).method_9564(),
                    3);
        } // Refresh gates
        for (int i = 0; i < portalEntity.getGatePosList().size(); i++) {
            world.method_8501(portalEntity.getGatePosList().get(i), world.method_8320(portalEntity.getGatePosList().get(i)).method_28493(DungeonGateBlock.ENABLED));
        }
        // Refresh boss loot
        world.method_8652(portalEntity.getBossLootBlockPos(),
                dungeon.getBlockIdBlockReplacementMap().containsKey(dungeon.getBossLootBlockId()) && dungeon.getBlockIdBlockReplacementMap().get(dungeon.getBossLootBlockId()) != -1
                        ? class_7923.field_41175.method_10200(dungeon.getBlockIdBlockReplacementMap().get(dungeon.getBossLootBlockId())).method_9564()
                        : class_7923.field_41175.method_10200(dungeon.getBossLootBlockId()).method_9564(),
                3);
        // Refresh spawner
        for (Entry<class_2338, Integer> entry : portalEntity.getSpawnerPosEntityIdMap().entrySet()) {
            world.method_8652(entry.getKey(), BlockInit.DUNGEON_SPAWNER.method_9564(), 3);
            ((DungeonSpawnerEntity) world.method_8321(entry.getKey())).getLogic().setDungeonInfo(dungeon, difficulty,
                    dungeon.getSpawnerEntityIdMap().containsKey(entry.getValue()) ? dungeon.getSpawnerEntityIdMap().get(entry.getValue()) : 0, class_7923.field_41177.method_10200(entry.getValue()));
        }
        // Refresh blocks
        for (Entry<class_2338, Integer> entry : portalEntity.getReplaceBlockIdMap().entrySet()) {
            world.method_8652(entry.getKey(), class_7923.field_41175.method_10200(entry.getValue()).method_9564(), 3);
        }
        // Refresh powered blocks
        for (Entry<class_2338, DungeonPortalEntity.Powered> entry : portalEntity.getPoweredBlockMap().entrySet()) {
            class_2680 blockState = class_7923.field_41175.method_10200(entry.getValue().getBlockId()).method_9564().method_11657(class_2741.field_12484, entry.getValue().getPowered());
            boolean hasFacing = blockState.method_28498(class_2741.field_12481);
            if (hasFacing) {
                blockState = blockState.method_11657(class_2741.field_12481, class_2350.method_10139(entry.getValue().getFacing()));
            }
            if (blockState.method_28498(class_2741.field_12555)) {
                blockState = blockState.method_11657(class_2741.field_12555, PropertyUtil.getBlockFacing(entry.getValue().getBlockFacing()));
            }
            world.method_8652(entry.getKey(), blockState, 3);
            world.method_8452(entry.getKey(), world.method_8320(entry.getKey()).method_26204());
            if (hasFacing) {
                world.method_8452(entry.getKey().method_10093(world.method_8320(entry.getKey()).method_11654(class_2741.field_12481).method_10153()), world.method_8320(entry.getKey()).method_26204());
            }
        }
        // Refresh moving blocks
        List<class_2338> freshPlacedBlockPoses = new ArrayList<>();
        for (Entry<class_2338, Integer> entry : portalEntity.getMovingBlockMap().entrySet()) {
            class_2248 block = class_7923.field_41175.method_10200(entry.getValue());
            if (!world.method_8320(entry.getKey()).method_27852(block)) {
                for (int i = 1; i < 50; i++) {
                    class_2338 checkPos = entry.getKey().method_10087(i);
                    if (freshPlacedBlockPoses.contains(checkPos)) {
                        continue;
                    }
                    if (world.method_8320(checkPos).method_27852(block)) {
                        world.method_8650(checkPos, false);
                        break;
                    }
                }
                world.method_8652(entry.getKey(), class_7923.field_41175.method_10200(entry.getValue()).method_9564(), 3);
                freshPlacedBlockPoses.add(entry.getKey());
            }
        }
        portalEntity.getDungeonPlayerUuids().clear();
        portalEntity.getDeadDungeonPlayerUUIDs().clear();
        portalEntity.method_5431();
    }

    @Nullable
    private static class_1308 createMob(class_3218 world, class_1299<?> type, @Nullable class_2487 nbt) {
        class_1308 mobEntity;
        try {
            Object entity = type.method_5883(world);
            if (!(entity instanceof class_1308)) {
                throw new IllegalStateException("Trying to spawn a non-mob: " + class_7923.field_41177.method_10221(type));
            }
            mobEntity = (class_1308) entity;

        } catch (Exception exception) {
            DungeonzMain.LOGGER.warn("Failed to create mob", exception);
            return null;
        }
        if (nbt != null) {
            class_2487 nbtCompound = mobEntity.method_5647(new class_2487());
            nbtCompound.method_10543(nbt);
            mobEntity.method_5651(nbtCompound);
        }
        if (mobEntity.method_5864().method_20210(TagInit.IMMUNE_TO_ZOMBIFICATION)) {
            class_2487 nbtCompound = mobEntity.method_5647(new class_2487());
            nbtCompound.method_10556("IsImmuneToZombification", true);
            mobEntity.method_5651(nbtCompound);
        }
        return mobEntity;
    }

    public static void strengthenMob(class_1308 mobEntity, Dungeon dungeon, String difficulty, boolean isBossEntity) {
        double mobHealth = mobEntity.method_45325(class_5134.field_23716);
        double mobDamage = 0.0D;
        double mobProtection = 0.0D;
        double mobSpeed = 0.0D;

        boolean hasAttackDamageAttribute = mobEntity.method_6127().method_45331(class_5134.field_23721);
        boolean hasArmorAttribute = mobEntity.method_6127().method_45331(class_5134.field_23724);
        boolean hasSpeedAttribute = mobEntity.method_6127().method_45331(class_5134.field_23719);

        if (hasAttackDamageAttribute) {
            mobDamage = mobEntity.method_45325(class_5134.field_23721);
        }
        if (hasArmorAttribute) {
            mobProtection = mobEntity.method_45325(class_5134.field_23724);
        }
        if (hasSpeedAttribute) {
            mobSpeed = mobEntity.method_45325(class_5134.field_23719);
        }

        float healthFactor = 0.0f;
        float damageFactor = 0.0f;
        float protectionFactor = 0.0f;
        float speedFactor = 0.0f;

        if (isBossEntity) {
            healthFactor = dungeon.getDifficultyBossHealthModificatorMap().get(difficulty);
            damageFactor = dungeon.getDifficultyBossDamageModificatorMap().get(difficulty);
            protectionFactor = dungeon.getDifficultyBossProtectionModificatorMap().get(difficulty);
            speedFactor = dungeon.getDifficultyBossSpeedModificatorMap().get(difficulty);
        } else {
            healthFactor = dungeon.getDifficultyMobHealthModificatorMap().get(difficulty);
            damageFactor = dungeon.getDifficultyMobDamageModificatorMap().get(difficulty);
            protectionFactor = dungeon.getDifficultyMobProtectionModificatorMap().get(difficulty);
            speedFactor = dungeon.getDifficultyMobSpeedModificatorMap().get(difficulty);
        }

        mobHealth *= healthFactor;
        mobDamage *= damageFactor;
        mobProtection *= protectionFactor;
        mobSpeed *= speedFactor;

        // round factor
        mobHealth = Math.round(mobHealth * 100.0D) / 100.0D;
        mobDamage = Math.round(mobDamage * 100.0D) / 100.0D;
        mobProtection = Math.round(mobProtection * 100.0D) / 100.0D;
        mobSpeed = Math.round(mobSpeed * 100.0D) / 100.0D;

        // Set Values
        mobEntity.method_5996(class_5134.field_23716).method_6192(mobHealth);
        mobEntity.method_6025(mobEntity.method_6063());
        if (hasAttackDamageAttribute) {
            mobEntity.method_5996(class_5134.field_23721).method_6192(mobDamage);
        }
        if (hasArmorAttribute) {
            mobEntity.method_5996(class_5134.field_23724).method_6192(mobProtection);
        }
        if (hasSpeedAttribute) {
            mobEntity.method_5996(class_5134.field_23719).method_6192(mobSpeed);
        }
        if (DungeonzMain.isRpgDifficultyLoaded) {
            MobStrengthener.setMobHealthMultiplier(mobEntity, healthFactor);
        }

    }

}
