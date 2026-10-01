package dev.jmiahman.hearthwind.survival.mixin;

import dev.jmiahman.hearthwind.survival.additionz.AdditionZParity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Rain puts out a campfire.
 *
 * <p>The reference keeps a per-campfire counter in NBT, samples it once a second
 * while the fire is lit and the sky is visible, and calls
 * {@code CampfireBlock.extinguish} when a roll comes up zero. The two conditions
 * are a pair: the counter only climbs while it is at or below the limit, so the
 * first sixty samples of Aged's setting do nothing but count and the earliest
 * fire dies on the sixty-first second.
 *
 * <p>26.2 has no {@code litServerTick} or {@code unlitServerTick} any more -
 * {@code CampfireBlock.getTicker} picks {@code cookTick} on a lit server level
 * and {@code cooldownTick} on a dark one - so the two halves are injected at the
 * HEAD of each. HEAD rather than the reference's TAIL on purpose: our own
 * {@code hearthwind$freezeBoilOnDarkFire} cancels {@code cooldownTick} while a
 * bottle is boiling, and a TAIL inject would never run behind a cancel.
 */
@Mixin(CampfireBlockEntity.class)
public abstract class CampfireRainMixin {

    @Unique
    private int hearthwind$rainBurnTime;

    @Inject(method = "cookTick", at = @At("HEAD"))
    private static void hearthwind$rainPutsOutTheFire(ServerLevel level, BlockPos pos, BlockState state,
            CampfireBlockEntity entity,
            RecipeManager.CachedCheck<SingleRecipeInput, CampfireCookingRecipe> recipeCache,
            CallbackInfo ci) {
        // The ticker is static, so the @Unique counter has to be reached
        // through the mixin's own class: at runtime the block entity IS this
        // mixin, and casting through Object keeps the compiler satisfied.
        CampfireRainMixin campfire = (CampfireRainMixin) (Object) entity;
        int limit = AdditionZParity.config().campfireRainExtinguish;
        if (!AdditionZParity.countsRain(level, pos, limit)
                || !AdditionZParity.isRainSampleTick(level.getGameTime())) {
            return;
        }
        int next = AdditionZParity.rainSample(campfire.hearthwind$rainBurnTime, limit,
                level.getRandom().nextInt(limit));
        if (next < 0) {
            level.setBlockAndUpdate(pos, level.getBlockState(pos).setValue(
                    net.minecraft.world.level.block.CampfireBlock.LIT, false));
            return;
        }
        campfire.hearthwind$rainBurnTime = next;
        entity.setChanged();
    }

    @Inject(method = "cooldownTick", at = @At("HEAD"))
    private static void hearthwind$darkFireForgetsTheRain(Level level, BlockPos pos, BlockState state,
            CampfireBlockEntity entity, CallbackInfo ci) {
        CampfireRainMixin campfire = (CampfireRainMixin) (Object) entity;
        if (campfire.hearthwind$rainBurnTime != 0) {
            campfire.hearthwind$rainBurnTime = 0;
            entity.setChanged();
        }
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void hearthwind$readRainTime(ValueInput input, CallbackInfo ci) {
        hearthwind$rainBurnTime = input.getIntOr(AdditionZParity.RAIN_BURN_TIME, 0);
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void hearthwind$writeRainTime(ValueOutput output, CallbackInfo ci) {
        output.putInt(AdditionZParity.RAIN_BURN_TIME, hearthwind$rainBurnTime);
    }
}
