package com.chailotl.particular.mixin;

import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({EnderChestBlockEntity.class})
public interface AccessorEnderChestBlockEntity {
   @Accessor
   ContainerOpenersCounter getStateManager();

   @Accessor
   ChestLidController getLidAnimator();
}
