package net.fleshz;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fleshz.block.render.WoodRackRenderer;
import net.fleshz.init.BlockInit;
import net.fleshz.network.RottenClientPacket;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

@Environment(EnvType.CLIENT)
public class FleshClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // 26.x: chunk section layers are automatic (texture alpha drives CUTOUT);
        // BlockRenderLayerMap was removed from fabric-api.
        BlockEntityRenderers.register(BlockInit.WOOD_RACK_ENTITY, WoodRackRenderer::new);
        RottenClientPacket.init();
    }
}
