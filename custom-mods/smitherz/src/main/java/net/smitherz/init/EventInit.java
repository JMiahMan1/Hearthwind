package net.smitherz.init;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents.Modify;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.BinomialDistributionGenerator;
import net.smitherz.data.GemLoader;

public class EventInit {
   public static void init() {
      Path directoryPath = Paths.get(FabricLoader.getInstance().getGameDir().toString(), "global_packs", "required_datapacks");
      if (Files.exists(directoryPath) && Files.isDirectory(directoryPath)) {
         try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directoryPath)) {
            for (Path filePath : directoryStream) {
               if (filePath.toString().endsWith(".zip")) {
                  try (ZipFile zipFile = new ZipFile(filePath.toString())) {
                     Enumeration<? extends ZipEntry> entries = zipFile.entries();

                     while (entries.hasMoreElements()) {
                        ZipEntry entry = entries.nextElement();
                        if (!entry.isDirectory() && entry.getName().startsWith("data/smitherz/gems")) {
                           File fileInstance = extractFileFromZip(zipFile, entry);
                           GemLoader.readGemFile(fileInstance);
                           fileInstance.delete();
                        }
                     }
                  } catch (IOException var11) {
                     var11.printStackTrace();
                  }
               } else {
                  Path gemDirectoryPath = Paths.get(filePath.toString(), "data", "smitherz", "gems");
                  if (Files.exists(gemDirectoryPath)) {
                     Files.list(gemDirectoryPath).forEach(path -> GemLoader.readGemFile(path.toFile()));
                  }
               }
            }
         } catch (IOException var13) {
            var13.printStackTrace();
         }
      }

      LootTableEvents.MODIFY
         .register(
            (Modify)(key, tableBuilder, source, registries) -> {
               if (BuiltInLootTables.END_CITY_TREASURE.equals(key)) {
                  LootPool pool = LootPool.lootPool()
                     .add(LootItem.lootTableItem(ItemInit.EXTRACTION_HAMMER_4).build())
                     .setRolls(BinomialDistributionGenerator.binomial(1, 0.005F))
                     .add(LootItem.lootTableItem(ItemInit.SMITHER_HAMMER_4).build())
                     .setRolls(BinomialDistributionGenerator.binomial(1, 0.005F))
                     .build();
                  tableBuilder.pool(pool);
               } else if (BuiltInLootTables.BURIED_TREASURE.equals(key) || BuiltInLootTables.SHIPWRECK_TREASURE.equals(key)) {
                  LootPool pool = LootPool.lootPool()
                     .add(LootItem.lootTableItem(ItemInit.EXTRACTION_HAMMER_1).build())
                     .setRolls(BinomialDistributionGenerator.binomial(1, 0.1F))
                     .add(LootItem.lootTableItem(ItemInit.EXTRACTION_HAMMER_2).build())
                     .setRolls(BinomialDistributionGenerator.binomial(1, 0.05F))
                     .add(LootItem.lootTableItem(ItemInit.EXTRACTION_HAMMER_3).build())
                     .setRolls(BinomialDistributionGenerator.binomial(1, 0.01F))
                     .build();
                  tableBuilder.pool(pool);
               } else if (BuiltInLootTables.BASTION_TREASURE.equals(key)) {
                  LootPool pool = LootPool.lootPool()
                     .add(LootItem.lootTableItem(ItemInit.SMITHER_HAMMER_1).build())
                     .setRolls(BinomialDistributionGenerator.binomial(1, 0.1F))
                     .add(LootItem.lootTableItem(ItemInit.SMITHER_HAMMER_2).build())
                     .setRolls(BinomialDistributionGenerator.binomial(1, 0.05F))
                     .add(LootItem.lootTableItem(ItemInit.SMITHER_HAMMER_3).build())
                     .setRolls(BinomialDistributionGenerator.binomial(1, 0.01F))
                     .build();
                  tableBuilder.pool(pool);
               }
            }
         );
   }

   private static File extractFileFromZip(ZipFile zipFile, ZipEntry zipEntry) throws IOException {
      File tempFile = File.createTempFile("temp_gem", ".json");

      try (
         InputStream inputStream = zipFile.getInputStream(zipEntry);
         FileOutputStream fos = new FileOutputStream(tempFile);
      ) {
         byte[] buffer = new byte[1024];

         int bytesRead;
         while ((bytesRead = inputStream.read(buffer)) != -1) {
            fos.write(buffer, 0, bytesRead);
         }
      }

      return tempFile;
   }
}
