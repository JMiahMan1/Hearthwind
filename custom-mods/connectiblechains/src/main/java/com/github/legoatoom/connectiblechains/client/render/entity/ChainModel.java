package com.github.legoatoom.connectiblechains.client.render.entity;

import com.github.legoatoom.connectiblechains.ConnectibleChains;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.Lightmap;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

public record ChainModel(float[] vertices, float[] uvs) {
   public static ChainModel.Builder builder(int initialCapacity) {
      return new ChainModel.Builder(initialCapacity);
   }

   public void render(VertexConsumer buffer, Pose matricesEntry, int bLight0, int bLight1, int sLight0, int sLight1) {
      int count = this.vertices.length / 3;

      for (int i = 0; i < count; i++) {
         float f = i % (count / 2.0F) / (count / 2.0F);
         int blockLight = (int)Mth.lerp(f, bLight0, bLight1);
         int skyLight = (int)Mth.lerp(f, sLight0, sLight1);
         int light = ((blockLight << 4) | (skyLight << 4));
         buffer.addVertex(matricesEntry, this.vertices[i * 3], this.vertices[i * 3 + 1], this.vertices[i * 3 + 2])
            .setColor(1.0F, 1.0F, 1.0F, 1.0F)
            .setUv(this.uvs[i * 2], this.uvs[i * 2 + 1])
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(light)
            .setLineWidth(1.0F)
            .setNormal(1.0F, 1.0F, 1.0F);
      }
   }

   public static class Builder {
      private final List<Float> vertices;
      private final List<Float> uvs;
      private int size;

      public Builder(int initialCapacity) {
         this.vertices = new ArrayList<>(initialCapacity * 3);
         this.uvs = new ArrayList<>(initialCapacity * 2);
      }

      public ChainModel.Builder vertex(Vector3f v) {
         this.vertices.add(v.x());
         this.vertices.add(v.y());
         this.vertices.add(v.z());
         return this;
      }

      public ChainModel.Builder uv(float u, float v) {
         this.uvs.add(u);
         this.uvs.add(v);
         return this;
      }

      public void next() {
         this.size++;
      }

      public ChainModel build() {
         if (this.vertices.size() != this.size * 3) {
            ConnectibleChains.LOGGER.error("Wrong count of vertices");
         }

         if (this.uvs.size() != this.size * 2) {
            ConnectibleChains.LOGGER.error("Wrong count of uvs");
         }

         return new ChainModel(this.toFloatArray(this.vertices), this.toFloatArray(this.uvs));
      }

      private float[] toFloatArray(List<Float> floats) {
         float[] array = new float[floats.size()];
         int i = 0;

         for (float f : floats) {
            array[i++] = f;
         }

         return array;
      }
   }
}
