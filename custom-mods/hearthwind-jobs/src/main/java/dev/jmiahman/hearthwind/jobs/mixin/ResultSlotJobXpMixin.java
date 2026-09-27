package dev.jmiahman.hearthwind.jobs.mixin;

import dev.jmiahman.hearthwind.jobs.JobEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Crafted items pay job XP by their registry id (Aged's jobsaddon rewards
 * crafting through the farmer/smither item ladders). Only the crafting TABLE
 * pays: the 2x2 inventory grid is excluded, which is also where the
 * restricted piece-to-ingot conversions are made, so they keep paying
 * nothing.
 */
@Mixin(ResultSlot.class)
public abstract class ResultSlotJobXpMixin {
    @Inject(method = "onTake", at = @At("TAIL"))
    private void hearthwind$craftXp(Player player, ItemStack stack, CallbackInfo ci) {
        if (player instanceof ServerPlayer sp && sp.containerMenu instanceof CraftingMenu) {
            JobEvents.awardItem(sp, stack);
        }
    }
}
