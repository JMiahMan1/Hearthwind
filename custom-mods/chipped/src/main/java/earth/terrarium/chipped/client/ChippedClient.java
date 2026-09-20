package earth.terrarium.chipped.client;

import com.teamresourceful.resourcefullib.common.registry.ResourcefulRegistry;
import earth.terrarium.chipped.client.fabric.ChippedClientImpl;
import earth.terrarium.chipped.client.screens.ChippedBlockPreviewRenderer;
import earth.terrarium.chipped.common.registry.ModBlocks;
import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public class ChippedClient {

    public static void init() {
        registerRenderTypes();
        PictureInPictureRendererRegistry.register(context -> new ChippedBlockPreviewRenderer());
    }

    private static void registerRenderTypes() {
        // 26.2: render type API removed; no-op
    }

    private static void createSetRenderType(ResourcefulRegistry<Block> registry, RenderType type) {
        registry.getEntries().forEach(b -> ChippedClientImpl.registerBlockRenderType(type, b));
    }

    public static void registerBlockRenderType(RenderType type, Supplier<Block> block) {
        ChippedClientImpl.registerBlockRenderType(type, block);
    }
}
