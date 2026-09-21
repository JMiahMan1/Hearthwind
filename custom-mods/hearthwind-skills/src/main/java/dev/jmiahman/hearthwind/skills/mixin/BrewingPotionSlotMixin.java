package dev.jmiahman.hearthwind.skills.mixin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import dev.jmiahman.hearthwind.skills.Skill;
import dev.jmiahman.hearthwind.skills.SkillGates;
import dev.jmiahman.hearthwind.skills.SkillXp;
import dev.jmiahman.hearthwind.skills.SkillsConfig;

/**
 * Brewing gate enforcement + alchemy XP.
 *
 * BrewingStandMenu has no result slot; the brewed potion bottles are taken
 * through BrewingStandMenu$PotionSlot.onTake. That is the single hook for
 * both the BREWING_GATES check (20 datapack files, previously loaded but
 * never enforced) and the alchemy XP award.
 */
@Mixin(targets = "net.minecraft.world.inventory.BrewingStandMenu$PotionSlot")
public abstract class BrewingPotionSlotMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger(BrewingPotionSlotMixin.class);

    @Inject(method = "onTake", at = @At("HEAD"), cancellable = true)
    private void hearthwind$gateBrewedPotionTake(Player player, ItemStack stack, CallbackInfo ci) {
        if (!(player instanceof ServerPlayer sp) || stack.isEmpty()) {
            return;
        }
        SkillGates.Gate gate = SkillGates.brewingGate(stack);
        if (gate != null && !SkillGates.allowed(sp, gate)) {
            sp.sendOverlayMessage(Component.literal(
                    "You need alchemy level " + gate.level() + " to brew this."));
            ci.cancel();
            return;
        }
        SkillXp.addXp(sp, Skill.ALCHEMY, SkillsConfig.get().xp.alchemyPerBrew);
    }
}