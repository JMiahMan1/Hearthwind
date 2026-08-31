package dev.jmiahman.hearthwind.jobs.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;

import dev.jmiahman.hearthwind.jobs.JobGates;
import dev.jmiahman.hearthwind.skills.SkillGates;

@Mixin(ResultSlot.class)
public abstract class CraftingGateMixin {
    @Inject(method = "remove", at = @At("HEAD"), cancellable = true)
    private void hearthwind_jobs$denyJobRestrictedCraft(int count,
            CallbackInfoReturnable<ItemStack> cir) {
        ResultSlot self = (ResultSlot) (Object) this;
        Player player = ((CraftingGateAccessor) self).hearthwind_jobs$getPlayer();
        if (!(player instanceof ServerPlayer sp)) {
            return;
        }
        ItemStack result = self.getItem();

        // Check job gates (jobs-addon parity)
        if (!JobGates.allowed(sp, result)) {
            sp.sendOverlayMessage(Component.literal(
                    "You need the required job level to craft this item."));
            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }

        // Check skill craft gates (levelz/crafting parity — CRAFT_GATES)
        SkillGates.Gate skillGate = SkillGates.craftGate(result);
        if (skillGate != null && !SkillGates.allowed(sp, skillGate)) {
            sp.sendOverlayMessage(Component.literal(
                    "You need " + skillGate.skill().id + " level " + skillGate.level()
                    + " to craft this."));
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }
}
