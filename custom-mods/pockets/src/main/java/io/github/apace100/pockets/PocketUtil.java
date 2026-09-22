package io.github.apace100.pockets;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class PocketUtil {

	public static final int MAX_OCCUPANCY = 64;

	public static boolean isAllowedInPockets(ItemStack stack) {
		return !stack.is(Pockets.BLACKLIST_TAG);
	}

	public static boolean hasPockets(ItemStack stack) {
		return stack.is(Pockets.HAS_POCKET_TAG);
	}

	public static ItemContainerContents getContents(ItemStack pocketItem) {
		return pocketItem.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
	}

	private static void setContents(ItemStack pocketItem, ItemContainerContents contents) {
		pocketItem.set(DataComponents.CONTAINER, contents);
	}

	private static void clearContents(ItemStack pocketItem) {
		pocketItem.remove(DataComponents.CONTAINER);
	}

	public static Stream<ItemStack> getPocketedStacks(ItemStack pocketItem) {
		return getContents(pocketItem).nonEmptyItemCopyStream();
	}

	public static int addToPockets(ItemStack pocketItem, ItemStack stack) {
		if (stack.isEmpty() || !canNest(stack)) {
			return 0;
		}

		int occupancy = getPocketOccupancy(pocketItem);
		int itemOccupancy = getItemOccupancy(stack);
		int maxAdd = Math.min(stack.getCount(), (MAX_OCCUPANCY - occupancy) / itemOccupancy);
		if (maxAdd <= 0) {
			return 0;
		}

		List<ItemStack> items = getPocketedStacks(pocketItem).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
		int remaining = maxAdd;

		if (!stack.is(Items.BUNDLE)) {
			for (int i = 0; i < items.size() && remaining > 0; i++) {
				ItemStack existing = items.get(i);
				if (existing.isEmpty() || !ItemStack.isSameItemSameComponents(existing, stack)) {
					continue;
				}
				int space = Math.min(64 - existing.getCount(), remaining);
				if (space <= 0) {
					continue;
				}
				existing.grow(space);
				remaining -= space;
			}
		}

		if (remaining > 0) {
			ItemStack copy = stack.copy();
			copy.setCount(remaining);
			items.add(copy);
			remaining = 0;
		}

		setContents(pocketItem, ItemContainerContents.fromItems(items));
		return maxAdd;
	}

	public static Optional<ItemStack> removeFirstStack(ItemStack pocketItem) {
		List<ItemStack> items = getPocketedStacks(pocketItem).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
		if (items.isEmpty()) {
			return Optional.empty();
		}
		ItemStack first = items.remove(0);
		if (items.isEmpty()) {
			clearContents(pocketItem);
		} else {
			setContents(pocketItem, ItemContainerContents.fromItems(items));
		}
		return Optional.of(first);
	}

	public static boolean dropAllPocketedItems(ItemStack pocketItem, Player player) {
		List<ItemStack> items = getPocketedStacks(pocketItem).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
		if (items.isEmpty()) {
			return false;
		}
		for (ItemStack item : items) {
			player.drop(item, false);
		}
		clearContents(pocketItem);
		return true;
	}

	public static boolean dropAllPocketedItems(ItemStack pocketItem, ItemEntity entity) {
		List<ItemStack> items = getPocketedStacks(pocketItem).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
		if (items.isEmpty()) {
			return false;
		}
		for (ItemStack item : items) {
			entity.level().addFreshEntity(new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), item));
		}
		clearContents(pocketItem);
		return true;
	}

	public static int getItemOccupancy(ItemStack stack) {
		if (hasPockets(stack)) {
			return (MAX_OCCUPANCY / stack.getMaxStackSize()) + getPocketOccupancy(stack);
		}
		if (stack.is(Items.BUNDLE)) {
			return 4 + getPocketOccupancy(stack);
		}
		return MAX_OCCUPANCY / stack.getMaxStackSize();
	}

	public static int getPocketOccupancy(ItemStack pocketItem) {
		return getPocketedStacks(pocketItem).mapToInt(item -> getItemOccupancy(item) * item.getCount()).sum();
	}

	private static boolean canNest(ItemStack stack) {
		return stack.getItem().canFitInsideContainerItems();
	}
}
