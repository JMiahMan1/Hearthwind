package dev.jmiahman.hearthwind.survival.hydration;

import dev.jmiahman.hearthwind.survival.HearthwindSurvivalConfig;
import dev.jmiahman.hearthwind.survival.ThirstMobEffect;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Port of Dehydration 1.3.6 {@code WaterBowlItem} (Aged 3.1.2).
 *
 * <p>Drinking takes 32 ticks (the {@code CONSUMABLE} component:
 * 1.6s DRINK). Finishing quenches through the hydration corpus (the
 * existing {@code ConsumableConsumeMixin} drives
 * {@code ThirstHelper.hydratePlayer}, with {@code water_bowl_quench} as the
 * fallback); a dirty water bowl additionally rolls the Aged thirst effect
 * at {@code water_bowl_thirst_chance}, using the exact upstream comparison
 * {@code nextFloat() >= chance} (so the effect lands when the roll is
 * greater-or-equal).
 *
 * <p>Both bowls roll it. Dehydration's {@code ItemInit} constructs
 * {@code water_bowl} at bytecode offset 300 and
 * {@code purified_water_bowl} at offset 328, and both push
 * {@code iconst_1} into the {@code WaterBowlItem(Properties, boolean
 * hasThirstChance)} constructor - so a bowl of PURIFIED water still has a
 * 40% chance of leaving you Thirsty in Aged. We briefly made the purified
 * bowl safe; that was our own idea, not the reference's, and it is reverted
 * here (0.1.47).
 *
 * <p>Returns {@link ItemStack#EMPTY} for a player, exactly like upstream: the
 * bowl is consumed and nothing is given back. The {@code pour_*_water_bowl}
 * shapeless recipes we used to ship were our own addition and are removed.
 */
public class WaterBowlItem extends Item {
    private final boolean hasThirstChance;

    public WaterBowlItem(Properties properties, boolean hasThirstChance) {
        super(properties);
        this.hasThirstChance = hasThirstChance;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity user) {
        ItemStack result = super.finishUsingItem(stack, level, user);
        if (this.hasThirstChance && user instanceof ServerPlayer player) {
            HearthwindSurvivalConfig.Flask cfg = HearthwindSurvivalConfig.get().flask;
            if (player.getRandom().nextFloat() >= cfg.waterBowlThirstChance) {
                player.addEffect(new MobEffectInstance(ThirstMobEffect.HOLDER,
                        cfg.potionBadThirstDuration / 2, 0, false, false, true));
            }
        }
        // The reference returns ItemStack.EMPTY for a player, so drinking the
        // bowl consumes it and hands back nothing - there is no bowl craft
        // remainder. Only a non-player (e.g. a dispenser-like caller) gets the
        // residual stack. 0.1.49.
        if (user instanceof Player) {
            return ItemStack.EMPTY;
        }
        return result;
    }
}
