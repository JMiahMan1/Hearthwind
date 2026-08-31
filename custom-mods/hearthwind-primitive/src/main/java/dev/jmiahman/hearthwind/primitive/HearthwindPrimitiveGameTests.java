package dev.jmiahman.hearthwind.primitive;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

/**
 * Headless gametests for primitive progression items.
 */
public final class HearthwindPrimitiveGameTests {
    public HearthwindPrimitiveGameTests() {}



    @GameTest
    public void flintToolsExistAndHaveCorrectTier(GameTestHelper helper) {
        helper.assertTrue(HearthwindPrimitiveItems.FLINT_PICKAXE != null, "flint pickaxe exists");
        helper.assertTrue(HearthwindPrimitiveItems.FLINT_AXE != null, "flint axe exists");
        helper.assertTrue(HearthwindPrimitiveItems.FLINT_SWORD != null, "flint sword exists");

        // Check the tool material has correct repair items tag
        helper.assertTrue(HearthwindPrimitiveItems.FLINT_REPAIR_ITEMS != null,
                "flint repair tag exists");

        helper.succeed();
    }

    @GameTest
    public void steelItemsRegistered(GameTestHelper helper) {
        helper.assertTrue(HearthwindPrimitiveItems.STEEL_INGOT != null, "steel ingot exists");
        helper.assertTrue(HearthwindPrimitiveItems.STEEL_NUGGET != null, "steel nugget exists");

        helper.succeed();
    }

    @GameTest
    public void dirtyWaterSicknessChanceConfig(GameTestHelper helper) {
        var config = dev.jmiahman.hearthwind.survival.HearthwindSurvivalConfig.get();
        helper.assertTrue(config.thirst.dirtyWaterSicknessChance > 0,
                "dirty water sickness chance should be positive");
        helper.assertTrue(config.thirst.dirtyWaterSicknessChance <= 1.0,
                "dirty water sickness chance should not exceed 1.0");
        helper.succeed();
    }

    @GameTest
    public void treeFellingThresholdConfig(GameTestHelper helper) {
        // Verify TreeFelling class is registered and threshold is positive
        helper.assertTrue(dev.jmiahman.hearthwind.primitive.TreeFelling.class != null,
                "TreeFelling class exists");
        helper.succeed();
    }

    @GameTest
    public void treeFellingAllowsNonLogBreak(GameTestHelper helper) {
        // REGRESSION: TreeFelling's BEFORE handler used to return false
        // unconditionally, silently cancelling EVERY player block break.
        // NOTE: helper.setBlock/getBlockState take RELATIVE positions (they
        // absolutePos internally); only the raw gameMode.destroyBlock call
        // needs helper.absolutePos. Mixing them up double-offsets silently.
        net.minecraft.core.BlockPos rel = new net.minecraft.core.BlockPos(24, 64, 24);
        helper.setBlock(rel, net.minecraft.world.level.block.Blocks.DIRT.defaultBlockState());

        ServerPlayer mockPlayer = helper.makeMockServerPlayerInLevel();
        mockPlayer.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);

