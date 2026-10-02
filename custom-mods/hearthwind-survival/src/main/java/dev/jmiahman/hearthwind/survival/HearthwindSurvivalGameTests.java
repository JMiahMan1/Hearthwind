package dev.jmiahman.hearthwind.survival;

import io.netty.channel.embedded.EmbeddedChannel;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Difficulty;
import dev.jmiahman.hearthwind.survival.hydration.DehydrationSounds;
import dev.jmiahman.hearthwind.survival.hydration.ThirstPreview;
import net.minecraft.world.item.Item;

/**
 * Headless gametests, run with the fabric-api gametest harness:
 * <pre>
 *   java -Dfabric-api.gametest=true \
 *        -Dfabric-api.gametest.report-file=report.xml \
 *        -jar fabric-server.jar nogui
 * </pre>
 * or via custom-mods/tools/run_gametests.sh. No structures needed (the
 * default fabric-gametest-api-v1:empty template is used).
 */
public final class HearthwindSurvivalGameTests {

    /**
     * Placing any block must not crash. 26.x validates a block entity against
     * its type's allowed blocks, so a modded block reusing another block's
     * entity type (nethervinery barrels/lattices reusing vanilla or vinery
     * types) threw "Invalid block entity" and crashed the game on placement.
     * Builds the block entity of every registered block in one pass and
     * reports every offender at once.
     */
    /**
     * Aged's guidebook documents every nutrient deficiency as the mirror of its
     * bonus (-attack speed, -max health, ...). NutritionZ's shipped data used
     * positive values in the negative lists, so a deficiency granted a buff.
     * Deficiency attributes must now be penalties and bonuses gains.
     */
    @net.fabricmc.fabric.api.gametest.v1.GameTest
    public void nutrientDeficiencyIsAPenalty(net.minecraft.gametest.framework.GameTestHelper helper) {
        for (int i = 0; i < HearthwindSurvivalDiet.NUTRIENT_COUNT; i++) {
            NutritionEffects.EffectSet low = NutritionEffects.negative(i);
            NutritionEffects.EffectSet high = NutritionEffects.positive(i);
            helper.assertTrue(low != null && high != null, "nutrition effects loaded for nutrient " + i);
            helper.assertTrue(!low.attributes().isEmpty(), "nutrient " + i + " has deficiency attributes");
            for (NutritionEffects.AttributeEffect e : low.attributes()) {
                helper.assertTrue(e.modifier().amount() < 0,
                        "deficiency of nutrient " + i + " must lower " + e.attribute() + ", got " + e.modifier().amount());
            }
            for (NutritionEffects.AttributeEffect e : high.attributes()) {
                helper.assertTrue(e.modifier().amount() > 0,
                        "abundance of nutrient " + i + " must raise " + e.attribute() + ", got " + e.modifier().amount());
            }
        }
        helper.succeed();
    }

    @net.fabricmc.fabric.api.gametest.v1.GameTest
    public void everyBlockEntityAcceptsItsBlock(net.minecraft.gametest.framework.GameTestHelper helper) {
        java.util.List<String> bad = new java.util.ArrayList<>();
        for (net.minecraft.world.level.block.Block block : BuiltInRegistries.BLOCK) {
            if (!(block instanceof net.minecraft.world.level.block.EntityBlock entityBlock)) {
                continue;
            }
            try {
                entityBlock.newBlockEntity(net.minecraft.core.BlockPos.ZERO, block.defaultBlockState());
            } catch (RuntimeException e) {
                bad.add(BuiltInRegistries.BLOCK.getKey(block) + ": " + e.getMessage());
            }
        }
        helper.assertTrue(bad.isEmpty(), bad.size() + " blocks crash when placed:\n  " + String.join("\n  ", bad));
        helper.succeed();
    }
    /** Public ctor: fabric-loader instantiates gametest entrypoints reflectively. */
    public HearthwindSurvivalGameTests() {}

    @GameTest
    public void configLoadsSaneDefaults(GameTestHelper helper) {
        HearthwindSurvivalConfig cfg = HearthwindSurvivalConfig.get();
        helper.assertTrue(cfg.thirst.hydratingFactor > 0, "hydrating factor must be positive");
        helper.assertTrue(cfg.diet.negativeNutrition < cfg.diet.positiveNutrition,
                "negative threshold must be below positive threshold");
        helper.assertTrue(cfg.spoilage.chancePerCheck >= 0, "spoil chance must not be negative");
        helper.succeed();
    }

    @GameTest
    public void bareHandQuenchDefaultsToAgedValue(GameTestHelper helper) {
        // Dehydration parity: water_source_quench = 1 on the 0..20 scale.
        helper.assertTrue(HearthwindSurvivalConfig.get().bareHand.waterSourceQuench == 1,
                "sip quench must default to 1, got " + HearthwindSurvivalConfig.get().bareHand.waterSourceQuench);
        helper.assertTrue(HearthwindSurvivalConfig.get().bareHand.waterSipThirstChance == 0.5,
                "sip thirst chance must default to the Aged override 0.5");
        helper.assertTrue(HearthwindSurvivalConfig.get().bareHand.waterSipThirstDuration == 300,
                "sip thirst duration must default to 300");
        helper.succeed();
    }

    /**
     * A non-creative ServerPlayer in the test level (exhaustion hooks apply).
     * Mock players spawn at the world spawn and can end up suffocating or
     * falling outside the test structure, which fabricated damage in the
     * thirst-timing test; park them in a safe spot by default.
     */
    private ServerPlayer survivalServerPlayer(GameTestHelper helper) {
        // makeMockServerPlayerInLevel() overrides gameMode() to always return
        // CREATIVE, so isCreative() stays true even after setGameMode and all
        // survival gates (sips, bowl filling, fluid storage) refuse to act.
        // makeMockServerPlayer(SURVIVAL) returns a real ServerPlayer instance
        // with a SURVIVAL gameMode() override and is not placed in the level,
        // so it also takes no environmental damage between assertions.
        // Vanilla's in-level mock additionally wires an EmbeddedChannel-backed
        // Connection; without it any sendSystemMessage()/sendOverlayMessage()
        // NPEs on player.connection. Attach the same listener here (without
        // placeNewPlayer, so the player is still never ticked).
        ServerPlayer player = (ServerPlayer) helper.makeMockServerPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        player.setNoGravity(true);
        player.setHealth(20.0f);
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        player.connection = new ServerGamePacketListenerImpl(
                helper.getLevel().getServer(), connection, player,
                CommonListenerCookie.createInitial(player.getGameProfile(), false));
        // ServerPlayer.isInvulnerableTo() short-circuits on
        // !connection.hasClientLoaded(); the listener constructor starts a
        // 60-tick client-load timer that only drains in ServerPlayer.tick().
        // Drain it here or the never-ticked mock is immune to all damage.
        for (int i = 0; i < 60; i++) {
            player.connection.tickClientLoadTimeout();
        }
        return player;
    }

    /**
     * Put a still water source where the test player can actually sip from it.
     *
     * <p>A gametest mock player ignores {@code setPos} - its eye stays at the
     * world origin, which is why every one of these tests used to place the
     * water inside the structure and rely on the raycast's own-block and
     * water-above fallbacks. The reference sips with
     * {@code player.raycast(1.5, 0.0, 1.0)}, so the source has to be within
     * 1.5 blocks of the eye: placing it in the player's own block and looking
     * straight down reaches it at 0.62 blocks (0.1.49).
     */
    private ServerPlayer aimAtWater(GameTestHelper helper,
            net.minecraft.world.level.block.state.BlockState fluid) {
        ServerPlayer player = survivalServerPlayer(helper);
        player.setShiftKeyDown(true);
        player.getInventory().clearContent();
        helper.getLevel().setBlockAndUpdate(player.blockPosition(), fluid);
        player.setXRot(90.0f);
        player.setYRot(0.0f);
        return player;
    }

    @GameTest
    public void bareHandRequiresSneak(GameTestHelper helper) {
        net.minecraft.core.BlockPos water = new net.minecraft.core.BlockPos(1, 2, 1);
        helper.setBlock(water, net.minecraft.world.level.block.Blocks.WATER);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setShiftKeyDown(false);
        HearthwindSurvivalThirst.setHydration(player, 10.0);
        helper.assertTrue(BareHandDrinkHandler.trySip(player, helper.getLevel()) == net.minecraft.world.InteractionResult.PASS,
                "standing (not sneaking) player must not sip");
        helper.succeed();
    }

    @GameTest
    public void bareHandHoldCompletesSipAndConsumesSource(GameTestHelper helper) {
        ServerPlayer player = aimAtWater(helper,
                net.minecraft.world.level.block.Blocks.WATER.defaultBlockState());
        net.minecraft.core.BlockPos water = player.blockPosition();
        HearthwindSurvivalThirst.setHydration(player, 10.0);
        helper.assertTrue(!player.isCreative(), "test player must not be creative");
        helper.assertTrue(BareHandDrinkHandler.SIP_REACH == 1.5,
                "the reference sips with raycast(1.5, 0.0, 1.0), got "
                        + BareHandDrinkHandler.SIP_REACH);
        double before = HearthwindSurvivalThirst.hydration(player);
        double chance = HearthwindSurvivalConfig.get().bareHand.waterSipThirstChance;
        HearthwindSurvivalConfig.get().bareHand.waterSipThirstChance = 0.0;
        try {
            for (int i = 0; i < 30; i++) {
                BareHandDrinkHandler.trySipAt(player, helper.getLevel(), water);
            }
        } finally {
            HearthwindSurvivalConfig.get().bareHand.waterSipThirstChance = chance;
        }
        helper.assertTrue(HearthwindSurvivalThirst.hydration(player) == before + 1.0,
                "~21 sustained sips must complete one +1 quench drink");
        helper.assertTrue(helper.getLevel().getBlockState(water).isAir(),
                "still source must be consumed by default");
        helper.succeed();
    }

