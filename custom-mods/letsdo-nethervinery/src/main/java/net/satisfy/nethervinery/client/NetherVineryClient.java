package net.satisfy.nethervinery.client;

import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockTintsFactory;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.resources.Identifier;
import net.satisfy.nethervinery.client.render.block.storage.*;
import net.satisfy.nethervinery.core.registry.NetherEntityTypeRegistry;
import net.satisfy.nethervinery.core.registry.NetherObjectRegistry;
import net.satisfy.nethervinery.core.registry.NetherStorageTypeRegistry;

@Environment(EnvType.CLIENT)
public class NetherVineryClient {
    public static void onInitializeClient() {
        // 26.2: cutout/translucent layers moved to blockstate JSON
        // ("render_type"); no runtime registration API.

        // 26.2: BlockTintSource takes (state) only; world/pos variant renamed
        // colorInWorld. Foliage tint preserved via BiomeColors.
        BlockColorRegistry.register((BlockTintsFactory) (state, world, pos, tints) -> {
            if (world == null || pos == null) {
                tints.add(-1);
                return;
            }
            tints.add(BiomeColors.getAverageFoliageColor(world, pos));
        }, NetherObjectRegistry.OBSIDIAN_STEM.get());

        BlockEntityRendererRegistry.register(NetherEntityTypeRegistry.STORAGE_ENTITY.get(), NetherStorageBlockEntityRenderer::new);

        registerNetherStorageType();

    }


    public static void registerNetherStorageTypes(Identifier location, NetherStorageTypeRenderer renderer){
        NetherStorageBlockEntityRenderer.registerStorageType(location, renderer);
    }

    public static void registerNetherStorageType(){
        registerNetherStorageTypes(NetherStorageTypeRegistry.BIG_BOTTLE, new NetherBigBottleRenderer());
        registerNetherStorageTypes(NetherStorageTypeRegistry.FOUR_BOTTLE, new NetherFourBottleRenderer());
        registerNetherStorageTypes(NetherStorageTypeRegistry.NINE_BOTTLE, new NetherNineBottleRenderer());
        registerNetherStorageTypes(NetherStorageTypeRegistry.WINE_BOTTLE, new NetherWineBottleRenderer());
    }
}
