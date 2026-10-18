package com.starfish_studios.another_furniture;

import com.starfish_studios.another_furniture.integration.IntegrationHandler;
import com.starfish_studios.another_furniture.registry.AFBlockEntityTypes;
import com.starfish_studios.another_furniture.registry.AFBlockTags;
import com.starfish_studios.another_furniture.registry.AFBlocks;
import com.starfish_studios.another_furniture.registry.AFEntityTypes;
import com.starfish_studios.another_furniture.registry.AFItemTags;
import com.starfish_studios.another_furniture.registry.AFItems;
import com.starfish_studios.another_furniture.registry.AFSoundEvents;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnotherFurniture {
   public static final String MOD_ID = "another_furniture";
   public static final Logger LOGGER = LoggerFactory.getLogger("another_furniture");

   public static void init() {
      AFBlocks.init();
      AFItems.init();
      AFSoundEvents.init();
      AFEntityTypes.init();
      AFBlockEntityTypes.init();
      AFItemTags.init();
      AFBlockTags.init();
      IntegrationHandler.init();
   }

   public static Identifier res(String name) {
      return Identifier.fromNamespaceAndPath("another_furniture", name);
   }
}
