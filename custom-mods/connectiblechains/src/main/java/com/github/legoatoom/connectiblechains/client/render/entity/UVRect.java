package com.github.legoatoom.connectiblechains.client.render.entity;

import com.mojang.serialization.Codec;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import com.github.legoatoom.connectiblechains.util.Pair;

@Environment(EnvType.CLIENT)
public record UVRect(float x0, float x1) {
   public static final Codec<Pair<UVRect, UVRect>> CODEC = Codec.FLOAT.listOf(4, 4).xmap(UVRect::to, UVRect::from);
   public static final UVRect DEFAULT_SIDE_A = new UVRect(0.0F, 3.0F);
   public static final UVRect DEFAULT_SIDE_B = new UVRect(3.0F, 6.0F);

   private static Pair<UVRect, UVRect> to(List<Float> floats) {
      return new Pair<>(new UVRect(floats.get(0), floats.get(1)), new UVRect(floats.get(2), floats.get(3)));
   }

   private static List<Float> from(Pair<UVRect, UVRect> uvRect) {
      return List.of(
         ((UVRect)uvRect.getLeft()).x0(), ((UVRect)uvRect.getLeft()).x1(), ((UVRect)uvRect.getRight()).x0(), ((UVRect)uvRect.getRight()).x1()
      );
   }
}
