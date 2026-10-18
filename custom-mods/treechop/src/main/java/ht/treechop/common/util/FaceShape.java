package ht.treechop.common.util;

import ht.tuber.math.Box3;
import ht.tuber.math.Vector3;
import java.util.EnumMap;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

public enum FaceShape {
   DOWN(Direction.DOWN, new Vector3(0.0, 0.0, 0.0), new Vector3(1.0, 0.0, 0.0), new Vector3(1.0, 0.0, 1.0), new Vector3(0.0, 0.0, 1.0)),
   UP(Direction.UP, new Vector3(0.0, 1.0, 0.0), new Vector3(1.0, 1.0, 0.0), new Vector3(1.0, 1.0, 1.0), new Vector3(0.0, 1.0, 1.0)),
   NORTH(Direction.NORTH, new Vector3(0.0, 0.0, 0.0), new Vector3(1.0, 0.0, 0.0), new Vector3(1.0, 1.0, 0.0), new Vector3(0.0, 1.0, 0.0)),
   SOUTH(Direction.SOUTH, new Vector3(0.0, 0.0, 1.0), new Vector3(1.0, 0.0, 1.0), new Vector3(1.0, 1.0, 1.0), new Vector3(0.0, 1.0, 1.0)),
   WEST(Direction.WEST, new Vector3(0.0, 0.0, 0.0), new Vector3(0.0, 1.0, 0.0), new Vector3(0.0, 1.0, 1.0), new Vector3(0.0, 0.0, 1.0)),
   EAST(Direction.EAST, new Vector3(1.0, 0.0, 0.0), new Vector3(1.0, 1.0, 0.0), new Vector3(1.0, 1.0, 1.0), new Vector3(1.0, 0.0, 1.0));

   private final Box3 faceBox;
   private final Direction direction;
   private static final EnumMap<net.minecraft.core.Direction, FaceShape> fromDirections = new EnumMap<>(Direction.class);

   private FaceShape(net.minecraft.core.Direction direction, Vector3 corner1, Vector3 corner2, Vector3 corner3, Vector3 corner4) {
      this.direction = direction;
      Vector3 depthVector = new Vector3(direction.getStepX(), direction.getStepY(), direction.getStepZ()).scale(-0.01);
      Vector3 pushedCorner1 = corner1.add(depthVector);
      this.faceBox = new Box3(pushedCorner1, corner3);
   }

   public static FaceShape get(Direction direction) {
      return fromDirections.get(direction);
   }

   public AABB toAABB() {
      return new AABB(
         this.faceBox.getMinX(), this.faceBox.getMinY(), this.faceBox.getMinZ(), this.faceBox.getMaxX(), this.faceBox.getMaxY(), this.faceBox.getMaxZ()
      );
   }

   public Direction getDirection() {
      return this.direction;
   }

   static {
      fromDirections.put(Direction.DOWN, DOWN);
      fromDirections.put(Direction.UP, UP);
      fromDirections.put(Direction.NORTH, NORTH);
      fromDirections.put(Direction.SOUTH, SOUTH);
      fromDirections.put(Direction.WEST, WEST);
      fromDirections.put(Direction.EAST, EAST);
   }
}
