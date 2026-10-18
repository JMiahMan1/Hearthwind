package net.jmb19905.niftycarts.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class WagonRenderState extends CartRenderState {
   public int maxChestCount;
   public int chestCount;
   public boolean hasRoof;
   public int unfurled;
   public Identifier roofTexture;
}
