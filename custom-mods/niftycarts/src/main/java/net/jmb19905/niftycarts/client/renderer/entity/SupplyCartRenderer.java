package net.jmb19905.niftycarts.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.jmb19905.niftycarts.NiftyCarts;
import net.jmb19905.niftycarts.client.renderer.NiftyCartsModelLayers;
import net.jmb19905.niftycarts.client.renderer.entity.model.CartBannerFlagModel;
import net.jmb19905.niftycarts.client.renderer.entity.model.SupplyCartModel;
import net.jmb19905.niftycarts.entity.SupplyCartEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.banner.BannerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public final class SupplyCartRenderer extends CargoCartRenderer<SupplyCartEntity, SupplyCartModel> {
   public SupplyCartRenderer(Context ctx) {
      super(
         ctx,
         new SupplyCartModel(
            ctx.bakeLayer(NiftyCartsModelLayers.SUPPLY_CART),
            new BannerModel(ctx.bakeLayer(ModelLayers.STANDING_BANNER)),
            new CartBannerFlagModel(ctx.bakeLayer(ModelLayers.STANDING_BANNER_FLAG))
         )
      );
   }

   @Override
   protected double getFlowerOffsetZ() {
      return -0.1875;
   }

   @Override
   protected Vec3 getWheelOffset() {
      return new Vec3(1.18, 0.1, -0.15);
   }

   @Override
   protected Vec3 getPaintingOffset(int i, int n, int count) {
      return new Vec3(0.0, 0.5 * (n - (count - 1) * 0.1) / count, -0.0625 * i);
   }

   @Override
   protected float getPaintingAngleFactor() {
      return 1.0F;
   }

   @Override
   protected double getSuppliesOffsetX(int x) {
      return (x - 0.5) * 11.0 / 16.0;
   }

   @Override
   protected double getSuppliesOffsetZ(int z) {
      return (z * 11.0 - 9.0) / 16.0;
   }

   @Override
   protected float getBlockSize() {
      return 0.65F;
   }

   @Override
   protected float getArmorSize() {
      return 1.0F;
   }

   @Override
   protected double getShieldOffsetY() {
      return 1.2;
   }

   @Override
   protected float getItemSize() {
      return 0.7F;
   }

   @NotNull
   public Identifier getTextureLocation(CargoCartRenderState state) {
      return NiftyCarts.resLoc("textures/entity/" + state.woodType.name() + "_supply_cart.png");
   }
}
