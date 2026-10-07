package com.github.legoatoom.connectiblechains.client.render.entity.texture;

import com.github.legoatoom.connectiblechains.client.ClientInitializer;
import com.github.legoatoom.connectiblechains.client.render.entity.UVRect;
import com.github.legoatoom.connectiblechains.client.render.entity.catenary.CatenaryModel;
import com.github.legoatoom.connectiblechains.client.render.entity.catenary.CatenaryRenderer;
import com.github.legoatoom.connectiblechains.util.Helper;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import com.github.legoatoom.connectiblechains.util.Pair;
import org.jetbrains.annotations.NotNull;

public class ChainModelReloader extends net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener<CatenaryModel> {
   private static final String MODEL_FILE_LOCATION = "models/entity/connectiblechains";
   private static final int EXPECTED_UNIQUE_CHAIN_COUNT = 64;
   public static final Identifier DEFAULT_CATENARY = Helper.identifier("cross");
   public static final Pair<UVRect, UVRect> DEFAULT_UV = new Pair<>(UVRect.DEFAULT_SIDE_A, UVRect.DEFAULT_SIDE_B);
   private Map<net.minecraft.resources.Identifier, CatenaryModel> models = new Object2ObjectOpenHashMap(64);

   public ChainModelReloader() {
      super(CatenaryModel.CODEC.codec(), FileToIdConverter.json("models/entity/connectiblechains"));
   }

   protected void apply(
      Map<net.minecraft.resources.Identifier, CatenaryModel> prepared,
      net.minecraft.server.packs.resources.ResourceManager manager,
      net.minecraft.util.profiling.ProfilerFiller profiler
   ) {
      this.clearCache();
      this.models = prepared;
   }

   public void clearCache() {
      ClientInitializer.getInstance().getChainKnotEntityRenderer().ifPresent(it -> it.getChainRenderer().purge());
   }

   @NotNull
   private static Identifier defaultChainTextureId(Identifier itemId) {
      return Identifier.fromNamespaceAndPath(itemId.getNamespace(), "block/%s".formatted(itemId.getPath()));
   }

   @NotNull
   private static Identifier defaultKnotTextureId(Identifier itemId) {
      return Identifier.fromNamespaceAndPath(itemId.getNamespace(), "item/%s".formatted(itemId.getPath()));
   }

   public CatenaryRenderer getCatenaryRenderer(Identifier sourceItemId) {
      Optional<CatenaryModel> catenaryModel = Optional.ofNullable(this.models.get(sourceItemId));
      Identifier catenaryId = catenaryModel.flatMap(CatenaryModel::catenaryRendererId).orElse(DEFAULT_CATENARY);
      Pair<UVRect, UVRect> uvMappings = catenaryModel.flatMap(CatenaryModel::uvRects).orElse(DEFAULT_UV);
      return CatenaryRenderer.getRenderer(catenaryId, uvMappings);
   }

   public Identifier getChainTexture(Identifier sourceItemId) {
      return Optional.ofNullable(this.models.get(sourceItemId))
         .flatMap(CatenaryModel::textures)
         .flatMap(CatenaryModel.CatenaryTextures::chainTexture)
         .orElse(defaultChainTextureId(sourceItemId))
         .withPath(xva$0 -> "textures/%s.png".formatted(xva$0));
   }

   public Identifier getKnotTexture(Identifier sourceItemId) {
      return Optional.ofNullable(this.models.get(sourceItemId))
         .flatMap(CatenaryModel::textures)
         .flatMap(CatenaryModel.CatenaryTextures::knotTexture)
         .orElse(defaultKnotTextureId(sourceItemId))
         .withPath(xva$0 -> "textures/%s.png".formatted(xva$0));
   }
}
