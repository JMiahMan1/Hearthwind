package net.mehvahdjukaar.smarterfarmers.mixins;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.Set;
import net.mehvahdjukaar.smarterfarmers.EatFoodGoal;
import net.mehvahdjukaar.smarterfarmers.SFPlatformStuff;
import net.mehvahdjukaar.smarterfarmers.SmarterFarmers;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Villager.class})
public abstract class VillagerMixin extends AbstractVillager {
   @Final
   @Shadow
   public static Map<Item, Integer> FOOD_POINTS;

   @Shadow
   public abstract VillagerData getVillagerData();

   protected VillagerMixin(net.minecraft.world.entity.EntityType<? extends AbstractVillager> entityType,
         net.minecraft.world.level.Level level) {
      super(entityType, level);
   }

   @Inject(method = {"registerBrainGoals"}, at = {@At("TAIL")})
   private void smarterfarmers$addEatGoal(Brain<Villager> brain, CallbackInfo ci) {
      if (SmarterFarmers.CONFIG != null && SmarterFarmers.CONFIG.eatFood) {
         brain.addActivity(Activity.MEET,
               ImmutableList.of(Pair.of(7, (BehaviorControl<Villager>) new EatFoodGoal(100, 140))),
               Set.of(), Set.of());
      }
   }

   @ModifyReturnValue(method = {"wantsToPickUp"}, at = {@At("RETURN")})
   private boolean wantsToPickUp(boolean original, @Local(argsOnly = true) ItemStack stack) {
      Item i = stack.getItem();
      if (FOOD_POINTS.containsKey(i)) {
         return true;
      } else {
         return !SFPlatformStuff.isValidSeed(stack, (Villager)(Object)this) ? original
               : this.smarterfarmers$isFarmer() && this.getInventory().canAddItem(stack);
      }
   }

   @Unique
   private boolean smarterfarmers$isFarmer() {
      return this.getVillagerData().profession().is(VillagerProfession.FARMER);
   }

   public boolean isInvulnerableTo(net.minecraft.server.level.ServerLevel level, @NotNull DamageSource pSource) {
      return pSource == this.damageSources().sweetBerryBush() && this.smarterfarmers$isFarmer() ? true
            : super.isInvulnerableTo(level, pSource);
   }

   @Inject(method = {"handleEntityEvent"}, at = {@At("HEAD")})
   public void smarterFarmers$addEatingParticles(byte pId, CallbackInfo ci) {
      if (pId == 45 && this.level().isClientSide()) {
         SmarterFarmers.spawnEatingParticles(this);
      }
   }
}
