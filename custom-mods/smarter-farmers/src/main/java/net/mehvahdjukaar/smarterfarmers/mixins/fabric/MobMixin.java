package net.mehvahdjukaar.smarterfarmers.mixins.fabric;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.mehvahdjukaar.smarterfarmers.SmarterFarmers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({Mob.class})
public abstract class MobMixin extends Entity {
   public MobMixin(net.minecraft.world.entity.EntityType<?> entityType, net.minecraft.world.level.Level level) {
      super(entityType, level);
   }

   @WrapOperation(
      method = {"aiStep"},
      require = 1,
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/level/gamerules/GameRules;get(Lnet/minecraft/world/level/gamerules/GameRule;)Ljava/lang/Object;"
      )}
   )
   public Object smarterFarmers$overrideMobGriefing(
      GameRules instance,
      GameRule<?> key,
      Operation<Object> original
   ) {
      if (key == GameRules.MOB_GRIEFING && ((Object)this) instanceof Villager && SmarterFarmers.CONFIG.pickupFoodOverride) {
         return Boolean.TRUE;
      }
      return original.call(instance, key);
   }
}
