package com.github.legoatoom.connectiblechains.client;

import com.github.legoatoom.connectiblechains.ConnectibleChains;
import com.github.legoatoom.connectiblechains.client.render.entity.ChainCollisionEntityRenderer;
import com.github.legoatoom.connectiblechains.client.render.entity.ChainKnotEntityRenderer;
import com.github.legoatoom.connectiblechains.client.render.entity.catenary.CatenaryRenderer;
import com.github.legoatoom.connectiblechains.client.render.entity.catenary.CrossCatenaryRenderer;
import com.github.legoatoom.connectiblechains.client.render.entity.catenary.PlussCatenaryRenderer;
import com.github.legoatoom.connectiblechains.client.render.entity.catenary.SquareCatenaryRenderer;
import com.github.legoatoom.connectiblechains.client.render.entity.model.ChainKnotEntityModel;
import com.github.legoatoom.connectiblechains.client.render.entity.texture.ChainModelReloader;
import com.github.legoatoom.connectiblechains.config.ModConfig;
import com.github.legoatoom.connectiblechains.entity.ModEntityTypes;
import com.github.legoatoom.connectiblechains.item.ChainItemCallbacks;
import com.github.legoatoom.connectiblechains.networking.packet.ChainAttachS2CPacket;
import com.github.legoatoom.connectiblechains.networking.packet.ConfigSyncPayload;
import com.github.legoatoom.connectiblechains.util.Helper;
import java.util.Optional;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.Init;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.InteractionResult;

@Environment(EnvType.CLIENT)
public class ClientInitializer implements ClientModInitializer {
   public static final ModelLayerLocation CHAIN_KNOT = new ModelLayerLocation(Helper.identifier("chain_knot"), "main");
   private static ClientInitializer instance;
   private final ChainModelReloader chainModelReloader = new ChainModelReloader();
   private ChainKnotEntityRenderer chainKnotEntityRenderer;

   public void onInitializeClient() {
      instance = this;
      this.initRenderers();
      this.registerNetworkEventHandlers();
      this.registerClientEventHandlers();
      registerConfigSync();
      ItemTooltipCallback.EVENT.register(ChainItemCallbacks::infoToolTip);
      this.registerCatenaryRenders();
   }

   private void registerCatenaryRenders() {
      CatenaryRenderer.addRenderer(Helper.identifier("cross"), CrossCatenaryRenderer::new);
      CatenaryRenderer.addRenderer(Helper.identifier("square"), SquareCatenaryRenderer::new);
      CatenaryRenderer.addRenderer(Helper.identifier("plus"), PlussCatenaryRenderer::new);
   }

   private static void registerConfigSync() {
      ConfigHolder<ModConfig> configHolder = AutoConfig.getConfigHolder(ModConfig.class);
      configHolder.registerSaveListener((holder, modConfig) -> {
         ClientInitializer clientInitializer = getInstance();
         if (clientInitializer != null) {
            clientInitializer.getChainKnotEntityRenderer().ifPresent(renderer -> renderer.getChainRenderer().purge());
         }

         MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
         if (server != null) {
            ConnectibleChains.LOGGER.info("Syncing config to clients");
            ConnectibleChains.fileConfig.syncToClients(server);
            ConnectibleChains.runtimeConfig.copyFrom(ConnectibleChains.fileConfig);
         }

         return InteractionResult.PASS;
      });
   }

   private void initRenderers() {
      ConnectibleChains.LOGGER.info("Initializing Renderers.");
      EntityRenderers.register(ModEntityTypes.CHAIN_KNOT, ctx -> {
         this.chainKnotEntityRenderer = new ChainKnotEntityRenderer(ctx);
         return this.chainKnotEntityRenderer;
      });
      EntityRenderers.register(ModEntityTypes.CHAIN_COLLISION, ChainCollisionEntityRenderer::new);
      ModelLayerRegistry.registerModelLayer(CHAIN_KNOT, ChainKnotEntityModel::getTexturedModelData);
   }

   private void registerNetworkEventHandlers() {
      ConnectibleChains.LOGGER.info("Initializing Network even handlers.");
      ClientPlayNetworking.registerGlobalReceiver(ChainAttachS2CPacket.PAYLOAD_ID, ChainAttachS2CPacket::apply);
      ClientPlayConnectionEvents.INIT.register((Init)(handler, client) -> {
         ConnectibleChains.runtimeConfig.copyFrom(ConnectibleChains.fileConfig);
         this.getChainKnotEntityRenderer().ifPresent(r -> r.getChainRenderer().purge());
      });
      ClientPlayNetworking.registerGlobalReceiver(ConfigSyncPayload.PAYLOAD_ID, ConfigSyncPayload::apply);
   }

   private void registerClientEventHandlers() {
      ConnectibleChains.LOGGER.info("Registering texture handlers..");
      ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(Helper.identifier("chain_model_reloader"), this.chainModelReloader);
   }

   public static ClientInitializer getInstance() {
      return instance;
   }

   public Optional<ChainKnotEntityRenderer> getChainKnotEntityRenderer() {
      return Optional.ofNullable(this.chainKnotEntityRenderer);
   }

   public ChainModelReloader getChainTextureManager() {
      return this.chainModelReloader;
   }
}
