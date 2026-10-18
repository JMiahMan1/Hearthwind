package com.starfish_studios.another_furniture.registry;

import com.starfish_studios.another_furniture.AnotherFurniture;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;

public class AFBlockTags {
   public static final net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> BENCHES = blockTag("benches");
   public static final net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> CHAIRS = blockTag("chairs");
   public static final net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> CURTAINS = blockTag("curtains");
   public static final net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> DRAWERS = blockTag("drawers");
   public static final net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> LAMPS = blockTag("lamps");
   public static final net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> PLANTER_BOXES = blockTag("planter_boxes");
   public static final net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> SHELVES = blockTag("shelves");
   public static final net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> SHUTTERS = blockTag("shutters");
   public static final net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> SOFAS = blockTag("sofas");
   public static final net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> STOOLS = blockTag("stools");
   public static final net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> TABLES = blockTag("tables");
   public static final net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> TALL_STOOLS = blockTag("tall_stools");
   public static final net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> CHAIRS_TUCKABLE_UNDER = blockTag("chairs_tuckable_under");
   public static final net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> ABOVE_BYPASSES_SEAT_CHECK = blockTag(
      "above_bypasses_seat_check"
   );
   public static final net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> TABLES_CONNECTABLE = blockTag("table_connectable");
   public static final net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> DONT_CONNECT_TO_PANES = blockTag("dont_connect_to_panes");
   public static final net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> CAN_USE_SHUTTERS_THROUGH = blockTag("can_use_shutters_through");
   public static final net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> SEATS = blockTag("seats");

   private static net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> blockTag(String name) {
      return TagKey.create(Registries.BLOCK, AnotherFurniture.res(name));
   }

   public static void init() {
   }
}
