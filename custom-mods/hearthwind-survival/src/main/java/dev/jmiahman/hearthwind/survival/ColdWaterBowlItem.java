package dev.jmiahman.hearthwind.survival;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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

import java.util.List;

/**
 * Cold water - chilled from snowy biome, ice, or winter. Gives the same
 * hydration as normal (+6) but also cools the drinker: -1.5 body temp
 * immediately and a 60s "cooled" window where heat drift is dampened.
 * Indication is aqua tint, icy tooltip, and a crisp sound. Hot water
 * (HotWaterBowlItem) does the opposite - scalds and gives no cooling.
 */
public final class ColdWaterBowlItem extends Item {
    private static final double HYDRATION = 6.0;
    private final boolean purified;

    public ColdWaterBowlItem(Properties properties, boolean purified) {
        super(properties);
        this.purified = purified;
    }

    private InteractionResult drink(Level level, Player player, ItemStack stack) {
        if (level instanceof ServerLevel server && player instanceof ServerPlayer sp) {
            HearthwindSurvivalThirst.addHydration(sp, HYDRATION);
            if (!purified && sp.getRandom().nextDouble() < 0.30) {
                sp.addEffect(new MobEffectInstance(ThirstMobEffect.HOLDER, 300, 0));
            }
            // Cold cooling: immediate -1.5 and 60s dampening (unless already hot)
            HearthwindSurvivalTemperature.shift(sp, -1.5);
            HearthwindSurvivalTemperature.applyColdCooldown(sp, 1200); // 60s
            sp.sendOverlayMessage(Component.literal("Refreshing cold water - you feel cooled.").withStyle(ChatFormatting.AQUA));
            server.playSound(null, sp.blockPosition(), SoundEvents.GENERIC_DRINK.value(), SoundSource.PLAYERS, 0.9f, 1.2f);
            sp.awardStat(Stats.ITEM_USED.get(this));
            if (!sp.getAbilities().instabuild) {
                stack.shrink(1);
                var bowl = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BOWL);
                if (!sp.getInventory().add(bowl)) sp.drop(bowl, false);
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
        return drink(ctx.getLevel(), ctx.getPlayer(), ctx.getItemInHand());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> out, net.minecraft.world.item.TooltipFlag flag) {
        out.accept(Component.literal("Icy cold - cools overheating for 60s").withStyle(ChatFormatting.AQUA));
        out.accept(Component.literal(purified ? "Purified and chilled" : "Cold but dirty (50% thirst)").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.literal(super.getName(stack).getString()).withStyle(ChatFormatting.AQUA);
    }
}
