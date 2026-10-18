package net.medievalweapons;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.medievalweapons.init.CompatInit;
import net.medievalweapons.init.ConfigInit;
import net.medievalweapons.init.EffectInit;
import net.medievalweapons.init.EntityInit;
import net.medievalweapons.init.ItemInit;
import net.medievalweapons.init.ParticleInit;
import net.medievalweapons.init.SoundInit;
import net.medievalweapons.init.TagInit;
import net.medievalweapons.network.MedievalServerPacket;
import net.medievalweapons.util.EnchantmentCache;
import net.minecraft.resources.Identifier;

public class MedievalMain implements ModInitializer {
   public void onInitialize() {
      ConfigInit.init();
      CompatInit.init();
      ItemInit.init();
      EffectInit.init();
      EntityInit.init();
      ParticleInit.init();
      MedievalServerPacket.init();
      SoundInit.init();
      TagInit.init();
      ServerLifecycleEvents.SERVER_STARTED.register(EnchantmentCache::onServerStarted);
   }

   public static Identifier identifierOf(String path) {
      return Identifier.fromNamespaceAndPath("medievalweapons", path);
   }
}
