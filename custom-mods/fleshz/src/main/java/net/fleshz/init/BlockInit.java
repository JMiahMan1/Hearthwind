package net.fleshz.init;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fleshz.FleshMain;
import net.fleshz.block.WoodRack;
import net.fleshz.block.entity.WoodRackEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class BlockInit {

    public static final Map<Block, List<Block>> RACKS = new HashMap<>();

    public static final Block WOOD_RACK = register("wood_rack", Blocks.OAK_PLANKS, Blocks.OAK_SLAB);
    public static final Block OAK_WOOD_RACK = register("oak_wood_rack", Blocks.OAK_PLANKS, Blocks.OAK_SLAB);
    public static final Block SPRUCE_WOOD_RACK = register("spruce_wood_rack", Blocks.SPRUCE_PLANKS, Blocks.SPRUCE_SLAB);
    public static final Block BIRCH_WOOD_RACK = register("birch_wood_rack", Blocks.BIRCH_PLANKS, Blocks.BIRCH_SLAB);
    public static final Block JUNGLE_WOOD_RACK = register("jungle_wood_rack", Blocks.JUNGLE_PLANKS, Blocks.JUNGLE_SLAB);
    public static final Block ACACIA_WOOD_RACK = register("acacia_wood_rack", Blocks.ACACIA_PLANKS, Blocks.ACACIA_SLAB);
    public static final Block DARK_OAK_WOOD_RACK = register("dark_oak_wood_rack", Blocks.DARK_OAK_PLANKS, Blocks.DARK_OAK_SLAB);
    public static final Block CRIMSON_WOOD_RACK = register("crimson_wood_rack", Blocks.CRIMSON_PLANKS, Blocks.CRIMSON_SLAB);
    public static final Block WARPED_WOOD_RACK = register("warped_wood_rack", Blocks.WARPED_PLANKS, Blocks.WARPED_SLAB);
    public static final Block MANGROVE_WOOD_RACK = register("mangrove_wood_rack", Blocks.MANGROVE_PLANKS, Blocks.MANGROVE_SLAB);
    public static final Block BAMBOO_WOOD_RACK = register("bamboo_wood_rack", Blocks.BAMBOO_PLANKS, Blocks.BAMBOO_SLAB);
    public static final Block CHERRY_WOOD_RACK = register("cherry_wood_rack", Blocks.CHERRY_PLANKS, Blocks.CHERRY_SLAB);

    public static final BlockEntityType<WoodRackEntity> WOOD_RACK_ENTITY = new BlockEntityType<>(WoodRackEntity::new,
            Set.of(WOOD_RACK, OAK_WOOD_RACK, SPRUCE_WOOD_RACK, BIRCH_WOOD_RACK, JUNGLE_WOOD_RACK, ACACIA_WOOD_RACK,
                    DARK_OAK_WOOD_RACK, CRIMSON_WOOD_RACK, WARPED_WOOD_RACK, MANGROVE_WOOD_RACK, BAMBOO_WOOD_RACK,
                    CHERRY_WOOD_RACK));

    private static Block register(String path, Block plankVariant, Block recipeIngredient) {
        Identifier id = FleshMain.identifierOf(path);
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id);
        Block block = new WoodRack(BlockBehaviour.Properties.ofFullCopy(plankVariant).setId(blockKey));
        RACKS.put(block, List.of(plankVariant, recipeIngredient));

        Item item = new BlockItem(block, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)));
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.BUILDING_BLOCKS)
                .register(output -> output.accept(item));

        Registry.register(BuiltInRegistries.ITEM, id, item);
        return Registry.register(BuiltInRegistries.BLOCK, id, block);
    }

    public static void init() {
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, FleshMain.identifierOf("wood_rack_entity"), WOOD_RACK_ENTITY);
    }
}
