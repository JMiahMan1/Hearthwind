package dev.jmiahman.hearthwind.skills.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;
import dev.jmiahman.hearthwind.skills.Skill;
import dev.jmiahman.hearthwind.skills.SkillGates;
import dev.jmiahman.hearthwind.skills.SkillXp;
import dev.jmiahman.hearthwind.skills.SkillsConfig;

@Mixin(SmithingMenu.class)
public abstract class SmithingGateMixin {

    @Inject(method = "createResult", at = @At("TAIL"))
    private void hearthwind$gateSmithingResult(CallbackInfo ci) {
        ItemCombinerMenu accessor = (ItemCombinerMenu) (Object) this;
        Player player = ((ItemCombinerMenuAccessor) accessor).hearthwind$getPlayer();
        if (!(player instanceof ServerPlayer sp)) {
            return;
        }

        ItemStack result = ((ItemCombinerMenuAccessor) accessor).hearthwind$getResultSlots().getItem(0);
        if (result.isEmpty()) {
            return;
        }

        SkillGates.Gate gate = SkillGates.smithingGate(result);
        if (gate != null && !SkillGates.allowed(sp, gate)) {
            ((ItemCombinerMenuAccessor) accessor).hearthwind$getResultSlots().setItem(0, ItemStack.EMPTY);
            sp.sendOverlayMessage(Component.literal(
                    "You need smithing level " + gate.level() + " to forge this item."));
        }
    }

    @Inject(method = "onTake", at = @At("HEAD"))
    private void hearthwind$awardSmithingXp(Player player, ItemStack stack, CallbackInfo ci) {
        if (player instanceof ServerPlayer sp && !stack.isEmpty()) {
            SkillXp.addXp(sp, Skill.SMITHING, SkillsConfig.get().xp.smithingPerCraft);
        }
    }
}
