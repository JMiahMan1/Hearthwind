package net.satisfy.candlelight.client;

import dev.architectury.registry.client.gui.MenuScreenRegistry;
import dev.architectury.registry.client.level.entity.EntityModelLayerRegistry;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.satisfy.candlelight.client.gui.LetterGui;
import net.satisfy.candlelight.client.gui.WallDecorationEditGui;
import net.satisfy.candlelight.client.model.*;
import net.satisfy.candlelight.client.renderer.block.*;
import net.satisfy.candlelight.core.block.entity.WallDecorationBlockEntity;
import net.satisfy.candlelight.core.registry.EntityTypeRegistry;
import net.satisfy.candlelight.core.registry.ScreenHandlerTypeRegistry;
import net.satisfy.candlelight.core.registry.StorageTypeRegistry;
import net.satisfy.candlelight.core.util.CandlelightUtil;

import static net.satisfy.candlelight.core.registry.ObjectRegistry.*;

@Environment(EnvType.CLIENT)
public class CandlelightClient {

    public static void initClient() {
        // 26.2: cutout/translucent layers moved to blockstate JSON
        // ("render_type"); no runtime registration API.

        BlockEntityRendererRegistry.register(EntityTypeRegistry.CANDLELIGHT_BANNER_ENTITY.get(), CompletionistBannerRenderer::new);
        BlockEntityRendererRegistry.register(EntityTypeRegistry.STORAGE_BLOCK_ENTITY.get(), StorageBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(EntityTypeRegistry.DINNER_BELL_BLOCK_ENTITY.get(), DinnerBellRenderer::new);
        BlockEntityRendererRegistry.register(EntityTypeRegistry.TYPE_WRITER_BLOCK_ENTITY.get(), TypewriterRenderer::new);
        BlockEntityRendererRegistry.register(EntityTypeRegistry.WALL_DECORATION.get(), WallDecorationBlockRenderer::new);

        MenuScreenRegistry.registerScreenFactory(ScreenHandlerTypeRegistry.LETTER_SCREEN_HANDLER.get(), LetterGui::new);

        registerStorageType();

        CandlelightUtil.registerColorArmor(DRESS.get(), 16744576);
        CandlelightUtil.registerColorArmor(TROUSERS_AND_VEST.get(), 0x333399);
    }

    public static void openWallDecorationScreen(WallDecorationBlockEntity entity) {
        Minecraft.getInstance().setScreenAndShow(new WallDecorationEditGui(entity));
    }

    public static void registerStorageType(Identifier location, StorageTypeRenderer renderer) {
        StorageBlockEntityRenderer.registerStorageType(location, renderer);
    }

    public static void registerStorageType() {
        registerStorageType(StorageTypeRegistry.SHELF, new ShelfRenderer());
        registerStorageType(StorageTypeRegistry.TABLE_SET, new TableSetRenderer());
        registerStorageType(StorageTypeRegistry.JEWELRY_BOX, new JewelryRenderer());
    }

    public static void preInitClient() {
        registerEntityModelLayers();
    }

    public static void registerEntityModelLayers() {
        EntityModelLayerRegistry.register(TypewriterModel.LAYER_LOCATION, TypewriterModel::getTexturedModelData);
        EntityModelLayerRegistry.register(DinnerBellModel.LAYER_LOCATION, DinnerBellModel::getTexturedModelData);
        EntityModelLayerRegistry.register(CompletionistBannerRenderer.LAYER_LOCATION, CompletionistBannerRenderer::createBodyLayer);
        EntityModelLayerRegistry.register(FlowerCrownModel.LAYER_LOCATION, FlowerCrownModel::createBodyLayer);
        EntityModelLayerRegistry.register(TieModel.LAYER_LOCATION, TieModel::createBodyLayer);
        EntityModelLayerRegistry.register(DressChestplateModel.LAYER_LOCATION, DressChestplateModel::createBodyLayer);
        EntityModelLayerRegistry.register(CookingHatModel.LAYER_LOCATION, CookingHatModel::createBodyLayer);
        EntityModelLayerRegistry.register(CookingChestplateModel.LAYER_LOCATION, CookingChestplateModel::createBodyLayer);
        EntityModelLayerRegistry.register(CookingLeggingsModel.LAYER_LOCATION, CookingLeggingsModel::createBodyLayer);
        EntityModelLayerRegistry.register(CookingBootsModel.LAYER_LOCATION, CookingBootsModel::createBodyLayer);
    }
}