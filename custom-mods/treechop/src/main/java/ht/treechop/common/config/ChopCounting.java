package ht.treechop.common.config;

public class ChopCounting {
   private static final int CACHE_SIZE = 1024;
   private static final int[] cache = new int[1024];
   private static int numCached = 1;
   private static final Rounder rounder = (Rounder)ConfigHandler.COMMON.chopCountRounding.get();
   private static final boolean canRequireMoreChopsThanBlocks = (Boolean)ConfigHandler.COMMON.canRequireMoreChopsThanBlocks.get();
   private static final ChopCountingAlgorithm algorithm = (ChopCountingAlgorithm)ConfigHandler.COMMON.chopCountingAlgorithm.get();

   public static int calculate(int support) {
      if (support >= 1024) {
         return recalculate(support);
      } else {
         if (numCached <= support) {
            while (numCached <= support) {
               cache[numCached] = recalculate(numCached);
               numCached++;
            }
         }

         return cache[support];
      }
   }

   private static int recalculate(int support) {
      int count = Math.max(1, rounder.round(algorithm.calculate(support, rounder, canRequireMoreChopsThanBlocks)));
      return canRequireMoreChopsThanBlocks ? count : Math.min(support, count);
   }
}
