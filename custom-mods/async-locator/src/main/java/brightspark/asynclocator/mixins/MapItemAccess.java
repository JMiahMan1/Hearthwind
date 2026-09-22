package brightspark.asynclocator.mixins;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapId;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MapItem.class)
public interface MapItemAccess {
	/**
	 * Creates and stores new map saved data, then writes MAP_ID onto the stack.
	 * 26.2 renamed createAndStoreSavedData - returns void after setting MAP_ID via invoker of createNewSavedData + set.
	 */
	@Invoker("createNewSavedData")
	static MapId callCreateNewSavedData(
		ServerLevel level,
		int x,
		int z,
		int scale,
		boolean trackingPosition,
		boolean unlimitedTracking,
		ResourceKey<Level> dimension
	) {
		throw new UnsupportedOperationException();
	}
}
