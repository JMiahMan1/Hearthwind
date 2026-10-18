package com.starfish_studios.another_furniture.registry;

import net.fabricmc.fabric.api.registry.FuelValueEvents;

public class AFFurnaceFuelRegistry {
   public static void init() {
      FuelValueEvents.BUILD.register((builder, context) -> {
         builder.add(AFItemTags.BENCHES, 300);
         builder.add(AFItemTags.CHAIRS, 300);
         builder.add(AFItemTags.CURTAINS, 300);
         builder.add(AFItemTags.DRAWERS, 300);
         builder.add(AFItemTags.GRANDFATHER_CLOCKS, 300);
         builder.add(AFItemTags.LAMPS, 300);
         builder.add(AFItemTags.PLANTER_BOXES, 300);
         builder.add(AFItemTags.SHELVES, 300);
         builder.add(AFItemTags.SHUTTERS, 300);
         builder.add(AFItemTags.SOFAS, 300);
         builder.add(AFItemTags.STOOLS, 300);
         builder.add(AFItemTags.TABLES, 300);
         builder.add(AFItemTags.TALL_STOOLS, 300);
      });
   }
}
