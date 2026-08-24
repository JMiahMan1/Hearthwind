package dev.jmiahman.aged.skills;

import net.minecraft.gametest.framework.GameTestHelper;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.world.entity.EntityTypes;

/**
 * Headless gametests for the skills module; run via
 * custom-mods/tools/run_gametests.sh (same harness as aged-survival).
 */
public final class AgedSkillsGameTests {
    /** Public ctor: fabric-loader instantiates gametest entrypoints reflectively. */
    public AgedSkillsGameTests() {}

    @GameTest
    public void levelCurveIsMonotonicAndCapped(GameTestHelper helper) {
        SkillsConfig.get().levels.baseXpPerLevel = 30;
        helper.assertTrue(SkillXp.xpForLevel(1) == 30, "level 1 costs 30");
        helper.assertTrue(SkillXp.xpForLevel(2) == 90, "level 2 cumulative 90");
        long prev = -1;
        for (int l = 0; l <= SkillXp.maxLevel(); l++) {
            long cost = SkillXp.xpForLevel(l);
            helper.assertTrue(cost >= prev, "curve must be monotonic");
            prev = cost;
        }
        helper.assertTrue(SkillXp.levelFor(Long.MAX_VALUE)
                == SkillXp.maxLevel(), "levels cap at maxLevel");
        helper.assertTrue(SkillXp.levelFor(29) == 0
                && SkillXp.levelFor(31) == 1, "boundary at first level");
        helper.succeed();
    }

    @GameTest
    public void xpAccruesAndPersistsOnEntity(GameTestHelper helper) {
        var pig = helper.spawn(EntityTypes.PIG, 1, 2, 1);
        SkillXp.addXp(pig, Skill.MINING, 100);
        SkillXp.addXp(pig, Skill.MINING, 50);
        helper.assertTrue(SkillXp.xp(pig, Skill.MINING) == 150.0,
                "xp accumulates in attachment");
        helper.assertTrue(SkillXp.level(pig, Skill.MINING) == 1,
                "150 xp with base 30 = level 1");
        helper.assertTrue(SkillXp.xp(pig, Skill.ARCHERY) == 0.0,
                "other skills untouched");
        helper.succeed();
    }

    @GameTest
    public void maxedSkillStopsAccruing(GameTestHelper helper) {
        var pig = helper.spawn(EntityTypes.PIG, 1, 2, 1);
        double huge = Double.MAX_VALUE / 4;
        SkillXp.addXp(pig, Skill.STRENGTH, huge);
        int level = SkillXp.level(pig, Skill.STRENGTH);
        helper.assertTrue(level == SkillXp.maxLevel(), "huge xp caps level");
        SkillXp.addXp(pig, Skill.STRENGTH, huge); // must be a no-op, not overflow
        helper.assertTrue(SkillXp.level(pig, Skill.STRENGTH) == SkillXp.maxLevel(),
                "still capped after further adds");
        helper.succeed();
    }

    @GameTest
    public void attributeBonusMath(GameTestHelper helper) {
        var pig = helper.spawn(EntityTypes.PIG, 1, 2, 1);
        SkillsConfig.get().bonuses.healthHpPerLevel = 0.5;
        SkillXp.addXp(pig, Skill.HEALTH, SkillXp.xpForLevel(10));
        helper.assertTrue(SkillAttributes.bonusFor(pig, Skill.HEALTH) == 5.0,
                "10 health levels x 0.5 = +5 HP");
        helper.assertTrue(SkillAttributes.bonusFor(pig, Skill.SMITHING) == 0.0,
                "unlock-only skills give no attribute bonus");
        helper.succeed();
    }
}
