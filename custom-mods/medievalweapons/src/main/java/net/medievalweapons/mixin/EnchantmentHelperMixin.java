package net.medievalweapons.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.medievalweapons.item.JavelinItem;
import net.medievalweapons.item.LanceItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({EnchantmentHelper.class})
public class EnchantmentHelperMixin {
   @Inject(
      method = {"getAvailableEnchantmentResults"},
      at = {@At("TAIL")},
      cancellable = true
   )
   private static void getPossibleEntriesMixin(
      int level,
      net.minecraft.world.item.ItemStack stack,
      Stream<net.minecraft.core.Holder<Enchantment>> possibleEnchantments,
      CallbackInfoReturnable<List<EnchantmentInstance>> info
   ) {
      List<EnchantmentInstance> currentEnchantments = info.getReturnValue();
      if (currentEnchantments != null) {
         if (stack.getItem() instanceof JavelinItem) {
            List<EnchantmentInstance> enchantments = new ArrayList<>();
            currentEnchantments.forEach(
               enchantment -> {
                  Enchantment value = enchantment.enchantment().value();
                  if (value.canEnchant(stack) && value.isPrimaryItem(stack)
                     || enchantment.enchantment().is(Enchantments.IMPALING)
                     || enchantment.enchantment().is(Enchantments.PIERCING)
                     || enchantment.enchantment().is(Enchantments.LOYALTY)) {
                     enchantments.add(enchantment);
                  }
               }
            );
            info.setReturnValue(enchantments);
         } else if (stack.getItem() instanceof LanceItem) {
            List<EnchantmentInstance> enchantments = new ArrayList<>();
            currentEnchantments.forEach(
               enchantment -> {
                  Enchantment value = enchantment.enchantment().value();
                  if (value.canEnchant(stack) && value.isPrimaryItem(stack) && !enchantment.enchantment().is(Enchantments.SWEEPING_EDGE)) {
                     enchantments.add(enchantment);
                  }
               }
            );
            info.setReturnValue(enchantments);
         }
      }
   }
}
