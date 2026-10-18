package com.chailotl.particular.sushi_bar.owo.config;

import io.wispforest.owo.config.ConfigWrapper;
import io.wispforest.owo.config.ui.ConfigScreen;
import io.wispforest.owo.config.ui.ConfigScreenProviders;
import io.wispforest.owo.config.ui.OptionComponentFactory.Result;
import io.wispforest.owo.util.ReflectionUtils;
import java.lang.reflect.Field;
import java.util.List;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public class SushiConfigScreen extends ConfigScreen {
   public SushiConfigScreen(
      net.minecraft.resources.Identifier modelId, ConfigWrapper<?> config, @Nullable net.minecraft.client.gui.screens.Screen parent
   ) {
      super(modelId, config, parent);
      this.extraFactories.put(option -> isIdentifierList(option.backingField().field()), (model, option) -> {
         IdentifierListOptionContainer layout = new IdentifierListOptionContainer(option);
         return new Result(layout, layout);
      });
   }

   public SushiConfigScreen(ConfigWrapper<?> config, @Nullable net.minecraft.client.gui.screens.Screen parent) {
      this(DEFAULT_MODEL_ID, config, parent);
   }

   public static SushiConfigScreen createWithCustomModel(
      net.minecraft.resources.Identifier modelId, ConfigWrapper<?> config, @Nullable net.minecraft.client.gui.screens.Screen parent
   ) {
      return new SushiConfigScreen(modelId, config, parent);
   }

   public static void registerConfig(String modId, ConfigWrapper<?> config) {
      ConfigScreenProviders.register(modId, screen -> new SushiConfigScreen(config, screen));
   }

   private static boolean isIdentifierList(Field field) {
      return field.getType() != List.class ? false : Identifier.class == ReflectionUtils.getTypeArgument(field.getGenericType(), 0);
   }
}
