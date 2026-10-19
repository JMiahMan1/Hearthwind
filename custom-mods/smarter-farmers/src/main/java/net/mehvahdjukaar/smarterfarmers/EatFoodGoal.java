package net.mehvahdjukaar.smarterfarmers;

import java.util.Map;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class EatFoodGoal extends net.minecraft.world.entity.ai.behavior.Behavior<net.minecraft.world.entity.npc.villager.Villager> {
   private int eatingTime;
   private int cooldown = 0;
   private int buffer = 40;

   public EatFoodGoal(int minDur, int maxDur) {
      super(
         Map.of(
            MemoryModuleType.INTERACTION_TARGET,
            MemoryStatus.VALUE_ABSENT,
            MemoryModuleType.HURT_BY_ENTITY,
            MemoryStatus.VALUE_ABSENT,
            MemoryModuleType.HURT_BY,
            MemoryStatus.VALUE_ABSENT,
            MemoryModuleType.NEAREST_HOSTILE,
            MemoryStatus.VALUE_ABSENT
         ),
         minDur,
         maxDur
      );
   }

   protected boolean checkExtraStartConditions(ServerLevel pLevel, Villager pOwner) {
      if (pOwner.isBaby()) {
         return false;
      } else if (this.cooldown > 0) {
         this.cooldown--;
         return false;
      } else if (pOwner.tickCount - pOwner.getLastHurtByMobTimestamp() < 400) {
         return false;
      } else if (pOwner.getBrain().isActive(Activity.PANIC)) {
         return false;
      } else if (pOwner.getHealth() < pOwner.getMaxHealth() && pOwner.getInventory().hasAnyOf(Villager.FOOD_POINTS.keySet())) {
         this.buffer--;
         return this.buffer <= 0;
      } else {
         return false;
      }
   }

   protected void start(ServerLevel pLevel, Villager pEntity, long pGameTime) {
      super.start(pLevel, pEntity, pGameTime);
      this.buffer = 40;
      pEntity.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
      pEntity.getNavigation().stop();
      this.eatingTime = 80;
      SimpleContainer inventory = pEntity.getInventory();

      for (int i = 0; i < inventory.getContainerSize(); i++) {
         ItemStack itemstack = inventory.getItem(i);
         Integer food = (Integer)Villager.FOOD_POINTS.get(itemstack.getItem());
         if (food != null) {
            ItemStack s = itemstack.copy();
            s.setCount(1);
            if (itemstack.getCount() == 0) {
               inventory.setItem(i, ItemStack.EMPTY);
            }

            pEntity.setItemInHand(InteractionHand.MAIN_HAND, s);
         }
      }
   }

   protected boolean canStillUse(ServerLevel pLevel, Villager pEntity, long pGameTime) {
      if (pEntity.getBrain().isActive(Activity.PANIC)) {
         return false;
      } else {
         return pEntity.tickCount - pEntity.getLastHurtByMobTimestamp() < 200
            ? false
            : this.eatingTime > 0 && Villager.FOOD_POINTS.get(pEntity.getMainHandItem().getItem()) != null;
      }
   }

   protected void tick(ServerLevel pLevel, Villager pOwner, long pGameTime) {
      if (this.eatingTime-- < 50) {
         ItemStack stack = pOwner.getMainHandItem();
         if (stack.isEmpty()) {
            return;
         }

         if (this.eatingTime % 2 == 0) {
            pLevel.broadcastEntityEvent(pOwner, (byte)45);
         }

         if (this.eatingTime % 5 == 0) {
            pOwner.playSound(
               eatingSound(pOwner, stack),
               0.3F + 0.4F * pLevel.getRandom().nextInt(2),
               (pLevel.getRandom().nextFloat() - pLevel.getRandom().nextFloat()) * 0.2F + 1.3F
            );
         }
      }
   }

   protected void stop(ServerLevel pLevel, Villager pEntity, long pGameTime) {
      super.stop(pLevel, pEntity, pGameTime);
      ItemStack stack = pEntity.getMainHandItem();
      Item item = stack.getItem();
      pEntity.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
      if (item != Items.AIR && this.eatingTime <= 0) {
         Integer food = (Integer)Villager.FOOD_POINTS.get(item);
         if (food != null) {
            for (int i = 0; i < pEntity.getInventory().getContainerSize(); i++) {
               ItemStack s = pEntity.getInventory().getItem(i);
               if (s.getItem() == item) {
                  s.split(1);
                  pEntity.heal(Math.max(1, food));
                  item.finishUsingItem(stack, pLevel, pEntity);
                  this.cooldown = 20 * (4 + pLevel.getRandom().nextInt(14)) + pLevel.getRandom().nextInt(20);
                  return;
               }
            }
         }
      }
   }

   private static net.minecraft.sounds.SoundEvent eatingSound(net.minecraft.world.entity.LivingEntity owner, ItemStack stack) {
      net.minecraft.world.item.component.Consumable consumable = stack.get(net.minecraft.core.component.DataComponents.CONSUMABLE);
      return consumable != null ? consumable.sound().value() : net.minecraft.sounds.SoundEvents.GENERIC_EAT.value();
   }
}
