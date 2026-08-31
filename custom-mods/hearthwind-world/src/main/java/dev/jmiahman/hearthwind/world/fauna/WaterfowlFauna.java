package dev.jmiahman.hearthwind.world.fauna;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

/**
 * Waterfowl & Duck mechanics (Aged parity with 'duckling'):
 * Waterfowl in river/swamp/ocean water can be fed water plants/seeds for breeding and feathers.
 */
public final class WaterfowlFauna {

    private WaterfowlFauna() {}

    public static void register() {
        UseEntityCallback.EVENT.register(WaterfowlFauna::onInteractFauna);
    }

    private static InteractionResult onInteractFauna(Player player, Level level, InteractionHand hand, Entity entity, EntityHitResult hitResult) {
        if (!(entity instanceof Animal animal)) {
            return InteractionResult.PASS;
        }

        ItemStack held = player.getItemInHand(hand);

        // Feeding seeds or seagrass/kelp to water animals
        if ((held.is(Items.WHEAT_SEEDS) || held.is(Items.DRIED_KELP) || held.is(Items.SEAGRASS)) && animal.isInWater()) {
            if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
                serverLevel.sendParticles(ParticleTypes.HEART, animal.getX(), animal.getY() + 0.5, animal.getZ(), 3, 0.2, 0.2, 0.2, 0.05);
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}
