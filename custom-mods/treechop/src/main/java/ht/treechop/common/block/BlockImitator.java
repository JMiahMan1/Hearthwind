package ht.treechop.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome.Precipitation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import org.jetbrains.annotations.NotNull;

public abstract class BlockImitator extends Block {
   public BlockImitator(Properties properties) {
      super(properties);
   }

   public abstract BlockState getImitatedBlockState(BlockGetter var1, BlockPos var2);

   public void animateTick(BlockState blockState, Level level, BlockPos pos, RandomSource random) {
      BlockState imitatedBlockState = this.getImitatedBlockState(level, pos);
      imitatedBlockState.getBlock().animateTick(imitatedBlockState, level, pos, random);
   }

   public void stepOn(Level level, BlockPos pos, BlockState blockState, Entity entity) {
      BlockState imitatedBlockState = this.getImitatedBlockState(level, pos);
      imitatedBlockState.getBlock().stepOn(level, pos, imitatedBlockState, entity);
   }

   public void fallOn(Level level, BlockState blockState, BlockPos pos, Entity entity, double speed) {
      BlockState imitatedBlockState = this.getImitatedBlockState(level, pos);
      imitatedBlockState.getBlock().fallOn(level, imitatedBlockState, pos, entity, speed);
   }

   @Override
   public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState blockState, boolean bl) {
      BlockState imitatedBlockState = this.getImitatedBlockState(level, pos);
      return imitatedBlockState.getCloneItemStack(level, pos, bl);
   }

   public void handlePrecipitation(BlockState blockState, Level level, BlockPos pos, Precipitation precipitation) {
      BlockState imitatedBlockState = this.getImitatedBlockState(level, pos);
      imitatedBlockState.getBlock().handlePrecipitation(imitatedBlockState, level, pos, precipitation);
   }

   public float getShadeBrightness(BlockState blockState, BlockGetter level, BlockPos pos) {
      return super.getShadeBrightness(blockState, level, pos);
   }

   public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos pos, Direction direction) {
      return this.getImitatedBlockState(level, pos).getAnalogOutputSignal(level, pos, direction);
   }

   public void randomTick(BlockState blockState, ServerLevel level, BlockPos pos, RandomSource random) {
      this.getImitatedBlockState(level, pos).randomTick(level, pos, random);
   }

   public void tick(BlockState blockState, ServerLevel level, BlockPos pos, RandomSource random) {
      this.getImitatedBlockState(level, pos).tick(level, pos, random);
   }

   public int getSignal(BlockState blockState, BlockGetter level, BlockPos pos, Direction direction) {
      return this.getImitatedBlockState(level, pos).getSignal(level, pos, direction);
   }

   public int getDirectSignal(BlockState blockState, BlockGetter level, BlockPos pos, Direction direction) {
      return this.getImitatedBlockState(level, pos).getDirectSignal(level, pos, direction);
   }
}
