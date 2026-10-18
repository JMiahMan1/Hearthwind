package net.voidz.block;

import net.minecraft.util.RandomSource;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.voidz.block.entity.VoidStoneBlockEntity;
import org.jetbrains.annotations.Nullable;

public class VoidStoneBlock extends Block implements EntityBlock {
   public static final BooleanProperty ACTIVATED = BooleanProperty.create("activated_void");
   public static final IntegerProperty DESTROYTIME = IntegerProperty.create("destroy_time", 0, 7);

   public VoidStoneBlock(Properties settings) {
      super(settings);
      this.registerDefaultState((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(ACTIVATED, false)).setValue(DESTROYTIME, 0));
   }

   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new VoidStoneBlockEntity(pos, state);
   }

   public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
      if (placer != null && placer.isShiftKeyDown()) {
         world.setBlockAndUpdate(pos, (BlockState)state.setValue(ACTIVATED, true));
      }
   }

   protected void createBlockStateDefinition(
      net.minecraft.world.level.block.state.StateDefinition.Builder<net.minecraft.world.level.block.Block, net.minecraft.world.level.block.state.BlockState> stateManager
   ) {
      stateManager.add(new Property[]{ACTIVATED});
      stateManager.add(new Property[]{DESTROYTIME});
   }

   @Environment(EnvType.CLIENT)
   public void randomDisplayTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
      if (random.nextInt(10) == 0) {
         world.addParticle(
            ParticleTypes.PORTAL,
            pos.getX() + random.nextDouble(),
            pos.getY() + 1.08,
            pos.getZ() + random.nextDouble(),
            0.0,
            0.0,
            0.0
         );
      }
   }
}
