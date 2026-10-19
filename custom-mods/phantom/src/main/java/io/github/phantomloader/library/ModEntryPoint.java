package io.github.phantomloader.library;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
public @interface ModEntryPoint {
   ModEntryPoint.Loader modLoader() default ModEntryPoint.Loader.COMMON;

   ModEntryPoint.Side side() default ModEntryPoint.Side.INIT;

   public static enum Loader {
      COMMON,
      FORGE,
      FABRIC;
   }

   public static enum Side {
      INIT,
      COMMON,
      CLIENT,
      SERVER;
   }
}
