package net.medievalweapons.mixin;

import net.medievalweapons.item.JavelinItem;
import net.medievalweapons.util.EnchantmentCache;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Enchantment.class})
public class EnchantmentMixin {
   @Inject(
      method = {"canEnchant"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void isAcceptableItemMixin(ItemStack stack, CallbackInfoReturnable<Boolean> info) {
      if (stack.getItem() instanceof JavelinItem && EnchantmentCache.isJavelinEnchantment((Enchantment)(Object)this)) {
         info.setReturnValue(true);
      }
   }
}
