package dev.jmiahman.hearthwind.jobs.mixin;

import dev.jmiahman.hearthwind.jobs.JobEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Anvil output pays job XP by its registry id: an iron or diamond
 * {@code minecraft:iron_ingot} reward is smither work.
 *
 * <p>26.2 keeps the anvil result in the <em>menu</em> rather than in an
 * {@code AnvilBlock} (the class the block state logic moved to does not
 * exist any more), and the result slot is one of the anonymous slots built
 * by {@code ItemCombinerMenu.createResultSlot}, so the existing furnace
 * result-slot mixin cannot see it. Hooking {@code AnvilMenu.onTake} - the
 * menu's own "player took the crafted result" callback - catches the single
 * place where the anvil result leaves the table.
 */
@Mixin(AnvilMenu.class)
public abstract class AnvilMenuJobXpMixin {
    @Inject(method = "onTake", at = @At("TAIL"))
    private void hearthwind$anvilXp(Player player, ItemStack taken, CallbackInfo ci) {
        if (player instanceof ServerPlayer sp) {
            JobEvents.awardItem(sp, taken);
        }
    }
}
