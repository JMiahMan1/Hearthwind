package net.jmb19905.niftycarts.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.jmb19905.niftycarts.NiftyCarts;
import net.jmb19905.niftycarts.client.renderer.NiftyCartsModelLayers;
import net.jmb19905.niftycarts.client.renderer.entity.model.CartBannerFlagModel;
import net.jmb19905.niftycarts.client.renderer.entity.model.PlowModel;
import net.jmb19905.niftycarts.entity.PlowEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.banner.BannerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public final class PlowRenderer extends DrawnRenderer<PlowEntity, PlowRenderState, PlowModel> {
   private final ItemModelResolver itemRenderer;

   public PlowRenderer(Context ctx) {
      super(
         ctx,
         new PlowModel(
            ctx.bakeLayer(NiftyCartsModelLayers.PLOW),
            new BannerModel(ctx.bakeLayer(ModelLayers.STANDING_BANNER)),
            new CartBannerFlagModel(ctx.bakeLayer(ModelLayers.STANDING_BANNER_FLAG))
         )
      );
      this.shadowRadius = 1.0F;
      this.itemRenderer = ctx.getItemModelResolver();
   }

   public void extractRenderState(PlowEntity entity, PlowRenderState state, float delta) {
      super.extractRenderState(entity, state, delta);
      state.plowing = entity.getPlowing();
      state.level = entity.level();
      state.items = NonNullList.create();
      state.itemStates = NonNullList.create();

      for (int i = 0; i < entity.getItemStacks().size(); i++) {
         state.items.add(i, entity.getStackInSlot(i));
         ItemStackRenderState itemStackRenderState = new ItemStackRenderState();
         this.itemRenderer.updateForNonLiving(itemStackRenderState, entity.getStackInSlot(i), ItemDisplayContext.FIXED, entity);
         state.itemStates.add(i, itemStackRenderState);
      }
   }

   @NotNull
   public PlowRenderState createRenderState() {
      return new PlowRenderState();
   }

   @NotNull
   public Identifier getTextureLocation(PlowRenderState state) {
      return NiftyCarts.resLoc("textures/entity/" + state.woodType.name() + "_plow.png");
   }

   protected void submitContents(PlowRenderState state, PoseStack stack, SubmitNodeCollector collector) {
      for (int i = 0; i < state.items.size(); i++) {
         ItemStack itemStack = (ItemStack)state.items.get(i);
         if (!itemStack.isEmpty()) {
            int finalI = i;
            this.attach(this.model.getBody(), this.model.getShaft(i), s -> {
               s.mulPose(Axis.XP.rotationDegrees(-90.0F));
               s.mulPose(Axis.YP.rotationDegrees(90.0F));
               s.translate(-0.25, 0.0625, 0.0);
               if (itemStack.getItem() instanceof BlockItem) {
                  s.translate(0.0, -0.1, 0.0);
                  s.mulPose(Axis.ZP.rotationDegrees(180.0F));
               }

               ((ItemStackRenderState)state.itemStates.get(finalI)).submit(stack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            }, stack);
         }
      }
   }
}
