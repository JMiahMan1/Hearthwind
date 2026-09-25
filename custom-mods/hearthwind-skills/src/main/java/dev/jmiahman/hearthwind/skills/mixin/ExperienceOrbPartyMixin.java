package dev.jmiahman.hearthwind.skills.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.jmiahman.hearthwind.skills.party.PartyManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Party XP sharing (Aged parity, PartyAddon ExperienceOrbEntityMixin): the XP
 * an orb gives (after Mending) goes to the party leader's pool instead of the
 * picker. Only orb pickup is shared, like Aged; commands, smelting and trades
 * are not.
 */
@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbPartyMixin {
    @WrapOperation(method = "playerTouch",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;giveExperiencePoints(I)V"))
    private void hearthwind$poolForParty(Player player, int amount, Operation<Void> original) {
        if (player instanceof ServerPlayer serverPlayer && PartyManager.poolOrbXp(serverPlayer, amount)) {
            return;
        }
        original.call(player, amount);
    }
}
