package dev.sapphic.couplings.mixin;

import dev.sapphic.couplings.impl.DoorBlockCoupling;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DoorBlock.class)
abstract class DoorBlockMixin extends Block {
  DoorBlockMixin(final Properties properties) {
    super(properties);
  }

  @Inject(
      method =
          "useWithoutItem("
              + "Lnet/minecraft/world/level/block/state/BlockState;"
              + "Lnet/minecraft/world/level/Level;"
              + "Lnet/minecraft/core/BlockPos;"
              + "Lnet/minecraft/world/entity/player/Player;"
              + "Lnet/minecraft/world/phys/BlockHitResult;"
              + ")Lnet/minecraft/world/InteractionResult;",
      at = @At("TAIL"),
      require = 1)
  private void used(
      final BlockState state,
      final Level level,
      final BlockPos pos,
      final Player player,
      final BlockHitResult hit,
      final CallbackInfoReturnable<InteractionResult> cir) {
    DoorBlockCoupling.used(state, level, pos, player);
  }

  @Inject(
      method =
          "setOpen("
              + "Lnet/minecraft/world/entity/Entity;"
              + "Lnet/minecraft/world/level/Level;"
              + "Lnet/minecraft/world/level/block/state/BlockState;"
              + "Lnet/minecraft/core/BlockPos;"
              + "Z"
              + ")V",
      at = @At("TAIL"),
      require = 1)
  private void openStateChanged(
      final Entity entity,
      final Level level,
      final BlockState state,
      final BlockPos pos,
      final boolean open,
      final CallbackInfo ci) {
    DoorBlockCoupling.openStateChanged(entity, state, level, pos, open);
  }

  @Inject(
      method =
          "neighborChanged("
              + "Lnet/minecraft/world/level/block/state/BlockState;"
              + "Lnet/minecraft/world/level/Level;"
              + "Lnet/minecraft/core/BlockPos;"
              + "Lnet/minecraft/world/level/block/Block;"
              + "Lnet/minecraft/world/level/redstone/Orientation;"
              + "Z"
              + ")V",
      at = @At("TAIL"),
      require = 1)
  private void neighborChanged(
      final BlockState state,
      final Level level,
      final BlockPos pos,
      final Block block,
      final Orientation orientation,
      final boolean movedByPiston,
      final CallbackInfo ci) {
    final var powered = level.hasNeighborSignal(pos);
    DoorBlockCoupling.neighborChanged(state, level, pos, powered);
  }
}
