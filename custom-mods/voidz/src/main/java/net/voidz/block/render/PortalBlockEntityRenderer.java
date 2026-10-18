package net.voidz.block.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;

@Environment(EnvType.CLIENT)
public class PortalBlockEntityRenderer extends TheEndPortalRenderer {
   public PortalBlockEntityRenderer() {
   }

   public int getViewDistance() {
      return 256;
   }
}
