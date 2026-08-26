package dev.jmiahman.hearthwind.primitive;

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

import dev.jmiahman.hearthwind.survival.HearthwindSurvivalThirst;
import dev.jmiahman.hearthwind.survival.HearthwindSurvivalTemperature;
import dev.jmiahman.hearthwind.survival.ThirstMobEffect;

/**
 * Clay cup water - early game, 2x2 craft, no table. Shows proper liquid (green tainted vs blue purified).
 * When drunk, returns the empty clay cup (fired or unfired) with durability handling.
 */
public class ClayCupWaterItem extends Item {
    private static final double HYDRATION = 6.0;
    private final boolean purified;
    private final boolean isFired; // if false, it's unfired (grey) - breaks after 3 uses, else 32

    public ClayCupWaterItem(Properties props, boolean purified, boolean isFired) {
        super(props);
        this.purified = purified;
        this.isFired = isFired;
    }

    private InteractionResult drink(Level level, Player player, ItemStack stack) {
        if (level instanceof ServerLevel server && player instanceof ServerPlayer sp) {
            // Determine which empty cup to return
            Item emptyCup = isFired ? HearthwindPrimitiveItems.CLAY_CUP : HearthwindPrimitiveItems.CLAY_CUP_UNFIRED;
            HearthwindSurvivalThirst.addHydration(sp, HYDRATION);
            if (!purified && sp.getRandom().nextDouble() < 0.30) {
                sp.addEffect(new MobEffectInstance(ThirstMobEffect.HOLDER, 300, 0));
            }
            // Normal water gives a small cooling vs overheating
            HearthwindSurvivalTemperature.applyColdCooldown(sp, 600);
            HearthwindSurvivalTemperature.shift(sp, -0.7);
            server.playSound(null, sp.blockPosition(), SoundEvents.GENERIC_DRINK.value(), SoundSource.PLAYERS, 0.9f, 1.0f);
            sp.awardStat(Stats.ITEM_USED.get(this));
            if (!sp.getAbilities().instabuild) {
                stack.shrink(1);
                ItemStack bowl = new ItemStack(emptyCup);
                // Clay cups have durability, so the returned empty cup will have damage
                // The filled cup itself is consumed, the empty is returned
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
    public void appendHoverText(ItemStack stack, TooltipContext ctx, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> out, net.minecraft.world.item.TooltipFlag flag) {
        out.accept(Component.literal(purified ? "Purified water" : "Tainted water - may cause thirst").withStyle(purified ? ChatFormatting.AQUA : ChatFormatting.YELLOW));
        out.accept(Component.literal(isFired ? "Fired clay cup (durable)" : "Unfired clay cup (fragile, 3 uses)").withStyle(ChatFormatting.GRAY));
    }
}
