package ht.treechop.client.model;

import ht.treechop.common.block.ChoppedLogBlock;
import ht.treechop.common.chop.ChopUtil;
import ht.treechop.common.properties.ChoppedLogShape;
import ht.tuber.math.Vector3;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ChoppedLogModel implements BlockStateModel {
   private static final List<Direction> SIDES = Arrays.asList(
      null, Direction.UP, Direction.DOWN, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST
   );

   private final Material.Baked particleMaterial;

   public ChoppedLogModel(Material.Baked particleMaterial) {
      this.particleMaterial = particleMaterial;
   }

   @Override
   public void collectParts(RandomSource random, List<BlockStateModelPart> list) {
   }

   @Override
   public Material.Baked particleMaterial() {
      return this.particleMaterial;
   }

   @Override
   public @BakedQuad.MaterialFlags int materialFlags() {
      return 0;
   }

   @Override
   public void emitQuads(
      QuadEmitter emitter,
      BlockAndTintGetter level,
      BlockPos pos,
      BlockState state,
      RandomSource random,
      Predicate<@Nullable Direction> cullTest
   ) {
      if (level.getBlockEntity(pos) instanceof ChoppedLogBlock.MyEntity entity) {
         BlockState strippedState = ChopUtil.getStrippedState(level, pos, entity.getOriginalState());
         Map<Direction, BlockState> strippedNeighbors = getStrippedNeighbors(level, pos, entity);
         for (BakedQuad quad : getQuads(strippedState, entity.getShape(), entity.getRadius(), random, strippedNeighbors)) {
            if (quad.direction() != null && cullTest.test(quad.direction())) {
               continue;
            }
            emitter.fromBakedQuad(quad).emit();
         }
      }
   }

   private static BlockState getStrippedNeighbor(BlockAndTintGetter level, BlockPos pos, Direction direction) {
      BlockPos neighborPos = pos.relative(direction);
      return ChopUtil.getStrippedState(level, pos, level.getBlockState(neighborPos));
   }

   private static Map<Direction, BlockState> getStrippedNeighbors(BlockAndTintGetter level, BlockPos pos, ChoppedLogBlock.MyEntity entity) {
      return entity.getOriginalState().isSolidRender()
         ? entity.streamSolidSides(level, pos).collect(Collectors.toMap(side -> side, side -> getStrippedNeighbor(level, pos, side)))
         : Collections.emptyMap();
   }

   private static List<BakedQuad> getBlockQuads(BlockState blockState, Direction side, RandomSource rand) {
      BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(blockState);
      List<BlockStateModelPart> parts = new ArrayList<>();
      model.collectParts(rand, parts);
      List<BakedQuad> quads = new ArrayList<>();
      for (BlockStateModelPart part : parts) {
         quads.addAll(part.getQuads(side));
      }
      return quads;
   }

   private static List<BakedQuad> getQuads(
      BlockState strippedState,
      ChoppedLogShape shape,
      int radius,
      RandomSource random,
      Map<Direction, BlockState> strippedNeighbors
   ) {
      AABB box = shape.getBoundingBox(radius);
      Vector3 mins = new Vector3(box.minX, box.minY, box.minZ);
      Vector3 maxes = new Vector3(box.maxX, box.maxY, box.maxZ);
      Stream<BakedQuad> baseQuads = SIDES.stream()
         .flatMap(
            side -> getBlockQuads(strippedState, side, random)
               .stream()
               .map(quad -> ModelUtil.trimQuad(quad, mins, maxes))
         );
      Stream<BakedQuad> neighborQuads = strippedNeighbors
         .entrySet()
         .stream()
         .flatMap(
            entry -> {
               Direction side = entry.getKey();
               BlockState neighbor = entry.getValue();
               net.minecraft.core.Vec3i normal = side.getUnitVec3i().multiply(16);
               Vector3 translation = new Vector3(normal.getX(), normal.getY(), normal.getZ());
               return getBlockQuads(neighbor, side.getOpposite(), random)
                  .stream()
                  .map(quad -> ModelUtil.translateQuad(quad, translation));
            }
         );
      return Stream.concat(baseQuads, neighborQuads).toList();
   }
}
