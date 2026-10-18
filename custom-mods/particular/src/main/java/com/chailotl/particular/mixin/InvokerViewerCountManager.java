package com.chailotl.particular.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({ContainerOpenersCounter.class})
public interface InvokerViewerCountManager {
   @Invoker
   void invokeOnContainerOpen(Level var1, BlockPos var2, BlockState var3);

   @Invoker
   void invokeOnContainerClose(Level var1, BlockPos var2, BlockState var3);

   @Invoker
   void invokeScheduleBlockTick(Level var1, BlockPos var2, BlockState var3);
}
