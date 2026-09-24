package net.adventurez.init;

import net.adventurez.AdventureMain;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.biome.Biome;

public class TagInit {

    // Block
    public static final TagKey<Block> UNBREAKABLE_BLOCKS = TagKey.create(Registries.BLOCK, AdventureMain.identifierOf("unbreakable_blocks"));
    public static final TagKey<Block> PLATFORM_NETHER_BLOCKS = TagKey.create(Registries.BLOCK, AdventureMain.identifierOf("platform_nether_blocks"));
    public static final TagKey<Block> PLATFORM_END_BLOCKS = TagKey.create(Registries.BLOCK, AdventureMain.identifierOf("platform_end_blocks"));
    // Item
    public static final TagKey<Item> LEATHER_ITEMS = TagKey.create(Registries.ITEM, AdventureMain.identifierOf("leather_items"));
    public static final TagKey<Item> HOLDER_ITEMS = TagKey.create(Registries.ITEM, AdventureMain.identifierOf("holder_items"));
    public static final TagKey<Item> PIGLIN_NOT_ATTACK_ITEMS = TagKey.create(Registries.ITEM, AdventureMain.identifierOf("piglin_not_attack_items"));
    // Biome
    public static final TagKey<Biome> IS_MUSHROOM = TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("c", "mushroom"));
    // Damage Type
    public static final TagKey<DamageType> IS_WALL = TagKey.create(Registries.DAMAGE_TYPE, AdventureMain.identifierOf("is_wall"));

    public static void init() {
    }

}