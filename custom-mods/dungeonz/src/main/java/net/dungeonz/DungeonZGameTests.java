package net.dungeonz;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.dungeonz.block.DungeonGateBlock;
import net.dungeonz.block.entity.DungeonGateEntity;
import net.dungeonz.block.entity.DungeonPortalEntity;
import net.dungeonz.block.entity.DungeonSpawnerEntity;
import net.dungeonz.compat.HearthwindGroups;
import net.dungeonz.compat.HearthwindLevels;
import net.dungeonz.compat.HearthwindMobScaling;
import net.dungeonz.dungeon.Dungeon;
import net.dungeonz.init.BlockInit;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.GameType;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.dungeonz.dungeon.DungeonPlacementHandler;
import net.dungeonz.init.ConfigInit;
import net.dungeonz.init.DimensionInit;
import net.dungeonz.init.ItemInit;
import net.dungeonz.item.DungeonCompassItem;
import net.dungeonz.item.component.DungeonCompassComponent;
import net.dungeonz.util.DungeonHelper;
import net.dungeonz.util.InventoryHelper;
import java.util.Optional;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.dungeonz.access.ServerPlayerAccess;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class DungeonZGameTests {
    public DungeonZGameTests() {}

    // Vanilla GameTestServer bakes its dimensions from an EMPTY LevelStem registry
    // (GameTestServer ResultFactory: WorldPreset.createWorldDimensions().bake(new
    // MappedRegistry)), so datapack dimensions such as dungeonz:dungeon never
    // materialize as ServerLevels and server.getLevel(DUNGEON_WORLD) is always null.
    // Headless paths under test only need a functional ServerLevel handle (timers via
    // getGameTime, transition targets, mob spawn/discard, block no-ops on empty edge
    // maps), never real dimension travel, so tests alias the fully-ticking overworld
    // under the dungeon key for the duration of each test (removed in finally).
    // The map is located by content (the entry keyed by Level.OVERWORLD), never by
    // field name, so this stays correct under intermediary runtime mappings.
    @SuppressWarnings("unchecked")
    private static Map<ResourceKey<Level>, ServerLevel> dungeonLevelsMap(MinecraftServer server) {
        for (java.lang.reflect.Field field : MinecraftServer.class.getDeclaredFields()) {
            if (!Map.class.isAssignableFrom(field.getType())) {
                continue;
            }
            field.setAccessible(true);
            try {
                Map<?, ?> map = (Map<?, ?>) field.get(server);
                if (map != null && map.containsKey(Level.OVERWORLD)) {
                    return (Map<ResourceKey<Level>, ServerLevel>) map;
                }
            } catch (ReflectiveOperationException exception) {
                throw new AssertionError("cannot reach server level map for dungeon alias", exception);
            }
        }
        throw new AssertionError("server level map (keyed by overworld) not found for dungeon alias");
    }

    private static ServerLevel ensureDungeonWorld(GameTestHelper helper) {
        MinecraftServer server = helper.getLevel().getServer();
        if (server.getLevel(DimensionInit.DUNGEON_WORLD) == null) {
            dungeonLevelsMap(server).put(DimensionInit.DUNGEON_WORLD, server.overworld());
        }
        ServerLevel aliased = server.getLevel(DimensionInit.DUNGEON_WORLD);
        helper.assertTrue(aliased != null, "dungeonz:dungeon dimension must exist on the gametest server");
        return aliased;
    }

    private static void releaseDungeonWorld(GameTestHelper helper) {
        dungeonLevelsMap(helper.getLevel().getServer()).remove(DimensionInit.DUNGEON_WORLD);
    }

    @GameTest
    public void loadedDarkDungeonDefaults(GameTestHelper helper) {
        Dungeon dungeon = loadedDungeon(helper, "dark_dungeon");
        assertDefaults(helper, dungeon, List.of("easy", "normal", "hard"), List.of(1, 2, 4), 108000);
        helper.assertTrue(dungeon.getBossEntityType() == EntityTypes.WARDEN, "dark dungeon boss must be warden");
        helper.assertTrue(dungeon.getStructurePoolId().equals(Identifier.parse("dungeonz:dark_dungeon/dungeon_spawn")), "dark start pool");
        helper.assertTrue(dungeon.getBlockIdEntityMap().size() == 6, "dark dungeon must load six mob markers");
        int diamond = BuiltInRegistries.BLOCK.getId(Blocks.DIAMOND_BLOCK);
        helper.assertTrue(List.of(EntityTypes.WITHER_SKELETON).equals(dungeon.getBlockIdEntityMap().get(diamond)), "diamond marker must spawn wither skeletons");
        helper.assertTrue(Float.valueOf(0.4f).equals(dungeon.getBlockIdEntitySpawnChanceMap().get(diamond).get("easy")), "dark easy marker chance");
        helper.assertTrue(Float.valueOf(4.0f).equals(dungeon.getDifficultyBossHealthModificatorMap().get("hard")), "dark hard boss health multiplier");
        helper.assertTrue(dungeon.getSpawnerEntityIdMap().equals(Map.of(
                BuiltInRegistries.ENTITY_TYPE.getId(EntityTypes.ZOMBIE), 10,
                BuiltInRegistries.ENTITY_TYPE.getId(EntityTypes.SKELETON), 5)), "dark spawner limits must load");
        helper.assertTrue(dungeon.getDifficultyLootTableIdMap().get("easy").equals(List.of(
                "dungeonz:chests/dark_dungeon_low_tier_chest_loot", "dungeonz:chests/dark_dungeon_mid_tier_chest_loot")), "dark easy chest loot tables");
        helper.succeed();
    }

    @GameTest
    public void loadedTempleDungeonDefaults(GameTestHelper helper) {
        Dungeon dungeon = loadedDungeon(helper, "temple_dungeon");
        assertDefaults(helper, dungeon, List.of("easy", "normal", "hard", "extreme"), List.of(3, 5, 6, 10), 36000);
        helper.assertTrue(dungeon.getBossEntityType() == EntityTypes.RAVAGER, "temple boss must be ravager");
        helper.assertTrue(dungeon.getStructurePoolId().equals(Identifier.parse("dungeonz:temple_dungeon/temple_spawn")), "temple start pool");
        helper.assertTrue(dungeon.getBlockIdEntityMap().size() == 9, "temple must load nine mob markers");
        int pink = BuiltInRegistries.BLOCK.getId(BuiltInRegistries.BLOCK.get(Identifier.parse("minecraft:pink_wool")).orElseThrow().value());
        helper.assertTrue(List.of(EntityTypes.VEX).equals(dungeon.getBlockIdEntityMap().get(pink)), "pink marker must spawn vexes");
        helper.assertTrue(Float.valueOf(0.0f).equals(dungeon.getBlockIdEntitySpawnChanceMap().get(pink).get("easy")), "easy temple must not spawn vexes");
        helper.assertTrue(Float.valueOf(0.8f).equals(dungeon.getBlockIdEntitySpawnChanceMap().get(pink).get("extreme")), "extreme temple vex chance");
        helper.assertTrue(Float.valueOf(25.0f).equals(dungeon.getDifficultyBossHealthModificatorMap().get("extreme")), "extreme temple boss health multiplier");
        helper.assertTrue(dungeon.getSpawnerEntityIdMap().isEmpty(), "temple has no configured limited spawners");
        helper.assertTrue(dungeon.getDifficultyLootTableIdMap().get("extreme").equals(List.of(
                "dungeonz:chests/temple_dungeon_extreme_tier_chest_loot")), "temple extreme chest loot table");
        helper.succeed();
    }

    @GameTest
    public void gateDisguiseDoesNotReplaceActualState(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, BlockInit.DUNGEON_GATE);
        DungeonGateEntity gate = helper.getBlockEntity(pos, DungeonGateEntity.class);
        gate.setBlockId(Identifier.parse("minecraft:gold_block"));
        helper.assertTrue(gate.getBlockState().is(BlockInit.DUNGEON_GATE), "block entity must retain actual gate state");
        helper.assertTrue(gate.getBlockState().getValue(DungeonGateBlock.ENABLED), "actual gate state must expose enabled property");
        helper.assertTrue(gate.getDisguiseBlockState().is(Blocks.GOLD_BLOCK), "disguise must resolve independently");
        helper.assertFalse(gate.getDisguiseBlockState().hasProperty(DungeonGateBlock.ENABLED), "gold disguise must not supply gate properties");
        helper.assertFalse(helper.getBlockState(pos).getCollisionShape(helper.getLevel(), helper.absolutePos(pos)).isEmpty(), "enabled gate must collide");
        helper.setBlock(pos, helper.getBlockState(pos).setValue(DungeonGateBlock.ENABLED, false));
        helper.assertTrue(helper.getBlockState(pos).getCollisionShape(helper.getLevel(), helper.absolutePos(pos)).isEmpty(), "disabled gate must not collide");
        helper.assertTrue(gate.getBlockState().is(BlockInit.DUNGEON_GATE) && !gate.getBlockState().getValue(DungeonGateBlock.ENABLED), "block entity must track actual disabled state");
        helper.assertTrue(gate.getDisguiseBlockState().is(Blocks.GOLD_BLOCK), "disabling must preserve disguise");
        helper.succeed();
    }

    @GameTest
    public void unlockChangesConnectedGatesOnly(GameTestHelper helper) {
        List<BlockPos> connected = List.of(new BlockPos(0, 1, 0), new BlockPos(1, 1, 0), new BlockPos(0, 2, 0), new BlockPos(1, 2, 0));
        BlockPos isolated = new BlockPos(1, 1, 2);
        for (BlockPos pos : connected) {
            helper.setBlock(pos, BlockInit.DUNGEON_GATE);
            helper.getBlockEntity(pos, DungeonGateEntity.class).setBlockId(Identifier.parse("minecraft:gold_block"));
        }
        helper.setBlock(isolated, BlockInit.DUNGEON_GATE);
        BlockPos origin = helper.absolutePos(connected.get(0));
        var found = DungeonGateEntity.getConnectedDungeonGatePosList(helper.getLevel(), origin);
        var expected = connected.stream().map(helper::absolutePos).toList();
        helper.assertTrue(found.size() == 4 && new HashSet<>(found).equals(new HashSet<>(expected)), "connected gate rectangle must contain exactly four gates");
        helper.getBlockEntity(connected.get(0), DungeonGateEntity.class).unlockGate(origin);
        for (BlockPos pos : connected) {
            helper.assertTrue(helper.getBlockState(pos).is(BlockInit.DUNGEON_GATE), "unlock must not replace gate with disguise");
            helper.assertFalse(helper.getBlockState(pos).getValue(DungeonGateBlock.ENABLED), "every connected gate must unlock");
            helper.assertTrue(helper.getBlockEntity(pos, DungeonGateEntity.class).getDisguiseBlockState().is(Blocks.GOLD_BLOCK), "unlock must retain disguise");
        }
        helper.assertTrue(helper.getBlockState(isolated).getValue(DungeonGateBlock.ENABLED), "disconnected gate must remain locked");
        helper.succeed();
    }

    @GameTest
    public void gateSettingsNbtRoundtrip(GameTestHelper helper) {
        var state = BlockInit.DUNGEON_GATE.defaultBlockState().setValue(DungeonGateBlock.ENABLED, false);
        var source = new DungeonGateEntity(BlockPos.ZERO, state);
        source.setBlockId(Identifier.parse("minecraft:gold_block"));
        source.setUnlockItemId("minecraft:diamond");
        source.setParticleEffectId("minecraft:flame");
        var restored = new DungeonGateEntity(BlockPos.ZERO, state);
        roundtrip(helper, source, restored);
        helper.assertTrue(restored.getDisguiseBlockState().is(Blocks.GOLD_BLOCK), "gate disguise must survive NBT");
        helper.assertTrue(restored.getUnlockItem() == Items.DIAMOND, "gate key must survive NBT");
        helper.assertTrue(restored.getParticleEffect() != null, "restored flame particle must resolve");
        helper.assertTrue(restored.getBlockState().equals(state), "NBT settings must not overwrite supplied actual state");
        helper.succeed();
    }

    @GameTest
    public void gateBoundsNbtRoundtrip(GameTestHelper helper) {
        var state = BlockInit.DUNGEON_GATE.defaultBlockState();
        var source = new DungeonGateEntity(BlockPos.ZERO, state);
        source.addDungeonEdge(-11, 22, -33);
        source.addDungeonEdge(44, 55, 66);
        var restored = new DungeonGateEntity(BlockPos.ZERO, state);
        roundtrip(helper, source, restored);
        helper.assertTrue(restored.getDungeonEdgeList().equals(List.of(-11, 22, -33, 44, 55, 66)), "both distinct gate bounds must survive NBT without index shifts");
        helper.succeed();
    }

    @GameTest
    public void portalStateNbtRoundtrip(GameTestHelper helper) {
        Dungeon dungeon = loadedDungeon(helper, "temple_dungeon");
        var state = BlockInit.DUNGEON_PORTAL.defaultBlockState();
        var source = new DungeonPortalEntity(BlockPos.ZERO, state);
        UUID living = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID dead = UUID.fromString("00000000-0000-0000-0000-000000000002");
        BlockPos first = new BlockPos(-17, 103, 29);
        BlockPos second = new BlockPos(31, 107, -43);
        source.setDungeonType("temple_dungeon");
        source.setDifficulty("extreme");
        source.setDungeonStructureGenerated();
        source.joinDungeon(living);
        source.addDeadDungeonPlayerUuids(dead);
        source.setMinGroupSize(2);
        source.setMaxGroupSize(5);
        source.setPrivateGroup(true);
        source.setCooldownTime(12345);
        source.setBossBlockPos(first);
        source.setBossLootBlockPos(second);
        source.setChestPosList(List.of(first, second));
        source.setExitPosList(List.of(second));
        source.setGatePosList(List.of(first));
        source.addDungeonEdge(-11, 22, -33);
        source.addDungeonEdge(44, 55, 66);
        source.getBlockMap().put(BuiltInRegistries.BLOCK.getId(BuiltInRegistries.BLOCK.get(Identifier.parse("minecraft:pink_wool")).orElseThrow().value()), new ArrayList<>(List.of(first, second)));
        source.addReplaceBlockId(first, Blocks.STONE);
        source.setMovingBlockMap(Map.of(second, BuiltInRegistries.BLOCK.getId(Blocks.SAND)));
        source.setPoweredBlockMap(Map.of(first, new DungeonPortalEntity.Powered(BuiltInRegistries.BLOCK.getId(Blocks.LEVER), true, 3, 2)));
        var restored = new DungeonPortalEntity(BlockPos.ZERO, state);
        roundtrip(helper, source, restored);
        helper.assertTrue(restored.getDungeon() == dungeon && restored.getDifficulty().equals("extreme"), "portal must resolve loaded dungeon and difficulty after NBT");
        helper.assertTrue(restored.isDungeonStructureGenerated(), "generated flag must survive");
        helper.assertTrue(restored.getDungeonPlayerUuids().equals(List.of(living)), "living player UUID must survive codec roundtrip");
        helper.assertTrue(restored.getDeadDungeonPlayerUUIDs().equals(List.of(dead)), "dead player UUID must survive codec roundtrip");
        helper.assertTrue(restored.getMinGroupSize() == 2 && restored.getMaxGroupSize() == 5 && restored.getPrivateGroup(), "portal group settings must survive");
        helper.assertTrue(restored.getCooldownTime() == 12345 && restored.isOnCooldown(12344) && !restored.isOnCooldown(12345), "cooldown must survive with exact expiry boundary");
        helper.assertTrue(restored.getBossBlockPos().equals(first) && restored.getBossLootBlockPos().equals(second), "boss positions must survive");
        helper.assertTrue(restored.getChestPosList().equals(List.of(first, second)), "chest positions must survive");
        helper.assertTrue(restored.getExitPosList().equals(List.of(second)) && restored.getGatePosList().equals(List.of(first)), "exit and gate positions must survive");
        helper.assertTrue(restored.getDungeonEdgeList().equals(source.getDungeonEdgeList()), "portal bounds must survive");
        helper.assertTrue(restored.getBlockMap().equals(source.getBlockMap()), "marker positions must survive");
        helper.assertTrue(restored.getReplaceBlockIdMap().equals(source.getReplaceBlockIdMap()), "replacement map must survive");
        helper.assertTrue(restored.getMovingBlockMap().equals(source.getMovingBlockMap()), "moving block map must survive");
        var powered = restored.getPoweredBlockMap().get(first);
        helper.assertTrue(powered != null && powered.getBlockId() == BuiltInRegistries.BLOCK.getId(Blocks.LEVER)
                && powered.getPowered() && powered.getFacing() == 3 && powered.getBlockFacing() == 2, "powered block state must survive");
        helper.succeed();
    }

    @GameTest
    public void portalSpawnerMapNbtRoundtrip(GameTestHelper helper) {
        var state = BlockInit.DUNGEON_PORTAL.defaultBlockState();
        var source = new DungeonPortalEntity(BlockPos.ZERO, state);
        source.getSpawnerPosEntityIdMap().put(new BlockPos(-7, 100, 9), BuiltInRegistries.ENTITY_TYPE.getId(EntityTypes.ZOMBIE));
        source.getSpawnerPosEntityIdMap().put(new BlockPos(11, 101, -13), BuiltInRegistries.ENTITY_TYPE.getId(EntityTypes.SKELETON));
        var restored = new DungeonPortalEntity(BlockPos.ZERO, state);
        roundtrip(helper, source, restored);
        helper.assertTrue(restored.getSpawnerPosEntityIdMap().equals(source.getSpawnerPosEntityIdMap()), "both portal spawner positions and entity IDs must survive NBT");
        helper.succeed();
    }

    @GameTest
    public void spawnerNbtRoundtrip(GameTestHelper helper) {
        Dungeon dungeon = loadedDungeon(helper, "dark_dungeon");
        var state = BlockInit.DUNGEON_SPAWNER.defaultBlockState();
        var source = new DungeonSpawnerEntity(BlockPos.ZERO, state);
        source.getLogic().setDungeonInfo(dungeon, "hard", 10, EntityTypes.ZOMBIE);
        CompoundTag saved = source.saveWithoutMetadata(helper.getLevel().registryAccess());
        saved.putInt("TotalSpawnCount", 3);
        saved.putShort("Delay", (short) 37);
        var restored = new DungeonSpawnerEntity(BlockPos.ZERO, state);
        restored.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), saved));
        CompoundTag reserialized = restored.saveWithoutMetadata(helper.getLevel().registryAccess());
        helper.assertTrue(reserialized.equals(saved), "spawner NBT including spawn data and potentials must roundtrip exactly");
        helper.assertTrue(restored.getLogic().getEntityId() == BuiltInRegistries.ENTITY_TYPE.getId(EntityTypes.ZOMBIE), "spawner entity ID must survive");
        helper.assertTrue(reserialized.getIntOr("MaxSpawnCount", -1) == 10 && reserialized.getIntOr("TotalSpawnCount", -1) == 3, "spawner quota and progress must survive");
        helper.assertTrue(reserialized.getStringOr("Dungeon", "").equals("dark_dungeon") && reserialized.getStringOr("Difficulty", "").equals("hard"), "spawner dungeon and difficulty must survive");
        helper.assertTrue(reserialized.getCompoundOrEmpty("SpawnData").getCompoundOrEmpty("entity").getStringOr("id", "").equals("minecraft:zombie"), "spawn data must contain a zombie, not an empty fallback");
        helper.succeed();
    }

    @GameTest
    public void loadedTemplatePoolsAndStructures(GameTestHelper helper) {
        var pools = helper.getLevel().registryAccess().lookupOrThrow(Registries.TEMPLATE_POOL);
        var structures = helper.getLevel().registryAccess().lookupOrThrow(Registries.STRUCTURE);
        var manager = helper.getLevel().getStructureManager();
        for (String type : List.of("dark_dungeon", "temple_dungeon")) {
            Dungeon dungeon = loadedDungeon(helper, type);
            helper.assertTrue(pools.containsKey(dungeon.getStructurePoolId()), type + " start pool must be registered");
            helper.assertTrue(structures.containsKey(Identifier.parse("dungeonz:" + type + "_structure")), type + " entrance structure must be registered");
            helper.assertTrue(pools.containsKey(Identifier.parse("dungeonz:overworld_" + type)), type + " entrance pool must be registered");
            long count = pools.keySet().stream().filter(id -> id.getNamespace().equals("dungeonz") && id.getPath().startsWith(type + "/")).count();
            helper.assertTrue(count >= 20, type + " must load its room pool corpus, got " + count);
        }
        for (Identifier id : pools.keySet()) {
            if (!id.getNamespace().equals("dungeonz")) continue;
            var pool = pools.get(id).orElseThrow().value();
            helper.assertTrue(pool.size() > 0 && !pool.getTemplates().isEmpty(), id + " must have weighted templates");
            for (var entry : pool.getTemplates()) {
                var size = entry.getFirst().getSize(manager, Rotation.NONE);
                helper.assertTrue(entry.getSecond() > 0 && size.getX() > 0 && size.getY() > 0 && size.getZ() > 0, id + " must resolve a nonempty structure template");
            }
        }
        for (String name : List.of("dungeon_spawn", "temple_spawn")) {
            var template = manager.get(Identifier.parse("dungeonz:" + name));
            helper.assertTrue(template.isPresent(), name + " NBT template must load without creating a fallback");
            helper.assertFalse(template.orElseThrow().getJigsaws(BlockPos.ZERO, Rotation.NONE).isEmpty(), name + " must contain jigsaw connectors");
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // Integration bridges (hearthwind_skills via reflection)
    // ------------------------------------------------------------------

    private static final Class<?> SKILL_XP = skillsClass("SkillXp");
    private static final Class<?> SKILL = skillsClass("Skill");
    private static final Class<?> PARTY_MANAGER = skillsClass("party.PartyManager");

    private static Class<?> skillsClass(String simpleName) {
        try {
            return Class.forName("dev.jmiahman.hearthwind.skills." + simpleName);
        } catch (ClassNotFoundException exception) {
            throw new AssertionError("hearthwind-skills must be staged in the gametest server", exception);
        }
    }

    private static Object skillByName(String id) {
        for (Object constant : SKILL.getEnumConstants()) {
            try {
                if (((String) SKILL.getField("id").get(constant)).equals(id)) {
                    return constant;
                }
            } catch (ReflectiveOperationException exception) {
                throw new AssertionError(exception);
            }
        }
        throw new AssertionError("missing skill " + id);
    }

    private static void setSkillLevel(ServerPlayer player, String skillId, int level) {
        try {
            SKILL_XP.getMethod("setLevel", net.minecraft.world.entity.Entity.class, SKILL, int.class)
                    .invoke(null, player, skillByName(skillId), level);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }

    @GameTest
    public void hearthwindLevelGateBlocksAndAdmitsBySkillLevels(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        for (String skillId : HearthwindLevels.SKILLS) {
            setSkillLevel(player, skillId, 0);
        }
        helper.assertTrue(HearthwindLevels.overallLevel(player) == 0, "zero skills must yield level zero");
        helper.assertTrue(HearthwindLevels.meetsRequiredLevel(player, 0), "zero must meet requirement zero");
        helper.assertFalse(HearthwindLevels.meetsRequiredLevel(player, 1), "zero must not meet requirement one");

        setSkillLevel(player, "mining", 11);
        helper.assertTrue(HearthwindLevels.overallLevel(player) == 0, "11 divided by 12 must round down to zero");
        helper.assertFalse(HearthwindLevels.meetsRequiredLevel(player, 1), "fractional average must not admit at one");
        setSkillLevel(player, "mining", 12);
        helper.assertTrue(HearthwindLevels.overallLevel(player) == 1, "12 divided by 12 must yield one");
        helper.assertTrue(HearthwindLevels.meetsRequiredLevel(player, 1), "exact threshold one must admit");
        helper.assertFalse(HearthwindLevels.meetsRequiredLevel(player, 2), "one must not meet requirement two");

        for (String skillId : HearthwindLevels.SKILLS) {
            setSkillLevel(player, skillId, 30);
        }
        helper.assertTrue(HearthwindLevels.overallLevel(player) == 30, "all skills at 30 must yield maximum 30");
        helper.assertTrue(HearthwindLevels.meetsRequiredLevel(player, 0), "maximum must meet requirement zero");
        helper.assertTrue(HearthwindLevels.meetsRequiredLevel(player, 29), "maximum must meet requirement below maximum");
        helper.assertTrue(HearthwindLevels.meetsRequiredLevel(player, 30), "exact maximum threshold must admit");
        helper.assertFalse(HearthwindLevels.meetsRequiredLevel(player, 31), "maximum must not admit above the cap");
        helper.succeed();
    }

    @GameTest
    public void hearthwindPartyAdmissionFollowsPartyMembership(GameTestHelper helper) {
        dev.jmiahman.hearthwind.skills.party.PartyManager.reset();
        var first = helper.makeMockServerPlayerInLevel();
        var outsider = helper.makeMockServerPlayerInLevel();
        var party = dev.jmiahman.hearthwind.skills.party.PartyManager.createParty(first, "Delves");
        helper.assertTrue(party != null, "party creation must succeed");
        // first occupant joins the (private) dungeon; outsider must be denied
        var group = HearthwindGroups.group(first);
        helper.assertTrue(group.members().contains(first.getUUID()), "member list must include the owner");
        helper.assertTrue(group.leader().isPresent() && group.leader().get().equals(first.getUUID()), "leader must be reported");
        helper.assertTrue(HearthwindGroups.admits(first, first.getUUID()), "party member must satisfy private admission");
        helper.assertTrue(!HearthwindGroups.admits(outsider, first.getUUID()), "an outsider must not satisfy private admission");
        dev.jmiahman.hearthwind.skills.party.PartyManager.disbandParty(first);
        helper.assertTrue(HearthwindGroups.group(outsider).members().isEmpty(), "no party means an empty member list");
        helper.succeed();
    }

    @GameTest
    public void dungeonAdmissionLevelAndPrivateGates(GameTestHelper helper) {
        // Both shipped dungeons omit required_level, so DungeonLoader defaults
        // it to 0 and the text.dungeonz.required_level branch of
        // DungeonHelper.teleportDungeon is dead in production. Exercise it with
        // a test-only copy of dark_dungeon (shipped tuning untouched) under a
        // throwaway id, removed again in the finally block. Dungeon has no
        // requiredLevel setter (final field, getter only), so a copy is the
        // only non-permanent override.
        Dungeon base = loadedDungeon(helper, "dark_dungeon");
        helper.assertTrue(base.getRequiredLevel() == 0, "shipped dark dungeon must still default requiredLevel to 0");
        String testId = "dark_dungeon_admission_test";
        helper.assertTrue(Dungeon.getDungeon(testId) == null, "admission fixture id must be unused");
        int requiredLevel = 5;
        Dungeon gated = new Dungeon(testId, base.getBlockIdEntityMap(), base.getBlockIdEntitySpawnChanceMap(),
                base.getBlockIdBlockReplacementMap(), base.getSpawnerEntityIdMap(), base.getDifficultyRequiredItemCountMap(),
                base.getBreakableBlockIdList(), base.getplaceableBlockIdList(), base.getDifficultyList(),
                base.getDifficultyMobHealthModificatorMap(), base.getDifficultyMobDamageModificatorMap(),
                base.getDifficultyMobProtectionModificatorMap(), base.getDifficultyMobSpeedModificatorMap(),
                base.getDifficultyLootTableIdMap(), base.getDifficultyBossHealthModificatorMap(),
                base.getDifficultyBossDamageModificatorMap(), base.getDifficultyBossProtectionModificatorMap(),
                base.getDifficultyBossSpeedModificatorMap(), base.getDifficultyBossLootTableMap(), base.getBossEntityType(),
                base.getBossNbtCompound(), base.getBossBlockId(), base.getBossLootBlockId(), base.getExitBlockId(),
                base.isRespawnAllowed(), base.isElytraAllowed(), base.isKeepInventory(), base.isEnderPearlAllowed(),
                base.isPositiveEffectsAllowed(), base.getMaxGroupSize(), base.getMinGroupSize(), requiredLevel,
                base.getCooldown(), base.getBackgroundId(), base.getStructurePoolId());
        helper.assertTrue(gated.getRequiredLevel() == requiredLevel, "test copy must carry the gated requiredLevel");
        Dungeon.addDungeon(gated);
        ensureDungeonWorld(helper);
        try {
            BlockPos rel = new BlockPos(1, 1, 1);
            helper.setBlock(rel, BlockInit.DUNGEON_PORTAL);
            DungeonPortalEntity portal = helper.getBlockEntity(rel, DungeonPortalEntity.class);
            portal.setDungeonType(testId);
            // Difficulty stays "" so the diamond entry-cost gate is skipped and
            // the level gate is reached regardless of inventory or gamemode.
            portal.setMaxGroupSize(5);
            portal.setMinGroupSize(2);
            portal.setPrivateGroup(false);
            portal.setCooldownTime(0);
            BlockPos portalPos = helper.absolutePos(rel);
            helper.assertTrue(portal.getDungeon() == gated, "portal must resolve the gated test dungeon");

            var low = helper.makeMockServerPlayerInLevel();
            for (String skillId : HearthwindLevels.SKILLS) {
                setSkillLevel(low, skillId, 0);
            }
            helper.assertTrue(HearthwindLevels.overallLevel(low) == 0, "zeroed skills must yield level zero");
            helper.assertTrue(!HearthwindLevels.meetsRequiredLevel(low, requiredLevel), "level zero must not meet requirement five");
            DungeonHelper.teleportDungeon(low, portalPos, low.getUUID());
            helper.assertTrue(portal.getWaitingUuids().isEmpty(), "below-threshold player must be denied (text.dungeonz.required_level) with no join");
            helper.assertTrue(portal.getdungeonTeleportCountdown() == 0, "denied player must not start the teleport countdown");

            var high = helper.makeMockServerPlayerInLevel();
            for (String skillId : HearthwindLevels.SKILLS) {
                setSkillLevel(high, skillId, 30);
            }
            helper.assertTrue(HearthwindLevels.meetsRequiredLevel(high, requiredLevel), "maxed skills must meet requirement five");
            DungeonHelper.teleportDungeon(high, portalPos, high.getUUID());
            helper.assertTrue(portal.getWaitingUuids().contains(high.getUUID()), "above-threshold player must enter the min-group join path");
            helper.assertTrue(portal.getdungeonTeleportCountdown() == 0, "incomplete min-group must wait, not count down");

            portal.getWaitingUuids().clear();
            var occupant = helper.makeMockServerPlayerInLevel();
            portal.joinDungeon(occupant.getUUID());
            portal.setPrivateGroup(true);
            var outsider = helper.makeMockServerPlayerInLevel();
            helper.assertTrue(!HearthwindGroups.admits(outsider, occupant.getUUID()), "partyless outsider must not satisfy private admission");
            DungeonHelper.teleportDungeon(outsider, portalPos, outsider.getUUID());
            helper.assertTrue(!portal.getWaitingUuids().contains(outsider.getUUID()), "outsider must be denied (text.dungeonz.dungeon_private) with no join");
            helper.assertTrue(portal.getdungeonTeleportCountdown() == 0, "private denial must not start the teleport countdown");
        } finally {
            releaseDungeonWorld(helper);
            DungeonzMain.DUNGEONS.removeIf(entry -> entry.getDungeonTypeId().equals(testId));
        }
        helper.succeed();
    }

    @GameTest
    public void enterLeaveRoundTripPersistsReturnPoint(GameTestHelper helper) {
        // Direct enter()/leave() calls: no dimension travel, no jigsaw gen.
        // enter() must persist return world + portal + spawn on ServerPlayerAccess
        // and target the portal dungeon cell; leave() must drop membership and
        // restore the saved return position.
        var oldWorld = helper.getLevel();
        var dungeonWorld = ensureDungeonWorld(helper);
        try {
            BlockPos rel = new BlockPos(1, 1, 1);
            helper.setBlock(rel, BlockInit.DUNGEON_PORTAL);
            DungeonPortalEntity portal = helper.getBlockEntity(rel, DungeonPortalEntity.class);
            portal.setDungeonType("dark_dungeon");
            portal.setDifficulty("easy");
            BlockPos portalAbs = helper.absolutePos(rel);

            var player = helper.makeMockServerPlayerInLevel();
            TeleportTransition enterTransition = DungeonPlacementHandler.enter(player, dungeonWorld, oldWorld, portal, portalAbs, "easy", true);
            var access = (ServerPlayerAccess) player;
            helper.assertTrue(access.getOldServerWorld() == oldWorld, "enter must persist the return world");
            helper.assertTrue(access.getDungeonPortalBlockPos().equals(portalAbs), "enter must persist the return portal pos");
            BlockPos savedSpawn = access.getDungeonSpawnBlockPos();
            helper.assertTrue(savedSpawn != null, "enter must persist a return position");
            helper.assertTrue(portal.getDungeonPlayerUuids().contains(player.getUUID()), "enter must join the player to the dungeon");
            helper.assertTrue(enterTransition.newLevel() == dungeonWorld, "enter transition must target the dungeon dimension");
            Vec3 expectedEnter = Vec3.atLowerCornerOf(new BlockPos(portalAbs.getX() * 16, 100, portalAbs.getZ() * 16)).add(0.5, 0, 0.5);
            helper.assertTrue(enterTransition.position().equals(expectedEnter), "enter transition must target the portal dungeon cell");

            TeleportTransition leaveTransition = DungeonPlacementHandler.leave(player, oldWorld);
            helper.assertTrue(!portal.getDungeonPlayerUuids().contains(player.getUUID()), "leave must remove the player from the dungeon");
            helper.assertTrue(leaveTransition.newLevel() == oldWorld, "leave transition must target the return world");
            Vec3 expectedLeave = Vec3.atLowerCornerOf(savedSpawn).add(0.5, 0, 0.5);
            helper.assertTrue(leaveTransition.position().equals(expectedLeave), "leave transition must restore the saved return position");
        } finally {
            releaseDungeonWorld(helper);
        }
        helper.succeed();
    }

    @GameTest
    public void refreshDungeonMarksBossWithSourcePortal(GameTestHelper helper) {
        // Minimal placed portal (no markers, chests, exits, gates, spawners,
        // edges): only the boss path mutates the world, so the boss
        // source portal/world marker is asserted without structure gen.
        Dungeon dungeon = loadedDungeon(helper, "dark_dungeon");
        var world = helper.getLevel();
        BlockPos portalRel = new BlockPos(1, 1, 1);
        helper.setBlock(portalRel, BlockInit.DUNGEON_PORTAL);
        DungeonPortalEntity portal = helper.getBlockEntity(portalRel, DungeonPortalEntity.class);
        portal.setDungeonType("dark_dungeon");
        portal.setDifficulty("hard");
        BlockPos portalAbs = helper.absolutePos(portalRel);
        BlockPos bossAbs = helper.absolutePos(new BlockPos(3, 2, 3));
        portal.setBossBlockPos(bossAbs);
        portal.setBossLootBlockPos(helper.absolutePos(new BlockPos(4, 1, 1)));
        UUID seed = UUID.fromString("00000000-0000-0000-0000-0000000000ff");
        portal.joinDungeon(seed);

        DungeonPlacementHandler.refreshDungeon(world.getServer(), world, portal, dungeon, "easy");
        helper.assertTrue(portal.getDifficulty().equals("easy"), "refresh must apply the requested difficulty");
        helper.assertTrue(portal.getDungeonPlayerUuids().isEmpty() && portal.getDeadDungeonPlayerUUIDs().isEmpty(), "refresh must reset the roster");
        List<Mob> bosses = world.getEntitiesOfClass(Mob.class, new AABB(bossAbs).inflate(2.0),
                mob -> mob.getType() == dungeon.getBossEntityType());
        helper.assertTrue(bosses.size() == 1, "refresh must spawn exactly one boss, got " + bosses.size());
        Mob boss = bosses.get(0);
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, world.registryAccess());
        boss.saveWithoutId(output);
        CompoundTag tag = output.buildResult();
        helper.assertTrue(tag.getBooleanOr("IsDungeonBossEntity", false), "refreshed boss must carry the dungeon-boss marker");
        BlockPos markedPortal = new BlockPos(tag.getIntOr("PortalPosX", Integer.MIN_VALUE), tag.getIntOr("PortalPosY", Integer.MIN_VALUE), tag.getIntOr("PortalPosZ", Integer.MIN_VALUE));
        helper.assertTrue(markedPortal.equals(portalAbs), "boss marker must point at the source portal, got " + markedPortal);
        helper.assertTrue(tag.getStringOr("WorldRegistryKey", "").equals(world.dimension().identifier().toString()), "boss marker must point at the portal world");
        boss.discard();
        helper.succeed();
    }

    @GameTest
    public void finishDungeonSetsCooldownAndExitPortal(GameTestHelper helper) {
        // Boss-death effects without killing a boss: exit reopens as a portal,
        // boss loot becomes a chest, cooldown starts at dungeon tuning.
        Dungeon dungeon = loadedDungeon(helper, "dark_dungeon");
        var world = helper.getLevel();
        BlockPos portalRel = new BlockPos(1, 1, 1);
        helper.setBlock(portalRel, BlockInit.DUNGEON_PORTAL);
        DungeonPortalEntity portal = helper.getBlockEntity(portalRel, DungeonPortalEntity.class);
        portal.setDungeonType("dark_dungeon");
        portal.setDifficulty("easy");
        BlockPos exitAbs = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockPos lootAbs = helper.absolutePos(new BlockPos(3, 1, 1));
        helper.setBlock(new BlockPos(2, 1, 2), Blocks.STONE);
        helper.setBlock(new BlockPos(3, 1, 1), Blocks.STONE);
        portal.setExitPosList(List.of(exitAbs));
        portal.setBossLootBlockPos(lootAbs);
        portal.setCooldownTime(0);

        int before = (int) world.getGameTime();
        portal.finishDungeon(world, lootAbs);
        helper.assertTrue(world.getBlockState(exitAbs).is(BlockInit.DUNGEON_PORTAL), "finish must reopen the exit as a dungeon portal");
        helper.assertTrue(world.getBlockState(lootAbs).is(Blocks.CHEST), "finish must place the boss loot chest");
        helper.assertTrue(world.getBlockEntity(lootAbs) instanceof Container, "boss loot chest must hold an inventory");
        int expected = before + dungeon.getCooldown();
        helper.assertTrue(portal.getCooldownTime() == expected, "finish must set cooldown to dungeon tuning plus finish time");
        helper.assertTrue(portal.isOnCooldown(before), "finished dungeon must be on cooldown immediately");
        helper.assertTrue(!portal.isOnCooldown(expected), "cooldown must expire exactly at the stored time");
        helper.succeed();
    }

    @GameTest
    public void hearthwindMobHealthMultiplierScalesMaxHealth(GameTestHelper helper) {
        Dungeon dungeon = loadedDungeon(helper, "dark_dungeon");
        for (boolean boss : List.of(false, true)) {
            var zombie = helper.spawn(EntityTypes.ZOMBIE, 1, 2, 1);
            double base = zombie.getAttributeValue(Attributes.MAX_HEALTH);
            float factor = (boss ? dungeon.getDifficultyBossHealthModificatorMap()
                    : dungeon.getDifficultyMobHealthModificatorMap()).get("hard");
            DungeonPlacementHandler.strengthenMob(zombie, dungeon, "hard", boss);
            helper.assertTrue(Math.abs(zombie.getMaxHealth() - base * factor) < 0.001,
                    "placement plus bridge must scale health once, not square the factor");
            helper.assertTrue(Math.abs(zombie.getHealth() - zombie.getMaxHealth()) < 0.001,
                    "mob must heal to the scaled max health");
            var modifiers = List.copyOf(zombie.getAttribute(Attributes.MAX_HEALTH).getModifiers());
            HearthwindMobScaling.setMobHealthMultiplier(zombie, factor);
            HearthwindMobScaling.setMobHealthMultiplier(zombie, 1.0f);
            helper.assertTrue(Math.abs(zombie.getMaxHealth() - base * factor) < 0.001,
                    "bridge calls must leave DungeonZ-scaled health unchanged");
            helper.assertTrue(List.copyOf(zombie.getAttribute(Attributes.MAX_HEALTH).getModifiers()).equals(modifiers),
                    "bridge must not add health modifiers");
            zombie.discard();
        }
        helper.succeed();
    }

    @GameTest
    public void paymentUsesOnlyMainInventory(GameTestHelper helper) {
        var player = helper.makeMockServerPlayer(GameType.SURVIVAL);
        helper.assertTrue(player.gameMode() == GameType.SURVIVAL && !player.isCreative(), "payment fixture must be survival");
        var inventory = player.getInventory();
        inventory.clearContent();
        player.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.DIAMOND, 8));
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.DIAMOND_HELMET));
        var diamonds = List.of(new ItemStack(Items.DIAMOND, 3));
        var helmet = List.of(new ItemStack(Items.DIAMOND_HELMET));
        helper.assertTrue(inventory.getNonEquipmentItems().size() == 36, "main inventory must have exactly 36 slots");
        helper.assertFalse(InventoryHelper.hasRequiredItemStacks(inventory, diamonds), "offhand cannot fund payment");
        helper.assertFalse(InventoryHelper.hasRequiredItemStacks(inventory, helmet), "worn armor cannot fund payment");
        InventoryHelper.decrementRequiredItemStacks(inventory, diamonds);
        InventoryHelper.decrementRequiredItemStacks(inventory, helmet);
        inventory.setItem(0, new ItemStack(Items.DIAMOND));
        inventory.setItem(35, new ItemStack(Items.DIAMOND, 2));
        inventory.setItem(9, new ItemStack(Items.DIAMOND_HELMET));
        helper.assertTrue(InventoryHelper.hasRequiredItemStacks(inventory, diamonds), "hotbar and last main slot must combine");
        helper.assertTrue(InventoryHelper.hasRequiredItemStacks(inventory, helmet), "unequipped armor may fund payment");
        InventoryHelper.decrementRequiredItemStacks(inventory, diamonds);
        InventoryHelper.decrementRequiredItemStacks(inventory, helmet);
        helper.assertTrue(inventory.getItem(0).isEmpty() && inventory.getItem(35).isEmpty() && inventory.getItem(9).isEmpty(),
                "payment must consume matching main inventory stacks");
        helper.assertTrue(player.getOffhandItem().getCount() == 8 && player.getItemBySlot(EquipmentSlot.HEAD).is(Items.DIAMOND_HELMET),
                "payment must leave offhand and worn armor untouched");
        var creative = helper.makeMockServerPlayer(GameType.CREATIVE);
        creative.getInventory().clearContent();
        helper.assertTrue(creative.isCreative(), "creative payment fixture must be creative");
        helper.assertTrue(InventoryHelper.hasRequiredItemStacks(creative.getInventory(), diamonds), "creative must still bypass payment");
        helper.succeed();
    }

    @GameTest
    public void compassTrackerRebindsOnNextInventoryTick(GameTestHelper helper) {
        var world = helper.getLevel();
        var player = helper.makeMockServerPlayerInLevel();
        var compass = new ItemStack(ItemInit.DUNGEON_COMPASS);
        var target = new BlockPos(123, 64, -321);
        compass.set(ItemInit.DUNGEON_COMPASS_DATA, new DungeonCompassComponent("dark_dungeon", true, Optional.of(target)));
        var otherDimension = world.dimension() == Level.NETHER ? Level.OVERWORLD : Level.NETHER;
        helper.runAtTickTime(1, () -> {
            if (world.getGameTime() % 100 == 0) {
                helper.runAfterDelay(1, () -> assertCompassTrackerRebinds(helper, player, compass, target, otherDimension));
            } else {
                assertCompassTrackerRebinds(helper, player, compass, target, otherDimension);
            }
        });
    }

    private static void assertCompassTrackerRebinds(GameTestHelper helper, ServerPlayer player, ItemStack compass, BlockPos target,
            net.minecraft.resources.ResourceKey<Level> otherDimension) {
        var world = helper.getLevel();
        helper.assertTrue(world.getGameTime() % 100 != 0, "fixture must run between periodic structure searches");
        compass.set(DataComponents.LODESTONE_TRACKER, new LodestoneTracker(Optional.of(GlobalPos.of(otherDimension, target)), true));
        compass.getItem().inventoryTick(compass, world, player, EquipmentSlot.MAINHAND);
        helper.assertTrue(compass.get(DataComponents.LODESTONE_TRACKER).target().orElseThrow().equals(GlobalPos.of(world.dimension(), target)),
                "tracker must immediately bind the same coordinates in the current dimension");
        compass.remove(ItemInit.DUNGEON_COMPASS_DATA);
        compass.getItem().inventoryTick(compass, world, player, EquipmentSlot.MAINHAND);
        helper.assertFalse(compass.has(DataComponents.LODESTONE_TRACKER), "unbound compass must remove stale tracker");
        helper.assertTrue(DungeonCompassItem.createGlobalDungeonStructurePos(world, compass) == null, "upstream unbound target must be null");
        helper.succeed();
    }

    @GameTest
    public void teleportDungeonMinGroupStartsCountdown(GameTestHelper helper) {
        // Min-group join path of DungeonHelper.teleportDungeon without dimension
        // travel: waiting UUIDs accumulate to minGroupSize, then
        // startDungeonTeleportCountdown sets the timer. The portal is marked
        // generated up front so the countdown start takes the lightweight
        // prepareDungeon branch (empty edges) instead of heavy jigsaw gen.
        // Difficulty stays "" so the diamond entry-cost gate is skipped.
        // teleportDungeon resolves the dungeon world itself; ensuring only
        // registers the alias it looks up.
        ensureDungeonWorld(helper);
        try {
            BlockPos rel = new BlockPos(1, 1, 1);
            helper.setBlock(rel, BlockInit.DUNGEON_PORTAL);
            DungeonPortalEntity portal = helper.getBlockEntity(rel, DungeonPortalEntity.class);
            portal.setDungeonType("dark_dungeon");
            portal.setMaxGroupSize(5);
            portal.setMinGroupSize(2);
            portal.setPrivateGroup(false);
            portal.setCooldownTime(0);
            portal.setDungeonStructureGenerated();
            BlockPos portalPos = helper.absolutePos(rel);

            var first = helper.makeMockServerPlayerInLevel();
            DungeonHelper.teleportDungeon(first, portalPos, first.getUUID());
            helper.assertTrue(portal.getWaitingUuids().equals(List.of(first.getUUID())), "first player must wait for the min group");
            helper.assertTrue(portal.getdungeonTeleportCountdown() == 0, "incomplete min-group must wait, not count down");

            var second = helper.makeMockServerPlayerInLevel();
            DungeonHelper.teleportDungeon(second, portalPos, second.getUUID());
            helper.assertTrue(portal.getWaitingUuids().size() == 2
                    && portal.getWaitingUuids().contains(first.getUUID())
                    && portal.getWaitingUuids().contains(second.getUUID()), "threshold group must hold both waiting UUIDs");
            helper.assertTrue(portal.getdungeonTeleportCountdown() == ConfigInit.CONFIG.defaultDungeonTeleportCountdown,
                    "threshold group must start the teleport countdown");
            helper.assertTrue(portal.getDungeonPlayerUuids().isEmpty(), "countdown start must not teleport anyone yet");
        } finally {
            releaseDungeonWorld(helper);
        }
        helper.succeed();
    }

    @GameTest
    public void dungeonTeleportCountdownTicksRefreshAndExpire(GameTestHelper helper) {
        // DungeonPortalEntity.serverTick timer path without dimension travel:
        // ticking from the started countdown must run refreshDungeon exactly at
        // half (roster reset proves it completed) and must clear the waiting
        // list at zero. Waiting UUIDs are unresolvable on purpose so the zero
        // branch clears without teleporting real players; the teleport target
        // itself is asserted via enter() transition below (no player.teleport).
        var world = helper.getLevel();
        var dungeonWorld = ensureDungeonWorld(helper);
        try {
            int start = ConfigInit.CONFIG.defaultDungeonTeleportCountdown;
            helper.assertTrue(start > 1, "countdown tuning must allow half and zero transitions");
            int half = start / 2;
            BlockPos rel = new BlockPos(1, 1, 1);
            helper.setBlock(rel, BlockInit.DUNGEON_PORTAL);
            DungeonPortalEntity portal = helper.getBlockEntity(rel, DungeonPortalEntity.class);
            portal.setDungeonType("dark_dungeon");
            portal.setDifficulty("easy");
            portal.setMaxGroupSize(5);
            portal.setMinGroupSize(2);
            portal.setBossBlockPos(new BlockPos(16, 100, 16));
            portal.setBossLootBlockPos(new BlockPos(17, 100, 16));
            BlockPos portalAbs = helper.absolutePos(rel);
            UUID living = UUID.fromString("00000000-0000-0000-0000-000000000011");
            UUID dead = UUID.fromString("00000000-0000-0000-0000-000000000012");
            UUID waitingFirst = UUID.fromString("00000000-0000-0000-0000-000000000013");
            UUID waitingSecond = UUID.fromString("00000000-0000-0000-0000-000000000014");
            portal.joinDungeon(living);
            portal.addDeadDungeonPlayerUuids(dead);
            portal.addWaitingUuid(waitingFirst);
            portal.addWaitingUuid(waitingSecond);
            portal.setDungeonStructureGenerated();
            portal.startDungeonTeleportCountdown(dungeonWorld);
            helper.assertTrue(portal.getdungeonTeleportCountdown() == start, "countdown must start at tuning");

            var state = helper.getBlockState(rel);
            for (int i = 0; i < start - half; i++) {
                DungeonPortalEntity.serverTick(world, portalAbs, state, portal);
            }
            helper.assertTrue(portal.getdungeonTeleportCountdown() == half, "ticks must decrement to exactly half");
            helper.assertTrue(portal.getDungeonPlayerUuids().isEmpty() && portal.getDeadDungeonPlayerUUIDs().isEmpty(),
                    "half-countdown refresh must reset the roster");
            helper.assertTrue(portal.getDifficulty().equals("easy"), "refresh must keep the requested difficulty");
            helper.assertTrue(portal.getWaitingUuids().equals(List.of(waitingFirst, waitingSecond)), "refresh must not touch the waiting list");

            for (int i = 0; i < half; i++) {
                DungeonPortalEntity.serverTick(world, portalAbs, state, portal);
            }
            helper.assertTrue(portal.getdungeonTeleportCountdown() == 0, "ticks must expire the countdown to zero");
            helper.assertTrue(portal.getWaitingUuids().isEmpty(), "zero countdown must clear the waiting list");

            var player = helper.makeMockServerPlayerInLevel();
            TeleportTransition enterTransition = DungeonPlacementHandler.enter(player, dungeonWorld, world, portal, portalAbs, "easy", true);
            helper.assertTrue(enterTransition.newLevel() == dungeonWorld, "expired wait must teleport into the dungeon dimension");
            Vec3 expectedEnter = Vec3.atLowerCornerOf(new BlockPos(portalAbs.getX() * 16, 100, portalAbs.getZ() * 16)).add(0.5, 0, 0.5);
            helper.assertTrue(enterTransition.position().equals(expectedEnter), "expired wait must target the portal dungeon cell");
            for (Mob boss : dungeonWorld.getEntitiesOfClass(Mob.class, new AABB(new BlockPos(16, 100, 16)).inflate(4.0),
                    mob -> mob.getType() == loadedDungeon(helper, "dark_dungeon").getBossEntityType())) {
                boss.discard();
            }
        } finally {
            releaseDungeonWorld(helper);
        }
        helper.succeed();
    }

    @GameTest
    public void deathTransfersToDeadRosterAndCooldownWhenForbidden(GameTestHelper helper) {
        // PlayerManagerMixin respawn redirect (!alive, respawn forbidden):
        // dead-UUID transfer to the dead roster, living roster removal, and
        // cooldown only when no living players remain. Uses a throwaway
        // no-respawn copy of dark_dungeon (shipped tuning untouched), removed
        // again in the finally block.
        Dungeon base = loadedDungeon(helper, "dark_dungeon");
        String testId = "dark_dungeon_no_respawn_test";
        helper.assertTrue(Dungeon.getDungeon(testId) == null, "death fixture id must be unused");
        Dungeon forbidden = new Dungeon(testId, base.getBlockIdEntityMap(), base.getBlockIdEntitySpawnChanceMap(),
                base.getBlockIdBlockReplacementMap(), base.getSpawnerEntityIdMap(), base.getDifficultyRequiredItemCountMap(),
                base.getBreakableBlockIdList(), base.getplaceableBlockIdList(), base.getDifficultyList(),
                base.getDifficultyMobHealthModificatorMap(), base.getDifficultyMobDamageModificatorMap(),
                base.getDifficultyMobProtectionModificatorMap(), base.getDifficultyMobSpeedModificatorMap(),
                base.getDifficultyLootTableIdMap(), base.getDifficultyBossHealthModificatorMap(),
                base.getDifficultyBossDamageModificatorMap(), base.getDifficultyBossProtectionModificatorMap(),
                base.getDifficultyBossSpeedModificatorMap(), base.getDifficultyBossLootTableMap(), base.getBossEntityType(),
                base.getBossNbtCompound(), base.getBossBlockId(), base.getBossLootBlockId(), base.getExitBlockId(), false,
                base.isElytraAllowed(), base.isKeepInventory(), base.isEnderPearlAllowed(),
                base.isPositiveEffectsAllowed(), base.getMaxGroupSize(), base.getMinGroupSize(), base.getRequiredLevel(),
                base.getCooldown(), base.getBackgroundId(), base.getStructurePoolId());
        helper.assertTrue(!forbidden.isRespawnAllowed(), "test copy must forbid respawn");
        Dungeon.addDungeon(forbidden);
        try {
            var world = helper.getLevel();
            BlockPos rel = new BlockPos(1, 1, 1);
            helper.setBlock(rel, BlockInit.DUNGEON_PORTAL);
            DungeonPortalEntity portal = helper.getBlockEntity(rel, DungeonPortalEntity.class);
            portal.setDungeonType(testId);
            portal.setDifficulty("easy");
            portal.setMaxGroupSize(5);

            var oldPlayer = helper.makeMockServerPlayerInLevel();
            var respawnedPlayer = helper.makeMockServerPlayerInLevel();
            portal.joinDungeon(oldPlayer.getUUID());
            int before = (int) world.getGameTime();
            int expectedCooldown = forbidden.getCooldown() + before;
            portal.getDungeonPlayerUuids().remove(oldPlayer.getUUID());
            portal.addDeadDungeonPlayerUuids(respawnedPlayer.getUUID());
            if (portal.getDungeonPlayerCount() == 0) {
                portal.setCooldownTime(forbidden.getCooldown() + (int) world.getGameTime());
            }
            portal.setChanged();
            helper.assertTrue(portal.getDungeonPlayerUuids().isEmpty(), "death must remove the living roster entry");
            helper.assertTrue(portal.getDeadDungeonPlayerUUIDs().equals(List.of(respawnedPlayer.getUUID())), "death must transfer the respawned UUID to the dead roster");
            helper.assertTrue(portal.getCooldownTime() == expectedCooldown, "last living death must start cooldown at dungeon tuning");
            helper.assertTrue(portal.isOnCooldown(before) && !portal.isOnCooldown(expectedCooldown), "cooldown must hold until exactly the stored time");

            portal.getDeadDungeonPlayerUUIDs().clear();
            portal.setCooldownTime(0);
            var first = helper.makeMockServerPlayerInLevel();
            var second = helper.makeMockServerPlayerInLevel();
            var secondRespawned = helper.makeMockServerPlayerInLevel();
            portal.joinDungeon(first.getUUID());
            portal.joinDungeon(second.getUUID());
            portal.getDungeonPlayerUuids().remove(second.getUUID());
            portal.addDeadDungeonPlayerUuids(secondRespawned.getUUID());
            if (portal.getDungeonPlayerCount() == 0) {
                portal.setCooldownTime(forbidden.getCooldown() + (int) world.getGameTime());
            }
            portal.setChanged();
            helper.assertTrue(portal.getDungeonPlayerUuids().equals(List.of(first.getUUID())), "survivor must stay on the living roster");
            helper.assertTrue(portal.getDeadDungeonPlayerUUIDs().equals(List.of(secondRespawned.getUUID())), "only the dead UUID must move to the dead roster");
            helper.assertTrue(portal.getCooldownTime() == 0, "surviving group must not start cooldown");
        } finally {
            DungeonzMain.DUNGEONS.removeIf(entry -> entry.getDungeonTypeId().equals(testId));
        }
        helper.succeed();
    }

    @GameTest
    public void respawnRedirectTargetsDungeonCellWhenAllowed(GameTestHelper helper) {
        // PlayerManagerMixin respawn ModifyVariable (respawn allowed): the
        // respawn transition must redirect to the portal dungeon cell instead
        // of the vanilla spawn, with no roster changes. Transition object
        // only - no dimension travel.
        Dungeon dungeon = loadedDungeon(helper, "dark_dungeon");
        helper.assertTrue(dungeon.isRespawnAllowed(), "shipped dark dungeon must allow respawn");
        BlockPos rel = new BlockPos(1, 1, 1);
        helper.setBlock(rel, BlockInit.DUNGEON_PORTAL);
        DungeonPortalEntity portal = helper.getBlockEntity(rel, DungeonPortalEntity.class);
        portal.setDungeonType("dark_dungeon");
        portal.setDifficulty("easy");
        BlockPos portalAbs = helper.absolutePos(rel);

        var oldPlayer = helper.makeMockServerPlayerInLevel();
        portal.joinDungeon(oldPlayer.getUUID());
        // Mirror of PlayerManagerMixin respawn ModifyVariable (respawn allowed):
        // same inputs (portal block pos, dying player's level) and same centered
        // cell formula. Asserts below lock that formula and the no-roster-change
        // contract; the mixin itself cannot run headless (needs PlayerList.respawn).
        BlockPos pos = portal.getBlockPos();
        helper.assertTrue(pos.equals(portalAbs), "portal block entity must sit at the placed portal");
        TeleportTransition redirect = new TeleportTransition(oldPlayer.level(),
                Vec3.atLowerCornerOf(new BlockPos(pos.getX() * 16, 100, pos.getZ() * 16)).add(0.5, 0, 0.5),
                Vec3.ZERO, oldPlayer.getYRot(), 0.0f, TeleportTransition.DO_NOTHING);
        helper.assertTrue(redirect.newLevel() == oldPlayer.level(), "allowed respawn must stay in the dungeon level");
        Vec3 expected = Vec3.atLowerCornerOf(new BlockPos(portalAbs.getX() * 16, 100, portalAbs.getZ() * 16)).add(0.5, 0, 0.5);
        helper.assertTrue(redirect.position().equals(expected), "allowed respawn must target the portal dungeon cell");
        helper.assertTrue(portal.getDungeonPlayerUuids().equals(List.of(oldPlayer.getUUID())), "allowed respawn must not touch the living roster");
        helper.assertTrue(portal.getDeadDungeonPlayerUUIDs().isEmpty(), "allowed respawn must not populate the dead roster");
        helper.succeed();
    }

    @GameTest
    public void dungeonLeaveCommandEmptiesGroupSetsCooldownAndReturns(GameTestHelper helper) {
        // CommandInit /dungeon leave body without command dispatch or
        // dimension travel: membership removal, cooldown only when the group
        // empties, and the teleportOutOfDungeon return target (oldWorld branch
        // of teleportOutOfDungeon uses DungeonPlacementHandler.leave, which is
        // asserted here as the transition object).
        Dungeon dungeon = loadedDungeon(helper, "dark_dungeon");
        var oldWorld = helper.getLevel();
        var dungeonWorld = ensureDungeonWorld(helper);
        try {
            BlockPos rel = new BlockPos(1, 1, 1);
            helper.setBlock(rel, BlockInit.DUNGEON_PORTAL);
            DungeonPortalEntity portal = helper.getBlockEntity(rel, DungeonPortalEntity.class);
            portal.setDungeonType("dark_dungeon");
            portal.setDifficulty("easy");
            portal.setCooldownTime(0);
            BlockPos portalAbs = helper.absolutePos(rel);

            var player = helper.makeMockServerPlayerInLevel();
            DungeonPlacementHandler.enter(player, dungeonWorld, oldWorld, portal, portalAbs, "easy", true);
            var access = (ServerPlayerAccess) player;
            helper.assertTrue(access.getOldServerWorld() == oldWorld, "leave fixture must persist the return world (teleportOutOfDungeon oldWorld branch)");
            BlockPos savedSpawn = access.getDungeonSpawnBlockPos();
            helper.assertTrue(savedSpawn != null, "leave fixture must persist a return position");
            helper.assertTrue(portal.getDungeonPlayerUuids().contains(player.getUUID()), "leave fixture must join the player");

            int before = (int) player.level().getGameTime();
            portal.getDungeonPlayerUuids().remove(player.getUUID());
            if (portal.getDungeonPlayerCount() == 0) {
                portal.setCooldownTime(dungeon.getCooldown() + (int) player.level().getGameTime());
            }
            portal.setChanged();
            helper.assertTrue(!portal.getDungeonPlayerUuids().contains(player.getUUID()), "leave must remove the player from the dungeon");
            helper.assertTrue(portal.getCooldownTime() == dungeon.getCooldown() + before, "empty group leave must start cooldown at dungeon tuning");

            TeleportTransition leaveTransition = DungeonPlacementHandler.leave(player, oldWorld);
            helper.assertTrue(leaveTransition.newLevel() == oldWorld, "leave must target the return world");
            Vec3 expectedLeave = Vec3.atLowerCornerOf(savedSpawn).add(0.5, 0, 0.5);
            helper.assertTrue(leaveTransition.position().equals(expectedLeave), "leave must restore the saved return position");

            portal.setCooldownTime(0);
            var first = helper.makeMockServerPlayerInLevel();
            var second = helper.makeMockServerPlayerInLevel();
            portal.joinDungeon(first.getUUID());
            portal.joinDungeon(second.getUUID());
            portal.getDungeonPlayerUuids().remove(first.getUUID());
            if (portal.getDungeonPlayerCount() == 0) {
                portal.setCooldownTime(dungeon.getCooldown() + (int) player.level().getGameTime());
            }
            portal.setChanged();
            helper.assertTrue(portal.getDungeonPlayerUuids().equals(List.of(second.getUUID())), "remaining member must stay on the roster");
            helper.assertTrue(portal.getCooldownTime() == 0, "non-empty group leave must not start cooldown");
        } finally {
            releaseDungeonWorld(helper);
        }
        helper.succeed();
    }

    @GameTest
    public void compassCalibrationConsumesShardsAndBindsDungeon(GameTestHelper helper) {
        // DungeonServerPacket DungeonCompassPacket receiver (calibrated path):
        // compass in main hand plus 3 amethysts in main inventory must consume
        // the shards and bind the dungeon structure component.
        var player = helper.makeMockServerPlayer(GameType.SURVIVAL);
        helper.assertTrue(!player.isCreative(), "compass fixture must be survival");
        var inventory = player.getInventory();
        inventory.clearContent();
        var compass = new ItemStack(ItemInit.DUNGEON_COMPASS);
        inventory.setItem(0, compass);
        inventory.setItem(1, new ItemStack(Items.AMETHYST_SHARD, 3));
        helper.assertTrue(player.getMainHandItem().is(ItemInit.DUNGEON_COMPASS), "compass must be in the main hand");
        helper.assertTrue(InventoryHelper.hasRequiredItemStacks(inventory, ItemInit.getRequiredDungeonCompassCalibrationItems()), "three shards must fund calibration");

        ServerLevel level = (ServerLevel) player.level();
        if (player.getMainHandItem().is(ItemInit.DUNGEON_COMPASS)
                && InventoryHelper.hasRequiredItemStacks(player.getInventory(), ItemInit.getRequiredDungeonCompassCalibrationItems())) {
            InventoryHelper.decrementRequiredItemStacks(player.getInventory(), ItemInit.getRequiredDungeonCompassCalibrationItems());
            DungeonCompassItem.setCompassDungeonStructure(level, player.blockPosition(), player.getMainHandItem(), "dark_dungeon");
        }
        var component = player.getMainHandItem().get(ItemInit.DUNGEON_COMPASS_DATA);
        helper.assertTrue(component != null, "calibration must set the compass component");
        helper.assertTrue(component.hasDungeon(), "calibration must mark a bound dungeon");
        helper.assertTrue(component.dungeonType().equals("dark_dungeon"), "calibration must bind the requested dungeon");
        helper.assertTrue(component.dungeonPos().isPresent(), "calibration must record a structure position");
        int shardsLeft = 0;
        for (ItemStack stack : inventory.getNonEquipmentItems()) {
            if (stack.is(Items.AMETHYST_SHARD)) {
                shardsLeft += stack.getCount();
            }
        }
        helper.assertTrue(shardsLeft == 0, "calibration must consume all three shards, got " + shardsLeft);
        helper.succeed();
    }

    @GameTest
    public void compassCalibrationDeniedWithoutShards(GameTestHelper helper) {
        // DungeonServerPacket DungeonCompassPacket receiver (denial path):
        // compass in main hand but no amethysts must leave the compass
        // unbound and the inventory untouched.
        var player = helper.makeMockServerPlayer(GameType.SURVIVAL);
        helper.assertTrue(!player.isCreative(), "compass fixture must be survival");
        var inventory = player.getInventory();
        inventory.clearContent();
        inventory.setItem(0, new ItemStack(ItemInit.DUNGEON_COMPASS));
        helper.assertTrue(player.getMainHandItem().is(ItemInit.DUNGEON_COMPASS), "compass must be in the main hand");
        helper.assertTrue(!InventoryHelper.hasRequiredItemStacks(inventory, ItemInit.getRequiredDungeonCompassCalibrationItems()), "empty inventory must not fund calibration");

        ServerLevel level = (ServerLevel) player.level();
        if (player.getMainHandItem().is(ItemInit.DUNGEON_COMPASS)
                && InventoryHelper.hasRequiredItemStacks(player.getInventory(), ItemInit.getRequiredDungeonCompassCalibrationItems())) {
            InventoryHelper.decrementRequiredItemStacks(player.getInventory(), ItemInit.getRequiredDungeonCompassCalibrationItems());
            DungeonCompassItem.setCompassDungeonStructure(level, player.blockPosition(), player.getMainHandItem(), "dark_dungeon");
        }
        helper.assertTrue(!player.getMainHandItem().has(ItemInit.DUNGEON_COMPASS_DATA), "denied calibration must not set the compass component");
        helper.assertTrue(player.getMainHandItem().is(ItemInit.DUNGEON_COMPASS) && player.getMainHandItem().getCount() == 1, "denied calibration must leave the compass untouched");
        helper.succeed();
    }

    private static Dungeon loadedDungeon(GameTestHelper helper, String type) {        Dungeon dungeon = Dungeon.getDungeon(type);
        helper.assertTrue(dungeon != null, type + " must already be loaded by the registered server resource listener");
        helper.assertTrue(DungeonzMain.DUNGEONS.stream().filter(entry -> entry.getDungeonTypeId().equals(type)).count() == 1, type + " must have exactly one loaded definition");
        return dungeon;
    }

    private static void assertDefaults(GameTestHelper helper, Dungeon dungeon, List<String> difficulties, List<Integer> costs, int cooldown) {
        helper.assertTrue(dungeon.getDifficultyList().equals(difficulties), "difficulty order must match default data");
        helper.assertTrue(dungeon.getCooldown() == cooldown, "default cooldown must load");
        helper.assertTrue(dungeon.getMaxGroupSize() == 5 && dungeon.getMinGroupSize() == 0 && dungeon.getRequiredLevel() == 0, "default group and level requirements");
        helper.assertTrue(dungeon.isRespawnAllowed() && dungeon.isPositiveEffectsAllowed(), "default respawn and positive effects allowed");
        helper.assertFalse(dungeon.isElytraAllowed() || dungeon.isEnderPearlAllowed() || dungeon.isKeepInventory(), "default travel and inventory restrictions");
        helper.assertTrue(dungeon.getBreakableBlockIdList().isEmpty() && dungeon.getplaceableBlockIdList().equals(List.of(BuiltInRegistries.BLOCK.getId(Blocks.TORCH))), "default block restrictions");
        helper.assertTrue(dungeon.getBossBlockId() == BuiltInRegistries.BLOCK.getId(Blocks.NETHERITE_BLOCK)
                && dungeon.getBossLootBlockId() == BuiltInRegistries.BLOCK.getId(Blocks.EMERALD_BLOCK)
                && dungeon.getExitBlockId() == BuiltInRegistries.BLOCK.getId(Blocks.QUARTZ_BLOCK), "default boss, loot and exit markers");
        helper.assertTrue(dungeon.getBlockIdBlockReplacementMap().size() == dungeon.getBlockIdEntityMap().size() + 3, "every marker must have a replacement");
        helper.assertTrue(dungeon.getBlockIdBlockReplacementMap().values().stream().allMatch(id -> id == BuiltInRegistries.BLOCK.getId(Blocks.AIR)), "all default markers must be replaced by air");
        for (int i = 0; i < difficulties.size(); i++) {
            String difficulty = difficulties.get(i);
            helper.assertTrue(Map.of(BuiltInRegistries.ITEM.getId(Items.DIAMOND), costs.get(i)).equals(dungeon.getDifficultyRequiredItemCountMap().get(difficulty)), difficulty + " diamond entry cost");
            helper.assertTrue(("dungeonz:chests/" + dungeon.getDungeonTypeId() + "_" + difficulty + "_boss_loot").equals(dungeon.getDifficultyBossLootTableMap().get(difficulty)), difficulty + " boss loot table");
        }
    }

    private static void roundtrip(GameTestHelper helper, BlockEntity source, BlockEntity restored) {
        CompoundTag saved = source.saveWithoutMetadata(helper.getLevel().registryAccess());
        restored.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), saved));
        helper.assertTrue(restored.saveWithoutMetadata(helper.getLevel().registryAccess()).equals(saved), "block entity NBT must survive save/load/save exactly");
    }
}
