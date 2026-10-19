package net.mehvahdjukaar.smarterfarmers;

import net.mehvahdjukaar.smarterfarmers.fabric.SFPlatformStuffImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Contract;

public class SFPlatformStuff {
   @Contract
   public static boolean isValidSeed(ItemStack item, Villager villager) {
      return SFPlatformStuffImpl.isValidSeed(item, villager);
   }

   public static boolean tillBlock(BlockState below, BlockPos belowPos, ServerLevel level) {
      return SFPlatformStuffImpl.tillBlock(below, belowPos, level);
   }

   public static boolean trySpecialPlant(ServerLevel level, BlockPos aboveFarmlandPos, ItemStack itemToPlant, Villager villager) {
      return SFPlatformStuffImpl.trySpecialPlant(level, aboveFarmlandPos, itemToPlant, villager);
   }
}
