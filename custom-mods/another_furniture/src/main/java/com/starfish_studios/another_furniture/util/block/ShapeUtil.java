package com.starfish_studios.another_furniture.util.block;

import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class ShapeUtil {
   public static VoxelShape rotateShape(VoxelShape source, Direction direction) {
      AtomicReference<net.minecraft.world.phys.shapes.VoxelShape> newShape = new AtomicReference<>(Shapes.empty());
      source.forAllBoxes(
         (minX, minY, minZ, maxX, maxY, maxZ) -> {
            Vec3 min = new Vec3(minX - 0.5, minY - 0.5, minZ - 0.5);
            Vec3 max = new Vec3(maxX - 0.5, maxY - 0.5, maxZ - 0.5);
            Vec3 v1 = rotateVec3(min, direction);
            Vec3 v2 = rotateVec3(max, direction);
            VoxelShape s = Shapes.create(
               0.5 + Math.min(v1.x, v2.x),
               0.5 + Math.min(v1.y, v2.y),
               0.5 + Math.min(v1.z, v2.z),
               0.5 + Math.max(v1.x, v2.x),
               0.5 + Math.max(v1.y, v2.y),
               0.5 + Math.max(v1.z, v2.z)
            );
            newShape.set(Shapes.or((VoxelShape)newShape.get(), s));
         }
      );
      return (VoxelShape)newShape.get();
   }

   public static Vec3 rotateVec3(Vec3 vec, Direction dir) {
      double cos = 1.0;
      double sin = 0.0;
      switch (dir) {
         case SOUTH:
            cos = -1.0;
            sin = 0.0;
            break;
         case WEST:
            cos = 0.0;
            sin = 1.0;
            break;
         case EAST:
            cos = 0.0;
            sin = -1.0;
      }

      double d0 = vec.x * cos + vec.z * sin;
      double d1 = vec.y;
      double d2 = vec.z * cos - vec.x * sin;
      return new Vec3(d0, d1, d2);
   }
}
