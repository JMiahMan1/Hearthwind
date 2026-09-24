package net.adventurez.item;

import java.util.List;
import java.util.function.Consumer;

import com.mojang.blaze3d.platform.InputConstants;

import net.adventurez.entity.EnderWhaleEntity;
import net.adventurez.init.ConfigInit;
import net.adventurez.init.SoundInit;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class EnderFlute extends Item {

    public EnderFlute(Item.Properties settings) {
        super(settings);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        if (ConfigInit.CONFIG.allow_extra_tooltips) {
            if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), 340)) {
                tooltip.accept(Component.translatable("item.adventurez.ender_flute.tooltip"));
            } else {
                tooltip.accept(Component.translatable("item.adventurez.moreinfo.tooltip"));
            }
        }
    }

    @Override
    public InteractionResult use(Level level, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);
        level.playSound(null, user.getX(), user.getY(), user.getZ(), SoundInit.FLUTE_CALL_EVENT, SoundSource.PLAYERS, 1.0F,
                level.getRandom().nextFloat() * 0.2F + 0.9F);
        if (!level.isClientSide()) {
            itemStack.hurtAndBreak(1, user, hand.asEquipmentSlot());
            List<EnderWhaleEntity> whales = level.getEntitiesOfClass(EnderWhaleEntity.class,
                    new AABB(user.blockPosition()).inflate(100D), EntitySelector.NO_SPECTATORS);
            if (!whales.isEmpty()) {
                whales.getFirst().getMoveControl().setWantedPosition(user.getX(), user.getY(), user.getZ(), 1.0D);
            }
        }

        user.getCooldowns().addCooldown(itemStack, 40);
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        if (!(entity instanceof Player player)) {
            return;
        }

        float cooldownProgress = player.getCooldowns().getCooldownPercent(stack, 0.0F);
        if (cooldownProgress > 0.0F) {
            Vec3 view = player.getViewVector(1.0F);
            level.sendParticles(ParticleTypes.NOTE, player.getX() + view.x() + level.getRandom().nextDouble() * 0.4D,
                    player.getY() + player.getBbHeight() * 0.8D, player.getZ() + view.z() + level.getRandom().nextDouble() * 0.4D,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }
}
