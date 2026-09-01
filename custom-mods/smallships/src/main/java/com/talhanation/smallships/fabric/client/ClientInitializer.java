package com.talhanation.smallships.fabric.client;

import com.talhanation.smallships.client.model.*;
import com.talhanation.smallships.client.option.KeyEvent;
import com.talhanation.smallships.client.option.ModGameOptions;
import com.talhanation.smallships.client.renderer.entity.*;
import com.talhanation.smallships.network.fabric.ModPacketsImpl;
import com.talhanation.smallships.world.entity.ModEntityTypes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

@Environment(EnvType.CLIENT)
public class ClientInitializer implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        com.talhanation.smallships.client.ClientInitializer.init();

        initRendererRegisterRenderers();

        initRendererRegisterLayerDefinitions();

        initRegisterKeyMappings();

        initRegisterTickEvents();

        initRegisterPacketReceivers();
    }

    private void initRendererRegisterRenderers() {
        EntityRendererRegistry.register(ModEntityTypes.CANNON_BALL, CannonBallRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.GROUND_CANNON, GroundCannonRenderer::new);

        EntityRendererRegistry.register(ModEntityTypes.COG, CogRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.BRIGG, BriggRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.GALLEY, GalleyRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.DRAKKAR, DrakkarRenderer::new);
    }

    private void initRendererRegisterLayerDefinitions() {
        ModelLayerRegistry.registerModelLayer(CannonBallModel.LAYER_LOCATION, CannonBallModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(CannonModel.LAYER_LOCATION, CannonModel::createBodyLayer);

        ModelLayerRegistry.registerModelLayer(CogModel.LAYER_LOCATION, CogModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(BriggModel.LAYER_LOCATION, BriggModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(GalleyModel.LAYER_LOCATION, GalleyModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(DrakkarModel.LAYER_LOCATION, DrakkarModel::createBodyLayer);
    }

    private void initRegisterKeyMappings() {
        KeyMappingHelper.registerKeyMapping(ModGameOptions.SAIL_KEY);
        KeyMappingHelper.registerKeyMapping(ModGameOptions.ENTER_CANNON_BARREL_KEY);
    }

    private void initRegisterTickEvents() {
        ClientTickEvents.END_CLIENT_TICK.register(KeyEvent::onKeyInput);
    }

    private void initRegisterPacketReceivers() {
        ModPacketsImpl.registerClientReceivers();
    }
}
