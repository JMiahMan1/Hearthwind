package dev.jmiahman.hearthwind.skills;

import net.minecraft.gametest.framework.GameTestHelper;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.world.entity.EntityTypes;

/**
 * Headless gametests for the skills module; run via
 * custom-mods/tools/run_gametests.sh (same harness as aged-survival).
 */
public final class HearthwindSkillsGameTests {
    /** Public ctor: fabric-loader instantiates gametest entrypoints reflectively. */
    public HearthwindSkillsGameTests() {}

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
        SkillsConfig.get().bonuses.baseStartingHealth = 6.0;
        SkillsConfig.get().bonuses.healthHpPerLevel = 1.0;
        helper.assertTrue(SkillAttributes.bonusFor(pig, Skill.HEALTH) == -14.0,
                "0 health level = -14 HP modifier (6 HP total = 3 hearts)");
        SkillXp.addXp(pig, Skill.HEALTH, SkillXp.xpForLevel(14));
        helper.assertTrue(SkillAttributes.bonusFor(pig, Skill.HEALTH) == 0.0,
                "14 health levels x 1.0 = +14 HP (20 HP total = 10 hearts)");
        helper.assertTrue(SkillAttributes.bonusFor(pig, Skill.SMITHING) == 0.0,
                "unlock-only skills give no attribute bonus");
        helper.succeed();
    }

    @GameTest
    public void mobScalingMath(GameTestHelper helper) {
        SkillsConfig.get().mobScaling.enabled = true;
        helper.assertTrue(MobScaling.stepsFor(100) == 0, "inside grace distance");
        helper.assertTrue(MobScaling.stepsFor(500) == 1, "one step at 500");
        helper.assertTrue(MobScaling.stepsFor(15000) == 60, "capped at maxSteps");
        helper.assertTrue(MobScaling.healthBonus(3) == 3.0
                && Math.abs(MobScaling.damageBonus(3) - 0.9) < 0.001, "per-step bonuses");
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
        // ids that resolve in our pack (~358), not the bundled digest's 577,
        // because the corpus counts entries for mods we do not ship and those
        // are skipped by design.
        helper.assertTrue(counts[0] > 200, "mining break gates loaded: " + counts[0]);
        // distinct ids after placeholder collapse: ~17 vanilla stations
        helper.assertTrue(counts[1] >= 10 && counts[1] < 50,
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

    @GameTest
    public void levelzCorpusLoadsAllGateCategories(GameTestHelper helper) {
        // The migrated corpus lives in the world datapack; load it the same way
        // SERVER_STARTING does and make sure every category resolves ids.
        SkillGates.load(helper.getLevel().getServer().getResourceManager());
        int[] c = SkillGates.debugCategoryCounts();
        // floors sit well under the corpus totals (mining 358, block 17,
        // item 18, crafting 135, smithing 149, brewing 23, entity 14 ids that
        // resolve in our pack) so they catch "nothing loaded" without
        // breaking whenever the migrated tuning grows.
        helper.assertTrue(c[0] > 200, "mining break gates: " + c[0]);
        helper.assertTrue(c[1] > 10, "block-use gates: " + c[1]);
        helper.assertTrue(c[2] > 10, "item-use gates: " + c[2]);
        helper.assertTrue(c[3] > 80, "crafting gates: " + c[3]);
        helper.assertTrue(c[4] > 100, "smithing gates: " + c[4]);
        helper.assertTrue(c[5] > 15, "brewing gates: " + c[5]);
        helper.assertTrue(c[6] > 10, "entity gates: " + c[6]);
        helper.succeed();
    }

    @GameTest
    public void levelzGatesResolveVanillaContent(GameTestHelper helper) {
        SkillGates.load(helper.getLevel().getServer().getResourceManager());
        // mining ladder straight out of the corpus
        helper.assertTrue(gateLevel(SkillGates.breakGate(
                net.minecraft.world.level.block.Blocks.STONE.defaultBlockState())) == 5,
                "stone requires mining 5");
        helper.assertTrue(gateLevel(SkillGates.breakGate(
                net.minecraft.world.level.block.Blocks.IRON_ORE.defaultBlockState())) == 13,
                "iron ore requires mining 13");
        helper.assertTrue(gateLevel(SkillGates.breakGate(
                net.minecraft.world.level.block.Blocks.DIAMOND_ORE.defaultBlockState())) == 21,
                "diamond ore requires mining 21");
        helper.assertTrue(SkillGates.breakGate(
                net.minecraft.world.level.block.Blocks.DIRT.defaultBlockState()) == null,
                "dirt is never gated");
        // smithing + brewing inputs are gated by their own categories
        helper.assertTrue(SkillGates.smithingGate(
                new net.minecraft.world.item.ItemStack(
                        net.minecraft.world.item.Items.FLINT)) != null,
                "flint is a smithing input gate");
        helper.assertTrue(SkillGates.brewingGate(
                new net.minecraft.world.item.ItemStack(
                        net.minecraft.world.item.Items.NETHER_WART)) != null,
                "nether wart is a brewing input gate");
        helper.assertTrue(SkillGates.itemGate(
                new net.minecraft.world.item.ItemStack(
                        net.minecraft.world.item.Items.COMPASS)) != null,
                "compass is an item-use gate");
        helper.assertTrue(SkillGates.itemGate(
                net.minecraft.world.item.ItemStack.EMPTY) == null,
                "empty stack never gated");
        helper.succeed();
    }

    @GameTest
    public void levelzEntityGatesResolve(GameTestHelper helper) {
        SkillGates.load(helper.getLevel().getServer().getResourceManager());
        var cow = helper.spawn(EntityTypes.COW, 1, 2, 1);
        SkillGates.Gate gate = SkillGates.entityGate(cow);
        helper.assertTrue(gate != null && gate.skill() == Skill.FARMING,
                "cow interaction is gated behind farming");
        helper.assertTrue(SkillGates.entityGate((net.minecraft.world.entity.Entity) null) == null,
                "no entity means no gate");
        helper.succeed();
    }

    private static int gateLevel(SkillGates.Gate gate) {
        return gate == null ? -1 : gate.level();
    }

    @GameTest
    public void rocksAreTheUngatedMiningXpSource(GameTestHelper helper) {
        // Every pickaxe-mineable block is gated (sandstone 2, stone 5...), so
        // the surface rocks must award MINING or a fresh player can never
        // level the skill and progression deadlocks.
        var player = helper.makeMockServerPlayerInLevel();
        player.getAbilities().instabuild = false;
        SkillsConfig.get().xp.miningPerBlock = 2;
        // resolved by id: the skills module has no dependency on primitive
        var rock = net.minecraft.core.registries.BuiltInRegistries.BLOCK
                .getOptional(net.minecraft.resources.Identifier
                        .fromNamespaceAndPath("earlystage", "rock"))
                .map(b -> b.defaultBlockState())
                .orElse(null);
        helper.assertTrue(rock != null, "earlystage:rock must be registered");
        SkillEvents.onBlockBroken(player.level(), player,
                new net.minecraft.core.BlockPos(1, 2, 1), rock, null);
        helper.assertTrue(SkillXp.xp(player, Skill.MINING) >= 2,
                "breaking a surface rock must award mining XP");
        // the tag is what keeps this decoupled from the primitive module
        helper.assertTrue(rock.is(net.minecraft.tags.TagKey.create(
                net.minecraft.core.registries.Registries.BLOCK,
                net.minecraft.resources.Identifier.fromNamespaceAndPath(
                        "earlystage", "rock_blocks"))),
                "rock must sit in earlystage:rock_blocks");
        helper.succeed();
    }

    @GameTest
    public void xpHooksAwardFarmingOnCropBreak(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        player.getAbilities().instabuild = false;
        SkillsConfig.get().xp.farmingPerCrop = 5;
        // Break a wheat crop (WHEAT is in CROPS tag when fully grown)
        net.minecraft.world.level.block.state.BlockState wheat =
                net.minecraft.world.level.block.Blocks.WHEAT.defaultBlockState()
                        .setValue(net.minecraft.world.level.block.CropBlock.AGE, 7);
        helper.setBlock(new net.minecraft.core.BlockPos(5, 64, 5), wheat);
        // Directly invoke the event handler (fabric events don't fire
        // through gameMode.destroyBlock() in the gametest framework)
        SkillEvents.onBlockBroken(player.level(), player,
                new net.minecraft.core.BlockPos(5, 64, 5),
                wheat, null);
        helper.assertTrue(SkillXp.xp(player, Skill.FARMING) >= 5,
                "breaking crop must award farming XP");
        helper.succeed();
    }

    @GameTest
    public void xpHooksAwardMiningOnPickaxeBreak(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        player.getAbilities().instabuild = false;
        SkillsConfig.get().xp.miningPerBlock = 3;
        // Break stone (MINEABLE_WITH_PICKAXE)
        var stone = net.minecraft.world.level.block.Blocks.STONE.defaultBlockState();
        helper.setBlock(new net.minecraft.core.BlockPos(5, 64, 5), stone);
        SkillEvents.onBlockBroken(player.level(), player,
                new net.minecraft.core.BlockPos(5, 64, 5),
                stone, null);
        helper.assertTrue(SkillXp.xp(player, Skill.MINING) >= 3,
                "breaking stone must award mining XP");
        helper.assertTrue(SkillXp.xp(player, Skill.FARMING) == 0.0,
                "mining must not award farming XP");
        helper.succeed();
    }

    @GameTest
    public void xpHooksAwardStaminaOnShovelDig(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        player.getAbilities().instabuild = false;
        SkillsConfig.get().xp.staminaPerDig = 2;
        // Break dirt (MINEABLE_WITH_SHOVEL)
        var dirt = net.minecraft.world.level.block.Blocks.DIRT.defaultBlockState();
        helper.setBlock(new net.minecraft.core.BlockPos(5, 64, 5), dirt);
        SkillEvents.onBlockBroken(player.level(), player,
                new net.minecraft.core.BlockPos(5, 64, 5),
                dirt, null);
        helper.assertTrue(SkillXp.xp(player, Skill.STAMINA) >= 2,
                "digging dirt must award stamina XP");
        helper.succeed();
    }

    @GameTest
    public void xpHooksAwardFarmingOnAnimalKill(GameTestHelper helper) {
        SkillsConfig.get().xp.farmingPerAnimalKill = 10;
        var player = helper.makeMockServerPlayerInLevel();
        player.getAbilities().instabuild = false;
        var chicken = helper.spawn(EntityTypes.CHICKEN, 5, 65, 5);
        // Directly invoke the death event handler with player as source entity
        SkillEvents.onDeath(chicken,
                player.level().damageSources().playerAttack(player));
        helper.assertTrue(SkillXp.xp(player, Skill.FARMING) >= 10,
                "killing animal must award farming XP");
        helper.succeed();
    }

    @GameTest
    public void xpHooksAwardStrengthOnMeleeKill(GameTestHelper helper) {
        SkillsConfig.get().xp.strengthPerMeleeKill = 8;
        var player = helper.makeMockServerPlayerInLevel();
        player.getAbilities().instabuild = false;
        var zombie = helper.spawn(EntityTypes.ZOMBIE, 5, 65, 5);
        // Directly invoke the death event handler (fabric events don't fire
        // through player.attack() in the gametest framework)
        SkillEvents.onDeath(zombie,
                player.level().damageSources().playerAttack(player));
        helper.assertTrue(SkillXp.xp(player, Skill.STRENGTH) >= 8,
                "killing mob with melee must award strength XP");
        helper.assertTrue(SkillXp.xp(player, Skill.ARCHERY) == 0.0,
                "melee kill must not award archery XP");
        helper.succeed();
    }

    @GameTest
    public void xpHooksAwardArcheryOnBowKill(GameTestHelper helper) {
        SkillsConfig.get().xp.archeryPerRangedKill = 6;
        var player = helper.makeMockServerPlayerInLevel();
        player.getAbilities().instabuild = false;
        var skeleton = helper.spawn(EntityTypes.SKELETON, 5, 65, 10);
        // Directly invoke the death event handler with a bow-equipped DamageSource
        var arrow = new net.minecraft.world.entity.projectile.arrow.Arrow(
                player.level(), 5.0, 66.0, 10.0,
                new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.ARROW),
                new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BOW));
        SkillEvents.onDeath(skeleton,
                player.level().damageSources().arrow(arrow, player));
        helper.assertTrue(SkillXp.xp(player, Skill.ARCHERY) >= 6,
                "ranged kill must award archery XP");
        helper.succeed();
    }

    @GameTest
    public void skillAttributesApplyOnLevelUp(GameTestHelper helper) {
        var pig = helper.spawn(EntityTypes.PIG, 1, 2, 1);
        SkillsConfig.get().bonuses.baseStartingHealth = 6.0;
        SkillsConfig.get().bonuses.healthHpPerLevel = 2.0;
        SkillXp.addXp(pig, Skill.HEALTH, SkillXp.xpForLevel(5));
        double bonus = SkillAttributes.bonusFor(pig, Skill.HEALTH);
        helper.assertTrue(bonus == -4.0, "5 health levels x 2.0 = -4.0 HP modifier (16 HP total = 8 hearts)");
        helper.succeed();
    }

    @GameTest
    public void skillIdsAreAllLowercase(GameTestHelper helper) {
        for (var skill : Skill.values()) {
            helper.assertTrue(skill.id.equals(skill.id.toLowerCase()),
                    "skill id must be lowercase: " + skill.id);
        }
        helper.succeed();
    }

    @GameTest
    public void partyCreationInviteAndJoinFlow(GameTestHelper helper) {
        var leader = helper.makeMockServerPlayerInLevel();
        var invitee = helper.makeMockServerPlayerInLevel();

        var party = dev.jmiahman.hearthwind.skills.party.PartyManager.createParty(leader, "WolfPack");
        helper.assertTrue(party != null, "Party must be created");
        helper.assertTrue(party.getName().equals("WolfPack"), "Party name must match");
        helper.assertTrue(party.isLeader(leader.getUUID()), "Leader must be party leader");

        boolean invited = dev.jmiahman.hearthwind.skills.party.PartyManager.invitePlayer(leader, invitee);
        helper.assertTrue(invited, "Invite must succeed");

        boolean accepted = dev.jmiahman.hearthwind.skills.party.PartyManager.acceptInvite(invitee);
        helper.assertTrue(accepted, "Accepting invite must succeed");

        helper.assertTrue(dev.jmiahman.hearthwind.skills.party.PartyManager.areInSameParty(leader.getUUID(), invitee.getUUID()),
                "Both players must be in the same party");

        dev.jmiahman.hearthwind.skills.party.PartyManager.leaveParty(invitee);
        helper.assertTrue(!dev.jmiahman.hearthwind.skills.party.PartyManager.areInSameParty(leader.getUUID(), invitee.getUUID()),
                "Players must not be in the same party after leaving");

        dev.jmiahman.hearthwind.skills.party.PartyManager.disbandParty(leader);
        helper.succeed();
    }

    @GameTest
    public void partyFriendlyFireAndPvpToggle(GameTestHelper helper) {
        var leader = helper.makeMockServerPlayerInLevel();
        var member = helper.makeMockServerPlayerInLevel();

        var party = dev.jmiahman.hearthwind.skills.party.PartyManager.createParty(leader, "Heroes");
        dev.jmiahman.hearthwind.skills.party.PartyManager.invitePlayer(leader, member);
        dev.jmiahman.hearthwind.skills.party.PartyManager.acceptInvite(member);

        helper.assertTrue(!party.isPvpEnabled(), "Party PvP must be disabled by default");
        dev.jmiahman.hearthwind.skills.party.PartyManager.togglePvp(leader, true);
        helper.assertTrue(party.isPvpEnabled(), "Party PvP must be enabled after toggle");

        dev.jmiahman.hearthwind.skills.party.PartyManager.disbandParty(leader);
        helper.succeed();
    }

    @GameTest
    public void partySharedXpAwardsNearbyMembers(GameTestHelper helper) {
        dev.jmiahman.hearthwind.skills.party.PartyManager.reset();
        var leader = helper.makeMockServerPlayerInLevel();
        var member = helper.makeMockServerPlayerInLevel();

        var party = dev.jmiahman.hearthwind.skills.party.PartyManager.createParty(leader, "Guild");
        dev.jmiahman.hearthwind.skills.party.PartyManager.invitePlayer(leader, member);
        dev.jmiahman.hearthwind.skills.party.PartyManager.acceptInvite(member);

        double initialMemberMiningXp = SkillXp.xp(member, Skill.MINING);
        dev.jmiahman.hearthwind.skills.party.PartyManager.shareXp(leader, Skill.MINING, 20);

        double newMemberMiningXp = SkillXp.xp(member, Skill.MINING);
        helper.assertTrue(newMemberMiningXp > initialMemberMiningXp, "Nearby party member must receive shared XP");

        dev.jmiahman.hearthwind.skills.party.PartyManager.disbandParty(leader);
        helper.succeed();
    }

    // ---------- P7: skill procs (parity: levelz capstones) ----------

    /**
     * A mock player can NEVER be damaged: {@code GameTestHelper$3} overrides
     * {@code gameMode()} to always return CREATIVE, so {@code hurtServer}
     * short-circuits on the invulnerability check whatever we do. Player-side
     * procs are therefore asserted by driving the fabric damage events
     * directly (same handlers vanilla invokes) and by exercising the mixin
     * through a damageable mob.
     */
    private static net.minecraft.server.level.ServerPlayer newPlayer(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        var abilities = player.getAbilities();
        abilities.instabuild = false;
        abilities.invulnerable = false;
        player.onUpdateAbilities();
        return player;
    }

    private static net.minecraft.util.RandomSource rng(long seed) {
        return net.minecraft.util.RandomSource.create(seed);
    }

    @GameTest
    public void procRollIsChanceDriven(GameTestHelper helper) {
        var random = rng(1234L);
        helper.assertTrue(!SkillProcs.roll(random, 0.0), "a 0% proc must never fire");
        helper.assertTrue(SkillProcs.roll(random, 1.0), "a 100% proc must always fire");
        helper.succeed();
    }

    @GameTest
    public void critChanceScalesWithLuckLevels(GameTestHelper helper) {
        var p = SkillsConfig.get().procs;
        double per = p.critChancePerLuckLevel;
        try {
            p.critChancePerLuckLevel = 0.01;
            helper.assertTrue(SkillProcs.critChance(0) == 0.0, "no crits at luck 0");
            helper.assertTrue(Math.abs(SkillProcs.critChance(15) - 0.15) < 1e-6, "15% crits at luck 15");
            helper.assertTrue(Math.abs(SkillProcs.critChance(30) - 0.3) < 1e-6, "30% crits at luck 30");
        } finally {
            p.critChancePerLuckLevel = per;
        }
        helper.succeed();
    }

    @GameTest
    public void doubleDamageIsAStrengthCapstone(GameTestHelper helper) {
        var player = newPlayer(helper);
        var p = SkillsConfig.get().procs;
        double chance = p.meleeDoubleDamageChance;
        try {
            p.meleeDoubleDamageChance = 1.0;
            SkillXp.setLevel(player, Skill.STRENGTH, 0);
            var low = SkillProcs.rollMelee(player, rng(7L));
            helper.assertTrue(!low.doubled(), "no double hits below max strength");
            SkillXp.setLevel(player, Skill.STRENGTH, SkillXp.maxLevel());
            var max = SkillProcs.rollMelee(player, rng(7L));
            helper.assertTrue(max.doubled(), "max strength rolls the double-damage proc");
            helper.assertTrue(SkillProcs.applyMelee(5f, max) == 10f, "double damage doubles the hit");
        } finally {
            p.meleeDoubleDamageChance = chance;
        }
        helper.succeed();
    }

    @GameTest
    public void critAddsBonusMeleeDamage(GameTestHelper helper) {
        var player = newPlayer(helper);
        var p = SkillsConfig.get().procs;
        double chance = p.meleeDoubleDamageChance;
        double crit = p.critChancePerLuckLevel;
        try {
            p.meleeDoubleDamageChance = 0.0;
            p.critChancePerLuckLevel = 1.0;
            SkillXp.setLevel(player, Skill.LUCK, 0);
            var none = SkillProcs.rollMelee(player, rng(11L));
            helper.assertTrue(!none.crit(), "no crit at luck 0");
            SkillXp.setLevel(player, Skill.LUCK, 5);
            var rolled = SkillProcs.rollMelee(player, rng(11L));
            helper.assertTrue(rolled.crit(), "crit rolls with luck levels");
            helper.assertTrue(Math.abs(rolled.multiplier() - 1.2f) < 1e-6, "crit is a +20% bonus");
            helper.assertTrue(Math.abs(SkillProcs.applyMelee(10f, rolled) - 12f) < 1e-6, "10 damage crits for 12");
        } finally {
            p.meleeDoubleDamageChance = chance;
            p.critChancePerLuckLevel = crit;
        }
        helper.succeed();
    }

    @GameTest
    public void capstoneProcsNeedMaxLevel(GameTestHelper helper) {
        var player = newPlayer(helper);
        var p = SkillsConfig.get().procs;
        double miss = p.missChance, reflect = p.reflectChance;
        double survive = p.surviveChance, twin = p.twinBabyChance;
        try {
            p.missChance = 1.0;
            p.reflectChance = 1.0;
            p.surviveChance = 1.0;
            p.twinBabyChance = 1.0;
            helper.assertTrue(!SkillProcs.dodges(player, rng(3L)), "dodge needs max agility");
            helper.assertTrue(!SkillProcs.reflects(player, rng(3L)), "reflect needs max defense");
            helper.assertTrue(!SkillProcs.survivesDeath(player, rng(3L)), "survive needs max luck");
            helper.assertTrue(!SkillProcs.twinBaby(player, rng(3L)), "twins need max farming");

            SkillXp.setLevel(player, Skill.AGILITY, SkillXp.maxLevel());
            SkillXp.setLevel(player, Skill.DEFENSE, SkillXp.maxLevel());
            SkillXp.setLevel(player, Skill.LUCK, SkillXp.maxLevel());
            SkillXp.setLevel(player, Skill.FARMING, SkillXp.maxLevel());
            helper.assertTrue(SkillProcs.dodges(player, rng(3L)), "max agility dodges");
            helper.assertTrue(SkillProcs.reflects(player, rng(3L)), "max defense reflects");
            helper.assertTrue(SkillProcs.survivesDeath(player, rng(3L)), "max luck survives");
            helper.assertTrue(SkillProcs.twinBaby(player, rng(3L)), "max farming twins");
        } finally {
            p.missChance = miss;
            p.reflectChance = reflect;
            p.surviveChance = survive;
            p.twinBabyChance = twin;
        }
        helper.succeed();
    }

    @GameTest
    public void fallProtectionScalesWithAgility(GameTestHelper helper) {
        var player = newPlayer(helper);
        var p = SkillsConfig.get().procs;
        double per = p.fallProtectionPerAgilityLevel;
        try {
            p.fallProtectionPerAgilityLevel = 0.25;
            SkillXp.setLevel(player, Skill.AGILITY, 0);
            helper.assertTrue(SkillProcs.applyFallProtection(player, 5f) == 5f, "no reduction at agility 0");
            SkillXp.setLevel(player, Skill.AGILITY, 4);
            helper.assertTrue(SkillProcs.applyFallProtection(player, 5f) == 4f, "agility 4 soaks 1 point");
            SkillXp.setLevel(player, Skill.AGILITY, SkillXp.maxLevel());
            helper.assertTrue(SkillProcs.applyFallProtection(player, 5f) == 0f, "reduction never goes negative");
        } finally {
            p.fallProtectionPerAgilityLevel = per;
        }
        helper.succeed();
    }

    @GameTest
    public void meleeProcsApplyThroughHurt(GameTestHelper helper) {
        var attacker = newPlayer(helper);
        var p = SkillsConfig.get().procs;
        double dmg = p.meleeDoubleDamageChance, crit = p.critChancePerLuckLevel;
        try {
            p.meleeDoubleDamageChance = 1.0;
            p.critChancePerLuckLevel = 0.0;
            SkillXp.setLevel(attacker, Skill.STRENGTH, SkillXp.maxLevel());
            var pig = helper.spawn(EntityTypes.PIG, 1, 1, 1);
            float before = pig.getHealth();
            pig.hurtServer(helper.getLevel(), attacker.damageSources().playerAttack(attacker), 4f);
            helper.assertTrue(Math.abs((before - pig.getHealth()) - 8f) < 0.01f,
                    "max strength turns a 4 hit into 8");

            p.meleeDoubleDamageChance = 0.0;
            p.critChancePerLuckLevel = 1.0;
            SkillXp.setLevel(attacker, Skill.STRENGTH, 0);
            SkillXp.setLevel(attacker, Skill.LUCK, 3);
            var second = helper.spawn(EntityTypes.PIG, 3, 1, 1);
            float before2 = second.getHealth();
            second.hurtServer(helper.getLevel(), attacker.damageSources().playerAttack(attacker), 5f);
            helper.assertTrue(Math.abs((before2 - second.getHealth()) - 6f) < 0.01f,
                    "a crit turns a 5 hit into 6");
        } finally {
            p.meleeDoubleDamageChance = dmg;
            p.critChancePerLuckLevel = crit;
        }
        helper.succeed();
    }

    @GameTest
    public void dodgeCancelsDamageAtMaxAgility(GameTestHelper helper) {
        var dodger = newPlayer(helper);
        var target = newPlayer(helper);
        var p = SkillsConfig.get().procs;
        double miss = p.missChance;
        try {
            p.missChance = 1.0;
            SkillXp.setLevel(dodger, Skill.AGILITY, SkillXp.maxLevel());
            boolean dodged = net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.ALLOW_DAMAGE
                    .invoker().allowDamage(dodger, dodger.damageSources().generic(), 5f);
            helper.assertTrue(!dodged, "the attack must miss at max agility");

            SkillXp.setLevel(target, Skill.AGILITY, 0);
            boolean hit = net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.ALLOW_DAMAGE
                    .invoker().allowDamage(target, target.damageSources().generic(), 5f);
            helper.assertTrue(hit, "without the capstone the hit lands");
        } finally {
            p.missChance = miss;
        }
        helper.succeed();
    }

    @GameTest
    public void fallReductionIsAPlayerPerk(GameTestHelper helper) {
        // The reduction is applied by SkillProcDamageMixin for players only;
        // mobs must take fall damage untouched.
        var pig = helper.spawn(EntityTypes.PIG, 1, 1, 1);
        float before = pig.getHealth();
        pig.hurtServer(helper.getLevel(), pig.damageSources().fall(), 2f);
        helper.assertTrue(Math.abs((before - pig.getHealth()) - 2f) < 0.01f,
                "mobs take full fall damage");
        helper.succeed();
    }

    @GameTest
    public void skillProcMixinsAreWired(GameTestHelper helper) throws Exception {
        // Loading a mixin class directly fails (Mixin strips it), so assert the
        // wiring through the mod's own mixin config instead. This catches the
        // "mixins exist but are never listed" failure, which is silent at boot.
        var path = net.fabricmc.loader.api.FabricLoader.getInstance()
                .getModContainer("hearthwind_skills")
                .orElseThrow()
                .findPath("hearthwind_skills.mixins.json")
                .orElseThrow();
        String json;
        try (var in = java.nio.file.Files.newInputStream(path)) {
            json = new String(in.readAllBytes());
        }
        for (String mixin : new String[] { "SkillProcDamageMixin", "SkillProcTwinMixin" }) {
            helper.assertTrue(json.contains("\"" + mixin + "\""), "mixin config must list " + mixin);
        }
        helper.succeed();
    }

    @GameTest
    public void surviveDeathCapstoneKeepsPlayerAlive(GameTestHelper helper) {
        var player = newPlayer(helper);
        var p = SkillsConfig.get().procs;
        double survive = p.surviveChance;
        try {
            p.surviveChance = 1.0;
            SkillXp.setLevel(player, Skill.LUCK, SkillXp.maxLevel());
            player.setHealth(0.5f);
            boolean died = net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.ALLOW_DEATH
                    .invoker().allowDeath(player, player.damageSources().generic(), 100f);
            helper.assertTrue(!died, "max luck must cheat death");
            helper.assertTrue(player.getHealth() == 1.0f, "survives on 1 HP");
            helper.assertTrue(player.hasEffect(net.minecraft.world.effect.MobEffects.REGENERATION),
                    "regeneration granted");
            helper.assertTrue(player.hasEffect(net.minecraft.world.effect.MobEffects.ABSORPTION),
                    "absorption granted");
        } finally {
            p.surviveChance = survive;
        }
        helper.succeed();
    }

    @GameTest
    public void reflectCapstoneDamagesTheAttacker(GameTestHelper helper) {
        var victim = newPlayer(helper);
        var attacker = helper.spawn(EntityTypes.PIG, 2, 1, 2);
        var p = SkillsConfig.get().procs;
        double reflect = p.reflectChance;
        try {
            p.reflectChance = 1.0;
            SkillXp.setLevel(victim, Skill.DEFENSE, SkillXp.maxLevel());
            float before = attacker.getHealth();
            net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.AFTER_DAMAGE.invoker()
                    .afterDamage(victim, victim.damageSources().mobAttack(attacker), 3f, 3f, false);
            helper.assertTrue(Math.abs((before - attacker.getHealth()) - 3f) < 0.01f,
                    "the attacker takes the hit straight back");
            helper.assertTrue(attacker.getHealth() > 0f, "thorns reflect must not ping-pong");
        } finally {
            p.reflectChance = reflect;
        }
        helper.succeed();
    }
}
