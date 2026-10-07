package com.github.legoatoom.connectiblechains.client.render.entity.catenary;

import com.github.legoatoom.connectiblechains.ConnectibleChains;
import com.github.legoatoom.connectiblechains.client.render.entity.ChainModel;
import com.github.legoatoom.connectiblechains.client.render.entity.UVRect;
import com.github.legoatoom.connectiblechains.util.Helper;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class CrossCatenaryRenderer extends CatenaryRenderer {
   public CrossCatenaryRenderer(UVRect a, UVRect b) {
      super(a, b);
   }

   @Override
   public ChainModel buildModel(Vector3f chainVec) {
      float desiredSegmentLength = 1.0F / ConnectibleChains.runtimeConfig.getQuality();
      int initialCapacity = (int)(2.0F * chainVec.lengthSquared() / desiredSegmentLength);
      ChainModel.Builder builder = ChainModel.builder(initialCapacity);
      if (chainVec.x() == 0.0F && chainVec.z() == 0.0F) {
         this.buildFaceVertical(builder, chainVec, 45.0F, this.SIDE_A);
         this.buildFaceVertical(builder, chainVec, -45.0F, this.SIDE_B);
      } else {
         this.buildFace(builder, chainVec, 45.0F, this.SIDE_A);
         this.buildFace(builder, chainVec, -45.0F, this.SIDE_B);
      }

      return builder.build();
   }

   private void buildFaceVertical(ChainModel.Builder builder, Vector3f endPosition, float angle, UVRect uv) {
      endPosition.x = 0.0F;
      endPosition.z = 0.0F;
      float chainWidth = (uv.x1() - uv.x0()) / 16.0F * 1.0F;
      Vector3f normal = new Vector3f((float)Math.cos(Math.toRadians(angle)), 0.0F, (float)Math.sin(Math.toRadians(angle)));
      normal.normalize(chainWidth / 2.0F);
      Vector3f offset = new Vector3f(5.0E-4F, 0.0F, 5.0E-4F);
      offset = offset.rotateY(-angle);
      Vector3f vert00 = new Vector3f(-normal.x(), 0.0F, -normal.z());
      Vector3f vert01 = new Vector3f(normal.x(), 0.0F, normal.z());
      Vector3f vert10 = new Vector3f(-normal.x(), endPosition.y(), -normal.z());
      Vector3f vert11 = new Vector3f(normal.x(), endPosition.y(), normal.z());
      float uvv0 = 0.0F;
      float uvv1 = Math.abs(endPosition.y()) / 1.0F;
      builder.vertex(vert00.add(offset)).uv(uv.x0() / 16.0F, uvv0).next();
      builder.vertex(vert01.add(offset)).uv(uv.x1() / 16.0F, uvv0).next();
      builder.vertex(vert11.add(offset)).uv(uv.x1() / 16.0F, uvv1).next();
      builder.vertex(vert10.add(offset)).uv(uv.x0() / 16.0F, uvv1).next();
      builder.vertex(vert10.sub(offset)).uv(uv.x0() / 16.0F, uvv1).next();
      builder.vertex(vert11.sub(offset)).uv(uv.x1() / 16.0F, uvv1).next();
      builder.vertex(vert01.sub(offset)).uv(uv.x1() / 16.0F, uvv0).next();
      builder.vertex(vert00.sub(offset)).uv(uv.x0() / 16.0F, uvv0).next();
   }

   private void buildFace(ChainModel.Builder builder, Vector3f endPosition, float angle, UVRect uv) {
      float desiredSegmentLength = 1.0F / ConnectibleChains.runtimeConfig.getQuality();
      float distance = endPosition.length();
      float distanceXZ = (float)Math.sqrt(Math.fma(endPosition.x(), endPosition.x(), endPosition.z() * endPosition.z()));
      float wrongDistanceFactor = distance / distanceXZ;
      float chainWidth = (uv.x1() - uv.x0()) / 16.0F * 1.0F;
      Vector3f normal = new Vector3f();
      Vector3f rotAxis = new Vector3f();
      Vector3f vert00 = new Vector3f();
      Vector3f vert01 = new Vector3f();
      Vector3f vert11 = new Vector3f();
      Vector3f vert10 = new Vector3f();
      Quaternionf rotator = new Quaternionf();
      Vector3f segmentStart = new Vector3f();
      Vector3f segmentEnd = new Vector3f();
      float uvv1 = 0.0F;
      float x = 0.0F;

      for (int segment = 0; segment < 2048; segment++) {
         float gradient = (float)Helper.drip2prime(x * wrongDistanceFactor, distance, endPosition.y());
         float var26 = x + this.estimateDeltaX(desiredSegmentLength, gradient);
         x = Math.min(var26, distanceXZ);
         float y = (float)Helper.drip2(x * wrongDistanceFactor, distance, endPosition.y());
         segmentEnd.set(x, y, 0.0F);
         rotAxis.set(segmentEnd.x() - segmentStart.x(), segmentEnd.y() - segmentStart.y(), segmentEnd.z() - segmentStart.z());
         rotAxis.normalize();
         rotator = rotator.fromAxisAngleDeg(rotAxis, angle);
         normal.set(-gradient, Math.abs(distanceXZ / distance), 0.0F);
         normal.normalize();
         normal.rotate(rotator);
         normal.normalize(chainWidth / 2.0F);
         if (segment == 0) {
            vert00.set(segmentStart).sub(normal);
            vert01.set(segmentStart).add(normal);
         } else {
            vert00.set(vert10);
            vert01.set(vert11);
         }

         vert10.set(segmentEnd).sub(normal);
         vert11.set(segmentEnd).add(normal);
         float actualSegmentLength = segmentStart.distance(segmentEnd);
         float uvv0 = uvv1;
         uvv1 += actualSegmentLength / 1.0F;
         if (angle > 0.0F) {
            builder.vertex(vert00).uv(uv.x0() / 16.0F, uvv0).next();
            builder.vertex(vert01).uv(uv.x1() / 16.0F, uvv0).next();
            builder.vertex(vert11).uv(uv.x1() / 16.0F, uvv1).next();
            builder.vertex(vert10).uv(uv.x0() / 16.0F, uvv1).next();
         } else {
            builder.vertex(vert10).uv(uv.x0() / 16.0F, uvv1).next();
            builder.vertex(vert11).uv(uv.x1() / 16.0F, uvv1).next();
            builder.vertex(vert01).uv(uv.x1() / 16.0F, uvv0).next();
            builder.vertex(vert00).uv(uv.x0() / 16.0F, uvv0).next();
         }

         if (x >= distanceXZ) {
            break;
         }

         segmentStart.set(segmentEnd);
      }
   }
}
