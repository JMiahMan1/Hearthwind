package dev.jmiahman.hearthwind.primitive.mixin;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlastFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.jmiahman.hearthwind.primitive.extra.ExtraBlastingRecipe;
import dev.jmiahman.hearthwind.primitive.extra.ExtraBlastingRecipes;

/**
 * Earlystage extra-blasting server logic on the 26.2 furnace.
 *
 * serverTick matches via quickCheck with SingleRecipeInput(items[0]); we
 * redirect that lookup so a blast furnace with something in the extra slot
 * (3) resolves an earlystage:blasting_extra recipe instead. Burn consumption
 * is redirected to also consume the extra ingredient and the remaining main
 * count. getTotalCookTime/setItem keep the progress bar in sync.
 */
@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin {

    @Shadow
    private int cookingTimer;

    @Shadow
    private int cookingTotalTime;

    @Shadow
    private static int getTotalCookTime(ServerLevel level, AbstractFurnaceBlockEntity furnace) {
        throw new AssertionError();
    }

    @Shadow
    private static void burn(NonNullList<ItemStack> items, ItemStack input, ItemStack result) {
        throw new AssertionError();
    }

    @Redirect(method = "serverTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/crafting/RecipeManager$CachedCheck;getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeInput;Lnet/minecraft/server/level/ServerLevel;)Ljava/util/Optional;"))
    private static Optional<RecipeHolder<?>> hearthwind$extraMatch(RecipeManager.CachedCheck check,
            RecipeInput input, ServerLevel tickLevel, ServerLevel level, BlockPos pos, BlockState state,
            AbstractFurnaceBlockEntity furnace) {
        if (furnace instanceof BlastFurnaceBlockEntity && furnace.getContainerSize() > 3
                && !furnace.getItem(3).isEmpty()) {
            Optional<RecipeHolder<ExtraBlastingRecipe>> found = ExtraBlastingRecipes.findRecipe(
                    furnace.getItem(0), furnace.getItem(3), tickLevel);
            if (found.isPresent()) {
                return found.map(holder -> (RecipeHolder<?>) holder);
            }
        }
        return check.getRecipeFor(input, tickLevel);
    }

    @Redirect(method = "serverTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;burn(Lnet/minecraft/core/NonNullList;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)V"))
    private static void hearthwind$extraBurn(NonNullList<ItemStack> items, ItemStack input, ItemStack result,
            ServerLevel level, BlockPos pos, BlockState state, AbstractFurnaceBlockEntity furnace) {
        Optional<RecipeHolder<ExtraBlastingRecipe>> found = Optional.empty();
        if (furnace instanceof BlastFurnaceBlockEntity && items.size() > 3 && !items.get(3).isEmpty()) {
            found = ExtraBlastingRecipes.findRecipe(items.get(0), items.get(3), level);
        }
        burn(items, input, result);
        found.ifPresent(holder -> holder.value().consumeInputs(items));
    }

    @Inject(method = "getTotalCookTime", at = @At("HEAD"), cancellable = true)
    private static void hearthwind$extraCookTime(ServerLevel level, AbstractFurnaceBlockEntity furnace,
            CallbackInfoReturnable<Integer> cir) {
        if (furnace instanceof BlastFurnaceBlockEntity && furnace.getContainerSize() > 3
                && !furnace.getItem(3).isEmpty()) {
            ExtraBlastingRecipes.findRecipe(furnace.getItem(0), furnace.getItem(3), level)
                    .ifPresent(holder -> cir.setReturnValue(holder.value().cookingTime()));
        }
    }

    @Inject(method = "setItem", at = @At("TAIL"))
    private void hearthwind$extraSlotChanged(int slot, ItemStack stack, CallbackInfo ci) {
        AbstractFurnaceBlockEntity self = (AbstractFurnaceBlockEntity) (Object) this;
        if (slot == 3 && self.getContainerSize() > 3 && self.getLevel() instanceof ServerLevel serverLevel) {
            Optional<RecipeHolder<ExtraBlastingRecipe>> found =
                    ExtraBlastingRecipes.findRecipe(self.getItem(0), self.getItem(3), serverLevel);
            this.cookingTotalTime = found.map(holder -> holder.value().cookingTime())
                    .orElseGet(() -> getTotalCookTime(serverLevel, self));
            this.cookingTimer = 0;
            self.setChanged();
        }
    }
}
