package io.github.phantomloader.library.platform;

public interface Platform {
   String platformName();

   boolean isModLoaded(String var1);

   boolean isDevelopmentEnvironment();

   boolean isClientSide();

   boolean isServerSide();
}
