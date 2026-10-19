package net.mehvahdjukaar.smarterfarmers.mixins;

import com.google.common.collect.ImmutableSet;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.mehvahdjukaar.smarterfarmers.FarmTaskLogic;
import net.minecraft.world.entity.ai.sensing.SecondaryPoiSensor;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({SecondaryPoiSensor.class})
public class SecondaryPoiSensorMixin {
   @WrapOperation(
      method = {"doTick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/npc/villager/Villager;)V"},
      at = {@At(
         value = "INVOKE",
         target = "Lcom/google/common/collect/ImmutableSet;contains(Ljava/lang/Object;)Z"
      )}
   )
   public boolean smarterfarmers$addMoreFarmland(
      ImmutableSet<net.minecraft.world.level.block.Block> instance,
      Object o,
      Operation<Boolean> original,
      @Local(argsOnly = true) net.minecraft.world.entity.npc.villager.Villager villager
   ) {
      return !villager.getVillagerData().profession().is(VillagerProfession.FARMER)
         ? (Boolean)original.call(new Object[]{instance, o})
         : (Boolean)original.call(new Object[]{instance, o}) || FarmTaskLogic.isFarmland(((Block)o).defaultBlockState());
   }
}
