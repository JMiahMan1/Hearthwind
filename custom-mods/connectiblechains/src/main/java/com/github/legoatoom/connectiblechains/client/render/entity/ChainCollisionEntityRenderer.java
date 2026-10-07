package com.github.legoatoom.connectiblechains.client.render.entity;

import com.github.legoatoom.connectiblechains.entity.ChainCollisionEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

@Environment(EnvType.CLIENT)
public class ChainCollisionEntityRenderer
   extends net.minecraft.client.renderer.entity.EntityRenderer<ChainCollisionEntity, net.minecraft.client.renderer.entity.state.EntityRenderState> {
   public ChainCollisionEntityRenderer(Context dispatcher) {
      super(dispatcher);
   }

   public EntityRenderState createRenderState() {
      return new EntityRenderState();
   }
}
