package net.satisfy.herbalbrews.core.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.satisfy.herbalbrews.core.util.HerbalBrewsIdentifier;

public class TagsRegistry {
    public static final TagKey<Block> ALLOWS_COOKING = TagKey.create(Registries.BLOCK, HerbalBrewsIdentifier.identifier("allows_cooking"));
    public static final TagKey<Item> CONTAINER_ITEMS = TagKey.create(Registries.ITEM, HerbalBrewsIdentifier.identifier("container_items"));
    public static final TagKey<Item> SMALL_WATER_FILL = TagKey.create(Registries.ITEM, HerbalBrewsIdentifier.identifier("small_water_fill"));
    public static final TagKey<Item> LARGE_WATER_FILL = TagKey.create(Registries.ITEM, HerbalBrewsIdentifier.identifier("large_water_fill"));
    public static final TagKey<Item> HEAT_ITEMS = TagKey.create(Registries.ITEM, HerbalBrewsIdentifier.identifier("heat_items"));
    public static final TagKey<Item> IS_MILK = TagKey.create(Registries.ITEM, HerbalBrewsIdentifier.identifier("is_milk"));
}
