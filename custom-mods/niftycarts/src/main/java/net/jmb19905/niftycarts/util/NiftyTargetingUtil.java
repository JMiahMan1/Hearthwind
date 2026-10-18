package net.jmb19905.niftycarts.util;

import java.util.List;
import java.util.function.Predicate;
import net.jmb19905.niftycarts.entity.AbstractDrawnEntity;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.Nullable;

public class NiftyTargetingUtil {
   @Nullable
   public static <T extends net.minecraft.world.entity.Entity> T getNearestEntity(
      List<? extends T> list,
      NiftyTargetingUtil.Conditions<T> targetingConditions,
      @Nullable net.minecraft.world.entity.Entity entity,
      double d,
      double e,
      double f
   ) {
      double g = -1.0;
      T livingEntity2 = null;

      for (T livingEntity3 : list) {
         if (targetingConditions.test(entity, livingEntity3)) {
            double h = livingEntity3.distanceToSqr(d, e, f);
            if (g == -1.0 || h < g) {
               g = h;
               livingEntity2 = livingEntity3;
            }
         }
      }

      return livingEntity2;
   }

   public static class Conditions<T extends net.minecraft.world.entity.Entity> {
      private static final double MIN_VISIBILITY_DISTANCE_FOR_INVISIBLE_TARGET = 2.0;
      private double range = -1.0;
      private boolean checkLineOfSight = true;
      private boolean testInvisible = true;
      @Nullable
      private Predicate<AbstractDrawnEntity> selector;

      public NiftyTargetingUtil.Conditions<T> copy() {
         NiftyTargetingUtil.Conditions<T> targetingConditions = new NiftyTargetingUtil.Conditions<>();
         targetingConditions.range = this.range;
         targetingConditions.checkLineOfSight = this.checkLineOfSight;
         targetingConditions.testInvisible = this.testInvisible;
         targetingConditions.selector = this.selector;
         return targetingConditions;
      }

      public NiftyTargetingUtil.Conditions<T> range(double d) {
         this.range = d;
         return this;
      }

      public NiftyTargetingUtil.Conditions<T> ignoreLineOfSight() {
         this.checkLineOfSight = false;
         return this;
      }

      public NiftyTargetingUtil.Conditions<T> ignoreInvisibilityTesting() {
         this.testInvisible = false;
         return this;
      }

      public NiftyTargetingUtil.Conditions<T> selector(Predicate<AbstractDrawnEntity> predicate) {
         this.selector = predicate;
         return this;
      }

      public boolean test(@Nullable net.minecraft.world.entity.Entity entity, T entity2) {
         if (entity == entity2) {
            return false;
         } else if (this.selector != null && !this.selector.test((AbstractDrawnEntity)entity2)) {
            return false;
         } else if (entity != null) {
            if (this.range > 0.0) {
               double e = Math.max(this.range, 2.0);
               double f = entity.distanceToSqr(entity2.getX(), entity2.getY(), entity2.getZ());
               if (f > e * e) {
                  return false;
               }
            }

            return !this.checkLineOfSight || !(entity instanceof Mob) || ((Mob)entity).getSensing().hasLineOfSight(entity2);
         } else {
            return true;
         }
      }
   }
}
