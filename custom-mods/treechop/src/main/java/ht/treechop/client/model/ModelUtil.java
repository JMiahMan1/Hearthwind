package ht.treechop.client.model;

import ht.tuber.math.Vector3;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class ModelUtil {
   public static BakedQuad trimQuad(BakedQuad quad, Vector3 corner1, Vector3 corner2) {
      Vector3 mins = new Vector3(Math.min(corner1.x(), corner2.x()), Math.min(corner1.y(), corner2.y()), Math.min(corner1.z(), corner2.z()));
      Vector3 maxes = new Vector3(Math.max(corner1.x(), corner2.x()), Math.max(corner1.y(), corner2.y()), Math.max(corner1.z(), corner2.z()));
      Vertex oldV1 = getVertex(quad, 0);
      Vertex oldV2 = getVertex(quad, 1);
      Vertex oldV3 = getVertex(quad, 2);
      Vertex oldV4 = getVertex(quad, 3);
      Vertex newV1 = lerpVertexUVsInTriangle(oldV1.xyz().clamp(mins, maxes), oldV1, oldV2, oldV3);
      Vertex newV2 = lerpVertexUVsInTriangle(oldV2.xyz().clamp(mins, maxes), oldV1, oldV2, oldV3);
      Vertex newV3 = lerpVertexUVsInTriangle(oldV3.xyz().clamp(mins, maxes), oldV1, oldV2, oldV3);
      Vertex newV4 = lerpVertexUVsInTriangle(oldV4.xyz().clamp(mins, maxes), oldV1, oldV2, oldV3);
      return new BakedQuad(
         newV1.pos(),
         newV2.pos(),
         newV3.pos(),
         newV4.pos(),
         UVPair.pack((float)newV1.u, (float)newV1.v),
         UVPair.pack((float)newV2.u, (float)newV2.v),
         UVPair.pack((float)newV3.u, (float)newV3.v),
         UVPair.pack((float)newV4.u, (float)newV4.v),
         quad.direction(),
         quad.materialInfo()
      );
   }

   private static Vertex lerpVertexUVsInTriangle(Vector3 pos, Vertex v1, Vertex v2, Vertex v3) {
      Vector3 v1Weights = getLerpWeightsInSimplex(pos, v1.xyz(), v2.xyz(), v3.xyz());
      double u = v1Weights.dot(v1.u, v2.u, v3.u);
      double v = v1Weights.dot(v1.v, v2.v, v3.v);
      return new Vertex(pos.x, pos.y, pos.z, u, v);
   }

   private static Vector3 getLerpWeightsInSimplex(Vector3 pos, Vector3 v1, Vector3 v2, Vector3 v3) {
      Vector3 side1 = v2.subtract(v1);
      Vector3 side2 = v3.subtract(v1);
      Vector3 normal = side2.cross(side1).normalize();
      double offset = pos.subtract(v1).dot(normal);
      Vector3 p = pos.subtract(normal.scale(offset));
      Vector3 f1 = v1.subtract(p);
      Vector3 f2 = v2.subtract(p);
      Vector3 f3 = v3.subtract(p);
      Vector3 va = side1.cross(side2);
      Vector3 va1 = f2.cross(f3);
      Vector3 va2 = f3.cross(f1);
      Vector3 va3 = f1.cross(f2);
      double a = va.length();
      double w1 = va1.length() / a * Math.signum(va.dot(va1));
      double w2 = va2.length() / a * Math.signum(va.dot(va2));
      double w3 = va3.length() / a * Math.signum(va.dot(va3));
      return new Vector3(w1, w2, w3);
   }

   private static Vertex getVertex(BakedQuad quad, int index) {
      Vector3fc p = quad.position(index);
      long uv = quad.packedUV(index);
      return new Vertex(p.x() * 16.0, p.y() * 16.0, p.z() * 16.0, UVPair.unpackU(uv), UVPair.unpackV(uv));
   }

   public static BakedQuad translateQuad(BakedQuad quad, Vector3 translation) {
      return new BakedQuad(
         translate(quad.position(0), translation),
         translate(quad.position(1), translation),
         translate(quad.position(2), translation),
         translate(quad.position(3), translation),
         quad.packedUV0(),
         quad.packedUV1(),
         quad.packedUV2(),
         quad.packedUV3(),
         quad.direction(),
         quad.materialInfo()
      );
   }

   private static Vector3f translate(Vector3fc p, Vector3 translation) {
      return new Vector3f(
         (float)((p.x() * 16.0 + translation.x) / 16.0),
         (float)((p.y() * 16.0 + translation.y) / 16.0),
         (float)((p.z() * 16.0 + translation.z) / 16.0)
      );
   }

   private record Vertex(double x, double y, double z, double u, double v) {
      public Vector3 xyz() {
         return new Vector3(this.x, this.y, this.z);
      }

      public Vector3f pos() {
         return new Vector3f((float)(this.x / 16.0), (float)(this.y / 16.0), (float)(this.z / 16.0));
      }
   }
}
