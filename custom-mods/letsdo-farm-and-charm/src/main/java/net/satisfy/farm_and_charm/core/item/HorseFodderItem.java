package net.satisfy.farm_and_charm.core.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.satisfy.farm_and_charm.core.registry.MobEffectRegistry;
import net.satisfy.farm_and_charm.platform.PlatformHelper;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HorseFodderItem extends Item {
    public HorseFodderItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(ItemStack stack, net.minecraft.world.entity.player.Player player, LivingEntity entity, net.minecraft.world.InteractionHand hand) {
        if (entity instanceof Horse horse) {
            if (!entity.level().isClientSide()) {
                if (PlatformHelper.isHorseTamingEnabled() && !horse.isTamed()) {
                    horse.tameWithName(player);
                    horse.setOwner(player);
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                    return (entity.level().isClientSide() ? InteractionResult.SUCCESS_SERVER : InteractionResult.SUCCESS);
                }

                if (PlatformHelper.isHorseEffectsEnabled()) {
                    horse.addEffect(new MobEffectInstance(MobEffectRegistry.getHolder(MobEffectRegistry.HORSE_FODDER), 6000, 0));
                    horse.heal(10.0F);
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                }
            } else {
                Level world = entity.level();
                world.addParticle(ParticleTypes.HEART, entity.getX(), entity.getY() + 1.0, entity.getZ(), 0.0, 1.0, 1.0);
                world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.HORSE_EAT, entity.getSoundSource(), 1.0f, 1.0f);
            }
            return (entity.level().isClientSide() ? InteractionResult.SUCCESS_SERVER : InteractionResult.SUCCESS);
        }
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, @NotNull Item.TooltipContext tooltipContext, @NotNull net.minecraft.world.item.component.TooltipDisplay display, @NotNull java.util.function.Consumer<Component> tooltip, @NotNull TooltipFlag tooltipFlag) {
        if (PlatformHelper.isHorseEffectsEnabled()) {
            tooltip.accept(Component.translatable("tooltip.farm_and_charm.animal_fed_to_horse").withStyle(ChatFormatting.GRAY));
            tooltip.accept(Component.translatable("tooltip.farm_and_charm.horse_effect_1").withStyle(ChatFormatting.BLUE));
            tooltip.accept(Component.translatable("tooltip.farm_and_charm.horse_effect_2").withStyle(ChatFormatting.BLUE));
        }
    }
}

