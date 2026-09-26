package dev.jmiahman.hearthwind.survival;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;

/**
 * Upstream Dehydration {@code ThirstHelper.hydratePlayer}: corpus template
 * lookup first (lowest tier wins), then potion/milk/honey fallbacks with
 * their exact Aged rolls.
 *
 * <p>Note the upstream quirks kept for parity: bad potions roll
 * {@code nextFloat() >= chance} (0.15 means an 85% Thirst risk) while the
 * bare-hand sip uses {@code <=}.
 */
public final class ThirstHelper {
    private ThirstHelper() {}

    /** 14 potions upstream treats as unsafe drinking water. */
    public static boolean isBadPotion(Potion potion) {
        return potion == Potions.WATER.value()
                || potion == Potions.AWKWARD.value()
                || potion == Potions.THICK.value()
                || potion == Potions.HARMING.value()
                || potion == Potions.LONG_POISON.value()
                || potion == Potions.LONG_SLOWNESS.value()
                || potion == Potions.LONG_WEAKNESS.value()
                || potion == Potions.MUNDANE.value()
                || potion == Potions.POISON.value()
                || potion == Potions.SLOWNESS.value()
                || potion == Potions.STRONG_HARMING.value()
                || potion == Potions.STRONG_POISON.value()
                || potion == Potions.STRONG_SLOWNESS.value()
                || potion == Potions.WEAKNESS.value();
    }

    public static void hydratePlayer(ServerPlayer player, ItemStack stack) {
        HearthwindSurvivalConfig cfg = HearthwindSurvivalConfig.get();
        int quench = HydrationCorpus.quench(stack);

        if (stack.is(Items.POTION)) {
            PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
            Potion potion = contents == null ? Potions.WATER.value()
                    : contents.potion().map(Holder::value).orElse(Potions.WATER.value());
            if (isBadPotion(potion)
                    && player.getRandom().nextFloat() >= cfg.flask.potionBadThirstChance) {
                player.addEffect(new MobEffectInstance(ThirstMobEffect.HOLDER,
                        cfg.flask.potionBadThirstDuration, 0, false, false, true));
            }
            if (quench == 0) {
                quench = (int) Math.round(cfg.thirst.potionThirstQuench);
            }
        } else if (stack.is(Items.MILK_BUCKET)) {
            if (player.getRandom().nextFloat() >= cfg.flask.milkThirstChance) {
                player.addEffect(new MobEffectInstance(ThirstMobEffect.HOLDER,
                        cfg.flask.potionBadThirstDuration / 2, 0, false, false, true));
            }
            if (quench == 0) {
                quench = cfg.flask.milkQuench;
            }
        } else if (stack.is(dev.jmiahman.hearthwind.survival.hydration.HydrationItems.WATER_BOWL)
                || stack.is(dev.jmiahman.hearthwind.survival.hydration.HydrationItems.PURIFIED_WATER_BOWL)) {
            // Dehydration water_bowl_quench fallback: the migrated corpus
            // lists both bowls at tier 3, so this only fires when the corpus
            // is disabled or empty. The dirty-water roll lives in WaterBowlItem.
            if (quench == 0) {
                quench = cfg.flask.waterBowlQuench;
            }
        } else if (quench == 0 && stack.is(Items.HONEY_BOTTLE)) {
            quench = cfg.flask.honeyQuench;
        }

        if (quench > 0) {
            HearthwindSurvivalThirst.addThirst(player, quench);
        }
    }
}
