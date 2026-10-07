package com.github.legoatoom.connectiblechains.client.render.entity.catenary;

import com.github.legoatoom.connectiblechains.ConnectibleChains;
import com.github.legoatoom.connectiblechains.client.render.entity.ChainModel;
import com.github.legoatoom.connectiblechains.client.render.entity.UVRect;
import com.github.legoatoom.connectiblechains.util.Helper;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class SquareCatenaryRenderer extends CatenaryRenderer {
   public static final float SQRT_2 = (float)Math.sqrt(2.0);
   protected static final float CHAIN_SCALE = 1.0F;

   public SquareCatenaryRenderer(UVRect a, UVRect b) {
      super(a, b);
   }

   @Override
   public ChainModel buildModel(Vector3f chainVec) {
      float desiredSegmentLength = 1.0F / ConnectibleChains.runtimeConfig.getQuality();
      int initialCapacity = (int)(4.0F * chainVec.lengthSquared() / desiredSegmentLength);
      ChainModel.Builder builder = ChainModel.builder(initialCapacity);
      if (chainVec.x() == 0.0F && chainVec.z() == 0.0F) {
         this.buildFaceVertical(builder, chainVec);
      } else {
         this.buildFace(builder, chainVec);
      }

      return builder.build();
   }

   private void buildFaceVertical(ChainModel.Builder builder, Vector3f endPosition) {
      endPosition.x = 0.0F;
      endPosition.z = 0.0F;
      float chainHalfWidthA = (this.SIDE_A.x1() - this.SIDE_A.x0()) / 32.0F * 1.0F;
      float chainHalfWidthB = (this.SIDE_B.x1() - this.SIDE_B.x0()) / 32.0F * 1.0F;
      Vector3f normalA = new Vector3f((float)Math.cos(Math.toRadians(45.0)), 0.0F, (float)Math.sin(Math.toRadians(45.0)));
      Vector3f normalB = new Vector3f((float)Math.cos(Math.toRadians(-45.0)), 0.0F, (float)Math.sin(Math.toRadians(-45.0)));
      normalA.normalize(chainHalfWidthA * SQRT_2);
      normalB.normalize(chainHalfWidthB * SQRT_2);
      Vector3f vert00A = new Vector3f(-normalA.x(), 0.0F, -normalA.z());
      Vector3f vert01A = new Vector3f(normalA.x(), 0.0F, normalA.z());
      Vector3f vert10A = new Vector3f(-normalA.x(), endPosition.y(), -normalA.z());
      Vector3f vert11A = new Vector3f(normalA.x(), endPosition.y(), normalA.z());
      Vector3f vert00B = new Vector3f(-normalB.x(), 0.0F, -normalB.z());
      Vector3f vert01B = new Vector3f(normalB.x(), 0.0F, normalB.z());
      Vector3f vert10B = new Vector3f(-normalB.x(), endPosition.y(), -normalB.z());
      Vector3f vert11B = new Vector3f(normalB.x(), endPosition.y(), normalB.z());
      float uvv0 = 0.0F;
      float uvv1 = Math.abs(endPosition.y()) / 1.0F;
      this.build4Sides(builder, uvv0, uvv1, vert00A, vert01A, vert10A, vert11A, vert00B, vert01B, vert10B, vert11B);
   }

   private void buildFace(ChainModel.Builder builder, Vector3f endPosition) {
      float desiredSegmentLength = 1.0F / ConnectibleChains.runtimeConfig.getQuality();
      float distance = endPosition.length();
      float distanceXZ = (float)Math.sqrt(Math.fma(endPosition.x(), endPosition.x(), endPosition.z() * endPosition.z()));
      float wrongDistanceFactor = distance / distanceXZ;
      float chainHalfWidthA = (this.SIDE_A.x1() - this.SIDE_A.x0()) / 32.0F * 1.0F;
      float chainHalfWidthB = (this.SIDE_B.x1() - this.SIDE_B.x0()) / 32.0F * 1.0F;
      Vector3f normal = new Vector3f();
      Vector3f rotAxis = new Vector3f();
      Vector3f vert00A = new Vector3f();
      Vector3f vert01A = new Vector3f();
      Vector3f vert11A = new Vector3f();
      Vector3f vert10A = new Vector3f();
      Vector3f vert00B = new Vector3f();
      Vector3f vert01B = new Vector3f();
      Vector3f vert11B = new Vector3f();
      Vector3f vert10B = new Vector3f();
      Quaternionf rotatorA = new Quaternionf();
      Quaternionf rotatorB = new Quaternionf();
      Vector3f segmentStart = new Vector3f();
      Vector3f segmentEnd = new Vector3f();
      float uvv1 = 0.0F;
      float x = 0.0F;

      for (int segment = 0; segment < 2048; segment++) {
         float gradient = (float)Helper.drip2prime(x * wrongDistanceFactor, distance, endPosition.y());
         float var32 = x + this.estimateDeltaX(desiredSegmentLength, gradient);
         x = Math.min(distanceXZ, var32);
         float y = (float)Helper.drip2(x * wrongDistanceFactor, distance, endPosition.y());
         segmentEnd.set(x, y, 0.0F);
         rotAxis.set(segmentEnd.x() - segmentStart.x(), segmentEnd.y() - segmentStart.y(), segmentEnd.z() - segmentStart.z());
         rotAxis.normalize();
         rotatorA = rotatorA.fromAxisAngleDeg(rotAxis, -45.0F);
         rotatorB = rotatorB.fromAxisAngleDeg(rotAxis, 45.0F);
         normal.set(-gradient, Math.abs(distanceXZ / distance), 0.0F);
         Vector3f normalA = new Vector3f();
         Vector3f normalB = new Vector3f();
         normal.rotate(rotatorA, normalA);
         normal.rotate(rotatorB, normalB);
         normalA.normalize(chainHalfWidthA * SQRT_2);
         normalB.normalize(chainHalfWidthB * SQRT_2);
         if (segment == 0) {
            vert00A.set(segmentStart).sub(normalA);
            vert01A.set(segmentStart).add(normalA);
            vert00B.set(segmentStart).sub(normalB);
            vert01B.set(segmentStart).add(normalB);
         } else {
            vert00A.set(vert10A);
            vert01A.set(vert11A);
            vert00B.set(vert10B);
            vert01B.set(vert11B);
         }

         vert10A.set(segmentEnd).sub(normalA);
         vert11A.set(segmentEnd).add(normalA);
         vert10B.set(segmentEnd).sub(normalB);
         vert11B.set(segmentEnd).add(normalB);
         float actualSegmentLength = segmentStart.distance(segmentEnd);
         float uvv0 = uvv1;
         uvv1 += actualSegmentLength / 1.0F;
         this.build4Sides(builder, uvv0, uvv1, vert00A, vert01A, vert10A, vert11A, vert00B, vert01B, vert10B, vert11B);
         if (x >= distanceXZ) {
            break;
         }

         segmentStart.set(segmentEnd);
      }
   }

   private void build4Sides(
      ChainModel.Builder builder,
      float uvv0,
      float uvv1,
      Vector3f vert00A,
      Vector3f vert01A,
      Vector3f vert10A,
      Vector3f vert11A,
      Vector3f vert00B,
      Vector3f vert01B,
      Vector3f vert10B,
      Vector3f vert11B
   ) {
      builder.vertex(vert10A).uv(this.SIDE_A.x1() / 16.0F, uvv1).next();
      builder.vertex(vert11B).uv(this.SIDE_A.x0() / 16.0F, uvv1).next();
      builder.vertex(vert01B).uv(this.SIDE_A.x0() / 16.0F, uvv0).next();
      builder.vertex(vert00A).uv(this.SIDE_A.x1() / 16.0F, uvv0).next();
      builder.vertex(vert00A).uv(this.SIDE_B.x0() / 16.0F, uvv0).next();
      builder.vertex(vert00B).uv(this.SIDE_B.x1() / 16.0F, uvv0).next();
      builder.vertex(vert10B).uv(this.SIDE_B.x1() / 16.0F, uvv1).next();
      builder.vertex(vert10A).uv(this.SIDE_B.x0() / 16.0F, uvv1).next();
      builder.vertex(vert00B).uv(this.SIDE_A.x1() / 16.0F, uvv0).next();
      builder.vertex(vert01A).uv(this.SIDE_A.x0() / 16.0F, uvv0).next();
      builder.vertex(vert11A).uv(this.SIDE_A.x0() / 16.0F, uvv1).next();
      builder.vertex(vert10B).uv(this.SIDE_A.x1() / 16.0F, uvv1).next();
      builder.vertex(vert01A).uv(this.SIDE_B.x0() / 16.0F, uvv0).next();
      builder.vertex(vert01B).uv(this.SIDE_B.x1() / 16.0F, uvv0).next();
      builder.vertex(vert11B).uv(this.SIDE_B.x1() / 16.0F, uvv1).next();
      builder.vertex(vert11A).uv(this.SIDE_B.x0() / 16.0F, uvv1).next();
   }
}
