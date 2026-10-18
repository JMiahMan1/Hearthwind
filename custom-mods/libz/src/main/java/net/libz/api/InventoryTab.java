package net.libz.api;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class InventoryTab {
   private final Class<?>[] screenClasses;
   private final Component title;
   @Nullable
   private final Identifier texture;
   private final int preferedPos;

   public InventoryTab(Component title, @Nullable Identifier texture, int preferedPos, Class<?>... screenClasses) {
      this.screenClasses = screenClasses;
      this.title = title;
      this.texture = texture;
      this.preferedPos = preferedPos;
   }

   public Component getTitle() {
      return this.title;
   }

   @Nullable
   public Identifier getTexture() {
      return this.texture;
   }

   @Nullable
   public ItemStack getItemStack(Minecraft client) {
      return null;
   }

   public int getPreferedPos() {
      return this.preferedPos;
   }

   public boolean shouldShow(Minecraft client) {
      return true;
   }

   public void onClick(Minecraft client) {
   }

   public boolean canClick(Class<?> screenClass, Minecraft client) {
      return !this.isSelectedScreen(screenClass);
   }

   public boolean isSelectedScreen(Class<?> screenClass) {
      for (int i = 0; i < this.screenClasses.length; i++) {
         if (this.screenClasses[i].equals(screenClass)) {
            return true;
         }
      }

      return false;
   }
}
