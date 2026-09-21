package snownee.passablefoliage;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;

/**
 * Headless gametests for the Passable Foliage 26.2 fork-port: leaves report
 * no collision (entity and non-entity contexts), a player entity falls
 * through a leaves platform, the Leaf Walker effect restores stand-on-top
 * collision, and config defaults match upstream tuning exactly.
 */
public final class PassableFoliageGameTests {
	public PassableFoliageGameTests() {}

	@GameTest
	public void leavesHaveNoCollisionShape(GameTestHelper helper) {
		BlockPos rel = new BlockPos(2, 64, 2);
		helper.setBlock(rel, Blocks.OAK_LEAVES.defaultBlockState());
		BlockState leaves = helper.getBlockState(rel);
		BlockPos abs = helper.absolutePos(rel);

		helper.assertTrue(PassableFoliage.isPassable(leaves), "oak leaves must be passable (passables tag)");
		helper.assertTrue(
				leaves.getCollisionShape(helper.getLevel(), abs).isEmpty(),
				"leaves collision shape (no entity) must be empty");
		Player player = helper.makeMockServerPlayer(GameType.SURVIVAL);
		helper.assertTrue(
				leaves.getCollisionShape(helper.getLevel(), abs, CollisionContext.of(player)).isEmpty(),
				"leaves collision shape (player context) must be empty");

		// Control: stone is untouched.
		BlockPos stoneRel = new BlockPos(4, 64, 2);
		helper.setBlock(stoneRel, Blocks.STONE.defaultBlockState());
		BlockState stone = helper.getBlockState(stoneRel);
		helper.assertFalse(PassableFoliage.isPassable(stone), "stone must not be passable");
		helper.assertFalse(
				stone.getCollisionShape(helper.getLevel(), helper.absolutePos(stoneRel)) == Shapes.empty()
						|| stone.getCollisionShape(helper.getLevel(), helper.absolutePos(stoneRel)).isEmpty(),
				"stone collision shape must be solid");
		helper.succeed();
	}

	@GameTest(maxTicks = 400)
	public void playerFallsThroughLeaves(GameTestHelper helper) {
		// 3x3 oak-leaves platform at y=66 (relative), open air above/below.
		for (int dx = 1; dx <= 3; dx++) {
			for (int dz = 1; dz <= 3; dz++) {
				helper.setBlock(new BlockPos(dx, 66, dz), Blocks.OAK_LEAVES.defaultBlockState());
			}
		}
		// Non-deprecated survival mock player (makeMockServerPlayerInLevel is
		// deprecated AND its player never ticks physics in 26.2 - observed
		// frozen at the teleport point for 400 ticks).
		Player player = helper.makeMockServerPlayer(GameType.SURVIVAL);
		BlockPos spawnAbs = helper.absolutePos(new BlockPos(2, 72, 2));
		player.setPos(spawnAbs.getX() + 0.5, spawnAbs.getY(), spawnAbs.getZ() + 0.5);
		player.setDeltaMovement(0, 0, 0);
		final double startY = player.getY();
		PassableFoliage.LOGGER.info(
				"PFTEST platform={} spawnAbs={} playerStart={}",
				helper.absolutePos(new BlockPos(2, 66, 2)),
				spawnAbs,
				player.position());

		// The player must fall 8+ blocks below the teleport point: the
		// platform sits 6 below it, so only true pass-through (collision
		// gone; entityInside merely slows + softens) gets there. Resting on
		// solid leaves would stop 5 above the start point. The poll drives
		// the mock player's movement directly via LivingEntity.travel (real
		// vanilla gravity + move + collision code): the level does not tick
		// mock players on its own, and full ServerPlayer.tick needs a live
		// network connection.
		helper.succeedWhen(() -> {
			player.travel(Vec3.ZERO);
			helper.assertTrue(
					player.getY() < startY - 8.0,
					"player must fall through the leaves platform, y=" + player.getY() + " start=" + startY);
		});
	}

