package ht.treechop.client;

import ht.treechop.client.model.ChoppedLogModelLoadingPlugin;
import ht.treechop.common.network.ServerConfirmSettingsPacket;
import ht.treechop.common.network.ServerPermissionsPacket;
import ht.treechop.common.network.ServerUpdateChopsPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.Join;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

@Environment(EnvType.CLIENT)
public class FabricClient extends Client implements ClientModInitializer {
   public void onInitializeClient() {
      ModelLoadingPlugin.register(new ChoppedLogModelLoadingPlugin());
      ClientPlayConnectionEvents.JOIN.register((Join)(handler, sender, client) -> syncOnJoin());
      this.registerPackets();
      this.registerKeybindings();
   }

   private void registerKeybindings() {
      KeyBindings.registerKeyMappings(mapping -> {});
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)client -> {
         for (KeyBindings.ActionableKeyBinding keyBinding : KeyBindings.allKeyBindings) {
            if (keyBinding.consumeClick()) {
               keyBinding.onPress();
               return;
            }
         }
      });
   }

   private void registerPackets() {
      ClientPlayNetworking.registerGlobalReceiver(ServerConfirmSettingsPacket.TYPE, (payload, context) -> payload.handle());
      ClientPlayNetworking.registerGlobalReceiver(ServerPermissionsPacket.TYPE, (payload, context) -> payload.handle());
      ClientPlayNetworking.registerGlobalReceiver(ServerUpdateChopsPacket.TYPE, (payload, context) -> payload.handle());
   }

   @Override
   public void sendToServer(CustomPacketPayload payload) {
      ClientPlayNetworking.send(payload);
   }

   static {
      Client.instance = new FabricClient();
   }
}
