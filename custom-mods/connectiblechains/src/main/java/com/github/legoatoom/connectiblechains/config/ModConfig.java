package com.github.legoatoom.connectiblechains.config;

import com.github.legoatoom.connectiblechains.networking.packet.ConfigSyncPayload;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry.BoundedDiscrete;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.Excluded;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.Tooltip;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

@Config(
   name = "connectiblechains"
)
public class ModConfig implements ConfigData {
   @Excluded
   private static final transient boolean IS_DEBUG_ENV = FabricLoader.getInstance().isDevelopmentEnvironment();
   @Tooltip(
      count = 3
   )
   private float chainHangAmount = 8.0F;
   @BoundedDiscrete(
      max = 32L
   )
   @Tooltip(
      count = 2
   )
   private int maxChainRange = 16;
   @BoundedDiscrete(
      min = 1L,
      max = 8L
   )
   @Tooltip
   private int quality = 4;
   @Tooltip
   private boolean showToolTip = true;

   public float getChainHangAmount() {
      return this.chainHangAmount;
   }

   public void setChainHangAmount(float chainHangAmount) {
      this.chainHangAmount = chainHangAmount;
   }

   public int getMaxChainRange() {
      return this.maxChainRange;
   }

   public void setMaxChainRange(int maxChainRange) {
      this.maxChainRange = maxChainRange;
   }

   public int getQuality() {
      return this.quality;
   }

   public void setQuality(int quality) {
      this.quality = quality;
   }

   public boolean doDebugDraw() {
      return IS_DEBUG_ENV && Minecraft.getInstance().debugEntries.isOverlayVisible();
   }

   public void syncToClients(MinecraftServer server) {
      for (ServerPlayer player : PlayerLookup.all(server)) {
         this.syncToClient(player);
      }
   }

   public void syncToClient(ServerPlayer player) {
      ServerPlayNetworking.send(player, new ConfigSyncPayload(this.chainHangAmount, this.maxChainRange));
   }

   public ModConfig copyFrom(ModConfig config) {
      this.chainHangAmount = config.chainHangAmount;
      this.maxChainRange = config.maxChainRange;
      this.quality = config.quality;
      this.showToolTip = config.showToolTip;
      return this;
   }

   public boolean doShowToolTip() {
      return this.showToolTip;
   }
}
