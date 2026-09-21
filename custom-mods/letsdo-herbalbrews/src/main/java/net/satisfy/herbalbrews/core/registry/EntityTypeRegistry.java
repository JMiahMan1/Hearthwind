package net.satisfy.herbalbrews.core.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.satisfy.herbalbrews.HerbalBrews;
import net.satisfy.herbalbrews.core.blocks.entity.*;
import net.satisfy.herbalbrews.core.util.HerbalBrewsIdentifier;

import java.util.function.Supplier;

import static net.satisfy.herbalbrews.core.registry.ObjectRegistry.*;

public class EntityTypeRegistry {

    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(HerbalBrews.MOD_ID, Registries.BLOCK_ENTITY_TYPE);

    public static final RegistrySupplier<BlockEntityType<TeaKettleBlockEntity>> TEA_KETTLE_BLOCK_ENTITY = registerBlockEntity("tea_kettle", () -> new BlockEntityType<>(TeaKettleBlockEntity::new, java.util.Set.of(TEA_KETTLE.get(), COPPER_TEA_KETTLE.get())));
    public static final RegistrySupplier<BlockEntityType<CauldronBlockEntity>> CAULDRON_BLOCK_ENTITY = registerBlockEntity("cauldron", () -> new BlockEntityType<>(CauldronBlockEntity::new, java.util.Set.of(CAULDRON.get())));
    public static final RegistrySupplier<BlockEntityType<TeaLeafBlockEntity>> TEA_LEAF_BLOCK_ENTITY = registerBlockEntity("tea_leaf", () -> new BlockEntityType<>(TeaLeafBlockEntity::new, java.util.Set.of(GREEN_TEA_LEAF_BLOCK.get(), MIXED_TEA_LEAF_BLOCK.get(), DRIED_GREEN_TEA_LEAF_BLOCK.get())));
    public static final RegistrySupplier<BlockEntityType<CompletionistBannerEntity>> HERBALBREWS_BANNER = registerBlockEntity("herbalbrews_banner", () -> new BlockEntityType<>(CompletionistBannerEntity::new, java.util.Set.of(ObjectRegistry.HERBALBREWS_BANNER.get(), HERBALBREWS_WALL_BANNER.get())));
    public static final RegistrySupplier<BlockEntityType<StoveBlockEntity>> STOVE_BLOCK_ENTITY = registerBlockEntity("stove_block_entity", () -> new BlockEntityType<>(StoveBlockEntity::new, java.util.Set.of(STOVE.get())));
    public static final RegistrySupplier<BlockEntityType<JugBlockEntity>> JUG_BLOCK_ENTITY = registerBlockEntity("jug_block_entity", () -> new BlockEntityType<>(JugBlockEntity::new, java.util.Set.of(JUG.get())));
    public static final RegistrySupplier<BlockEntityType<DrinkBlockEntity>> DRINK_BLOCK_ENTITY = registerBlockEntity("drink_block_entity", () -> new BlockEntityType<>(DrinkBlockEntity::new, java.util.Set.of(GREEN_TEA_BLOCK.get(), COFFEE_BLOCK.get(), BLACK_TEA_BLOCK.get(), LAVENDER_TEA_BLOCK.get(), YERBA_MATE_TEA_BLOCK.get(), OOLONG_TEA_BLOCK.get(), ROOIBOS_TEA_BLOCK.get(), HIBISCUS_TEA_BLOCK.get(), MILK_COFFEE_BLOCK.get())));


    private static <T extends BlockEntityType<?>> RegistrySupplier<T> registerBlockEntity(String name, final Supplier<T> type) {
        return BLOCK_ENTITY_TYPES.register(HerbalBrewsIdentifier.identifier(name), type);
    }

    public static void init() {
        BLOCK_ENTITY_TYPES.register();
    }
}
