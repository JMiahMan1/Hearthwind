package net.medievalweapons.init;

import net.medievalweapons.MedievalMain;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;

public class TagInit {
   public static final net.minecraft.tags.TagKey<net.minecraft.world.item.Item> DOUBLE_HANDED_ITEMS = TagKey.create(
      Registries.ITEM, MedievalMain.identifierOf("double_handed_items")
   );
   public static final net.minecraft.tags.TagKey<net.minecraft.world.item.Item> ACROSS_DOUBLE_HANDED_ITEMS = TagKey.create(
      Registries.ITEM, MedievalMain.identifierOf("across_double_handed_items")
   );

   public static void init() {
   }
}
