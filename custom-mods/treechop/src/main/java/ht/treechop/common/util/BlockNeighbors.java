package ht.treechop.common.util;

import java.util.Arrays;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;

public class BlockNeighbors {
   protected final BlockPos[] blocks;
   public static final BlockNeighbors UP = new BlockNeighbors(new BlockPos[]{new BlockPos(Direction.UP.getUnitVec3i())});
   public static final BlockNeighbors DOWN = new BlockNeighbors(new BlockPos[]{new BlockPos(Direction.DOWN.getUnitVec3i())});
   public static final BlockNeighbors HORIZONTAL_ADJACENTS = new BlockNeighbors(
      new BlockPos[]{new BlockPos(-1, 0, 0), new BlockPos(0, 0, -1), new BlockPos(1, 0, 0), new BlockPos(0, 0, 1)}
   );
   public static final BlockNeighbors HORIZONTAL_DIAGONALS = new BlockNeighbors(
      Stream.of(new BlockPos(-1, 0, -1), new BlockPos(-1, 0, 1), new BlockPos(1, 0, -1), new BlockPos(1, 0, 1)).toArray(BlockPos[]::new)
   );
   public static final BlockNeighbors VERTICAL_ADJACENTS = join(UP, DOWN);
   public static final BlockNeighbors ADJACENTS = join(HORIZONTAL_ADJACENTS, VERTICAL_ADJACENTS);
   public static final BlockNeighbors HORIZONTAL = join(HORIZONTAL_ADJACENTS, HORIZONTAL_DIAGONALS);
   public static final BlockNeighbors ABOVE = join(UP, offset(new BlockPos(0, 1, 0), HORIZONTAL));
   public static final BlockNeighbors HORIZONTAL_AND_ABOVE = join(HORIZONTAL, ABOVE);
   public static final BlockNeighbors BELOW = join(DOWN, offset(new BlockPos(0, -1, 0), HORIZONTAL));
   public static final BlockNeighbors ADJACENTS_AND_DIAGONALS = join(ABOVE, HORIZONTAL, BELOW);
   public static final BlockNeighbors ADJACENTS_AND_BELOW = join(ADJACENTS, BELOW);

   public BlockNeighbors(BlockPos[] blocks) {
      this.blocks = blocks;
   }

   public static BlockNeighbors join(BlockNeighbors... neighbors) {
      return new BlockNeighbors(Arrays.stream(neighbors).flatMap(BlockNeighbors::asStream).distinct().toArray(BlockPos[]::new));
   }

   protected static BlockNeighbors offset(Vec3i offset, BlockNeighbors neighbors) {
      return new BlockNeighbors(neighbors.asStream().map(p -> p.offset(offset)).toArray(BlockPos[]::new));
   }

   protected Stream<net.minecraft.core.BlockPos> asStream() {
      return Arrays.stream(this.blocks);
   }

   public Stream<net.minecraft.core.BlockPos> asStream(net.minecraft.core.BlockPos pos) {
      return Arrays.stream(this.blocks).map(pos::offset);
   }
}
