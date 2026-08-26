package dev.jmiahman.hearthwind.survival;

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

    private InteractionResult drink(Level level, Player player, ItemStack stack) {
        if (level instanceof ServerLevel server && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            HearthwindSurvivalThirst.addHydration(serverPlayer, HYDRATION_PER_BOWL);
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
                // return empty bowl like vanilla stew
                ItemStack bowl = new ItemStack(net.minecraft.world.item.Items.BOWL);
                if (!player.addItem(bowl)) {
                    player.drop(bowl, false);
                }
            }
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        return drink(level, player, player.getItemInHand(hand));
    }

    @Override
    public InteractionResult useOn(net.minecraft.world.item.context.UseOnContext ctx) {
        // Allow drinking even when looking at a block (e.g., water) - vanilla
        // stew/bowl items only override use() for air, so right-clicking a
        // block with a water_bowl would otherwise do nothing.
        return drink(ctx.getLevel(), ctx.getPlayer(), ctx.getItemInHand());
    }
}
