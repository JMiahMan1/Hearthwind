package dev.jmiahman.hearthwind.survival;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * Consumable temperature countermeasure (parity: environmentz ice pack /
 * heating stones). Each use shifts body temperature by a fixed delta and
 * costs one durability point.
 */
public class TempControlItem extends Item {
    private final double delta;

    public TempControlItem(Properties properties, double delta) {
        super(properties);
        this.delta = delta;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!(level instanceof ServerLevel serverLevel)
                || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.CONSUME;
        }
        double shifted = HearthwindSurvivalTemperature.shift(serverPlayer, delta);
        serverLevel.playSound(null, serverPlayer.blockPosition(),
                EnvironmentzItems.drinkSound().value(), player.getSoundSource());
        if (!player.getAbilities().instabuild) {
            player.getItemInHand(hand).hurtAndBreak(1, serverPlayer,
                    net.minecraft.world.entity.EquipmentSlot.MAINHAND);
        }
        HearthwindSurvivalTemperature.sendFeedback(serverPlayer, shifted);
        return InteractionResult.SUCCESS_SERVER;
    }
}
