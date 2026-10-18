package net.jmb19905.niftycarts.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.Objects;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.jmb19905.niftycarts.NiftyCarts;
import net.jmb19905.niftycarts.client.renderer.NiftyCartsModelLayers;
import net.jmb19905.niftycarts.client.renderer.entity.model.CartBannerFlagModel;
import net.jmb19905.niftycarts.client.renderer.entity.model.SeedDrillModel;
import net.jmb19905.niftycarts.entity.SeedDrillEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.banner.BannerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class SeedDrillRenderer extends DrawnRenderer<SeedDrillEntity, SeedDrillRenderState, SeedDrillModel> {
   private final Random random = new Random();
   private final ItemModelResolver itemRenderer;

   public SeedDrillRenderer(Context renderManager) {
      super(
         renderManager,
         new SeedDrillModel(
            renderManager.bakeLayer(NiftyCartsModelLayers.SEED_DRILL),
            new BannerModel(renderManager.bakeLayer(ModelLayers.STANDING_BANNER)),
            new CartBannerFlagModel(renderManager.bakeLayer(ModelLayers.STANDING_BANNER_FLAG))
         )
      );
      this.shadowRadius = 1.0F;
      this.itemRenderer = renderManager.getItemModelResolver();
   }

   @NotNull
   public SeedDrillRenderState createRenderState() {
      return new SeedDrillRenderState();
   }

   public void extractRenderState(SeedDrillEntity entity, SeedDrillRenderState state, float delta) {
      super.extractRenderState(entity, state, delta);
      state.level = entity.level();
      state.seeds = NonNullList.create();
      state.itemStates = NonNullList.create();

      for (int i = 0; i < entity.getItemStacks().size(); i++) {
         state.seeds.add(i, entity.getStackInSlot(i));
         ItemStackRenderState itemStackRenderState = new ItemStackRenderState();
         this.itemRenderer.updateForNonLiving(itemStackRenderState, entity.getStackInSlot(i), ItemDisplayContext.FIXED, entity);
         state.itemStates.add(i, itemStackRenderState);
      }
   }

   @NotNull
   public Identifier getTextureLocation(SeedDrillRenderState state) {
      return NiftyCarts.resLoc("textures/entity/" + state.woodType.name() + "_seed_drill.png");
   }

   protected void submitContents(SeedDrillRenderState state, PoseStack stack, SubmitNodeCollector collector) {
      for (int i = 0; i < state.seeds.size(); i++) {
         ItemStack itemStack = (ItemStack)state.seeds.get(i);
         if (!itemStack.isEmpty()) {
            int finalI = i;
            this.random.setSeed(32L * i + Objects.hashCode(BuiltInRegistries.ITEM.getKey(itemStack.getItem())));

            for (int j = 0; j < itemStack.getCount() / 4; j++) {
               int finalJ = j;
               this.attach(this.model.getBody(), this.model.getBody(), s -> {
                  float f = finalI - 4.5F;
                  s.translate(f / 6.5 + 0.0625, -0.125F - finalJ / 32.0F + finalI % 4 * 0.001, this.random.nextFloat(0.375F) - 0.1875F);
                  s.mulPose(Axis.XP.rotationDegrees(-90.0F));
                  s.scale(0.6F, 0.6F, 0.6F);
                  s.mulPose(Axis.ZP.rotation(this.random.nextFloat((float) (Math.PI * 2))));
                  ((ItemStackRenderState)state.itemStates.get(finalI)).submit(stack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
               }, stack);
            }
         }
      }
   }
}
