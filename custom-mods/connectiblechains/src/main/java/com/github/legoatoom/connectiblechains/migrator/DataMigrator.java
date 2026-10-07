package com.github.legoatoom.connectiblechains.migrator;

import com.github.legoatoom.connectiblechains.ConnectibleChains;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;
import net.minecraft.util.ProblemReporter.ScopedCollector;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public abstract class DataMigrator<Context extends net.minecraft.world.entity.Entity> {
   private static final String DATA_VERSION_KEY = "connectiblechains_DataVersion";
   private final SortedMap<Integer, Migration<Context>> migrations = new TreeMap<>();

   public DataMigrator() {
      this.registerMigrations();
   }

   public abstract void registerMigrations();

   public net.minecraft.world.level.storage.ValueInput migrate(net.minecraft.world.level.storage.ValueInput readView, Context context) {
      int version = readView.getIntOr("connectiblechains_DataVersion", 0);
      ValueInput currentReadView = readView;
      ScopedCollector logging = new ScopedCollector(context.problemPath(), ConnectibleChains.LOGGER);

      try {
         for (Entry<Integer, Migration<Context>> entry : this.migrations.entrySet()) {
            Integer migrationVersion = entry.getKey();
            Migration<Context> migration = entry.getValue();
            if (version >= migrationVersion) {
               break;
            }

            TagValueOutput newWriteView = TagValueOutput.createWithoutContext(logging);

            try {
               migration.migrate(currentReadView, newWriteView, context);
            } catch (Exception var13) {
               ConnectibleChains.LOGGER.error("Error during fixing {} for '{}':", new Object[]{context, version, var13});
            }

            currentReadView = TagValueInput.create(logging, context.registryAccess(), newWriteView.buildResult());
         }
      } catch (Throwable var14) {
         try {
            logging.close();
         } catch (Throwable var12) {
            var14.addSuppressed(var12);
         }

         throw var14;
      }

      logging.close();
      return currentReadView;
   }

   public void addVersionTag(ValueOutput writeView) {
      writeView.putInt("connectiblechains_DataVersion", this.getLatestVersion());
   }

   private int getLatestVersion() {
      return this.migrations.lastKey();
   }

   protected void registerMigration(int version, Migration<Context> migration) {
      if (!this.migrations.containsKey(version)) {
         this.migrations.put(version, migration);
      } else {
         ConnectibleChains.LOGGER.error("Version {} already registered!", version);
      }
   }
}
