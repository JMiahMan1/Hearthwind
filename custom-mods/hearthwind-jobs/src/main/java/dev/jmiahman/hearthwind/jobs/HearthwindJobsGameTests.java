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
}
