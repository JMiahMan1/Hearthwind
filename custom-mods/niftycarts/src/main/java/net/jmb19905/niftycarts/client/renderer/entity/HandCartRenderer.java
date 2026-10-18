package net.jmb19905.niftycarts.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.jmb19905.niftycarts.NiftyCarts;
import net.jmb19905.niftycarts.client.renderer.NiftyCartsModelLayers;
import net.jmb19905.niftycarts.client.renderer.entity.model.CartBannerFlagModel;
import net.jmb19905.niftycarts.client.renderer.entity.model.HandCartModel;
import net.jmb19905.niftycarts.entity.HandCartEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.banner.BannerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class HandCartRenderer extends CargoCartRenderer<HandCartEntity, HandCartModel> {
   public HandCartRenderer(Context ctx) {
      super(
         ctx,
         new HandCartModel(
            ctx.bakeLayer(NiftyCartsModelLayers.HAND_CART),
            new BannerModel(ctx.bakeLayer(ModelLayers.STANDING_BANNER)),
            new CartBannerFlagModel(ctx.bakeLayer(ModelLayers.STANDING_BANNER_FLAG))
         )
      );
   }

   @Override
   protected double getFlowerOffsetZ() {
      return -0.0625;
   }

   @Override
   protected Vec3 getWheelOffset() {
      return new Vec3(0.91, 0.05, -0.15);
   }

   @Override
   protected Vec3 getPaintingOffset(int i, int n, int count) {
      return new Vec3(0.0, 0.03, -0.0625 * i + 1.0E-4F);
   }

   @Override
   protected float getPaintingAngleFactor() {
      return 0.2F;
   }

   @Override
   protected double getSuppliesOffsetX(int x) {
      return (x * 2 - 1) * 4 / 16.0;
   }

   @Override
   protected double getSuppliesOffsetZ(int z) {
      return (z * 2 - 1) * 5 / 16.0;
   }

   @Override
   protected float getBlockSize() {
      return 0.5F;
   }

   @Override
   protected float getArmorSize() {
      return 0.9F;
   }

   @Override
   protected double getShieldOffsetY() {
      return 1.2;
   }

   @Override
   protected float getItemSize() {
      return 0.6F;
   }

   @NotNull
   public Identifier getTextureLocation(CargoCartRenderState state) {
      return NiftyCarts.resLoc("textures/entity/" + state.woodType.name() + "_hand_cart.png");
   }
}
