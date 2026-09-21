package net.satisfy.bakery.client;

import dev.architectury.registry.client.level.entity.EntityModelLayerRegistry;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockTintsFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.BiomeColors;
import net.satisfy.bakery.client.gui.StreetSignEditGui;
import net.satisfy.bakery.client.renderer.block.*;
import net.satisfy.bakery.core.block.entity.StreetSignBlockEntity;
import net.satisfy.bakery.core.registry.EntityTypeRegistry;
import net.satisfy.bakery.core.registry.ObjectRegistry;
import net.satisfy.bakery.core.registry.StorageTypeRegistry;

@Environment(EnvType.CLIENT)
public class BakeryClient {

    public static void initClient() {
        // 26.2: cutout/translucent layers moved to blockstate JSON
        // ("render_type"); no runtime registration API.

        registerStorageType();
        registerBlockEntityRenderer();

        // 26.2: BlockTintSource takes (state) only; world/pos variant renamed
        // colorInWorld. Water tint preserved via BiomeColors average.
        BlockColorRegistry.register((BlockTintsFactory) (state, world, pos, tints) -> {
            if (world == null || pos == null) {
                tints.add(-1);
                return;
            }
            tints.add(BiomeColors.getAverageWaterColor(world, pos));
        }, ObjectRegistry.KITCHEN_SINK.get());
    }

    public static void openStreetSignScreen(StreetSignBlockEntity entity) {
        Minecraft.getInstance().setScreenAndShow(new StreetSignEditGui(entity));
    }

    public static void preInitClient() {
        registerEntityModelLayer();
    }

    public static void registerStorageType(Identifier location, StorageTypeRenderer renderer) {
       StorageBlockEntityRenderer.registerStorageType(location, renderer);
    }

    public static void registerStorageType() {
        registerStorageType(StorageTypeRegistry.CAKE_STAND, new CakeStandRenderer());
        registerStorageType(StorageTypeRegistry.TRAY, new TrayRenderer());
        registerStorageType(StorageTypeRegistry.BREADBOX, new BreadBoxRenderer());
        registerStorageType(StorageTypeRegistry.CAKE_DISPLAY, new CakeDisplayRenderer());
        registerStorageType(StorageTypeRegistry.CUPCAKE_DISPLAY, new CupcakeDisplayRenderer());
        registerStorageType(StorageTypeRegistry.WALL_DISPLAY, new WallDisplayRenderer());
    }

    public static void registerBlockEntityRenderer() {
        BlockEntityRendererRegistry.register(EntityTypeRegistry.BAKERY_BANNER.get(), CompletionistBannerRenderer::new);
        BlockEntityRendererRegistry.register(EntityTypeRegistry.STORAGE_ENTITY.get(), StorageBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(EntityTypeRegistry.STREET_SIGN_BLOCK_ENTITY.get(), StreetSignBlockRenderer::new);
    }

    public static void registerEntityModelLayer() {
        EntityModelLayerRegistry.register(CompletionistBannerRenderer.LAYER_LOCATION, CompletionistBannerRenderer::createBodyLayer);
    }
}
