package net.satisfy.farm_and_charm.client;

import dev.architectury.registry.client.level.entity.EntityModelLayerRegistry;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.client.particle.ParticleProviderRegistry;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.architectury.registry.client.gui.MenuScreenRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockTintsFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.satisfy.farm_and_charm.client.event.ClientEventHandler;
import net.satisfy.farm_and_charm.client.gui.CookingPotGui;
import net.satisfy.farm_and_charm.client.gui.PetBowlEditGui;
import net.satisfy.farm_and_charm.client.gui.RoasterGui;
import net.satisfy.farm_and_charm.client.gui.StoveGui;
import net.satisfy.farm_and_charm.client.model.*;
import net.satisfy.farm_and_charm.client.particle.SoupBubbleParticle;
import net.satisfy.farm_and_charm.client.particle.SoupCookingBubbleParticle;
import net.satisfy.farm_and_charm.client.particle.SoupSteamParticle;
import net.satisfy.farm_and_charm.client.renderer.block.*;
import net.satisfy.farm_and_charm.client.renderer.entity.ChairRenderer;
import net.satisfy.farm_and_charm.client.renderer.entity.PlowCartRenderer;
import net.satisfy.farm_and_charm.client.renderer.entity.SupplyCartRenderer;
import net.satisfy.farm_and_charm.core.block.entity.PetBowlBlockEntity;
import net.satisfy.farm_and_charm.core.registry.EntityTypeRegistry;
import net.satisfy.farm_and_charm.core.registry.ParticleTypeRegistry;
import net.satisfy.farm_and_charm.core.registry.ScreenhandlerTypeRegistry;
import net.satisfy.farm_and_charm.core.registry.StorageTypeRegistry;

import static net.satisfy.farm_and_charm.core.registry.ObjectRegistry.*;

public class FarmAndCharmClient {

    public static void onInitializeClient() {
        // 26.2: cutout render layers moved to blockstate JSON
        // ("render_type": "minecraft:cutout"); no runtime registration API.
        // 26.2: RenderTypeRegistry removed from Architectury; cutout is data-driven.

        ParticleProviderRegistry.register(ParticleTypeRegistry.SOUP_BUBBLE.get(), SoupBubbleParticle.Provider::new);
        ParticleProviderRegistry.register(ParticleTypeRegistry.SOUP_STEAM.get(), SoupSteamParticle.Provider::new);
        ParticleProviderRegistry.register(ParticleTypeRegistry.SOUP_COOKING_BUBBLE.get(), SoupCookingBubbleParticle.Provider::new);

        // 26.2: BlockTintSource takes (state) only; world/pos variant renamed
        // colorInWorld. Water tint preserved via BiomeColors average.
        BlockColorRegistry.register((BlockTintsFactory) (state, world, pos, tints) -> {
            if (world == null || pos == null) {
                tints.add(-1);
                return;
            }
            tints.add(BiomeColors.getAverageWaterColor(world, pos));
        }, WATER_TROUGH.get(), TIMBER_WELL.get());

        ClientStorageTypes.init();
        ClientEventHandler.init();
        registerStorageTypeRenderers();
        registerBlockEntityRenderer();
        MenuScreenRegistry.registerScreenFactory(ScreenhandlerTypeRegistry.COOKING_POT_SCREEN_HANDLER.get(), CookingPotGui::new);
        MenuScreenRegistry.registerScreenFactory(ScreenhandlerTypeRegistry.STOVE_SCREEN_HANDLER.get(), StoveGui::new);
        MenuScreenRegistry.registerScreenFactory(ScreenhandlerTypeRegistry.ROASTER_SCREEN_HANDLER.get(), RoasterGui::new);
    }

    public static void registerEntityRenderers() {
        EntityRendererRegistry.register(EntityTypeRegistry.ROTTEN_TOMATO, ThrownItemRenderer::new);
        EntityRendererRegistry.register(EntityTypeRegistry.SUPPLY_CART, SupplyCartRenderer::new);
        EntityRendererRegistry.register(EntityTypeRegistry.PLOW, PlowCartRenderer::new);
        EntityRendererRegistry.register(EntityTypeRegistry.CHAIR, ChairRenderer::new);
    }


    public static void preInitClient() {
        registerEntityRenderers();
        registerEntityModelLayer();
    }

    public static void registerEntityModelLayer() {
        EntityModelLayerRegistry.register(WaterSprinklerModel.LAYER_LOCATION, WaterSprinklerModel::getTexturedModelData);
        EntityModelLayerRegistry.register(CraftingBowlModel.LAYER_LOCATION, CraftingBowlModel::getTexturedModelData);
        EntityModelLayerRegistry.register(MincerModel.LAYER_LOCATION, MincerModel::getTexturedModelData);
        EntityModelLayerRegistry.register(ScarecrowModel.LAYER_LOCATION, ScarecrowModel::getTexturedModelData);
        EntityModelLayerRegistry.register(SupplyCartModel.LAYER_LOCATION, SupplyCartModel::createBodyLayer);
        EntityModelLayerRegistry.register(PlowCartModel.LAYER_LOCATION, PlowCartModel::createBodyLayer);
        EntityModelLayerRegistry.register(DungareesLeggingsModel.LAYER_LOCATION, DungareesLeggingsModel::createBodyLayer);
    }

    public static void registerStorageTypeRenderers() {
        StorageBlockEntityRenderer.registerStorageType(StorageTypeRegistry.TOOL_RACK, new ToolRackRenderer());
        StorageBlockEntityRenderer.registerStorageType(StorageTypeRegistry.WINDOW_SILL, new WindowSillRenderer());
        StorageBlockEntityRenderer.registerStorageType(StorageTypeRegistry.CHICKEN_NEST, new ChickenNestRenderer());
    }

    public static void registerBlockEntityRenderer() {
        BlockEntityRendererRegistry.register(EntityTypeRegistry.ROPE_KNOT_BLOCK_ENTITY.get(), RopeKnotRenderer::new);
        BlockEntityRendererRegistry.register(EntityTypeRegistry.STOVE_BLOCK_ENTITY.get(), StoveBlockRenderer::new);
        BlockEntityRendererRegistry.register(EntityTypeRegistry.SCARECROW_BLOCK_ENTITY.get(), ScarecrowRenderer::new);
        BlockEntityRendererRegistry.register(EntityTypeRegistry.MINCER_BLOCK_ENTITY.get(), MincerRenderer::new);
        BlockEntityRendererRegistry.register(EntityTypeRegistry.CRAFTING_BOWL_BLOCK_ENTITY.get(), CraftingBowlRenderer::new);
        BlockEntityRendererRegistry.register(EntityTypeRegistry.SPRINKLER_BLOCK_ENTITY.get(), WaterSprinklerRenderer::new);
        BlockEntityRendererRegistry.register(EntityTypeRegistry.PET_BOWL_BLOCK_ENTITY.get(), PetBowlBlockRenderer::new);
        BlockEntityRendererRegistry.register(EntityTypeRegistry.STORAGE_ENTITY.get(), StorageBlockEntityRenderer::new);
    }

    public static void openPetBowlScreen(PetBowlBlockEntity entity) {
        Minecraft.getInstance().setScreenAndShow(new PetBowlEditGui(entity));
    }

}
