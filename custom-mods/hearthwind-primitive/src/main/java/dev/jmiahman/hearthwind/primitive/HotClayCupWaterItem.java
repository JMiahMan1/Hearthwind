package dev.jmiahman.hearthwind.primitive;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import dev.jmiahman.hearthwind.survival.HearthwindSurvivalThirst;
import dev.jmiahman.hearthwind.survival.ThirstMobEffect;

public class HotClayCupWaterItem extends Item {
    private static final String HOT_UNTIL = "hearthwind:hot_until";
    private static final long HOT_DUR = 600;
    private final boolean purified;

    public HotClayCupWaterItem(Properties props, boolean purified) {
        super(props);
        this.purified = purified;
    }

    public static ItemStack createHotStack(Item item, Level lvl) {
        ItemStack s = new ItemStack(item);
        CompoundTag t = new CompoundTag();
        t.putLong(HOT_UNTIL, lvl.getGameTime() + HOT_DUR);
        s.set(DataComponents.CUSTOM_DATA, CustomData.of(t));
        return s;
    }

    private boolean isHot(ItemStack stack, Level lvl) {
        var d = stack.get(DataComponents.CUSTOM_DATA);
        if (d == null) return false;
        var tag = d.copyTag();
        if (!tag.contains(HOT_UNTIL)) return false;
        return lvl.getGameTime() < tag.getLongOr(HOT_UNTIL, 0);
    }

    private InteractionResult drink(Level lvl, Player player, ItemStack stack) {
        if (lvl instanceof ServerLevel server && player instanceof ServerPlayer sp) {
            boolean hot = isHot(stack, lvl);
            Item empty = HearthwindPrimitiveItems.CLAY_CUP; // fired is more common for hot (boiled)
            if (hot) {
                sp.setHealth(Math.max(0.1f, sp.getHealth() - 2.0f));
                sp.setRemainingFireTicks(20);
                sp.addEffect(new MobEffectInstance(ThirstMobEffect.HOLDER, 400, 0));
                HearthwindSurvivalThirst.addHydration(sp, 3.0);
                sp.sendOverlayMessage(Component.literal("Ouch! Scalding hot!").withStyle(ChatFormatting.RED));
                server.playSound(null, sp.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.7f, 1.5f);
            } else {
                HearthwindSurvivalThirst.addHydration(sp, 6.0);
                if (!purified && sp.getRandom().nextDouble() < 0.30) sp.addEffect(new MobEffectInstance(ThirstMobEffect.HOLDER, 300, 0));
                server.playSound(null, sp.blockPosition(), SoundEvents.GENERIC_DRINK.value(), SoundSource.PLAYERS, 0.9f, 1.0f);
            }
            sp.awardStat(Stats.ITEM_USED.get(this));
            if (!sp.getAbilities().instabuild) {
                stack.shrink(1);
                ItemStack ret = new ItemStack(empty);
                if (!sp.getInventory().add(ret)) sp.drop(ret, false);
            }
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }

    @Override public InteractionResult use(Level l, Player p, InteractionHand h) { return drink(l,p,p.getItemInHand(h)); }
    @Override public InteractionResult useOn(net.minecraft.world.item.context.UseOnContext c) { return drink(c.getLevel(), c.getPlayer(), c.getItemInHand()); }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, net.minecraft.world.item.component.TooltipDisplay d, java.util.function.Consumer<Component> out, net.minecraft.world.item.TooltipFlag f) {
        out.accept(Component.literal("Steaming hot! Let it cool 30s").withStyle(ChatFormatting.RED));
        out.accept(Component.literal(purified ? "Purified but scalding" : "Tainted and scalding").withStyle(ChatFormatting.GRAY));
    }
    @Override public Component getName(ItemStack s) { return Component.literal(super.getName(s).getString()).withStyle(ChatFormatting.RED); }
}
