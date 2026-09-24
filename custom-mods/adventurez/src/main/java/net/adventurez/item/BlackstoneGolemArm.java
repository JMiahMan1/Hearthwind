package net.adventurez.item;

import java.util.function.Consumer;

import com.mojang.blaze3d.platform.InputConstants;

import net.adventurez.entity.nonliving.ThrownRockEntity;
import net.adventurez.init.ConfigInit;
import net.adventurez.init.ItemInit;
import net.adventurez.init.SoundInit;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class BlackstoneGolemArm extends Item {

    public BlackstoneGolemArm(Item.Properties settings) {
        super(settings);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        if (ConfigInit.CONFIG.allow_extra_tooltips) {
            if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), 340)) {
                tooltip.accept(Component.translatable("item.adventurez.blackstone_golem_arm.tooltip"));
            } else {
                tooltip.accept(Component.translatable("item.adventurez.moreinfo.tooltip"));
            }
        }
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity user, int remainingUseTicks) {
        if (!(user instanceof Player player)) {
            return false;
        }

        int stoneCounter = getUseDuration(stack, user) - remainingUseTicks;
        if (stoneCounter < 30) {
            return false;
        }

        stack.set(ItemInit.LAVA_LIGHT, false);
        if (!level.isClientSide()) {
            float strength = getStoneStrength(stoneCounter);
            stack.hurtAndBreak(1, player, user.getUsedItemHand().asEquipmentSlot());
            ThrownRockEntity thrownRockEntity = new ThrownRockEntity(level, player);
            thrownRockEntity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, strength * 1.2F, 0.0F);
            level.addFreshEntity(thrownRockEntity);
            level.playSound(null, thrownRockEntity, SoundInit.ROCK_THROW_EVENT, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        return true;
    }

    @Override
    public InteractionResult use(Level level, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);
        if (itemStack.getDamageValue() >= itemStack.getMaxDamage() - 1) {
            return InteractionResult.FAIL;
        }
        user.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        if (!(entity instanceof Player player)) {
            return;
        }

        if (slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) {
            player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 9, 0, false, false, false));
        }

        boolean usingItem = player.isUsingItem() && player.getUseItem() == stack;
        if (usingItem && player.getUseItemRemainingTicks() < 71970) {
            stack.set(ItemInit.LAVA_LIGHT, true);
        } else if (!usingItem && Boolean.TRUE.equals(stack.get(ItemInit.LAVA_LIGHT))) {
            stack.set(ItemInit.LAVA_LIGHT, false);
        }
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.NONE;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 72000;
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        Vec3 view = attacker.getViewVector(1.0F);
        double xVector = view.x() / 2D;
        double zVector = view.z() / 2D;
        EquipmentSlot slot = attacker.getMainHandItem() == stack ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
        stack.hurtAndBreak(1, attacker, slot);
        target.push(xVector, 0.45D, zVector);
    }

    public static float getStoneStrength(int useTicks) {
        float strength = (float) useTicks / 20.0F;
        strength = (strength * strength + strength * 2.0F) / 3.0F;
        return Math.min(strength, 1.0F);
    }
}
