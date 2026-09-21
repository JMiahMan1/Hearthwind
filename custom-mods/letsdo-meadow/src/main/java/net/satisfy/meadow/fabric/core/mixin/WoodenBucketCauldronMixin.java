package net.satisfy.meadow.fabric.core.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.satisfy.meadow.core.registry.ObjectRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraft.world.level.block.LayeredCauldronBlock.LEVEL;

/**
 * Lets wooden buckets interact with vanilla cauldrons the same way vanilla
 * buckets do (fill empty ones, scoop from full water ones).
 */
@Mixin(AbstractCauldronBlock.class)
public class WoodenBucketCauldronMixin {

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void meadow$woodenBucketUse(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        if (stack.is(ObjectRegistry.WOODEN_WATER_BUCKET.get()) && state.is(Blocks.CAULDRON)) {
            if (!level.isClientSide()) {
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(ObjectRegistry.WOODEN_BUCKET.get())));
                player.awardStat(Stats.FILL_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
                level.setBlockAndUpdate(pos, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LEVEL, 3));
                level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
            }
            cir.setReturnValue(level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME);
        } else if (stack.is(ObjectRegistry.WOODEN_BUCKET.get()) && state.is(Blocks.WATER_CAULDRON) && state.getValue(LEVEL) == 3) {
            if (!level.isClientSide()) {
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(ObjectRegistry.WOODEN_WATER_BUCKET.get())));
                player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
                LayeredCauldronBlock.lowerFillLevel(state, level, pos);
                level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
            }
            cir.setReturnValue(level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME);
        } else if (stack.is(ObjectRegistry.WOODEN_WATER_BUCKET.get()) && state.is(Blocks.WATER_CAULDRON)
                || stack.is(Items.POWDER_SNOW_BUCKET) && state.is(Blocks.POWDER_SNOW_CAULDRON)) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }
}
