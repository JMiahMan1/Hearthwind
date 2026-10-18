package com.starfish_studios.another_furniture.registry;

import com.starfish_studios.another_furniture.AnotherFurniture;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;

public class AFItemTags {
   public static final net.minecraft.tags.TagKey<net.minecraft.world.item.Item> BENCHES = itemTag("benches");
   public static final net.minecraft.tags.TagKey<net.minecraft.world.item.Item> CHAIRS = itemTag("chairs");
   public static final net.minecraft.tags.TagKey<net.minecraft.world.item.Item> CURTAINS = itemTag("curtains");
   public static final net.minecraft.tags.TagKey<net.minecraft.world.item.Item> DRAWERS = itemTag("drawers");
   public static final net.minecraft.tags.TagKey<net.minecraft.world.item.Item> GRANDFATHER_CLOCKS = itemTag("grandfather_clocks");
   public static final net.minecraft.tags.TagKey<net.minecraft.world.item.Item> LAMPS = itemTag("lamps");
   public static final net.minecraft.tags.TagKey<net.minecraft.world.item.Item> PLANTER_BOXES = itemTag("planter_boxes");
   public static final net.minecraft.tags.TagKey<net.minecraft.world.item.Item> SHELVES = itemTag("shelves");
   public static final net.minecraft.tags.TagKey<net.minecraft.world.item.Item> SHUTTERS = itemTag("shutters");
   public static final net.minecraft.tags.TagKey<net.minecraft.world.item.Item> SOFAS = itemTag("sofas");
   public static final net.minecraft.tags.TagKey<net.minecraft.world.item.Item> STOOLS = itemTag("stools");
   public static final net.minecraft.tags.TagKey<net.minecraft.world.item.Item> TABLES = itemTag("tables");
   public static final net.minecraft.tags.TagKey<net.minecraft.world.item.Item> TALL_STOOLS = itemTag("tall_stools");
   public static final net.minecraft.tags.TagKey<net.minecraft.world.item.Item> FLOWER_BOX_PLACEABLES = itemTag("flower_box_placeables");
   public static final net.minecraft.tags.TagKey<net.minecraft.world.item.Item> FLOWER_BOX_BANNED = itemTag("flower_box_banned");
   public static final net.minecraft.tags.TagKey<net.minecraft.world.item.Item> FURNITURE_HAMMER = itemTag("furniture_hammers");

   private static net.minecraft.tags.TagKey<net.minecraft.world.item.Item> itemTag(String name) {
      return TagKey.create(Registries.ITEM, AnotherFurniture.res(name));
   }

   public static void init() {
   }
}
