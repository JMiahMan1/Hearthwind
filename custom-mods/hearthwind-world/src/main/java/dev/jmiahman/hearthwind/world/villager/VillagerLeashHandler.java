package dev.jmiahman.hearthwind.world.villager;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

/**
 * In-house port of upstream Aged mod 'villager-transportation' (MIT):
 * Allows leashing and leading villagers and wandering traders with vanilla leads.
 */
public final class VillagerLeashHandler {

    private VillagerLeashHandler() {}

    public static void register() {
        UseEntityCallback.EVENT.register(VillagerLeashHandler::onUseEntity);
    }

    private static InteractionResult onUseEntity(Player player, Level level, InteractionHand hand, Entity entity, EntityHitResult hitResult) {
        if (!(entity instanceof AbstractVillager villager)) {
            return InteractionResult.PASS;
        }

        ItemStack held = player.getItemInHand(hand);

        // 1. Leash with lead
        if (held.is(Items.LEAD) && !villager.isLeashed()) {
            if (!level.isClientSide()) {
                villager.setLeashedTo(player, true);
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
                level.playSound(null, villager.blockPosition(), SoundEvents.LEAD_TIED, SoundSource.NEUTRAL, 1.0F, 1.0F);
            }
            return InteractionResult.SUCCESS;
        }

        // 2. Unleash if holding lead/empty and clicking leashed villager
        if (villager.isLeashed() && villager.getLeashHolder() == player && player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                villager.dropLeash();
                level.playSound(null, villager.blockPosition(), SoundEvents.LEAD_UNTIED, SoundSource.NEUTRAL, 1.0F, 1.0F);
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    public static boolean tryLeashVillager(Player player, AbstractVillager villager) {
        if (villager.isLeashed()) return false;
        villager.setLeashedTo(player, true);
        return true;
    }
}
