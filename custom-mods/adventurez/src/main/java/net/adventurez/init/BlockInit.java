package net.adventurez.init;

import net.minecraft.world.level.block.state.BlockBehaviour;
import java.util.Set;

import net.adventurez.AdventureMain;
import net.adventurez.block.*;
import net.adventurez.block.entity.*;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class BlockInit {
    public static final Block CHISELED_POLISHED_BLACKSTONE_HOLDER = register("chiseled_polished_blackstone_holder",
            new ChiseledPolishedBlackstoneHolder(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(blockKey("chiseled_polished_blackstone_holder"))));
    public static final Block PIGLIN_FLAG = register("piglin_flag",
            new PiglinFlag(BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_PLANKS).setId(blockKey("piglin_flag"))));
    public static final Block SHADOW_CHEST = register("shadow_chest",
            new ShadowChest(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(100F, 1000.0F).setId(blockKey("shadow_chest"))
                    .lightLevel(state -> 8)));

    public static BlockEntityType<ChiseledPolishedBlackstoneHolderEntity> CHISELED_POLISHED_BLACKSTONE_HOLDER_ENTITY;
    public static BlockEntityType<PiglinFlagEntity> PIGLIN_FLAG_ENTITY;
    public static BlockEntityType<DragonEggEntity> DRAGON_EGG_ENTITY;
    public static BlockEntityType<ShadowChestEntity> SHADOW_CHEST_ENTITY;

    private static ResourceKey<Block> blockKey(String id) {
        return ResourceKey.create(Registries.BLOCK, AdventureMain.identifierOf(id));
    }

    private static Block register(String id, Block block) {
        return register(AdventureMain.identifierOf(id), block);
    }

    private static Block register(Identifier id, Block block) {
        Item item = new BlockItem(block, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)));
        CreativeModeTabEvents.modifyOutputEvent(ItemInit.ADVENTUREZ_ITEM_GROUP).register(output -> output.accept(item));
        Registry.register(BuiltInRegistries.ITEM, id, item);
        return Registry.register(BuiltInRegistries.BLOCK, id, block);
    }

    public static void init() {
        CHISELED_POLISHED_BLACKSTONE_HOLDER_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, AdventureMain.identifierOf("chiseled_polished_blackstone_holder_entity"),
                new BlockEntityType<>(ChiseledPolishedBlackstoneHolderEntity::new, Set.of(CHISELED_POLISHED_BLACKSTONE_HOLDER)));
        PIGLIN_FLAG_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, AdventureMain.identifierOf("piglin_flag_entity"),
                new BlockEntityType<>(PiglinFlagEntity::new, Set.of(PIGLIN_FLAG)));
        DRAGON_EGG_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, AdventureMain.identifierOf("dragon_egg_entity"),
                new BlockEntityType<>(DragonEggEntity::new, Set.of(Blocks.DRAGON_EGG)));
        SHADOW_CHEST_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, AdventureMain.identifierOf("shadow_chest_entity"),
                new BlockEntityType<>(ShadowChestEntity::new, Set.of(SHADOW_CHEST)));
    }
}
