package net.satisfy.nethervinery.core.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.satisfy.nethervinery.core.NetherVinery;
import net.satisfy.nethervinery.core.block.entity.NetherStorageBlockEntity;

import java.util.HashSet;
import java.util.Set;

public class NetherEntityTypeRegistry {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(NetherVinery.MODID, Registries.BLOCK_ENTITY_TYPE);

    public static final RegistrySupplier<BlockEntityType<NetherStorageBlockEntity>> STORAGE_ENTITY = BLOCK_ENTITY_TYPES.register("storage", () -> {Set<Block> blocks = NetherStorageTypeRegistry.registerBlocks(new HashSet<>());return new BlockEntityType<>(NetherStorageBlockEntity::new, blocks);});

    public static void init() {
        BLOCK_ENTITY_TYPES.register();
    }
}
