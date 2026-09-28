package dev.jmiahman.hearthwind.jobs.mixin;

import dev.jmiahman.hearthwind.jobs.JobEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Brewing pays brewer job XP.
 *
 * <p>{@code BrewingStandBlockEntity.doBrew} is private and static and takes
 * no player, so it cannot tell who brewed what. The menu's own potion slot
 * does know: the three bottle slots at the bottom of a brewing stand all use
 * this slot type, and taking a filled bottle out of one is what vanilla
 * itself uses to fire the {@code BREWED_POTION} statistic, so this is the
 * same "a potion left a brewing stand" moment vanilla already treats as
 * brewing.
 *
 * <p>The award is keyed on the brewed potion's own id against the brewer
 * corpus ladder - the ladder is a list of potions ({@code minecraft:awkward},
 * {@code minecraft:strong_swiftness}, ...), not of block or item ids. A
 * potion the ladder does not list therefore pays nothing, which is what
 * keeps the hook from turning every bottle-handling click into XP.
 */
@Mixin(targets = "net.minecraft.world.inventory.BrewingStandMenu$PotionSlot")
public abstract class BrewingStandPotionSlotJobXpMixin {
    @Inject(method = "onTake", at = @At("HEAD"))
    private void hearthwind$brewerXp(Player player, ItemStack carried, CallbackInfo ci) {
        if (player instanceof ServerPlayer sp) {
            JobEvents.awardBrew(sp, carried);
        }
    }
}
