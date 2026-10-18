package com.starfish_studios.another_furniture.registry;

import com.starfish_studios.another_furniture.item.HammerItem;
import java.util.function.Supplier;
import net.minecraft.world.item.Item.Properties;

public class AFItems {
   public static final Supplier<net.minecraft.world.item.Item> FURNITURE_HAMMER = AFRegistry.registerItem(
      "furniture_hammer", (Supplier<net.minecraft.world.item.Item>)(() -> new HammerItem(new Properties().durability(892).setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, com.starfish_studios.another_furniture.AnotherFurniture.res("furniture_hammer"))))), "tab"
   );

   public static void init() {
   }
}