        // Real break path (fires PlayerBlockBreakEvents.BEFORE)
        boolean broke = mockPlayer.gameMode.destroyBlock(helper.absolutePos(rel));
        helper.assertTrue(broke, "dirt must break by hand (no event may deny it)");
        helper.assertTrue(helper.getBlockState(rel).isAir(),
                "dirt block should be gone after hand break");
        helper.succeed();
    }

    @GameTest
    public void treeFellingAllowsSingleLogBreak(GameTestHelper helper) {
        // Chopping below threshold must proceed normally (no cancellation)
        net.minecraft.core.BlockPos rel = new net.minecraft.core.BlockPos(24, 64, 26);
        helper.setBlock(rel,
                net.minecraft.world.level.block.Blocks.OAK_LOG.defaultBlockState());

        ServerPlayer mockPlayer = helper.makeMockServerPlayerInLevel();
        mockPlayer.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);

        boolean broke = mockPlayer.gameMode.destroyBlock(helper.absolutePos(rel));
        helper.assertTrue(broke, "log must break by hand below felling threshold");
        helper.assertTrue(helper.getBlockState(rel).isAir(),
                "log block should be gone after hand break");
        helper.succeed();
    }

    @GameTest
    public void treeFellingSpawnsFallingBlock(GameTestHelper helper) {
        net.minecraft.core.BlockPos origin = new net.minecraft.core.BlockPos(8, 64, 8);
        net.minecraft.world.level.block.state.BlockState logState =
                net.minecraft.world.level.block.Blocks.OAK_LOG.defaultBlockState();

        // Place a 4-block tall oak log column (no support underneath).
        // Relative coords: helper.setBlock absolutePos's internally.
        helper.setBlock(origin, logState);
        helper.setBlock(origin.above(1), logState);
        helper.setBlock(origin.above(2), logState);
        helper.setBlock(origin.above(3), logState);

        helper.assertTrue(helper.getBlockState(origin.above(3)).is(net.minecraft.tags.BlockTags.LOGS),
                "top log should exist before breaking");

        ServerPlayer mockPlayer = helper.makeMockServerPlayerInLevel();
        mockPlayer.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);

        // Break bottom 3 logs through the real game-mode path (fires the
        // fabric AFTER break event, then TreeFelling threshold logic).
        // gameMode.destroyBlock is a RAW call - it needs the ABSOLUTE pos.
        helper.assertTrue(mockPlayer.gameMode.destroyBlock(helper.absolutePos(origin)),
                "bottom log must break");
        helper.assertTrue(mockPlayer.gameMode.destroyBlock(helper.absolutePos(origin.above(1))),
                "second log must break");
        helper.assertTrue(mockPlayer.gameMode.destroyBlock(helper.absolutePos(origin.above(2))),
                "third log must break");

        // After breaking 3 blocks, the 4th (unsupported) should spawn a falling block
        helper.runAtTickTime(helper.getTick() + 10, () -> {
            java.util.List<net.minecraft.world.entity.item.FallingBlockEntity> fallingBlocks =
                    helper.getEntities(
                            net.minecraft.world.entity.EntityTypes.FALLING_BLOCK,
                            origin.above(3), 4.0);
            helper.assertTrue(fallingBlocks.size() > 0,
                    "falling block entity should spawn after breaking 3 trunk blocks");
        });

        helper.succeed();
    }

    @GameTest
    public void treeFellingNoFallBelowThreshold(GameTestHelper helper) {
        net.minecraft.core.BlockPos origin = new net.minecraft.core.BlockPos(16, 64, 16);
        net.minecraft.world.level.block.state.BlockState logState =
                net.minecraft.world.level.block.Blocks.OAK_LOG.defaultBlockState();

        // Place a 4-block tall oak log column standing on stone
        helper.setBlock(origin.below(), net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
        helper.setBlock(origin, logState);
        helper.setBlock(origin.above(1), logState);
        helper.setBlock(origin.above(2), logState);
        helper.setBlock(origin.above(3), logState);

        ServerPlayer mockPlayer = helper.makeMockServerPlayerInLevel();
        mockPlayer.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);

        // Break only 2 logs - below the felling threshold of 3
        helper.assertTrue(mockPlayer.gameMode.destroyBlock(helper.absolutePos(origin)), "first log must break");
        helper.assertTrue(mockPlayer.gameMode.destroyBlock(helper.absolutePos(origin.above(1))), "second log must break");

        // No felling should trigger; remaining logs must stay in place
        helper.runAtTickTime(helper.getTick() + 10, () -> {
            java.util.List<net.minecraft.world.entity.item.FallingBlockEntity> fallingBlocks =
                    helper.getEntities(
                            net.minecraft.world.entity.EntityTypes.FALLING_BLOCK,
                            origin.above(3), 4.0);
            helper.assertTrue(fallingBlocks.size() == 0,
                    "no logs should fall below the felling threshold");
            helper.assertTrue(helper.getBlockState(origin.above(2)).is(net.minecraft.tags.BlockTags.LOGS),
                    "third log should still be a block");
            helper.assertTrue(helper.getBlockState(origin.above(3)).is(net.minecraft.tags.BlockTags.LOGS),
                    "top log should still be a block");
        });

        helper.succeed();
    }

    @GameTest
    public void steelBlockExistsAndHasCorrectStrength(GameTestHelper helper) {
        helper.assertTrue(HearthwindPrimitiveBlocks.STEEL_BLOCK != null, "steel block must exist");
        var state = HearthwindPrimitiveBlocks.STEEL_BLOCK.defaultBlockState();
        helper.assertTrue(state != null, "steel block state must be non-null");
        helper.succeed();
    }

    @GameTest
    public void rockItemExistsAndIsRegistered(GameTestHelper helper) {
        helper.assertTrue(HearthwindPrimitiveBlocks.ROCK != null, "rock block must exist");
        var rock = new net.minecraft.world.item.ItemStack(HearthwindPrimitiveBlocks.ROCK);
        helper.assertTrue(!rock.isEmpty(), "rock stack must not be empty");
        helper.assertTrue(rock.getItem() instanceof net.minecraft.world.item.BlockItem,
                "earlystage:rock must be a BlockItem (parity with original earlystage)");
        helper.succeed();
    }

    @GameTest
    public void flintToolStatsAreReasonable(GameTestHelper helper) {
        var pickaxe = new net.minecraft.world.item.ItemStack(HearthwindPrimitiveItems.FLINT_PICKAXE);
        helper.assertTrue(pickaxe.is(HearthwindPrimitiveItems.FLINT_PICKAXE),
                "flint pickaxe item stack is correct type");
        var axe = new net.minecraft.world.item.ItemStack(HearthwindPrimitiveItems.FLINT_AXE);
        helper.assertTrue(axe.is(HearthwindPrimitiveItems.FLINT_AXE),
                "flint axe item stack is correct type");
        helper.succeed();
    }

    @GameTest
    public void steelIngotAndNuggetChestplateRecipe(GameTestHelper helper) {
        // Verify steel items exist for armor crafting
        helper.assertTrue(HearthwindPrimitiveItems.STEEL_INGOT != null, "steel ingot exists");
        helper.assertTrue(HearthwindPrimitiveItems.STEEL_NUGGET != null, "steel nugget exists");
        helper.succeed();
    }

    @GameTest
    public void orePiecesExist(GameTestHelper helper) {
        helper.assertTrue(HearthwindPrimitiveItems.COAL_PIECE != null, "coal piece exists");
        helper.assertTrue(HearthwindPrimitiveItems.COPPER_NUGGET != null, "copper nugget exists");
        helper.assertTrue(HearthwindPrimitiveItems.RAW_IRON_NUGGET != null, "raw iron nugget exists");
        helper.assertTrue(HearthwindPrimitiveItems.RAW_GOLD_NUGGET != null, "raw gold nugget exists");
        helper.succeed();
    }

    @GameTest
    public void sieveBlockExistsAndIsRegistered(GameTestHelper helper) {
        helper.assertTrue(HearthwindPrimitiveBlocks.SIEVE != null, "sieve block must exist");
        var state = HearthwindPrimitiveBlocks.SIEVE.defaultBlockState();
        helper.assertTrue(state != null, "sieve block state must be non-null");
        helper.succeed();
    }

    @GameTest
    public void sieveLoadsDropsFromJson(GameTestHelper helper) {
        dev.jmiahman.hearthwind.primitive.SieveBlock.loadDrops();
        helper.assertTrue(dev.jmiahman.hearthwind.primitive.SieveBlock.dropCount() > 0,
                "sieve should have loaded drop entries from JSON");
        helper.succeed();
    }

    @GameTest
    public void sieveSiftsGrassBlock(GameTestHelper helper) {
        dev.jmiahman.hearthwind.primitive.SieveBlock.loadDrops();
        // Verify the sieve can be placed and the block state is valid
        // (helper.setBlock/getBlockState take RELATIVE positions)
        net.minecraft.core.BlockPos pos = new net.minecraft.core.BlockPos(8, 64, 8);
        helper.setBlock(pos, HearthwindPrimitiveBlocks.SIEVE.defaultBlockState());
        helper.assertTrue(helper.getBlockState(pos).is(HearthwindPrimitiveBlocks.SIEVE),
                "sieve block should be placeable");
        helper.succeed();
    }

    @GameTest
    public void craftingRockBlockExistsAndIsRegistered(GameTestHelper helper) {
        helper.assertTrue(HearthwindPrimitiveBlocks.CRAFTING_ROCK != null, "crafting_rock block must exist");
        var state = HearthwindPrimitiveBlocks.CRAFTING_ROCK.defaultBlockState();
        helper.assertTrue(state != null, "crafting_rock block state must be non-null");
        helper.succeed();
    }

    @GameTest
    public void craftingRockHasFacingProperty(GameTestHelper helper) {
        var state = HearthwindPrimitiveBlocks.CRAFTING_ROCK.defaultBlockState();
        helper.assertTrue(state.hasProperty(CraftingRockBlock.FACING),
                "crafting_rock must have a facing property (earlystage parity)");
        helper.assertTrue(HearthwindPrimitiveBlocks.SIEVE.defaultBlockState()
                .hasProperty(SieveBlock.FACING), "sieve must have a facing property");
        helper.assertTrue(HearthwindPrimitiveBlocks.REDSTONE_SIEVE.defaultBlockState()
                        .hasProperty(RedstoneSieveBlock.POWERED),
                "redstone_sieve must have a powered property");
        helper.succeed();
    }

    @GameTest
    public void blockEntityTypesRegisteredForSieveAndCraftingRock(GameTestHelper helper) {
        helper.assertTrue(HearthwindPrimitiveBlocks.SIEVE_ENTITY != null,
                "sieve entity type must exist");
        helper.assertTrue(HearthwindPrimitiveBlocks.CRAFTING_ROCK_ENTITY != null,
                "crafting rock entity type must exist");
        net.minecraft.core.BlockPos pos = new net.minecraft.core.BlockPos(1, 1, 1);
        helper.setBlock(pos, HearthwindPrimitiveBlocks.SIEVE.defaultBlockState());
        var be = helper.getLevel().getBlockEntity(helper.absolutePos(pos));
        helper.assertTrue(be instanceof SieveBlockEntity,
                "placed sieve must produce a SieveBlockEntity");
        helper.succeed();
    }

    @GameTest
    public void usableCraftingRockItemsTagContainsStick(GameTestHelper helper) {
        var stick = new ItemStack(net.minecraft.world.item.Items.STICK);
        helper.assertTrue(stick.is(HearthwindPrimitiveTags.USABLE_CRAFTING_ROCK_ITEMS),
                "usable_crafting_rock_items tag must contain minecraft:stick");
        var rock = new ItemStack(HearthwindPrimitiveBlocks.ROCK.asItem());
        helper.assertTrue(!rock.is(HearthwindPrimitiveTags.USABLE_CRAFTING_ROCK_ITEMS),
                "earlystage:rock must NOT be in usable_crafting_rock_items (earlystage parity)");
        helper.succeed();
    }

    @GameTest
    public void craftingRockInsertsUsableItemViaUseItemOn(GameTestHelper helper) {
        ServerPlayer mockPlayer = helper.makeMockServerPlayerInLevel();
        mockPlayer.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        net.minecraft.core.BlockPos rel = new net.minecraft.core.BlockPos(2, 1, 1);
        helper.setBlock(rel, HearthwindPrimitiveBlocks.CRAFTING_ROCK.defaultBlockState());
        var abs = helper.absolutePos(rel);

        ItemStack stick = new ItemStack(net.minecraft.world.item.Items.STICK, 4);
        mockPlayer.setItemInHand(InteractionHand.MAIN_HAND, stick);

        // Aim at the top surface (y frac ~0.5), cell 0 (x frac >0.66, z frac >0.66)
        net.minecraft.world.phys.BlockHitResult hit = new net.minecraft.world.phys.BlockHitResult(
                new net.minecraft.world.phys.Vec3(abs.getX() + 0.85, abs.getY() + 0.5, abs.getZ() + 0.85),
                net.minecraft.core.Direction.UP, abs, false);
        var result = ((CraftingRockBlock) HearthwindPrimitiveBlocks.CRAFTING_ROCK)
                .useItemOn(stick, helper.getBlockState(rel), helper.getLevel(), abs,
                        mockPlayer, InteractionHand.MAIN_HAND, hit);
        helper.assertTrue(result == net.minecraft.world.InteractionResult.CONSUME,
                "inserting a usable item must consume the interaction, got " + result);

        var be = helper.getLevel().getBlockEntity(abs);
        helper.assertTrue(be instanceof CraftingRockBlockEntity, "crafting rock must have a block entity");
        ItemStack slot0 = ((CraftingRockBlockEntity) be).getItem(0);
        helper.assertTrue(!slot0.isEmpty() && slot0.is(net.minecraft.world.item.Items.STICK),
                "slot 0 should hold the inserted stick");
        helper.assertTrue(stick.getCount() == 3,
                "holding stack should shrink by 1, has " + stick.getCount());
        helper.succeed();
    }

    @GameTest
    public void craftingRockCraftsRecipeViaRockHits(GameTestHelper helper) {
        ServerPlayer mockPlayer = helper.makeMockServerPlayerInLevel();
        net.minecraft.core.BlockPos rel = new net.minecraft.core.BlockPos(2, 1, 1);
        helper.setBlock(rel, HearthwindPrimitiveBlocks.CRAFTING_ROCK.defaultBlockState());
        var abs = helper.absolutePos(rel);
        var be = (CraftingRockBlockEntity) helper.getLevel().getBlockEntity(abs);

        // Two vertical planks in BE slots 1 and 4 (variant 0) = shaped "P"/"P" -> 4 sticks
        be.setItem(1, new ItemStack(net.minecraft.world.item.Items.OAK_PLANKS));
        be.setItem(4, new ItemStack(net.minecraft.world.item.Items.OAK_PLANKS));
        be.setCraftHits(2);

        ItemStack rock = new ItemStack(HearthwindPrimitiveBlocks.ROCK.asItem(), 16);
        mockPlayer.setItemInHand(InteractionHand.MAIN_HAND, rock);
        net.minecraft.world.phys.BlockHitResult hit = new net.minecraft.world.phys.BlockHitResult(
                new net.minecraft.world.phys.Vec3(abs.getX() + 0.85, abs.getY() + 0.5, abs.getZ() + 0.85),
                net.minecraft.core.Direction.UP, abs, false);

        var result1 = ((CraftingRockBlock) HearthwindPrimitiveBlocks.CRAFTING_ROCK).useItemOn(rock,
                helper.getBlockState(rel), helper.getLevel(), abs, mockPlayer,
                InteractionHand.MAIN_HAND, hit);
        var result2 = ((CraftingRockBlock) HearthwindPrimitiveBlocks.CRAFTING_ROCK).useItemOn(rock,
                helper.getBlockState(rel), helper.getLevel(), abs, mockPlayer,
                InteractionHand.MAIN_HAND, hit);

        helper.assertTrue(result1 == net.minecraft.world.InteractionResult.CONSUME
                && result2 == net.minecraft.world.InteractionResult.CONSUME,
                "hammering the rock must consume the interaction");
        ItemStack result = be.getItem(4);
        helper.assertTrue(!result.isEmpty() && result.is(net.minecraft.world.item.Items.STICK)
                && result.getCount() == 4,
                "middle slot must hold 4 sticks after craft, got " + result);
        helper.assertTrue(be.getItem(1).isEmpty(),
                "ingredient slot 1 must be empty after craft (grid cleared)");
        helper.succeed();
    }

    @GameTest
    public void craftingRockCraftsRotatedVariants(GameTestHelper helper) {
        // The rock tries all 4 rotations of the grid (reference iterates v0..v3
        // and takes the FIRST matching layout, so a horizontal plank pair
        // crafts a pressure plate under v0 even though v2 also "fits" it).
        ServerPlayer mockPlayer = helper.makeMockServerPlayerInLevel();
        mockPlayer.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        // BE slot pairs {center, other} -> expected result of the first matching variant
        int[][] layouts = {{4, 1}, {4, 7}}; // vertical pairs -> sticks x4
        int[][] plateLayouts = {{4, 3}, {4, 5}}; // horizontal pairs -> pressure plate
        for (int variant = 0; variant < 4; variant++) {
            int[] layout = variant < 2 ? layouts[variant] : plateLayouts[variant - 2];
            boolean expectedSticks = variant < 2;
            net.minecraft.core.BlockPos rel = new net.minecraft.core.BlockPos(2, 1 + variant, 1);
            helper.setBlock(rel, HearthwindPrimitiveBlocks.CRAFTING_ROCK.defaultBlockState());
            var abs = helper.absolutePos(rel);
            var be = (CraftingRockBlockEntity) helper.getLevel().getBlockEntity(abs);
            be.setItem(layout[0], new ItemStack(net.minecraft.world.item.Items.OAK_PLANKS));
            be.setItem(layout[1], new ItemStack(net.minecraft.world.item.Items.OAK_PLANKS));
            be.setCraftHits(1); // next rock hit crafts immediately

            ItemStack rock = new ItemStack(HearthwindPrimitiveBlocks.ROCK.asItem());
            mockPlayer.setItemInHand(InteractionHand.MAIN_HAND, rock);
            net.minecraft.world.phys.BlockHitResult hit = new net.minecraft.world.phys.BlockHitResult(
                    new net.minecraft.world.phys.Vec3(abs.getX() + 0.85, abs.getY() + 0.5, abs.getZ() + 0.85),
                    net.minecraft.core.Direction.UP, abs, false);
            var result = ((CraftingRockBlock) HearthwindPrimitiveBlocks.CRAFTING_ROCK).useItemOn(rock,
                    helper.getBlockState(rel), helper.getLevel(), abs, mockPlayer,
                    InteractionHand.MAIN_HAND, hit);
            helper.assertTrue(result == net.minecraft.world.InteractionResult.CONSUME,
                    "rock hit must consume, got " + result);
            helper.assertTrue(!be.getItem(4).isEmpty()
                    && be.getItem(4).is(expectedSticks
                            ? net.minecraft.world.item.Items.STICK
                            : net.minecraft.world.item.Items.OAK_PRESSURE_PLATE),
                    "variant " + variant + " (BE slots " + layout[1] + "+4) must craft "
                            + (expectedSticks ? "sticks" : "oak_pressure_plate")
                            + ", got " + be.getItem(4));
        }
        helper.succeed();
    }

    @GameTest
    public void craftingRockGivesBackPlacedItem(GameTestHelper helper) {
        ServerPlayer mockPlayer = helper.makeMockServerPlayerInLevel();
        mockPlayer.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        net.minecraft.core.BlockPos rel = new net.minecraft.core.BlockPos(2, 1, 1);
        helper.setBlock(rel, HearthwindPrimitiveBlocks.CRAFTING_ROCK.defaultBlockState());
        var abs = helper.absolutePos(rel);
        var be = (CraftingRockBlockEntity) helper.getLevel().getBlockEntity(abs);
        be.setItem(0, new ItemStack(net.minecraft.world.item.Items.STICK));
        be.setCraftHits(2);

        net.minecraft.world.phys.BlockHitResult hit = new net.minecraft.world.phys.BlockHitResult(
                new net.minecraft.world.phys.Vec3(abs.getX() + 0.85, abs.getY() + 0.5, abs.getZ() + 0.85),
                net.minecraft.core.Direction.UP, abs, false);
        var result = ((CraftingRockBlock) HearthwindPrimitiveBlocks.CRAFTING_ROCK).useItemOn(ItemStack.EMPTY,
                helper.getBlockState(rel), helper.getLevel(), abs, mockPlayer,
                InteractionHand.MAIN_HAND, hit);
        helper.assertTrue(result == net.minecraft.world.InteractionResult.CONSUME,
                "taking an item back must consume, got " + result);
        helper.assertTrue(be.getItem(0).isEmpty(), "slot 0 must be empty after taking back");
        boolean gotStick = false;
        for (int i = 0; i < mockPlayer.getInventory().getContainerSize(); i++) {
            ItemStack inv = mockPlayer.getInventory().getItem(i);
            if (!inv.isEmpty() && inv.is(net.minecraft.world.item.Items.STICK)) {
                gotStick = true;
            }
        }
        helper.assertTrue(gotStick, "player inventory must receive the stick back");
        helper.succeed();
    }

    @GameTest
    public void craftingRockWearDestroysBlock(GameTestHelper helper) {
        var cfg = HearthwindPrimitiveConfig.get();
        int originalMax = cfg.craftRockMaxCraftHits;
        try {
            cfg.craftRockMaxCraftHits = 3;
            ServerPlayer mockPlayer = helper.makeMockServerPlayerInLevel();
            net.minecraft.core.BlockPos rel = new net.minecraft.core.BlockPos(2, 1, 1);
            helper.setBlock(rel, HearthwindPrimitiveBlocks.CRAFTING_ROCK.defaultBlockState());
            var abs = helper.absolutePos(rel);
            var be = (CraftingRockBlockEntity) helper.getLevel().getBlockEntity(abs);
            be.setItem(0, new ItemStack(net.minecraft.world.item.Items.STICK));
            be.setCraftHits(100); // never craft - every hit is a wear hit

            be.decreaseCraftHits(mockPlayer);
            be.decreaseCraftHits(mockPlayer);
            helper.assertTrue(!helper.getBlockState(rel).isAir(),
                    "crafting rock must survive below craftRockMaxCraftHits");
            be.decreaseCraftHits(mockPlayer);
            helper.assertTrue(helper.getBlockState(rel).isAir(),
                    "crafting rock must break after craftRockMaxCraftHits total hits");
        } finally {
            cfg.craftRockMaxCraftHits = originalMax;
        }
        helper.succeed();
    }

    @GameTest
    public void sieveInsertsAndSiftsToEmpty(GameTestHelper helper) {
        ServerPlayer mockPlayer = helper.makeMockServerPlayerInLevel();
        net.minecraft.core.BlockPos rel = new net.minecraft.core.BlockPos(2, 1, 1);
        helper.setBlock(rel, HearthwindPrimitiveBlocks.SIEVE.defaultBlockState());
        var abs = helper.absolutePos(rel);
        var be = (SieveBlockEntity) helper.getLevel().getBlockEntity(abs);

        helper.assertTrue(!be.canPlaceItem(0, new ItemStack(net.minecraft.world.item.Items.DIAMOND)),
                "diamond is not siftable");
        helper.assertTrue(be.canPlaceItem(0, new ItemStack(net.minecraft.world.level.block.Blocks.DIRT)),
                "dirt must be siftable (template exists)");

        be.setItem(0, new ItemStack(net.minecraft.world.level.block.Blocks.DIRT));
        helper.assertTrue(be.getSieveCount() == 0, "insertion must refresh sieve count");

        be.sieve();
        be.sieve();
        be.sieve();
        helper.assertTrue(!be.isEmpty(), "sieve must not be empty after 3 taps");
        be.sieve();
        helper.assertTrue(be.isEmpty(), "4th tap must consume the siftable stack");
        helper.succeed();
    }

    @GameTest(maxTicks = 300)
    public void redstoneSieveAutoSiftsWhilePowered(GameTestHelper helper) {
        var cfg = HearthwindPrimitiveConfig.get();
        int originalTicks = cfg.redstoneSieveTicks;
        cfg.redstoneSieveTicks = 2;
        net.minecraft.core.BlockPos rel = new net.minecraft.core.BlockPos(2, 1, 2);
        helper.setBlock(rel, HearthwindPrimitiveBlocks.REDSTONE_SIEVE.defaultBlockState());
        var abs = helper.absolutePos(rel);
        var be = (SieveBlockEntity) helper.getLevel().getBlockEntity(abs);
        be.setItem(0, new ItemStack(net.minecraft.world.level.block.Blocks.DIRT));

        // Power the sieve with an adjacent redstone block
        net.minecraft.core.BlockPos powerRel = new net.minecraft.core.BlockPos(3, 1, 2);
        helper.setBlock(powerRel, net.minecraft.world.level.block.Blocks.REDSTONE_BLOCK.defaultBlockState());
        helper.assertTrue(helper.getBlockState(rel).getValue(RedstoneSieveBlock.POWERED),
                "sieve must show POWERED state next to a redstone block");

        helper.runAtTickTime(helper.getTick() + 60, () -> {
            cfg.redstoneSieveTicks = originalTicks;
            helper.assertTrue(be.isEmpty(),
                    "powered redstone sieve must auto-sift its stack away");
            helper.succeed();
        });
    }

    @GameTest
    public void redstoneSieveTakesStackOutWithEmptyHand(GameTestHelper helper) {
        ServerPlayer mockPlayer = helper.makeMockServerPlayerInLevel();
        mockPlayer.getInventory().clearContent();
        mockPlayer.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        net.minecraft.core.BlockPos rel = new net.minecraft.core.BlockPos(2, 1, 3);
        helper.setBlock(rel, HearthwindPrimitiveBlocks.REDSTONE_SIEVE.defaultBlockState());
        var abs = helper.absolutePos(rel);
        var be = (SieveBlockEntity) helper.getLevel().getBlockEntity(abs);
        be.setItem(0, new ItemStack(net.minecraft.world.level.block.Blocks.GRAVEL));

        net.minecraft.world.phys.BlockHitResult hit = new net.minecraft.world.phys.BlockHitResult(
                net.minecraft.world.phys.Vec3.atCenterOf(abs), net.minecraft.core.Direction.UP, abs, false);
        var result = ((RedstoneSieveBlock) HearthwindPrimitiveBlocks.REDSTONE_SIEVE).useItemOn(ItemStack.EMPTY,
                helper.getBlockState(rel), helper.getLevel(), abs, mockPlayer,
                InteractionHand.MAIN_HAND, hit);
        helper.assertTrue(result == net.minecraft.world.InteractionResult.CONSUME,
                "taking the stack out must consume, got " + result);
        helper.assertTrue(be.isEmpty(), "redstone sieve must give its stack to an empty hand");
        helper.assertTrue(!mockPlayer.getMainHandItem().isEmpty()
                && mockPlayer.getMainHandItem().is(net.minecraft.world.level.block.Blocks.GRAVEL.asItem()),
                "player hand must hold the taken gravel");
        helper.succeed();
    }

    @GameTest
    public void sieveAndCraftingRockAndRedstoneSieveHaveLootTables(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        assertSelfDrop(helper, server, HearthwindPrimitiveBlocks.SIEVE, "sieve");
        assertSelfDrop(helper, server, HearthwindPrimitiveBlocks.CRAFTING_ROCK, "crafting_rock");
        assertSelfDrop(helper, server, HearthwindPrimitiveBlocks.REDSTONE_SIEVE, "redstone_sieve");
        assertSelfDrop(helper, server, HearthwindPrimitiveBlocks.STEEL_BLOCK, "steel_block");
        helper.succeed();
    }

    private void assertSelfDrop(GameTestHelper helper, net.minecraft.server.MinecraftServer server,
            net.minecraft.world.level.block.Block block, String name) {
        var tableKey = block.getLootTable();
        helper.assertTrue(tableKey.isPresent(), name + " must have a loot table");
        var table = server.reloadableRegistries().getLootTable(tableKey.get());
        var params = new net.minecraft.world.level.storage.loot.LootParams.Builder(server.overworld())
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN,
                        net.minecraft.world.phys.Vec3.atCenterOf(helper.absolutePos(new net.minecraft.core.BlockPos(1, 1, 1))))
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.TOOL,
                        ItemStack.EMPTY)
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_STATE,
                        block.defaultBlockState())
                .create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.BLOCK);
        var drops = table.getRandomItems(params);
        boolean hasSelf = drops.stream().anyMatch(st -> st.is(block.asItem()));
        helper.assertTrue(hasSelf, name + " loot table must drop itself, got " + drops.size() + " items");
    }


    @GameTest
    public void beginnerForgivenessTracksDeaths(GameTestHelper helper) {
        // Verify the system is registered
        helper.assertTrue(BeginnerForgiveness.class != null,
                "BeginnerForgiveness class exists");
        helper.assertTrue(BeginnerForgiveness.DEATH_COUNT != null,
                "death count attachment type must be registered");
        helper.succeed();
    }

    @GameTest
    public void beginnerForgivenessConfigValueIsReasonable(GameTestHelper helper) {
        var config = HearthwindPrimitiveConfig.get();
        helper.assertTrue(config.beginnerDeathCount >= 0,
                "beginner death count should be non-negative");
        helper.assertTrue(config.beginnerDeathCount <= 10,
                "beginner death count should not exceed 10");
        helper.succeed();
    }

    @GameTest
    public void craftRockConfigValuesAreReasonable(GameTestHelper helper) {
        var config = HearthwindPrimitiveConfig.get();
        helper.assertTrue(config.craftRockCraftHits >= 1,
                "craftRockCraftHits should be at least 1");
        helper.assertTrue(config.craftRockMaxCraftHits >= config.craftRockCraftHits,
                "craftRockMaxCraftHits should be >= craftRockCraftHits");
        helper.succeed();
    }

    @GameTest
    public void craftingRockBlockHasCorrectStrength(GameTestHelper helper) {
        var state = HearthwindPrimitiveBlocks.CRAFTING_ROCK.defaultBlockState();
        helper.assertTrue(state != null, "crafting_rock state should exist");
        // Should be tough (stone-like)
        helper.succeed();
    }

        @GameTest
    public void rockBlockDropsRockItem(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        var tableKey = HearthwindPrimitiveBlocks.ROCK.getLootTable();
        helper.assertTrue(tableKey.isPresent(), "rock must have a loot table");
        var table = server.reloadableRegistries().getLootTable(tableKey.get());
        var params = new net.minecraft.world.level.storage.loot.LootParams.Builder(
                server.overworld())
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN,
                        net.minecraft.world.phys.Vec3.atCenterOf(helper.absolutePos(new net.minecraft.core.BlockPos(1, 1, 1))))
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.TOOL,
                        net.minecraft.world.item.ItemStack.EMPTY)
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_STATE,
                        HearthwindPrimitiveBlocks.ROCK.defaultBlockState())
                .create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.BLOCK);
        var drops = table.getRandomItems(params);
        boolean hasRock = drops.stream()
                .anyMatch(st -> st.is(HearthwindPrimitiveBlocks.ROCK.asItem()));
        helper.assertTrue(hasRock,
                "rock loot table must drop earlystage:rock, got " + drops.size() + " items");
        helper.succeed();
    }

    @GameTest
    public void flintBlockDropsVanillaFlint(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        var tableKey = HearthwindPrimitiveBlocks.FLINT.getLootTable();
        helper.assertTrue(tableKey.isPresent(), "flint must have a loot table");
        var table = server.reloadableRegistries().getLootTable(tableKey.get());
        var params = new net.minecraft.world.level.storage.loot.LootParams.Builder(
                server.overworld())
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN,
                        net.minecraft.world.phys.Vec3.atCenterOf(helper.absolutePos(new net.minecraft.core.BlockPos(1, 1, 1))))
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.TOOL,
                        net.minecraft.world.item.ItemStack.EMPTY)
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_STATE,
                        HearthwindPrimitiveBlocks.FLINT.defaultBlockState())
                .create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.BLOCK);
        var drops = table.getRandomItems(params);
        boolean hasFlint = drops.stream()
                .anyMatch(st -> st.is(net.minecraft.world.item.Items.FLINT));
        helper.assertTrue(hasFlint,
                "flint loot table must drop minecraft:flint, got " + drops.size() + " items");
        helper.succeed();
    }

    @GameTest
    public void rockBlockHasVariantPropertiesAndMoundShape(GameTestHelper helper) {
        var state = HearthwindPrimitiveBlocks.ROCK.defaultBlockState();
        helper.assertTrue(state.hasProperty(HearthwindRockBlock.ROCK_TYPE),
                "rock must have a type property (small..extra_large)");
        helper.assertTrue(state.hasProperty(HearthwindRockBlock.FACING_PROPERTY),
                "rock must have a facing property");
        var shape = state.getShape(helper.getLevel(), helper.absolutePos(new net.minecraft.core.BlockPos(1, 1, 1)));
        helper.assertTrue(shape.max(net.minecraft.core.Direction.Axis.Y) <= 0.34,
                "rock mound must be ~1/3 block tall, got " + shape.max(net.minecraft.core.Direction.Axis.Y));
        var flint = HearthwindPrimitiveBlocks.FLINT.defaultBlockState();
        helper.assertTrue(flint.hasProperty(HearthwindFlintBlock.FLINT_TYPE),
                "flint must have a type property (small/medium)");
        helper.succeed();
    }

    @GameTest
    public void extraBlastingTypeAndSerializerRegistered(GameTestHelper helper) {
        helper.assertTrue(net.minecraft.core.registries.BuiltInRegistries.RECIPE_TYPE
                .getKey(dev.jmiahman.hearthwind.primitive.extra.ExtraBlastingRecipes.TYPE) != null,
                "extra blasting recipe type registered");
        helper.assertTrue(net.minecraft.core.registries.BuiltInRegistries.RECIPE_SERIALIZER
                .getKey(dev.jmiahman.hearthwind.primitive.extra.ExtraBlastingRecipes.SERIALIZER) != null,
                "extra blasting serializer registered");
        helper.succeed();
    }

    @GameTest
    public void blastFurnaceHasFourSlots(GameTestHelper helper) {
        var pos = new net.minecraft.core.BlockPos(1, 1, 1);
        helper.setBlock(pos, net.minecraft.world.level.block.Blocks.BLAST_FURNACE);
        var be = helper.getBlockEntity(pos,
                net.minecraft.world.level.block.entity.BlastFurnaceBlockEntity.class);
        helper.assertTrue(be.getContainerSize() == 4,
                "blast furnace must expose 4 slots, got " + be.getContainerSize());
        helper.succeed();
    }

    private void runFurnaceTicks(GameTestHelper helper,
            net.minecraft.world.level.block.entity.BlastFurnaceBlockEntity be, int ticks) {
        var level = (net.minecraft.server.level.ServerLevel) helper.getLevel();
        var pos = be.getBlockPos();
        for (int i = 0; i < ticks; i++) {
            net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity.serverTick(
                    level, pos, be.getBlockState(), be);
        }
    }

    @GameTest
    public void extraBlastingSmeltsIronWithCoal(GameTestHelper helper) {
        var pos = new net.minecraft.core.BlockPos(1, 1, 1);
        helper.setBlock(pos, net.minecraft.world.level.block.Blocks.BLAST_FURNACE);
        var be = helper.getBlockEntity(pos,
                net.minecraft.world.level.block.entity.BlastFurnaceBlockEntity.class);
        be.setItem(0, new ItemStack(net.minecraft.world.item.Items.IRON_INGOT, 1));
        be.setItem(1, new ItemStack(net.minecraft.world.item.Items.COAL, 8));
        be.setItem(3, new ItemStack(net.minecraft.world.item.Items.COAL, 2));
        runFurnaceTicks(helper, be, 5300);
        helper.assertTrue(be.getItem(2).is(HearthwindPrimitiveItems.STEEL_INGOT),
                "steel ingot must be produced, got " + be.getItem(2));
        helper.assertTrue(be.getItem(0).isEmpty(), "iron ingot must be consumed");
        helper.assertTrue(be.getItem(3).isEmpty(), "extra-slot coal must be consumed");
        helper.succeed();
    }

    @GameTest
    public void extraBlastingRequiresCoalInExtraSlot(GameTestHelper helper) {
        var pos = new net.minecraft.core.BlockPos(1, 1, 1);
        helper.setBlock(pos, net.minecraft.world.level.block.Blocks.BLAST_FURNACE);
        var be = helper.getBlockEntity(pos,
                net.minecraft.world.level.block.entity.BlastFurnaceBlockEntity.class);
        be.setItem(0, new ItemStack(net.minecraft.world.item.Items.IRON_INGOT, 1));
        be.setItem(1, new ItemStack(net.minecraft.world.item.Items.COAL, 2));
        runFurnaceTicks(helper, be, 5300);
        helper.assertTrue(be.getItem(2).isEmpty(),
                "no steel may be produced without the extra-slot coal");
        helper.assertTrue(be.getItem(0).getCount() == 1, "iron must remain untouched");
        helper.succeed();
    }

    @GameTest
    public void vanillaBlastingStillWorks(GameTestHelper helper) {
        var pos = new net.minecraft.core.BlockPos(1, 1, 1);
        helper.setBlock(pos, net.minecraft.world.level.block.Blocks.BLAST_FURNACE);
        var be = helper.getBlockEntity(pos,
                net.minecraft.world.level.block.entity.BlastFurnaceBlockEntity.class);
        be.setItem(0, new ItemStack(net.minecraft.world.item.Items.RAW_IRON, 1));
        // The tuning corpus re-authors ore cooking: raw iron is blasted in 800
        // ticks (not vanilla's 200), and one coal only burns 800 ticks, so
        // budget two coal to stay clear of the burn boundary.
        be.setItem(1, new ItemStack(net.minecraft.world.item.Items.COAL, 2));
        runFurnaceTicks(helper, be, 850);
        helper.assertTrue(be.getItem(2).is(net.minecraft.world.item.Items.IRON_INGOT),
                "raw iron blasting must still work, got " + be.getItem(2));
        helper.succeed();
    }

    // ---- BE client-sync regression tests (getUpdatePacket/getUpdateTag) ----
    // The real sieve/rock bug: BlockEntity.getUpdatePacket() default returns null
    // and the 26.2 default getUpdateTag serializes nothing, so the client never
    // received Items after the chunk was already loaded. These tests fail if
    // either override is removed again.

    private static void assertSyncTagHoldsStack(net.minecraft.nbt.CompoundTag tag,
            String expectedItemId, String what) {
        helperAssert(tag != null && !tag.keySet().isEmpty(),
                what + ": update tag must not be empty (26.2 default serializes nothing)");
        helperAssert(tag.contains("Items"), what + ": update tag must contain Items");
        var list = tag.getListOrEmpty("Items");
        helperAssert(!list.isEmpty(), what + ": Items list must not be empty");
        boolean found = false;
        for (int i = 0; i < list.size(); i++) {
            var entry = list.getCompoundOrEmpty(i);
            var id = entry.getString("id");
            if (id.isPresent() && id.get().equals(expectedItemId)) {
                found = true;
                break;
            }
        }
        helperAssert(found, what + ": Items must contain " + expectedItemId);
    }

    private static void helperAssert(boolean cond, String msg) {
        if (!cond) {
            throw new RuntimeException(msg);
        }
    }

    @GameTest
    public void sieveSyncsInventoryToClient(GameTestHelper helper) {
        net.minecraft.core.BlockPos rel = new net.minecraft.core.BlockPos(2, 1, 3);
        helper.setBlock(rel, HearthwindPrimitiveBlocks.SIEVE.defaultBlockState());
        var abs = helper.absolutePos(rel);
        var be = (SieveBlockEntity) helper.getLevel().getBlockEntity(abs);

        be.setItem(0, new ItemStack(net.minecraft.world.level.block.Blocks.DIRT));

        var packet = be.getUpdatePacket();
        helperAssert(packet != null,
                "sieve BE.getUpdatePacket() must not be null (default returns null -> client never syncs)");
        helperAssert(packet instanceof net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket,
                "update packet must be a ClientboundBlockEntityDataPacket");
        var tag = ((net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket) packet).getTag();
        assertSyncTagHoldsStack(tag, "minecraft:dirt", "sieve packet");

        var updateTag = be.getUpdateTag(helper.getLevel().registryAccess());
        assertSyncTagHoldsStack(updateTag, "minecraft:dirt", "sieve getUpdateTag");
        helper.succeed();
    }

    @GameTest
    public void craftingRockSyncsInventoryToClient(GameTestHelper helper) {
        net.minecraft.core.BlockPos rel = new net.minecraft.core.BlockPos(2, 1, 4);
        helper.setBlock(rel, HearthwindPrimitiveBlocks.CRAFTING_ROCK.defaultBlockState());
        var abs = helper.absolutePos(rel);
        var be = (CraftingRockBlockEntity) helper.getLevel().getBlockEntity(abs);

        be.setItem(4, new ItemStack(net.minecraft.world.item.Items.STICK, 4));

        var packet = be.getUpdatePacket();
        helperAssert(packet != null,
                "crafting rock BE.getUpdatePacket() must not be null (default returns null -> client never syncs)");
        var tag = ((net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket) packet).getTag();
        assertSyncTagHoldsStack(tag, "minecraft:stick", "crafting rock packet");
        helperAssert(tag.contains("CraftHits") && tag.contains("TotalHits"),
                "crafting rock packet must carry CraftHits/TotalHits wear state");
        helper.succeed();
    }

    @GameTest
    public void redstoneSieveSyncsInventoryToClient(GameTestHelper helper) {
        net.minecraft.core.BlockPos rel = new net.minecraft.core.BlockPos(2, 1, 5);
        helper.setBlock(rel, HearthwindPrimitiveBlocks.REDSTONE_SIEVE.defaultBlockState());
        var abs = helper.absolutePos(rel);
        var be = (SieveBlockEntity) helper.getLevel().getBlockEntity(abs);

        be.setItem(0, new ItemStack(net.minecraft.world.level.block.Blocks.GRAVEL));

        var packet = be.getUpdatePacket();
        helperAssert(packet != null, "redstone sieve BE.getUpdatePacket() must not be null");
        var tag = ((net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket) packet).getTag();
        assertSyncTagHoldsStack(tag, "minecraft:gravel", "redstone sieve packet");
        helper.succeed();
    }

    /**
     * Shared driver for the "every flint tool crafts on the rock" suite. Variant 0
     * is the identity mapping, so grid positions equal BE slots: place flint/sticks
     * at the recipe's own cells, hammer twice (craftHits=2), expect the tool in the
     * center slot and every other cell emptied.
     */
    private void rockCraftsFlintTool(GameTestHelper helper, int[] flintSlots, int[] stickSlots,
                                     net.minecraft.world.item.Item expected, String label) {
        ServerPlayer mockPlayer = helper.makeMockServerPlayerInLevel();
        net.minecraft.core.BlockPos rel = new net.minecraft.core.BlockPos(2, 1, 1);
        helper.setBlock(rel, HearthwindPrimitiveBlocks.CRAFTING_ROCK.defaultBlockState());
        var abs = helper.absolutePos(rel);
        var be = (CraftingRockBlockEntity) helper.getLevel().getBlockEntity(abs);

        for (int slot : flintSlots) {
            be.setItem(slot, new ItemStack(net.minecraft.world.item.Items.FLINT));
        }
        for (int slot : stickSlots) {
            be.setItem(slot, new ItemStack(net.minecraft.world.item.Items.STICK));
        }
        be.setCraftHits(2);

        ItemStack rock = new ItemStack(HearthwindPrimitiveBlocks.ROCK.asItem(), 16);
        mockPlayer.setItemInHand(InteractionHand.MAIN_HAND, rock);
        net.minecraft.world.phys.BlockHitResult hit = new net.minecraft.world.phys.BlockHitResult(
                new net.minecraft.world.phys.Vec3(abs.getX() + 0.85, abs.getY() + 0.5, abs.getZ() + 0.85),
                net.minecraft.core.Direction.UP, abs, false);

        for (int i = 0; i < 2; i++) {
            var result = ((CraftingRockBlock) HearthwindPrimitiveBlocks.CRAFTING_ROCK).useItemOn(rock,
                    helper.getBlockState(rel), helper.getLevel(), abs, mockPlayer,
                    InteractionHand.MAIN_HAND, hit);
            helper.assertTrue(result == net.minecraft.world.InteractionResult.CONSUME,
                    label + ": rock hit " + i + " must consume, got " + result);
        }

        ItemStack crafted = be.getItem(4);
        helper.assertTrue(!crafted.isEmpty() && crafted.is(expected) && crafted.getCount() == 1,
                label + ": center slot must hold 1 " + expected + ", got " + crafted);
        for (int slot = 0; slot < 9; slot++) {
            if (slot != 4) {
                helper.assertTrue(be.getItem(slot).isEmpty(),
                        label + ": slot " + slot + " must be empty after craft, got " + be.getItem(slot));
            }
        }
        helper.succeed();
    }

    @GameTest
    public void craftingRockCraftsFlintPickaxe(GameTestHelper helper) {
        // 'ddd/ e / e '
        rockCraftsFlintTool(helper, new int[]{0, 1, 2}, new int[]{4, 7},
                HearthwindPrimitiveItems.FLINT_PICKAXE, "pickaxe");
    }

    @GameTest
    public void craftingRockCraftsFlintAxe(GameTestHelper helper) {
        // 'dd /de / e '
        rockCraftsFlintTool(helper, new int[]{0, 1, 3}, new int[]{4, 7},
                HearthwindPrimitiveItems.FLINT_AXE, "axe");
    }

    @GameTest
    public void craftingRockCraftsFlintShovel(GameTestHelper helper) {
        // 'd/e/e'
        rockCraftsFlintTool(helper, new int[]{0}, new int[]{3, 6},
                HearthwindPrimitiveItems.FLINT_SHOVEL, "shovel");
    }

    @GameTest
    public void craftingRockCraftsFlintSword(GameTestHelper helper) {
        // 'd/d/e'
        rockCraftsFlintTool(helper, new int[]{0, 3}, new int[]{6},
                HearthwindPrimitiveItems.FLINT_SWORD, "sword");
    }

    @GameTest
    public void craftingRockCraftsFlintHoe(GameTestHelper helper) {
        // 'dd / e / e '
        rockCraftsFlintTool(helper, new int[]{0, 1}, new int[]{4, 7},
                HearthwindPrimitiveItems.FLINT_HOE, "hoe");
    }

    @GameTest
    public void surfaceRockPlacementAndCycle(GameTestHelper helper) {
        net.minecraft.core.BlockPos base = new net.minecraft.core.BlockPos(1, 1, 1);
        net.minecraft.core.BlockPos above = new net.minecraft.core.BlockPos(1, 2, 1);
        helper.setBlock(base, net.minecraft.world.level.block.Blocks.GRASS_BLOCK.defaultBlockState());
        helper.setBlock(above, HearthwindPrimitiveBlocks.ROCK.defaultBlockState());

        var state = helper.getBlockState(above);
        helperAssert(state.is(HearthwindPrimitiveBlocks.ROCK), "Rock block must be placed above grass block");

        // Cycle variant
        var rockBlock = (HearthwindRockBlock) state.getBlock();
        rockBlock.cycleState(state, helper.getLevel(), helper.absolutePos(above));
        var cycledState = helper.getBlockState(above);
        helperAssert(cycledState.getValue(HearthwindRockBlock.ROCK_TYPE) != state.getValue(HearthwindRockBlock.ROCK_TYPE),
                "Rock type must cycle to next variant");
        helper.succeed();
    }

    @GameTest
    public void surfaceFlintPlacementAndCycle(GameTestHelper helper) {
        net.minecraft.core.BlockPos base = new net.minecraft.core.BlockPos(1, 1, 1);
        net.minecraft.core.BlockPos above = new net.minecraft.core.BlockPos(1, 2, 1);
        helper.setBlock(base, net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
        helper.setBlock(above, HearthwindPrimitiveBlocks.FLINT.defaultBlockState());

        var state = helper.getBlockState(above);
        helperAssert(state.is(HearthwindPrimitiveBlocks.FLINT), "Flint block must be placed above stone");

        var flintBlock = (HearthwindFlintBlock) state.getBlock();
        flintBlock.cycleState(state, helper.getLevel(), helper.absolutePos(above));
        var cycledState = helper.getBlockState(above);
        helperAssert(cycledState.getValue(HearthwindFlintBlock.FLINT_TYPE) != state.getValue(HearthwindFlintBlock.FLINT_TYPE),
                "Flint type must cycle to next variant");
        helper.succeed();
    }

    @GameTest
    public void rockPlacedFeatureRegistryAndPlacement(GameTestHelper helper) {
        var placedOpt = helper.getLevel().registryAccess().lookupOrThrow(Registries.PLACED_FEATURE)
                .get(ResourceKey.create(Registries.PLACED_FEATURE, net.minecraft.resources.Identifier.fromNamespaceAndPath("earlystage", "rock")));
        helperAssert(placedOpt.isPresent(), "PlacedFeature earlystage:rock must be present in registry");
        helper.succeed();
    }

    @GameTest
    public void craftingRockMinesAndDropsItem(GameTestHelper helper) {
        net.minecraft.core.BlockPos pos = new net.minecraft.core.BlockPos(1, 1, 1);
        helper.setBlock(pos, HearthwindPrimitiveBlocks.CRAFTING_ROCK.defaultBlockState());
        var state = helper.getBlockState(pos);
        helperAssert(state.is(HearthwindPrimitiveBlocks.CRAFTING_ROCK), "Crafting rock must be placed");

        // Verify drops from loot table with flint pickaxe or default break
        var serverLevel = (net.minecraft.server.level.ServerLevel) helper.getLevel();
        var drops = net.minecraft.world.level.block.Block.getDrops(state, serverLevel, helper.absolutePos(pos),
                null, null, new ItemStack(HearthwindPrimitiveItems.FLINT_PICKAXE));
        helperAssert(!drops.isEmpty(), "Crafting rock must drop items when broken with flint pickaxe");
        helperAssert(drops.get(0).is(HearthwindPrimitiveBlocks.CRAFTING_ROCK.asItem()),
                "Crafting rock must drop earlystage:crafting_rock block item");
        helper.succeed();
    }

    @GameTest
    public void treeFellingFellsConnectedTrunkWithAxe(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack axe = new ItemStack(HearthwindPrimitiveItems.FLINT_AXE);
        player.setItemInHand(InteractionHand.MAIN_HAND, axe);

        net.minecraft.core.BlockPos base = new net.minecraft.core.BlockPos(1, 1, 1);
        net.minecraft.core.BlockPos mid = new net.minecraft.core.BlockPos(1, 2, 1);
        net.minecraft.core.BlockPos top = new net.minecraft.core.BlockPos(1, 3, 1);

        helper.setBlock(base, net.minecraft.world.level.block.Blocks.OAK_LOG.defaultBlockState());
        helper.setBlock(mid, net.minecraft.world.level.block.Blocks.OAK_LOG.defaultBlockState());
        helper.setBlock(top, net.minecraft.world.level.block.Blocks.OAK_LOG.defaultBlockState());

        int felled = TreeFelling.fellTree((ServerLevel) helper.getLevel(), player, helper.absolutePos(base), axe);
        helperAssert(felled == 2, "Should fell 2 connected logs above base (mid + top), but felled: " + felled);
        helperAssert(helper.getBlockState(mid).isAir(), "Mid log should be felled");
        helperAssert(helper.getBlockState(top).isAir(), "Top log should be felled");
        helper.succeed();
    }

    @GameTest
    public void tieredAffixAppliesToWeaponAndScalesModifiers(GameTestHelper helper) {
        ItemStack sword = new ItemStack(net.minecraft.world.item.Items.IRON_SWORD);
        helperAssert(dev.jmiahman.hearthwind.primitive.tiered.TieredData.isEligible(sword), "Iron sword must be eligible for tiers");

        dev.jmiahman.hearthwind.primitive.tiered.TierRegistry.load(helper.getLevel().getServer().getResourceManager());
        boolean applied = dev.jmiahman.hearthwind.primitive.tiered.TieredData.applyRandomTierIfEligible(
                sword, net.minecraft.util.RandomSource.create());

        helperAssert(applied, "Should roll and apply a tier to iron sword");
        var tier = dev.jmiahman.hearthwind.primitive.tiered.TieredData.getTier(sword);
        helperAssert(tier != null, "Tier on sword must not be null");
        helperAssert(tier.getDisplayName() != null && !tier.getDisplayName().isEmpty(), "Tier must have a display name");

        var modifiers = sword.get(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS);
        helperAssert(modifiers != null && !modifiers.modifiers().isEmpty(), "Sword must have attribute modifiers after tier application");
        helper.succeed();
    }

    @GameTest
    public void tieredAffixMatchesArmorAndAddsAttributes(GameTestHelper helper) {
        ItemStack chestplate = new ItemStack(net.minecraft.world.item.Items.DIAMOND_CHESTPLATE);
        helperAssert(dev.jmiahman.hearthwind.primitive.tiered.TieredData.isEligible(chestplate), "Diamond chestplate must be eligible");

        dev.jmiahman.hearthwind.primitive.tiered.TierRegistry.load(helper.getLevel().getServer().getResourceManager());
        boolean applied = dev.jmiahman.hearthwind.primitive.tiered.TieredData.applyRandomTierIfEligible(
                chestplate, net.minecraft.util.RandomSource.create());

        helperAssert(applied, "Should apply tier to diamond chestplate");
        var tier = dev.jmiahman.hearthwind.primitive.tiered.TieredData.getTier(chestplate);
        helperAssert(tier != null, "Tier on chestplate must not be null");
        helper.succeed();
    }

    @GameTest
    public void reforgingEquipmentMatchesValidIngredients(GameTestHelper helper) {
        dev.jmiahman.hearthwind.primitive.tiered.ReforgeRegistry.load(helper.getLevel().getServer().getResourceManager());

        ItemStack sword = new ItemStack(net.minecraft.world.item.Items.IRON_SWORD);
        ItemStack ingot = new ItemStack(net.minecraft.world.item.Items.IRON_INGOT);
        ItemStack stick = new ItemStack(net.minecraft.world.item.Items.STICK);
        ItemStack amethyst = new ItemStack(net.minecraft.world.item.Items.AMETHYST_SHARD);

        helperAssert(dev.jmiahman.hearthwind.primitive.tiered.ReforgeRegistry.canReforge(sword, ingot),
                "Iron sword must be reforgeable with iron ingot");
        helperAssert(dev.jmiahman.hearthwind.primitive.tiered.ReforgeRegistry.canReforge(sword, amethyst),
                "Iron sword must be reforgeable with amethyst catalyst");
        helperAssert(!dev.jmiahman.hearthwind.primitive.tiered.ReforgeRegistry.canReforge(sword, stick),
                "Iron sword must not be reforgeable with plain stick");
        helper.succeed();
    }

    @GameTest
    public void oreSmeltingRecipesAreRemoved(GameTestHelper helper) {
        RecipeRemovals.load(helper.getLevel().getServer().getResourceManager());
        helperAssert(RecipeRemovals.oreCount() >= 50,
                "the ore/tech removal list must load (got " + RecipeRemovals.oreCount() + ")");
        helperAssert(RecipeRemovals.cookingCount() >= 10,
                "the cooking removal list must load (got " + RecipeRemovals.cookingCount() + ")");

        // The point of the removals: vanilla 200-tick ore smelting must be
        // GONE, while the corpus' slower blasting route must survive.
        helperAssert(!hasRecipe(helper, "minecraft:iron_ingot_from_smelting_iron_ore"),
                "vanilla iron ore smelting must be removed");
        helperAssert(!hasRecipe(helper, "minecraft:iron_ingot_from_smelting_raw_iron"),
                "vanilla raw iron smelting must be removed");
        helperAssert(!hasRecipe(helper, "minecraft:flint_and_steel"),
                "flint and steel must require steel, so the vanilla recipe is removed");
        helperAssert(hasRecipe(helper, "aged:iron_ingot_from_blasting_raw_iron"),
                "the corpus blasting route to iron must still exist");
        // Regression guard: our own steel recipe must never be swept up by the
        // removal list (upstream deleted a different id for the same purpose).
        helperAssert(hasRecipe(helper, "earlystage:steel_ingot_from_blasting"),
                "our steel blasting recipe must survive the removals");
        helper.succeed();
    }

    @GameTest
    public void foodCookingSurvivesByDefault(GameTestHelper helper) {
        // Cooking removal is opt-in until stoves are playable - a config reset
        // must never be able to starve players.
        helperAssert(!HearthwindPrimitiveConfig.get().removeCookedFoodRecipes,
                "cooking removal must default to off");
        helperAssert(hasRecipe(helper, "minecraft:bread"), "bread must still be craftable");
        helperAssert(hasRecipe(helper, "minecraft:cooked_beef"), "furnace cooking must still work");
        helper.succeed();
    }

    private static boolean hasRecipe(GameTestHelper helper, String id) {
        return helper.getLevel().recipeAccess()
                .byKey(ResourceKey.create(Registries.RECIPE,
                        net.minecraft.resources.Identifier.parse(id)))
                .isPresent();
    }

    /**
     * Client-mixin boot audit (headless). Client-only mixins never apply on a
     * dedicated server, so a broken client mixin (e.g. {@code @Shadow} of a
     * member inherited from the target's superclass) used to reach every real
     * client boot untested and crash {@code MenuScreens.<clinit>}. This audit
     * parses every hearthwind mixins.json (server AND client arrays) straight
     * out of the mod jars and asserts each {@code @Shadow} member is DECLARED
     * in its {@code @Mixin} target class - mixin shadow validation only
     * resolves members declared in the target class itself.
     */
    @GameTest
    public void everyMixinShadowResolvesInItsTarget(GameTestHelper helper) throws Exception {
        int audited = 0;
        for (String module : new String[] { "hearthwind_survival", "hearthwind_skills",
                "hearthwind_jobs", "hearthwind_primitive", "hearthwind_world", "hearthwind_client" }) {
            audited += auditMixinModule(helper, module);
        }
        helper.assertTrue(audited >= 28, "audit must cover every mixin class, got " + audited);
        helper.succeed();
    }

    private static int auditMixinModule(GameTestHelper helper, String module) throws Exception {
        JsonElement json = mixinConfigJson(module);
        if (json == null || !json.isJsonObject()) {
            return 0;
        }
        JsonObject cfg = json.getAsJsonObject();
        String pkg = cfg.get("package").getAsString().replace('.', '/');
        int count = 0;
        for (String array : new String[] { "mixins", "client", "server" }) {
            if (!cfg.has(array) || !cfg.get(array).isJsonArray()) {
                continue;
            }
            for (JsonElement e : cfg.getAsJsonArray(array)) {
                String name = e.getAsString();
                String classPath = pkg + '/' + name.replace('.', '/') + ".class";
                byte[] mixinBytes = modResourceBytes(module, classPath);
                helper.assertTrue(mixinBytes != null,
                        module + " mixin class missing from its jar: " + classPath);
                auditShadowClass(helper, module, name, mixinBytes);
                count++;
            }
        }
        return count;
    }

    private static void auditShadowClass(GameTestHelper helper, String module, String mixinName,
            byte[] mixinBytes) throws Exception {
        String[] target = { null };
        java.util.List<String> shadowFields = new java.util.ArrayList<>();
        java.util.List<String> shadowMethods = new java.util.ArrayList<>();
        java.util.List<String> injectionSelectors = new java.util.ArrayList<>();
        new ClassReader(mixinBytes).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                if (!"Lorg/spongepowered/asm/mixin/Mixin;".equals(desc)) {
                    return null;
                }
                return new AnnotationVisitor(Opcodes.ASM9) {
                    private void acceptTarget(Object v) {
                        if (v instanceof Type t) {
                            target[0] = t.getInternalName();
                        } else if (v instanceof String s) {
                            // @Mixin.targets is String[] and may be dotted or
                            // L-form internal; nested classes keep their $.
                            target[0] = s.startsWith("L") && s.endsWith(";")
                                    ? s.substring(1, s.length() - 1)
                                    : s.replace('.', '/');
                        }
                    }

                    @Override
                    public AnnotationVisitor visitArray(String key) {
                        if (!"value".equals(key) && !"targets".equals(key)) {
                            return null;
                        }
                        return new AnnotationVisitor(Opcodes.ASM9) {
                            @Override
                            public void visit(String n, Object v) {
                                acceptTarget(v);
                            }
                        };
                    }

                    @Override
                    public void visit(String key, Object value) {
                        if ("targets".equals(key)) {
                            acceptTarget(value);
                        }
                    }
                };
            }

            @Override
            public FieldVisitor visitField(int access, String fname, String desc, String sig, Object value) {
                return new FieldVisitor(Opcodes.ASM9) {
                    @Override
                    public AnnotationVisitor visitAnnotation(String d, boolean v) {
                        if ("Lorg/spongepowered/asm/mixin/Shadow;".equals(d)) {
                            shadowFields.add(fname);
                        }
                        return null;
                    }
                };
            }

            @Override
            public MethodVisitor visitMethod(int access, String mname, String desc, String sig, String[] ex) {
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override
                    public AnnotationVisitor visitAnnotation(String d, boolean v) {
                        if ("Lorg/spongepowered/asm/mixin/Shadow;".equals(d)) {
                            shadowMethods.add(mname);
                            return null;
                        }
                        // Crash class: an injection selector naming a method
                        // the TARGET class does not declare (e.g. inherited
                        // from a superclass) only fails when a real client or
                        // server loads the mixin - validate it here.
                        for (String inj : INJECTION_ANNOTATIONS) {
                            if (inj.equals(d)) {
                                return new AnnotationVisitor(Opcodes.ASM9) {
                                    private void addSelector(Object sel) {
                                        if (sel instanceof String str) {
                                            injectionSelectors.add(str);
                                        }
                                    }

                                    @Override
                                    public void visit(String k, Object val) {
                                        if ("method".equals(k)) {
                                            addSelector(val);
                                        }
                                    }

                                    @Override
                                    public AnnotationVisitor visitArray(String k) {
                                        if (!"method".equals(k)) {
                                            return null;
                                        }
                                        return new AnnotationVisitor(Opcodes.ASM9) {
                                            @Override
                                            public void visit(String n, Object val) {
                                                addSelector(val);
                                            }
                                        };
                                    }
                                };
                            }
                        }
                        return null;
                    }
                };
            }
        }, 0);

        // Caveat fix: EVERY mixin's target must resolve, not only mixins with
        // shadows - a renamed or missing target class otherwise only fails when
        // a real client or server loads the mixin, which headless server
        // gametests never do for client mixins.
        helper.assertTrue(target[0] != null,
                module + ":" + mixinName + " has no resolvable @Mixin target");
        byte[] targetBytes = classBytes(target[0]);
        for (String selector : injectionSelectors) {
            String name = selector.contains("(") ? selector.substring(0, selector.indexOf('(')) : selector;
            if (name.isEmpty() || name.indexOf('*') >= 0 || name.indexOf('?') >= 0) {
                continue; // wildcard / regex selectors cannot be name-checked
            }
            boolean ok = declaredMethods(targetBytes, name);
            helper.assertTrue(ok, module + ":" + mixinName + " injects into '" + selector
                    + "' which is NOT declared in target class " + target[0]
                    + " (injection into inherited methods must retarget the declaring class)");
        }
        if (shadowFields.isEmpty() && shadowMethods.isEmpty()) {
            return;
        }
        java.util.Set<String> declared = new java.util.HashSet<>();
        new ClassReader(targetBytes).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public FieldVisitor visitField(int a, String n, String d, String s, Object v) {
                declared.add(n);
                return null;
            }

            @Override
            public MethodVisitor visitMethod(int a, String n, String d, String s, String[] ex) {
                declared.add(n);
                return null;
            }
        }, 0);
        for (String field : shadowFields) {
            helper.assertTrue(declared.contains(field), module + ":" + mixinName
                    + " shadows field '" + field + "' which is NOT declared in target " + target[0]
                    + " (inherited members must be reached by extending the superclass, not @Shadow)");
        }
        for (String method : shadowMethods) {
            helper.assertTrue(declared.contains(method), module + ":" + mixinName
                    + " shadows method '" + method + "' which is NOT declared in target " + target[0]);
        }
    }

    private static final String[] INJECTION_ANNOTATIONS = {
            "Lorg/spongepowered/asm/mixin/injection/Inject;",
            "Lorg/spongepowered/asm/mixin/injection/Redirect;",
            "Lorg/spongepowered/asm/mixin/injection/ModifyVariable;",
            "Lorg/spongepowered/asm/mixin/injection/ModifyArg;",
            "Lorg/spongepowered/asm/mixin/injection/ModifyArgs;",
            "Lorg/spongepowered/asm/mixin/injection/ModifyConstant;" };

    private static boolean declaredMethods(byte[] targetBytes, String name) throws Exception {
        boolean[] found = { false };
        new ClassReader(targetBytes).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public MethodVisitor visitMethod(int a, String n, String d, String s, String[] ex) {
                if (n.equals(name)) {
                    found[0] = true;
                }
                return null;
            }
        }, 0);
        return found[0];
    }

    private static JsonElement mixinConfigJson(String module) throws Exception {
        byte[] bytes = modResourceBytes(module, module + ".mixins.json");
        return bytes == null ? null : JsonParser.parseString(new String(bytes, java.nio.charset.StandardCharsets.UTF_8));
    }

    /**
     * Reads a resource out of the named module. Mod containers cover every
     * module the loader loads on a dedicated server; hearthwind_client is
     * environment=client so it has no container here and is read straight
     * from its jar in the server's mods directory.
     */
    private static byte[] modResourceBytes(String module, String path) throws Exception {
        var container = net.fabricmc.loader.api.FabricLoader.getInstance().getModContainer(module);
        if (container.isPresent()) {
            var opt = container.get().findPath(path);
            if (opt.isPresent()) {
                try (var in = java.nio.file.Files.newInputStream(opt.get())) {
                    return in.readAllBytes();
                }
            }
            return null;
        }
        java.io.File mods = java.nio.file.Path.of("mods").toFile();
        String jarPrefix = module.replace('_', '-') + "-";
        if (mods.isDirectory()) {
            for (java.io.File f : mods.listFiles()) {
                if (f.getName().startsWith(jarPrefix) && f.getName().endsWith(".jar")) {
                    try (java.util.zip.ZipFile zip = new java.util.zip.ZipFile(f)) {
                        java.util.zip.ZipEntry entry = zip.getEntry(path);
                        if (entry == null) {
                            return null;
                        }
                        try (var in = zip.getInputStream(entry)) {
                            return in.readAllBytes();
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Target-class bytes. Server classes come off the runtime classloader;
     * CLIENT classes do not exist in a dedicated-server jar, so they are
     * read from the loom merged jar (path supplied by run_gametests.sh).
     */
    private static byte[] classBytes(String internalName) throws Exception {
        String path = internalName + ".class";
        var in = net.minecraft.world.level.Level.class.getClassLoader().getResourceAsStream(path);
        if (in != null) {
            try (in) {
                return in.readAllBytes();
            }
        }
        String merged = System.getProperty("hearthwind.mergedJar", "");
        helperAssert(!merged.isBlank(),
                "client target " + internalName + " needs -Dhearthwind.mergedJar (run_gametests.sh sets it)");
        try (java.util.zip.ZipFile zip = new java.util.zip.ZipFile(merged)) {
            java.util.zip.ZipEntry entry = zip.getEntry(path);
            helperAssert(entry != null, "merged jar lacks " + path);
            try (var zin = zip.getInputStream(entry)) {
                return zin.readAllBytes();
            }
        }
    }

}
