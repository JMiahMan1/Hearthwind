package net.smitherz.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.Level;
import net.smitherz.SmitherzMain;
import net.smitherz.init.ConfigInit;
import net.smitherz.init.ItemInit;
import net.smitherz.init.TagInit;
import net.smitherz.item.Gem;
import net.smitherz.item.Hammer;
import net.smitherz.item.Upgradeable;
import net.smitherz.item.component.GemComponent;
import org.jetbrains.annotations.Nullable;

public class UpgradeHelper {
   public static final Random RANDOM = new Random();

   public static void spawnItemContents(net.minecraft.world.entity.item.ItemEntity itemEntity, Stream<net.minecraft.world.item.ItemStack> contents) {
      Level world = itemEntity.level();
      if (!world.isClientSide()) {
         contents.forEach(
            stack -> world.addFreshEntity(new ItemEntity(world, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), stack))
         );
      }
   }

   public static Stream<net.minecraft.world.item.ItemStack> getGemStacks(net.minecraft.world.item.ItemStack stack) {
      int gemSlots = getGemSlotSize(stack);
      if (gemSlots == 0) {
         return Stream.empty();
      } else {
         return stack.get(ItemInit.GEMS) == null ? Stream.empty() : ((GemComponent)stack.get(ItemInit.GEMS)).gems().stream();
      }
   }

   public static boolean tryAddStackToUpgradeable(ItemStack upgradeable, ItemStack gemStack, @Nullable ItemStack hammer) {
      if (!gemStack.isEmpty() && net.smitherz.util.UpgradeHelper.isUpgradeable(upgradeable)) {
         float linkChance = 0.0F;
         if (gemStack.getItem() instanceof Gem gem) {
            if (!gem.canLinkToItemStack(upgradeable)) {
               return false;
            }

            if (!ConfigInit.CONFIG.canLinkSameGem && upgradeable.get(ItemInit.GEMS) != null) {
               for (int i = 0; i < ((GemComponent)upgradeable.get(ItemInit.GEMS)).gems().size(); i++) {
                  if (((ItemStack)((GemComponent)upgradeable.get(ItemInit.GEMS)).gems().get(i)).is(gemStack.getItem())) {
                     return false;
                  }
               }
            }

            linkChance = gem.getLinkChance();
         } else {
            linkChance = 0.1F;
         }

         boolean hasHammer = hammer != null && !hammer.isEmpty() && hammer.is(TagInit.BONUS_ITEMS);
         if (hasHammer) {
            if (hammer.getItem() instanceof Hammer hammerItem) {
               linkChance += hammerItem.getBonusChance();
            } else {
               linkChance += ConfigInit.CONFIG.bonusItemExtraChance;
            }

            hammer.shrink(1);
         }

         if (linkChance >= RANDOM.nextFloat()) {
            addStackToUpgradeable(upgradeable, gemStack);
            gemStack.shrink(1);
            return true;
         } else {
            if (!hasHammer && gemStack.getItem() instanceof Gem gem && gem.getLinkBreakChance() > 1.0E-5F && RANDOM.nextFloat() <= gem.getLinkBreakChance()
               )
             {
               upgradeable.shrink(1);
            }

            if (!upgradeable.isEmpty() && ConfigInit.CONFIG.linkBreakChance > 1.0E-5F && RANDOM.nextFloat() < ConfigInit.CONFIG.linkBreakChance) {
               upgradeable.shrink(1);
            }

            gemStack.shrink(1);
            return false;
         }
      } else {
         return false;
      }
   }

   public static void addStackToUpgradeable(ItemStack upgradeable, ItemStack gemStack) {
      if (getGemSlotSize(upgradeable) >= getGemStacks(upgradeable).toList().size() + 1) {
         GemComponent gemComponent = (GemComponent)upgradeable.getOrDefault(ItemInit.GEMS, GemComponent.DEFAULT);
         List<net.minecraft.world.item.ItemStack> gems = new ArrayList<>(gemComponent.gems());
         gems.add(gemStack.copy());
         upgradeable.set(ItemInit.GEMS, new GemComponent(gems, gemComponent.size()));
      }
   }

   public static List<net.minecraft.world.item.ItemStack> tryRemoveStackFromUpgradeable(
      net.minecraft.world.item.ItemStack upgradeable, @Nullable net.minecraft.world.item.ItemStack hammer
   ) {
      if (net.smitherz.util.UpgradeHelper.isUpgradeable(upgradeable) && !getGemStacks(upgradeable).toList().isEmpty()) {
         ItemStack itemStack2 = upgradeable.copy();
         if (itemStack2.get(ItemInit.GEMS) == null) {
            return List.of(ItemStack.EMPTY);
         } else {
            float unlinkChance = 0.0F;
            boolean hasHammer = hammer != null && !hammer.isEmpty() && hammer.is(TagInit.EXTRACTION_ITEMS);
            if (hasHammer) {
               if (hammer.getItem() instanceof Hammer hammerItem) {
                  unlinkChance += hammerItem.getBonusChance();
               } else {
                  unlinkChance += ConfigInit.CONFIG.bonusItemExtraChance;
               }
            }

            List<net.minecraft.world.item.ItemStack> list = new ArrayList<>();
            if (hammer != null && !hammer.isEmpty()) {
               ItemStack stack = ((ItemStack)((GemComponent)itemStack2.get(ItemInit.GEMS)).gems().getLast()).copy();
               if (stack.getItem() instanceof Gem gem) {
                  unlinkChance = gem.getUnlinkChance()
                     + (
                        hasHammer
                           ? (hammer.getItem() instanceof Hammer hammerItem ? hammerItem.getBonusChance() : ConfigInit.CONFIG.bonusItemExtraChance)
                           : 0.0F
                     );
                  if (RANDOM.nextFloat() <= unlinkChance) {
                     list.add(stack);
                  }
               } else if (unlinkChance >= RANDOM.nextFloat()) {
                  list.add(stack);
               }

               List<net.minecraft.world.item.ItemStack> remainingGems = new ArrayList<>(((GemComponent)itemStack2.get(ItemInit.GEMS)).gems());
               remainingGems.removeLast();
               itemStack2.set(ItemInit.GEMS, new GemComponent(remainingGems, ((GemComponent)itemStack2.get(ItemInit.GEMS)).size()));
            } else {
               for (ItemStack stack : new ArrayList<>(((GemComponent)itemStack2.get(ItemInit.GEMS)).gems())) {
                  if (stack.getItem() instanceof Gem gemx) {
                     unlinkChance = gemx.getUnlinkChance()
                        + (
                           hasHammer
                              ? (hammer.getItem() instanceof Hammer hammerItem ? hammerItem.getBonusChance() : ConfigInit.CONFIG.bonusItemExtraChance)
                              : 0.0F
                        );
                     if (RANDOM.nextFloat() <= unlinkChance) {
                        list.add(stack);
                     }
                  } else if (unlinkChance >= RANDOM.nextFloat()) {
                     list.add(stack);
                  }
               }

               itemStack2.set(ItemInit.GEMS, new GemComponent(new ArrayList<>(), ((GemComponent)itemStack2.get(ItemInit.GEMS)).size()));
            }

            list.add(0, itemStack2);
            return list;
         }
      } else {
         return List.of(ItemStack.EMPTY);
      }
   }

   public static boolean removeStackFromUpgradeable(ItemStack upgradeable, ItemStack gemStack) {
      if (getGemStacks(upgradeable).toList().isEmpty()) {
         return false;
      } else {
         boolean hasGemStack = false;
         List<net.minecraft.world.item.ItemStack> gems = new ArrayList<>();

         for (ItemStack stack : ((GemComponent)upgradeable.get(ItemInit.GEMS)).gems()) {
            if (!ItemStack.matches(stack, gemStack)) {
               gems.add(stack);
            } else {
               hasGemStack = true;
            }
         }

         upgradeable.set(ItemInit.GEMS, new GemComponent(gems, ((GemComponent)upgradeable.get(ItemInit.GEMS)).size()));
         return hasGemStack;
      }
   }

   public static void setGemSlots(ItemStack itemStack, int count) {
      if (net.smitherz.util.UpgradeHelper.isUpgradeable(itemStack)) {
         GemComponent gemComponent = (GemComponent)itemStack.getOrDefault(ItemInit.GEMS, GemComponent.DEFAULT);
         if (count >= 0) {
            gemComponent = new GemComponent(new ArrayList<>(), Math.min(count, ConfigInit.CONFIG.maxGemSlots));
         } else if (ConfigInit.CONFIG.gemSlotRandomness) {
            gemComponent = new GemComponent(new ArrayList<>(), skewedRandomInt(ConfigInit.CONFIG.maxGemSlots));
         } else {
            gemComponent = new GemComponent(new ArrayList<>(), ConfigInit.CONFIG.maxGemSlots);
         }

         itemStack.set(ItemInit.GEMS, gemComponent);
      }
   }

   public static void addGemSlots(ItemStack itemStack, int count) {
      if (count > 0 && net.smitherz.util.UpgradeHelper.isUpgradeable(itemStack)) {
         GemComponent gemComponent = (GemComponent)itemStack.getOrDefault(ItemInit.GEMS, GemComponent.DEFAULT);
         gemComponent = new GemComponent(gemComponent.gems(), Math.min(gemComponent.size() + count, ConfigInit.CONFIG.maxGemSlots));
         itemStack.set(ItemInit.GEMS, gemComponent);
      }
   }

   public static void removeGemSlots(ItemStack itemStack, int count) {
      if (count > 0 && net.smitherz.util.UpgradeHelper.isUpgradeable(itemStack)) {
         GemComponent gemComponent = (GemComponent)itemStack.getOrDefault(ItemInit.GEMS, GemComponent.DEFAULT);
         if (count >= gemComponent.size()) {
            itemStack.remove(ItemInit.GEMS);
         } else {
            List<net.minecraft.world.item.ItemStack> gems = new ArrayList<>();

            for (int i = 0; i < gemComponent.size() - count && !gemComponent.gems().isEmpty() && gemComponent.gems().size() > i; i++) {
               gems.add(gemComponent.gems().get(i));
            }

            gemComponent = new GemComponent(gems, Math.max(gemComponent.size() - count, 0));
            itemStack.set(ItemInit.GEMS, gemComponent);
         }
      }
   }

   public static int getGemSlotSize(ItemStack itemStack) {
      return itemStack.get(ItemInit.GEMS) != null ? ((GemComponent)itemStack.get(ItemInit.GEMS)).size() : 0;
   }

   public static boolean isUpgradeable(net.minecraft.world.item.ItemStack stack) {
      if (stack.has(net.minecraft.core.component.DataComponents.TOOL) || stack.has(net.minecraft.core.component.DataComponents.WEAPON)) {
         return true;
      }

      net.minecraft.world.item.equipment.Equippable equippable = stack.get(net.minecraft.core.component.DataComponents.EQUIPPABLE);
      if (equippable != null) {
         net.minecraft.world.entity.EquipmentSlot slot = equippable.slot();
         return slot == net.minecraft.world.entity.EquipmentSlot.HEAD
            || slot == net.minecraft.world.entity.EquipmentSlot.CHEST
            || slot == net.minecraft.world.entity.EquipmentSlot.LEGS
            || slot == net.minecraft.world.entity.EquipmentSlot.FEET;
      }

      return false;
   }

   public static int skewedRandomInt(int maxValue) {
      double exponent = 1.5;
      double randomValue = RANDOM.nextDouble();
      double skewedValue = Math.pow(randomValue, exponent);
      return (int)(skewedValue * (maxValue + 1));
   }
}
