package com.github.legoatoom.connectiblechains.client.render.entity;

import com.github.legoatoom.connectiblechains.ConnectibleChains;
import com.github.legoatoom.connectiblechains.client.render.entity.catenary.CatenaryRenderer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Objects;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class ChainRenderer {
   private final Object2ObjectOpenHashMap<ChainRenderer.BakeKey, ChainModel> models = new Object2ObjectOpenHashMap(256);

   public void renderBaked(
      CatenaryRenderer renderer,
      VertexConsumer buffer,
      Pose matricesEntry,
      ChainRenderer.BakeKey key,
      Vector3f chainVec,
      int blockLight0,
      int blockLight1,
      int skyLight0,
      int skyLight1
   ) {
      ChainModel model;
      if (this.models.containsKey(key)) {
         model = (ChainModel)this.models.get(key);
      } else {
         model = renderer.buildModel(chainVec);
         this.models.put(key, model);
      }

      if (FabricLoader.getInstance().isDevelopmentEnvironment() && this.models.size() > 10000) {
         ConnectibleChains.LOGGER.error("Chain model leak found!");
      }

      model.render(buffer, matricesEntry, blockLight0, blockLight1, skyLight0, skyLight1);
   }

   public void render(
      CatenaryRenderer renderer, VertexConsumer buffer, Pose matricesEntry, Vector3f chainVec, int blockLight0, int blockLight1, int skyLight0, int skyLight1
   ) {
      ChainModel model = renderer.buildModel(chainVec);
      model.render(buffer, matricesEntry, blockLight0, blockLight1, skyLight0, skyLight1);
   }

   public void purge() {
      this.models.clear();
   }

   public static class BakeKey {
      private final Vec3 srcPos;
      private final Vec3 dstPos;

      public BakeKey(Vec3 srcPos, Vec3 dstPos) {
         this.srcPos = srcPos;
         this.dstPos = dstPos;
      }

      @Override
      public boolean equals(Object o) {
         return !(o instanceof ChainRenderer.BakeKey bakeKey)
            ? false
            : Objects.equals(this.srcPos, bakeKey.srcPos) && Objects.equals(this.dstPos, bakeKey.dstPos);
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.srcPos, this.dstPos);
      }
   }
}
