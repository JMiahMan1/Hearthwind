package net.dungeonz.mixin.block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dungeonz.init.ConfigInit;
import net.dungeonz.init.DimensionInit;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.server.level.ServerLevel;

@Mixin(FireBlock.class)
public class FireBlockMixin {

    @Inject(method = "isValidFireLocation", at = @At("HEAD"), cancellable = true)
    private void areBlocksAroundFlammableMixin(BlockGetter world, BlockPos pos, CallbackInfoReturnable<Boolean> info) {
        if ((world instanceof ServerLevel && ((ServerLevel) world).dimension() == DimensionInit.DUNGEON_WORLD) || ConfigInit.CONFIG.devMode) {
            info.cancel();
        }
    }

}
