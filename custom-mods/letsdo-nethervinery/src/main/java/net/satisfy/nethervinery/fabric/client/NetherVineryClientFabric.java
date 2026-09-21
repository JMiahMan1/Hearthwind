package net.satisfy.nethervinery.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.satisfy.nethervinery.client.NetherVineryClient;

public class NetherVineryClientFabric implements ClientModInitializer {
    
    @Override
    public void onInitializeClient() {
        NetherVineryClient.onInitializeClient();
    }
}
