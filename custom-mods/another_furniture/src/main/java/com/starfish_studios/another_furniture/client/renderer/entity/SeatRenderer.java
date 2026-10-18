package com.starfish_studios.another_furniture.client.renderer.entity;

import com.starfish_studios.another_furniture.entity.SeatEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

@Environment(EnvType.CLIENT)
public class SeatRenderer extends EntityRenderer<SeatEntity, EntityRenderState> {
   public SeatRenderer(Context context) {
      super(context);
   }

   public EntityRenderState createRenderState() {
      return new EntityRenderState();
   }

   public void extractRenderState(SeatEntity seatEntity, EntityRenderState state, float partialTick) {
      super.extractRenderState(seatEntity, state, partialTick);
   }

   protected boolean shouldShowName(SeatEntity seatEntity, double d) {
      return false;
   }
}
