package de.keksuccino.loadmyresources.pack;

import de.keksuccino.loadmyresources.LoadMyResources;
import java.io.File;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.flag.FeatureFlags;

public class PackHandler {
   public static final String PACK_NAME = "loadmyresources.hiddenpack";
   public static final int PACK_FORMAT = 8;
   public static final String PACK_DESCRIPTION = "LMR Resources";
   public static final Identifier DUMMY_PACK_META = Identifier.fromNamespaceAndPath("loadmyresources", "dummy.pack.mcmeta");
   public static final PackLocationInfo LOCATION = new PackLocationInfo(
      PACK_NAME, Component.literal(PACK_NAME), PackSource.DEFAULT, Optional.empty()
   );
   public static File resourcesDirectory = new File(LoadMyResources.getGameDirectory(), "resources/");

   public static void init() {
      if (LoadMyResources.config != null) {
         resourcesDirectory = new File(LoadMyResources.getGameDirectory(), LoadMyResources.config.getOrDefault("resource_path", "resources/"));
      } else {
         LoadMyResources.LOGGER.error("[LOAD MY RESOURCES] ERROR: Config not loaded! Can't get resource path! Path set to default 'resources/'!");
      }

      LoadMyResources.LOGGER.info("[LOAD MY RESOURCES] PackHandler initialized! Resources path set to: " + resourcesDirectory.getAbsolutePath());
   }

   public static void prepareResourcesFolder() {
      if (!resourcesDirectory.isDirectory()) {
         resourcesDirectory.mkdirs();
      }
   }

   public static Pack createPack(Pack.ResourcesSupplier resources) {
      Pack.Metadata metadata = new Pack.Metadata(
         Component.literal(PACK_DESCRIPTION), PackCompatibility.COMPATIBLE, FeatureFlags.VANILLA_SET, List.of()
      );
      PackSelectionConfig selection = new PackSelectionConfig(true, Pack.Position.TOP, true);
      return new Pack(LOCATION, resources, metadata, selection);
   }
}
