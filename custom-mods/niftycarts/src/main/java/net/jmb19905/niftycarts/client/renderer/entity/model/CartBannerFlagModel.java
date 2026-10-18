package net.jmb19905.niftycarts.client.renderer.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.banner.BannerFlagModel;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class CartBannerFlagModel extends BannerFlagModel {
   public CartBannerFlagModel(ModelPart root) {
      super(root);
   }

   public void setupAnim(@NotNull Float sway) {
      super.setupAnim(sway);
      this.flag.xRot = 0.01F * Mth.cos((float) (Math.PI * 2) * sway) * (float) Math.PI;
   }
}
