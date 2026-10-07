package com.github.legoatoom.connectiblechains.tag;

import com.github.legoatoom.connectiblechains.util.Helper;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;

public class ModTagRegistry {
   public static final net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> CHAIN_CONNECTIBLE = makeTag(
      Registries.BLOCK, Helper.identifier("chain_connectible")
   );
   public static final net.minecraft.tags.TagKey<net.minecraft.world.item.Item> CATENARY_ITEMS = makeTag(
      Registries.ITEM, Helper.identifier("catenary_items")
   );
   public static final net.minecraft.tags.TagKey<net.minecraft.world.item.Item> ROPES = makeTag(Registries.ITEM, Helper.identifier("ropes"));

   public static <T> net.minecraft.tags.TagKey<T> makeTag(
      net.minecraft.resources.ResourceKey<net.minecraft.core.Registry<T>> registry, net.minecraft.resources.Identifier id
   ) {
      return TagKey.create(registry, id);
   }
}
