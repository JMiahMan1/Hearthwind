package net.mehvahdjukaar.smarterfarmers.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.Map;
import net.mehvahdjukaar.smarterfarmers.SmarterFarmers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.UseBonemeal;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({UseBonemeal.class})
public abstract class UseBonemealMixin extends net.minecraft.world.entity.ai.behavior.Behavior<net.minecraft.world.entity.npc.villager.Villager> {
   protected UseBonemealMixin(
      Map<net.minecraft.world.entity.ai.memory.MemoryModuleType<?>, net.minecraft.world.entity.ai.memory.MemoryStatus> pEntryCondition
   ) {
      super(pEntryCondition);
   }

   @ModifyReturnValue(
      method = {"validPos"},
      at = {@At("RETURN")}
   )
   private boolean validPos(boolean original, @Local(argsOnly = true) ServerLevel level, @Local(argsOnly = true) BlockPos pos) {
      if (original) {
         BlockState state = level.getBlockState(pos);
         if (state.is(SmarterFarmers.HARVEST_ON_TILLABLE_NO_REPLANT)) {
            return false;
         }
      }

      return original;
   }
}
