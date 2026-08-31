package dev.jmiahman.hearthwind.skills.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import dev.jmiahman.hearthwind.skills.Skill;
import dev.jmiahman.hearthwind.skills.SkillXp;
import dev.jmiahman.hearthwind.skills.SkillsConfig;

@Mixin(targets = "net.minecraft.world.inventory.BrewingStandMenu$PotionSlot")
public abstract class BrewingPotionSlotMixin {
    @Inject(method = "onTake", at = @At("HEAD"))
    private void hearthwind$awardAlchemyXp(Player player, ItemStack stack, CallbackInfo ci) {
        if (player instanceof ServerPlayer sp && !stack.isEmpty()) {
            SkillXp.addXp(sp, Skill.ALCHEMY, SkillsConfig.get().xp.alchemyPerBrew);
        }
    }
}
