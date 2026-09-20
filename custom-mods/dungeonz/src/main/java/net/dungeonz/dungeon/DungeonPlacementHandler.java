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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.StructureManager;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.block.Fallable;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.util.ProblemReporter;
import net.minecraft.server.MinecraftServer;
import net.dungeonz.compat.HearthwindMobScaling;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.Map.Entry;

public class DungeonPlacementHandler {

    public static TeleportTransition enter(ServerPlayer serverPlayerEntity, ServerLevel dungeonWorld, ServerLevel oldWorld, DungeonPortalEntity portalEntity, BlockPos portalPos, String difficulty,
                                       boolean positiveEffects) {
        BlockPos playerBlockPos = serverPlayerEntity.blockPosition().mutable();

        if (oldWorld.getBlockState(playerBlockPos).is(BlockInit.DUNGEON_PORTAL) || oldWorld.getBlockState(playerBlockPos.below()).is(BlockInit.DUNGEON_PORTAL)) {
            if (oldWorld.getBlockState(playerBlockPos).is(BlockInit.DUNGEON_PORTAL)) {
                playerBlockPos = playerBlockPos.above();
            }
            for (int i = 0; i < 4; i++) {
                if (oldWorld.getBlockState(playerBlockPos.relative(Direction.from2DDataValue(i), 1).above(0)).isAir()
                        && oldWorld.getBlockState(playerBlockPos.relative(Direction.from2DDataValue(i), 1).above(1)).isAir()) {
                    playerBlockPos = playerBlockPos.relative(Direction.from2DDataValue(i), 1);
                    break;
                }
                if (i == 3) {
                    Vec3 vec3d = serverPlayerEntity.findRespawnPositionAndUseSpawnBlock(true, TeleportTransition.DO_NOTHING).position();
                    playerBlockPos = BlockPos.containing(vec3d.x(), vec3d.y(), vec3d.z());
                }
            }
        }
        ((ServerPlayerAccess) serverPlayerEntity).setDungeonInfo(oldWorld, portalPos, playerBlockPos);
        if (!positiveEffects) {
            serverPlayerEntity.removeAllEffects();
        }

        portalEntity.joinDungeon(serverPlayerEntity.getUUID());

        return new TeleportTransition(dungeonWorld, Vec3.atLowerCornerOf(new BlockPos(0, 0, 0).offset(portalPos.getX() * 16, 100, portalPos.getZ() * 16)).add(0.5, 0, 0.5), Vec3.ZERO, 0, 0, TeleportTransition.DO_NOTHING);
    }

    public static TeleportTransition leave(ServerPlayer serverPlayerEntity, ServerLevel serverWorld) {
        if (serverWorld.getBlockEntity(((ServerPlayerAccess) serverPlayerEntity).getDungeonPortalBlockPos()) != null) {
            ((DungeonPortalEntity) serverWorld.getBlockEntity(((ServerPlayerAccess) serverPlayerEntity).getDungeonPortalBlockPos())).leaveDungeon(serverPlayerEntity.getUUID());
        }
        return new TeleportTransition(serverWorld, Vec3.atLowerCornerOf(((ServerPlayerAccess) serverPlayerEntity).getDungeonSpawnBlockPos()).add(0.5, 0, 0.5), Vec3.ZERO, serverWorld.getRandom().nextFloat() * 360F, 0,
                TeleportTransition.DO_NOTHING);
    }

    public static void generateDungeonStructure(ServerLevel world, BlockPos pos, DungeonPortalEntity portalEntity) {
        Registry<StructureTemplatePool> registry = world.registryAccess().lookupOrThrow(Registries.TEMPLATE_POOL);

        Holder<StructureTemplatePool> registryEntry = registry.getOrThrow(ResourceKey.create(Registries.TEMPLATE_POOL, portalEntity.getDungeon().getStructurePoolId()));
        generate(world, portalEntity, portalEntity.getDungeon(), registryEntry, Identifier.parse("dungeonz:spawn"), 64, pos, false);
    }

