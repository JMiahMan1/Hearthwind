package net.libz.access;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface MouseAccessor {
   void setMousePosition(int var1, int var2);
}
