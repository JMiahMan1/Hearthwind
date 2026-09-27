package dev.jmiahman.hearthwind.jobs.mixin;

import dev.jmiahman.hearthwind.jobs.JobEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.FurnaceResultSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Furnace, smoker and blast furnace outputs pay job XP by their registry id;
 * the ladders decide the job (cooked food -> farmer, metal -> smither).
 */
@Mixin(FurnaceResultSlot.class)
public abstract class FurnaceResultSlotJobXpMixin {
    @Inject(method = "onTake", at = @At("TAIL"))
    private void hearthwind$furnaceXp(Player player, ItemStack stack, CallbackInfo ci) {
        if (player instanceof ServerPlayer sp) {
            JobEvents.awardItem(sp, stack);
        }
    }
}
