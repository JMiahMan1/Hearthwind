package ht.treechop;

import ht.treechop.api.ITreeChopAPIProvider;

import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import fuzs.forgeconfigapiport.fabric.api.v5.ModConfigEvents;
import ht.treechop.common.FabricCommon;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.loot.CountBlockChopsLootItemCondition;
import ht.treechop.common.loot.TreeFelledLootItemCondition;
import ht.treechop.common.network.ClientRequestSettingsPacket;
import ht.treechop.common.network.ServerConfirmSettingsPacket;
import ht.treechop.common.network.ServerPermissionsPacket;
import ht.treechop.common.network.ServerUpdateChopsPacket;
import ht.treechop.common.platform.FabricPlatform;
import ht.treechop.common.registry.FabricModBlocks;
import ht.treechop.compat.TreeChopFabricAPITest;
import ht.treechop.server.FabricServer;
import ht.treechop.server.commands.ServerCommands;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents.TagsLoaded;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.config.ModConfig.Type;

public class TreeChopFabric extends TreeChop implements ModInitializer {
   public void onInitialize() {
      platform = new FabricPlatform();
      api = new TreeChopFabricAPI("treechop");
      PlayerBlockBreakEvents.BEFORE.register(FabricCommon::onBreakEvent);
      CommonLifecycleEvents.TAGS_LOADED.register((TagsLoaded)(registries, client) -> ConfigHandler.updateTags());
      ModConfigEvents.reloading("treechop").register(TreeChopFabric::onReload);
      ConfigRegistry.INSTANCE.register("treechop", Type.COMMON, ConfigHandler.COMMON_SPEC);
      ConfigRegistry.INSTANCE.register("treechop", Type.CLIENT, ConfigHandler.CLIENT_SPEC);
      Registry.register(BuiltInRegistries.BLOCK, resource("chopped_log"), FabricModBlocks.CHOPPED_LOG);
      Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, resource("chopped_log_entity"), FabricModBlocks.CHOPPED_LOG_ENTITY);
      PayloadTypeRegistry<net.minecraft.network.RegistryFriendlyByteBuf> r = PayloadTypeRegistry.clientboundPlay();
      r.register(ServerConfirmSettingsPacket.TYPE, ServerConfirmSettingsPacket.STREAM_CODEC);
      r.register(ServerPermissionsPacket.TYPE, ServerPermissionsPacket.STREAM_CODEC);
      r.register(ServerUpdateChopsPacket.TYPE, ServerUpdateChopsPacket.STREAM_CODEC);
      r = PayloadTypeRegistry.serverboundPlay();
      r.register(ClientRequestSettingsPacket.TYPE, ClientRequestSettingsPacket.STREAM_CODEC);
      FabricServer.initialize();
      FabricLoader.getInstance().getObjectShare().put("treechop:api_provider", (ht.treechop.api.ITreeChopAPIProvider) modId -> new TreeChopFabricAPI(modId));
      TreeChopFabricAPITest.init();
      Registry.register(BuiltInRegistries.SOUND_EVENT, CHOP_WOOD, CHOP_WOOD_EVENT.get());
      Registry.register(BuiltInRegistries.LOOT_CONDITION_TYPE, CountBlockChopsLootItemCondition.ID, CountBlockChopsLootItemCondition.CODEC);
      Registry.register(BuiltInRegistries.LOOT_CONDITION_TYPE, TreeFelledLootItemCondition.ID, TreeFelledLootItemCondition.CODEC);
      CommandRegistrationCallback.EVENT.register((CommandRegistrationCallback)(dispatcher, registryAccess, environment) -> ServerCommands.register(dispatcher));
   }

   private static void onReload(ModConfig config) {
      if (config.getModId().equals("treechop")) {
         ConfigHandler.onReload();
      }
   }
}
