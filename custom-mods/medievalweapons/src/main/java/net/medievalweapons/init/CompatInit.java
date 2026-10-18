package net.medievalweapons.init;

import net.fabricmc.loader.api.FabricLoader;

public class CompatInit {
   public static final boolean isBetterCombatLoaded = FabricLoader.getInstance().isModLoaded("bettercombat");
   public static final boolean isLevelZLoaded = FabricLoader.getInstance().isModLoaded("levelz");

   public static void init() {
   }
}
