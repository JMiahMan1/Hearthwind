package net.dungeonz.init;

import java.util.Set;

import net.dungeonz.block.*;
import net.dungeonz.block.entity.*;
import net.dungeonz.block.screen.DungeonPortalScreenHandler;
import net.dungeonz.network.packet.DungeonPortalPacket;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.core.registries.BuiltInRegistries;

public class BlockInit {

    public static final Block DUNGEON_PORTAL = register("dungeon_portal", new DungeonPortalBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.END_PORTAL)
            .setId(blockKey("dungeon_portal"))));
    public static final Block DUNGEON_SPAWNER = register("dungeon_spawner", new DungeonSpawnerBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SPAWNER)
            .setId(blockKey("dungeon_spawner"))));
    public static final Block DUNGEON_GATE = register("dungeon_gate", new DungeonGateBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK).noOcclusion()
            .setId(blockKey("dungeon_gate"))));

    public static BlockEntityType<DungeonPortalEntity> DUNGEON_PORTAL_ENTITY;
    public static BlockEntityType<DungeonSpawnerEntity> DUNGEON_SPAWNER_ENTITY;
    public static BlockEntityType<DungeonGateEntity> DUNGEON_GATE_ENTITY;

  //  public static final ScreenHandlerType<DungeonPortalScreenHandler> PORTAL = new ExtendedScreenHandlerType<>(DungeonPortalScreenHandler::new);

    // 26.x: ExtendedScreenHandlerType -> ExtendedMenuType (fabric-menu-api-v1); the factory
    // now receives the codec-decoded packet instead of the raw buf.
    public static final MenuType<DungeonPortalScreenHandler> PORTAL = new ExtendedMenuType<DungeonPortalScreenHandler, DungeonPortalPacket>(
            DungeonPortalScreenHandler::new, DungeonPortalPacket.PACKET_CODEC);

    private static ResourceKey<Block> blockKey(String id) {
        return ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("dungeonz", id));
    }

    private static Block register(String id, Block block) {
        return register(Identifier.fromNamespaceAndPath("dungeonz", id), block);
    }

    private static Block register(Identifier id, Block block) {
        Item item = Registry.register(BuiltInRegistries.ITEM, id, new BlockItem(block, new Item.Properties().rarity(Rarity.EPIC)
                .setId(ResourceKey.create(Registries.ITEM, id))));
        CreativeModeTabEvents.modifyOutputEvent(ItemInit.DUNGEONZ_ITEM_GROUP).register(output -> output.accept(item));

        return Registry.register(BuiltInRegistries.BLOCK, id, block);
    }

    public static void init() {
        DUNGEON_PORTAL_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, "dungeonz:dungeon_portal_entity",
                new BlockEntityType<>(DungeonPortalEntity::new, Set.of(DUNGEON_PORTAL)));
        DUNGEON_SPAWNER_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, "dungeonz:dungeon_spawner_entity",
                new BlockEntityType<>(DungeonSpawnerEntity::new, Set.of(DUNGEON_SPAWNER)));
        DUNGEON_GATE_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, "dungeonz:dungeon_gate_entity", new BlockEntityType<>(DungeonGateEntity::new, Set.of(DUNGEON_GATE)));

        Registry.register(BuiltInRegistries.MENU, "dungeonz:portal", PORTAL);
    }
}
