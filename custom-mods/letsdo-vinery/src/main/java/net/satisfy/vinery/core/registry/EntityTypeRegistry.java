package net.satisfy.vinery.core.registry;

import dev.architectury.registry.level.entity.EntityAttributeRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.satisfy.vinery.core.Vinery;
import net.satisfy.vinery.core.block.entity.*;
import net.satisfy.vinery.core.entity.ChairEntity;
import net.satisfy.vinery.core.entity.DarkCherryBoatEntity;
import net.satisfy.vinery.core.entity.DarkCherryChestBoatEntity;
import net.satisfy.vinery.core.entity.TraderMuleEntity;
import net.satisfy.vinery.core.entity.WanderingWinemakerEntity;
import net.satisfy.vinery.platform.PlatformHelper;

import java.util.HashSet;
import java.util.function.Supplier;

import static net.satisfy.vinery.core.registry.ObjectRegistry.*;

public class EntityTypeRegistry {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Vinery.MOD_ID, Registries.BLOCK_ENTITY_TYPE);
    // 26.2: entities register EAGERLY (registrar path, like blocks/items) because
    // item suppliers (spawn eggs) resolve them during ObjectRegistry init.
    private static final dev.architectury.registry.registries.Registrar<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Vinery.MOD_ID, Registries.ENTITY_TYPE).getRegistrar();

    public static final RegistrySupplier<BlockEntityType<DarkCherrySignBlockEntity>> MOD_SIGN = BLOCK_ENTITY_TYPES.register("mod_sign", () -> new BlockEntityType<>(DarkCherrySignBlockEntity::new, java.util.Set.of(DARK_CHERRY_SIGN.get(), DARK_CHERRY_WALL_SIGN.get())));
    public static final RegistrySupplier<BlockEntityType<DarkCherryHangingSignBlockEntity>> MOD_HANGING_SIGN = BLOCK_ENTITY_TYPES.register("mod_hanging_sign", () -> new BlockEntityType<>(DarkCherryHangingSignBlockEntity::new, java.util.Set.of(DARK_CHERRY_HANGING_SIGN.get(), DARK_CHERRY_WALL_HANGING_SIGN.get())));

    public static final RegistrySupplier<BlockEntityType<StorageBlockEntity>> STORAGE_ENTITY =
            registerBlockEntity("storage", () ->
                    new BlockEntityType<>(StorageBlockEntity::new, new java.util.HashSet<>(StorageTypeRegistry.registerBlocks(new HashSet<>())))
            );

    public static final RegistrySupplier<BlockEntityType<CabinetBlockEntity>> CABINET_BLOCK_ENTITY = registerBlockEntity("cabinet", () -> {
        Block[] cabinetBlocks = StorageTypeRegistry.getCabinetBlocks();
        return new BlockEntityType<>(CabinetBlockEntity::new, java.util.Set.of(cabinetBlocks));
    });

    public static final RegistrySupplier<BlockEntityType<StoragePotBlockEntity>> STORAGE_POT_ENTITY =
            registerBlockEntity("storage_pot", () ->
                    new BlockEntityType<>(StoragePotBlockEntity::new, java.util.Set.of(STORAGE_POT.get()))
            );

    public static final Supplier<EntityType<DarkCherryBoatEntity>> DARK_CHERRY_BOAT = PlatformHelper.registerBoatType("dark_cherry_boat", DarkCherryBoatEntity::new, MobCategory.MISC, 1.375F, 0.5625F, 10);
    public static final Supplier<EntityType<DarkCherryChestBoatEntity>> DARK_CHERRY_CHEST_BOAT = PlatformHelper.registerBoatType("dark_cherry_chest_boat", DarkCherryChestBoatEntity::new, MobCategory.MISC, 1.375F, 0.5625F, 10);

    public static final RegistrySupplier<BlockEntityType<ApplePressBlockEntity>> APPLE_PRESS_BLOCK_ENTITY = registerBlockEntity("apple_press", () -> new BlockEntityType<>(ApplePressBlockEntity::new, java.util.Set.of(APPLE_PRESS.get())));
    public static final RegistrySupplier<BlockEntityType<FermentationBarrelBlockEntity>> FERMENTATION_BARREL_ENTITY = registerBlockEntity("fermentation_barrel", () -> new BlockEntityType<>(FermentationBarrelBlockEntity::new, java.util.Set.of(FERMENTATION_BARREL.get())));
    public static final RegistrySupplier<BlockEntityType<CompletionistBannerEntity>> VINERY_STANDARD = registerBlockEntity("vinery_standard", () -> new BlockEntityType<>(CompletionistBannerEntity::new, java.util.Set.of(ObjectRegistry.VINERY_STANDARD.get(), VINERY_WALL_STANDARD.get())));
    public static final RegistrySupplier<BlockEntityType<DarkCherryBarrelBlockEntity>> DARK_CHERRY_BARREL_ENTITY =
            registerBlockEntity("dark_cherry_barrel", () ->
                    new BlockEntityType<>(DarkCherryBarrelBlockEntity::new, java.util.Set.of(DARK_CHERRY_BARREL.get()))
            );

    public static final RegistrySupplier<BlockEntityType<LatticeBlockEntity>> LATTICE = registerBlockEntity("lattice", () -> new BlockEntityType<>(LatticeBlockEntity::new, java.util.Set.of(OAK_LATTICE.get(), SPRUCE_LATTICE.get(), CHERRY_LATTICE.get(), BIRCH_LATTICE.get(), DARK_OAK_LATTICE.get(), ACACIA_LATTICE.get(), BAMBOO_LATTICE.get(), JUNGLE_LATTICE.get(), MANGROVE_LATTICE.get(), DARK_CHERRY_LATTICE.get())));

    public static final RegistrySupplier<EntityType<TraderMuleEntity>> MULE = registerEntity("mule", () -> EntityType.Builder.of(TraderMuleEntity::new, MobCategory.CREATURE).sized(0.9f, 1.87f).clientTrackingRange(10).build(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, Vinery.identifier("mule"))));
    public static final RegistrySupplier<EntityType<WanderingWinemakerEntity>> WANDERING_WINEMAKER = registerEntity("wandering_winemaker", () -> EntityType.Builder.of(WanderingWinemakerEntity::new, MobCategory.CREATURE).sized(0.6f, 1.95f).clientTrackingRange(10).build(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, Vinery.identifier("wandering_winemaker"))));
    public static final RegistrySupplier<EntityType<ChairEntity>> CHAIR = registerEntity("chair", () -> EntityType.Builder.of(ChairEntity::new, MobCategory.MISC).sized(0.001F, 0.001F).build(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, Vinery.identifier("chair"))));

    public static <T extends EntityType<?>> RegistrySupplier<T> registerEntity(final String path, final Supplier<T> type) {
        return ENTITY_TYPES.register(Vinery.identifier(path), type);
    }

    private static <T extends BlockEntityType<?>> RegistrySupplier<T> registerBlockEntity(final String path, final Supplier<T> type) {
        return BLOCK_ENTITY_TYPES.register(Vinery.identifier(path), type);
    }

    static void registerAttributes() {
        EntityAttributeRegistry.register(MULE, () -> Llama.createAttributes().add(Attributes.MOVEMENT_SPEED, 0.2f));
        EntityAttributeRegistry.register(WANDERING_WINEMAKER, () -> Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 20.0).add(Attributes.MOVEMENT_SPEED, 0.5).add(Attributes.FOLLOW_RANGE, 16.0));
    }

    public static void init() {
        BLOCK_ENTITY_TYPES.register();
        net.satisfy.vinery.platform.PlatformHelper.initBoats();
        registerAttributes();
    }
}
