package net.satisfy.candlelight.core.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.satisfy.candlelight.Candlelight;
import net.satisfy.candlelight.core.block.entity.*;
import net.satisfy.farm_and_charm.core.block.entity.EffectFoodBlockEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import static net.satisfy.candlelight.core.registry.ObjectRegistry.*;

public class EntityTypeRegistry {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Candlelight.MOD_ID, Registries.BLOCK_ENTITY_TYPE);
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Candlelight.MOD_ID, Registries.ENTITY_TYPE);

    public static final RegistrySupplier<BlockEntityType<StorageBlockEntity>> STORAGE_BLOCK_ENTITY = registerBlockEntity("storage", () -> new BlockEntityType<>(StorageBlockEntity::new, new HashSet<>(StorageTypeRegistry.registerBlocks(new HashSet<>()))));
    public static final RegistrySupplier<BlockEntityType<SideBoardBlockEntity>> SIDEBOARD_BLOCK_ENTITY = registerBlockEntity("sideboard", () -> new BlockEntityType<>(SideBoardBlockEntity::new, java.util.Set.of(SIDEBOARD.get())));
    public static final RegistrySupplier<BlockEntityType<LargeCookingPotBlockEntity>> LARGE_COOKING_POT_BLOCK_ENTITY = registerBlockEntity("large_cooking_pot", () -> new BlockEntityType<>(LargeCookingPotBlockEntity::new, java.util.Set.of(COOKING_POT.get())));
    public static final RegistrySupplier<BlockEntityType<TypewriterEntity>> TYPE_WRITER_BLOCK_ENTITY = registerBlockEntity("type_writer", () -> new BlockEntityType<>(TypewriterEntity::new, java.util.Set.of(TYPEWRITER_IRON.get(), TYPEWRITER_GOLD.get())));
    public static final RegistrySupplier<BlockEntityType<CookingPanBlockEntity>> COOKING_PAN_BLOCK_ENTITY = registerBlockEntity("cooking_pan", () -> new BlockEntityType<>(CookingPanBlockEntity::new, java.util.Set.of(COOKING_PAN.get())));
    public static final RegistrySupplier<BlockEntityType<DinnerBellBlockEntity>> DINNER_BELL_BLOCK_ENTITY = registerBlockEntity("dinner_bell", () -> new BlockEntityType<>(DinnerBellBlockEntity::new, java.util.Set.of(DINNER_BELL.get())));
    public static final RegistrySupplier<BlockEntityType<CStoveBlockEntity>> STOVE_BLOCK_ENTITY = registerBlockEntity("stove_block", () -> new BlockEntityType<>(CStoveBlockEntity::new, java.util.Set.of(COBBLESTONE_STOVE.get(), MUD_STOVE.get(), GRANITE_STOVE.get(), SANDSTONE_STOVE.get(), STONE_BRICKS_STOVE.get(), RED_NETHER_BRICKS_STOVE.get(), DEEPSLATE_STOVE.get(), QUARTZ_STOVE.get(), END_STOVE.get(), BASALT_STOVE.get(), BAMBOO_STOVE.get())));
    public static final RegistrySupplier<BlockEntityType<CabinetBlockEntity>> CABINET_BLOCK_ENTITY = registerBlockEntity("cabinet", () -> new BlockEntityType<>(CabinetBlockEntity::new, new HashSet<>(addCabinet(new HashSet<>()))));
    public static final RegistrySupplier<BlockEntityType<CompletionistBannerEntity>> CANDLELIGHT_BANNER_ENTITY = registerBlockEntity("candlelight_banner_entity", () -> new BlockEntityType<>(CompletionistBannerEntity::new, java.util.Set.of(CANDLELIGHT_BANNER.get(), CANDLELIGHT_WALL_BANNER.get())));
    public static final RegistrySupplier<BlockEntityType<TableSetBlockEntity>> TABLE_SET_BLOCK_ENTITY = registerBlockEntity("table_set", () -> new BlockEntityType<>(TableSetBlockEntity::new, java.util.Set.of(TABLE_SET.get())));
    public static final RegistrySupplier<BlockEntityType<EffectFoodBlockEntity>> EFFECT_FOOD_BLOCK_ENTITY = registerBlockEntity("effect_food_block", () -> new BlockEntityType<>(EffectFoodBlockEntity::new, java.util.Set.of(LASAGNE_BLOCK.get(), TOMATO_MOZZARELLA_BLOCK.get(), PORK_RIBS_BLOCK.get(), FRESH_GARDEN_SALAD_BLOCK.get(), BEEF_WELLINGTON_BLOCK.get())));
    public static final RegistrySupplier<BlockEntityType<WallDecorationBlockEntity>> WALL_DECORATION = registerBlockEntity("wall_decoration", () -> new BlockEntityType<>(WallDecorationBlockEntity::new, java.util.Set.of(HEART.get())));

    private static <T extends BlockEntityType<?>> RegistrySupplier<T> registerBlockEntity(final String path, final Supplier<T> type) {
        return BLOCK_ENTITY_TYPES.register(Candlelight.identifier(path), type);
    }

    public static void init() {
        ENTITY_TYPES.register();
        BLOCK_ENTITY_TYPES.register();
    }

    private static Set<Block> addCabinet(Set<Block> blocks) {
        blocks.add(DRAWER.get());
        blocks.add(OAK_DRAWER.get());
        blocks.add(BIRCH_DRAWER.get());
        blocks.add(SPRUCE_DRAWER.get());
        blocks.add(DARK_OAK_DRAWER.get());
        blocks.add(ACACIA_DRAWER.get());
        blocks.add(JUNGLE_DRAWER.get());
        blocks.add(MANGROVE_DRAWER.get());
        blocks.add(WARPED_DRAWER.get());
        blocks.add(CRIMSON_DRAWER.get());
        blocks.add(CHERRY_DRAWER.get());
        blocks.add(BAMBOO_DRAWER.get());
        blocks.add(CABINET.get());
        blocks.add(OAK_CABINET.get());
        blocks.add(BIRCH_CABINET.get());
        blocks.add(SPRUCE_CABINET.get());
        blocks.add(DARK_OAK_CABINET.get());
        blocks.add(ACACIA_CABINET.get());
        blocks.add(JUNGLE_CABINET.get());
        blocks.add(MANGROVE_CABINET.get());
        blocks.add(WARPED_CABINET.get());
        blocks.add(CRIMSON_CABINET.get());
        blocks.add(CHERRY_CABINET.get());
        blocks.add(BAMBOO_CABINET.get());
        return blocks;
    }
}
