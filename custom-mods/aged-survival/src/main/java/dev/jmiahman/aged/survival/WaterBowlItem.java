package dev.jmiahman.aged.survival;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class WaterBowlItem extends Item {
    private static final double BAD_SIP_CHANCE = 0.5;
    private static final int BAD_SIP_DURATION_TICKS = 300;
    private static final double HYDRATION_PER_BOWL = 6.0;

    private final boolean purified;

    public WaterBowlItem(Properties properties, boolean purified) {
        super(properties);
        this.purified = purified;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel server && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            AgedSurvivalThirst.addHydration(serverPlayer, HYDRATION_PER_BOWL);
            if (!purified && !player.getAbilities().instabuild
                    && player.getRandom().nextDouble() < BAD_SIP_CHANCE) {
                player.addEffect(new MobEffectInstance(
                        ThirstMobEffect.HOLDER, BAD_SIP_DURATION_TICKS, 0));
            }
            server.playSound(null, player.blockPosition(),
                    SoundEvents.GENERIC_DRINK.value(), SoundSource.PLAYERS, 0.9f, 1.0f);
            player.awardStat(Stats.ITEM_USED.get(this));
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }
}