    private static boolean generate(ServerLevel world, DungeonPortalEntity portalEntity, Dungeon dungeon, Holder<StructureTemplatePool> structurePool, Identifier id, int size, BlockPos pos,
                                    boolean keepJigsaws) {
        ChunkGenerator chunkGenerator = world.getChunkSource().getGenerator();
        StructureTemplateManager structureTemplateManager = world.getStructureManager();
        StructureManager structureAccessor = world.structureManager();
        RandomSource random = world.getRandom();
        Structure.GenerationContext context = new Structure.GenerationContext(world.registryAccess(), chunkGenerator, chunkGenerator.getBiomeSource(), world.getChunkSource().randomState(),
                structureTemplateManager, world.getSeed(), new ChunkPos(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ())), world, registryEntry -> true);

        Optional<Structure.GenerationStub> optional = JigsawPlacement.addPieces(context, structurePool, Optional.of(id), size, pos, false, Optional.empty(), new JigsawStructure.MaxDistance(512),
                PoolAliasLookup.EMPTY, JigsawStructure.DEFAULT_DIMENSION_PADDING, JigsawStructure.DEFAULT_LIQUID_SETTINGS);
        if (optional.isPresent()) {
            HashMap<Integer, ArrayList<BlockPos>> blockIdPosMap = new HashMap<Integer, ArrayList<BlockPos>>();
            ArrayList<BlockPos> chestPosList = new ArrayList<BlockPos>();
            ArrayList<BlockPos> exitPosList = new ArrayList<BlockPos>();
            ArrayList<BlockPos> gatePosList = new ArrayList<BlockPos>();
            Map<BlockPos, Integer> movingBlockMap = new HashMap<>();
            Map<BlockPos, DungeonPortalEntity.Powered> poweredBlockMap = new HashMap<>();
            HashMap<BlockPos, Integer> spawnerPosEntityIdMap = new HashMap<BlockPos, Integer>();
            Block exitBlock = BuiltInRegistries.BLOCK.byId(dungeon.getExitBlockId());
            Block bossLootBlock = BuiltInRegistries.BLOCK.byId(dungeon.getBossLootBlockId());

            StructurePiecesBuilder structurePiecesCollector = optional.get().getPiecesBuilder();
            for (StructurePiece structurePiece : structurePiecesCollector.build().pieces()) {
                if (!(structurePiece instanceof PoolElementStructurePiece poolStructurePiece)) {
                    continue;
                }
                poolStructurePiece.place(world, structureAccessor, chunkGenerator, random, BoundingBox.infinite(), pos, keepJigsaws);

                portalEntity.addDungeonEdge(poolStructurePiece.getBoundingBox().minX(), poolStructurePiece.getBoundingBox().minY(), poolStructurePiece.getBoundingBox().minZ());
                portalEntity.addDungeonEdge(poolStructurePiece.getBoundingBox().maxX(), poolStructurePiece.getBoundingBox().maxY(), poolStructurePiece.getBoundingBox().maxZ());
                for (int i = poolStructurePiece.getBoundingBox().minX(); i <= poolStructurePiece.getBoundingBox().maxX(); i++) {
                    for (int u = poolStructurePiece.getBoundingBox().minY(); u <= poolStructurePiece.getBoundingBox().maxY(); u++) {
                        for (int o = poolStructurePiece.getBoundingBox().minZ(); o <= poolStructurePiece.getBoundingBox().maxZ(); o++) {
                            BlockPos checkPos = new BlockPos(i, u, o);
                            BlockState state = world.getBlockState(checkPos);
                            if (!state.isAir()) {
                                int blockId = BuiltInRegistries.BLOCK.getId(state.getBlock());
                                if (dungeon.containsBlockId(blockId)) {
                                    if (!blockIdPosMap.containsKey(blockId)) {
                                        ArrayList<BlockPos> newList = new ArrayList<BlockPos>();
                                        newList.add(checkPos);
                                        blockIdPosMap.put(blockId, newList);
                                    } else {
                                        blockIdPosMap.get(blockId).add(checkPos);
                                    }
                                } else if (dungeon.getBossBlockId() == blockId) {
                                    portalEntity.setBossBlockPos(checkPos);
                                } else if (state.is(Blocks.CHEST) || state.is(Blocks.BARREL) || state.is(Blocks.TRAPPED_CHEST)) {
                                    chestPosList.add(checkPos);
                                } else if (state.is(exitBlock)) {
                                    exitPosList.add(checkPos);
                                } else if (state.is(bossLootBlock)) {
                                    portalEntity.setBossLootBlockPos(checkPos);
                                } else if (state.is(BlockInit.DUNGEON_SPAWNER)) {
                                    spawnerPosEntityIdMap.put(checkPos, ((DungeonSpawnerEntity) world.getBlockEntity(checkPos)).getLogic().getEntityId());
                                } else if (state.is(BlockInit.DUNGEON_GATE)) {
                                    gatePosList.add(checkPos);
                                    if (world.getBlockEntity(checkPos) != null && ((DungeonGateEntity) world.getBlockEntity(checkPos)).getUnlockItem() == null) {
                                        DungeonGateEntity dungeonGateEntity = (DungeonGateEntity) world.getBlockEntity(checkPos);
                                        dungeonGateEntity.addDungeonEdge(poolStructurePiece.getBoundingBox().minX(), poolStructurePiece.getBoundingBox().minY(),
                                                poolStructurePiece.getBoundingBox().minZ());
                                        dungeonGateEntity.addDungeonEdge(poolStructurePiece.getBoundingBox().maxX(), poolStructurePiece.getBoundingBox().maxY(),
                                                poolStructurePiece.getBoundingBox().maxZ());
                                        dungeonGateEntity.setChanged();
                                    }
                                } else if (state.getBlock() instanceof Fallable) {
                                    movingBlockMap.put(checkPos, blockId);
                                } else if (state.hasProperty(BlockStateProperties.POWERED) && !state.is(Blocks.OBSERVER)) {
                                    poweredBlockMap.put(checkPos, new DungeonPortalEntity.Powered(blockId, state.getValue(BlockStateProperties.POWERED), PropertyUtil.getHorizontalFacing(state), PropertyUtil.getBlockFacing(state)));
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
            portalEntity.setChanged();
            return true;
        }
        return false;
    }

    public static void prepareDungeon(ServerLevel dungeonWorld, DungeonPortalEntity portalEntity) {
        List<ChunkPos> chunkPosList = new ArrayList<>();
        for (int i = 0; i < portalEntity.getDungeonEdgeList().size() / 6; i++) {
            int x1 = portalEntity.getDungeonEdgeList().get(6 * i);
            int z1 = portalEntity.getDungeonEdgeList().get(2 + 6 * i);

            int x2 = portalEntity.getDungeonEdgeList().get(3 + 6 * i);
            int z2 = portalEntity.getDungeonEdgeList().get(5 + 6 * i);

            int xCount = Math.abs(x1 - x2) / 16 + ((x1 - x2) % 16 == 0 ? 0 : 1);
            int zCount = Math.abs(z1 - z2) / 16 + ((z1 - z2) % 16 == 0 ? 0 : 1);
            for (int u = 0; u < xCount; u++) {
                for (int o = 0; o < zCount; o++) {
                    ChunkPos chunkPos = new ChunkPos(SectionPos.blockToSectionCoord(x1 + 16 * u), SectionPos.blockToSectionCoord(z1 + 16 * o));
                    if (!chunkPosList.contains(chunkPos)) {
                        chunkPosList.add(chunkPos);
                    }
                }
            }
        }
        for (ChunkPos chunkPos : chunkPosList) {
            dungeonWorld.getChunkSource().addTicketWithRadius(TicketType.PORTAL, chunkPos, 1);
        }
    }

    public static void refreshDungeon(MinecraftServer server, ServerLevel world, DungeonPortalEntity portalEntity, Dungeon dungeon, String difficulty) {

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
            List<Entity> entities = world.getEntities(null,
                    new AABB(portalEntity.getDungeonEdgeList().get(6 * u), portalEntity.getDungeonEdgeList().get(1 + 6 * u), portalEntity.getDungeonEdgeList().get(2 + 6 * u),
                            portalEntity.getDungeonEdgeList().get(3 + 6 * u), portalEntity.getDungeonEdgeList().get(4 + 6 * u), portalEntity.getDungeonEdgeList().get(5 + 6 * u)));

            for (Entity entity : entities) {
                if (!(entity instanceof ServerPlayer)
                        && (entity instanceof LivingEntity || entity instanceof ItemEntity || entity instanceof Projectile)) {
                    entity.discard();
                }
            }
        }
        portalEntity.setDifficulty(difficulty);

        for (Entry<Integer, ArrayList<BlockPos>> entry : portalEntity.getBlockMap().entrySet()) {
            for (int i = 0; i < entry.getValue().size(); i++) {
                if (dungeon.getBlockIdBlockReplacementMap().get(entry.getKey()) != -1) {
                    if (dungeon.getBlockIdBlockReplacementMap().get(entry.getKey()) == 0) {
                        world.removeBlock(entry.getValue().get(i), false);
                    } else {
                        world.setBlock(entry.getValue().get(i), BuiltInRegistries.BLOCK.byId(dungeon.getBlockIdBlockReplacementMap().get(entry.getKey())).defaultBlockState(), 3);
                    }
                }
                // dungeon.getBlockIdEntitySpawnChanceMap().containsKey(blockId) &&
                if (world.getRandom().nextFloat() <= dungeon.getBlockIdEntitySpawnChanceMap().get(entry.getKey()).get(difficulty)) {
                    Mob mobEntity = createMob(world, dungeon.getBlockIdEntityMap().get(entry.getKey()).get(world.getRandom().nextInt(dungeon.getBlockIdEntityMap().get(entry.getKey()).size())),
                            null);
                    // hopefully initialize doesn't lead to problems
                    mobEntity.finalizeSpawn(world, world.getCurrentDifficultyAt(entry.getValue().get(i)), EntitySpawnReason.STRUCTURE, null);
                    mobEntity.setPersistenceRequired();
                    strengthenMob(mobEntity, dungeon, difficulty, false);
                    dungeon.getBlockIdBlockReplacementMap().get(entry.getKey());
                    mobEntity.setPos(entry.getValue().get(i).getX(), entry.getValue().get(i).getY(), entry.getValue().get(i).getZ());
                    mobEntity.setYRot(360f * world.getRandom().nextFloat());
                    mobEntity.setXRot(0.0f);
                    world.addFreshEntity(mobEntity);
                }
            }
        }

        // Refresh boss
        Mob bossEntity = createMob(world, dungeon.getBossEntityType(), dungeon.getBossNbtCompound());
        bossEntity.finalizeSpawn(world, world.getCurrentDifficultyAt(portalEntity.getBossBlockPos()), EntitySpawnReason.STRUCTURE, null);
        bossEntity.setPersistenceRequired();
        ((BossEntityAccess) bossEntity).setBoss(portalEntity.getBlockPos(), portalEntity.getLevel().dimension().identifier().toString());
        strengthenMob(bossEntity, dungeon, difficulty, true);

        if (dungeon.getBlockIdBlockReplacementMap().get(dungeon.getBossBlockId()) != -1) {
            if (dungeon.getBlockIdBlockReplacementMap().get(dungeon.getBossBlockId()) == 0) {
                world.removeBlock(portalEntity.getBossBlockPos(), false);
            } else {
                world.setBlock(portalEntity.getBossBlockPos(), BuiltInRegistries.BLOCK.byId(dungeon.getBlockIdBlockReplacementMap().get(dungeon.getBossBlockId())).defaultBlockState(), 3);
            }
        }
        bossEntity.setPos(portalEntity.getBossBlockPos().getX(), portalEntity.getBossBlockPos().getY(), portalEntity.getBossBlockPos().getZ());
        bossEntity.setYRot(360f * world.getRandom().nextFloat());
        bossEntity.setXRot(0.0f);
        world.addFreshEntity(bossEntity);

        // Refresh chests
        for (int i = 0; i < portalEntity.getChestPosList().size(); i++) {
            String lootTableString = dungeon.getDifficultyLootTableIdMap().get(difficulty).get(world.getRandom().nextInt(dungeon.getDifficultyLootTableIdMap().get(difficulty).size()));
            InventoryHelper.fillInventoryWithLoot(server, world, portalEntity.getChestPosList().get(i), lootTableString);
        }
        // Refresh exit
        for (int i = 0; i < portalEntity.getExitPosList().size(); i++) {
            world.setBlock(portalEntity.getExitPosList().get(i),
                    dungeon.getBlockIdBlockReplacementMap().containsKey(dungeon.getExitBlockId()) && dungeon.getBlockIdBlockReplacementMap().get(dungeon.getExitBlockId()) != -1
                            ? BuiltInRegistries.BLOCK.byId(dungeon.getBlockIdBlockReplacementMap().get(dungeon.getExitBlockId())).defaultBlockState()
                            : BuiltInRegistries.BLOCK.byId(dungeon.getExitBlockId()).defaultBlockState(),
                    3);
        } // Refresh gates
        for (int i = 0; i < portalEntity.getGatePosList().size(); i++) {
            world.setBlockAndUpdate(portalEntity.getGatePosList().get(i), world.getBlockState(portalEntity.getGatePosList().get(i)).cycle(DungeonGateBlock.ENABLED));
        }
        // Refresh boss loot
        world.setBlock(portalEntity.getBossLootBlockPos(),
                dungeon.getBlockIdBlockReplacementMap().containsKey(dungeon.getBossLootBlockId()) && dungeon.getBlockIdBlockReplacementMap().get(dungeon.getBossLootBlockId()) != -1
                        ? BuiltInRegistries.BLOCK.byId(dungeon.getBlockIdBlockReplacementMap().get(dungeon.getBossLootBlockId())).defaultBlockState()
                        : BuiltInRegistries.BLOCK.byId(dungeon.getBossLootBlockId()).defaultBlockState(),
                3);
        // Refresh spawner
        for (Entry<BlockPos, Integer> entry : portalEntity.getSpawnerPosEntityIdMap().entrySet()) {
            world.setBlock(entry.getKey(), BlockInit.DUNGEON_SPAWNER.defaultBlockState(), 3);
            ((DungeonSpawnerEntity) world.getBlockEntity(entry.getKey())).getLogic().setDungeonInfo(dungeon, difficulty,
                    dungeon.getSpawnerEntityIdMap().containsKey(entry.getValue()) ? dungeon.getSpawnerEntityIdMap().get(entry.getValue()) : 0, BuiltInRegistries.ENTITY_TYPE.byId(entry.getValue()));
        }
        // Refresh blocks
        for (Entry<BlockPos, Integer> entry : portalEntity.getReplaceBlockIdMap().entrySet()) {
            world.setBlock(entry.getKey(), BuiltInRegistries.BLOCK.byId(entry.getValue()).defaultBlockState(), 3);
        }
        // Refresh powered blocks
        for (Entry<BlockPos, DungeonPortalEntity.Powered> entry : portalEntity.getPoweredBlockMap().entrySet()) {
            BlockState blockState = BuiltInRegistries.BLOCK.byId(entry.getValue().getBlockId()).defaultBlockState().setValue(BlockStateProperties.POWERED, entry.getValue().getPowered());
            boolean hasFacing = blockState.hasProperty(BlockStateProperties.HORIZONTAL_FACING);
            if (hasFacing) {
                blockState = blockState.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.from2DDataValue(entry.getValue().getFacing()));
            }
            if (blockState.hasProperty(BlockStateProperties.ATTACH_FACE)) {
                blockState = blockState.setValue(BlockStateProperties.ATTACH_FACE, PropertyUtil.getBlockFacing(entry.getValue().getBlockFacing()));
            }
            world.setBlock(entry.getKey(), blockState, 3);
            world.updateNeighborsAt(entry.getKey(), world.getBlockState(entry.getKey()).getBlock());
            if (hasFacing) {
                world.updateNeighborsAt(entry.getKey().relative(world.getBlockState(entry.getKey()).getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite()), world.getBlockState(entry.getKey()).getBlock());
            }
        }
        // Refresh moving blocks
        List<BlockPos> freshPlacedBlockPoses = new ArrayList<>();
        for (Entry<BlockPos, Integer> entry : portalEntity.getMovingBlockMap().entrySet()) {
            Block block = BuiltInRegistries.BLOCK.byId(entry.getValue());
            if (!world.getBlockState(entry.getKey()).is(block)) {
                for (int i = 1; i < 50; i++) {
                    BlockPos checkPos = entry.getKey().below(i);
                    if (freshPlacedBlockPoses.contains(checkPos)) {
                        continue;
                    }
                    if (world.getBlockState(checkPos).is(block)) {
                        world.removeBlock(checkPos, false);
                        break;
                    }
                }
                world.setBlock(entry.getKey(), BuiltInRegistries.BLOCK.byId(entry.getValue()).defaultBlockState(), 3);
                freshPlacedBlockPoses.add(entry.getKey());
            }
        }
        portalEntity.getDungeonPlayerUuids().clear();
        portalEntity.getDeadDungeonPlayerUUIDs().clear();
        portalEntity.setChanged();
    }

    @Nullable
    private static Mob createMob(ServerLevel world, EntityType<?> type, @Nullable CompoundTag nbt) {
        Mob mobEntity;
        try {
            Entity entity = type.create(world, EntitySpawnReason.STRUCTURE);
            if (!(entity instanceof Mob)) {
                throw new IllegalStateException("Trying to spawn a non-mob: " + BuiltInRegistries.ENTITY_TYPE.getKey(type));
            }
            mobEntity = (Mob) entity;

        } catch (Exception exception) {
            DungeonzMain.LOGGER.warn("Failed to create mob", exception);
            return null;
        }
        if (nbt != null) {
            TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, world.registryAccess());
            mobEntity.saveWithoutId(output);
            CompoundTag nbtCompound = output.buildResult();
            nbtCompound.merge(nbt);
            mobEntity.load(TagValueInput.create(ProblemReporter.DISCARDING, world.registryAccess(), nbtCompound));
        }
        if (mobEntity.getType().builtInRegistryHolder().is(TagInit.IMMUNE_TO_ZOMBIFICATION)) {
            TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, world.registryAccess());
            mobEntity.saveWithoutId(output);
            CompoundTag nbtCompound = output.buildResult();
            nbtCompound.putBoolean("IsImmuneToZombification", true);
            mobEntity.load(TagValueInput.create(ProblemReporter.DISCARDING, world.registryAccess(), nbtCompound));
        }
        return mobEntity;
    }

    public static void strengthenMob(Mob mobEntity, Dungeon dungeon, String difficulty, boolean isBossEntity) {
        double mobHealth = mobEntity.getAttributeValue(Attributes.MAX_HEALTH);
        double mobDamage = 0.0D;
        double mobProtection = 0.0D;
        double mobSpeed = 0.0D;

        boolean hasAttackDamageAttribute = mobEntity.getAttributes().hasAttribute(Attributes.ATTACK_DAMAGE);
        boolean hasArmorAttribute = mobEntity.getAttributes().hasAttribute(Attributes.ARMOR);
        boolean hasSpeedAttribute = mobEntity.getAttributes().hasAttribute(Attributes.MOVEMENT_SPEED);

        if (hasAttackDamageAttribute) {
            mobDamage = mobEntity.getAttributeValue(Attributes.ATTACK_DAMAGE);
        }
        if (hasArmorAttribute) {
            mobProtection = mobEntity.getAttributeValue(Attributes.ARMOR);
        }
        if (hasSpeedAttribute) {
            mobSpeed = mobEntity.getAttributeValue(Attributes.MOVEMENT_SPEED);
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
        mobEntity.getAttribute(Attributes.MAX_HEALTH).setBaseValue(mobHealth);
        mobEntity.heal(mobEntity.getMaxHealth());
        if (hasAttackDamageAttribute) {
            mobEntity.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(mobDamage);
        }
        if (hasArmorAttribute) {
            mobEntity.getAttribute(Attributes.ARMOR).setBaseValue(mobProtection);
        }
        if (hasSpeedAttribute) {
            mobEntity.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(mobSpeed);
        }
        if (DungeonzMain.isRpgDifficultyLoaded) {
            HearthwindMobScaling.setMobHealthMultiplier(mobEntity, healthFactor);
        }

    }

}
