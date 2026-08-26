package dev.jmiahman.hearthwind.survival;

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
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Hot water - must cool before safe drinking. While hot (30s after filling
 * from a heated cauldron or hot biome), drinking scalds: 2 damage (1 heart)
 * + fire 1s. Indication is red name, tooltip, steam sound, and overlay.
 * After the cooldown the same stack is treated as its cooled counterpart
 * (purified or dirty) with no burn. Uses CustomData hearthwind:hot_until.
 */
public final class HotWaterBowlItem extends Item {
    private static final String HOT_UNTIL_KEY = "hearthwind:hot_until";
    private static final long HOT_DURATION_TICKS = 600; // 30s
    private static final double HYDRATION = 6.0;
    private final boolean purified;

    public HotWaterBowlItem(Properties properties, boolean purified) {
        super(properties);
        this.purified = purified;
    }

    public static ItemStack createHotStack(Item hotItem, Level level) {
        ItemStack stack = new ItemStack(hotItem);
        long until = level.getGameTime() + HOT_DURATION_TICKS;
        CompoundTag tag = new CompoundTag();
        tag.putLong(HOT_UNTIL_KEY, until);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }

    private static long getHotUntil(ItemStack stack, Level level) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return 0;
        CompoundTag tag = data.copyTag();
        if (!tag.contains(HOT_UNTIL_KEY)) return 0;
        return tag.getLongOr(HOT_UNTIL_KEY, 0);
    }

    private static boolean isStillHot(ItemStack stack, Level level) {
        long until = getHotUntil(stack, level);
        return until != 0 && level.getGameTime() < until;
    }

    private static long remainingSeconds(ItemStack stack, Level level) {
        long until = getHotUntil(stack, level);
        if (until == 0) return 0;
        long rem = until - level.getGameTime();
        return rem > 0 ? (rem + 19) / 20 : 0;
    }

    private InteractionResult drink(Level level, Player player, ItemStack stack) {
        if (level instanceof ServerLevel server && player instanceof ServerPlayer sp) {
            boolean hot = isStillHot(stack, level);
            if (hot) {
                long secs = remainingSeconds(stack, level);
                // Scald - direct health set to bypass invulnerability checks (peaceful, etc.)
                float newHealth = Math.max(0.1f, sp.getHealth() - 2.0f);
                sp.setHealth(newHealth);
                sp.setRemainingFireTicks(20); // 1s fire
                sp.addEffect(new MobEffectInstance(ThirstMobEffect.HOLDER, 200, 0));
                sp.sendOverlayMessage(Component.literal(
                        "Ouch! It's still scalding hot! (" + secs + "s left - let it cool)")
                        .withStyle(ChatFormatting.RED));
                server.playSound(null, sp.blockPosition(),
                        SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.7f, 1.5f);
                // Still consume and give hydration but with penalty (half)
                HearthwindSurvivalThirst.addHydration(sp, HYDRATION * 0.5);
                if (!purified) {
                    // dirty hot is even worse
                    sp.addEffect(new MobEffectInstance(ThirstMobEffect.HOLDER, 400, 0));
                }
                sp.awardStat(Stats.ITEM_USED.get(this));
                if (!sp.getAbilities().instabuild) {
                    stack.shrink(1);
                    ItemStack bowl = new ItemStack(net.minecraft.world.item.Items.BOWL);
                    if (!sp.getInventory().add(bowl)) sp.drop(bowl, false);
                }
                return InteractionResult.SUCCESS_SERVER;
            }
            // Cooled - treat as normal purified/dirty (no burn) but no cooling buff
            HearthwindSurvivalThirst.addHydration(sp, HYDRATION);
            if (!purified && sp.getRandom().nextDouble() < 0.5) {
                sp.addEffect(new MobEffectInstance(ThirstMobEffect.HOLDER, 300, 0));
            }
            server.playSound(null, sp.blockPosition(),
                    SoundEvents.GENERIC_DRINK.value(), SoundSource.PLAYERS, 0.9f, 1.0f);
            sp.awardStat(Stats.ITEM_USED.get(this));
            if (!sp.getAbilities().instabuild) {
                stack.shrink(1);
                ItemStack bowl = new ItemStack(net.minecraft.world.item.Items.BOWL);
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
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> out, TooltipFlag flag) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        boolean hasHot = data != null && data.copyTag().contains(HOT_UNTIL_KEY);
        if (hasHot) {
            out.accept(Component.literal("Steaming hot - let it cool!").withStyle(ChatFormatting.RED));
            out.accept(Component.literal("Drinking now scalds (2 damage + thirst)").withStyle(ChatFormatting.GRAY));
            if (purified) {
                out.accept(Component.literal("Purified, but still too hot").withStyle(ChatFormatting.AQUA));
            } else {
                out.accept(Component.literal("Dirty hot water").withStyle(ChatFormatting.YELLOW));
            }
        } else {
            out.accept(Component.literal("Hot water - handle with care").withStyle(ChatFormatting.RED));
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        // Red tint to indicate hot
        return Component.literal(super.getName(stack).getString()).withStyle(ChatFormatting.RED);
    }
}
