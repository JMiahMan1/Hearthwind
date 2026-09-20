package net.dungeonz.mixin.block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dungeonz.init.ConfigInit;
import net.dungeonz.init.DimensionInit;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.util.RandomSource;

@Mixin(VineBlock.class)
public abstract class VineBlockMixin extends Block {

    public VineBlockMixin(Properties settings) {
        super(settings);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (ConfigInit.CONFIG.devMode && world.getBlockState(pos).is(Blocks.VINE)) {
            if (!world.isClientSide()) {
                if (world.isEmptyBlock(pos.below())) {
                    world.setBlock(pos.below(), (BlockState) this.defaultBlockState().setValue(VineBlock.getPropertyForFace(hit.getDirection().getOpposite()), true), Block.UPDATE_CLIENTS);
                }
            }
            return (world.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME);
        }
        return super.useWithoutItem(state, world, pos, player, hit);
    }

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random, CallbackInfo info) {
        if (world.dimension() == DimensionInit.DUNGEON_WORLD || ConfigInit.CONFIG.devMode) {
            info.cancel();
        }
    }

}
