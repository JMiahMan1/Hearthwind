package net.nameplate;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.nameplate.config.NameplateConfig;
import net.nameplate.network.NameplateServerPacket;

public class NameplateMain implements ModInitializer {
   public static NameplateConfig CONFIG = new NameplateConfig();
   public static final boolean isRpgDifficultyLoaded = FabricLoader.getInstance().isModLoaded("rpgdifficulty");

   public void onInitialize() {
      AutoConfig.register(NameplateConfig.class, GsonConfigSerializer::new);
      CONFIG = (NameplateConfig)AutoConfig.getConfigHolder(NameplateConfig.class).getConfig();
      NameplateServerPacket.init();
   }
}
