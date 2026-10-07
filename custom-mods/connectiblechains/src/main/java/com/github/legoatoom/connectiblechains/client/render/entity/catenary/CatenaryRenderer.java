package com.github.legoatoom.connectiblechains.client.render.entity.catenary;

import com.github.legoatoom.connectiblechains.util.Pair;

import com.github.legoatoom.connectiblechains.client.render.entity.ChainModel;
import com.github.legoatoom.connectiblechains.client.render.entity.UVRect;
import java.util.HashMap;
import java.util.function.BiFunction;
import org.joml.Vector3f;

public abstract class CatenaryRenderer {
   protected static final float CHAIN_SCALE = 1.0F;
   protected static final int MAX_SEGMENTS = 2048;
   private static final HashMap<net.minecraft.resources.Identifier, BiFunction<UVRect, UVRect, CatenaryRenderer>> renderers = new HashMap<>();
   protected final UVRect SIDE_A;
   protected final UVRect SIDE_B;

   protected CatenaryRenderer(UVRect a, UVRect b) {
      this.SIDE_A = a;
      this.SIDE_B = b;
   }

   public static void addRenderer(net.minecraft.resources.Identifier id, BiFunction<UVRect, UVRect, CatenaryRenderer> rendererSupplier) {
      renderers.put(id, rendererSupplier);
   }

   public static CatenaryRenderer getRenderer(net.minecraft.resources.Identifier id, Pair<UVRect, UVRect> uvRects) {
      return renderers.getOrDefault(id, CrossCatenaryRenderer::new).apply((UVRect)uvRects.getLeft(), (UVRect)uvRects.getRight());
   }

   public abstract ChainModel buildModel(Vector3f var1);

   protected float estimateDeltaX(float s, float k) {
      return (float)(s / Math.sqrt(1.0F + k * k));
   }
}
