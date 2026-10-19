package net.mehvahdjukaar.smarterfarmers;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class FarmTaskLogic {
   public static ItemStack getHoe(Villager pEntity) {
      return switch (pEntity.getVillagerData().level()) {
         case 1 -> Items.WOODEN_HOE.getDefaultInstance();
         case 2 -> Items.STONE_HOE.getDefaultInstance();
         default -> Items.IRON_HOE.getDefaultInstance();
         case 4 -> Items.GOLDEN_HOE.getDefaultInstance();
         case 5 -> Items.DIAMOND_HOE.getDefaultInstance();
         case 6 -> Items.NETHERITE_HOE.getDefaultInstance();
      };
   }

   public static boolean isCropMature(BlockState state, BlockPos pos, Level level) {
      Block b = state.getBlock();
      if (state.isAir()) {
         return false;
      } else if (state.is(SmarterFarmers.HARVEST_ON_FARMLAND_SPECIAL)) {
         return true;
      } else {
         return b instanceof CropBlock crop
            ? crop.isMaxAge(state)
            : b instanceof SweetBerryBushBlock && (Integer)state.getValue(SweetBerryBushBlock.AGE) == 2 || hardcodedCheckMaxAge(state, b);
      }
   }

   private static boolean hardcodedCheckMaxAge(BlockState state, Block b) {
      return state.getBlock().asItem().builtInRegistryHolder().is(ItemTags.VILLAGER_PLANTABLE_SEEDS)
         && (
            checkAge(state, BlockStateProperties.AGE_1, 1)
               || checkAge(state, BlockStateProperties.AGE_2, 2)
               || checkAge(state, BlockStateProperties.AGE_3, 3)
               || checkAge(state, BlockStateProperties.AGE_4, 4)
               || checkAge(state, BlockStateProperties.AGE_5, 5)
               || checkAge(state, BlockStateProperties.AGE_7, 7)
         );
   }

   private static boolean checkAge(BlockState state, IntegerProperty property, int max) {
      return state.hasProperty(property) && (Integer)state.getValue(property) == max;
   }

   public static boolean isFarmland(BlockState block) {
      return block.getBlock() instanceof FarmlandBlock || block.is(SmarterFarmers.VALID_FARMLAND);
   }
}
