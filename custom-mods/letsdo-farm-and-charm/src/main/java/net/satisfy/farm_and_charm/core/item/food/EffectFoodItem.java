package net.satisfy.farm_and_charm.core.item.food;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EffectFoodItem extends Item implements EffectFood {

    private final int foodStages;

    public EffectFoodItem(Properties settings, int foodStages) {
        super(settings);
        this.foodStages = foodStages;
    }

    @Override
    public @NotNull ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        if (!world.isClientSide()) {
            List<Pair<MobEffectInstance, Float>> effects = EffectFoodHelper.getEffects(stack);
            for (Pair<MobEffectInstance, Float> effect : effects) {
                if (effect.getFirst() != null && world.getRandom().nextFloat() < effect.getSecond()) {
                    user.addEffect(new MobEffectInstance(effect.getFirst()));
                }
            }
        }
        int stage = EffectFoodHelper.getStage(stack);
        int slot = -1;
        Inventory inv = null;
        if (user instanceof Player player && !player.isCreative()) {
            inv = player.getInventory();
            slot = inv.findSlotMatchingItem(stack);
        }
        ItemStack eaten = stack.getOrDefault(net.minecraft.core.component.DataComponents.CONSUMABLE, net.minecraft.world.item.component.Consumable.builder().build()).onConsume(world, user, stack);
        if (inv != null && stage < this.foodStages) {
            ItemStack next = EffectFoodHelper.setStage(new ItemStack(this), stage + 1);
            if (slot >= 0 && slot < inv.getContainerSize()) {
                if (inv.getItem(slot).isEmpty()) {
                    inv.add(slot, next);
                    return eaten;
                }
            }
            int space = inv.getSlotWithRemainingSpace(next);
            if (space >= 0 && space < inv.getContainerSize()) {
                inv.add(space, next);
            }
        }
        return eaten;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, @NotNull Item.TooltipContext tooltipContext, @NotNull net.minecraft.world.item.component.TooltipDisplay display, @NotNull java.util.function.Consumer<Component> list, @NotNull TooltipFlag tooltipFlag) {
        EffectFoodHelper.getTooltip(itemStack, tooltipContext, list);
    }
}
