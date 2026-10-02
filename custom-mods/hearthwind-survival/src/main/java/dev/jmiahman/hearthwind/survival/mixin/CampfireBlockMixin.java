package dev.jmiahman.hearthwind.survival.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.jmiahman.hearthwind.survival.CampfirePurification;

/**
 * Vanilla 26.2 gates campfire placement on
 * {@code RecipePropertySet.CAMPFIRE_INPUT} before ever calling
 * {@code CampfireBlockEntity.placeFood}, so a water bottle is rejected with
 * no recipe. The reference Dehydration mod hooked {@code CampfireBlock.use}
 * for exactly this; mirror it here.
 */
@Mixin(CampfireBlock.class)
public abstract class CampfireBlockMixin {
    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void hearthwind$placeWaterBottle(ItemStack itemStack, BlockState state, Level level,
            BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult,
            CallbackInfoReturnable<InteractionResult> cir) {
        if (!CampfirePurification.isWaterPotion(itemStack)
                || !(level.getBlockEntity(pos) instanceof CampfireBlockEntity campfire)) {
            return;
        }
        if (level instanceof ServerLevel serverLevel
                && CampfirePurification.placeWaterBottle(serverLevel, player, campfire, itemStack)) {
            player.awardStat(Stats.INTERACT_WITH_CAMPFIRE);
            // No lit check and no warning: CampfireBlockMixin in the reference
            // accepts the bottle on a cold fire and says nothing (the boil
            // only completes in litServerTick). We added a chat hint in 0.1.33;
            // under the parity rule it is gone again (0.1.49).
            cir.setReturnValue(InteractionResult.SUCCESS_SERVER);
        } else {
            cir.setReturnValue(InteractionResult.CONSUME);
        }
    }
}
