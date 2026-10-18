package net.smitherz.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.libz.api.Tab;
import net.minecraft.client.gui.screens.inventory.SmithingScreen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Environment(EnvType.CLIENT)
@Mixin({SmithingScreen.class})
public class SmithingScreenMixin implements Tab {
   @Nullable
   public Class<?> getParentScreenClass() {
      return this.getClass();
   }
}
