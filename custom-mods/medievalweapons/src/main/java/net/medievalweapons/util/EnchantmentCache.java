package net.medievalweapons.util;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

public final class EnchantmentCache {
   private static Enchantment piercing;
   private static Enchantment impaling;
   private static Enchantment loyalty;

   private EnchantmentCache() {
   }

   public static void onServerStarted(MinecraftServer server) {
      Registry<Enchantment> registry = server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
      piercing = registry.getValue(Enchantments.PIERCING);
      impaling = registry.getValue(Enchantments.IMPALING);
      loyalty = registry.getValue(Enchantments.LOYALTY);
   }

   public static boolean isJavelinEnchantment(Enchantment enchantment) {
      return enchantment != null && (enchantment == piercing || enchantment == impaling || enchantment == loyalty);
   }
}
