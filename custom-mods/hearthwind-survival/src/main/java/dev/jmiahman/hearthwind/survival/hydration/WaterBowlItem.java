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
 * greater-or-equal). The purified bowl never thirsts.
 *
 * <p>Returns a plain bowl unless the drinker has infinite materials
 * (creative), exactly like upstream.
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
        if (user instanceof Player player && player.hasInfiniteMaterials()) {
            return result;
        }
        return new ItemStack(Items.BOWL);
    }
}
