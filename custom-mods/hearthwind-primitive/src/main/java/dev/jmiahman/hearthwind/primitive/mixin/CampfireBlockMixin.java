package dev.jmiahman.hearthwind.primitive.mixin;

import dev.jmiahman.hearthwind.primitive.HearthwindPrimitiveTags;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Bark lights an unlit campfire (earlystage parity): using any bark_items
 * item on an unlit campfire lights it and consumes the bark.
 */
@Mixin(net.minecraft.world.level.block.CampfireBlock.class)
abstract class CampfireBlockMixin {

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void hearthwind$barkLightsCampfire(ItemStack stack, BlockState state, net.minecraft.world.level.Level level,
            BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit,
            CallbackInfoReturnable<InteractionResult> cir) {
        if (!state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT)
                && stack.is(dev.jmiahman.hearthwind.primitive.HearthwindPrimitiveTags.BARK_ITEMS)) {
            if (!level.isClientSide()) {
                level.setBlock(pos, state.setValue(
                        net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT, Boolean.TRUE), 3);
                level.playSound(null, pos, net.minecraft.sounds.SoundEvents.FLINTANDSTEEL_USE,
                        net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);
                if (player != null) {
                    player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
                    if (!player.hasInfiniteMaterials()) {
                        stack.shrink(1);
                    }
                }
                cir.setReturnValue(InteractionResult.CONSUME);
            } else {
                cir.setReturnValue(InteractionResult.SUCCESS);
            }
        }
    }
}
