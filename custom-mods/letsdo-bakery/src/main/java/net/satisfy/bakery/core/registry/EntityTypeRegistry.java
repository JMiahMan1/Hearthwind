package net.satisfy.bakery.core.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.satisfy.bakery.Bakery;
import net.satisfy.bakery.core.block.entity.*;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class EntityTypeRegistry {
    private static final Registrar<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Bakery.MOD_ID, Registries.BLOCK_ENTITY_TYPE).getRegistrar();

    public static final RegistrySupplier<BlockEntityType<SmallCookingPotBlockEntity>> SMALL_COOKING_POT_BLOCK_ENTITY = registerBlockEntity("small_cooking_pot", () -> new BlockEntityType<>(SmallCookingPotBlockEntity::new, java.util.Set.of(ObjectRegistry.SMALL_COOKING_POT.get())));
    public static final RegistrySupplier<BlockEntityType<CompletionistBannerEntity>> BAKERY_BANNER = registerBlockEntity("bakery_banner", () -> new BlockEntityType<>(CompletionistBannerEntity::new, java.util.Set.of(ObjectRegistry.BAKERY_BANNER.get(), ObjectRegistry.BAKERY_WALL_BANNER.get())));
    public static final RegistrySupplier<BlockEntityType<StorageBlockEntity>> STORAGE_ENTITY = registerBlockEntity("storage", () -> new BlockEntityType<>(StorageBlockEntity::new, new java.util.HashSet<>(StorageTypeRegistry.registerBlocks(new HashSet<>()))));
    public static final RegistrySupplier<BlockEntityType<CabinetBlockEntity>> CABINET_BLOCK_ENTITY = registerBlockEntity("cabinet", () -> new BlockEntityType<>(CabinetBlockEntity::new, new java.util.HashSet<>(addCabinet(new HashSet<>()))));
    public static final RegistrySupplier<BlockEntityType<StreetSignBlockEntity>> STREET_SIGN_BLOCK_ENTITY = registerBlockEntity("street_sign", () -> new BlockEntityType<>(StreetSignBlockEntity::new, java.util.Set.of(ObjectRegistry.STREET_SIGN.get())));

    public static Set<Block> addCabinet(Set<Block> blocks) {
        blocks.add(ObjectRegistry.CABINET.get());
        blocks.add(ObjectRegistry.DRAWER.get());
        blocks.add(ObjectRegistry.WALL_CABINET.get());
        return blocks;
    }

    private static <T extends BlockEntityType<?>> RegistrySupplier<T> registerBlockEntity(String name, final Supplier<T> type) {
        return BLOCK_ENTITY_TYPES.register(Bakery.identifier(name), type);
    }

    public static void init() {
    }
}
