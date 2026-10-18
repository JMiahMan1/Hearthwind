package com.starfish_studios.another_furniture.registry;

import com.starfish_studios.another_furniture.AnotherFurniture;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;

public class AFEntityTypeTags {
   public static final net.minecraft.tags.TagKey<net.minecraft.world.entity.EntityType<?>> CAN_SIT_IN_SEATS = entityTypeTag("can_sit_in_seats");

   private static net.minecraft.tags.TagKey<net.minecraft.world.entity.EntityType<?>> entityTypeTag(String name) {
      return TagKey.create(Registries.ENTITY_TYPE, AnotherFurniture.res(name));
   }

   public static void init() {
   }
}
