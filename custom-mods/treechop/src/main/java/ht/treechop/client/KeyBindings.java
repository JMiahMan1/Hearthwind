package ht.treechop.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;
import com.mojang.blaze3d.platform.InputConstants.Type;
import ht.treechop.client.gui.screen.ClientSettingsScreen;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class KeyBindings {
   public static final net.minecraft.client.KeyMapping.Category CATEGORY = net.minecraft.client.KeyMapping.Category.register(net.minecraft.resources.Identifier.fromNamespaceAndPath("treechop", "treechop"));
   public static final List<KeyBindings.ActionableKeyBinding> allKeyBindings = new LinkedList<>();

   public static void registerKeyMappings(Consumer<net.minecraft.client.KeyMapping> register) {
      registerKeyBinding("toggle_chopping", InputConstants.UNKNOWN, Client::toggleChopping, register);
      registerKeyBinding("cycle_sneak_behavior", InputConstants.UNKNOWN, Client::cycleSneakBehavior, register);
      registerKeyBinding("open_settings_overlay", getKey(78), Client::toggleSettingsOverlay, register);
   }

   private static KeyBindings.ActionableKeyBinding registerKeyBinding(
      String name, Key defaultKey, Runnable callback, Consumer<net.minecraft.client.KeyMapping> register
   ) {
      KeyBindings.ActionableKeyBinding keyBinding = new KeyBindings.ActionableKeyBinding(String.format("%s.key.%s", "treechop", name), defaultKey, callback);
      register.accept(keyBinding);
      allKeyBindings.add(keyBinding);
      return keyBinding;
   }

   static Key getKey(int key) {
      return InputConstants.Type.KEYSYM.getOrCreate(key);
   }

   public static class ActionableKeyBinding extends KeyMapping {
      private final Runnable callback;

      public ActionableKeyBinding(String resourceName, Key inputByCode, Runnable callback) {
         super(resourceName, Type.KEYSYM, inputByCode.getValue(), CATEGORY);
         this.callback = () -> {
            Screen screen = Minecraft.getInstance().gui.screen();
            if (screen == null || screen instanceof ClientSettingsScreen) {
               callback.run();
            }
         };
      }

      public void onPress() {
         this.callback.run();
      }
   }
}
