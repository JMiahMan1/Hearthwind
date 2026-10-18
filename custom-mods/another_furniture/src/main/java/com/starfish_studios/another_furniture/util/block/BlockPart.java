package com.starfish_studios.another_furniture.util.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.phys.Vec3;

public class BlockPart {
   public static int get1D(BlockPos blockPos, Vec3 clickedPos, Direction axisDir, int parts) {
      Axis axis = axisDir.getAxis();
      double posNormal = clickedPos.get(axis) - blockPos.get(axis);
      if (axisDir == Direction.SOUTH || axisDir == Direction.EAST || axisDir == Direction.UP && posNormal != 0.5) {
         posNormal = 1.0 - posNormal;
      }

      double partSize = 1.0 / parts;
      return (int)Math.min(posNormal / partSize, (double)(parts - 1));
   }

   public static int get2D(BlockPos blockPos, Vec3 clickedPos, Direction axisDir, Direction axisDir2, int partsX, int partsY) {
      int slotX = get1D(blockPos, clickedPos, axisDir, partsX);
      int slotY = get1D(blockPos, clickedPos, axisDir2, partsY);
      return partsX * slotY + slotX;
   }
}
