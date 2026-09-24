package net.adventurez.item;

import java.util.function.Consumer;

import com.mojang.blaze3d.platform.InputConstants;

import net.adventurez.init.ConfigInit;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

public class PrimeEyeItem extends Item {
    public PrimeEyeItem(Item.Properties settings) {
        super(settings);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        if (ConfigInit.CONFIG.allow_extra_tooltips) {
            if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), 340)) {
                tooltip.accept(Component.translatable("item.adventurez.prime_eye.tooltip"));
            } else {
                tooltip.accept(Component.translatable("item.adventurez.moreinfo.tooltip"));
            }
        }
    }

    @Override
    public InteractionResult use(Level level, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);
        if (itemStack.getDamageValue() >= itemStack.getMaxDamage()) {
            return InteractionResult.PASS;
        }

        level.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENDER_PEARL_THROW, SoundSource.NEUTRAL, 0.5F,
                0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
        user.getCooldowns().addCooldown(itemStack, 20);
        if (!level.isClientSide()) {
            ThrownEnderpearl enderPearlEntity = new ThrownEnderpearl(level, user, itemStack.copyWithCount(1));
            enderPearlEntity.shootFromRotation(user, user.getXRot(), user.getYRot(), 0.0F, 1.5F, 0.0F);
            level.addFreshEntity(enderPearlEntity);
            itemStack.setDamageValue(itemStack.getDamageValue() + 1);
        }

        user.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResult.SUCCESS_SERVER;
    }
}
