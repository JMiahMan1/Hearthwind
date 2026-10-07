package com.github.legoatoom.connectiblechains;

import com.github.legoatoom.connectiblechains.config.ModConfig;
import com.github.legoatoom.connectiblechains.entity.ModEntityTypes;
import com.github.legoatoom.connectiblechains.item.ChainItemCallbacks;
import com.github.legoatoom.connectiblechains.networking.packet.Payloads;
import com.mojang.logging.LogUtils;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.Join;
import org.slf4j.Logger;

public class ConnectibleChains implements ModInitializer {
   public static final String MODID = "connectiblechains";
   public static final Logger LOGGER = LogUtils.getLogger();
   public static ModConfig fileConfig;
   public static ModConfig runtimeConfig;

   public void onInitialize() {
      ModEntityTypes.init();
      Payloads.init();
      AutoConfig.register(ModConfig.class, Toml4jConfigSerializer::new);
      ConfigHolder<ModConfig> configHolder = AutoConfig.getConfigHolder(ModConfig.class);
      fileConfig = (ModConfig)configHolder.getConfig();
      runtimeConfig = new ModConfig().copyFrom(fileConfig);
      UseBlockCallback.EVENT.register(ChainItemCallbacks::chainUseEvent);
      ServerPlayConnectionEvents.JOIN.register((Join)(handler, sender, server) -> fileConfig.syncToClient(handler.getPlayer()));
   }
}