	@GameTest(maxTicks = 400)
	public void mobFallsThroughLeaves(GameTestHelper helper) {
		// Same proof with a non-creative LivingEntity (no mock-player quirks).
		for (int dx = 1; dx <= 3; dx++) {
			for (int dz = 1; dz <= 3; dz++) {
				helper.setBlock(new BlockPos(dx, 66, dz), Blocks.OAK_LEAVES.defaultBlockState());
			}
		}
		var pig = helper.spawn(EntityTypes.PIG, 2, 72, 2);
		final double startY = pig.getY();
		helper.succeedWhen(() -> {
			helper.assertTrue(
					pig.getY() < startY - 8.0,
					"pig must fall through the leaves platform, y=" + pig.getY() + " start=" + startY);
		});
	}

	@GameTest
	public void leafWalkerRestoresStandingCollision(GameTestHelper helper) {
		BlockPos rel = new BlockPos(2, 64, 2);
		helper.setBlock(rel, Blocks.OAK_LEAVES.defaultBlockState());
		BlockState leaves = helper.getBlockState(rel);
		BlockPos abs = helper.absolutePos(rel);

		Player player = helper.makeMockServerPlayer(GameType.SURVIVAL);
		helper.assertFalse(PassableFoliage.hasLeafWalker(player), "unenchanted boots must not grant leaf walking");

		// Enchant properly (end-to-end: our data enchantment JSON + the
		// registered leaf_walker effect component). NOTE: setting the
		// component directly on the boots is NOT enough - EnchantmentHelper
		// reads effects granted by enchantments, exactly like upstream.
		var enchLookup = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
		var leafWalker = enchLookup.getOrThrow(
				ResourceKey.create(Registries.ENCHANTMENT, PassableFoliage.id("leaf_walker")));
		ItemStack boots = new ItemStack(Items.DIAMOND_BOOTS);
		boots.enchant(leafWalker, 1);
		player.setItemSlot(EquipmentSlot.FEET, boots);
		helper.assertTrue(PassableFoliage.hasLeafWalker(player), "leaf_walker boots must grant leaf walking");

		// Standing above the leaves, not descending: vanilla (solid) shape
		// returns, so leaf walkers can stand on top.
		BlockPos aboveAbs = abs.above(2);
		player.setPos(aboveAbs.getX() + 0.5, aboveAbs.getY(), aboveAbs.getZ() + 0.5);
		player.setDeltaMovement(0, 0, 0);
		helper.assertFalse(
				leaves.getCollisionShape(helper.getLevel(), abs, CollisionContext.of(player)).isEmpty(),
				"leaf walker above leaves must get a solid shape (can stand)");
		helper.succeed();
	}

	@GameTest
	public void configDefaultsMatchUpstream(GameTestHelper helper) {
		helper.assertTrue(PassableFoliageCommonConfig.fallDamageMultiplier == 0.5f, "fallDamageMultiplier default 0.5");
		helper.assertTrue(PassableFoliageCommonConfig.fallDamageThreshold == 20, "fallDamageThreshold default 20");
		helper.assertTrue(
				PassableFoliageCommonConfig.speedMultiplierHorizontal == 0.9f,
				"speedMultiplierHorizontal default 0.9");
		helper.assertTrue(
				PassableFoliageCommonConfig.speedMultiplierVertical == 0.9f, "speedMultiplierVertical default 0.9");
		helper.assertTrue(PassableFoliageCommonConfig.modifyPathFinding, "modifyPathFinding default true");
		helper.assertFalse(PassableFoliageCommonConfig.playerOnly, "playerOnly default false");
		helper.assertTrue(
				PassableFoliageCommonConfig.alwaysNotViewBlocking, "alwaysNotViewBlocking default true");
		helper.assertFalse(PassableFoliageCommonConfig.alwaysLeafWalking, "alwaysLeafWalking default false");
		helper.assertFalse(PassableFoliageCommonConfig.headHitter, "headHitter default false");
		helper.assertFalse(PassableFoliageCommonConfig.soundsPlayerOnly, "soundsPlayerOnly default false");
		helper.assertTrue(PassableFoliageCommonConfig.soundVolume == 1.0f, "soundVolume default 1");
		helper.succeed();
	}
}
