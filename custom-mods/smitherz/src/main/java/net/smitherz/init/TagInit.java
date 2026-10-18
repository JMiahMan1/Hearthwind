package net.smitherz.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.smitherz.SmitherzMain;

public class TagInit {
   public static final net.minecraft.tags.TagKey<net.minecraft.world.item.Item> GEMS = TagKey.create(
      Registries.ITEM, SmitherzMain.identifierOf("gems")
   );
   public static final net.minecraft.tags.TagKey<net.minecraft.world.item.Item> BONUS_ITEMS = TagKey.create(
      Registries.ITEM, SmitherzMain.identifierOf("bonus_items")
   );
   public static final net.minecraft.tags.TagKey<net.minecraft.world.item.Item> EXTRACTION_ITEMS = TagKey.create(
      Registries.ITEM, SmitherzMain.identifierOf("extraction_items")
   );

   public static void init() {
   }
}
