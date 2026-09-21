package net.satisfy.herbalbrews.client;

import dev.architectury.registry.client.gui.MenuScreenRegistry;
import dev.architectury.registry.client.level.entity.EntityModelLayerRegistry;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.satisfy.herbalbrews.client.gui.CauldronGui;
import net.satisfy.herbalbrews.client.gui.TeaKettleGui;
import net.satisfy.herbalbrews.client.model.TopHatModel;
import net.satisfy.herbalbrews.client.model.WitchHatModel;
import net.satisfy.herbalbrews.client.renderer.CompletionistBannerRenderer;
import net.satisfy.herbalbrews.core.registry.EntityTypeRegistry;
import net.satisfy.herbalbrews.core.registry.MenuTypeRegistry;

@Environment(EnvType.CLIENT)
public class HerbalbrewsClient {
    public static void onInitializeClient() {
        // 26.2: cutout/translucent layers moved to blockstate JSON
        // ("render_type"); no runtime registration API.
        MenuScreenRegistry.registerScreenFactory(MenuTypeRegistry.TEA_KETTLE_SCREEN_HANDLER.get(), TeaKettleGui::new);
        MenuScreenRegistry.registerScreenFactory(MenuTypeRegistry.CAULDRON_SCREEN_HANDLER.get(), CauldronGui::new);
        BlockEntityRendererRegistry.register(EntityTypeRegistry.HERBALBREWS_BANNER.get(), CompletionistBannerRenderer::new);
    }

    public static void preInitClient() {
        EntityModelLayerRegistry.register(TopHatModel.LAYER_LOCATION, TopHatModel::createBodyLayer);
        EntityModelLayerRegistry.register(WitchHatModel.LAYER_LOCATION, WitchHatModel::createBodyLayer);
        EntityModelLayerRegistry.register(CompletionistBannerRenderer.LAYER_LOCATION, CompletionistBannerRenderer::createBodyLayer);
    }
}
