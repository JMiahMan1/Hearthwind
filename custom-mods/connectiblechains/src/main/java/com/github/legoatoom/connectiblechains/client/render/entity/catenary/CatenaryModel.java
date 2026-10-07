package com.github.legoatoom.connectiblechains.client.render.entity.catenary;

import com.github.legoatoom.connectiblechains.util.Pair;

import com.github.legoatoom.connectiblechains.client.render.entity.UVRect;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.resources.Identifier;

public record CatenaryModel(
   Optional<CatenaryModel.CatenaryTextures> textures,
   Optional<net.minecraft.resources.Identifier> catenaryRendererId,
   Optional<Pair<UVRect, UVRect>> uvRects
) {
   public static final MapCodec<CatenaryModel> CODEC = RecordCodecBuilder.mapCodec(
      instance -> instance.group(
            CatenaryModel.CatenaryTextures.CODEC.codec().optionalFieldOf("textures").forGetter(CatenaryModel::textures),
            Identifier.CODEC.optionalFieldOf("model").forGetter(CatenaryModel::catenaryRendererId),
            UVRect.CODEC.optionalFieldOf("uv").forGetter(CatenaryModel::uvRects)
         )
         .apply(instance, CatenaryModel::new)
   );

   public record CatenaryTextures(Optional<net.minecraft.resources.Identifier> chainTexture, Optional<net.minecraft.resources.Identifier> knotTexture) {
      public static final MapCodec<CatenaryModel.CatenaryTextures> CODEC = RecordCodecBuilder.mapCodec(
         instance -> instance.group(
               Identifier.CODEC.optionalFieldOf("chain").forGetter(CatenaryModel.CatenaryTextures::chainTexture),
               Identifier.CODEC.optionalFieldOf("knot").forGetter(CatenaryModel.CatenaryTextures::knotTexture)
            )
            .apply(instance, CatenaryModel.CatenaryTextures::new)
      );
   }
}
