package io.github.phantomloader.library.config;

import io.github.phantomloader.library.ModEntryPoint;
import java.util.NoSuchElementException;
import java.util.ServiceLoader;
import java.util.function.Supplier;

public interface ConfigBuilder {
   static ConfigBuilder instantiate() {
      return ServiceLoader.load(ConfigBuilder.class)
         .findFirst()
         .orElseThrow(
            () -> new NoSuchElementException(
               "No config builder has been defined in META-INF/services. Make sure you are using the correct version of the library mod for your mod loader."
            )
         );
   }

   void beginCategory(String var1);

   Supplier<Boolean> define(String var1, boolean var2, String... var3);

   Supplier<Integer> define(String var1, int var2, String... var3);

   default Supplier<Integer> define(String key, int defaultValue, int min, int max, String... comment) {
      return this.define(key, defaultValue, comment);
   }

   Supplier<Double> define(String var1, double var2, String... var4);

   default Supplier<Double> define(String key, double defaultValue, double min, double max, String... comment) {
      return this.define(key, defaultValue, comment);
   }

   Supplier<Long> define(String var1, long var2, String... var4);

   default Supplier<Long> define(String key, long defaultValue, long min, long max, String... comment) {
      return this.define(key, defaultValue, comment);
   }

   Supplier<String> define(String var1, String var2, String... var3);

   void endCategory();

   void register(String var1, ModEntryPoint.Side var2);
}
