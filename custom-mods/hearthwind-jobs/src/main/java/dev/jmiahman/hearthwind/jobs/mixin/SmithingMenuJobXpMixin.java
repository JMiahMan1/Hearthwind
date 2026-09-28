package dev.jmiahman.hearthwind.jobs.mixin;

import dev.jmiahman.hearthwind.jobs.JobEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Smithing table output pays job XP by its registry id, the same way the
 * anvil does. Like the anvil, 26.2 computes the result inside the menu
 * ({@code SmithingMenu}, whose result slot is another anonymous
 * {@code ItemCombinerMenu} slot), so {@code onTake} is the hook.
 */
@Mixin(SmithingMenu.class)
public abstract class SmithingMenuJobXpMixin {
    @Inject(method = "onTake", at = @At("TAIL"))
    private void hearthwind$smithingXp(Player player, ItemStack taken, CallbackInfo ci) {
        if (player instanceof ServerPlayer sp) {
            JobEvents.awardItem(sp, taken);
        }
    }
}
