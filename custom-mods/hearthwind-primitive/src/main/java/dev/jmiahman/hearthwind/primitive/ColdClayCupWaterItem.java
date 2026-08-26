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

public class ColdClayCupWaterItem extends Item {
    private final boolean purified;
    public ColdClayCupWaterItem(Properties p, boolean purified) { super(p); this.purified=purified; }
    private InteractionResult drink(Level lvl, Player player, ItemStack stack) {
        if (lvl instanceof ServerLevel server && player instanceof ServerPlayer sp) {
            HearthwindSurvivalThirst.addHydration(sp, 6.0);
            if (!purified && sp.getRandom().nextDouble() < 0.30) sp.addEffect(new MobEffectInstance(ThirstMobEffect.HOLDER, 300, 0));
            HearthwindSurvivalTemperature.shift(sp, -1.5);
            HearthwindSurvivalTemperature.applyColdCooldown(sp, 1200);
            sp.sendOverlayMessage(Component.literal("Icy cold! Cooled for 60s").withStyle(ChatFormatting.AQUA));
            server.playSound(null, sp.blockPosition(), SoundEvents.GENERIC_DRINK.value(), SoundSource.PLAYERS, 0.9f, 1.2f);
            sp.awardStat(Stats.ITEM_USED.get(this));
            if (!sp.getAbilities().instabuild) {
                stack.shrink(1);
                ItemStack ret = new ItemStack(HearthwindPrimitiveItems.CLAY_CUP);
                if (!sp.getInventory().add(ret)) sp.drop(ret, false);
            }
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }
    @Override public InteractionResult use(Level l, Player p, InteractionHand h) { return drink(l,p,p.getItemInHand(h)); }
    @Override public InteractionResult useOn(net.minecraft.world.item.context.UseOnContext c) { return drink(c.getLevel(), c.getPlayer(), c.getItemInHand()); }
    @Override public void appendHoverText(ItemStack s, TooltipContext ctx, net.minecraft.world.item.component.TooltipDisplay d, java.util.function.Consumer<Component> out, net.minecraft.world.item.TooltipFlag f) {
        out.accept(Component.literal("Icy cold - cools overheating 60s").withStyle(ChatFormatting.AQUA));
        out.accept(Component.literal(purified ? "Purified and chilled" : "Cold but dirty").withStyle(ChatFormatting.GRAY));
    }
    @Override public Component getName(ItemStack s) { return Component.literal(super.getName(s).getString()).withStyle(ChatFormatting.AQUA); }
}