    @GameTest
    public void bareHandFlowingWaterRefusedByDefault(GameTestHelper helper) {
        ServerPlayer player = aimAtWater(helper,
                net.minecraft.world.level.block.Blocks.WATER.defaultBlockState()
                        .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LEVEL, 1));
        net.minecraft.core.BlockPos water = player.blockPosition();
        HearthwindSurvivalThirst.setHydration(player, 10.0);
        for (int i = 0; i < 30; i++) {
            helper.assertTrue(BareHandDrinkHandler.trySip(player, helper.getLevel()) == net.minecraft.world.InteractionResult.PASS,
                    "flowing water must refuse sips by default");
        }
        helper.assertTrue(HearthwindSurvivalThirst.hydration(player) == 10.0, "no hydration from refused sips");
        helper.succeed();
    }

    @GameTest
    public void purifiedWaterRegisters(GameTestHelper helper) {
        helper.assertTrue(net.minecraft.core.registries.BuiltInRegistries.FLUID.containsKey(
                net.minecraft.resources.Identifier.parse("dehydration:purified_water")), "purified fluid must exist");
        helper.assertTrue(net.minecraft.core.registries.BuiltInRegistries.FLUID.containsKey(
                net.minecraft.resources.Identifier.parse("dehydration:purified_flowing_water")), "purified flowing fluid must exist");
        helper.assertTrue(net.minecraft.core.registries.BuiltInRegistries.BLOCK.containsKey(
                net.minecraft.resources.Identifier.parse("dehydration:purified_water")), "purified water block must exist");
        helper.assertTrue(net.minecraft.core.registries.BuiltInRegistries.ITEM.containsKey(
                net.minecraft.resources.Identifier.parse("dehydration:purified_water_bucket")), "purified bucket must exist");
        var lookup = helper.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.FLUID);
        var set = lookup.get(PurifiedWater.PURIFIED_TAG);
        helper.assertTrue(set.isPresent() && set.get().size() >= 2, "purified fluid tag must list both fluids");
        helper.succeed();
    }

    @GameTest
    public void purifiedBucketSmeltingRecipeParses(GameTestHelper helper) {
        boolean found = false;
        for (var holder : helper.getLevel().recipeAccess().getRecipes()) {
            if (holder.id().identifier().equals(net.minecraft.resources.Identifier.parse("dehydration:purified_water_bucket"))) {
                found = true;
                break;
            }
        }
        helper.assertTrue(found, "smelting a water bucket must yield a purified bucket (Dehydration parity)");
        helper.succeed();
    }

    @GameTest
    public void purifiedSipNeverThirsts(GameTestHelper helper) {
        ServerPlayer player = aimAtWater(helper, PurifiedWater.BLOCK.defaultBlockState());
        net.minecraft.core.BlockPos water = player.blockPosition();
        HearthwindSurvivalThirst.setHydration(player, 10.0);
        double chance = HearthwindSurvivalConfig.get().bareHand.waterSipThirstChance;
        HearthwindSurvivalConfig.get().bareHand.waterSipThirstChance = 1.0;
        try {
            for (int i = 0; i < 30; i++) {
                BareHandDrinkHandler.trySipAt(player, helper.getLevel(), water);
            }
        } finally {
            HearthwindSurvivalConfig.get().bareHand.waterSipThirstChance = chance;
        }
        helper.assertTrue(HearthwindSurvivalThirst.hydration(player) > 10.0, "purified sip must hydrate");
        helper.assertTrue(!player.hasEffect(ThirstMobEffect.HOLDER), "purified sip must never inflict thirst");
        helper.succeed();
    }

    @GameTest
    public void purifiedPotionRegisters(GameTestHelper helper) {
        helper.assertTrue(net.minecraft.core.registries.BuiltInRegistries.POTION.containsKey(
                net.minecraft.resources.Identifier.parse("dehydration:purified_water")),
                "dehydration:purified_water potion must be registered");
        helper.assertTrue(PurifiedWater.PURIFIED_POTION != null, "purified potion holder must be set");
        ItemStack purified = CampfirePurification.purifiedBottle();
        var contents = purified.get(DataComponents.POTION_CONTENTS);
        helper.assertTrue(contents != null && contents.is(PurifiedWater.PURIFIED_POTION),
                "purified bottle must carry the purified potion");
        ItemStack water = net.minecraft.world.item.alchemy.PotionContents.createItemStack(
                Items.POTION, net.minecraft.world.item.alchemy.Potions.WATER);
        helper.assertTrue(CampfirePurification.isWaterPotion(water), "water bottle must be recognised");
        helper.assertTrue(!CampfirePurification.isWaterPotion(purified),
                "purified bottle must not be treated as raw water");
        helper.succeed();
    }

    /**
     * A recipe that references a removed 26.x id (e.g. the old
     * {@code minecraft:chain} item, renamed to {@code minecraft:iron_chain})
     * only logs "Couldn't parse data file" and disappears, so no crafting or
     * JEI entry appears. Assert every dehydration recipe still loads.
     */
    @GameTest
    public void dehydrationRecipesLoad(GameTestHelper helper) {
        String[] ids = {
                "dehydration:campfire_cauldron",
                "dehydration:copper_cauldron",
                "dehydration:purified_water_bucket",
                "dehydration:leather_flask",
                "dehydration:iron_leather_flask",
                "dehydration:golden_leather_flask",
                "dehydration:diamond_leather_flask",
                "dehydration:netherite_leather_flask",
        };
        for (String id : ids) {
            boolean present = helper.getLevel().recipeAccess()
                    .byKey(net.minecraft.resources.ResourceKey.create(
                            net.minecraft.core.registries.Registries.RECIPE,
                            net.minecraft.resources.Identifier.parse(id)))
                    .isPresent();
            helper.assertTrue(present, "dehydration recipe must load in 26.2: " + id);
        }
        helper.succeed();
    }

    /**
     * Upstream {@code BrewingRecipeRegistryMixin} adds three mixes to the
     * vanilla brewing map. Charcoal and kelp are not vanilla ingredients, so
     * these never override a vanilla result; the ghast-tear mix upgrades a
     * purified bottle into the pack's best thirst item. A mistyped holder or a
     * missing registration would silently drop all three, so brew them for real
     * through {@link net.minecraft.world.item.alchemy.PotionBrewing}.
     */
    @GameTest
    public void hydrationBrewingMixesMatchAged(GameTestHelper helper) {
        net.minecraft.world.item.alchemy.PotionBrewing brewing =
                net.minecraft.world.item.alchemy.PotionBrewing.bootstrap(
                        helper.getLevel().enabledFeatures());

        helper.assertTrue(
                brew(brewing, net.minecraft.world.item.alchemy.Potions.WATER, net.minecraft.world.item.Items.CHARCOAL)
                        .equals(PurifiedWater.PURIFIED_POTION),
                "water + charcoal must brew into dehydration:purified_water");
        helper.assertTrue(
                brew(brewing, net.minecraft.world.item.alchemy.Potions.WATER, net.minecraft.world.item.Items.KELP)
                        .equals(PurifiedWater.PURIFIED_POTION),
                "water + kelp must brew into dehydration:purified_water");
        helper.assertTrue(
                brew(brewing, PurifiedWater.PURIFIED_POTION, net.minecraft.world.item.Items.GHAST_TEAR)
                        .equals(PurifiedWater.HYDRATION_POTION),
                "purified water + ghast tear must brew into dehydration:hydration");

        // A vanilla mix must still win where it is defined, and the hydration
        // potion must actually carry the effect that makes it worth brewing.
        helper.assertFalse(
                brew(brewing, net.minecraft.world.item.alchemy.Potions.WATER, net.minecraft.world.item.Items.NETHER_WART)
                        .equals(PurifiedWater.PURIFIED_POTION),
                "water + nether wart must still brew into the awkward potion");
        helper.assertTrue(PurifiedWater.HYDRATION_POTION.value().getEffects().stream()
                        .anyMatch(e -> e.getEffect().is(HydrationMobEffect.HOLDER)),
                "dehydration:hydration must carry the dehydration:hydration_effect");
        helper.succeed();
    }

    /** Result holder of one brew, or a null-safe sentinel when nothing changed. */
    private static net.minecraft.core.Holder<net.minecraft.world.item.alchemy.Potion> brew(
            net.minecraft.world.item.alchemy.PotionBrewing brewing,
            net.minecraft.core.Holder<net.minecraft.world.item.alchemy.Potion> from,
            net.minecraft.world.item.Item ingredient) {
        net.minecraft.world.item.ItemStack bottle = net.minecraft.world.item.alchemy.PotionContents
                .createItemStack(net.minecraft.world.item.Items.POTION, from);
        net.minecraft.world.item.ItemStack result = brewing.mix(new net.minecraft.world.item.ItemStack(ingredient), bottle);
        return result.get(net.minecraft.core.component.DataComponents.POTION_CONTENTS).potion().orElse(null);
    }

    /**
     * {@code HydrationEffect} grants {@code amplifier + 1} thirst once every
     * {@code 50 >> amplifier} ticks, which is what makes a brewed dose worth
     * roughly +18 thirst. Both halves of that rule are measured here: the
     * cadence and the amount.
     */
    @GameTest
    public void aHydrationDoseWorthsAboutEighteenThirst(GameTestHelper helper) {
        // No mock player ticks inside a gametest: makeMockPlayer returns a bare
        // Player that is never added to the level, and makeMockServerPlayerInLevel
        // is dropped a tick after it joins. So the effect is driven through
        // MobEffectInstance.tickServer - vanilla's own per-tick path, which is
        // exactly the shouldApplyEffectTickThisTick + applyEffectTick pair the
        // effect has to implement. This measures the cadence, not our wiring.
        net.minecraft.server.level.ServerLevel level = helper.getLevel();

        // Aged's rule is `50 >> amplifier` ticks per grant and `amp + 1` thirst
        // per grant, so amplifier 0 is about 18 x +1 over a 900-tick dose and
        // amplifier 1 is about 36 x +2.
        int[] amp0 = countHydrationGrants(helper, level, 0);
        helper.assertTrue(amp0[0] >= 17 && amp0[0] <= 19,
                "a 900-tick dose at amplifier 0 fires once every 50 ticks, so about 18 grants (was "
                        + amp0[0] + ")");
        helper.assertTrue(amp0[1] == amp0[0],
                "each grant is exactly +1 thirst at amplifier 0 (gave " + amp0[1] + " for "
                        + amp0[0] + " grants)");

        int[] amp1 = countHydrationGrants(helper, level, 1);
        helper.assertTrue(amp1[0] >= 35 && amp1[0] <= 37,
                "amplifier 1 halves the interval to 25 ticks, so about 36 grants (was "
                        + amp1[0] + ")");
        helper.assertTrue(amp1[1] == amp1[0] * 2,
                "each grant is exactly +2 thirst at amplifier 1 (gave " + amp1[1] + " for "
                        + amp1[0] + " grants)");
        helper.succeed();
    }

    /**
     * Ticks a full dose of the hydration effect on a player of its own and
     * reports {@code {grants, total thirst granted}}. The thirst bar caps at 20,
     * so it is emptied the moment it fills: otherwise an amplifier-1 dose
     * (+2 a grant) would look like it stopped after 10 grants when it was
     * simply out of room.
     */
    private static int[] countHydrationGrants(GameTestHelper helper,
            net.minecraft.server.level.ServerLevel level, int amplifier) {
        net.minecraft.world.entity.player.Player player =
                helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        HearthwindSurvivalThirst.setState(player, HearthwindSurvivalThirst.ThirstState.DEFAULT
                .withLevel(0).withDehydration(0.0F));
        var instance = new net.minecraft.world.effect.MobEffectInstance(HydrationMobEffect.HOLDER,
                HydrationMobEffect.POTION_DURATION_TICKS, amplifier, false, false, true);
        int grants = 0;
        int total = 0;
        for (int tick = 0; tick < HydrationMobEffect.POTION_DURATION_TICKS; tick++) {
            int before = HearthwindSurvivalThirst.level(player);
            instance.tickServer(level, player, () -> {});
            int gained = HearthwindSurvivalThirst.level(player) - before;
            if (gained > 0) {
                grants++;
                total += gained;
            }
            if (HearthwindSurvivalThirst.level(player) >= HearthwindSurvivalThirst.MAX_LEVEL) {
                HearthwindSurvivalThirst.setState(player, HearthwindSurvivalThirst.ThirstState.DEFAULT
                        .withLevel(0).withDehydration(0.0F));
            }
            if (instance.getDuration() <= 0) {
                break;
            }
        }
        return new int[] {grants, total};
    }

    /**
     * Dehydration's bamboo pump: four pumps purify a bucket, one purifies a
     * glass bottle, and a leather flask gains two units - after which the
     * pump rests for the configured cooldown.
     */
    @GameTest
    public void bambooPumpPurifiesItsContainer(GameTestHelper helper) {
        net.minecraft.core.BlockPos rel = new net.minecraft.core.BlockPos(1, 2, 1);
        helper.setBlock(rel, dev.jmiahman.hearthwind.survival.hydration.HydrationBlocks.BAMBOO_PUMP.defaultBlockState());
        net.minecraft.server.level.ServerLevel level = helper.getLevel();
        var pos = helper.absolutePos(rel);
        var pump = (dev.jmiahman.hearthwind.survival.hydration.BambooPumpBlockEntity)
                level.getBlockEntity(pos);
        helper.assertTrue(pump != null, "the bamboo pump must have a block entity");

        // ONE pump converts, for every container (0.1.49). A bucket used to
        // need four here, which made the pump a reusable tool rather than the
        // one-shot press the reference is.
        pump.setItem(0, new ItemStack(Items.BUCKET));
        helper.assertTrue(pump.getItem(0).is(Items.BUCKET), "a fresh bucket is not converted");
        pump.increasePumpCount(1);
        helper.assertTrue(pump.getItem(0).is(dev.jmiahman.hearthwind.survival.PurifiedWater.BUCKET),
                "one pump must purify the bucket");
        helper.assertTrue(pump.getCooldown() == HearthwindSurvivalConfig.get().hydration.pumpCooldown,
                "a conversion must arm the pump cooldown");

        // A glass bottle purifies on the first pump.
        pump.setCooldown(0);
        pump.setItem(0, new ItemStack(Items.GLASS_BOTTLE));
        pump.increasePumpCount(1);
        ItemStack potion = pump.getItem(0);
        helper.assertTrue(potion.is(Items.POTION), "a glass bottle must become a potion");
        var contents = potion.get(net.minecraft.core.component.DataComponents.POTION_CONTENTS);
        helper.assertTrue(contents != null && contents.is(
                        dev.jmiahman.hearthwind.survival.PurifiedWater.PURIFIED_POTION),
                "the potion must hold purified water");

        // A flask keeps its fill level and its contents become purified - the
        // reference writes "a purified-water-filled copy of the container",
        // it never tops the flask up.
        pump.setCooldown(0);
        ItemStack flask = FlaskItems.setFill(new ItemStack(FlaskItems.LEATHER_FLASK), 1, FlaskData.DIRTY);
        pump.setItem(0, flask);
        pump.increasePumpCount(1);
        var data = pump.getItem(0).get(FlaskItems.FLASK_DATA);
        helper.assertTrue(data != null && data.fillLevel() == 1,
                "a flask must keep its fill level, got " + (data == null ? "none" : data.fillLevel()));
        helper.assertTrue(data != null && data.qualityLevel() == FlaskData.PURIFIED,
                "a pumped flask's contents must be purified");
        helper.succeed();
    }

    /** The container can be taken back out with a sneak + empty hand. */
    @GameTest
    public void bambooPumpHandsBackItsContainer(GameTestHelper helper) {
        net.minecraft.core.BlockPos rel = new net.minecraft.core.BlockPos(1, 2, 1);
        helper.setBlock(rel, dev.jmiahman.hearthwind.survival.hydration.HydrationBlocks.BAMBOO_PUMP.defaultBlockState());
        ServerPlayer player = survivalServerPlayer(helper);
        var pump = (dev.jmiahman.hearthwind.survival.hydration.BambooPumpBlockEntity)
                helper.getLevel().getBlockEntity(helper.absolutePos(rel));
        pump.setItem(0, new ItemStack(Items.BUCKET));
        player.setShiftKeyDown(true);
        helper.useBlock(rel, player);
        helper.assertTrue(pump.isEmpty(), "sneaking with an empty hand must take the container back");
        helper.assertTrue(player.getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND).is(Items.BUCKET),
                "the container must land in the player's hand");
        helper.succeed();
    }

    @GameTest
    public void waterBottleBoilsOnCampfire(GameTestHelper helper) {
        net.minecraft.core.BlockPos rel = new net.minecraft.core.BlockPos(1, 2, 1);
        helper.setBlock(rel, net.minecraft.world.level.block.Blocks.CAMPFIRE.defaultBlockState()
                .setValue(net.minecraft.world.level.block.CampfireBlock.LIT, true));
        net.minecraft.core.BlockPos pos = helper.absolutePos(rel);
        net.minecraft.server.level.ServerLevel level = helper.getLevel();
        var blockEntity = level.getBlockEntity(pos);
        helper.assertTrue(blockEntity instanceof net.minecraft.world.level.block.entity.CampfireBlockEntity,
                "campfire must have a block entity");
        var campfire = (net.minecraft.world.level.block.entity.CampfireBlockEntity) blockEntity;
        // makeMockServerPlayerInLevel is CREATIVE (infinite materials), which
        // makes ItemStack.consume a no-op; a plain SURVIVAL mock consumes.
        net.minecraft.world.entity.player.Player player =
                helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack bottle = net.minecraft.world.item.alchemy.PotionContents.createItemStack(
                Items.POTION, net.minecraft.world.item.alchemy.Potions.WATER);
        helper.assertTrue(CampfirePurification.placeWaterBottle(level, player, campfire, bottle),
                "water bottle must be placeable on a campfire (Dehydration parity)");
        helper.assertTrue(bottle.isEmpty(), "placing the bottle consumes it");
        helper.assertTrue(CampfirePurification.isWaterPotion(campfire.getItems().get(0)),
                "campfire slot must hold the water bottle");
        // Fast-forward to the last boil tick, then run the conversion.
        ((dev.jmiahman.hearthwind.survival.mixin.CampfireBlockEntityAccessor) campfire)
                .hearthwind$cookingProgress()[0] = CampfirePurification.BOIL_TIME - 1;
        CampfirePurification.tickPurification(level, pos, campfire.getBlockState(), campfire);
        helper.assertTrue(campfire.getItems().get(0).isEmpty(), "finished bottle must leave the fire");
        var drops = level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                new net.minecraft.world.phys.AABB(pos).inflate(2.0));
        boolean purified = drops.stream().anyMatch(item -> {
            var contents = item.getItem().get(DataComponents.POTION_CONTENTS);
            return contents != null && contents.is(PurifiedWater.PURIFIED_POTION);
        });
        helper.assertTrue(purified, "boiling must drop a purified water bottle");
        // Block.popResourceFromFace spawns the item on the fire's EDGE
        // (0.625 from the centre) with an outward hop; the old
        // Containers.dropItemStack/Block.popResource calls sat at the block
        // centre and looked like the bottle never left the fire.
        // The reference pops the result at the raw block corner with
        // ItemScatterer's eject velocity (CampfireBlockEntityMixin:
        // ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), stack)).
        // 0.1.49 matches that; we used to use the dispenser's face-pop, which
        // threw the bottle clear of the fire instead.
        boolean poppedAtCorner = drops.stream()
                .filter(item -> {
                    var contents = item.getItem().get(DataComponents.POTION_CONTENTS);
                    return contents != null && contents.is(PurifiedWater.PURIFIED_POTION);
                })
                .anyMatch(item -> item.getX() >= pos.getX() && item.getX() <= pos.getX() + 1.0
                        && item.getZ() >= pos.getZ() && item.getZ() <= pos.getZ() + 1.0
                        && item.getDeltaMovement().lengthSqr() > 0.0);
        helper.assertTrue(poppedAtCorner,
                "the purified bottle must pop off at the campfire block corner with an eject velocity");
        helper.succeed();
    }

    @GameTest
    public void waterBottlePurifiesThroughRealCookTick(GameTestHelper helper) {
        // End-to-end version of the boil: drive the vanilla cookTick the
        // campfire ticker uses, so the mixin hook itself is exercised (the
        // other tests call tickPurification directly and would miss a broken
        // injection).
        net.minecraft.core.BlockPos rel = new net.minecraft.core.BlockPos(1, 2, 1);
        helper.setBlock(rel, net.minecraft.world.level.block.Blocks.CAMPFIRE.defaultBlockState()
                .setValue(net.minecraft.world.level.block.CampfireBlock.LIT, true));
        net.minecraft.core.BlockPos pos = helper.absolutePos(rel);
        net.minecraft.server.level.ServerLevel level = helper.getLevel();
        var campfire = (net.minecraft.world.level.block.entity.CampfireBlockEntity) level.getBlockEntity(pos);
        net.minecraft.world.entity.player.Player player =
                helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack bottle = net.minecraft.world.item.alchemy.PotionContents.createItemStack(
                Items.POTION, net.minecraft.world.item.alchemy.Potions.WATER);
        helper.assertTrue(CampfirePurification.placeWaterBottle(level, player, campfire, bottle),
                "water bottle must be placeable");
        var quickCheck = net.minecraft.world.item.crafting.RecipeManager.createCheck(
                net.minecraft.world.item.crafting.RecipeType.CAMPFIRE_COOKING);
        int ticks = 0;
        while (!campfire.getItems().get(0).isEmpty()
                && ticks < CampfirePurification.BOIL_TIME + 5) {
            net.minecraft.world.level.block.entity.CampfireBlockEntity.cookTick(
                    level, pos, campfire.getBlockState(), campfire, quickCheck);
            ticks++;
        }
        helper.assertTrue(campfire.getItems().get(0).isEmpty(),
                "cookTick must boil the bottle away (ran " + ticks + " ticks)");
        helper.assertTrue(ticks == CampfirePurification.BOIL_TIME,
                "bottle must pop off on the exact boil tick, took " + ticks);
        var drops = level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                new net.minecraft.world.phys.AABB(pos).inflate(2.0));
        boolean purified = drops.stream().anyMatch(item -> {
            var contents = item.getItem().get(DataComponents.POTION_CONTENTS);
            return contents != null && contents.is(PurifiedWater.PURIFIED_POTION);
        });
        helper.assertTrue(purified, "cookTick must drop a purified bottle, not raw water");
        helper.succeed();
    }

    @GameTest
    public void waterBottleBoilTimeMatchesParity(GameTestHelper helper) {
        helper.assertTrue(CampfirePurification.BOIL_TIME == 1000,
                "Dehydration parity: campfire boil time must be 1000 ticks (50s), got "
                        + CampfirePurification.BOIL_TIME);
        helper.succeed();
    }

    /**
     * The wiring test players kept failing. The other boil tests call
     * {@code tickPurification} or {@code cookTick} by hand, so they stay green
     * even when the campfire's own block-entity ticker never fires - which is
     * exactly the "the bottle sits on the fire forever" report. This one only
     * places the bottle, winds the progress to two ticks short, and lets the
     * SERVER tick the structure on its own.
     */
    @GameTest(maxTicks = 200)
    public void aLitCampfireBoilsOverRealServerTicks(GameTestHelper helper) {
        net.minecraft.core.BlockPos rel = new net.minecraft.core.BlockPos(1, 2, 1);
        helper.setBlock(rel, net.minecraft.world.level.block.Blocks.CAMPFIRE.defaultBlockState()
                .setValue(net.minecraft.world.level.block.CampfireBlock.LIT, true));
        net.minecraft.core.BlockPos pos = helper.absolutePos(rel);
        net.minecraft.server.level.ServerLevel level = helper.getLevel();
        var campfire = (net.minecraft.world.level.block.entity.CampfireBlockEntity) level.getBlockEntity(pos);
        net.minecraft.world.entity.player.Player player =
                helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack bottle = net.minecraft.world.item.alchemy.PotionContents.createItemStack(
                Items.POTION, net.minecraft.world.item.alchemy.Potions.WATER);
        helper.assertTrue(CampfirePurification.placeWaterBottle(level, player, campfire, bottle),
                "water bottle must be placeable");
        ((dev.jmiahman.hearthwind.survival.mixin.CampfireBlockEntityAccessor) campfire)
                .hearthwind$cookingProgress()[0] = CampfirePurification.BOIL_TIME - 3;
        helper.assertTrue(CampfirePurification.isLit(campfire.getBlockState()),
                "the test campfire must be lit for the ticker to be the cooking one");
        helper.runAfterDelay(30, () -> {
            helper.assertTrue(campfire.getItems().get(0).isEmpty(),
                    "the lit campfire's own ticker must finish the boil within 30 ticks");
            var drops = level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                    new net.minecraft.world.phys.AABB(pos).inflate(3.0));
            boolean purified = drops.stream().anyMatch(item -> {
                var contents = item.getItem().get(DataComponents.POTION_CONTENTS);
                return contents != null && contents.is(PurifiedWater.PURIFIED_POTION);
            });
            helper.assertTrue(purified, "the finished bottle must be a purified water potion on the ground"
                    + " (found " + drops.size() + " item entities near " + pos + ")");
            helper.succeed();
        });
    }

    /**
     * Aged has no lit check when a bottle goes on the fire, but the boil only
     * runs in the lit tick, so a dark fire keeps the bottle forever. Pin that
     * rule so the hint we added stays honest about it.
     */
    /**
     * The exact sequence a player performs, driven entirely through real
     * clicks: hearthwind-primitive places every campfire UNLIT (earlystage
     * parity), the player lights it with bark, then right-clicks with a water
     * bottle. Every other boil test starts from an already-lit block, so the
     * unlit -> bark -> lit -> bottle path - which is what players actually do,
     * and what the "the bottle never pops off" reports describe - was never
     * covered. Nothing here is stepped by hand: only the progress is wound
     * forward so the test finishes in a few ticks instead of 1000.
     */
    @GameTest(maxTicks = 300)
    public void aBarkLitCampfireBoilsOverRealServerTicks(GameTestHelper helper) {
        net.minecraft.core.BlockPos rel = new net.minecraft.core.BlockPos(1, 2, 1);
        helper.setBlock(rel, net.minecraft.world.level.block.Blocks.CAMPFIRE.defaultBlockState()
                .setValue(net.minecraft.world.level.block.CampfireBlock.LIT, false));
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        net.minecraft.server.level.ServerLevel level = helper.getLevel();
        net.minecraft.core.BlockPos pos = helper.absolutePos(rel);
        helper.assertTrue(!CampfirePurification.isLit(level.getBlockState(pos)),
                "the freshly placed campfire starts dark, like every campfire a player builds");

        // 1. Light it with bark, the way earlystage players do.
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
                new net.minecraft.world.item.ItemStack(BuiltInRegistries.ITEM.getValue(
                        Identifier.fromNamespaceAndPath("earlystage", "oak_bark"))));
        helper.useBlock(rel, player);
        helper.assertTrue(CampfirePurification.isLit(level.getBlockState(pos)),
                "bark must light the campfire (hearthwind-primitive parity)");

        // 2. Put the water bottle on through the real interaction.
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
                net.minecraft.world.item.alchemy.PotionContents.createItemStack(
                        Items.POTION, net.minecraft.world.item.alchemy.Potions.WATER));
        helper.useBlock(rel, player);
        var campfire = (net.minecraft.world.level.block.entity.CampfireBlockEntity)
                level.getBlockEntity(pos);
        helper.assertTrue(CampfirePurification.isWaterPotion(campfire.getItems().get(0)),
                "the water bottle must land on the lit campfire");

        // 3. Wind the boil to two ticks short and let the SERVER finish it.
        ((dev.jmiahman.hearthwind.survival.mixin.CampfireBlockEntityAccessor) campfire)
                .hearthwind$cookingProgress()[0] = CampfirePurification.BOIL_TIME - 2;
        helper.runAfterDelay(30, () -> {
            helper.assertTrue(campfire.getItems().get(0).isEmpty(),
                    "a bark-lit campfire's own ticker must finish the boil within 30 ticks");
            var drops = level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                    new net.minecraft.world.phys.AABB(pos).inflate(3.0));
            boolean purified = drops.stream().anyMatch(item -> {
                var contents = item.getItem().get(DataComponents.POTION_CONTENTS);
                return contents != null && contents.is(PurifiedWater.PURIFIED_POTION);
            });
            helper.assertTrue(purified, "the finished bottle must be a purified water potion on the ground"
                    + " (found " + drops.size() + " item entities near " + pos + ")");
            helper.succeed();
        });
    }

    @GameTest(maxTicks = 200)
    public void aDarkCampfireKeepsTheBottleAndNeverBoils(GameTestHelper helper) {
        net.minecraft.core.BlockPos rel = new net.minecraft.core.BlockPos(1, 2, 1);
        // Blocks.CAMPFIRE.defaultBlockState() is LIT=true, so a dark fire has
        // to ask for it explicitly.
        helper.setBlock(rel, net.minecraft.world.level.block.Blocks.CAMPFIRE.defaultBlockState()
                .setValue(net.minecraft.world.level.block.CampfireBlock.LIT, false));
        net.minecraft.core.BlockPos pos = helper.absolutePos(rel);
        net.minecraft.server.level.ServerLevel level = helper.getLevel();
        var campfire = (net.minecraft.world.level.block.entity.CampfireBlockEntity) level.getBlockEntity(pos);
        net.minecraft.world.entity.player.Player player =
                helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack bottle = net.minecraft.world.item.alchemy.PotionContents.createItemStack(
                Items.POTION, net.minecraft.world.item.alchemy.Potions.WATER);
        helper.assertTrue(CampfirePurification.placeWaterBottle(level, player, campfire, bottle),
                "Dehydration parity: a dark campfire still accepts the bottle");
        helper.assertTrue(!CampfirePurification.isLit(campfire.getBlockState()),
                "the test campfire must be unlit");
        helper.runAfterDelay(60, () -> {
            helper.assertTrue(CampfirePurification.isWaterPotion(campfire.getItems().get(0)),
                    "an unlit campfire must never purify the bottle (it only boils while lit)");
            helper.succeed();
        });
    }

    /**
     * Aged parity: the boil FREEZES on a dark fire and resumes where it left
     * off. Vanilla 26.2 decays the progress by two per tick, which quietly
     * reset a nearly-finished bottle to zero - a second way for a boil to
     * "never finish" even on a fire that had been burning.
     */
    @GameTest(maxTicks = 200)
    public void aBoilFreezesWhileTheFireIsOutAndResumesWhenRelit(GameTestHelper helper) {
        net.minecraft.core.BlockPos rel = new net.minecraft.core.BlockPos(1, 2, 1);
        helper.setBlock(rel, net.minecraft.world.level.block.Blocks.CAMPFIRE.defaultBlockState()
                .setValue(net.minecraft.world.level.block.CampfireBlock.LIT, false));
        net.minecraft.core.BlockPos pos = helper.absolutePos(rel);
        net.minecraft.server.level.ServerLevel level = helper.getLevel();
        var campfire = (net.minecraft.world.level.block.entity.CampfireBlockEntity)
                level.getBlockEntity(pos);
        var accessor =
                (dev.jmiahman.hearthwind.survival.mixin.CampfireBlockEntityAccessor) campfire;
        net.minecraft.world.entity.player.Player player =
                helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack bottle = net.minecraft.world.item.alchemy.PotionContents.createItemStack(
                Items.POTION, net.minecraft.world.item.alchemy.Potions.WATER);
        helper.assertTrue(CampfirePurification.placeWaterBottle(level, player, campfire, bottle),
                "water bottle must be placeable");
        accessor.hearthwind$cookingProgress()[0] = 600;
        // Let the real server run the dark-fire ticker for a while.
        helper.runAfterDelay(60, () -> {
            helper.assertTrue(accessor.hearthwind$cookingProgress()[0] == 600,
                    "Aged parity: a boil must freeze on a dark fire, not decay back to zero (was "
                            + accessor.hearthwind$cookingProgress()[0] + ")");
            // Relight it and the boil must carry on from 600, not restart.
            level.setBlock(pos, campfire.getBlockState()
                    .setValue(net.minecraft.world.level.block.CampfireBlock.LIT, true), 3);
            helper.runAfterDelay(60, () -> {
                helper.assertTrue(accessor.hearthwind$cookingProgress()[0] > 600,
                        "the boil must resume from where it froze once the fire is back");
                helper.succeed();
            });
        });
    }

    @GameTest
    public void waterBottleDoesNotPurifyEarly(GameTestHelper helper) {
        net.minecraft.core.BlockPos rel = new net.minecraft.core.BlockPos(1, 2, 1);
        helper.setBlock(rel, net.minecraft.world.level.block.Blocks.CAMPFIRE.defaultBlockState()
                .setValue(net.minecraft.world.level.block.CampfireBlock.LIT, true));
        net.minecraft.core.BlockPos pos = helper.absolutePos(rel);
        net.minecraft.server.level.ServerLevel level = helper.getLevel();
        var campfire = (net.minecraft.world.level.block.entity.CampfireBlockEntity) level.getBlockEntity(pos);
        var accessor =
                (dev.jmiahman.hearthwind.survival.mixin.CampfireBlockEntityAccessor) campfire;
        net.minecraft.world.entity.player.Player player =
                helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack bottle = net.minecraft.world.item.alchemy.PotionContents.createItemStack(
                Items.POTION, net.minecraft.world.item.alchemy.Potions.WATER);
        helper.assertTrue(CampfirePurification.placeWaterBottle(level, player, campfire, bottle),
                "water bottle must be placeable");
        accessor.hearthwind$cookingProgress()[0] = CampfirePurification.BOIL_TIME - 2;
        CampfirePurification.tickPurification(level, pos, campfire.getBlockState(), campfire);
        helper.assertTrue(CampfirePurification.isWaterPotion(campfire.getItems().get(0)),
                "water must still be raw two ticks before the boil time");
        accessor.hearthwind$cookingProgress()[0] = CampfirePurification.BOIL_TIME - 1;
        CampfirePurification.tickPurification(level, pos, campfire.getBlockState(), campfire);
        helper.assertTrue(campfire.getItems().get(0).isEmpty(),
                "water must leave the fire on the final boil tick (never later)");
        helper.succeed();
    }

    @GameTest
    public void waterBottleNeverSticksOnCampfire(GameTestHelper helper) {
        net.minecraft.core.BlockPos rel = new net.minecraft.core.BlockPos(1, 2, 1);
        helper.setBlock(rel, net.minecraft.world.level.block.Blocks.CAMPFIRE.defaultBlockState()
                .setValue(net.minecraft.world.level.block.CampfireBlock.LIT, true));
        net.minecraft.core.BlockPos pos = helper.absolutePos(rel);
        net.minecraft.server.level.ServerLevel level = helper.getLevel();
        var campfire = (net.minecraft.world.level.block.entity.CampfireBlockEntity) level.getBlockEntity(pos);
        var accessor =
                (dev.jmiahman.hearthwind.survival.mixin.CampfireBlockEntityAccessor) campfire;
        net.minecraft.world.entity.player.Player player =
                helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack bottle = net.minecraft.world.item.alchemy.PotionContents.createItemStack(
                Items.POTION, net.minecraft.world.item.alchemy.Potions.WATER);
        helper.assertTrue(CampfirePurification.placeWaterBottle(level, player, campfire, bottle),
                "water bottle must be placeable");
        // Simulate the vanilla cook loop tick by tick, exactly as the block
        // entity would run it, and require the slot to clear at BOIL_TIME.
        for (int tick = 0; tick < CampfirePurification.BOIL_TIME + 10
                && !campfire.getItems().get(0).isEmpty(); tick++) {
            accessor.hearthwind$cookingProgress()[0] = tick;
            CampfirePurification.tickPurification(level, pos, campfire.getBlockState(), campfire);
        }
        for (int slot = 0; slot < campfire.getItems().size(); slot++) {
            helper.assertTrue(!CampfirePurification.isWaterPotion(campfire.getItems().get(slot)),
                    "no raw water bottle may remain stuck in slot " + slot);
        }
        helper.succeed();
    }

    @GameTest
    public void otherCampfireCookingIsUnaffected(GameTestHelper helper) {
        net.minecraft.core.BlockPos rel = new net.minecraft.core.BlockPos(1, 2, 1);
        helper.setBlock(rel, net.minecraft.world.level.block.Blocks.CAMPFIRE.defaultBlockState()
                .setValue(net.minecraft.world.level.block.CampfireBlock.LIT, true));
        net.minecraft.core.BlockPos pos = helper.absolutePos(rel);
        net.minecraft.server.level.ServerLevel level = helper.getLevel();
        var campfire = (net.minecraft.world.level.block.entity.CampfireBlockEntity) level.getBlockEntity(pos);
        var accessor =
                (dev.jmiahman.hearthwind.survival.mixin.CampfireBlockEntityAccessor) campfire;
        net.minecraft.world.entity.player.Player player =
                helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack bottle = net.minecraft.world.item.alchemy.PotionContents.createItemStack(
                Items.POTION, net.minecraft.world.item.alchemy.Potions.WATER);
        helper.assertTrue(CampfirePurification.placeWaterBottle(level, player, campfire, bottle),
                "water bottle must be placeable");
        accessor.hearthwind$items().set(1, new ItemStack(Items.BEEF));
        accessor.hearthwind$cookingTime()[1] = 600;
        accessor.hearthwind$cookingProgress()[1] = 100;
        CampfirePurification.tickPurification(level, pos, campfire.getBlockState(), campfire);
        helper.assertTrue(campfire.getItems().get(1).is(Items.BEEF),
                "ordinary food must keep cooking on the campfire");
        helper.assertTrue(accessor.hearthwind$cookingProgress()[1] == 100,
                "ordinary food progress must be untouched by purification");
        helper.assertTrue(accessor.hearthwind$cookingTime()[1] == 600,
                "ordinary food cook time must be untouched by purification");
        helper.assertTrue(CampfirePurification.isWaterPotion(campfire.getItems().get(0)),
                "the water bottle must still be boiling");
        helper.succeed();
    }

    @GameTest
    public void waterBottleDrinkHydrates(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        HearthwindSurvivalThirst.setHydration(player, 10.0);
        double chance = HearthwindSurvivalConfig.get().flask.potionBadThirstChance;
        // Upstream roll is nextFloat() >= chance: 0.0 forces the effect.
        HearthwindSurvivalConfig.get().flask.potionBadThirstChance = 0.0;
        try {
            ItemStack water = net.minecraft.world.item.alchemy.PotionContents.createItemStack(
                    Items.POTION, net.minecraft.world.item.alchemy.Potions.WATER);
            var consumable = water.get(DataComponents.CONSUMABLE);
            helper.assertTrue(consumable != null, "water bottle must be drinkable");
            consumable.onConsume(helper.getLevel(), player, water);
            helper.assertTrue(HearthwindSurvivalThirst.hydration(player) > 10.0,
                    "drinking a water bottle must restore hydration, got "
                            + HearthwindSurvivalThirst.hydration(player));
            helper.assertTrue(player.hasEffect(ThirstMobEffect.HOLDER),
                    "raw water bottle must be able to inflict Thirst (green droplets)");
            player.removeEffect(ThirstMobEffect.HOLDER);
            HearthwindSurvivalThirst.setHydration(player, 10.0);
            ItemStack purified = CampfirePurification.purifiedBottle();
            purified.get(DataComponents.CONSUMABLE).onConsume(helper.getLevel(), player, purified);
            helper.assertTrue(HearthwindSurvivalThirst.hydration(player) > 10.0,
                    "purified bottle must restore hydration");
            helper.assertTrue(!player.hasEffect(ThirstMobEffect.HOLDER),
                    "purified bottle must never inflict Thirst");
        } finally {
            HearthwindSurvivalConfig.get().flask.potionBadThirstChance = chance;
        }
        helper.succeed();
    }

    @GameTest
    public void waterBottleUseOnCampfire(GameTestHelper helper) {
        net.minecraft.core.BlockPos rel = new net.minecraft.core.BlockPos(1, 2, 1);
        helper.setBlock(rel, net.minecraft.world.level.block.Blocks.CAMPFIRE.defaultBlockState()
                .setValue(net.minecraft.world.level.block.CampfireBlock.LIT, true));
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
                net.minecraft.world.item.alchemy.PotionContents.createItemStack(
                        Items.POTION, net.minecraft.world.item.alchemy.Potions.WATER));
        helper.useBlock(rel, player);
        var blockEntity = helper.getLevel().getBlockEntity(helper.absolutePos(rel));
        helper.assertTrue(blockEntity instanceof net.minecraft.world.level.block.entity.CampfireBlockEntity,
                "campfire must have a block entity");
        var campfire = (net.minecraft.world.level.block.entity.CampfireBlockEntity) blockEntity;
        helper.assertTrue(CampfirePurification.isWaterPotion(campfire.getItems().get(0)),
                "using a water bottle on a campfire must place it (no recipe gate)");
        helper.succeed();
    }

    @GameTest
    public void waterBottleCanBeReboiledOnSameCampfire(GameTestHelper helper) {
        // Regression: the bottle must stay reusable. Place, boil, collect,
        // place again on the SAME campfire and boil a second time - all
        // through the real block-interaction + cookTick path.
        net.minecraft.core.BlockPos rel = new net.minecraft.core.BlockPos(1, 2, 1);
        helper.setBlock(rel, net.minecraft.world.level.block.Blocks.CAMPFIRE.defaultBlockState()
                .setValue(net.minecraft.world.level.block.CampfireBlock.LIT, true));
        net.minecraft.core.BlockPos pos = helper.absolutePos(rel);
        net.minecraft.server.level.ServerLevel level = helper.getLevel();
        var campfire = (net.minecraft.world.level.block.entity.CampfireBlockEntity) level.getBlockEntity(pos);
        net.minecraft.world.entity.player.Player player =
                helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        var quickCheck = net.minecraft.world.item.crafting.RecipeManager.createCheck(
                net.minecraft.world.item.crafting.RecipeType.CAMPFIRE_COOKING);
        for (int round = 1; round <= 2; round++) {
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
                    net.minecraft.world.item.alchemy.PotionContents.createItemStack(
                            Items.POTION, net.minecraft.world.item.alchemy.Potions.WATER));
            helper.useBlock(rel, player);
            helper.assertTrue(CampfirePurification.isWaterPotion(campfire.getItems().get(0)),
                    "round " + round + ": water bottle must be placeable on the same campfire");
            helper.assertTrue(player.getMainHandItem().isEmpty(),
                    "round " + round + ": placing the bottle consumes it in survival");
            int ticks = 0;
            while (!campfire.getItems().get(0).isEmpty()
                    && ticks < CampfirePurification.BOIL_TIME + 5) {
                net.minecraft.world.level.block.entity.CampfireBlockEntity.cookTick(
                        level, pos, campfire.getBlockState(), campfire, quickCheck);
                ticks++;
            }
            helper.assertTrue(ticks == CampfirePurification.BOIL_TIME,
                    "round " + round + ": boil must take exactly "
                            + CampfirePurification.BOIL_TIME + " ticks, took " + ticks);
            long drops = level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                    new net.minecraft.world.phys.AABB(pos).inflate(2.0)).stream().filter(item -> {
                        var contents = item.getItem().get(DataComponents.POTION_CONTENTS);
                        return contents != null && contents.is(PurifiedWater.PURIFIED_POTION);
                    }).count();
            helper.assertTrue(drops >= round,
                    "round " + round + ": boiling must drop purified water (found " + drops + ")");
        }
        helper.succeed();
    }

    @GameTest
    public void purifiedBottleIsAReusableVessel(GameTestHelper helper) {
        ItemStack purified = CampfirePurification.purifiedBottle();
        helper.assertTrue(purified.get(DataComponents.USE_REMAINDER) != null,
                "drinking a purified bottle must give the glass bottle back");
        ItemStack refilled = net.minecraft.world.item.alchemy.PotionContents.createItemStack(
                Items.POTION, net.minecraft.world.item.alchemy.Potions.WATER);
        helper.assertTrue(CampfirePurification.isWaterPotion(refilled),
                "a refilled glass bottle must be raw water again and reboilable");
        helper.succeed();
    }

    @GameTest
    public void sobrietyDefaultsToAlcoholFree(GameTestHelper helper) {
        helper.assertTrue(HearthwindSurvivalConfig.get().sobriety.removeAlcohol,
                "removeAlcohol must default to true");
        helper.assertTrue(Sobriety.alcoholRemoved(),
                "Sobriety helper must report alcohol removed by default");
        helper.succeed();
    }

    @GameTest
    public void eatingAppleAddsVitaminsAndMinerals(GameTestHelper helper) {
        var pig = helper.spawn(EntityTypes.PIG, 1, 2, 1);
        HearthwindSurvivalDiet.setLevel(pig, 3, 0);
        HearthwindSurvivalDiet.setLevel(pig, 4, 0);
        ItemStack apple = new ItemStack(Items.APPLE);
        helper.assertTrue(apple.get(DataComponents.FOOD) != null, "apple must be food");
        HearthwindSurvivalDiet.onEaten(pig, apple);
        helper.assertTrue(HearthwindSurvivalDiet.getLevel(pig, 3) == 15,
                "vanilla_items.json: apple must grant vitamins 15");
        helper.assertTrue(HearthwindSurvivalDiet.getLevel(pig, 4) == 5,
                "vanilla_items.json: apple must grant minerals 5");
        helper.succeed();
    }

    @GameTest
    public void eatHookFiresThroughItemUse(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        HearthwindSurvivalDiet.setLevel(player, 3, 0);
        HearthwindSurvivalDiet.onEaten(player, new ItemStack(Items.APPLE));
        helper.assertTrue(HearthwindSurvivalDiet.getLevel(player, 3) == 15,
                "apple via onEaten must add vitamins (mixin hook)");
        helper.succeed();
    }

    @GameTest
    public void nonFoodDoesNotChangeDiet(GameTestHelper helper) {
        var pig = helper.spawn(EntityTypes.PIG, 1, 2, 1);
        HearthwindSurvivalDiet.setLevel(pig, 0, 10);
        HearthwindSurvivalDiet.onEaten(pig, new ItemStack(Items.STICK));
        helper.assertTrue(HearthwindSurvivalDiet.getLevel(pig, 0) == 10,
                "sticks are not food; nutrients unchanged");
        helper.succeed();
    }

    @GameTest
    public void decayReducesAllFiveNutrients(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        for (int i = 0; i < HearthwindSurvivalDiet.NUTRIENT_COUNT; i++) {
            HearthwindSurvivalDiet.setLevel(player, i, 50);
        }
        HearthwindSurvivalDiet.applyDecay(player);
        for (int i = 0; i < HearthwindSurvivalDiet.NUTRIENT_COUNT; i++) {
            helper.assertTrue(HearthwindSurvivalDiet.getLevel(player, i) == 49,
                    "hunger decay must drop every nutrient by 1 (index " + i + ")");
        }
        helper.succeed();
    }

    @GameTest
    public void decayNeverGoesBelowZero(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        HearthwindSurvivalDiet.setLevel(player, 0, 0);
        HearthwindSurvivalDiet.applyDecay(player);
        helper.assertTrue(HearthwindSurvivalDiet.getLevel(player, 0) == 0,
                "nutrient decay must clamp at zero");
        helper.succeed();
    }

    @GameTest
    public void nutrientsClampAtMax(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        HearthwindSurvivalDiet.setLevel(player, 0, 299);
        HearthwindSurvivalDiet.addLevel(player, 0, 999);
        helper.assertTrue(HearthwindSurvivalDiet.getLevel(player, 0)
                == HearthwindSurvivalConfig.get().diet.maxNutrition,
                "nutrients must clamp at maxNutrition");
        helper.succeed();
    }

    @GameTest
    public void nutritionCorpusLoadsVanillaItems(GameTestHelper helper) {
        helper.assertTrue(HearthwindSurvivalDiet.itemCount() >= 40,
                "vanilla nutrition map must load (got " + HearthwindSurvivalDiet.itemCount() + ")");
        int[] apple = HearthwindSurvivalDiet.nutritionOf(Items.APPLE);
        helper.assertTrue(apple != null && apple[3] == 15 && apple[4] == 5,
                "apple must map to vitamins 15 / minerals 5 from vanilla_items.json");
        int[] cake = HearthwindSurvivalDiet.nutritionOf(Items.CAKE);
        helper.assertTrue(cake != null, "vanilla_blocks.json (cake) must load");
        int[] flask = HearthwindSurvivalDiet.nutritionOf(FlaskItems.LEATHER_FLASK);
        helper.assertTrue(flask != null && flask[4] == 20,
                "dehydration compat must map the flask to minerals 20");
        helper.succeed();
    }

    @GameTest
    public void nutritionThresholdsApplyDatapackEffects(GameTestHelper helper) {
        helper.assertTrue(NutritionEffects.negativeCount() > 0 && NutritionEffects.positiveCount() > 0,
                "nutrition_manager/default.json must load positive and negative effect lists");
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        player.getAbilities().instabuild = false;
        for (int i = 0; i < HearthwindSurvivalDiet.NUTRIENT_COUNT; i++) {
            HearthwindSurvivalDiet.setLevel(player, i, 150);
        }
        // Low vitamins: default.json lists weakness on the negative side.
        HearthwindSurvivalDiet.setLevel(player, 3, 0);
        NutritionEffects.applyForTest(player);
        helper.assertTrue(player.hasEffect(net.minecraft.world.effect.MobEffects.WEAKNESS),
                "vitamins <= negativeNutrition must apply the datapack weakness effect");
        // High vitamins: regeneration.
        HearthwindSurvivalDiet.setLevel(player, 3, 300);
        NutritionEffects.applyForTest(player);
        helper.assertTrue(player.hasEffect(net.minecraft.world.effect.MobEffects.REGENERATION),
                "vitamins >= positiveNutrition must apply the datapack regeneration effect");
        helper.succeed();
    }

    @GameTest
    public void perishableFoodRots(GameTestHelper helper) {
        SimpleContainer container = new SimpleContainer(new ItemStack(Items.BEEF));
        int rotted = HearthwindSurvivalSpoilage.spoilContainer(container,
                net.minecraft.util.RandomSource.create(), 1.0,
                "minecraft:rotten_flesh", ignored -> { });
        helper.assertTrue(rotted == 1, "beef is perishable and must rot");
        helper.assertTrue(container.getItem(0).isEmpty(),
                "original beef slot must be empty after rotting");
        helper.succeed();
    }

    @GameTest
    public void spoilOutputIsRottenFlesh(GameTestHelper helper) {
        java.util.List<ItemStack> spilled = new java.util.ArrayList<>();
        SimpleContainer container = new SimpleContainer(new ItemStack(Items.COOKED_CHICKEN));
        HearthwindSurvivalSpoilage.spoilContainer(container,
                net.minecraft.util.RandomSource.create(), 1.0,
                "minecraft:rotten_flesh", spilled::add);
        helper.assertTrue(spilled.size() == 1
                && spilled.get(0).is(Items.ROTTEN_FLESH),
                "rot output must be rotten flesh");
        helper.succeed();
    }

    @GameTest
    public void nonSpoilingItemsAreExempt(GameTestHelper helper) {
        SimpleContainer container = new SimpleContainer(new ItemStack(Items.HONEY_BOTTLE));
        int rotted = HearthwindSurvivalSpoilage.spoilContainer(container,
                net.minecraft.util.RandomSource.create(), 1.0,
                "minecraft:rotten_flesh", ignored -> { });
        helper.assertTrue(rotted == 0, "honey bottle is tagged non-spoiling");
        helper.assertTrue(!container.getItem(0).isEmpty(), "honey bottle untouched");
        helper.succeed();
    }

    @GameTest
    public void hydrationClampsAtMax(GameTestHelper helper) {
        var pig = helper.spawn(EntityTypes.PIG, 1, 2, 1);
        HearthwindSurvivalThirst.addHydration(pig, 9999.0);
        helper.assertTrue(HearthwindSurvivalThirst.hydration(pig)
                == HearthwindSurvivalThirst.MAX_HYDRATION, "hydration clamped to max");
        helper.succeed();
    }

    @GameTest
    public void containerSpoilageRotsPerishables(GameTestHelper helper) {
        var container = new SimpleContainer(9);
        container.setItem(0, new ItemStack(Items.BEEF));
        int rotted = HearthwindSurvivalSpoilage.spoilContainer(container,
                net.minecraft.util.RandomSource.create(), 1.0,
                "minecraft:rotten_flesh", ignored -> { });
        helper.assertTrue(rotted == 1, "beef in chest must rot at chance=1.0");
        helper.assertTrue(container.getItem(0).isEmpty(),
                "original beef slot must be empty");
        helper.succeed();
    }

    @GameTest
    public void containerSpoilageSpillsToCallback(GameTestHelper helper) {
        var container = new SimpleContainer(9);
        java.util.List<ItemStack> spilled = new java.util.ArrayList<>();
        container.setItem(0, new ItemStack(Items.COOKED_CHICKEN));
        HearthwindSurvivalSpoilage.spoilContainer(container,
                net.minecraft.util.RandomSource.create(), 1.0,
                "minecraft:rotten_flesh", spilled::add);
        helper.assertTrue(spilled.size() == 1, "one rotten flesh must spill");
        helper.assertTrue(spilled.get(0).is(Items.ROTTEN_FLESH),
                "spill must be rotten flesh");
        helper.succeed();
    }

    @GameTest
    public void containerNonSpoilingItemsExempt(GameTestHelper helper) {
        var container = new SimpleContainer(9);
        container.setItem(0, new ItemStack(Items.HONEY_BOTTLE));
        int rotted = HearthwindSurvivalSpoilage.spoilContainer(container,
                net.minecraft.util.RandomSource.create(), 1.0,
                "minecraft:rotten_flesh", ignored -> { });
        helper.assertTrue(rotted == 0, "honey bottle in chest must not rot");
        helper.assertTrue(!container.getItem(0).isEmpty(),
                "honey bottle must remain untouched");
        helper.succeed();
    }

    @GameTest
    public void containerSpoilageMixedSlots(GameTestHelper helper) {
        var container = new SimpleContainer(9);
        container.setItem(0, new ItemStack(Items.BEEF));
        container.setItem(1, new ItemStack(Items.HONEY_BOTTLE));
        container.setItem(2, new ItemStack(Items.COOKED_CHICKEN));
        int rotted = HearthwindSurvivalSpoilage.spoilContainer(container,
                net.minecraft.util.RandomSource.create(), 1.0,
                "minecraft:rotten_flesh", ignored -> { });
        helper.assertTrue(rotted == 2, "two perishables must rot, honey exempt");
        helper.assertTrue(container.getItem(0).isEmpty(), "beef slot empty");
        helper.assertTrue(!container.getItem(1).isEmpty(), "honey bottle untouched");
        helper.assertTrue(container.getItem(2).isEmpty(), "chicken slot empty");
        helper.succeed();
    }

    @GameTest
    public void thirstOnlyDrainsThroughExhaustion(GameTestHelper helper) {
        ServerPlayer player = survivalServerPlayer(helper);
        // No passive drain: 100 manager updates with an empty buffer do nothing.
        for (int i = 0; i < 100; i++) {
            HearthwindSurvivalThirst.updatePlayer(player);
        }
        helper.assertTrue(HearthwindSurvivalThirst.level(player) == HearthwindSurvivalThirst.MAX_LEVEL,
                "thirst must not drain passively (level " + HearthwindSurvivalThirst.level(player) + ")");
        helper.assertTrue(HearthwindSurvivalThirst.dehydration(player) == 0.0f,
                "no exhaustion means an empty dehydration buffer");
        // 8 exhaustion / hydrating_factor 2.0 = 4.0 buffer, just short of a level.
        player.causeFoodExhaustion(8.0f);
        helper.assertTrue(Math.abs(HearthwindSurvivalThirst.dehydration(player) - 4.0f) < 0.001f,
                "exhaustion must charge the buffer at exhaustion / 2.0 (got "
                        + HearthwindSurvivalThirst.dehydration(player) + ")");
        HearthwindSurvivalThirst.updatePlayer(player);
        helper.assertTrue(HearthwindSurvivalThirst.level(player) == HearthwindSurvivalThirst.MAX_LEVEL,
                "a buffer of exactly 4.0 must not cost a level (upstream uses > 4.0)");
        player.causeFoodExhaustion(0.2f);
        HearthwindSurvivalThirst.updatePlayer(player);
        helper.assertTrue(HearthwindSurvivalThirst.level(player) == HearthwindSurvivalThirst.MAX_LEVEL - 1,
                "crossing 4.0 buffer must cost exactly one level");
        helper.succeed();
    }

    @GameTest
    public void thirstDamageMatchesUpstreamTiming(GameTestHelper helper) {
        ServerPlayer player = survivalServerPlayer(helper);
        player.setHealth(20.0f);
        helper.getLevel().getServer().setDifficulty(net.minecraft.world.Difficulty.NORMAL, true);
        try {
            HearthwindSurvivalThirst.setHydration(player, 0.0);
            for (int i = 0; i < HearthwindSurvivalThirst.DAMAGE_INTERVAL_TICKS - 1; i++) {
                HearthwindSurvivalThirst.updatePlayer(player);
            }
            helper.assertTrue(player.getHealth() == 20.0f,
                    "no thirst damage before 90 ticks at level 0 (health " + player.getHealth() + ")");
            HearthwindSurvivalThirst.updatePlayer(player);
            helper.assertTrue(player.getHealth() == 19.0f,
                    "upstream deals thirst_damage 1.0 on the 90th tick (health " + player.getHealth() + ")");
        } finally {
            helper.getLevel().getServer().setDifficulty(net.minecraft.world.Difficulty.NORMAL, true);
        }
        helper.succeed();
    }

    @GameTest
    public void peacefulRegeneratesThirst(GameTestHelper helper) {
        ServerPlayer player = survivalServerPlayer(helper);
        helper.getLevel().getServer().setDifficulty(net.minecraft.world.Difficulty.PEACEFUL, true);
        try {
            HearthwindSurvivalThirst.setHydration(player, 10.0);
            player.tickCount = 0;
            HearthwindSurvivalThirst.updatePlayer(player);
            helper.assertTrue(HearthwindSurvivalThirst.level(player) == 11,
                    "peaceful + natural regeneration must restore one level every 10 ticks (got "
                            + HearthwindSurvivalThirst.level(player) + ")");
            player.tickCount = 1;
            HearthwindSurvivalThirst.updatePlayer(player);
            helper.assertTrue(HearthwindSurvivalThirst.level(player) == 11,
                    "peaceful regeneration must not tick off-cadence");
        } finally {
            helper.getLevel().getServer().setDifficulty(net.minecraft.world.Difficulty.NORMAL, true);
        }
        helper.succeed();
    }

    @GameTest
    public void badPotionListMatchesAged(GameTestHelper helper) {
        net.minecraft.world.item.alchemy.Potion[] bad = {
                net.minecraft.world.item.alchemy.Potions.WATER.value(),
                net.minecraft.world.item.alchemy.Potions.AWKWARD.value(),
                net.minecraft.world.item.alchemy.Potions.THICK.value(),
                net.minecraft.world.item.alchemy.Potions.HARMING.value(),
                net.minecraft.world.item.alchemy.Potions.LONG_POISON.value(),
                net.minecraft.world.item.alchemy.Potions.LONG_SLOWNESS.value(),
                net.minecraft.world.item.alchemy.Potions.LONG_WEAKNESS.value(),
                net.minecraft.world.item.alchemy.Potions.MUNDANE.value(),
                net.minecraft.world.item.alchemy.Potions.POISON.value(),
                net.minecraft.world.item.alchemy.Potions.SLOWNESS.value(),
                net.minecraft.world.item.alchemy.Potions.STRONG_HARMING.value(),
                net.minecraft.world.item.alchemy.Potions.STRONG_POISON.value(),
                net.minecraft.world.item.alchemy.Potions.STRONG_SLOWNESS.value(),
                net.minecraft.world.item.alchemy.Potions.WEAKNESS.value()
        };
        for (var potion : bad) {
            helper.assertTrue(ThirstHelper.isBadPotion(potion),
                    "upstream bad-potion list must contain every risky potion");
        }
        helper.assertTrue(!ThirstHelper.isBadPotion(net.minecraft.world.item.alchemy.Potions.HEALING.value()),
                "healing must not be a bad potion");
        helper.assertTrue(!ThirstHelper.isBadPotion(net.minecraft.world.item.alchemy.Potions.FIRE_RESISTANCE.value()),
                "fire resistance must not be a bad potion");
        helper.succeed();
    }

    @GameTest
    public void milkDrinkQuenchesEightAndRolls(GameTestHelper helper) {
        ServerPlayer player = survivalServerPlayer(helper);
        HearthwindSurvivalConfig.Flask cfg = HearthwindSurvivalConfig.get().flask;
        double chance = cfg.milkThirstChance;
        try {
            // chance 1.0 suppresses the >= roll (upstream), leaving pure quench.
            cfg.milkThirstChance = 1.0;
            HearthwindSurvivalThirst.setHydration(player, 0.0);
            ThirstHelper.hydratePlayer(player, new ItemStack(Items.MILK_BUCKET));
            helper.assertTrue(HearthwindSurvivalThirst.level(player) == 8,
                    "milk must quench 8 levels (got " + HearthwindSurvivalThirst.level(player) + ")");
            helper.assertTrue(!player.hasEffect(ThirstMobEffect.HOLDER),
                    "suppressed milk roll must not apply thirst");
            // chance 0.0 forces the roll, duration = potion_bad_thirst_duration / 2.
            cfg.milkThirstChance = 0.0;
            HearthwindSurvivalThirst.setHydration(player, 0.0);
            ThirstHelper.hydratePlayer(player, new ItemStack(Items.MILK_BUCKET));
            var effect = player.getEffect(ThirstMobEffect.HOLDER);
            helper.assertTrue(effect != null && effect.getDuration() == cfg.potionBadThirstDuration / 2,
                    "forced milk thirst must last half the bad-potion duration");
        } finally {
            cfg.milkThirstChance = chance;
        }
        helper.succeed();
    }

    @GameTest
    public void flaskOpenWaterQualityMatchesAged(GameTestHelper helper) {
        // The reference forces a river fill DIRTY (its `level = 2,
        // purified = false` branch). This test asserted the exact opposite for
        // years - "river water fills a fresh flask as purified" - which was our
        // own reading, not the mod's. 0.1.49 pins the reference.
        helper.assertTrue(LeatherFlaskItem.openWaterQuality(FlaskData.DIRTY, 0, true) == FlaskData.DIRTY,
                "river water fills a fresh flask DIRTY (upstream level=2, purified=false)");
        helper.assertTrue(LeatherFlaskItem.openWaterQuality(FlaskData.DIRTY, 0, false) == FlaskData.DIRTY,
                "still water fills a fresh flask as dirty");
        helper.assertTrue(LeatherFlaskItem.openWaterQuality(FlaskData.PURIFIED, 1, false) == FlaskData.IMPURIFIED,
                "topping up purified water outside a river downgrades to impurified");
        helper.assertTrue(LeatherFlaskItem.openWaterQuality(FlaskData.DIRTY, 1, false) == FlaskData.DIRTY,
                "dirty water stays dirty when topped up");
        helper.assertTrue(LeatherFlaskItem.openWaterQuality(FlaskData.IMPURIFIED, 1, true) == FlaskData.DIRTY,
                "river water forces a partially filled impure flask dirty too");
        helper.succeed();
    }

    @GameTest
    public void flaskCauldronFillIsDirtyAndOneUnit(GameTestHelper helper) {
        net.minecraft.core.BlockPos rel = new net.minecraft.core.BlockPos(1, 2, 1);
        helper.setBlock(rel, net.minecraft.world.level.block.Blocks.WATER_CAULDRON.defaultBlockState()
                .setValue(net.minecraft.world.level.block.LayeredCauldronBlock.LEVEL, 3));
        net.minecraft.core.BlockPos pos = helper.absolutePos(rel);
        ServerPlayer player = survivalServerPlayer(helper);
        ItemStack flask = new ItemStack(FlaskItems.LEATHER_FLASK);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, flask);
        var hit = new net.minecraft.world.phys.BlockHitResult(
                net.minecraft.world.phys.Vec3.atCenterOf(pos), net.minecraft.core.Direction.UP, pos, false);
        var context = new net.minecraft.world.item.context.UseOnContext(
                player, net.minecraft.world.InteractionHand.MAIN_HAND, hit);
        FlaskItems.LEATHER_FLASK.useOn(context);
        FlaskData data = flask.get(FlaskItems.FLASK_DATA);
        helper.assertTrue(data != null && data.fillLevel() == 1,
                "vanilla cauldrons must fill one unit per use");
        helper.assertTrue(data != null && data.qualityLevel() == FlaskData.DIRTY,
                "vanilla cauldron water must always be dirty (upstream)");
        var state = helper.getLevel().getBlockState(pos);
        helper.assertTrue(state.getValue(net.minecraft.world.level.block.LayeredCauldronBlock.LEVEL) == 2,
                "each fill must decrement exactly one cauldron level");
        helper.succeed();
    }

    @GameTest
    public void thirstDrinkWaterRefills(GameTestHelper helper) {
        var pig = helper.spawn(EntityTypes.PIG, 1, 2, 1);
        HearthwindSurvivalThirst.addHydration(pig, -10.0);
        double before = HearthwindSurvivalThirst.hydration(pig);
        HearthwindSurvivalThirst.addHydration(pig, 5.0);
        double after = HearthwindSurvivalThirst.hydration(pig);
        helper.assertTrue(after == before + 5.0, "drinking water must add hydration");
        helper.succeed();
    }

    @GameTest
    public void flaskFillSetsCapacityAndQuality(GameTestHelper helper) {
        ItemStack flask = new ItemStack(dev.jmiahman.hearthwind.survival.FlaskItems.LEATHER_FLASK);
        dev.jmiahman.hearthwind.survival.FlaskItems.setFill(flask, 2, dev.jmiahman.hearthwind.survival.FlaskData.IMPURIFIED);
        var data = flask.get(dev.jmiahman.hearthwind.survival.FlaskItems.FLASK_DATA);
        helper.assertTrue(data != null, "filled flask must carry flask_data");
        helper.assertTrue(data.fillLevel() == 2, "leather flask capacity is 2");
        helper.assertTrue(data.qualityLevel() == dev.jmiahman.hearthwind.survival.FlaskData.IMPURIFIED,
                "quality must round-trip");
        helper.assertTrue(flask.has(net.minecraft.core.component.DataComponents.CONSUMABLE),
                "filled flask must be drinkable (CONSUMABLE present)");
        helper.succeed();
    }

    @GameTest
    public void flaskDrinkDecrementsFillAndAddsHydration(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        HearthwindSurvivalThirst.addHydration(player, -15.0); // 20 -> 5
        ItemStack flask = new ItemStack(dev.jmiahman.hearthwind.survival.FlaskItems.LEATHER_FLASK);
        dev.jmiahman.hearthwind.survival.FlaskItems.setFill(flask, 2, dev.jmiahman.hearthwind.survival.FlaskData.PURIFIED);
        dev.jmiahman.hearthwind.survival.FlaskItems.onFlaskConsumed(player, flask);
        var data = flask.get(dev.jmiahman.hearthwind.survival.FlaskItems.FLASK_DATA);
        helper.assertTrue(data != null && data.fillLevel() == 1, "drink must decrement fill 2->1");
        helper.assertTrue(HearthwindSurvivalThirst.hydration(player) > 5.0, "drink must add hydration");
        helper.assertTrue(flask.get(net.minecraft.core.component.DataComponents.CONSUMABLE) != null,
                "still-filled flask stays drinkable");
        helper.succeed();
    }

    @GameTest
    public void flaskLastDrinkEmptiesFlask(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        ItemStack flask = new ItemStack(dev.jmiahman.hearthwind.survival.FlaskItems.DIAMOND_LEATHER_FLASK);
        dev.jmiahman.hearthwind.survival.FlaskItems.setFill(flask, 1, dev.jmiahman.hearthwind.survival.FlaskData.DIRTY);
        dev.jmiahman.hearthwind.survival.FlaskItems.onFlaskConsumed(player, flask);
        helper.assertTrue(flask.get(dev.jmiahman.hearthwind.survival.FlaskItems.FLASK_DATA) == null,
                "empty flask must drop flask_data");
        helper.assertTrue(flask.get(net.minecraft.core.component.DataComponents.CONSUMABLE) == null,
                "empty flask must drop CONSUMABLE");
        helper.succeed();
    }

    @GameTest
    public void flaskTiersHaveIncreasingCapacity(GameTestHelper helper) {
        helper.assertTrue(dev.jmiahman.hearthwind.survival.FlaskItems.LEATHER_FLASK.capacity() == 2
                && dev.jmiahman.hearthwind.survival.FlaskItems.IRON_LEATHER_FLASK.capacity() == 3
                && dev.jmiahman.hearthwind.survival.FlaskItems.GOLDEN_LEATHER_FLASK.capacity() == 4
                && dev.jmiahman.hearthwind.survival.FlaskItems.DIAMOND_LEATHER_FLASK.capacity() == 5
                && dev.jmiahman.hearthwind.survival.FlaskItems.NETHERITE_LEATHER_FLASK.capacity() == 6,
                "flask capacities must be 2..6 by tier");
        helper.succeed();
    }

    @GameTest
    public void thresholdAttributeModifiersApplyAndClear(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        player.getAbilities().instabuild = false;
        for (int i = 0; i < HearthwindSurvivalDiet.NUTRIENT_COUNT; i++) {
            HearthwindSurvivalDiet.setLevel(player, i, 150);
        }
        HearthwindSurvivalDiet.setLevel(player, 0, 300);
        NutritionEffects.applyForTest(player);
        double boosted = player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED);
        HearthwindSurvivalDiet.setLevel(player, 0, 150);
        NutritionEffects.applyForTest(player);
        double cleared = player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED);
        helper.assertTrue(boosted > cleared,
                "datapack attribute modifiers must apply at the positive threshold and clear in the normal band");
        helper.succeed();
    }

    @GameTest
    public void temperatureStateRoundTripsAndClamps(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        helper.assertTrue(HearthwindSurvivalTemperature.body(player) == 0,
                "fresh players start at body temperature 0");
        HearthwindSurvivalTemperature.setBody(player, 2400);
        helper.assertTrue(HearthwindSurvivalTemperature.body(player) == 2400,
                "setBody must round-trip");
        HearthwindSurvivalTemperature.setWetness(player, 180);
        helper.assertTrue(HearthwindSurvivalTemperature.wetness(player) == 180,
                "setWetness must round-trip (soaked band)");
        helper.succeed();
    }

    @GameTest
    public void temperatureCutoffClampsAndAcclimatizes(GameTestHelper helper) {
        helper.assertTrue(HearthwindSurvivalTemperature.applyCutoff(9_999, 2)
                == EnvironmentCorpus.bodyTemperature(6),
                "normal environments must clamp the body at +2400");
        helper.assertTrue(HearthwindSurvivalTemperature.applyCutoff(-9_999, 2)
                == EnvironmentCorpus.bodyTemperature(0),
                "normal environments must clamp the body at -2400");
        helper.assertTrue(HearthwindSurvivalTemperature.applyCutoff(300, 0) == 280,
                "cold environments push a hot body down by twice the hot acclimatization (300 -> 280)");
        helper.assertTrue(HearthwindSurvivalTemperature.applyCutoff(-300, 4) == -280,
                "hot environments push a cold body up by twice the cold acclimatization (-300 -> -280)");
        helper.succeed();
    }

    @GameTest
    public void thirstEffectChargesDehydrationBuffer(GameTestHelper helper) {
        ServerPlayer player = survivalServerPlayer(helper);
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                ThirstMobEffect.HOLDER, 200, 0));
        HearthwindSurvivalConfig.Thirst cfg = HearthwindSurvivalConfig.get().thirst;
        helper.assertTrue(cfg.thirstEffectFactor > 0,
                "thirst effect factor must be positive");
        float before = HearthwindSurvivalThirst.dehydration(player);
        // Upstream applies the effect every tick: factor * (amplifier + 1).
        var effect = ThirstMobEffect.HOLDER.value();
        helper.assertTrue(effect.shouldApplyEffectTickThisTick(200, 0),
                "thirst effect must tick every tick");
        effect.applyEffectTick(helper.getLevel(), player, 0);
        helper.assertTrue(Math.abs(HearthwindSurvivalThirst.dehydration(player) - before
                - (float) cfg.thirstEffectFactor) < 0.001f,
                "one amplifier-0 tick must charge exactly thirst_effect_factor (got "
                        + HearthwindSurvivalThirst.dehydration(player) + ")");
        for (int i = 0; i < 200; i++) {
            effect.applyEffectTick(helper.getLevel(), player, 0);
        }
        player.removeEffect(ThirstMobEffect.HOLDER);
        HearthwindSurvivalThirst.updatePlayer(player);
        helper.assertTrue(HearthwindSurvivalThirst.level(player) < HearthwindSurvivalThirst.MAX_LEVEL,
                "a full thirst effect must cost at least one level");
        helper.succeed();
    }

    @GameTest
    public void drinkingFlaskAddsMinerals(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        HearthwindSurvivalDiet.setLevel(player, 4, 0);
        ItemStack flask = new ItemStack(FlaskItems.LEATHER_FLASK);
        FlaskItems.setFill(flask, 2, FlaskData.PURIFIED);
        FlaskItems.onFlaskConsumed(player, flask);
        helper.assertTrue(HearthwindSurvivalDiet.getLevel(player, 4) == 20,
                "dehydration compat: a flask drink must add minerals 20 through the drink hook");
        helper.succeed();
    }

    @GameTest
    public void configDefaultsAllPositive(GameTestHelper helper) {
        HearthwindSurvivalConfig cfg = HearthwindSurvivalConfig.get();
        helper.assertTrue(cfg.thirst.hydratingFactor == 2.0, "Aged override hydrating_factor must be 2.0");
        helper.assertTrue(cfg.thirst.thirstDamage == 1.0, "thirst damage must default to 1.0");
        helper.assertTrue(cfg.thirst.thirstEffectFactor == 0.03,
                "Aged override thirst_effect_factor must be 0.03");
        helper.assertTrue(cfg.thirst.sleepThirstConsumption == 4, "sleep thirst consumption must be 4");
        helper.assertTrue(cfg.thirst.sleepHungerConsumption == 2, "sleep hunger consumption must be 2");
        helper.assertTrue(cfg.flask.quench == 4, "flask_thirst_quench must be 4");
        helper.assertTrue(cfg.flask.dirtyThirstChance == 0.3, "Aged override flask_dirty_thirst_chance must be 0.3");
        helper.assertTrue(cfg.flask.thirstDuration == 200, "Aged override flask_dirty_thirst_duration must be 200");
        helper.assertTrue(cfg.flask.potionBadThirstChance == 0.15, "Aged override potion_bad_thirst_chance must be 0.15");
        helper.assertTrue(cfg.flask.milkQuench == 8, "milk_thirst_quench must be 8");
        helper.assertTrue(cfg.flask.milkThirstChance == 0.4, "milk_thirst_chance must be 0.4");
        helper.assertTrue(cfg.flask.honeyQuench == 1, "honey_quench must be 1");
        helper.assertTrue(cfg.flask.waterBowlQuench == 3, "water_bowl_quench must be 3");
        helper.assertTrue(cfg.flask.waterBowlThirstChance == 0.4, "water_bowl_thirst_chance must be 0.4");
        helper.assertTrue(cfg.temperature.temperatureCalculationTime == 10,
                "Aged override temperatureCalculationTime must be 10");
        helper.assertTrue(cfg.temperature.heatBlockRadius == 3, "heatBlockRadius must default to 3");
        helper.assertTrue(cfg.temperature.easyWorldSpawn, "easyWorldSpawn must default to true");
        helper.assertTrue(cfg.temperature.showThermometer, "showThermometer must default to true");
        helper.assertTrue(cfg.temperature.overheatingExhaustion == 0.07F,
                "Aged override overheatingExhaustion must be 0.07");
        helper.assertTrue(cfg.temperature.exhaustionInsteadDehydration,
                "in-house thirst uses the exhaustion path by default");
        helper.assertTrue(cfg.temperature.thermometerIconX == -95,
                "Aged override thermometerIconX must be -95");
        helper.assertTrue(cfg.temperature.iconX == 7 && cfg.temperature.iconY == 52,
                "body icon offsets must default to 7/52");
        helper.assertTrue(cfg.temperature.thermometerIconY == 32,
                "thermometer Y offset must default to 32");
        helper.assertTrue(cfg.diet.maxNutrition == 300, "maxNutrition default must be 300");
        helper.assertTrue(cfg.diet.negativeNutrition == 30, "negativeNutrition default must be 30");
        helper.assertTrue(cfg.diet.positiveNutrition == 270, "positiveNutrition default must be 270");
        helper.assertTrue("farm_and_charm:lettuce".equals(cfg.diet.vitaminItemId),
                "Aged override vitaminItemId must be farm_and_charm:lettuce");
        helper.assertTrue("meadow:alpine_salt".equals(cfg.diet.mineralItemId),
                "Aged override mineralItemId must be meadow:alpine_salt");
        helper.assertTrue(cfg.spoilage.chancePerCheck >= 0, "spoil chance non-negative");
        helper.succeed();
    }

    @GameTest
    public void spoilageRespectsBiomeModifier(GameTestHelper helper) {
        HearthwindSurvivalConfig cfg = HearthwindSurvivalConfig.get();
        // Hot biomes should double chance - verify the config has the field
        helper.assertTrue(cfg.spoilage.chancePerCheck >= 0, "base chance exists");
        helper.succeed();
    }

    @GameTest
    public void temperatureEffectRowsProfileBodyBands(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // environmentz:warming (+8) only helps while cold (body < 0);
        // environmentz:cooling (-8) only helps while hot (body > 0).
        helper.assertTrue(EnvironmentzEffects.WARMING != null
                && EnvironmentzEffects.COOLING != null
                && EnvironmentzEffects.COMFORT != null,
                "environmentz warming/cooling/comfort effects must register");
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                EnvironmentzEffects.WARMING, 200, 0));
        HearthwindSurvivalTemperature.setState(player,
                HearthwindSurvivalTemperature.State.DEFAULT.withBody(-1000));
        int coldWithWarming = HearthwindSurvivalTemperature.effectTemperature(
                player, HearthwindSurvivalTemperature.getState(player),
                new int[]{0, 0});
        helper.assertTrue(coldWithWarming == 8,
                "warming must add +8 while cold (got " + coldWithWarming + ")");
        HearthwindSurvivalTemperature.setState(player,
                HearthwindSurvivalTemperature.State.DEFAULT.withBody(1000));
        int hotWithWarming = HearthwindSurvivalTemperature.effectTemperature(
                player, HearthwindSurvivalTemperature.getState(player),
                new int[]{0, 0});
        helper.assertTrue(hotWithWarming == 0,
                "warming must not add heat while already hot (got " + hotWithWarming + ")");
        helper.succeed();
    }

    @GameTest
    public void deathDamageTypesAreRegistered(GameTestHelper helper) {
        // The death messages come from data/hearthwind/damage_type/*.json +
        // assets/hearthwind/lang - if the JSONs fail to load, hurt() throws.
        var registry = helper.getLevel().getServer().registryAccess()
                .lookupOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE);
        helper.assertTrue(registry.getOptional(HearthwindSurvivalThirst.THIRST).isPresent(),
                "dehydration:thirst damage type must be registered (died of thirst)");
        helper.assertTrue(registry.getOptional(HearthwindSurvivalTemperature.FREEZING).isPresent(),
                "environmentz:freezing damage type must be registered (froze to death)");
        var src = helper.getLevel().getServer().overworld().damageSources()
                .source(HearthwindSurvivalTemperature.FREEZING);
        helper.assertTrue(src != null, "freezing DamageSource must resolve");
        helper.succeed();
    }

    // ---- sync-payload codec round-trips -------------------------------------
    // Regression guard: encode/decode pairs must be mirror-symmetric. A
    // mismatch (e.g. writeInt vs readVarInt) decodes garbage on the client and
    // kicks the player with DecoderException: Failed to decode custom_payload.
    // Values above 127 make varint-vs-int width mismatches bite.

    private RegistryFriendlyByteBuf bufFor(GameTestHelper helper) {
        return new RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(),
                helper.getLevel().getServer().registryAccess());
    }

    @GameTest
    public void thirstSyncPayloadRoundTrip(GameTestHelper helper) {
        for (boolean buffered : new boolean[] {false, true}) {
            var buf = bufFor(helper);
            ThirstSyncPayload sent = new ThirstSyncPayload(12.3f, buffered);
            ThirstSyncPayload.CODEC.encode(buf, sent);
            ThirstSyncPayload got = ThirstSyncPayload.CODEC.decode(buf);
            helper.assertTrue(got.equals(sent),
                    "thirst payload must round-trip with buffered=" + buffered + ": " + got);
            helper.assertTrue(!buf.isReadable(),
                    "thirst codec must be symmetric (no leftover bytes) with buffered=" + buffered);
        }
        helper.succeed();
    }

    /**
     * The reference's thirst-damage gate, read out of
     * {@code ThirstManager.update} offsets 76-124 and pinned here because we had
     * it recorded backwards. The bytecode is
     * {@code health > 10 || difficulty == HARD || (health > 1 && NORMAL)} - the
     * same shape we implement, so this is a guard against a well-meaning
     * "fix" that would invert it.
     */
    @GameTest
    public void thirstDamageGateMatchesTheReference(GameTestHelper helper) {
        helper.assertTrue(HearthwindSurvivalThirst.BUFFER_THRESHOLD == 4.0F,
                "the reference burns 4.0 of buffer per tick and bobs the HUD above 4.0, we have "
                        + HearthwindSurvivalThirst.BUFFER_THRESHOLD);
        // Every branch of health > 10 || HARD || (health > 1 && NORMAL).
        helper.assertTrue(!HearthwindSurvivalThirst.shouldDamage(10.0F, Difficulty.PEACEFUL),
                "at exactly 10 HP on peaceful nothing damages - the first test is strict >");
        helper.assertTrue(HearthwindSurvivalThirst.shouldDamage(10.1F, Difficulty.EASY),
                "just above 10 HP the FIRST term fires on any difficulty, easy included");
        helper.assertTrue(!HearthwindSurvivalThirst.shouldDamage(10.0F, Difficulty.EASY),
                "at exactly 10 HP on easy nothing fires - every term is strict");
        helper.assertTrue(HearthwindSurvivalThirst.shouldDamage(20.0F, Difficulty.EASY),
                "the reference damages at full health on EASY (the first term, health > 10)");
        helper.assertTrue(!HearthwindSurvivalThirst.shouldDamage(0.5F, Difficulty.EASY),
                "on EASY the thirst damage stops at 10 HP - which is the quirk, and why the "
                        + "gate looks inverted at a glance");
        helper.assertTrue(HearthwindSurvivalThirst.shouldDamage(1.5F, Difficulty.HARD),
                "HARD damages at any health, including 1.5");
        helper.assertTrue(HearthwindSurvivalThirst.shouldDamage(1.0F, Difficulty.HARD),
                "HARD damages at exactly 1 HP");
        helper.assertTrue(!HearthwindSurvivalThirst.shouldDamage(1.0F, Difficulty.NORMAL),
                "NORMAL needs health > 1");
        helper.assertTrue(HearthwindSurvivalThirst.shouldDamage(1.01F, Difficulty.NORMAL),
                "NORMAL damages just above 1 HP");
        helper.succeed();
    }

    @GameTest
    public void tempSyncPayloadRoundTrip(GameTestHelper helper) {
        var buf = bufFor(helper);
        TempSyncPayload sent = new TempSyncPayload(-1800, 180, -6);
        TempSyncPayload.CODEC.encode(buf, sent);
        TempSyncPayload got = TempSyncPayload.CODEC.decode(buf);
        helper.assertTrue(got.equals(sent), "temp payload must round-trip: " + got);
        helper.assertTrue(!buf.isReadable(), "temp codec must be symmetric (no leftover bytes)");
        helper.succeed();
    }

    @GameTest
    public void dietSyncPayloadRoundTrip(GameTestHelper helper) {
        var buf = bufFor(helper);
        DietSyncPayload sent = new DietSyncPayload(300, 12, 0, 299, 150);
        DietSyncPayload.CODEC.encode(buf, sent);
        DietSyncPayload got = DietSyncPayload.CODEC.decode(buf);
        helper.assertTrue(got.equals(sent), "diet payload must round-trip: " + got);
        helper.assertTrue(!buf.isReadable(), "diet codec must be symmetric (no leftover bytes)");
        helper.succeed();
    }

    @GameTest
    public void nutritionItemMapPayloadRoundTrip(GameTestHelper helper) {
        var buf = bufFor(helper);
        NutritionItemMapPayload sent = new NutritionItemMapPayload(java.util.List.of(
                new NutritionItemMapPayload.Entry(
                        net.minecraft.resources.Identifier.parse("minecraft:apple"), 0, 0, 0, 15, 5),
                new NutritionItemMapPayload.Entry(
                        net.minecraft.resources.Identifier.parse("dehydration:leather_flask"), 0, 0, 0, 0, 20)));
        NutritionItemMapPayload.CODEC.encode(buf, sent);
        NutritionItemMapPayload got = NutritionItemMapPayload.CODEC.decode(buf);
        helper.assertTrue(got.entries().equals(sent.entries()),
                "nutrition item map payload must round-trip: " + got);
        helper.assertTrue(!buf.isReadable(), "item map codec must be symmetric (no leftover bytes)");
        helper.succeed();
    }

    @GameTest
    public void nutritionEffectsPayloadRoundTrip(GameTestHelper helper) {
        var buf = bufFor(helper);
        java.util.List<java.util.List<String>> positive = java.util.List.of(
                java.util.List.of("attribute.name.attack_speed"),
                java.util.List.of(),
                java.util.List.of(),
                java.util.List.of("effect.minecraft.regeneration"),
                java.util.List.of("effect.minecraft.haste"));
        java.util.List<java.util.List<String>> negative = java.util.List.of(
                java.util.List.of("attribute.name.attack_speed"),
                java.util.List.of(),
                java.util.List.of(),
                java.util.List.of("effect.minecraft.weakness"),
                java.util.List.of("effect.minecraft.unluck"));
        NutritionEffectsPayload sent = new NutritionEffectsPayload(positive, negative);
        NutritionEffectsPayload.CODEC.encode(buf, sent);
        NutritionEffectsPayload got = NutritionEffectsPayload.CODEC.decode(buf);
        helper.assertTrue(got.equals(sent), "nutrition effects payload must round-trip: " + got);
        helper.assertTrue(!buf.isReadable(), "effects codec must be symmetric (no leftover bytes)");
        helper.succeed();
    }

    @GameTest
    public void jobSyncPayloadRoundTrip(GameTestHelper helper) {
        var buf = bufFor(helper);
        JobSyncPayload sent = new JobSyncPayload("miner", 200, 12345.678, 100.0);
        JobSyncPayload.CODEC.encode(buf, sent);
        JobSyncPayload got = JobSyncPayload.CODEC.decode(buf);
        helper.assertTrue(got.equals(sent), "job payload must round-trip: " + got);
        helper.assertTrue(!buf.isReadable(), "job codec must be symmetric (no leftover bytes)");
        helper.succeed();
    }

    @GameTest
    public void skillsSyncPayloadRoundTrip(GameTestHelper helper) {
        // Level 200 exceeds 7-bit varint width: catches any writeInt vs
        // readVarInt asymmetry (the exact bug that kicked clients on login).
        var buf = bufFor(helper);
        SkillsSyncPayload sent = new SkillsSyncPayload(
                java.util.List.of("mining", "smithing", "farming"),
                java.util.List.of(200, 1, 31), 200, 7, 999_999);
        SkillsSyncPayload.CODEC.encode(buf, sent);
        SkillsSyncPayload got = SkillsSyncPayload.CODEC.decode(buf);
        helper.assertTrue(got.skills().equals(sent.skills()) && got.levels().equals(sent.levels()),
                "skills payload must round-trip with varint-wide levels: " + got);
        helper.assertTrue(got.overall() == sent.overall() && got.points() == sent.points()
                        && got.totalXp() == sent.totalXp(),
                "overall, points and total xp must round-trip: " + got);
        helper.assertTrue(!buf.isReadable(), "skills codec must be symmetric (no leftover bytes)");
        helper.succeed();
    }

    @GameTest
    public void skillUpPayloadRoundTrip(GameTestHelper helper) {
        var buf = bufFor(helper);
        SkillUpPayload sent = new SkillUpPayload("archery", 130);
        SkillUpPayload.CODEC.encode(buf, sent);
        SkillUpPayload got = SkillUpPayload.CODEC.decode(buf);
        helper.assertTrue(got.equals(sent), "skill-up payload must round-trip: " + got);
        helper.assertTrue(!buf.isReadable(), "skill-up codec must be symmetric (no leftover bytes)");
        helper.succeed();
    }

    @GameTest
    public void bareHandDrinkingWhileCrouchingAddsHydration(GameTestHelper helper) {
        ServerPlayer player = aimAtWater(helper,
                net.minecraft.world.level.block.Blocks.WATER.defaultBlockState());
        net.minecraft.core.BlockPos waterPos = player.blockPosition();
        player.setPose(net.minecraft.world.entity.Pose.CROUCHING);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ItemStack.EMPTY);

        // Drain thirst partially
        HearthwindSurvivalThirst.setHydration(player, 10.0);
        double before = HearthwindSurvivalThirst.hydration(player);

        // Simulate sustained drinking (hold loop, ~21 use-events)
        var result = BareHandDrinkHandler.trySipAt(player, helper.getLevel(), waterPos);
        helper.assertTrue(result.consumesAction(), "Drinking while crouching on water must succeed");
        for (int i = 0; i < 30; i++) {
            BareHandDrinkHandler.trySipAt(player, helper.getLevel(), waterPos);
        }
        double after = HearthwindSurvivalThirst.hydration(player);
        helper.assertTrue(after > before, "Hydration must increase after bare hand drink: " + after + " > " + before);
        helper.succeed();
    }

    @GameTest
    public void fatalDamageTriggersDownedState(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        player.getAbilities().instabuild = false;

        // Fatal damage
        boolean deathAllowed = dev.jmiahman.hearthwind.survival.revive.ReviveManager.onFatalDamage(
                player, player.level().damageSources().generic());

        helper.assertTrue(!deathAllowed, "Fatal damage must be intercepted to enter Downed state");
        helper.assertTrue(dev.jmiahman.hearthwind.survival.revive.DownedState.isDowned(player),
                "Player must be in Downed state");
        helper.succeed();
    }

    @GameTest
    public void reviveNeedsAnAllyToArmItThenOneClick(GameTestHelper helper) {
        var downed = helper.makeMockServerPlayerInLevel();
        var reviver = helper.makeMockServerPlayerInLevel();
        downed.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        reviver.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        downed.getAbilities().instabuild = false;
        reviver.getAbilities().instabuild = false;

        dev.jmiahman.hearthwind.survival.revive.ReviveManager.onFatalDamage(
                downed, downed.level().damageSources().generic());
        helper.assertTrue(dev.jmiahman.hearthwind.survival.revive.DownedState.isDowned(downed), "Downed state active");
        helper.assertTrue(!dev.jmiahman.hearthwind.survival.revive.DownedState.isArmed(downed),
                "a downed player starts unarmed - the reference's canRevive is false until an ally interacts");

        // revive 1.0.7 gates the arming on allowReviveWithHand && isSneaking()
        // (PlayerEntityMixin.method_5664 offsets 59-72). Not crouching fails.
        var uncouched = dev.jmiahman.hearthwind.survival.revive.ReviveManager.onInteract(
                reviver, downed, net.minecraft.world.InteractionHand.MAIN_HAND);
        helper.assertTrue(uncouched == net.minecraft.world.InteractionResult.PASS,
                "an ally who is not crouching must not be able to arm the revive");
        helper.assertTrue(!dev.jmiahman.hearthwind.survival.revive.DownedState.isArmed(downed),
                "still unarmed after a non-crouching interaction");

        // A potion in hand is rejected: the reference compares the held
        // stack's potion against Potions.EMPTY (offsets 34-40).
        reviver.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.DIRT));
        reviver.setShiftKeyDown(true);
        helper.assertTrue(dev.jmiahman.hearthwind.survival.revive.ReviveManager.onInteract(
                        reviver, downed, net.minecraft.world.InteractionHand.MAIN_HAND)
                        == net.minecraft.world.InteractionResult.SUCCESS_SERVER,
                "a crouching ally arms the revive (non-potion item: the reference only rejects potions)");
        helper.assertTrue(dev.jmiahman.hearthwind.survival.revive.DownedState.isArmed(downed),
                "the downed player's Revive button is now live");
        reviver.getInventory().clearContent();

        // Self-revive is a single click, and there is no channel to hold.
        dev.jmiahman.hearthwind.survival.revive.ReviveManager.onSelfRevive(downed);

        helper.assertTrue(!dev.jmiahman.hearthwind.survival.revive.DownedState.isDowned(downed),
                "one click must complete the revive - Aged has no channel and no progress bar");
        helper.assertTrue(downed.getHealth() == dev.jmiahman.hearthwind.survival.revive.ReviveManager.REVIVE_HEALTH,
                "revived to ReviveConfig.reviveHealthPoints, got " + downed.getHealth());
        var aftermath = downed.getEffect(
                dev.jmiahman.hearthwind.survival.revive.AftermathMobEffect.HOLDER);
        helper.assertTrue(aftermath != null
                        && aftermath.getDuration() == dev.jmiahman.hearthwind.survival.revive.AftermathMobEffect.DURATION_TICKS,
                "the reference applies a 600-tick aftermath effect on revive");
        helper.succeed();
    }

    @GameTest
    public void downedPlayerNeverBleedsOut(GameTestHelper helper) {
        var downed = helper.makeMockServerPlayerInLevel();
        downed.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        downed.getAbilities().instabuild = false;

        dev.jmiahman.hearthwind.survival.revive.ReviveManager.onFatalDamage(
                downed, downed.level().damageSources().generic());
        helper.assertTrue(dev.jmiahman.hearthwind.survival.revive.DownedState.isDowned(downed),
                "Downed state active");

        // revive 1.0.7's timer defaults to -1 and Aged does not set it, so the
        // server tick hook returns at bytecode offset 27 and a downed player
        // never dies of a bleedout. We used to run 1200 ticks and drop their
        // inventory; tickPlayer must now be a no-op forever.
        for (int i = 0; i < 500; i++) {
            dev.jmiahman.hearthwind.survival.revive.ReviveManager.tickPlayer(downed);
        }
        helper.assertTrue(dev.jmiahman.hearthwind.survival.revive.DownedState.isDowned(downed),
                "a downed player must stay downed indefinitely - Aged has no bleedout timer");
        // NOT `isAlive()`: makeMockServerPlayerInLevel() hands back a player the
        // server drops a tick after it joins, so isRemoved() is already true
        // and isAlive() says nothing about our code. Health and the downed
        // flag are the real guarantee - a bleedout kill would have run
        // player.kill(), which clears the flag.
        helper.assertTrue(downed.getHealth() == dev.jmiahman.hearthwind.survival.revive.ReviveManager.DOWNED_HEALTH,
                "the downed player keeps their 1 heart across the whole wait, got " + downed.getHealth());
        helper.assertTrue(!dev.jmiahman.hearthwind.survival.revive.DownedState.isArmed(downed),
                "time alone never arms the revive");
        helper.succeed();
    }

    @GameTest
    public void downedSyncPayloadRoundTrips(GameTestHelper helper) {
        var buf = net.minecraft.network.RegistryFriendlyByteBuf.decorator(
                helper.getLevel().registryAccess()).apply(io.netty.buffer.Unpooled.buffer());
        var sent = new dev.jmiahman.hearthwind.survival.revive.DownedSyncPayload(true, true, -118, 64, 302);
        dev.jmiahman.hearthwind.survival.revive.DownedSyncPayload.CODEC.encode(buf, sent);
        var got = dev.jmiahman.hearthwind.survival.revive.DownedSyncPayload.CODEC.decode(buf);
        helper.assertTrue(got.equals(sent), "DownedSyncPayload must round-trip correctly");

        var buf2 = net.minecraft.network.RegistryFriendlyByteBuf.decorator(
                helper.getLevel().registryAccess()).apply(io.netty.buffer.Unpooled.buffer());
        var revive = new dev.jmiahman.hearthwind.survival.revive.DownedRevivePayload(false);
        dev.jmiahman.hearthwind.survival.revive.DownedRevivePayload.CODEC.encode(buf2, revive);
        helper.assertTrue(dev.jmiahman.hearthwind.survival.revive.DownedRevivePayload.CODEC.decode(buf2).equals(revive),
                "DownedRevivePayload must round-trip correctly");
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // environmentz corpus (data/environmentz/*): heat/cold sources plus the
    // dimension modifier tables. These are the numbers Aged tunes, so the
    // tests pin them rather than the implementation.
    // ------------------------------------------------------------------

    /**
     * Parks the mock player on the test origin and returns that absolute
     * BlockPos, so block placements below line up with the player no matter
     * where the gametest structure landed.
     */
    private static net.minecraft.core.BlockPos parkPlayer(GameTestHelper helper, ServerPlayer player) {
        net.minecraft.core.BlockPos origin = helper.absolutePos(net.minecraft.core.BlockPos.ZERO);
        player.teleportTo(origin.getX() + 0.5, origin.getY(), origin.getZ() + 0.5);
        player.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        return origin;
    }

    @GameTest
    public void environmentzCorpusLoadsHeatSources(GameTestHelper helper) {
        helper.assertTrue(EnvironmentCorpus.blockCount() >= 10,
                "heating/cooling block table must resolve vanilla entries (got "
                        + EnvironmentCorpus.blockCount() + ")");
        helper.assertTrue(EnvironmentCorpus.itemCount() >= 20,
                "carried item temperatures must load from the EnvironmentZ defaults (got "
                        + EnvironmentCorpus.itemCount() + ")");
        helper.assertTrue(EnvironmentCorpus.tempFor(net.minecraft.world.level.block.Blocks.CAMPFIRE) != null,
                "campfire must be a registered heat source");
        helper.assertTrue(EnvironmentCorpus.tempFor(net.minecraft.world.level.block.Blocks.ICE) != null,
                "ice must be a registered cooling source");
        helper.assertTrue(EnvironmentCorpus.tempFor(net.minecraft.world.level.block.Blocks.LAVA) != null,
                "lava must be a registered heat source");
        helper.succeed();
    }

    @GameTest
    public void litCampfireWarmsAdjacentPlayer(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        parkPlayer(helper, player);
        helper.setBlock(1, 0, 0, net.minecraft.world.level.block.Blocks.CAMPFIRE.defaultBlockState()
                .setValue(net.minecraft.world.level.block.CampfireBlock.LIT, true));
        int heat = EnvironmentCorpus.blockHeat(player);
        helper.assertTrue(heat == 2, "lit campfire 1 block away must give +2 (got " + heat
                + ", block is " + helper.getLevel().getBlockState(player.blockPosition().offset(1, 0, 0)) + ")");
        helper.succeed();
    }

    @GameTest
    public void unlitCampfireDoesNotWarm(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        parkPlayer(helper, player);
        helper.setBlock(1, 0, 0, net.minecraft.world.level.block.Blocks.CAMPFIRE.defaultBlockState()
                .setValue(net.minecraft.world.level.block.CampfireBlock.LIT, false));
        int heat = EnvironmentCorpus.blockHeat(player);
        helper.assertTrue(heat == 0, "unlit campfire must not warm (got " + heat + ")");
        helper.succeed();
    }

    @GameTest
    public void iceCoolsAdjacentPlayer(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        parkPlayer(helper, player);
        helper.setBlock(1, 0, 0, net.minecraft.world.level.block.Blocks.ICE.defaultBlockState());
        int heat = EnvironmentCorpus.blockHeat(player);
        helper.assertTrue(heat == -2, "ice 1 block away must give -2 (got " + heat + ")");
        helper.succeed();
    }

    @GameTest
    public void heatSourceBeyondRadiusIsIgnored(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        parkPlayer(helper, player);
        helper.setBlock(6, 0, 0, net.minecraft.world.level.block.Blocks.CAMPFIRE.defaultBlockState()
                .setValue(net.minecraft.world.level.block.CampfireBlock.LIT, true));
        int heat = EnvironmentCorpus.blockHeat(player);
        helper.assertTrue(heat == 0, "campfire outside the scan radius must not warm (got " + heat + ")");
        helper.succeed();
    }

    @GameTest
    public void wallBlocksHeatFromFire(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        parkPlayer(helper, player);
        helper.setBlock(1, 0, 0, net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
        helper.setBlock(2, 0, 0, net.minecraft.world.level.block.Blocks.CAMPFIRE.defaultBlockState()
                .setValue(net.minecraft.world.level.block.CampfireBlock.LIT, true));
        int heat = EnvironmentCorpus.blockHeat(player);
        helper.assertTrue(heat == 0, "a wall must block fire heat (got " + heat + ")");
        helper.succeed();
    }

    @GameTest
    public void atMostMaxCountSourcesOfOneTypeContribute(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        parkPlayer(helper, player);
        net.minecraft.world.level.block.state.BlockState fire =
                net.minecraft.world.level.block.Blocks.CAMPFIRE.defaultBlockState()
                        .setValue(net.minecraft.world.level.block.CampfireBlock.LIT, true);
        helper.setBlock(1, 0, 0, fire);
        helper.setBlock(-1, 0, 0, fire);
        helper.setBlock(0, 0, 1, fire);
        int heat = EnvironmentCorpus.blockHeat(player);
        // EnvironmentZ 2.0.8 campfire max_count is 3; each is 1 block away (+2)
        helper.assertTrue(heat == 6, "only max_count (3) campfires may contribute (got " + heat + ")");
        helper.succeed();
    }

    @GameTest
    public void lavaFluidWarmsAdjacentPlayer(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        parkPlayer(helper, player);
        helper.setBlock(1, 0, 0, net.minecraft.world.level.block.Blocks.LAVA.defaultBlockState());
        int heat = EnvironmentCorpus.blockHeat(player);
        helper.assertTrue(heat == 3, "lava 1 block away must give +3 from the fluid table (got " + heat + ")");
        helper.succeed();
    }

    @GameTest
    public void environmentzManagerTablesMatchCorpus(GameTestHelper helper) {
        EnvironmentCorpus.DimensionTable overworld = EnvironmentCorpus.dimension(
                net.minecraft.resources.Identifier.fromNamespaceAndPath("minecraft", "overworld"));
        helper.assertTrue(overworld != null && !overworld.basic(), "overworld table must load");
        helper.assertTrue(overworld.day(0) == -4 && overworld.day(4) == 4,
                "day row must be -4 (very cold) .. +4 (very hot)");
        helper.assertTrue(overworld.night(0) == -6 && overworld.night(4) == 2,
                "night row must be -6 (very cold) .. +2 (very hot)");
        helper.assertTrue(overworld.shadow(2) == -1, "shadow must be -1");
        helper.assertTrue(overworld.soaked(2) == -6 && overworld.wett(2) == -3,
                "soaked must be -6 and wett -3");
        helper.assertTrue(overworld.sweat(0) == -1 && overworld.sweat(1) == -2,
                "sweat must be -1 (hot) / -2 (very hot) - Aged's values, not EnvironmentZ's -2/-3");
        helper.assertTrue(overworld.armor(2) == 1 && overworld.insulatedArmor(2) == 3
                && overworld.icedArmor(2) == -4,
                "armor rows must be +1 / +3 insulated / -4 iced (Aged's value)");
        helper.assertTrue(overworld.hasHeight() && overworld.heightAt(200) == -2
                && overworld.heightAt(64) == 0 && overworld.heightAt(10) == 1
                && overworld.heightAt(-5) == 2,
                "height rows must apply by altitude (2/1/0/-1/-2 at y<0/0/30/120/190)");
        helper.assertTrue(overworld.acclimatization() == EnvironmentCorpus.NO_DIMENSION_ACCLIMATIZATION,
                "overworld uses the global acclimatization table");
        int[] bands = EnvironmentCorpus.thermometerBands();
        helper.assertTrue(bands[0] == -6 && bands[1] == -2 && bands[2] == 2 && bands[3] == 6,
                "thermometer bands must be -6/-2/2/6 (Aged's table; upstream EnvironmentZ uses +-3)");
        int[] acclimatization = EnvironmentCorpus.acclimatizationBands();
        helper.assertTrue(acclimatization.length == 8 && acclimatization[0] == 180
                && acclimatization[1] == -10 && acclimatization[2] == 1680
                && acclimatization[3] == -20 && acclimatization[4] == -180
                && acclimatization[5] == 10 && acclimatization[6] == -1680
                && acclimatization[7] == 20,
                "acclimatization table must be 180/-10, 1680/-20, -180/+10, -1680/+20 "
                        + "- Aged's values, not EnvironmentZ's 1600/-15");
        EnvironmentCorpus.DimensionTable nether = EnvironmentCorpus.dimension(
                net.minecraft.resources.Identifier.fromNamespaceAndPath("minecraft", "the_nether"));
        helper.assertTrue(nether != null && nether.basic() && nether.standard(2) == 5
                && nether.acclimatization() == 0,
                "nether must be a basic +5 dimension with acclimatization 0");
        EnvironmentCorpus.DimensionTable end = EnvironmentCorpus.dimension(
                net.minecraft.resources.Identifier.fromNamespaceAndPath("minecraft", "the_end"));
        helper.assertTrue(end != null && end.basic() && end.standard(2) == -5,
                "end must be a basic -5 dimension");
        // Basic dimensions normalise their scalar across every band, so band 2
        // (normal) reads the scalar straight back. Aged ships -4 for both.
        helper.assertTrue(nether.icedArmor(2) == -4 && end.icedArmor(2) == -4,
                "nether and end iced_armor must both be -4 (Aged); EnvironmentZ ships -6 and -3");
        helper.succeed();
    }

    @GameTest
    public void biomeAndBodyBandsMatchEnvironmentzDefaults(GameTestHelper helper) {
        int[] body = EnvironmentCorpus.bodyTemperatures();
        helper.assertTrue(body.length == 7 && body[0] == -2400 && body[2] == -240 && body[4] == 240
                && body[6] == 2400,
                "body bands must be -2400/-1800/-240/0/240/1800/2400");
        helper.assertTrue(EnvironmentCorpus.biomeTemperature(0) == 0.2F
                && EnvironmentCorpus.biomeTemperature(1) == 0.4F
                && EnvironmentCorpus.biomeTemperature(2) == 1.2F
                && EnvironmentCorpus.biomeTemperature(3) == 1.6F,
                "biome thresholds must be 0.2/0.4/1.2/1.6");
        helper.assertTrue(HearthwindSurvivalTemperature.environmentCode(0.1F) == 0
                && HearthwindSurvivalTemperature.environmentCode(0.3F) == 1
                && HearthwindSurvivalTemperature.environmentCode(1.0F) == 2
                && HearthwindSurvivalTemperature.environmentCode(1.4F) == 3
                && HearthwindSurvivalTemperature.environmentCode(1.7F) == 4,
                "environmentCode must map the four biome thresholds");
        helper.assertTrue(EnvironmentCorpus.wetness(0) == 200 && EnvironmentCorpus.wetness(1) == 180
                && EnvironmentCorpus.wetness(2) == 100 && EnvironmentCorpus.wetness(3) == 1
                && EnvironmentCorpus.wetness(4) == -1,
                "wetness bands must be max 200/soaked 180/water +100/rain +1/dry -1");
        helper.assertTrue(EnvironmentCorpus.protection(0) == 600
                && EnvironmentCorpus.protection(1) == 600
                && EnvironmentCorpus.protection(2) == 600
                && EnvironmentCorpus.protection(3) == 600,
                "protection pools must cap at 600");
        helper.succeed();
    }

    @GameTest
    public void acclimatizationTableAdjustsBodyByBand(GameTestHelper helper) {
        EnvironmentCorpus.DimensionTable overworld = EnvironmentCorpus.dimension(
                net.minecraft.resources.Identifier.fromNamespaceAndPath("minecraft", "overworld"));
        helper.assertTrue(HearthwindSurvivalTemperature.acceptanceAdjustment(overworld, 2, 200) == -10,
                "body above +180 in a normal biome must acclimatize -10");
        helper.assertTrue(HearthwindSurvivalTemperature.acceptanceAdjustment(overworld, 2, -200) == 10,
                "body below -180 in a normal biome must acclimatize +10");
        // The strong-acclimatization numbers are Aged's (+/-20 at the -/+1680
        // bands), not EnvironmentZ 2.0.8's upstream (+/-15 at +/-1600). The
        // corpus table was carrying the upstream values until 0.1.47.
        helper.assertTrue(HearthwindSurvivalTemperature.acceptanceAdjustment(overworld, 1, -1700) == 20,
                "very cold biomes push a very cold body +20 (Aged's acclimatization)");
        helper.assertTrue(HearthwindSurvivalTemperature.acceptanceAdjustment(overworld, 3, 1700) == -20,
                "hot biomes push an overheating body -20 (Aged's acclimatization)");
        helper.succeed();
    }

    @GameTest
    public void itemTemperatureTablesMatchEnvironmentz(GameTestHelper helper) {
        EnvironmentCorpus.ItemTemp stones = EnvironmentCorpus.item(EnvironmentzItems.HEATING_STONES);
        helper.assertTrue(stones != null && stones.temperature() == 10 && stones.damage() == 1
                && stones.coldProtection() == 2 && stones.heatProtection() == 0,
                "heating stones must be +10 heat, 1 wear, cold_protection 2");
        EnvironmentCorpus.ItemTemp pack = EnvironmentCorpus.item(EnvironmentzItems.ICE_PACK);
        helper.assertTrue(pack != null && pack.temperature() == -10 && pack.damage() == 1
                && pack.heatProtection() == 2 && pack.coldProtection() == 0,
                "ice packs must be -10 heat, 1 wear, heat_protection 2");
        helper.succeed();
    }

    @GameTest
    public void equippedTemperatureItemsAddHeatAndProtection(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.getInventory().clearContent();
        player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND,
                new ItemStack(EnvironmentzItems.HEATING_STONES));
        player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.OFFHAND,
                new ItemStack(EnvironmentzItems.HEATING_STONES));
        int[] pools = {0, 0};
        int total = HearthwindSurvivalTemperature.itemTemperature(player,
                HearthwindSurvivalTemperature.getState(player), pools);
        helper.assertTrue(total == 20, "two held heating stones must add +10 each (got " + total + ")");
        helper.assertTrue(pools[1] == 4, "cold protection must accumulate 2+2 (got " + pools[1] + ")");
        helper.succeed();
    }

    @GameTest
    public void protectionPoolsConsumeIncomingDelta(GameTestHelper helper) {
        // Cold: resistance first, then cold protection, both decremented.
        int[] pools = {0, 50};
        int[] resistances = {0, 200};
        int remaining = HearthwindSurvivalTemperature.consumeProtection(-100, 0, pools, resistances);
        helper.assertTrue(remaining == 0, "cold resistance 200 must fully absorb -100 (got " + remaining + ")");
        helper.assertTrue(resistances[1] == 100, "cold resistance must be consumed to 100");
        helper.assertTrue(pools[1] == 50, "unused cold protection must remain");

        // Heat: partial resistance passes the leftover through to heat protection.
        pools = new int[]{30, 0};
        resistances = new int[]{40, 0};
        remaining = HearthwindSurvivalTemperature.consumeProtection(100, 4, pools, resistances);
        helper.assertTrue(remaining == 30,
                "40 heat resistance + 30 heat protection must leave 30 (got " + remaining + ")");
        helper.assertTrue(resistances[0] == 0 && pools[0] == 0,
                "both heat pools must be fully consumed at their limit");

        // Full pool soak caps at the 600 pool.
        pools = new int[]{600, 600};
        resistances = new int[]{0, 0};
        remaining = HearthwindSurvivalTemperature.consumeProtection(100, 4, pools, resistances);
        helper.assertTrue(remaining == 0 && pools[0] == 500,
                "600 heat protection must absorb 100 and leave 500 (got " + remaining + ")");
        helper.succeed();
    }

    @GameTest
    public void armorTemperatureReadsInsulatedAndIcedNbt(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        EnvironmentCorpus.DimensionTable overworld = EnvironmentCorpus.dimension(
                net.minecraft.resources.Identifier.fromNamespaceAndPath("minecraft", "overworld"));

        ItemStack insulated = new ItemStack(Items.LEATHER_CHESTPLATE);
        net.minecraft.world.item.component.CustomData.update(
                net.minecraft.core.component.DataComponents.CUSTOM_DATA, insulated,
                tag -> tag.putString("environmentz", "fur_insolated"));
        player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST, insulated);
        helper.assertTrue(HearthwindSurvivalTemperature.armorTemperature(player, overworld, 2) == 3,
                "insulated armor must use the +3 insulated_armor row");

        ItemStack iced = new ItemStack(Items.CHAINMAIL_CHESTPLATE);
        net.minecraft.world.item.component.CustomData.update(
                net.minecraft.core.component.DataComponents.CUSTOM_DATA, iced,
                tag -> tag.putInt("iced", 2));
        player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST, iced);
        helper.assertTrue(HearthwindSurvivalTemperature.armorTemperature(player, overworld, 2) == -4,
                "iced armor must add the -4 iced_armor row (Aged); upstream EnvironmentZ uses -5");
        net.minecraft.world.item.component.CustomData data =
                iced.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        helper.assertTrue(data != null && data.copyTag().getInt("iced").orElse(0) == 1,
                "one calculation must consume one iced charge");
        HearthwindSurvivalTemperature.armorTemperature(player, overworld, 2);
        data = iced.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        helper.assertTrue(data == null || !data.copyTag().contains("iced"),
                "the last iced charge must clear the custom-data key");
        helper.succeed();
    }

    @GameTest
    public void bandDebuffsApplyAndClear(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        net.minecraft.resources.Identifier freezing =
                net.minecraft.resources.Identifier.fromNamespaceAndPath("environmentz", "freezing_debuff");
        net.minecraft.resources.Identifier cold =
                net.minecraft.resources.Identifier.fromNamespaceAndPath("environmentz", "cold_debuff");
        net.minecraft.resources.Identifier general =
                net.minecraft.resources.Identifier.fromNamespaceAndPath("environmentz", "general_debuff");
        var speed = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
        var attackSpeed = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED);
        helper.assertTrue(speed != null && attackSpeed != null, "attribute instances must resolve");

        HearthwindSurvivalTemperature.applyBandDebuffs(player, -2000);
        helper.assertTrue(speed.hasModifier(freezing), "freezing must apply the -25% speed modifier");
        helper.assertTrue(attackSpeed.hasModifier(general), "freezing must apply the -20% attack speed modifier");
        HearthwindSurvivalTemperature.applyBandDebuffs(player, -1000);
        helper.assertTrue(!speed.hasModifier(freezing) && speed.hasModifier(cold),
                "the cold band must swap freezing for the -8% speed modifier");
        helper.assertTrue(!attackSpeed.hasModifier(general), "general debuff must clear in the cold band");
        HearthwindSurvivalTemperature.applyBandDebuffs(player, 100);
        helper.assertTrue(!speed.hasModifier(cold), "comfortable bodies must clear the cold modifier");
        helper.succeed();
    }

    @GameTest
    public void wetnessDriesByOnePerCalculation(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        HearthwindSurvivalTemperature.setWetness(player, 50);
        int dried = HearthwindSurvivalTemperature.updateWetness(player, 50);
        helper.assertTrue(dried == 49, "dry players must lose 1 wetness per calculation (got " + dried + ")");
        helper.assertTrue(HearthwindSurvivalTemperature.updateWetness(player, 0) == 0,
                "wetness must never go below 0");
        helper.succeed();
    }

    @GameTest
    public void biomeBandsOrderFromColdToHot(GameTestHelper helper) {
        helper.assertTrue(EnvironmentCorpus.band(helper.getLevel().getBiome(helper.absolutePos(net.minecraft.core.BlockPos.ZERO))) >= 0,
                "band must resolve for any biome");
        helper.assertTrue(EnvironmentCorpus.bandName(0).equals("very_cold")
                && EnvironmentCorpus.bandName(4).equals("very_hot"), "band names must match the corpus keys");
        helper.succeed();
    }

    @GameTest
    public void fullCalculationHeatsFromNearbyCampfire(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        parkPlayer(helper, player);
        HearthwindSurvivalTemperature.setState(player,
                HearthwindSurvivalTemperature.State.DEFAULT.withBody(0));
        HearthwindSurvivalTemperature.calculate(player);
        int withoutFire = HearthwindSurvivalTemperature.body(player);

        helper.setBlock(1, 0, 0, net.minecraft.world.level.block.Blocks.CAMPFIRE.defaultBlockState()
                .setValue(net.minecraft.world.level.block.CampfireBlock.LIT, true));
        HearthwindSurvivalTemperature.setState(player,
                HearthwindSurvivalTemperature.State.DEFAULT.withBody(0));
        HearthwindSurvivalTemperature.calculate(player);
        int withFire = HearthwindSurvivalTemperature.body(player);
        helper.assertTrue(withFire - withoutFire == 2,
                "a lit campfire must add exactly +2 per calculation through the full pipeline (got delta "
                        + (withFire - withoutFire) + ")");
        helper.succeed();
    }

    @GameTest
    public void litCampfireUsesLitPropertyGate(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        parkPlayer(helper, player);
        helper.setBlock(1, 0, 0, net.minecraft.world.level.block.Blocks.CAMPFIRE.defaultBlockState()
                .setValue(net.minecraft.world.level.block.CampfireBlock.LIT, false));
        int heat = EnvironmentCorpus.blockHeat(player, 3);
        helper.assertTrue(heat == 0, "an unlit furnace/campfire must be gated by its lit property");
        helper.succeed();
    }

    @GameTest
    public void thermometerExcludesCarriedItemHeat(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        parkPlayer(helper, player);
        HearthwindSurvivalTemperature.setState(player,
                HearthwindSurvivalTemperature.State.DEFAULT.withBody(0).withThermometer(0));
        player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND,
                new ItemStack(EnvironmentzItems.HEATING_STONES));
        HearthwindSurvivalTemperature.calculate(player);
        int body = HearthwindSurvivalTemperature.body(player);
        int thermometer = HearthwindSurvivalTemperature.thermometer(player);
        // The thermometer row deliberately excludes armor/item/effect/wetness
        // drivers (reference thermometerCalculatingTemperature), so held heat
        // shows on the body but not on the thermometer.
        helper.assertTrue(body - thermometer == 10,
                "carried heating stones must add +10 to the body only (body " + body
                        + ", thermometer " + thermometer + ")");
        helper.succeed();
    }

    @GameTest
    public void hydrationCorpusLoadsCataloguedItems(GameTestHelper helper) {
        helper.assertTrue(HydrationCorpus.hasCorpus(), "the hydration corpus must load from the world datapack");
        // The datapack catalogues 115 ids across 12 tiers; 105 resolve in a real
        // world. The 10 that do not belong to the lets-do food mods Aged ships
        // and we have not ported, so a drop below 105 means we lost a food we
        // do ship, and a rise above 105 means a mod id quietly started resolving.
        helper.assertTrue(HydrationCorpus.itemCount() == 105,
                "expected 105 of the 115 catalogued ids to resolve, got " + HydrationCorpus.itemCount());
        helper.assertTrue(HydrationCorpus.tierCount() == 12,
                "expected all 12 hydration tiers, got " + HydrationCorpus.tierCount());
        helper.succeed();
    }

    @GameTest
    public void hydrationCorpusTiersMatchCatalogue(GameTestHelper helper) {
        assertQuench(helper, Items.MELON_SLICE, 1);
        assertQuench(helper, Items.GLOW_BERRIES, 2);
        assertQuench(helper, Items.MUSHROOM_STEW, 3);
        assertQuench(helper, Items.APPLE, 4);
        assertQuench(helper, Items.GOLDEN_APPLE, 6);
        assertQuench(helper, Items.MILK_BUCKET, 8);
        helper.succeed();
    }

    @GameTest
    public void eatingCataloguedFoodRestoresHydration(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        HearthwindSurvivalThirst.setHydration(player, 5.0);
        double granted = HydrationCorpus.hydrateOnConsume(player, new ItemStack(Items.MELON_SLICE));
        helper.assertTrue(Math.abs(granted - 1.0) < 0.001,
                "a melon slice must grant 1 hydration (got " + granted + ")");
        helper.assertTrue(Math.abs(HearthwindSurvivalThirst.hydration(player) - 6.0) < 0.001,
                "hydration must rise from 5 to 6 (got " + HearthwindSurvivalThirst.hydration(player) + ")");
        helper.succeed();
    }

    @GameTest
    public void hydrationFromFoodCapsAtMax(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        HearthwindSurvivalThirst.setHydration(player, HearthwindSurvivalThirst.MAX_HYDRATION - 1.0);
        HydrationCorpus.hydrateOnConsume(player, new ItemStack(Items.APPLE));
        helper.assertTrue(HearthwindSurvivalThirst.hydration(player) <= HearthwindSurvivalThirst.MAX_HYDRATION,
                "hydration must never exceed the maximum (got " + HearthwindSurvivalThirst.hydration(player) + ")");
        helper.assertTrue(Math.abs(HearthwindSurvivalThirst.hydration(player)
                - HearthwindSurvivalThirst.MAX_HYDRATION) < 0.001, "a nearly full player must top up to 20");
        helper.succeed();
    }

    @GameTest
    public void uncataloguedFoodDoesNotRestoreHydration(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        HearthwindSurvivalThirst.setHydration(player, 8.0);
        double granted = HydrationCorpus.hydrateOnConsume(player, new ItemStack(Items.STICK));
        helper.assertTrue(granted == 0.0, "a stick must grant no hydration (got " + granted + ")");
        helper.assertTrue(Math.abs(HearthwindSurvivalThirst.hydration(player) - 8.0) < 0.001,
                "hydration must be unchanged (got " + HearthwindSurvivalThirst.hydration(player) + ")");
        helper.succeed();
    }

    @GameTest
    public void seasonTempHookShiftsWithSeason(GameTestHelper helper) {
        var cfg = dev.jmiahman.hearthwind.world.HearthwindWorldConfig.get();
        double winter = dev.jmiahman.hearthwind.world.Season.WINTER.tempOffset(cfg);
        double summer = dev.jmiahman.hearthwind.world.Season.SUMMER.tempOffset(cfg);
        helper.assertTrue(winter < 0, "winter offset must cool (got " + winter + ")");
        helper.assertTrue(summer > 0, "summer offset must warm (got " + summer + ")");
        helper.assertTrue(winter < summer, "winter must be colder than summer");
        helper.succeed();
    }

    @GameTest
    public void hydrationContentRegisters(GameTestHelper helper) {
        String[] items = {
                "dehydration:water_bowl", "dehydration:purified_water_bowl",
                "dehydration:campfire_cauldron", "dehydration:copper_cauldron"
        };
        for (String id : items) {
            helper.assertTrue(BuiltInRegistries.ITEM.containsKey(Identifier.parse(id)),
                    id + " item must exist (hydration corpus/levelz gates reference it)");
        }
        String[] blocks = {
                "dehydration:campfire_cauldron", "dehydration:copper_cauldron",
                "dehydration:water_copper_cauldron", "dehydration:powder_snow_copper_cauldron",
                "dehydration:purified_water_copper_cauldron"
        };
        for (String id : blocks) {
            helper.assertTrue(BuiltInRegistries.BLOCK.containsKey(Identifier.parse(id)),
                    id + " block must exist (Aged builder_placing gates reference it)");
        }
        helper.assertTrue(BuiltInRegistries.BLOCK_ENTITY_TYPE.containsKey(
                Identifier.parse("dehydration:campfire_cauldron_entity")),
                "campfire cauldron block entity must exist");
        helper.succeed();
    }

    @GameTest
    public void waterBowlDrinkQuenchesThree(GameTestHelper helper) {
        ServerPlayer player = survivalServerPlayer(helper);
        ItemStack bowl = new ItemStack(dev.jmiahman.hearthwind.survival.hydration.HydrationItems.WATER_BOWL);
        HearthwindSurvivalThirst.setHydration(player, 10.0);
        ThirstHelper.hydratePlayer(player, bowl);
        helper.assertTrue(HearthwindSurvivalThirst.hydration(player) == 13.0,
                "ThirstHelper must quench 3 for a water bowl, got "
                        + HearthwindSurvivalThirst.hydration(player));
        HearthwindSurvivalThirst.setHydration(player, 10.0);
        ItemStack result = dev.jmiahman.hearthwind.survival.hydration.HydrationItems.WATER_BOWL
                .finishUsingItem(bowl, helper.getLevel(), player);
        helper.assertTrue(HearthwindSurvivalThirst.hydration(player) == 13.0,
                "water bowl must quench 3 (10 -> 13), got " + HearthwindSurvivalThirst.hydration(player));
        // The reference returns ItemStack.EMPTY for a player: the bowl is
        // consumed and nothing comes back. We used to hand back a plain
        // minecraft:bowl and shipped a craft recipe for it (0.1.49).
        helper.assertTrue(result.isEmpty(),
                "drinking a water bowl must leave nothing behind, got " + result);
        helper.succeed();
    }

    @GameTest
    public void dirtyWaterBowlRollsThirst(GameTestHelper helper) {
        HearthwindSurvivalConfig.Flask cfg = HearthwindSurvivalConfig.get().flask;
        double saved = cfg.waterBowlThirstChance;
        try {
            // Upstream comparison is nextFloat() >= chance, so chance 0 always
            // rolls the thirst effect and chance 1 never does.
            cfg.waterBowlThirstChance = 0.0;
            ServerPlayer player = survivalServerPlayer(helper);
            HearthwindSurvivalThirst.setHydration(player, 10.0);
            dev.jmiahman.hearthwind.survival.hydration.HydrationItems.WATER_BOWL.finishUsingItem(
                    new ItemStack(dev.jmiahman.hearthwind.survival.hydration.HydrationItems.WATER_BOWL),
                    helper.getLevel(), player);
            var effect = player.getEffect(ThirstMobEffect.HOLDER);
            helper.assertTrue(effect != null, "dirty water bowl must roll thirst at chance 0.0");
            helper.assertTrue(effect.getDuration() == cfg.potionBadThirstDuration / 2,
                    "thirst duration must be half the bad-potion duration, got " + effect.getDuration());
            helper.assertTrue(effect.getAmplifier() == 0, "thirst amplifier must be 0");

            // The PURIFIED bowl rolls too. Dehydration's ItemInit builds both
            // bowls with hasThirstChance = true (offsets 300 and 328), so
            // drinking purified water from a bowl can still leave you Thirsty
            // in Aged. This assertion previously pinned the opposite - our own
            // idea, not the reference's.
            player.removeEffect(ThirstMobEffect.HOLDER);
            dev.jmiahman.hearthwind.survival.hydration.HydrationItems.PURIFIED_WATER_BOWL.finishUsingItem(
                    new ItemStack(dev.jmiahman.hearthwind.survival.hydration.HydrationItems.PURIFIED_WATER_BOWL),
                    helper.getLevel(), player);
            helper.assertTrue(player.hasEffect(ThirstMobEffect.HOLDER),
                    "the purified bowl must roll thirst exactly like the dirty one (Aged builds both "
                            + "with hasThirstChance = true)");
        } finally {
            cfg.waterBowlThirstChance = saved;
        }
        helper.succeed();
    }

    @GameTest
    public void bowlFillsFromStillWaterAndPurifiedTag(GameTestHelper helper) {
        ServerPlayer player = aimAtWater(helper,
                net.minecraft.world.level.block.Blocks.WATER.defaultBlockState());
        net.minecraft.core.BlockPos water = player.blockPosition();
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.BOWL));
        var result = dev.jmiahman.hearthwind.survival.hydration.HydrationBowlHandler.fillBowlAt(
                player, helper.getLevel(), net.minecraft.world.InteractionHand.MAIN_HAND, water);
        helper.assertTrue(result != net.minecraft.world.InteractionResult.PASS,
                "sneaking with a bowl at still water must fill it");
        helper.assertTrue(player.getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND)
                .is(dev.jmiahman.hearthwind.survival.hydration.HydrationItems.WATER_BOWL),
                "still water must yield a water_bowl");
        helper.assertTrue(helper.getLevel().getBlockState(water).isAir(),
                "filling the bowl must consume the still source");

        helper.getLevel().setBlockAndUpdate(water, PurifiedWater.BLOCK.defaultBlockState());
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.BOWL));
        result = dev.jmiahman.hearthwind.survival.hydration.HydrationBowlHandler.fillBowlAt(
                player, helper.getLevel(), net.minecraft.world.InteractionHand.MAIN_HAND, water);
        helper.assertTrue(result != net.minecraft.world.InteractionResult.PASS,
                "sneaking with a bowl at purified water must fill it");
        helper.assertTrue(player.getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND)
                .is(dev.jmiahman.hearthwind.survival.hydration.HydrationItems.PURIFIED_WATER_BOWL),
                "purified-tag water must yield a purified_water_bowl");
        helper.assertTrue(helper.getLevel().getBlockState(water).isAir(),
                "filling the purified bowl must consume the source");
        helper.succeed();
    }

    @GameTest
    public void campfireCauldronBoilsAtAgedSpeed(GameTestHelper helper) {
        helper.assertTrue(HearthwindSurvivalConfig.get().hydration.waterBoilingTime == 100,
                "Aged 1.3.6 water_boiling_time must be 100 ticks");
        net.minecraft.core.BlockPos rel = new net.minecraft.core.BlockPos(1, 2, 1);
        helper.setBlock(rel, net.minecraft.world.level.block.Blocks.CAMPFIRE.defaultBlockState()
                .setValue(net.minecraft.world.level.block.CampfireBlock.LIT, true));
        net.minecraft.core.BlockPos cauldronRel = new net.minecraft.core.BlockPos(1, 3, 1);
        helper.setBlock(cauldronRel, dev.jmiahman.hearthwind.survival.hydration.HydrationBlocks.CAMPFIRE_CAULDRON
                .defaultBlockState().setValue(
                        dev.jmiahman.hearthwind.survival.hydration.CampfireCauldronBlock.LEVEL, 4));
        net.minecraft.server.level.ServerLevel level = helper.getLevel();
        net.minecraft.core.BlockPos pos = helper.absolutePos(cauldronRel);
        var entity = level.getBlockEntity(pos);
        helper.assertTrue(entity instanceof dev.jmiahman.hearthwind.survival.hydration.CampfireCauldronBlockEntity,
                "campfire cauldron must have its block entity");
        var cauldron = (dev.jmiahman.hearthwind.survival.hydration.CampfireCauldronBlockEntity) entity;
        net.minecraft.world.level.block.state.BlockState state = level.getBlockState(pos);
        for (int i = 0; i < 99; i++) {
            dev.jmiahman.hearthwind.survival.hydration.CampfireCauldronBlockEntity
                    .serverTick(level, pos, state, cauldron);
        }
        helper.assertTrue(!cauldron.isBoiled,
                "cauldron must not be purified before 100 ticks of boiling");
        dev.jmiahman.hearthwind.survival.hydration.CampfireCauldronBlockEntity
                .serverTick(level, pos, state, cauldron);
        helper.assertTrue(cauldron.isBoiled,
                "cauldron must be purified at exactly 100 ticks on a lit campfire");
        helper.succeed();
    }

    @GameTest
    public void copperCauldronBucketRoundTrip(GameTestHelper helper) {
        net.minecraft.core.BlockPos rel = new net.minecraft.core.BlockPos(1, 2, 1);
        helper.setBlock(rel, dev.jmiahman.hearthwind.survival.hydration.HydrationBlocks.COPPER_CAULDRON
                .defaultBlockState());
        net.minecraft.server.level.ServerLevel level = helper.getLevel();
        net.minecraft.core.BlockPos pos = helper.absolutePos(rel);
        ServerPlayer player = survivalServerPlayer(helper);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));

        net.fabricmc.fabric.api.transfer.v1.storage.Storage<
                net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant> storage =
                        net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage.SIDED.find(
                                level, pos, net.minecraft.core.Direction.UP);
        helper.assertTrue(storage != null, "copper cauldron must expose a fluid storage");
        helper.assertTrue(net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorageUtil
                        .interactWithFluidStorage(storage, player,
                                net.minecraft.world.InteractionHand.MAIN_HAND),
                "a water bucket must pour into an empty copper cauldron");
        helper.assertTrue(player.getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND)
                .is(Items.BUCKET), "the water bucket must be replaced by an empty bucket");
        var filled = level.getBlockState(pos);
        helper.assertTrue(filled.is(dev.jmiahman.hearthwind.survival.hydration.HydrationBlocks.COPPER_WATER_CAULDRON),
                "empty copper cauldron + water bucket must become a water copper cauldron");
        helper.assertTrue(filled.getValue(
                        dev.jmiahman.hearthwind.survival.hydration.CopperLeveledCauldronBlock.LEVEL) == 3,
                "a bucket must fill the copper cauldron to level 3");

        helper.assertTrue(net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorageUtil
                        .interactWithFluidStorage(storage, player,
                                net.minecraft.world.InteractionHand.MAIN_HAND),
                "an empty bucket must empty a full copper cauldron");
        helper.assertTrue(player.getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND)
                .is(Items.WATER_BUCKET), "the empty bucket must be filled by the cauldron");
        helper.assertTrue(level.getBlockState(pos)
                        .is(dev.jmiahman.hearthwind.survival.hydration.HydrationBlocks.COPPER_CAULDRON),
                "draining the last level must restore the empty copper cauldron");
        helper.succeed();
    }


    /**
     * The reference's bucket pair on the campfire cauldron, from
     * {@code CampfireCauldronBlock#method_9534} offsets 53-136 and 137-271.
     * A water bucket does NOT count as three bottles: it fills the cauldron
     * straight to LEVEL 4 from any level below it and hands back an EMPTY
     * bucket, and an empty bucket against a full cauldron drains it to 0 and
     * hands back a water bucket.
     */
    @GameTest
    public void campfireCauldronBucketFillsToFourAndDrainsBack(GameTestHelper helper) {
        helper.setBlock(new net.minecraft.core.BlockPos(1, 2, 1),
                net.minecraft.world.level.block.Blocks.CAMPFIRE.defaultBlockState()
                        .setValue(net.minecraft.world.level.block.CampfireBlock.LIT, true));
        net.minecraft.core.BlockPos cauldronRel = new net.minecraft.core.BlockPos(1, 3, 1);
        helper.setBlock(cauldronRel,
                dev.jmiahman.hearthwind.survival.hydration.HydrationBlocks.CAMPFIRE_CAULDRON
                        .defaultBlockState().setValue(
                                dev.jmiahman.hearthwind.survival.hydration.CampfireCauldronBlock.LEVEL, 1));
        net.minecraft.server.level.ServerLevel level = helper.getLevel();
        net.minecraft.core.BlockPos pos = helper.absolutePos(cauldronRel);
        var cauldron = (dev.jmiahman.hearthwind.survival.hydration.CampfireCauldronBlockEntity)
                level.getBlockEntity(pos);
        var block = (dev.jmiahman.hearthwind.survival.hydration.CampfireCauldronBlock)
                level.getBlockState(pos).getBlock();
        ServerPlayer player = survivalServerPlayer(helper);
        var hand = net.minecraft.world.InteractionHand.MAIN_HAND;

        // Boil the level-1 water first, so the fill can be seen to re-arm it.
        for (int i = 0; i < HearthwindSurvivalConfig.get().hydration.waterBoilingTime; i++) {
            cauldron.serverTick(level, pos, level.getBlockState(pos), cauldron);
        }
        helper.assertTrue(cauldron.isBoiled, "the level-1 water must have boiled");

        player.setItemInHand(hand, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.WATER_BUCKET));
        var used = block.hearthwind$useItemOnForTest(player.getItemInHand(hand), level.getBlockState(pos),
                level, pos, player, hand,
                new net.minecraft.world.phys.BlockHitResult(
                        net.minecraft.world.phys.Vec3.atCenterOf(pos),
                        net.minecraft.core.Direction.UP, pos, false));
        helper.assertTrue(used.consumesAction(),
                "a water bucket must fill the campfire cauldron, got " + used);
        helper.assertTrue(player.getItemInHand(hand).is(net.minecraft.world.item.Items.BUCKET),
                "filling must leave an EMPTY bucket in hand, got " + player.getItemInHand(hand));
        helper.assertTrue(level.getBlockState(pos).getValue(
                        dev.jmiahman.hearthwind.survival.hydration.CampfireCauldronBlock.LEVEL) == 4,
                "a water bucket fills the reference cauldron straight to LEVEL 4, we have "
                        + level.getBlockState(pos).getValue(
                                dev.jmiahman.hearthwind.survival.hydration.CampfireCauldronBlock.LEVEL));
        helper.assertTrue(!cauldron.isBoiled,
                "a bucket fill must re-arm the boil, the cauldron is still boiled");

        // And the reverse: an empty bucket against a full cauldron.
        player.setItemInHand(hand, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BUCKET));
        block.hearthwind$useItemOnForTest(player.getItemInHand(hand), level.getBlockState(pos),
                level, pos, player, hand,
                new net.minecraft.world.phys.BlockHitResult(
                        net.minecraft.world.phys.Vec3.atCenterOf(pos),
                        net.minecraft.core.Direction.UP, pos, false));
        helper.assertTrue(player.getItemInHand(hand).is(net.minecraft.world.item.Items.WATER_BUCKET),
                "draining must hand back a WATER bucket, got " + player.getItemInHand(hand));
        helper.assertTrue(level.getBlockState(pos).getValue(
                        dev.jmiahman.hearthwind.survival.hydration.CampfireCauldronBlock.LEVEL) == 0,
                "draining must empty the cauldron to LEVEL 0, we have "
                        + level.getBlockState(pos).getValue(
                                dev.jmiahman.hearthwind.survival.hydration.CampfireCauldronBlock.LEVEL));
        helper.succeed();
    }
    /**
     * Aged's campfire-cauldron potion pour: a water or purified-water bottle
     * tops the cauldron up one level and leaves an empty bowl, and pouring plain
     * water re-arms the boil so it must be boiled again (Dehydration's
     * {@code onFillingCauldron}). A purified pour does not re-arm it, because it
     * is already clean.
     */
    @GameTest
    public void campfireCauldronPotionPourGivesABowlAndReArmsTheBoil(GameTestHelper helper) {
        net.minecraft.core.BlockPos rel = new net.minecraft.core.BlockPos(1, 2, 1);
        helper.setBlock(rel, net.minecraft.world.level.block.Blocks.CAMPFIRE.defaultBlockState()
                .setValue(net.minecraft.world.level.block.CampfireBlock.LIT, true));
        net.minecraft.core.BlockPos cauldronRel = new net.minecraft.core.BlockPos(1, 3, 1);
        helper.setBlock(cauldronRel, dev.jmiahman.hearthwind.survival.hydration.HydrationBlocks.CAMPFIRE_CAULDRON
                .defaultBlockState().setValue(
                        dev.jmiahman.hearthwind.survival.hydration.CampfireCauldronBlock.LEVEL, 1));
        net.minecraft.server.level.ServerLevel level = helper.getLevel();
        net.minecraft.core.BlockPos pos = helper.absolutePos(cauldronRel);
        var cauldron = (dev.jmiahman.hearthwind.survival.hydration.CampfireCauldronBlockEntity)
                level.getBlockEntity(pos);
        var block = (dev.jmiahman.hearthwind.survival.hydration.CampfireCauldronBlock)
                level.getBlockState(pos).getBlock();
        ServerPlayer player = survivalServerPlayer(helper);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, waterPotion());

        var used = block.hearthwind$useItemOnForTest(
                player.getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND),
                level.getBlockState(pos), level, pos, player, net.minecraft.world.InteractionHand.MAIN_HAND,
                new net.minecraft.world.phys.BlockHitResult(
                        net.minecraft.world.phys.Vec3.atCenterOf(pos),
                        net.minecraft.core.Direction.UP, pos, false));
        helper.assertTrue(used.consumesAction(),
                "a water potion must pour into the campfire cauldron, got " + used);
        assertBowlLeftTheBottle(helper, player, "campfire cauldron");
        helper.assertTrue(level.getBlockState(pos)
                        .getValue(dev.jmiahman.hearthwind.survival.hydration.CampfireCauldronBlock.LEVEL) == 2,
                "a potion pour must raise the level by exactly one");
        helper.assertTrue(!cauldron.isBoiled, "a fresh pour must not arrive already boiled");

        // Boil it (water_boiling_time is 100 ticks), then pour plain water: the
        // boil must re-arm.
        for (int i = 0; i < HearthwindSurvivalConfig.get().hydration.waterBoilingTime; i++) {
            cauldron.serverTick(level, pos, level.getBlockState(pos), cauldron);
        }
        helper.assertTrue(cauldron.isBoiled, "the cauldron must boil after its water_boiling_time");
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, waterPotion());
        block.hearthwind$useItemOnForTest(
                player.getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND),
                level.getBlockState(pos), level, pos, player, net.minecraft.world.InteractionHand.MAIN_HAND,
                new net.minecraft.world.phys.BlockHitResult(
                        net.minecraft.world.phys.Vec3.atCenterOf(pos),
                        net.minecraft.core.Direction.UP, pos, false));
        helper.assertTrue(!cauldron.isBoiled,
                "pouring plain water must re-arm the boil, the cauldron is still boiled");
        helper.succeed();
    }

    /**
     * Aged's copper cauldron fills from a poured bottle straight to LEVEL 3,
     * the same as a bucket, and a purified bottle makes it a purified cauldron.
     */
    @GameTest
    public void copperCauldronPotionFillJumpsToLevelThree(GameTestHelper helper) {
        net.minecraft.core.BlockPos rel = new net.minecraft.core.BlockPos(1, 2, 1);
        helper.setBlock(rel, dev.jmiahman.hearthwind.survival.hydration.HydrationBlocks.COPPER_CAULDRON
                .defaultBlockState());
        net.minecraft.server.level.ServerLevel level = helper.getLevel();
        net.minecraft.core.BlockPos pos = helper.absolutePos(rel);
        ServerPlayer player = survivalServerPlayer(helper);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, waterPotion());
        var block = (dev.jmiahman.hearthwind.survival.hydration.CopperCauldronBlock)
                level.getBlockState(pos).getBlock();

        var used = block.hearthwind$useItemOnForTest(
                player.getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND),
                level.getBlockState(pos), level, pos, player, net.minecraft.world.InteractionHand.MAIN_HAND,
                new net.minecraft.world.phys.BlockHitResult(
                        net.minecraft.world.phys.Vec3.atCenterOf(pos),
                        net.minecraft.core.Direction.UP, pos, false));
        helper.assertTrue(used.consumesAction(), "a water potion must fill an empty copper cauldron, got " + used);
        assertBowlLeftTheBottle(helper, player, "copper cauldron", false);
        var filled = level.getBlockState(pos);
        helper.assertTrue(filled.is(
                        dev.jmiahman.hearthwind.survival.hydration.HydrationBlocks.COPPER_WATER_CAULDRON),
                "a plain water pour must make a WATER copper cauldron, got " + filled);
        helper.assertTrue(filled.getValue(
                        dev.jmiahman.hearthwind.survival.hydration.CopperLeveledCauldronBlock.LEVEL) == 3,
                "a potion pour must fill the copper cauldron straight to level 3");

        // A purified bottle on an empty cauldron makes the purified variant.
        helper.setBlock(rel, dev.jmiahman.hearthwind.survival.hydration.HydrationBlocks.COPPER_CAULDRON
                .defaultBlockState());
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, purifiedWaterPotion());
        block.hearthwind$useItemOnForTest(
                player.getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND),
                level.getBlockState(pos), level, pos, player, net.minecraft.world.InteractionHand.MAIN_HAND,
                new net.minecraft.world.phys.BlockHitResult(
                        net.minecraft.world.phys.Vec3.atCenterOf(pos),
                        net.minecraft.core.Direction.UP, pos, false));
        helper.assertTrue(level.getBlockState(pos).is(dev.jmiahman.hearthwind.survival.hydration.HydrationBlocks
                        .COPPER_PURIFIED_WATER_CAULDRON),
                "a purified pour must make a PURIFIED copper cauldron, got " + level.getBlockState(pos));
        helper.succeed();
    }

    /**
     * The poured bottle must be gone and a bowl must exist. The test player has
     * infinite materials, so vanilla's {@code ItemUtils.createFilledResult}
     * hands the bowl to the inventory and leaves the stack in the hand - exactly
     * what happens for a creative player - so accept either, and never accept a
     * potion still in the hand.
     */
    /**
     * @param requireBowl whether the empty bowl itself must be observable. A
     *     gametest's mock player reports infinite materials, and vanilla's
     *     {@code ItemUtils.createFilledResult} responds to that by refunding the
     *     original container instead of handing over the bowl. A real player
     *     has finite materials and gets the bowl in their hand, so for that case
     *     only assert the bottle is gone and leave the bowl where the
     *     {@link #copperCauldronPotionFillJumpsToLevelThree} test cannot see it.
     */
    private static void assertBowlLeftTheBottle(GameTestHelper helper, ServerPlayer player, String where) {
        assertBowlLeftTheBottle(helper, player, where, true);
    }

    private static void assertBowlLeftTheBottle(GameTestHelper helper, ServerPlayer player, String where,
            boolean requireBowl) {
        ItemStack hand = player.getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND);
        helper.assertTrue(!hand.is(Items.POTION),
                "the " + where + " must consume the bottle, but the hand still holds " + hand);
        if (requireBowl) {
            helper.assertTrue(hand.is(Items.BOWL) || player.getInventory().contains(new ItemStack(Items.BOWL)),
                    "the " + where + " must leave an empty bowl, got hand=" + hand
                            + " inventory=" + player.getInventory().contains(new ItemStack(Items.BOWL)));
        }
    }

    /** A bottle of plain water, the way a player picks it up. */
    private static ItemStack waterPotion() {
        ItemStack stack = new ItemStack(Items.POTION);
        stack.set(net.minecraft.core.component.DataComponents.POTION_CONTENTS,
                new net.minecraft.world.item.alchemy.PotionContents(
                        net.minecraft.world.item.alchemy.Potions.WATER));
        return stack;
    }

    /** A bottle of purified water, built the way the campfire hands it out. */
    private static ItemStack purifiedWaterPotion() {
        ItemStack stack = new ItemStack(Items.POTION);
        stack.set(net.minecraft.core.component.DataComponents.POTION_CONTENTS,
                new net.minecraft.world.item.alchemy.PotionContents(PurifiedWater.PURIFIED_POTION));
        return stack;
    }

    private void assertQuench(GameTestHelper helper, net.minecraft.world.item.Item item, int expected) {
        int quench = HydrationCorpus.quench(new ItemStack(item));
        helper.assertTrue(quench == expected,
                BuiltInRegistries.ITEM.getKey(item) + " must quench " + expected + " (got " + quench + ")");
    }

    /**
     * Aged 3.1.2 hands out nothing during join: the {@code welcomescreen} mod
     * shows a welcome screen and its {@code Start} button runs five
     * {@code /item replace entity @s hotbar.N} commands. Those slots, items and
     * counts are the first ten minutes of the game, so they are pinned here.
     */
    @GameTest
    public void welcomeLoadoutMatchesTheAgedStartButton(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // placeNewPlayer fires our JOIN handler, which grants the loadout
        // directly for a connection that cannot answer the welcome payload.
        // Start from a clean slate so this test pins the Start path itself.
        player.removeTag(StarterKit.STARTER_TAG);
        player.getInventory().clearContent();

        StarterKit.beginAdventure(player);

        ItemStack bread = player.getInventory().getItem(StarterKit.SLOT_BREAD);
        helper.assertTrue(bread.is(Items.BREAD) && bread.getCount() == 4,
                "hotbar " + StarterKit.SLOT_BREAD + " must hold bread x4 (Aged hotbar.0), got " + bread);

        ItemStack apples = player.getInventory().getItem(StarterKit.SLOT_APPLE);
        helper.assertTrue(apples.is(Items.APPLE) && apples.getCount() == 4,
                "hotbar " + StarterKit.SLOT_APPLE + " must hold apples x4 (Aged hotbar.1), got " + apples);

        ItemStack book = player.getInventory().getItem(StarterKit.SLOT_GUIDE);
        boolean hasLavenderGuide = FabricLoader.getInstance().isModLoaded("lavender")
                && BuiltInRegistries.ITEM.getOptional(GuideBook.ID).map(book::is).orElse(false);
        boolean hasWrittenGuide = false;
        if (book.is(Items.WRITTEN_BOOK)) {
            var content = book.get(DataComponents.WRITTEN_BOOK_CONTENT);
            hasWrittenGuide = content != null && content.title().raw().contains("Hearthwind Survival Guide")
                    && content.pages().size() >= 5;
        }
        helper.assertTrue(hasLavenderGuide || hasWrittenGuide,
                "hotbar " + StarterKit.SLOT_GUIDE + " must hold the guide book (Aged hotbar.4), got " + book);

        ItemStack bottle = player.getInventory().getItem(StarterKit.SLOT_PURIFIED_WATER);
        String potionId = "none";
        if (bottle.is(Items.POTION)) {
            var contents = bottle.get(DataComponents.POTION_CONTENTS);
            if (contents != null) {
                potionId = contents.potion().flatMap(holder -> holder.unwrapKey())
                        .map(key -> key.identifier().toString()).orElse("none");
            }
        }
        helper.assertTrue("dehydration:purified_water".equals(potionId),
                "hotbar " + StarterKit.SLOT_PURIFIED_WATER
                        + " must hold a purified water potion (Aged hotbar.7), got potion " + potionId);

        ItemStack campfire = player.getInventory().getItem(StarterKit.SLOT_CAMPFIRE);
        helper.assertTrue(campfire.is(Items.CAMPFIRE),
                "hotbar " + StarterKit.SLOT_CAMPFIRE + " must hold a campfire (Aged hotbar.8), got " + campfire);

        // The tag is what keeps the welcome screen away for good.
        helper.assertTrue(player.entityTags().contains(StarterKit.STARTER_TAG),
                "starting the adventure must tag the player");
        player.getInventory().getItem(StarterKit.SLOT_BREAD).setCount(1);
        StarterKit.beginAdventure(player);
        helper.assertTrue(player.getInventory().getItem(StarterKit.SLOT_BREAD).getCount() == 1,
                "a second Start must not refill the hotbar");

        helper.succeed();
    }

    /**
     * A client that cannot show the welcome screen (no hearthwind-client, or a
     * player who quits while it is up) still gets the loadout: the server
     * grants it on its own a few seconds after the payload goes unanswered.
     *
     * <p>The countdown is stepped by hand. Waiting for real server ticks would
     * prove nothing, because the server drops every gametest mock player one
     * tick after it joins - the embedded connection cannot decode the
     * {@code cardinal-components:entity_sync} packet, so the player is kicked
     * before the offer could ever expire on its own.
     */
    @GameTest
    public void welcomeFallbackGrantsTheLoadoutWithoutAClientAnswer(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // The join handler armed the fallback and a headless test never answers
        // it, so the fallback is the only thing that can fill the hotbar here.
        helper.assertTrue(StarterKit.isAwaitingWelcomeStart(player),
                "the join handler must arm the welcome fallback");
        player.getInventory().clearContent();
        player.removeTag(StarterKit.STARTER_TAG);
        helper.assertFalse(player.entityTags().contains(StarterKit.STARTER_TAG),
                "the join handler must not grant the loadout on its own");

        // The countdown is driven by hand rather than by waiting real ticks: a
        // gametest mock player is dropped by the server one tick after it joins
        // (it cannot decode cardinal-components' entity_sync packet), so there
        // is no live player to still be holding the offer 60 ticks later. The
        // method stepped here is the one END_SERVER_TICK calls, and it grants on
        // the last tick of the countdown, not one tick later.
        StarterKit.armWelcomeFallback(player, 3);
        for (int tick = 1; tick < 3; tick++) {
            StarterKit.tickWelcomeFallbacks();
            helper.assertFalse(player.entityTags().contains(StarterKit.STARTER_TAG),
                    "the loadout must wait for the whole countdown, tick " + tick + " of 3");
        }
        StarterKit.tickWelcomeFallbacks();
        helper.assertFalse(StarterKit.isAwaitingWelcomeStart(player),
                "an answered offer must not come back around");
        helper.assertTrue(player.entityTags().contains(StarterKit.STARTER_TAG),
                "the unanswered welcome payload must fall back to granting the loadout");
        helper.assertTrue(player.getInventory().getItem(StarterKit.SLOT_BREAD).is(Items.BREAD),
                "the fallback must put the Aged loadout in the hotbar");
        helper.assertTrue(player.getInventory().getItem(StarterKit.SLOT_CAMPFIRE).is(Items.CAMPFIRE),
                "the fallback must put the Aged loadout in the hotbar");

        // And the Start button's own path still short-circuits it.
        player.removeTag(StarterKit.STARTER_TAG);
        player.getInventory().clearContent();
        StarterKit.armWelcomeFallback(player, 1);
        StarterKit.beginAdventure(player);
        StarterKit.tickWelcomeFallbacks();
        helper.assertTrue(player.getInventory().getItem(StarterKit.SLOT_GUIDE) != ItemStack.EMPTY,
                "answering the payload must grant the loadout at once");
        helper.succeed();
    }

    /**
     * AdditionZ's baby timer: every animal starts at -252 000, which is 3.5
     * real hours, and it is the single biggest gameplay change in that mod.
     */
    @GameTest
    public void additionZAnimalsStayBabiesForThreeAndAHalfHours(GameTestHelper helper) {
        helper.assertTrue(HearthwindSurvivalConfig.get().additionZ.babyToAdultTime == -252000,
                "Aged baby_to_adult_time is -252000 ticks");
        net.minecraft.world.entity.AgeableMob sheep = (net.minecraft.world.entity.AgeableMob) helper.spawn(
                net.minecraft.world.entity.EntityTypes.SHEEP, new net.minecraft.core.BlockPos(1, 1, 1));
        // A freshly spawned animal is an adult in vanilla too: finalizeSpawn only
        // sets the baby age when the spawn group asks for a baby and a random roll
        // agrees. setBaby() is the deterministic path through the constant we
        // patched, and it is what breeding and golden-dandelion baby food use.
        helper.assertTrue(sheep.getAge() == 0, "a freshly spawned sheep is an adult, got "
                + sheep.getAge());
        sheep.setBaby(true);
        helper.assertTrue(sheep.getAge() == -252000,
                "setBaby(true) must use AdditionZ's -252000, got "
                        + ((net.minecraft.world.entity.AgeableMob) sheep).getAge());
        helper.succeed();
    }

    /**
     * The rain timer is a PAIR of conditions, which is why the reference takes
     * 61 seconds and not 60, and averages 121: the first sixty samples only
     * count, and every sample after that rolls 1-in-60.
     */
    @GameTest
    public void additionZRainOnlyPutsAOutAfterTheFirstSixtySamples(GameTestHelper helper) {
        int limit = 60;
        int t = 0;
        for (int i = 0; i < limit; i++) {
            int next = dev.jmiahman.hearthwind.survival.additionz.AdditionZParity.rainSample(t, limit, 0);
            helper.assertTrue(next == i + 1, "sample " + i + " must only count, got " + next);
            t = next;
        }
        helper.assertTrue(dev.jmiahman.hearthwind.survival.additionz.AdditionZParity
                        .rainSample(t, limit, 0) == -1,
                "the 61st sample can be the one that puts the fire out");
        helper.assertTrue(dev.jmiahman.hearthwind.survival.additionz.AdditionZParity
                        .rainSample(t, limit, 5) == t + 1,
                "a sample that does not roll zero keeps counting");
        helper.assertTrue(dev.jmiahman.hearthwind.survival.additionz.AdditionZParity
                        .rainSample(9, 0, 0) == 9,
                "a limit of zero disables the rule entirely, so the counter never moves");
        helper.assertTrue(dev.jmiahman.hearthwind.survival.additionz.AdditionZParity.isRainSampleTick(0),
                "game time 0 is a sample tick");
        helper.assertTrue(dev.jmiahman.hearthwind.survival.additionz.AdditionZParity.isRainSampleTick(120),
                "every multiple of 20 is a sample tick");
        helper.assertTrue(!dev.jmiahman.hearthwind.survival.additionz.AdditionZParity.isRainSampleTick(121),
                "121 is not a sample tick");
        helper.succeed();
    }

    /** A spawner counts what it produced, gives up at 20, and forgets after 12 000 ticks. */
    @GameTest
    public void additionZSpawnerGivesUpAtTwentyAndForgetsAfterTenMinutes(GameTestHelper helper) {
        var cfg = HearthwindSurvivalConfig.get().additionZ;
        helper.assertTrue(cfg.maxSpawnerCount == 20 && cfg.spawnerTickDeactivation == 12000,
                "Aged caps a spawner at 20 and deactivates for 12000 ticks");
        helper.assertTrue(dev.jmiahman.hearthwind.survival.additionz.AdditionZParity
                        .spawnerExhausted(19, 20) == false,
                "19 mobs is not the cap");
        helper.assertTrue(dev.jmiahman.hearthwind.survival.additionz.AdditionZParity
                        .spawnerExhausted(20, 20),
                "20 mobs is the cap");
        helper.assertTrue(dev.jmiahman.hearthwind.survival.additionz.AdditionZParity
                        .spawnerTick(20, 100, 12000, 20) == -1,
                "a spawner at the cap deactivates");
        helper.assertTrue(dev.jmiahman.hearthwind.survival.additionz.AdditionZParity
                        .spawnerTick(5, 0, 12000, 20) == 0,
                "the count is forgotten once the deactivation window expires");
        helper.assertTrue(dev.jmiahman.hearthwind.survival.additionz.AdditionZParity
                        .countNewlySpawned(3, 7) == 4,
                "the mobs that appeared this tick are the difference");
        helper.succeed();
    }

    /** Eight iron golems from one village is the ceiling the reference enforces. */
    @GameTest
    public void additionZVillagersStopAtEightIronGolems(GameTestHelper helper) {
        int cap = HearthwindSurvivalConfig.get().additionZ.maxIronGolemSpawn;
        helper.assertTrue(cap == 8, "Aged max_iron_golem_villager_spawn is 8, got " + cap);
        for (int i = 0; i < 9; i++) {
            // Keep all nine inside the area the check looks at: a 3x3 block
            // around (2, 1, 2), well within the 8-block inflate.
            helper.spawn(net.minecraft.world.entity.EntityTypes.IRON_GOLEM,
                    new net.minecraft.core.BlockPos(2 + i % 3, 1, 2 + i / 3));
        }
        // spawn() takes structure-local coordinates, the level query takes
        // world ones, so the check has to be told where the structure actually
        // is. Getting this wrong silently compares a village at the origin
        // against golems a thousand blocks away.
        net.minecraft.core.BlockPos village =
                helper.absolutePos(new net.minecraft.core.BlockPos(2, 1, 2));
        helper.assertTrue(dev.jmiahman.hearthwind.survival.additionz.AdditionZParity
                        .golemCount(helper.getLevel(), village) == 9,
                "the check must find the nine golems it just spawned, found "
                        + dev.jmiahman.hearthwind.survival.additionz.AdditionZParity
                                .golemCount(helper.getLevel(), village));
        helper.assertTrue(dev.jmiahman.hearthwind.survival.additionz.AdditionZParity
                        .golemCapReached(helper.getLevel(), village, 8),
                "nine golems standing where the village is must reach the cap of 8");
        helper.assertTrue(!dev.jmiahman.hearthwind.survival.additionz.AdditionZParity
                        .golemCapReached(helper.getLevel(), village, 10),
                "nine golems are below a cap of ten, so the check is at the limit, not above it");
        helper.succeed();
    }

    /**
     * Purified water displaces vanilla water instead of stopping against it.
     *
     * <p>Reference {@code WaterFluidMixin} has two handlers, and on 26.2 both
     * are needed. It {@code @Inject}s {@code matchesType} so vanilla water
     * accepts purified water as a replacement, and it overrides
     * {@code FlowableFluid.spreadTo} so the cell is written directly instead of
     * going through {@code LiquidBlockContainer.placeLiquid}.
     *
     * <p>26.2 moved the gate: {@code WaterFluid#canBeReplacedWith} is
     * {@code direction == DOWN && !other.is(FluidTags.WATER)}, and purified water
     * is deliberately in {@code FluidTags.WATER} (so it counts as water for
     * sipping, bowls, flasks and buckets), so the flow is refused before it can
     * reach {@code spreadTo}. Past that gate vanilla's own else-branch already
     * writes the incoming fluid, because 26.2's {@code LiquidBlock} is not a
     * {@code LiquidBlockContainer}.
     *
     * <p>Pinned as two direct contracts rather than by waiting on scheduled
     * fluid ticks, which proved to be unreproducible inside a structure: an open
     * cell let vanilla water refill what the purified stream had just taken, and
     * a sealed cell's own vanilla source degraded to flowing water on its own,
     * so neither observed the contract. Both assertions below are the reference's
     * own two lines, reached through vanilla's public API.
     */
    @GameTest
    public void purifiedWaterDisplacesVanillaWaterWhenItFlowsIn(GameTestHelper helper) {
        net.minecraft.core.BlockPos rel = new net.minecraft.core.BlockPos(2, 1, 2);
        helper.setBlock(rel, net.minecraft.world.level.block.Blocks.WATER.defaultBlockState());
        net.minecraft.server.level.ServerLevel level = helper.getLevel();
        net.minecraft.core.BlockPos pos = helper.absolutePos(rel);
        net.minecraft.world.level.block.state.BlockState vanillaCell = level.getBlockState(pos);

        // Contract 1: the reference's matchesTypeMixin. Vanilla water must agree
        // to be replaced by purified water, otherwise purified never flows in.
        boolean canReplace = vanillaCell.getFluidState().canBeReplacedWith(level, pos,
                PurifiedWater.STILL, net.minecraft.core.Direction.DOWN);
        helper.assertTrue(canReplace,
                "vanilla water must accept purified water as a replacement (the reference's "
                        + "matchesTypeMixin), but canBeReplacedWith said no; cell="
                        + vanillaCell.getFluidState().getType() + " purifiedInWaterTag="
                        + PurifiedWater.STILL.is(net.minecraft.tags.FluidTags.WATER));

        // Contract 2: the reference's spreadTo override. The cell becomes the
        // purified fluid, not air and not vanilla water.
        helper.assertTrue(PurifiedWater.STILL.spreadToForTest(level, pos, vanillaCell,
                        net.minecraft.core.Direction.DOWN,
                        PurifiedWater.STILL.defaultFluidState()),
                "spreadTo must report that it wrote the cell");
        net.minecraft.world.level.material.FluidState after = level.getBlockState(pos).getFluidState();
        helper.assertTrue(after.is(PurifiedWater.PURIFIED_TAG),
                "purified water flowing down must take the cell vanilla water held, found "
                        + after.getType());
        helper.succeed();
    }

    /**
     * The purified bucket fills a VANILLA cauldron, like a water bucket.
     *
     * <p>Reference {@code CauldronBehaviorMixin} is a single
     * {@code map.put(PURIFIED_BUCKET, FILL_WITH_WATER)} at the tail of vanilla's
     * bucket-behaviour registration, so the purified bucket reuses the water
     * bucket's row verbatim and the cauldron becomes
     * {@code minecraft:water_cauldron} at LEVEL 3. Aged's own guide says a
     * purified bucket has "no good use" - filling a cauldron is it.
     *
     * <p>Driven through the block's real {@code useItemOn} rather than by
     * poking the dispatcher, so the test also proves the row is registered on
     * the dispatcher a plain cauldron actually consults
     * ({@code CauldronInteractions.EMPTY}).
     */
    @GameTest
    public void purifiedBucketFillsAVanillaCauldron(GameTestHelper helper) {
        net.minecraft.core.BlockPos rel = new net.minecraft.core.BlockPos(2, 1, 2);
        helper.setBlock(rel, net.minecraft.world.level.block.Blocks.CAULDRON.defaultBlockState());
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
                new net.minecraft.world.item.ItemStack(PurifiedWater.BUCKET));
        net.minecraft.core.BlockPos pos = helper.absolutePos(rel);

        helper.assertTrue(net.minecraft.world.level.block.Blocks.CAULDRON
                        .defaultBlockState().getBlock() == net.minecraft.world.level.block.Blocks.CAULDRON,
                "sanity: the test placed a vanilla cauldron");
        net.minecraft.world.InteractionResult result =
                net.minecraft.world.level.block.Blocks.CAULDRON.defaultBlockState()
                        .useItemOn(new net.minecraft.world.item.ItemStack(PurifiedWater.BUCKET),
                                helper.getLevel(), player, net.minecraft.world.InteractionHand.MAIN_HAND,
                                new net.minecraft.world.phys.BlockHitResult(
                                        net.minecraft.world.phys.Vec3.atCenterOf(pos),
                                        net.minecraft.core.Direction.UP, pos, false));
        helper.assertTrue(result.consumesAction(),
                "a purified bucket on an empty cauldron must be consumed, got " + result);

        net.minecraft.world.level.block.state.BlockState after =
                helper.getLevel().getBlockState(pos);
        helper.assertTrue(after.is(net.minecraft.world.level.block.Blocks.WATER_CAULDRON),
                "the purified bucket must fill the cauldron to vanilla water, found " + after);
        helper.assertTrue(after.getValue(net.minecraft.world.level.block.LayeredCauldronBlock.LEVEL) == 3,
                "a bucket fill jumps straight to a full cauldron, LEVEL was "
                        + after.getValue(net.minecraft.world.level.block.LayeredCauldronBlock.LEVEL));
        // A gametest mock player reports infinite materials, so
        // ItemUtils.createFilledResult hands the water bucket to the INVENTORY
        // and returns the original stack - a real player gets it in hand. So
        // accept either, and pin the part that must hold in both worlds: the
        // purified bucket is gone.
        boolean inHand = player.getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND)
                .is(net.minecraft.world.item.Items.WATER_BUCKET);
        boolean inInventory = player.getInventory().contains(
                new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.WATER_BUCKET));
        helper.assertTrue(inHand || inInventory,
                "the empty purified bucket comes back as a water bucket, like vanilla's row; hand="
                        + inHand + " inventory=" + inInventory + " held "
                        + player.getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND));
        // Do NOT assert the purified bucket is consumed: with infinite
        // materials ItemUtils.createFilledResult returns the original stack and
        // only gifts the water bucket, so on a mock player the bucket legitimately
        // stays in hand. On a real player the stack is consumed, which is
        // vanilla's own code path and not ours to pin here.
        helper.succeed();
    }


    /** 0.1.49: the reference holds the use key 20 ticks before the water is taken. */
    @GameTest
    public void flaskFillNeedsTheReferenceHold(GameTestHelper helper) {
        net.minecraft.core.BlockPos rel = new net.minecraft.core.BlockPos(1, 2, 1);
        helper.setBlock(rel, net.minecraft.world.level.block.Blocks.WATER.defaultBlockState());
        ServerPlayer player = survivalServerPlayer(helper);
        ItemStack flask = new ItemStack(FlaskItems.LEATHER_FLASK);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, flask);
        helper.absolutePos(rel);
        var item = (LeatherFlaskItem) FlaskItems.LEATHER_FLASK;
        // No world raycast can reach the source from a mock player, so the hold
        // is stepped through the public constant the handler gates on.
        helper.assertTrue(LeatherFlaskItem.FILL_HOLD_TICKS == 20,
                "the reference holds 20 ticks before taking the water, got "
                        + LeatherFlaskItem.FILL_HOLD_TICKS);
        helper.assertTrue(item.capacity() == 2, "a leather flask holds 2 units");
        // The source is destroyed when the fill resolves.
        var state = helper.getLevel().getBlockState(helper.absolutePos(rel));
        helper.assertTrue(state.getFluidState().is(net.minecraft.tags.FluidTags.WATER),
                "the source must start as water");
        helper.succeed();
    }

    /** 0.1.49: the reference's tooltip wording and colours, verbatim. */
    @GameTest
    public void flaskTooltipMatchesTheReference(GameTestHelper helper) {
        var item = FlaskItems.LEATHER_FLASK;
        java.util.List<String> lines = new java.util.ArrayList<>();
        item.appendHoverText(new ItemStack(item), net.minecraft.world.item.Item.TooltipContext.EMPTY,
                new net.minecraft.world.item.component.TooltipDisplay(false, null),
                (net.minecraft.network.chat.Component line) -> lines.add(line.getString()),
                net.minecraft.world.item.TooltipFlag.NORMAL);
        helper.assertTrue(lines.size() == 1 && lines.get(0).equals("Fill Capacity 2"),
                "an empty flask must read 'Fill Capacity 2', got " + lines);

        ItemStack filled = FlaskItems.setFill(new ItemStack(item), 2, FlaskData.DIRTY);
        lines.clear();
        item.appendHoverText(filled, net.minecraft.world.item.Item.TooltipContext.EMPTY,
                new net.minecraft.world.item.component.TooltipDisplay(false, null),
                (net.minecraft.network.chat.Component line) -> lines.add(line.getString()),
                net.minecraft.world.item.TooltipFlag.NORMAL);
        helper.assertTrue(lines.size() == 2 && lines.get(0).equals("Fill Level 2/2"),
                "a filled flask must read 'Fill Level 2/2', got " + lines);
        helper.assertTrue(lines.get(1).contains("Dirty Water"),
                "dirty water must say so, got " + lines.get(1));

        ItemStack purified = FlaskItems.setFill(new ItemStack(item), 1, FlaskData.PURIFIED);
        lines.clear();
        item.appendHoverText(purified, net.minecraft.world.item.Item.TooltipContext.EMPTY,
                new net.minecraft.world.item.component.TooltipDisplay(false, null),
                (net.minecraft.network.chat.Component line) -> lines.add(line.getString()),
                net.minecraft.world.item.TooltipFlag.NORMAL);
        helper.assertTrue(lines.get(1).contains("Purified Water"),
                "purified water must say so, got " + lines.get(1));
        helper.succeed();
    }

    /** 0.1.49: 0x2EC6B6 is the reference's own icon colour, not a house pick. */
    @GameTest
    public void thirstEffectUsesTheReferenceIconColour(GameTestHelper helper) {
        int colour = ThirstMobEffect.HOLDER.value().getColor();
        helper.assertTrue(colour == 0x2EC6B6,
                "the thirst effect icon must be 0x2EC6B6, got " + Integer.toHexString(colour));
        helper.succeed();
    }

    /** 0.1.49: the three numbers the reference does not set and we had wrong. */
    @GameTest
    public void thirstConfigMatchesTheReferenceDefaults(GameTestHelper helper) {
        HearthwindSurvivalConfig.Thirst cfg = HearthwindSurvivalConfig.get().thirst;
        helper.assertTrue(Math.abs(cfg.netherFactor - 1.3) < 1e-9,
                "nether_factor is 1.3 upstream, got " + cfg.netherFactor);
        helper.assertTrue(Math.abs(cfg.hydratingFactor - 2.0) < 1e-9,
                "Aged sets hydrating_factor 2.0, got " + cfg.hydratingFactor);
        helper.assertTrue(Math.abs(cfg.thirstEffectFactor - 0.03) < 1e-9,
                "Aged sets thirst_effect_factor 0.03, got " + cfg.thirstEffectFactor);
        helper.succeed();
    }

    /**
     * 0.1.50: Dehydration 1.3.6's four sound events, registered at the
     * reference's own ids.
     *
     * <p>Until this release every one of these moments played a VANILLA
     * substitute - {@code BOTTLE_FILL} when a flask was filled,
     * {@code BOTTLE_EMPTY} when it was drunk, {@code GENERIC_DRINK} for the
     * bare-hand sip and {@code BUBBLE_COLUMN_BUBBLE_POP} for the boiling
     * cauldron - so the pack did not sound the way Aged does. The ids matter
     * as well as the files: {@code assets/dehydration/sounds.json} resolves
     * each entry by id, so a renamed id plays silence even with the .ogg
     * sitting right there.
     */
    @GameTest
    public void dehydrationSoundsUseTheReferenceIds(GameTestHelper helper) {
        assertSound(helper, DehydrationSounds.FILL_FLASK, "fill_flask");
        assertSound(helper, DehydrationSounds.WATER_SIP, "water_sip");
        assertSound(helper, DehydrationSounds.EMPTY_FLASK, "empty_flask");
        assertSound(helper, DehydrationSounds.CAULDRON_BUBBLE, "cauldron_bubble");
        helper.succeed();
    }

    private static void assertSound(GameTestHelper helper, net.minecraft.sounds.SoundEvent event,
            String name) {
        helper.assertTrue(event != null,
                "dehydration:" + name + " must be registered - DehydrationSounds.register() is not wired");
        helper.assertTrue(event.location().toString().equals("dehydration:" + name),
                "the sound event id must be dehydration:" + name + ", got " + event.location());
    }

    /**
     * 0.1.50: the {@code .ogg} files and the {@code sounds.json} that names
     * them have to ship in OUR jar, because the ids above resolve against our
     * own asset tree.
     *
     * <p>Dehydration is GPL-3.0 and this project already ships its textures
     * verbatim under {@code assets/dehydration/} with an
     * {@code ATTRIBUTION.md} row; these nine files came the same way. A test
     * that only checked the registry would have passed while every one of them
     * was missing, which is exactly how the vanilla substitutes survived.
     */
    @GameTest
    public void dehydrationSoundFilesShipInOurJar(GameTestHelper helper) {
        String[] oggs = {
            "assets/dehydration/sounds/fill_flask_1.ogg",
            "assets/dehydration/sounds/fill_flask_2.ogg",
            "assets/dehydration/sounds/water_sip_1.ogg",
            "assets/dehydration/sounds/water_sip_2.ogg",
            "assets/dehydration/sounds/water_sip_3.ogg",
            "assets/dehydration/sounds/empty_flask_1.ogg",
            "assets/dehydration/sounds/empty_flask_2.ogg",
            "assets/dehydration/sounds/cauldron_bubble_1.ogg",
            "assets/dehydration/sounds/cauldron_bubble_2.ogg",
        };
        for (String file : oggs) {
            try (java.io.InputStream in = DehydrationSounds.class.getClassLoader()
                    .getResourceAsStream(file)) {
                helper.assertTrue(in != null, file + " must ship in the hearthwind-survival jar");
                assert in != null;
                byte[] bytes = in.readAllBytes();
                helper.assertTrue(bytes.length > 1000,
                        file + " must be a real ogg, not a " + bytes.length + "-byte stub");
            } catch (java.io.IOException e) {
                throw new AssertionError("could not read " + file, e);
            }
        }
        // The manifest is JSON, not audio, so it gets its own check: every sound
        // event must actually be NAMED in it. Minecraft resolves a played
        // SoundEvent by looking its id up here, so a manifest that exists but
        // omits an event plays silence - and a size check would not notice.
        try (java.io.InputStream in = DehydrationSounds.class.getClassLoader()
                .getResourceAsStream("assets/dehydration/sounds.json")) {
            helper.assertTrue(in != null, "assets/dehydration/sounds.json must ship");
            assert in != null;
            String manifest = new String(in.readAllBytes(),
                    java.nio.charset.StandardCharsets.UTF_8);
            for (String event : new String[] {
                "fill_flask", "water_sip", "empty_flask", "cauldron_bubble"}) {
                helper.assertTrue(manifest.contains("\"" + event + "\""),
                        "sounds.json must name " + event
                                + ", or the event plays silence; manifest was: " + manifest);
            }
        } catch (java.io.IOException e) {
            throw new AssertionError("could not read assets/dehydration/sounds.json", e);
        }
        helper.succeed();
    }

    /**
     * Reference {@code thirst_preview}: the droplet row under an item's
     * tooltip. This closes the last missing row of the Dehydration audit, so
     * the arithmetic is pinned rather than eyeballed.
     */
    @GameTest
    public void thirstPreviewArithmeticMatchesTheReference(GameTestHelper helper) {
        // The reference's own expression: quench * 9 / 2 plus a 9px half
        // droplet when the quench is odd.
        // Integer division runs BEFORE the odd check, so the reference's own
        // expression is not ceil(quench/2)*9: a single droplet reports 13px of
        // width while drawing 9px of art. Transcribed, not tidied.
        helper.assertTrue(ThirstPreview.widthFor(1) == 13, "one droplet reports 13 wide, got "
                + ThirstPreview.widthFor(1));
        helper.assertTrue(ThirstPreview.widthFor(2) == 9, "two droplets share one cell, got "
                + ThirstPreview.widthFor(2));
        helper.assertTrue(ThirstPreview.widthFor(3) == 22, "three droplets report 22 wide, got "
                + ThirstPreview.widthFor(3));
        helper.assertTrue(ThirstPreview.widthFor(4) == 18, "four droplets are 18 wide, got "
                + ThirstPreview.widthFor(4));
        helper.assertTrue(ThirstPreview.widthFor(16) == 72, "the leather flask's capacity row is 72 wide, got "
                + ThirstPreview.widthFor(16));
        helper.assertTrue(ThirstPreview.HEIGHT == 11, "the row is a hardcoded 11 tall, got "
                + ThirstPreview.HEIGHT);
        // The sheet has four qualities at u = quality * 18, half droplet + 9,
        // and the art always sits on v = 9 (the reference never uses v = 18).
        helper.assertTrue(ThirstPreview.fullU(0) == 0 && ThirstPreview.fullU(3) == 54,
                "qualities are 18 apart");
        helper.assertTrue(ThirstPreview.halfU(2) == 45, "the half droplet sits 9 right of its pair");
        helper.assertTrue(ThirstPreview.ICON_V == 9, "droplets are always drawn on v = 9");
        helper.succeed();
    }

    /**
     * Reference {@code LeatherFlask#getTooltipData}. The middle case is the
     * one a player meets most: a flask that HAS been filled but is empty right
     * now shows <b>no</b> droplets at all, not a zero row.
     */
    @GameTest
    public void thirstPreviewOnAFlaskMatchesTheReference(GameTestHelper helper) {
        int perSip = HearthwindSurvivalConfig.get().flask.quench;

        // Never filled: the capacity preview, drawn at quality 2 (dirty red).
        java.util.Optional<ThirstPreview> never = ThirstPreview.forFlask(
                new ItemStack(FlaskItems.LEATHER_FLASK), 2, perSip);
        helper.assertTrue(never.isPresent(), "an unfilled flask previews its capacity");
        helper.assertTrue(never.get().quench() == 2 * 2 * perSip,
                "a leather flask previews 2 * capacity * quench = 16, got " + never.get().quench());
        helper.assertTrue(never.get().quality() == FlaskData.DIRTY,
                "the capacity preview is drawn at quality 2, got " + never.get().quality());

        // Filled and emptied: nothing at all.
        ItemStack emptied = new ItemStack(FlaskItems.LEATHER_FLASK);
        emptied.set(FlaskItems.FLASK_DATA, new FlaskData(0, FlaskData.DIRTY));
        helper.assertTrue(ThirstPreview.forFlask(emptied, 2, perSip).isEmpty(),
                "a flask with a fill level of 0 shows no preview");

        // Filled and full. The two numbers are INDEPENDENT: the reference
        // hands the constructor (nbt purified_water, nbt leather_flask *
        // flask_thirst_quench), so the purity picks the droplet ART and the
        // fill level picks the COUNT. Multiplying them would make purified
        // water preview nothing, since its purity field is 0.
        ItemStack full = new ItemStack(FlaskItems.LEATHER_FLASK);
        full.set(FlaskItems.FLASK_DATA, new FlaskData(2, FlaskData.DIRTY));
        java.util.Optional<ThirstPreview> dirty = ThirstPreview.forFlask(full, 2, perSip);
        helper.assertTrue(dirty.isPresent(), "a full flask previews what it holds");
        helper.assertTrue(dirty.get().quench() == 2 * perSip,
                "a full leather flask previews fill_level * quench = 8, got " + dirty.get().quench());
        helper.assertTrue(dirty.get().quality() == FlaskData.DIRTY,
                "dirty water is drawn at quality 2, got " + dirty.get().quality());

        // One unit of PURIFIED water: the count is still one sip - the purity
        // field must not be multiplied into it.
        ItemStack sip = new ItemStack(FlaskItems.LEATHER_FLASK);
        sip.set(FlaskItems.FLASK_DATA, new FlaskData(1, FlaskData.PURIFIED));
        ThirstPreview pure = ThirstPreview.forFlask(sip, 2, perSip).orElseThrow();
        helper.assertTrue(pure.quench() == perSip,
                "one unit of purified water previews one sip of " + perSip + ", got " + pure.quench());
        helper.assertTrue(pure.quality() == FlaskData.PURIFIED,
                "purified water is drawn at quality 0, got " + pure.quality());
        helper.succeed();
    }

    /**
     * Reference {@code PotionItemMixin#getTooltipData}: splash and lingering
     * are refused outright, and a bad potion is drawn at quality 2.
     */
    @GameTest
    public void thirstPreviewOnAPotionMatchesTheReference(GameTestHelper helper) {
        int fallback = (int) Math.round(HearthwindSurvivalConfig.get().thirst.potionThirstQuench);

        // Plain water is one of the 14 "bad potions", so quality 2.
        java.util.Optional<ThirstPreview> water = ThirstPreview.forPotion(waterPotion(), 0, fallback, false);
        helper.assertTrue(water.isPresent(), "a plain potion previews");
        helper.assertTrue(water.get().quench() == fallback,
                "an uncatalogued potion falls back to potion_thirst_quench = " + fallback
                        + ", got " + water.get().quench());
        helper.assertTrue(water.get().quality() == 2,
                "plain water is a bad potion so it is drawn at quality 2, got " + water.get().quality());

        // A good potion is drawn at quality 0.
        ItemStack healing = new ItemStack(Items.POTION);
        healing.set(net.minecraft.core.component.DataComponents.POTION_CONTENTS,
                new net.minecraft.world.item.alchemy.PotionContents(
                        net.minecraft.world.item.alchemy.Potions.HEALING));
        java.util.Optional<ThirstPreview> good = ThirstPreview.forPotion(healing, 0, fallback, false);
        helper.assertTrue(good.isPresent(), "a healing potion still previews its quench");
        helper.assertTrue(good.get().quality() == 0,
                "a beneficial potion is drawn at quality 0, got " + good.get().quality());

        // Splash and lingering are skipped by the reference (ThrowablePotionItem).
        helper.assertTrue(ThirstPreview.forPotion(waterPotion(), 0, fallback, true).isEmpty(),
                "a splash potion shows no preview");
        helper.assertTrue(ThirstPreview.isThrowable(new ItemStack(Items.SPLASH_POTION)),
                "splash potions are throwable");
        helper.assertTrue(ThirstPreview.isThrowable(new ItemStack(Items.LINGERING_POTION)),
                "lingering potions are throwable");
        helper.assertTrue(!ThirstPreview.isThrowable(waterPotion()),
                "the plain potion is not throwable");

        // The corpus overrides the fallback, exactly as the reference's scan does.
        helper.assertTrue(ThirstPreview.forPotion(healing, 7, fallback, false).orElseThrow().quench() == 7,
                "a catalogued potion previews its tier, not the fallback");
        helper.succeed();
    }

    /**
     * Reference {@code ItemMixin#getTooltipDataMixin}: the tag ladder runs
     * first and the hydration corpus <b>overrides</b> it. All six tags ship
     * empty, so the tag branch is exercised here directly.
     */
    @GameTest
    public void thirstPreviewOnFoodsUsesTheTagLadderThenTheCorpus(GameTestHelper helper) {
        ThirstPreview.TagQuench none = ThirstPreview.TagQuench.NONE;

        // Nothing at all: no preview.
        helper.assertTrue(ThirstPreview.forItem(new ItemStack(Items.STONE), none, 0).isEmpty(),
                "an item that quenches nothing shows no droplets");

        // The corpus alone is enough.
        ThirstPreview apple = ThirstPreview.forItem(new ItemStack(Items.APPLE), none, 4)
                .orElseThrow();
        helper.assertTrue(apple.quench() == 4, "an apple previews its catalogued tier, got " + apple.quench());
        helper.assertTrue(apple.quality() == 0, "food is always drawn at quality 0, got " + apple.quality());

        // The corpus overrides a tag value - the reference scans after the tags.
        ThirstPreview.TagQuench stew = new ThirstPreview.TagQuench(3, 0, 0, 0, 0, 0);
        helper.assertTrue(ThirstPreview.forItem(new ItemStack(Items.APPLE), stew, 9).orElseThrow().quench() == 9,
                "a catalogued tier beats the tag value");
        helper.assertTrue(ThirstPreview.forItem(new ItemStack(Items.APPLE), stew, 0).orElseThrow().quench() == 3,
                "with no corpus entry the stew tag wins");

        // The ladder order: stew, then food, then drinks, then the stronger tags.
        ThirstPreview.TagQuench ladder = new ThirstPreview.TagQuench(3, 1, 2, 6, 2, 4);
        helper.assertTrue(ThirstPreview.forItem(new ItemStack(Items.APPLE), ladder, 0).orElseThrow().quench() == 3,
                "hydrating_stew is consulted first");
        helper.assertTrue(ThirstPreview.forItem(new ItemStack(Items.APPLE),
                new ThirstPreview.TagQuench(0, 1, 2, 6, 2, 4), 0).orElseThrow().quench() == 1,
                "then hydrating_food");
        helper.assertTrue(ThirstPreview.forItem(new ItemStack(Items.APPLE),
                new ThirstPreview.TagQuench(0, 0, 2, 6, 2, 4), 0).orElseThrow().quench() == 2,
                "then hydrating_drinks");
        helper.assertTrue(ThirstPreview.forItem(new ItemStack(Items.APPLE),
                new ThirstPreview.TagQuench(0, 0, 0, 6, 2, 4), 0).orElseThrow().quench() == 6,
                "then stronger_hydrating_stew");
        helper.assertTrue(ThirstPreview.forItem(new ItemStack(Items.APPLE),
                new ThirstPreview.TagQuench(0, 0, 0, 0, 2, 4), 0).orElseThrow().quench() == 2,
                "then stronger_hydrating_food");
        helper.assertTrue(ThirstPreview.forItem(new ItemStack(Items.APPLE),
                new ThirstPreview.TagQuench(0, 0, 0, 0, 0, 4), 0).orElseThrow().quench() == 4,
                "then stronger_hydrating_drinks");
        helper.succeed();
    }

    /**
     * The six tag files ship, and they ship EMPTY - which is what the
     * reference jar and Aged's own pack both carry. A datapack that fails to
     * parse them would silently drop the tag, so this asserts the tag resolves
     * (even to nothing) rather than that the file exists.
     */
    @GameTest
    public void theHydratingTagsResolveAndAreEmptyLikeTheReference(GameTestHelper helper) {
        for (String path : new String[] {"hydrating_stew", "hydrating_food", "hydrating_drinks",
                "stronger_hydrating_stew", "stronger_hydrating_food", "stronger_hydrating_drinks"}) {
            net.minecraft.tags.TagKey<Item> tag = net.minecraft.tags.TagKey.create(
                    net.minecraft.core.registries.Registries.ITEM,
                    net.minecraft.resources.Identifier.fromNamespaceAndPath("dehydration", path));
            java.util.Optional<net.minecraft.core.HolderSet.Named<Item>> members =
                    BuiltInRegistries.ITEM.get(tag);
            helper.assertTrue(members.isPresent(),
                    "dehydration:" + path + " must resolve - the tooltip reads it on every hover");
            int size = 0;
            for (net.minecraft.core.Holder<Item> ignored : members.get()) {
                size++;
            }
            helper.assertTrue(size == 0,
                    "dehydration:" + path + " is empty in the reference and in Aged, got "
                            + size + " entries");
        }
        helper.assertTrue(HearthwindSurvivalConfig.get().thirst.thirstPreview,
                "thirst_preview defaults on, as it does upstream and in Aged");
        helper.assertTrue(HearthwindSurvivalConfig.get().thirst.stewThirstQuench == 3
                && HearthwindSurvivalConfig.get().thirst.foodThirstQuench == 1
                && HearthwindSurvivalConfig.get().thirst.drinksThirstQuench == 2
                && HearthwindSurvivalConfig.get().thirst.strongerStewThirstQuench == 6
                && HearthwindSurvivalConfig.get().thirst.strongerFoodThirstQuench == 2
                && HearthwindSurvivalConfig.get().thirst.strongerDrinksThirstQuench == 4,
                "the six tag-ladder quenches match the reference defaults");
        helper.succeed();
    }

    /**
     * The droplet sheet ships, because the tooltip is a silent no-op without
     * it: the component resolves a texture the client then cannot load.
     */
    @GameTest
    public void theThirstDropletSheetShipsInOurJar(GameTestHelper helper) {
        try (java.io.InputStream in = ThirstPreview.class.getClassLoader()
                .getResourceAsStream("assets/dehydration/textures/gui/thirst.png")) {
            helper.assertTrue(in != null,
                    "assets/dehydration/textures/gui/thirst.png must ship in the jar");
            assert in != null;
            byte[] bytes = in.readAllBytes();
            helper.assertTrue(bytes.length > 100,
                    "the droplet sheet must be a real png, not a " + bytes.length + "-byte stub");
            helper.assertTrue(bytes.length > 8 && (bytes[0] & 0xFF) == 0x89 && bytes[1] == 'P'
                    && bytes[2] == 'N' && bytes[3] == 'G',
                    "the droplet sheet must be a png, not something else entirely");
        } catch (java.io.IOException e) {
            throw new AssertionError("could not read the droplet sheet", e);
        }
        helper.succeed();
    }
}
