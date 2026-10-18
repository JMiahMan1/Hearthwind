package com.chailotl.particular.mixin;

import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({ChestBlockEntity.class})
public interface AccessorChestBlockEntity {
   @Accessor
   ContainerOpenersCounter getStateManager();

   @Accessor
   ChestLidController getLidAnimator();
}
