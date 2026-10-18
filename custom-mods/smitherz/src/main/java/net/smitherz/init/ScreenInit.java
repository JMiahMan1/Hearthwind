package net.smitherz.init;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.smitherz.SmitherzMain;
import net.smitherz.screen.GrinderScreenHandler;
import net.smitherz.screen.SmitherScreenHandler;

public class ScreenInit {
   public static net.minecraft.world.inventory.MenuType<SmitherScreenHandler> SMITHER_SCREEN_HANDLER_TYPE;
   public static net.minecraft.world.inventory.MenuType<GrinderScreenHandler> GRINDER_SCREEN_HANDLER_TYPE;

   public static void init() {
      SMITHER_SCREEN_HANDLER_TYPE = (MenuType)Registry.register(
         BuiltInRegistries.MENU,
         SmitherzMain.identifierOf("smither"),
         new MenuType((syncId, inventory) -> new SmitherScreenHandler(syncId, inventory, ContainerLevelAccess.NULL), FeatureFlags.VANILLA_SET)
      );
      GRINDER_SCREEN_HANDLER_TYPE = (MenuType)Registry.register(
         BuiltInRegistries.MENU,
         SmitherzMain.identifierOf("grinder"),
         new MenuType((syncId, inventory) -> new GrinderScreenHandler(syncId, inventory, ContainerLevelAccess.NULL), FeatureFlags.VANILLA_SET)
      );
   }
}
