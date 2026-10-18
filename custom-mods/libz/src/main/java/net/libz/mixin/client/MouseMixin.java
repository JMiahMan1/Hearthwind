package net.libz.mixin.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.libz.access.MouseAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Environment(EnvType.CLIENT)
@Mixin({MouseHandler.class})
public class MouseMixin implements MouseAccessor {
   @Shadow
   private double xpos;
   @Shadow
   private double ypos;
   @Shadow
   private Minecraft minecraft;

   @Override
   public void setMousePosition(int xPos, int yPos) {
      this.xpos = xPos;
      this.ypos = yPos;
      InputConstants.grabOrReleaseMouse(this.minecraft.getWindow(), 212993, this.xpos, this.ypos);
   }
}
