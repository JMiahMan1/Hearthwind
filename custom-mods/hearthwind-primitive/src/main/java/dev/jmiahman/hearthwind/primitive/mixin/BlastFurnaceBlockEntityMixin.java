package dev.jmiahman.hearthwind.primitive.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlastFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Earlystage parity: the blast furnace inventory gains a 4th slot (index 3)
 * which holds the "extra ingredient" (coal) for extra-blasting recipes.
 */
@Mixin(BlastFurnaceBlockEntity.class)
public abstract class BlastFurnaceBlockEntityMixin {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void hearthwind$fourSlots(BlockPos pos, BlockState state, CallbackInfo ci) {
        ((AbstractFurnaceBlockEntityItemsAccessor) (Object) this).hearthwind$setItemsList(
                NonNullList.withSize(4, ItemStack.EMPTY));
    }
}
