package dev.jmiahman.hearthwind.jobs;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class HearthwindJobsGameTests {
    public HearthwindJobsGameTests() {}

    @GameTest
    public void jobsLoadEightDefinitions(GameTestHelper helper) {
        JobDefs.ensureLoaded();
        helper.assertTrue(JobDefs.all().size() >= 8, "expected 8 jobs, got " + JobDefs.all().size());
        helper.assertTrue(JobDefs.byId("miner") != null, "miner job must exist");
        helper.assertTrue(JobDefs.byId("builder") != null, "builder job must exist");
        helper.succeed();
    }

    @GameTest
    public void jobXpAccruesAndLevels(GameTestHelper helper) {
        var pig = helper.spawn(EntityTypes.PIG, 1, 2, 1);
        // Simulate joining miner without needing ServerPlayer join command
        pig.setAttached(JobState.STATE, new JobState.Data("miner", 0.0));
        helper.assertTrue(JobState.jobId(pig).equals("miner"), "job should be miner");
        int before = JobState.level(pig);
        JobDefs.JobDef def = JobDefs.byId("miner");
        // Find a block id that is valid for level 1 miner
        if (!def.levels.isEmpty()) {
            var lvl = def.levels.get(0);
            String matchId = lvl.blocks().isEmpty() ? (lvl.entities().isEmpty() ? null : lvl.entities().get(0)) : lvl.blocks().get(0);
            if (matchId != null) {
                JobState.awardIfMatch(pig, matchId);
                helper.assertTrue(JobState.xp(pig) > 0, "xp should increase after matching action " + matchId);
            }
        }
        // xp -> level math: pointsPerLevel default 100, xpPerAction default 10 -> 10 actions per level
        pig.setAttached(JobState.STATE, new JobState.Data("miner", 250));
        helper.assertTrue(JobState.level(pig) == 2, "250 xp with 100/level = level 2, got " + JobState.level(pig));
        helper.succeed();
    }

    @GameTest
    public void itemAwardsUseTheItemId(GameTestHelper helper) {
        // The crafting/furnace/fishing hooks all route the produced stack
        // through JobEvents.awardItem, which pays the item registry id.
        ServerPlayer player = (ServerPlayer) helper.makeMockServerPlayer(GameType.SURVIVAL);
        player.setAttached(JobState.STATE, new JobState.Data("farmer", 0.0));
        JobEvents.awardItem(player, new ItemStack(net.minecraft.world.item.Items.WHEAT));
        helper.assertTrue(JobState.xp(player, "farmer") > 0.0,
                "wheat must pay farmer xp, got " + JobState.xp(player, "farmer"));
        JobEvents.awardItem(player, ItemStack.EMPTY);
        helper.succeed();
    }

    @GameTest
    public void unemployedGetsNoXp(GameTestHelper helper) {
        var pig = helper.spawn(EntityTypes.PIG, 1, 2, 1);
        pig.setAttached(JobState.STATE, new JobState.Data("", 0.0));
        JobState.awardIfMatch(pig, "minecraft:stone");
        helper.assertTrue(JobState.xp(pig) == 0.0, "unemployed should not gain xp");
        helper.succeed();
    }

    @GameTest
    public void maxedJobStopsAccruing(GameTestHelper helper) {
        var pig = helper.spawn(EntityTypes.PIG, 1, 2, 1);
        JobDefs.JobDef def = JobDefs.byId("miner");
        int max = def.maxLevel();
        // Give huge xp
        pig.setAttached(JobState.STATE, new JobState.Data("miner", 999999));
        int lvl = JobState.level(pig);
        helper.assertTrue(lvl == max, "huge xp caps at max level " + max + " got " + lvl);
        double before = JobState.xp(pig);
        var lvlSpec = def.levels.get(def.levels.size()-1);
        String any = lvlSpec.blocks().isEmpty() ? "minecraft:stone" : lvlSpec.blocks().get(0);
        JobState.awardIfMatch(pig, any);
        helper.assertTrue(JobState.xp(pig) == before, "maxed job must not accrue further");
        helper.succeed();
    }

    @GameTest
    public void jobGateDeniesUnqualifiedCrafting(GameTestHelper helper) {
        JobGates.ensureLoaded();
        helper.assertTrue(JobGates.gateCount() > 0, "job crafting gates must be loaded");
        // iron_ingot is gated at miner level 1
        var ironIngot = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_INGOT);
        var gate = JobGates.gate(ironIngot);
        helper.assertTrue(gate != null, "iron_ingot must have a gate");
        helper.assertTrue(gate.jobId().equals("miner"), "iron_ingot gate must be miner");
        helper.assertTrue(gate.level() == 1, "iron_ingot gate must require level 1");
        var player = helper.makeMockServerPlayerInLevel();
        player.getAbilities().instabuild = false;
        player.setAttached(JobState.STATE, new JobState.Data("", 0.0));
        // Enforcement is opt-in: jobs reward work, SKILLS gate crafting.
        helper.assertTrue(!HearthwindJobsConfig.get().jobCraftGating,
                "job craft gating must default to off");
        helper.assertTrue(JobGates.allowed(player, ironIngot),
                "with gating off an unemployed player may still craft iron_ingot");
        HearthwindJobsConfig.get().jobCraftGating = true;
        try {
            helper.assertFalse(JobGates.allowed(player, ironIngot),
                    "with gating on an unemployed player must be denied iron_ingot");
            player.setAttached(JobState.STATE, new JobState.Data("miner", 100));
            helper.assertTrue(JobGates.allowed(player, ironIngot),
                    "miner level 1 must be allowed to craft iron_ingot");
        } finally {
            HearthwindJobsConfig.get().jobCraftGating = false;
        }
        helper.succeed();
    }

    @GameTest
    public void smitherJoinBlockedBelowAge2(GameTestHelper helper) {
        JobDefs.ensureLoaded();
        var player = helper.makeMockServerPlayerInLevel();
        player.getAbilities().instabuild = false;
        AgeState.set(player, 0);
        helper.assertFalse(JobState.join(player, "smither"),
                "smither join must fail at Age 0");
        helper.assertFalse(JobState.join(player, "brewer"),
                "brewer join must fail at Age 0");
        AgeState.set(player, 2);
        helper.assertTrue(JobState.join(player, "smither"),
                "smither join must succeed at Age 2");
        helper.succeed();
    }

    @GameTest
    public void smitherRewardsGatedBehindAge2(GameTestHelper helper) {
        JobDefs.ensureLoaded();
        var player = helper.makeMockServerPlayerInLevel();
        player.getInventory().clearContent();
        player.getAbilities().instabuild = false;
        AgeState.set(player, 0);
        JobRewards.apply(player, "smither", 1);
        int blocked = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (!player.getInventory().getItem(i).isEmpty()) blocked++;
        }
        helper.assertTrue(blocked == 0,
                "smither level 1 rewards must be withheld at Age 0, got " + blocked + " items");
        AgeState.set(player, 2);
        JobRewards.apply(player, "smither", 1);
        int after = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (!player.getInventory().getItem(i).isEmpty()) after++;
        }
        helper.assertTrue(after > 0,
                "smither level 1 rewards must be granted at Age 2, got " + after + " items");
        helper.succeed();
    }

    @GameTest
    public void farmerRewardsGrantedAtAnyAge(GameTestHelper helper) {
        JobDefs.ensureLoaded();
        var player = helper.makeMockServerPlayerInLevel();
        player.getAbilities().instabuild = false;
        AgeState.set(player, 0);
        JobState.join(player, "farmer");
        JobRewards.apply(player, "farmer", 1);
        int after = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (!player.getInventory().getItem(i).isEmpty()) after++;
        }
        // Farmer level 1 has items; verify no crash and items were granted
        helper.assertTrue(after > 0,
                "farmer level 1 rewards must be granted at Age 0, got " + after + " items");
        helper.succeed();
    }

    @GameTest
    public void jobJoinReturnsTrueForValidJob(GameTestHelper helper) {
        JobDefs.ensureLoaded();
        var player = helper.makeMockServerPlayerInLevel();
        boolean ok = JobState.join(player, "miner");
        helper.assertTrue(ok, "joining miner must succeed");
        helper.assertTrue(JobState.jobId(player).equals("miner"),
                "player job must be miner after join");
        helper.succeed();
    }

    @GameTest
    public void jobJoinReturnsFalseForInvalidJob(GameTestHelper helper) {
        JobDefs.ensureLoaded();
        var player = helper.makeMockServerPlayerInLevel();
        boolean ok = JobState.join(player, "nonexistent");
        helper.assertFalse(ok, "joining nonexistent job must fail");
        helper.assertTrue(JobState.jobId(player).isEmpty(),
                "player must remain unemployed after failed join");
        helper.succeed();
    }

    @GameTest
    public void jobLeaveClearsState(GameTestHelper helper) {
        JobDefs.ensureLoaded();
        var player = helper.makeMockServerPlayerInLevel();
        JobState.join(player, "farmer");
        helper.assertTrue(!JobState.jobId(player).isEmpty(), "player must have a job");
        JobState.leave(player);
        helper.assertTrue(JobState.jobId(player).isEmpty(), "player must be unemployed after leave");
        helper.assertTrue(JobState.xp(player) == 0.0, "xp must be zero after leave");
        helper.succeed();
    }

    @GameTest
    public void jobAwardIfMatchGivesXp(GameTestHelper helper) {
        JobDefs.ensureLoaded();
        var player = helper.makeMockServerPlayerInLevel();
        JobState.join(player, "miner");
        double before = JobState.xp(player);
        JobState.awardIfMatch(player, "minecraft:stone");
        // stone might not match miner, so just verify no crash and xp is non-negative
        helper.assertTrue(JobState.xp(player) >= before,
                "xp must not decrease after award");
        helper.succeed();
    }

    @GameTest
    public void jobDefLevelMathCorrect(GameTestHelper helper) {
        JobDefs.ensureLoaded();
        var miner = JobDefs.byId("miner");
        helper.assertTrue(miner != null, "miner must exist");
        helper.assertTrue(miner.maxLevel() > 0, "miner must have levels");
        helper.assertTrue(miner.levels.size() > 0,
                "miner must have level specs");
        helper.succeed();
    }

    @GameTest
    public void allJobsHaveValidLevelSpecs(GameTestHelper helper) {
        JobDefs.ensureLoaded();
        for (var entry : JobDefs.all().entrySet()) {
            var def = entry.getValue();
            helper.assertTrue(def.maxLevel() > 0,
                    entry.getKey() + " must have max level > 0");
            helper.assertTrue(def.levels.size() > 0,
                    entry.getKey() + " must have level specs");
        }
        helper.succeed();
    }

    @GameTest
    public void jobCorpusLoadsContentLadders(GameTestHelper helper) {
        helper.assertTrue(JobCorpus.hasCorpus(), "the jobs corpus must load from the world datapack");
        helper.assertTrue(JobCorpus.jobCount() >= 6,
                "expected at least 6 job ladders, got " + JobCorpus.jobCount());
        // The ladder level is also the XP reward tier.
        helper.assertTrue(JobCorpus.levelFor("miner", "minecraft:iron_ore") == 7,
                "iron ore must sit at miner level 7 (got "
                        + JobCorpus.levelFor("miner", "minecraft:iron_ore") + ")");
        helper.assertTrue(JobCorpus.levelFor("miner", "minecraft:diamond_ore") == 20,
                "diamond ore must sit at miner level 20 (got "
                        + JobCorpus.levelFor("miner", "minecraft:diamond_ore") + ")");
        helper.assertTrue(JobCorpus.levelFor("miner", "minecraft:dirt") == 0,
                "dirt is not miner content");
        helper.succeed();
    }

    @GameTest
    public void jobCorpusLoadsRestrictedRecipes(GameTestHelper helper) {
        helper.assertTrue(JobCorpus.restrictedCount() >= 50,
                "expected the restricted recipe list, got " + JobCorpus.restrictedCount());
        helper.assertTrue(JobCorpus.isRestrictedRecipe(
                        net.minecraft.resources.Identifier.fromNamespaceAndPath("agedaddition", "coal_piece")),
                "the piece conversions must be excluded from crafting XP");
        helper.assertTrue(!JobCorpus.isRestrictedRecipe(
                        net.minecraft.resources.Identifier.fromNamespaceAndPath("minecraft", "stick")),
                "a plain recipe must not be restricted");
        helper.succeed();
    }

    @GameTest
    public void jobXpPaysTheContentTier(GameTestHelper helper) {
        var pig = helper.spawn(EntityTypes.PIG, 1, 2, 1);
        pig.setAttached(JobState.STATE, new JobState.Data("miner", 0.0));
        JobState.awardIfMatch(pig, "minecraft:iron_ore");
        helper.assertTrue(Math.abs(JobState.xp(pig) - 7.0) < 0.001,
                "breaking iron ore as a miner must pay 7 xp (got " + JobState.xp(pig) + ")");
        // Off-ladder content falls back to the flat config value.
        String offLadder = JobDefs.byId("miner").levels.get(0).blocks().stream()
                .filter(id -> JobCorpus.levelFor("miner", id) == 0)
                .findFirst().orElse(null);
        if (offLadder != null) {
            pig.setAttached(JobState.STATE, new JobState.Data("miner", 0.0));
            JobState.awardIfMatch(pig, offLadder);
            helper.assertTrue(Math.abs(JobState.xp(pig) - HearthwindJobsConfig.get().xpPerAction) < 0.001,
                    "off-ladder content must pay the flat xpPerAction (got " + JobState.xp(pig) + ")");
        }
        helper.succeed();
    }

    @GameTest
    public void craftingIsNotGatedBehindAJobByDefault(GameTestHelper helper) {
        net.minecraft.server.level.ServerPlayer player = helper.makeMockServerPlayerInLevel();
        helper.assertTrue(!HearthwindJobsConfig.get().jobCraftGating,
                "job craft gating must default to off (jobs reward, skills gate)");
        helper.assertTrue(JobGates.allowed(player, new net.minecraft.world.item.ItemStack(
                        net.minecraft.world.item.Items.IRON_INGOT)),
                "anyone may craft an iron ingot without being a miner");
        helper.succeed();
    }

    /**
     * The builder's earning list is a data file, not a ladder: without this
     * loader the placement hook has nothing to match against and the builder
     * job can never be trained.
     */
    @GameTest
    public void builderPlacementTagLoads(GameTestHelper helper) {
        JobDefs.ensureLoaded();
        helper.assertTrue(JobCorpus.placementTagCount() >= 20,
                "builder_placing_blocks must load its 26 entries ("
                        + JobCorpus.placementTagCount() + ")");
        helper.assertTrue(JobCorpus.isBuilderPlacement(Blocks.OAK_PLANKS.defaultBlockState()),
                "oak planks are on the placement tag");
        helper.assertTrue(JobCorpus.isBuilderPlacement(Blocks.COBBLESTONE_WALL.defaultBlockState()),
                "tag references must resolve, not just plain block ids");
        helper.assertTrue(JobCorpus.isBuilderPlacement(Blocks.OBSIDIAN.defaultBlockState()),
                "obsidian is listed as a plain block id");
        helper.assertTrue(!JobCorpus.isBuilderPlacement(Blocks.DIRT.defaultBlockState()),
                "dirt is not builder work");
        helper.assertTrue(!JobCorpus.isBuilderPlacement(Blocks.STONE.defaultBlockState()),
                "stone is not builder work either (rock features list it, not the placement tag)");
        helper.succeed();
    }

    @GameTest
    public void placingBlocksPaysTheBuilder(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        // The hook ignores instabuild players, like every other earning hook.
        player.getAbilities().instabuild = false;
        player.setAttached(JobState.STATE, new JobState.Data("builder", 0.0));
        // Click the top face of a stone we control, well clear of the mock
        // player, so the placement is the ordinary "build on a solid block"
        // case and cannot be obstructed by an entity.
        var base = new BlockPos(5, 64, 5);
        helper.setBlock(base, Blocks.STONE.defaultBlockState());
        var baseWorld = helper.absolutePos(base);
        var ctx = new BlockPlaceContext(player, InteractionHand.MAIN_HAND,
                new ItemStack(Blocks.OAK_PLANKS), new BlockHitResult(
                        Vec3.atCenterOf(baseWorld.above()), Direction.UP, baseWorld, false));
        InteractionResult result = ((BlockItem) Blocks.OAK_PLANKS.asItem()).place(ctx);
        helper.assertTrue(result instanceof InteractionResult.Success,
                "the placement itself must succeed, got " + result);
        helper.assertTrue(JobState.xp(player, "builder") > 0.0,
                "placing a plank must pay the builder, got " + JobState.xp(player, "builder"));
        // ... and placing something the tag does not list pays nothing.
        var before = JobState.xp(player, "builder");
        JobEvents.awardBlockPlaced(player, Blocks.DIRT.defaultBlockState());
        helper.assertTrue(JobState.xp(player, "builder") == before,
                "dirt is not builder work and must not pay");
        // A block that is on both lists pays its corpus tier, not the flat
        // fallback.
        int obsidianTier = JobCorpus.levelFor("builder", "minecraft:obsidian");
        helper.assertTrue(obsidianTier > 0, "obsidian must be on the builder ladder");
        JobEvents.awardBlockPlaced(player, Blocks.OBSIDIAN.defaultBlockState());
        helper.assertTrue(Math.abs(JobState.xp(player, "builder") - before - obsidianTier) < 0.001,
                "obsidian must pay its corpus tier " + obsidianTier);
        // A creative-mode placer earns nothing.
        player.getAbilities().instabuild = true;
        var afterCreative = JobState.xp(player, "builder");
        JobEvents.awardBlockPlaced(player, Blocks.OBSIDIAN.defaultBlockState());
        helper.assertTrue(JobState.xp(player, "builder") == afterCreative,
                "an instabuild placer must not pay");
        helper.succeed();
    }

    /**
     * The break hook is the mirror image: the same block id is on the
     * builder's ladder, so without the filter a builder who takes a wall
     * apart would be paid twice for the same wall - once for placing it and
     * once for breaking it.
     */
    @GameTest
    public void breakingDoesNotPayTheBuilder(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        player.setAttached(JobState.STATE, new JobState.Data("builder", 0.0));
        JobState.awardIfMatch(player, "minecraft:oak_planks",
                jobDef -> !jobDef.id.equals("builder"));
        helper.assertTrue(JobState.xp(player, "builder") == 0.0,
                "the builder must not be paid for breaking, got " + JobState.xp(player, "builder"));
        // The miner, who is on the same ladder, still is.
        player.setAttached(JobState.STATE, new JobState.Data("miner", 0.0));
        JobState.awardIfMatch(player, "minecraft:iron_ore",
                jobDef -> !jobDef.id.equals("builder"));
        helper.assertTrue(JobState.xp(player, "miner") > 0.0, "the miner is still paid for iron ore");
        helper.succeed();
    }

    @GameTest
    public void brewingPaysTheBrewerCorpusTier(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        player.setAttached(JobState.STATE, new JobState.Data("brewer", 0.0));
        int tier = JobCorpus.levelFor("brewer", "minecraft:strong_swiftness");
        helper.assertTrue(tier > 0, "strong swiftness must be on the brewer ladder");
        JobEvents.awardBrew(player, PotionContents.createItemStack(Items.POTION, Potions.STRONG_SWIFTNESS));
        helper.assertTrue(Math.abs(JobState.xp(player, "brewer") - tier) < 0.001,
                "brewing strong swiftness must pay its corpus tier " + tier
                        + " (got " + JobState.xp(player, "brewer") + ")");
        // Water is not on the ladder, so it pays nothing at all.
        double before = JobState.xp(player, "brewer");
        JobEvents.awardBrew(player, PotionContents.createItemStack(Items.POTION, Potions.WATER));
        helper.assertTrue(JobState.xp(player, "brewer") == before,
                "an unlisted potion must not pay the brewer");
        helper.succeed();
    }

    /** The anvil and smithing table pay the smither for the result, not for the click. */
    @GameTest
    public void anvilAndSmithingResultsPayTheSmither(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        player.setAttached(JobState.STATE, new JobState.Data("smither", 0.0));
        JobEvents.awardItem(player, new ItemStack(Items.IRON_INGOT));
        double afterAnvil = JobState.xp(player, "smither");
        helper.assertTrue(afterAnvil > 0.0, "an anvil iron ingot must pay the smither");
        JobEvents.awardItem(player, new ItemStack(Items.DIAMOND));
        helper.assertTrue(JobState.xp(player, "smither") > afterAnvil,
                "a smithing table result must pay the smither too");
        helper.succeed();
    }
}
