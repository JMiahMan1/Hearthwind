package net.satisfy.farm_and_charm.core.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.satisfy.farm_and_charm.FarmAndCharm;
import net.satisfy.farm_and_charm.core.block.entity.*;
import net.satisfy.farm_and_charm.core.entity.ChairEntity;
import net.satisfy.farm_and_charm.core.entity.PlowCartEntity;
import net.satisfy.farm_and_charm.core.entity.RottenTomatoEntity;
import net.satisfy.farm_and_charm.core.entity.SupplyCartEntity;

import java.util.HashSet;
import java.util.function.Supplier;

public class EntityTypeRegistry {
    private static final Registrar<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(FarmAndCharm.MOD_ID, Registries.BLOCK_ENTITY_TYPE).getRegistrar();
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(FarmAndCharm.MOD_ID, Registries.ENTITY_TYPE);

    public static final RegistrySupplier<BlockEntityType<StorageBlockEntity>> STORAGE_ENTITY = registerBlockEntity("storage", () -> new BlockEntityType<>(StorageBlockEntity::new, new java.util.HashSet<>(StorageTypeRegistry.registerBlocks(new HashSet<>()))));
    public static final RegistrySupplier<BlockEntityType<RoasterBlockEntity>> ROASTER_BLOCK_ENTITY = registerBlockEntity("roaster", () -> new BlockEntityType<>(RoasterBlockEntity::new, java.util.Set.of(ObjectRegistry.ROASTER.get())));
    public static final RegistrySupplier<BlockEntityType<CraftingBowlBlockEntity>> CRAFTING_BOWL_BLOCK_ENTITY = registerBlockEntity("crafting_bowl", () -> new BlockEntityType<>(CraftingBowlBlockEntity::new, java.util.Set.of(ObjectRegistry.CRAFTING_BOWL.get())));
    public static final RegistrySupplier<BlockEntityType<CookingPotBlockEntity>> COOKING_POT_BLOCK_ENTITY = registerBlockEntity("cooking_pot", () -> new BlockEntityType<>(CookingPotBlockEntity::new, java.util.Set.of(ObjectRegistry.COOKING_POT.get())));
    public static final RegistrySupplier<BlockEntityType<StoveBlockEntity>> STOVE_BLOCK_ENTITY = registerBlockEntity("stove_block", () -> new BlockEntityType<>(StoveBlockEntity::new, java.util.Set.of(ObjectRegistry.STOVE.get())));
    public static final RegistrySupplier<BlockEntityType<WaterSprinklerBlockEntity>> SPRINKLER_BLOCK_ENTITY = registerBlockEntity("water_sprinkler", () -> new BlockEntityType<>(WaterSprinklerBlockEntity::new, java.util.Set.of(ObjectRegistry.WATER_SPRINKLER.get())));
    public static final RegistrySupplier<BlockEntityType<SiloBlockEntity>> SILO_BLOCK_ENTITY = registerBlockEntity("silo", () -> new BlockEntityType<>(SiloBlockEntity::new, java.util.Set.of(ObjectRegistry.SILO_WOOD.get(), ObjectRegistry.SILO_COPPER.get())));
    public static final RegistrySupplier<BlockEntityType<FeedingTroughBlockEntity>> FEEDING_TROUGH_BLOCK_ENTITY = registerBlockEntity("feeding_trough", () -> new BlockEntityType<>(FeedingTroughBlockEntity::new, java.util.Set.of(ObjectRegistry.FEEDING_TROUGH.get())));
    public static final RegistrySupplier<BlockEntityType<ScarecrowBlockEntity>> SCARECROW_BLOCK_ENTITY = registerBlockEntity("scarecrow", () -> new BlockEntityType<>(ScarecrowBlockEntity::new, java.util.Set.of(ObjectRegistry.SCARECROW.get())));
    public static final RegistrySupplier<BlockEntityType<MincerBlockEntity>> MINCER_BLOCK_ENTITY = registerBlockEntity("mincer", () -> new BlockEntityType<>(MincerBlockEntity::new, java.util.Set.of(ObjectRegistry.MINCER.get())));
    public static final RegistrySupplier<BlockEntityType<EffectFoodBlockEntity>> EFFECT_FOOD_BLOCK_ENTITY = registerBlockEntity("effect_food_block", () -> new BlockEntityType<>(EffectFoodBlockEntity::new, java.util.Set.of()));
    public static final RegistrySupplier<BlockEntityType<PetBowlBlockEntity>> PET_BOWL_BLOCK_ENTITY = registerBlockEntity("pet_bowl", () -> new BlockEntityType<>(PetBowlBlockEntity::new, java.util.Set.of(ObjectRegistry.PET_BOWL.get())));
    public static final RegistrySupplier<BlockEntityType<ChickenCoopBlockEntity>> CHICKEN_COOP_BLOCK_ENTITY = registerBlockEntity("chicken_coop", () -> new BlockEntityType<>(ChickenCoopBlockEntity::new, java.util.Set.of(ObjectRegistry.CHICKEN_COOP.get())));
    public static final RegistrySupplier<BlockEntityType<RopeKnotBlockEntity>> ROPE_KNOT_BLOCK_ENTITY = registerBlockEntity("rope_knot", () -> new BlockEntityType<>(RopeKnotBlockEntity::new, java.util.Set.of(ObjectRegistry.ROPE_KNOT.get())));

    public static final RegistrySupplier<EntityType<RottenTomatoEntity>> ROTTEN_TOMATO = registerEntityType("rotten_tomato", () -> EntityType.Builder.<RottenTomatoEntity>of(RottenTomatoEntity::new, MobCategory.MISC).sized(0.25f, 0.25f).build(ResourceKey.create(Registries.ENTITY_TYPE, FarmAndCharm.identifier("rotten_tomato"))));
    public static final RegistrySupplier<EntityType<SupplyCartEntity>> SUPPLY_CART = registerEntityType("cart", () -> EntityType.Builder.of(SupplyCartEntity::new, MobCategory.MISC).sized(1.875f, 0.875f).clientTrackingRange(10).build(ResourceKey.create(Registries.ENTITY_TYPE, FarmAndCharm.identifier("supply_cart"))));
    public static final RegistrySupplier<EntityType<PlowCartEntity>> PLOW = registerEntityType("plow", () -> EntityType.Builder.of(PlowCartEntity::new, MobCategory.MISC).sized(1.875f, 0.875f).clientTrackingRange(10).build(ResourceKey.create(Registries.ENTITY_TYPE, FarmAndCharm.identifier("plow"))));
    public static final RegistrySupplier<EntityType<ChairEntity>> CHAIR = registerEntityType("chair", () -> EntityType.Builder.of(ChairEntity::new, MobCategory.MISC).sized(0.001F, 0.001F).build(ResourceKey.create(Registries.ENTITY_TYPE, FarmAndCharm.identifier("chair"))));

    private static <T extends BlockEntityType<?>> RegistrySupplier<T> registerBlockEntity(final String path, final Supplier<T> type) {
        return BLOCK_ENTITY_TYPES.register(FarmAndCharm.identifier(path), type);
    }

    private static <T extends EntityType<?>> RegistrySupplier<T> registerEntityType(final String path, final Supplier<T> type) {
        return ENTITY_TYPES.register(FarmAndCharm.identifier(path), type);
    }


    public static void init() {
        ENTITY_TYPES.register();
    }


}
