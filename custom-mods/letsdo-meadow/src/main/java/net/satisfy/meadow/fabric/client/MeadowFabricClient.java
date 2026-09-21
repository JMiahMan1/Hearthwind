package net.satisfy.meadow.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.model.object.boat.BoatModel;
import net.satisfy.meadow.client.MeadowClient;
import net.satisfy.meadow.client.gui.handler.WoodcutterGuiHandler;
import net.satisfy.meadow.client.gui.handler.WoodcutterRecipesPayload;
import net.satisfy.meadow.client.renderer.entity.PineBoatRenderer;
import net.satisfy.meadow.core.registry.ObjectRegistry;
import net.satisfy.meadow.fabric.client.renderer.FurBootsRenderer;
import net.satisfy.meadow.fabric.client.renderer.FurChestplateRenderer;
import net.satisfy.meadow.fabric.client.renderer.FurHelmetRenderer;
import net.satisfy.meadow.fabric.client.renderer.FurLeggingsRenderer;

public class MeadowFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MeadowClient.preInitClient();
        MeadowClient.initClient();
        registerBoatModels();

        ArmorRenderer.register(new FurHelmetRenderer(), ObjectRegistry.FUR_HELMET.get());
        ArmorRenderer.register(new FurChestplateRenderer(), ObjectRegistry.FUR_CHESTPLATE.get());
        ArmorRenderer.register(new FurLeggingsRenderer(), ObjectRegistry.FUR_LEGGINGS.get());
        ArmorRenderer.register(new FurBootsRenderer(), ObjectRegistry.FUR_BOOTS.get());

        ClientPlayNetworking.registerGlobalReceiver(WoodcutterRecipesPayload.TYPE, (payload, context) -> context.client().execute(() -> {
            if (context.player().containerMenu instanceof WoodcutterGuiHandler handler
                    && handler.containerId == payload.containerId()) {
                handler.setClientResults(payload.results());
            }
        }));
    }

    private void registerBoatModels() {
        dev.architectury.registry.client.level.entity.EntityModelLayerRegistry.register(PineBoatRenderer.BOAT_LAYER, BoatModel::createBoatModel);
        dev.architectury.registry.client.level.entity.EntityModelLayerRegistry.register(PineBoatRenderer.CHEST_BOAT_LAYER, BoatModel::createChestBoatModel);
    }
}
