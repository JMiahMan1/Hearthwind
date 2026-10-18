package ht.treechop.common.config;

import java.util.function.Function;

public enum Rounder implements net.minecraft.util.StringRepresentable {
   DOWN(value -> (int)Math.floor(value)),
   NEAREST(value -> (int)Math.round(value)),
   UP(value -> (int)Math.ceil(value));

   private final Function<Double, Integer> transformation;

   private Rounder(Function<Double, Integer> transformation) {
      this.transformation = transformation;
   }

   public int round(double value) {
      return this.transformation.apply(value);
   }

   public String getSerializedName() {
      return this.name();
   }
}
