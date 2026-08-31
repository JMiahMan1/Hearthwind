package dev.jmiahman.hearthwind.skills.party;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public final class PartyCombat {
    private PartyCombat() {}

    public static void register() {
        AttackEntityCallback.EVENT.register(PartyCombat::onAttackEntity);
    }

    private static InteractionResult onAttackEntity(Player player, Level level, InteractionHand hand,
            Entity target, EntityHitResult hitResult) {
        if (target instanceof Player targetPlayer) {
            if (PartyManager.areInSameParty(player.getUUID(), targetPlayer.getUUID())) {
                Party party = PartyManager.getPartyByPlayer(player.getUUID());
                if (party != null && !party.isPvpEnabled()) {
                    player.sendOverlayMessage(Component.literal("Friendly fire is disabled in your party!").withStyle(ChatFormatting.GOLD));
                    return InteractionResult.FAIL;
                }
            }
        }
        return InteractionResult.PASS;
    }
}
