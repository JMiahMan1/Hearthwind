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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.TagValueInput;

public final class DungeonZGameTests {
    public DungeonZGameTests() {}

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
        try {
            helper.assertTrue(helper.getLevel().getServer().getLevel(DimensionInit.DUNGEON_WORLD) != null, "dungeonz:dungeon dimension must exist on the gametest server");
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
            DungeonzMain.DUNGEONS.removeIf(entry -> entry.getDungeonTypeId().equals(testId));
        }
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

    private static Dungeon loadedDungeon(GameTestHelper helper, String type) {
        Dungeon dungeon = Dungeon.getDungeon(type);
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
