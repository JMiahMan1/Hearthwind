package net.smitherz.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.libz.api.Tab;
import net.minecraft.client.gui.screens.inventory.GrindstoneScreen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Environment(EnvType.CLIENT)
@Mixin({GrindstoneScreen.class})
public class GrindstoneScreenMixin implements Tab {
   @Nullable
   public Class<?> getParentScreenClass() {
      return this.getClass();
   }
}
