package ht.treechop.common.config;

import java.util.function.Function;
import org.jetbrains.annotations.NotNull;

public enum ChopCountingAlgorithm implements net.minecraft.util.StringRepresentable {
   LINEAR(numBlocks -> {
      double x = numBlocks.intValue();
      double m = (Double)ConfigHandler.COMMON.linearM.get();
      double b = (Double)ConfigHandler.COMMON.linearB.get();
      return m * x + b;
   }),
   LOGARITHMIC(numBlocks -> {
      double x = numBlocks.intValue();
      double a = (Double)ConfigHandler.COMMON.logarithmicA.get();
      return 1.0 + a * Math.log(1.0 + (x - 1.0) / a);
   });

   private final Function<Integer, Double> preciseCalculation;

   private ChopCountingAlgorithm(Function<Integer, Double> preciseCalculation) {
      this.preciseCalculation = preciseCalculation;
   }

   public int calculate(int numBlocks, Rounder rounder, boolean canRequireMoreChopsThanBlocks) {
      int count = Math.max(1, rounder.round(this.preciseCalculation.apply(numBlocks)));
      return canRequireMoreChopsThanBlocks ? count : Math.min(numBlocks, count);
   }

   @NotNull
   public String getSerializedName() {
      return this.name();
   }
}
