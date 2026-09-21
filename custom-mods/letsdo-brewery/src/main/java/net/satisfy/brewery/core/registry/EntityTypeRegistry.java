package net.satisfy.brewery.core.registry;

import dev.architectury.registry.level.entity.EntityAttributeRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.satisfy.brewery.Brewery;
import net.satisfy.brewery.core.block.entity.*;
import net.satisfy.brewery.core.entity.BeerElementalAttackEntity;
import net.satisfy.brewery.core.entity.BeerElementalEntity;
import net.satisfy.brewery.core.entity.DarkBrewEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class EntityTypeRegistry {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Brewery.MOD_ID, Registries.BLOCK_ENTITY_TYPE);
    // 26.2: entities register EAGERLY (registrar path, like blocks/items) because
    // item suppliers (spawn eggs) resolve them during ObjectRegistry init.
    private static final Registrar<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Brewery.MOD_ID, Registries.ENTITY_TYPE).getRegistrar();

    public static final RegistrySupplier<EntityType<DarkBrewEntity>> DARK_BREW = registerEntity("dark_brew", () -> EntityType.Builder.<DarkBrewEntity>of(DarkBrewEntity::new, MobCategory.MISC).sized(0.5f, 0.5f).build(ResourceKey.create(Registries.ENTITY_TYPE, Brewery.identifier("dark_brew"))));
    public static final RegistrySupplier<EntityType<BeerElementalEntity>> BEER_ELEMENTAL = registerEntity("beer_elemental", () -> EntityType.Builder.of(BeerElementalEntity::new, MobCategory.MONSTER).sized(1.0F, 1.6F).clientTrackingRange(80).updateInterval(3).build(ResourceKey.create(Registries.ENTITY_TYPE, Brewery.identifier("beer_elemental"))));
    public static final RegistrySupplier<EntityType<BeerElementalAttackEntity>> BEER_ELEMENTAL_ATTACK = registerEntity("beer_elemental_attack", () -> EntityType.Builder.<BeerElementalAttackEntity>of(BeerElementalAttackEntity::new, MobCategory.MISC).sized(0.3125F, 0.3125F).clientTrackingRange(4).updateInterval(10).build(ResourceKey.create(Registries.ENTITY_TYPE, Brewery.identifier("beer_elemental_attack"))));

    public static final RegistrySupplier<BlockEntityType<BrewstationBlockEntity>> BREWINGSTATION_BLOCK_ENTITY = registerBlockEntity("brewingstation", () -> new BlockEntityType<>(BrewstationBlockEntity::new, Set.of(ObjectRegistry.WOODEN_BREWINGSTATION.get(), ObjectRegistry.COPPER_BREWINGSTATION.get(), ObjectRegistry.NETHERITE_BREWINGSTATION.get())));
    public static final RegistrySupplier<BlockEntityType<BeerMugBlockEntity>> BEER_MUG_BLOCK_ENTITY = registerBlockEntity("beer_mug", () -> new BlockEntityType<>(BeerMugBlockEntity::new, Set.of(ObjectRegistry.BEER_MUG.get())));
    public static final RegistrySupplier<BlockEntityType<StorageBlockEntity>> STORAGE_ENTITY = registerBlockEntity("storage", () -> new BlockEntityType<>(StorageBlockEntity::new, new HashSet<>(StorageTypeRegistry.registerBlocks(new HashSet<>()))));
    public static final RegistrySupplier<BlockEntityType<CabinetBlockEntity>> CABINET_BLOCK_ENTITY = registerBlockEntity("cabinet", () -> new BlockEntityType<>(CabinetBlockEntity::new, new HashSet<>(addCabinet(new HashSet<>()))));
    public static final RegistrySupplier<BlockEntityType<CompletionistBannerEntity>> BREWERY_BANNER = registerBlockEntity("brewery_banner", () -> new BlockEntityType<>(CompletionistBannerEntity::new, Set.of(ObjectRegistry.BREWERY_BANNER.get(), ObjectRegistry.BREWERY_WALL_BANNER.get())));
    public static final RegistrySupplier<BlockEntityType<WallDecorationBlockEntity>> WALL_DECORATION = registerBlockEntity("wall_decoration", () -> new BlockEntityType<>(WallDecorationBlockEntity::new, Set.of(ObjectRegistry.GINGERBREAD.get())));

    public static Set<Block> addCabinet(Set<Block> blocks) {
        blocks.add(ObjectRegistry.CABINET.get());
        blocks.add(ObjectRegistry.DRAWER.get());
        blocks.add(ObjectRegistry.SIDEBOARD.get());
        blocks.add(ObjectRegistry.WALL_CABINET.get());
        return blocks;
    }

    public static void registerBeerElemental(Supplier<? extends EntityType<? extends Monster>> typeSupplier) {
        EntityAttributeRegistry.register(typeSupplier, BeerElementalEntity::createAttributes);
    }

    private static <T extends BlockEntityType<?>> RegistrySupplier<T> registerBlockEntity(final String path, final Supplier<T> type) {
        return BLOCK_ENTITY_TYPES.register(Brewery.identifier(path), type);
    }

    public static <T extends EntityType<?>> RegistrySupplier<T> registerEntity(final String path, final Supplier<T> type) {
        return ENTITY_TYPES.register(Brewery.identifier(path), type);
    }

    public static void init() {
        BLOCK_ENTITY_TYPES.register();
        registerBeerElemental(BEER_ELEMENTAL);
    }
}
