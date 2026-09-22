package io.github.apace100.pockets.mixin;

import io.github.apace100.pockets.PocketUtil;
import io.github.apace100.pockets.SoundUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.function.Consumer;

@Mixin(Item.class)
public class ItemMixin {

	@Inject(method = "overrideOtherStackedOnMe", at = @At("HEAD"), cancellable = true)
	private void pockets$overrideOtherStackedOnMe(ItemStack stack, ItemStack otherStack, Slot slot, ClickAction clickType, Player player, SlotAccess cursorStack, CallbackInfoReturnable<Boolean> cir) {
		if (clickType != ClickAction.SECONDARY || !slot.mayPickup(player)) {
			return;
		}
		boolean pocketable = PocketUtil.hasPockets(stack) && PocketUtil.isAllowedInPockets(otherStack);
		if (otherStack.isEmpty()) {
			if (PocketUtil.hasPockets(stack)) {
				Optional<ItemStack> removed = PocketUtil.removeFirstStack(stack);
				if (removed.isPresent()) {
					SoundUtil.playRemoveOneSound(player);
					cursorStack.set(removed.get());
					cir.setReturnValue(true);
					return;
				}
				cir.setReturnValue(false);
				return;
			}
		} else if (pocketable) {
			int added = PocketUtil.addToPockets(stack, otherStack);
			if (added > 0) {
				SoundUtil.playInsertSound(player);
				otherStack.shrink(added);
				cir.setReturnValue(true);
				return;
			}
		}
		cir.setReturnValue(pocketable);
	}

	@Inject(method = "overrideStackedOnOther", at = @At("HEAD"), cancellable = true)
	private void pockets$overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction clickType, Player player, CallbackInfoReturnable<Boolean> cir) {
		if (clickType != ClickAction.SECONDARY || !slot.mayPickup(player)) {
			return;
		}
		if (!PocketUtil.hasPockets(stack)) {
			return;
		}
		ItemStack slotStack = slot.getItem();
		if (slotStack.isEmpty()) {
			cir.setReturnValue(false);
			return;
		}
		if (PocketUtil.isAllowedInPockets(slotStack)) {
			int added = PocketUtil.addToPockets(stack, slotStack);
			if (added > 0) {
				SoundUtil.playInsertSound(player);
				slotStack.shrink(added);
				if (slotStack.isEmpty()) {
					slot.set(ItemStack.EMPTY);
				}
				cir.setReturnValue(true);
				return;
			}
		}
		cir.setReturnValue(PocketUtil.isAllowedInPockets(slotStack));
	}

	@Inject(method = "appendHoverText", at = @At("TAIL"))
	private void pockets$appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag, CallbackInfo ci) {
		if (!PocketUtil.hasPockets(stack)) {
			return;
		}
		if (display.hideTooltip()) {
			return;
		}
		tooltip.accept(Component.translatable("item.minecraft.bundle.fullness", PocketUtil.getPocketOccupancy(stack), PocketUtil.MAX_OCCUPANCY).withStyle(net.minecraft.ChatFormatting.GRAY));
	}

	@Inject(method = "onDestroyed", at = @At("HEAD"))
	private void pockets$onDestroyed(ItemEntity entity, CallbackInfo ci) {
		if (!PocketUtil.hasPockets(entity.getItem())) {
			return;
		}
		SoundUtil.playDropContentsSound(entity);
		PocketUtil.dropAllPocketedItems(entity.getItem(), entity);
	}
}
