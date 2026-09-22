package brightspark.asynclocator.logic;

import brightspark.asynclocator.ALConstants;
import brightspark.asynclocator.AsyncLocator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;

public class ExplorationMapFunctionLogic {
	private ExplorationMapFunctionLogic() {}

	public static void handleLocationFound(
		ItemStack mapStack,
		ServerLevel level,
		BlockPos pos,
		int scale,
		Holder<MapDecorationType> destinationType,
		BlockPos invPos
	) {
		if (pos == null) {
			ALConstants.logInfo("No location found - invalidating map stack");
			invalidateMap(mapStack, level, invPos);
		} else {
			ALConstants.logInfo("Location found - updating treasure map in chest");
			CommonLogic.updateMap(mapStack, level, pos, scale, destinationType, (net.minecraft.network.chat.Component) null);
			CommonLogic.broadcastChestChanges(level, level.getBlockEntity(invPos));
		}
	}

	public static ItemStack updateMapAsync(
		ServerLevel level,
		BlockPos blockPos,
		int scale,
		int searchRadius,
		boolean skipKnownStructures,
		Holder<MapDecorationType> destinationType,
		TagKey<Structure> destination
	) {
		ItemStack mapStack = CommonLogic.createEmptyMap();
		AsyncLocator.locate(level, destination, blockPos, searchRadius, skipKnownStructures)
			.thenOnServerThread(pos -> handleLocationFound(mapStack, level, pos, scale, destinationType, blockPos));
		return mapStack;
	}

	private static void invalidateMap(ItemStack mapStack, ServerLevel level, BlockPos invPos) {
		net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(invPos);
		if (be instanceof net.minecraft.world.level.block.entity.ChestBlockEntity chest) {
			for (int i = 0; i < chest.getContainerSize(); i++) {
				if (chest.getItem(i) == mapStack) {
					chest.setItem(i, new ItemStack(net.minecraft.world.item.Items.MAP));
					CommonLogic.broadcastChestChanges(level, be);
					return;
				}
			}
		} else {
			ALConstants.logWarn(
				"Couldn't find chest block entity on block {} at {}",
				level.getBlockState(invPos), invPos
			);
		}
	}
}
