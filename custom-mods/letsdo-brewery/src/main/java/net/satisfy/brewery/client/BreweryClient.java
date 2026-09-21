package net.satisfy.brewery.client;

import dev.architectury.registry.client.level.entity.EntityModelLayerRegistry;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockTintsFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.satisfy.brewery.client.gui.WallDecorationEditGui;
import net.satisfy.brewery.client.model.BeerElementalModel;
import net.satisfy.brewery.client.model.BrewfestBootsModel;
import net.satisfy.brewery.client.model.BrewfestChestplateModel;
import net.satisfy.brewery.client.model.BrewfestHatModel;
import net.satisfy.brewery.client.model.BrewfestLeggingsModel;
import net.satisfy.brewery.client.renderer.block.BeerMugRenderer;
import net.satisfy.brewery.client.renderer.block.BeverageRenderer;
import net.satisfy.brewery.client.renderer.block.BrewingstationRenderer;
import net.satisfy.brewery.client.renderer.block.CompletionistBannerRenderer;
import net.satisfy.brewery.client.renderer.block.StorageBlockEntityRenderer;
import net.satisfy.brewery.client.renderer.block.WallDecorationBlockRenderer;
import net.satisfy.brewery.core.block.entity.WallDecorationBlockEntity;
import net.satisfy.brewery.core.registry.EntityTypeRegistry;
import net.satisfy.brewery.core.registry.StorageTypeRegistry;

import static net.satisfy.brewery.core.registry.ObjectRegistry.*;

@Environment(EnvType.CLIENT)
public class BreweryClient {

    public static void onInitializeClient() {
        // 26.2: cutout/translucent layers moved to blockstate JSON
        // ("render_type"); no runtime registration API.

        // 26.2: BlockTintSource takes (state) only; world/pos variant renamed
        // colorInWorld. Water tint preserved via BiomeColors average.
        BlockColorRegistry.register((BlockTintsFactory) (state, world, pos, tints) -> {
            if (world == null || pos == null) {
                tints.add(-1);
                return;
            }
            tints.add(BiomeColors.getAverageWaterColor(world, pos));
        }, WOODEN_BREWINGSTATION.get(), COPPER_BREWINGSTATION.get(), NETHERITE_BREWINGSTATION.get());

        BlockEntityRendererRegistry.register(EntityTypeRegistry.BREWERY_BANNER.get(), CompletionistBannerRenderer::new);
        BlockEntityRendererRegistry.register(EntityTypeRegistry.STORAGE_ENTITY.get(), StorageBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(EntityTypeRegistry.BEER_MUG_BLOCK_ENTITY.get(), BeerMugRenderer::new);
        BlockEntityRendererRegistry.register(EntityTypeRegistry.BREWINGSTATION_BLOCK_ENTITY.get(), BrewingstationRenderer::new);
        BlockEntityRendererRegistry.register(EntityTypeRegistry.WALL_DECORATION.get(), WallDecorationBlockRenderer::new);

        StorageBlockEntityRenderer.registerStorageType(StorageTypeRegistry.BEVERAGE, new BeverageRenderer());
    }

    public static void openStreetSignScreen(WallDecorationBlockEntity entity) {
        Minecraft.getInstance().setScreenAndShow(new WallDecorationEditGui(entity));
    }

    public static void preInitClient() {
        registerEntityRenderers();
        registerEntityModelLayers();
    }

    private static void registerEntityRenderers() {
        // TODO: re-enable once entity renderers are ported to 26.2 (out of scope:
        // BeerElementalRenderer needs 3 type args, BeerElementalAttackRenderer
        // needs the submit model, DarkBrewEntity is not an Entity subtype).
        // EntityRendererRegistry.register(EntityTypeRegistry.BEER_ELEMENTAL, BeerElementalRenderer::new);
        // EntityRendererRegistry.register(EntityTypeRegistry.BEER_ELEMENTAL_ATTACK, BeerElementalAttackRenderer::new);
        // EntityRendererRegistry.register(EntityTypeRegistry.DARK_BREW, ThrownItemRenderer::new);
    }

    public static void registerEntityModelLayers() {
        EntityModelLayerRegistry.register(BrewfestHatModel.LAYER_LOCATION, BrewfestHatModel::createBodyLayer);
        EntityModelLayerRegistry.register(BrewfestChestplateModel.LAYER_LOCATION, BrewfestChestplateModel::createBodyLayer);
        EntityModelLayerRegistry.register(BrewfestLeggingsModel.LAYER_LOCATION, BrewfestLeggingsModel::createBodyLayer);
        EntityModelLayerRegistry.register(BrewfestBootsModel.LAYER_LOCATION, BrewfestBootsModel::createBodyLayer);
        EntityModelLayerRegistry.register(BeerElementalModel.BEER_ELEMENTAL_MODEL_LAYER, BeerElementalModel::createBodyLayer);
        EntityModelLayerRegistry.register(CompletionistBannerRenderer.LAYER_LOCATION, CompletionistBannerRenderer::createBodyLayer);
    }
}
