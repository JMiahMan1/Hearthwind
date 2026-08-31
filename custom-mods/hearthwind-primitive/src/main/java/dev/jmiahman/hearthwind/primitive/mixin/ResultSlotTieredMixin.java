package dev.jmiahman.hearthwind.primitive.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.jmiahman.hearthwind.primitive.tiered.TieredData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;

@Mixin(ResultSlot.class)
public abstract class ResultSlotTieredMixin {

    @Inject(method = "onTake", at = @At("HEAD"))
    private void hearthwind$applyTierOnCraftTake(Player player, ItemStack stack, CallbackInfo ci) {
        if (!stack.isEmpty() && player != null) {
            TieredData.applyRandomTierIfEligible(stack, player.getRandom());
        }
    }
}
