package net.satisfy.meadow.core.registry;

import net.minecraft.resources.ResourceKey;

import dev.architectury.registry.level.entity.EntityAttributeRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.satisfy.meadow.Meadow;
import net.satisfy.meadow.core.block.entity.*;
import net.satisfy.meadow.core.entity.ChairEntity;
import net.satisfy.meadow.core.entity.PineBoatEntity;
import net.satisfy.meadow.core.entity.PineChestBoatEntity;
import net.satisfy.meadow.core.entity.WaterBuffaloEntity;
import net.satisfy.meadow.core.entity.WoolyCowEntity;
import net.satisfy.meadow.platform.PlatformHelper;

import java.util.HashSet;
import java.util.function.Supplier;

public class EntityTypeRegistry {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Meadow.MOD_ID, Registries.BLOCK_ENTITY_TYPE);
    // 26.2: entities register EAGERLY (registrar path, like blocks/items) because
    // item suppliers (spawn eggs) resolve them during ObjectRegistry init.
    private static final dev.architectury.registry.registries.Registrar<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Meadow.MOD_ID, Registries.ENTITY_TYPE).getRegistrar();

    public static final RegistrySupplier<BlockEntityType<StorageBlockEntity>> STORAGE_ENTITY = registerBlockEntity("storage", () -> new BlockEntityType<>(StorageBlockEntity::new, StorageTypeRegistry.registerBlocks(new HashSet<>())));
    public static final RegistrySupplier<BlockEntityType<CookingCauldronBlockEntity>> COOKING_CAULDRON = registerBlockEntity("cooking_cauldron", () -> new BlockEntityType<>(CookingCauldronBlockEntity::new, java.util.Set.of(ObjectRegistry.COOKING_CAULDRON.get(), ObjectRegistry.COOKING_FRAME.get())));
    public static final RegistrySupplier<BlockEntityType<CheeseFormBlockEntity>> CHEESE_FORM_BLOCK_ENTITY = registerBlockEntity("cheese_form", () -> new BlockEntityType<>(CheeseFormBlockEntity::new, java.util.Set.of(ObjectRegistry.CHEESE_FORM.get())));
    public static final RegistrySupplier<BlockEntityType<CheeseRackBlockEntity>> CHEESE_RACK_BLOCK_ENTITY = registerBlockEntity("cheese_rack", () -> new BlockEntityType<>(CheeseRackBlockEntity::new, java.util.Set.of(ObjectRegistry.PINE_CHEESE_RACK.get())));
    public static final RegistrySupplier<BlockEntityType<StoveBlockEntity>> STOVE_BLOCK_ENTITY = registerBlockEntity("stove_block_entity", () -> new BlockEntityType<>(StoveBlockEntity::new, java.util.Set.of(ObjectRegistry.TILED_STOVE_SMOKER.get())));
    public static final RegistrySupplier<BlockEntityType<CabinetBlockEntity>> CABINET_BLOCK_ENTITY = registerBlockEntity("cabinet", () -> new BlockEntityType<>(CabinetBlockEntity::new, StorageTypeRegistry.registerBlocks(new HashSet<>())));
    public static final RegistrySupplier<BlockEntityType<CompletionistBannerEntity>> MEADOW_BANNER = registerBlockEntity("meadow_banner", () -> new BlockEntityType<>(CompletionistBannerEntity::new, java.util.Set.of(ObjectRegistry.MEADOW_BANNER.get(), ObjectRegistry.MEADOW_WALL_BANNER.get())));
    public static final RegistrySupplier<BlockEntityType<PineSignBlockEntity>> MOD_SIGN = BLOCK_ENTITY_TYPES.register("mod_sign", () -> new BlockEntityType<>(PineSignBlockEntity::new, java.util.Set.of(ObjectRegistry.PINE_SIGN.get(), ObjectRegistry.PINE_WALL_SIGN.get())));
    public static final RegistrySupplier<BlockEntityType<PineHangingSignBlockEntity>> MOD_HANGING_SIGN = BLOCK_ENTITY_TYPES.register("mod_hanging_sign", () -> new BlockEntityType<>(PineHangingSignBlockEntity::new, java.util.Set.of(ObjectRegistry.PINE_HANGING_SIGN.get(), ObjectRegistry.PINE_WALL_HANGING_SIGN.get())));
    public static final RegistrySupplier<BlockEntityType<WardrobeBlockEntity>> WARDROBE_BLOCK_ENTITY = registerBlockEntity("wardrobe", () -> new BlockEntityType<>(WardrobeBlockEntity::new, java.util.Set.of(ObjectRegistry.PINE_WARDROBE.get())));

    public static final RegistrySupplier<EntityType<WaterBuffaloEntity>> WATER_BUFFALO = registerEntity("water_buffalo", () -> EntityType.Builder.of(WaterBuffaloEntity::new, MobCategory.CREATURE).sized(0.9f, 1.4f).build(ResourceKey.create(Registries.ENTITY_TYPE, Meadow.identifier("water_buffalo"))));
    public static final RegistrySupplier<EntityType<WoolyCowEntity>> WOOLY_COW = registerEntity("wooly_cow", () -> EntityType.Builder.of(WoolyCowEntity::new, MobCategory.CREATURE).sized(0.9f, 1.4f).build(ResourceKey.create(Registries.ENTITY_TYPE, Meadow.identifier("wooly_cow"))));
    public static final RegistrySupplier<EntityType<ChairEntity>> CHAIR = registerEntity("chair", () -> EntityType.Builder.of(ChairEntity::new, MobCategory.MISC).sized(0.001F, 0.001F).build(ResourceKey.create(Registries.ENTITY_TYPE, Meadow.identifier("chair"))));

    public static final Supplier<EntityType<PineBoatEntity>> PINE_BOAT = PlatformHelper.registerBoatType("pine_boat", PineBoatEntity::new, MobCategory.MISC, 1.375F, 0.5625F, 10);
    public static final Supplier<EntityType<PineChestBoatEntity>> PINE_CHEST_BOAT = PlatformHelper.registerBoatType("pine_chest_boat", PineChestBoatEntity::new, MobCategory.MISC, 1.375F, 0.5625F, 10);

    public static void registerCow(Supplier<? extends EntityType<? extends Animal>> typeSupplier) {
        EntityAttributeRegistry.register(typeSupplier, net.minecraft.world.entity.animal.cow.AbstractCow::createAttributes);
    }

    public static <T extends EntityType<?>> RegistrySupplier<T> registerEntity(final String path, final Supplier<T> type) {
        return ENTITY_TYPES.register(Meadow.identifier(path), type);
    }

    private static <T extends BlockEntityType<?>> RegistrySupplier<T> registerBlockEntity(final String path, final Supplier<T> type) {
        return BLOCK_ENTITY_TYPES.register(Meadow.identifier(path), type);
    }

    public static void init() {
        BLOCK_ENTITY_TYPES.register();
        net.satisfy.meadow.platform.PlatformHelper.initBoats();
        registerCow(WOOLY_COW);
        registerCow(WATER_BUFFALO);
    }
}