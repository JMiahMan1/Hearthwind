package io.github.phantomloader.library.utils;

import java.util.stream.Stream;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;

public class CreativeTabsUtils {
   public static final net.minecraft.resources.ResourceKey<net.minecraft.world.item.CreativeModeTab> BUILDING_BLOCKS = createKey("building_blocks");
   public static final net.minecraft.resources.ResourceKey<net.minecraft.world.item.CreativeModeTab> COLORED_BLOCKS = createKey("colored_blocks");
   public static final net.minecraft.resources.ResourceKey<net.minecraft.world.item.CreativeModeTab> NATURAL_BLOCKS = createKey("natural_blocks");
   public static final net.minecraft.resources.ResourceKey<net.minecraft.world.item.CreativeModeTab> FUNCTIONAL_BLOCKS = createKey("functional_blocks");
   public static final net.minecraft.resources.ResourceKey<net.minecraft.world.item.CreativeModeTab> REDSTONE_BLOCKS = createKey("redstone_blocks");
   public static final net.minecraft.resources.ResourceKey<net.minecraft.world.item.CreativeModeTab> TOOLS_AND_UTILITIES = createKey(
      "tools_and_utilities"
   );
   public static final net.minecraft.resources.ResourceKey<net.minecraft.world.item.CreativeModeTab> COMBAT = createKey("combat");
   public static final net.minecraft.resources.ResourceKey<net.minecraft.world.item.CreativeModeTab> FOOD_AND_DRINKS = createKey("food_and_drinks");
   public static final net.minecraft.resources.ResourceKey<net.minecraft.world.item.CreativeModeTab> INGREDIENTS = createKey("ingredients");
   public static final net.minecraft.resources.ResourceKey<net.minecraft.world.item.CreativeModeTab> SPAWN_EGGS = createKey("spawn_eggs");

   public static net.minecraft.resources.ResourceKey<net.minecraft.world.item.CreativeModeTab> createKey(String name) {
      return ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath("phantom", name));
   }

   public static net.minecraft.resources.ResourceKey<net.minecraft.world.item.CreativeModeTab> createKey(String modId, String name) {
      return ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(modId, name));
   }

   public static Stream<net.minecraft.resources.ResourceKey<net.minecraft.world.item.CreativeModeTab>> allTabKeys() {
      return Stream.of(
         BUILDING_BLOCKS,
         COLORED_BLOCKS,
         NATURAL_BLOCKS,
         FUNCTIONAL_BLOCKS,
         REDSTONE_BLOCKS,
         TOOLS_AND_UTILITIES,
         COMBAT,
         FOOD_AND_DRINKS,
         INGREDIENTS,
         SPAWN_EGGS
      );
   }
}
