package straywave.minecraft.immersivesnow.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import straywave.minecraft.immersivesnow.Command;
import straywave.minecraft.immersivesnow.ImmersiveSnow;
import straywave.minecraft.immersivesnow.ImmersiveSnowEvents;

public class Entrypoint implements ModInitializer {
   public void onInitialize() {
      ImmersiveSnow.init();
      ServerLevelEvents.LOAD.register((_server, level) -> ImmersiveSnowEvents.onLevelLoad(level));
      ServerLevelEvents.UNLOAD.register((_server, level) -> ImmersiveSnowEvents.onLevelUnload(level));
      ServerChunkEvents.CHUNK_LOAD.register(ImmersiveSnowEvents::onChunkLoad);
      ServerTickEvents.START_LEVEL_TICK.register(ImmersiveSnowEvents::onWorldTick);

      CommandRegistrationCallback.EVENT.register((dispatcher, _1, _2) -> Command.register(dispatcher));
   }
}
