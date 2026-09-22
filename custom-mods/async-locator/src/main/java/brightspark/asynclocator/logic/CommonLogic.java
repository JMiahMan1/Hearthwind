package brightspark.asynclocator.logic;

import brightspark.asynclocator.mixins.MapItemAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.core.Holder;

public class CommonLogic {
	private static final String MAP_HOVER_NAME_KEY = "menu.working";
	private static final String KEY_LOCATING = "asynclocator.locating";

	private CommonLogic() {}

	/**
	 * Creates an empty "Filled Map", with a hover tooltip name stating that it's locating a feature.
	 */
	public static ItemStack createEmptyMap() {
		ItemStack stack = new ItemStack(Items.FILLED_MAP);
		stack.set(DataComponents.CUSTOM_NAME, Component.translatable(MAP_HOVER_NAME_KEY));
		CompoundTag tag = new CompoundTag();
		tag.putBoolean(KEY_LOCATING, true);
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		return stack;
	}

	/**
	 * Returns true if the stack is an empty FILLED_MAP awaiting location data.
	 */
	public static boolean isEmptyPendingMap(ItemStack stack) {
		if (!stack.is(Items.FILLED_MAP)) return false;
		CustomData data = stack.get(DataComponents.CUSTOM_DATA);
		return data != null && data.copyTag().contains(KEY_LOCATING);
	}

	public static void updateMap(
		ItemStack mapStack,
		ServerLevel level,
		BlockPos pos,
		int scale,
		Holder<MapDecorationType> destinationType
	) {
		updateMap(mapStack, level, pos, scale, destinationType, (Component) null);
	}

	public static void updateMap(
		ItemStack mapStack,
		ServerLevel level,
		BlockPos pos,
		int scale,
		Holder<MapDecorationType> destinationType,
		String displayName
	) {
		updateMap(mapStack, level, pos, scale, destinationType, Component.translatable(displayName));
	}

	public static void updateMap(
		ItemStack mapStack,
		ServerLevel level,
		BlockPos pos,
		int scale,
		Holder<MapDecorationType> destinationType,
		Component displayName
	) {
		MapId mapId = MapItemAccess.callCreateNewSavedData(
			level, pos.getX(), pos.getZ(), scale, true, true, level.dimension()
		);
		mapStack.set(DataComponents.MAP_ID, mapId);
		MapItem.renderBiomePreviewMap(level, mapStack);
		MapItemSavedData.addTargetDecoration(mapStack, pos, "+", destinationType);
		if (displayName != null)
			mapStack.set(DataComponents.CUSTOM_NAME, displayName);
		mapStack.remove(DataComponents.CUSTOM_DATA);
	}

	/**
	 * Broadcasts slot changes to all players that have the chest container open.
	 */
	public static void broadcastChestChanges(ServerLevel level, BlockEntity be) {
		if (!(be instanceof ChestBlockEntity))
			return;

		level.players().forEach(player -> {
			AbstractContainerMenu container = player.containerMenu;
			if (container instanceof ChestMenu chestMenu && chestMenu.getContainer() == be) {
				chestMenu.broadcastChanges();
			}
		});
	}
}
