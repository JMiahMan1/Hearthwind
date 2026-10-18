package net.smitherz.mixin;

import net.libz.access.ScreenHandlerAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SmithingMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({SmithingMenu.class})
public class SmithingScreenHandlerMixin implements ScreenHandlerAccess {
   @Unique
   private BlockPos pos;

   @Inject(
      method = {"<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V"},
      at = {@At("TAIL")}
   )
   private void initMixin(int syncId, Inventory inventory, ContainerLevelAccess context, CallbackInfo info) {
      context.execute((world, pos) -> this.setPos(pos));
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public void setPos(BlockPos pos) {
      this.pos = pos;
   }
}
