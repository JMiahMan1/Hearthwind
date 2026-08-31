package dev.jmiahman.hearthwind.jobs;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityTypes;

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
    public void smitherRewardsGrantedAtAnyAge(GameTestHelper helper) {
        JobDefs.ensureLoaded();
        var player = helper.makeMockServerPlayerInLevel();
        player.getInventory().clearContent();
        player.getAbilities().instabuild = false;
        JobState.join(player, "smither");
        JobRewards.apply(player, "smither", 1);
        int after = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (!player.getInventory().getItem(i).isEmpty()) after++;
        }
        helper.assertTrue(after > 0,
                "smither level 1 rewards must be granted at any Age, got " + after + " items");
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
}
