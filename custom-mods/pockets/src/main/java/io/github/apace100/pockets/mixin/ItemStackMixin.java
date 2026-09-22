package io.github.apace100.pockets.mixin;

import io.github.apace100.pockets.PocketUtil;
import io.github.apace100.pockets.SoundUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

	@Inject(method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerPlayer;Ljava/util/function/Consumer;)V", at = @At("HEAD"))
	private void pockets$dropContainedStacks(int amount, ServerLevel level, ServerPlayer player, Consumer<Item> onBreak, CallbackInfo ci) {
		ItemStack stack = (ItemStack) (Object) this;
		if (player != null && PocketUtil.hasPockets(stack) && PocketUtil.getPocketOccupancy(stack) > 0) {
			if (PocketUtil.dropAllPocketedItems(stack, player)) {
				SoundUtil.playDropContentsSound(player);
			}
		}
	}

	@Inject(method = "hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V", at = @At("HEAD"))
	private void pockets$dropContainedStacksEquip(int amount, LivingEntity entity, EquipmentSlot slot, CallbackInfo ci) {
		ItemStack stack = (ItemStack) (Object) this;
		if (entity instanceof Player player && PocketUtil.hasPockets(stack) && PocketUtil.getPocketOccupancy(stack) > 0) {
			if (PocketUtil.dropAllPocketedItems(stack, player)) {
				SoundUtil.playDropContentsSound(player);
			}
		}
	}
}
