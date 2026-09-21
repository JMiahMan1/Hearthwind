package net.satisfy.vinery.client;

import dev.architectury.registry.client.gui.MenuScreenRegistry;
import dev.architectury.registry.client.level.entity.EntityModelLayerRegistry;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockTintsFactory;


import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.level.GrassColor;
import net.satisfy.vinery.client.gui.ApplePressGui;
import net.satisfy.vinery.client.gui.FermentationBarrelGui;
import net.satisfy.vinery.client.model.StrawHatModel;
import net.satisfy.vinery.client.model.WinemakerBootsModel;
import net.satisfy.vinery.client.model.WinemakerChestplateModel;
import net.satisfy.vinery.client.model.WinemakerLeggingsModel;
import net.satisfy.vinery.client.render.block.CompletionistBannerRenderer;
import net.satisfy.vinery.client.render.block.DarkCherryHangingSignRenderer;
import net.satisfy.vinery.client.render.block.DarkCherrySignRenderer;

import net.satisfy.vinery.client.render.block.LatticeRenderer;
import net.satisfy.vinery.client.render.block.storage.*;
import net.satisfy.vinery.client.render.entity.ChairRenderer;
import net.satisfy.vinery.client.render.entity.DarkCherryBoatRenderer;
import net.satisfy.vinery.client.render.entity.MuleRenderer;
import net.satisfy.vinery.client.render.entity.WanderingWinemakerRenderer;
import net.satisfy.vinery.core.registry.EntityTypeRegistry;
import net.satisfy.vinery.core.registry.ScreenhandlerTypeRegistry;
import net.satisfy.vinery.core.registry.StorageTypeRegistry;

import static net.satisfy.vinery.core.registry.ObjectRegistry.*;

@Environment(EnvType.CLIENT)
public class VineryClient {
    public static void onInitializeClient() {
        // 26.2: cutout/translucent layers moved to blockstate JSON
        // ("render_type"); no runtime registration API.
        // 26.2: no item-tint API in fabric-rendering-v1; the grass-slab item
        // keeps its default tint while placed blocks keep biome colors below.

        // 26.2: BlockTintSource takes (state) only; world/pos variant renamed
        // colorInWorld. Grass/foliage tints preserved via BiomeColors.
        BlockColorRegistry.register((BlockTintsFactory) (state, world, pos, tints) -> {
            if (world == null || pos == null) {
                tints.add(-1);
                return;
            }
            tints.add(BiomeColors.getAverageGrassColor(world, pos));
        }, GRASS_SLAB.get());
        BlockColorRegistry.register((BlockTintsFactory) (state, world, pos, tints) -> {
            if (world == null || pos == null) {
                tints.add(-1);
                return;
            }
            tints.add(BiomeColors.getAverageFoliageColor(world, pos));
        }, JUNGLE_RED_GRAPE_BUSH.get(), JUNGLE_WHITE_GRAPE_BUSH.get());

        registerStorageType();
        registerScreenFactory();
        registerBlockEntityRenderer();
    }

    public static void preInitClient() {
        registerEntityModelLayer();
        registerEntityRenderers();
    }

    public static void registerStorageTypes(net.minecraft.resources.Identifier location, StorageTypeRenderer renderer) {
        StorageBlockEntityRenderer.registerStorageType(location, renderer);
    }

    public static void registerStorageType() {
        registerStorageTypes(StorageTypeRegistry.BIG_BOTTLE, new BigBottleRenderer());
        registerStorageTypes(StorageTypeRegistry.FOUR_BOTTLE, new FourBottleRenderer());
        registerStorageTypes(StorageTypeRegistry.NINE_BOTTLE, new NineBottleRenderer());
        registerStorageTypes(StorageTypeRegistry.SHELF, new ShelfRenderer());
        registerStorageTypes(StorageTypeRegistry.WINE_BOX, new WineBoxRenderer());
        registerStorageTypes(StorageTypeRegistry.WINE_BOTTLE, new WineBottleRenderer());
    }

    public static void registerScreenFactory() {
        MenuScreenRegistry.registerScreenFactory(ScreenhandlerTypeRegistry.FERMENTATION_BARREL_GUI_HANDLER.get(), FermentationBarrelGui::new);
        MenuScreenRegistry.registerScreenFactory(ScreenhandlerTypeRegistry.APPLE_PRESS_GUI_HANDLER.get(), ApplePressGui::new);
    }

    public static void registerBlockEntityRenderer() {
        BlockEntityRendererRegistry.register(EntityTypeRegistry.VINERY_STANDARD.get(), CompletionistBannerRenderer::new);
        BlockEntityRendererRegistry.register(EntityTypeRegistry.STORAGE_ENTITY.get(), StorageBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(EntityTypeRegistry.LATTICE.get(), LatticeRenderer::new);
        BlockEntityRendererRegistry.register(EntityTypeRegistry.MOD_SIGN.get(), DarkCherrySignRenderer::new);
        BlockEntityRendererRegistry.register(EntityTypeRegistry.MOD_HANGING_SIGN.get(), DarkCherryHangingSignRenderer::new);
    }

    public static void registerEntityModelLayer() {
        EntityModelLayerRegistry.register(StrawHatModel.LAYER_LOCATION, StrawHatModel::createBodyLayer);
        EntityModelLayerRegistry.register(WinemakerChestplateModel.LAYER_LOCATION, WinemakerChestplateModel::createBodyLayer);
        EntityModelLayerRegistry.register(WinemakerLeggingsModel.LAYER_LOCATION, WinemakerLeggingsModel::createBodyLayer);
        EntityModelLayerRegistry.register(WinemakerBootsModel.LAYER_LOCATION, WinemakerBootsModel::createBodyLayer);
        EntityModelLayerRegistry.register(CompletionistBannerRenderer.LAYER_LOCATION, CompletionistBannerRenderer::createBodyLayer);
        EntityModelLayerRegistry.register(LatticeRenderer.LAYER_LOCATION, LatticeRenderer::getTexturedModelData);
        EntityModelLayerRegistry.register(DarkCherrySignRenderer.LAYER_LOCATION, DarkCherrySignRenderer::createSignLayer);
        EntityModelLayerRegistry.register(DarkCherryHangingSignRenderer.LAYER_LOCATION, DarkCherryHangingSignRenderer::createHangingSignLayer);
    }

    public static void registerEntityRenderers() {
        EntityRendererRegistry.register(EntityTypeRegistry.CHAIR, ChairRenderer::new);
        EntityRendererRegistry.register(EntityTypeRegistry.MULE, MuleRenderer::new);
        EntityRendererRegistry.register(EntityTypeRegistry.WANDERING_WINEMAKER, WanderingWinemakerRenderer::new);
        EntityRendererRegistry.register(EntityTypeRegistry.DARK_CHERRY_BOAT, context -> (net.minecraft.client.renderer.entity.EntityRenderer<net.satisfy.vinery.core.entity.DarkCherryBoatEntity, ?>) (net.minecraft.client.renderer.entity.EntityRenderer<?, ?>) new DarkCherryBoatRenderer(context, false));
        EntityRendererRegistry.register(EntityTypeRegistry.DARK_CHERRY_CHEST_BOAT, context -> (net.minecraft.client.renderer.entity.EntityRenderer<net.satisfy.vinery.core.entity.DarkCherryChestBoatEntity, ?>) (net.minecraft.client.renderer.entity.EntityRenderer<?, ?>) new DarkCherryBoatRenderer(context, true));
    }
}
