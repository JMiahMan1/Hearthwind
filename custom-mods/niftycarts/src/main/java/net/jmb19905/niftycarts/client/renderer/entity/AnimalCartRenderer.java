package net.jmb19905.niftycarts.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.jmb19905.niftycarts.NiftyCarts;
import net.jmb19905.niftycarts.client.renderer.NiftyCartsModelLayers;
import net.jmb19905.niftycarts.client.renderer.entity.model.AnimalCartModel;
import net.jmb19905.niftycarts.client.renderer.entity.model.CartBannerFlagModel;
import net.jmb19905.niftycarts.entity.AnimalCartEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.banner.BannerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public final class AnimalCartRenderer extends DrawnRenderer<AnimalCartEntity, CartRenderState, AnimalCartModel> {
   public AnimalCartRenderer(Context renderManager) {
      super(
         renderManager,
         new AnimalCartModel(
            renderManager.bakeLayer(NiftyCartsModelLayers.ANIMAL_CART),
            new BannerModel(renderManager.bakeLayer(ModelLayers.STANDING_BANNER)),
            new CartBannerFlagModel(renderManager.bakeLayer(ModelLayers.STANDING_BANNER_FLAG))
         )
      );
      this.shadowRadius = 1.0F;
   }

   @NotNull
   @Override
   public CartRenderState createRenderState() {
      return new CartRenderState();
   }

   @Override
   protected void submitContents(CartRenderState state, PoseStack stack, SubmitNodeCollector submitNodeCollector) {
      if (state.bannerColor != null) {
         stack.pushPose();
         this.model.getBody().translateAndRotate(stack);
         stack.translate(0.0, -0.6, 1.56);
         this.submitBanner(state, stack, submitNodeCollector);
         stack.popPose();
      }
   }

   @Override
   public Identifier getTextureLocation(CartRenderState state) {
      return NiftyCarts.resLoc("textures/entity/" + state.woodType.name() + "_animal_cart.png");
   }
}
