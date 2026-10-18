package com.starfish_studios.another_furniture.registry;

import com.starfish_studios.another_furniture.entity.SeatEntity;
import java.util.function.Supplier;
import net.minecraft.world.entity.MobCategory;

public class AFEntityTypes {
   public static final Supplier<net.minecraft.world.entity.EntityType<SeatEntity>> SEAT = AFRegistry.registerEntityType(
      "seat", (type, world) -> new SeatEntity(world), MobCategory.MISC, 0.0F, 0.0F
   );

   public static void init() {
   }
}
