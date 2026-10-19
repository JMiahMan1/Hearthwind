package io.github.phantomloader.library.integration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
public @interface FabricCustomEntryPoint {
   String name();

   String interfaceName();
}
