package net.dungeonz.mixin.block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dungeonz.init.ItemInit;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.CartographyTableBlock;
import net.minecraft.world.phys.BlockHitResult;

@Mixin(CartographyTableBlock.class)
public abstract class CartographyTableBlockMixin extends Block {

    public CartographyTableBlockMixin(Properties settings) {
        super(settings);
    }

    @Inject(method = "useWithoutItem", at = @At("HEAD"), cancellable = true)
    private void onUseMixin(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> info) {
        if (player.getMainHandItem().is(ItemInit.DUNGEON_COMPASS)) {
            info.setReturnValue(InteractionResult.PASS);
        }
    }

}
