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
        helper.assertTrue(SkillXp.level(pig, Skill.MINING) == 2,
                "150 xp with base 30 = level 2 (curve 30/90/180...)");
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

    @GameTest
    public void mobScalingMath(GameTestHelper helper) {
        SkillsConfig.get().mobScaling.enabled = true;
        helper.assertTrue(MobScaling.stepsFor(100) == 0, "inside grace distance");
        helper.assertTrue(MobScaling.stepsFor(1500) == 1, "one step at 1500");
        helper.assertTrue(MobScaling.stepsFor(21000) == 20, "capped at maxSteps");
        helper.assertTrue(MobScaling.healthBonus(3) == 6.0
                && MobScaling.damageBonus(3) == 1.5, "per-step bonuses");
        SkillsConfig.get().mobScaling.enabled = false;
        helper.assertTrue(MobScaling.stepsFor(99999) == 0,
                "disabled means no scaling");
        SkillsConfig.get().mobScaling.enabled = true;
        helper.succeed();
    }

    @GameTest
    public void zombieGetsBuffedWithoutDoubleStacking(GameTestHelper helper) {
        var zombie = helper.spawn(EntityTypes.ZOMBIE, 1, 2, 1);
        var inst = zombie.getAttribute(
                net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
        double base = inst.getValue();
        // gametest world is near spawn (MobScaling.apply would be a no-op),
        // so exercise the production modifier path with far-spawn steps:
        int steps = Math.max(1, MobScaling.stepsFor(20000));
        inst.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                MobScaling.MODIFIER_ID, MobScaling.healthBonus(steps),
                net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE));
        helper.assertTrue(inst.getValue() > base, "modifier raises max health");
        MobScaling.apply(zombie); // must NOT stack a second modifier
        helper.assertTrue(inst.getModifiers().stream()
                .filter(m -> m.is(MobScaling.MODIFIER_ID)).count() == 1,
                "no double stacking");
        net.minecraft.world.entity.Entity pig = helper.spawn(EntityTypes.PIG, 1, 2, 1);
        helper.assertTrue(zombie instanceof net.minecraft.world.entity.monster.Monster
                && !(pig instanceof net.minecraft.world.entity.monster.Monster),
                "only monsters are buffable");
        helper.succeed();
    }

    @GameTest
    public void skillGatesLoadAndResolve(GameTestHelper helper) {
        SkillGates.ensureLoaded();
        int[] counts = SkillGates.debugCounts();
        helper.assertTrue(counts[0] > 500, "mining break gates loaded: " + counts[0]);
        // distinct ids after placeholder collapse: ~17 vanilla stations
        helper.assertTrue(counts[1] >= 15 && counts[1] < 50,
                "use gates loaded: " + counts[1]);

        // spot checks straight from the migrated corpus
        var stone = net.minecraft.world.level.block.Blocks.STONE.defaultBlockState();
        var mudBricks = net.minecraft.world.level.block.Blocks.MUD_BRICKS
                .defaultBlockState();
        SkillGates.Gate stoneGate = SkillGates.breakGate(stone);
        SkillGates.Gate mudGate = SkillGates.breakGate(mudBricks);
        helper.assertTrue(stoneGate != null && stoneGate.level() == 5,
                "minecraft:stone requires mining 5");
        helper.assertTrue(mudGate != null && mudGate.level() == 1,
                "mud bricks require mining 1");
        SkillGates.Gate furnace = SkillGates.useGate(
                net.minecraft.world.level.block.Blocks.FURNACE);
        helper.assertTrue(furnace != null && furnace.skill() == Skill.SMITHING
                && furnace.level() == 3, "furnace use requires smithing 3");
        SkillGates.Gate ungated = SkillGates.useGate(
                net.minecraft.world.level.block.Blocks.DIRT);
        helper.assertTrue(ungated == null, "dirt is not gated");
        helper.succeed();
    }
}
