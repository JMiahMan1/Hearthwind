package dev.jmiahman.hearthwind.world.mixin;

import dev.jmiahman.hearthwind.world.HearthwindWorldConfig;
import dev.jmiahman.hearthwind.world.SeasonCrops;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * seasons.json {@code isSeasonMessingBonemeal}: out-of-season crops ignore
 * bone meal entirely (the item is not consumed), matching Serene Seasons.
 *
 * <p>{@code growCrop} is the crop branch of {@code useOn}; returning false
 * lets vanilla fall through to the water-plant branch, which does not apply,
 * so the interaction ends with PASS and the stack is untouched.
 */
@Mixin(BoneMealItem.class)
public class BoneMealItemSeasonMixin {

    @Inject(method = "growCrop", at = @At("HEAD"), cancellable = true)
    private static void hearthwind$blockOutOfSeasonBonemeal(ItemStack stack, Level level, BlockPos pos,
            CallbackInfoReturnable<Boolean> cir) {
        if (!HearthwindWorldConfig.get().messingBonemeal
                || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        double multiplier = SeasonCrops.multiplier(serverLevel.getBlockState(pos).getBlock(), serverLevel);
        if (SeasonCrops.blocksBonemeal(multiplier)) {
            cir.setReturnValue(false);
        }
    }
}
