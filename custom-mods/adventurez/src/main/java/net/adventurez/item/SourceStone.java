package net.adventurez.item;

import java.util.function.Consumer;

import com.mojang.blaze3d.platform.InputConstants;

import net.adventurez.entity.nonliving.VoidBulletEntity;
import net.adventurez.init.ConfigInit;
import net.adventurez.init.SoundInit;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class SourceStone extends Item {

    public SourceStone(Item.Properties settings) {
        super(settings);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        if (ConfigInit.CONFIG.allow_extra_tooltips) {
            if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), 340)) {
                tooltip.accept(Component.translatable("item.adventurez.source_stone.tooltip"));
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

        boolean used = false;
        if (!level.isClientSide()) {
            if (ConfigInit.CONFIG.allow_source_stone_tp && player.isShiftKeyDown()) {
                HitResult hitResult = player.pick(8D, 1.0F, false);
                if (hitResult.getType() == HitResult.Type.BLOCK) {
                    BlockHitResult blockHitResult = (BlockHitResult) hitResult;
                    used = tryTeleport((ServerLevel) level, blockHitResult.getBlockPos(), blockHitResult.getDirection(), player);
                } else if (hitResult.getType() == HitResult.Type.ENTITY) {
                    EntityHitResult entityHitResult = (EntityHitResult) hitResult;
                    used = tryTeleport((ServerLevel) level, entityHitResult.getEntity().blockPosition(), entityHitResult.getEntity().getDirection(), player);
                }
            } else {
                Vec3 view = player.getViewVector(1.0F);
                VoidBulletEntity voidBulletEntity = new VoidBulletEntity(player, view, level);
                level.addFreshEntity(voidBulletEntity);
                level.playSound(null, voidBulletEntity, SoundInit.SHADOW_CAST_EVENT, SoundSource.PLAYERS, 1.0F, 1.0F);
                used = true;
            }
        }
        player.getCooldowns().addCooldown(stack, 40);
        return used;
    }

    private boolean tryTeleport(ServerLevel level, BlockPos blockPos, Direction direction, Player player) {
        if (level.getBlockState(blockPos).isAir()) {
            return false;
        }

        for (int distance = 2; distance < 6; distance++) {
            BlockPos newBlockPos = blockPos.relative(direction.getOpposite(), distance);
            BlockPos upperBlockPos = newBlockPos.above();
            if (!level.getBlockState(newBlockPos).blocksMotion() && !level.getBlockState(upperBlockPos).blocksMotion()) {
                player.teleport(new TeleportTransition(level, Vec3.atBottomCenterOf(newBlockPos), player.getDeltaMovement(), player.getYRot(), player.getXRot(),
                        TeleportTransition.DO_NOTHING));
                level.playSound(null, player, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
                return true;
            }
        }
        return false;
    }

    @Override
    public InteractionResult use(Level level, Player user, InteractionHand hand) {
        user.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }
}
