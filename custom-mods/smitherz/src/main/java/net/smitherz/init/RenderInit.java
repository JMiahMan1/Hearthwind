package net.smitherz.init;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;
import net.libz.registry.TabRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.GrindstoneScreen;
import net.minecraft.client.gui.screens.inventory.SmithingScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.smitherz.screen.GrinderScreen;
import net.smitherz.screen.SmitherScreen;
import net.smitherz.screen.widget.GrinderTab;
import net.smitherz.screen.widget.GrindstoneTab;
import net.smitherz.screen.widget.SmitherTab;
import net.smitherz.screen.widget.SmithingTab;
import net.smitherz.tooltip.SmitherTooltipComponent;
import net.smitherz.tooltip.SmitherTooltipData;

@Environment(EnvType.CLIENT)
public class RenderInit {
   private static final Identifier SMITHING_TAB_ICON = Identifier.parse("smitherz:textures/gui/smithing_tab_icon.png");
   private static final Identifier SMITHER_TAB_ICON = Identifier.parse("smitherz:textures/gui/smither_tab_icon.png");
   private static final Identifier GRINDSTONE_TAB_ICON = Identifier.parse("smitherz:textures/gui/grindstone_tab_icon.png");
   private static final Identifier GRINDER_TAB_ICON = Identifier.parse("smitherz:textures/gui/grinder_tab_icon.png");

   public static void init() {
      MenuScreens.register(ScreenInit.SMITHER_SCREEN_HANDLER_TYPE, SmitherScreen::new);
      MenuScreens.register(ScreenInit.GRINDER_SCREEN_HANDLER_TYPE, GrinderScreen::new);
      TabRegistry.registerOtherTab(
         new SmithingTab(Component.translatable("container.upgrade"), SMITHING_TAB_ICON, 0, SmithingScreen.class), SmithingScreen.class
      );
      TabRegistry.registerOtherTab(new SmitherTab(Component.translatable("container.link"), SMITHER_TAB_ICON, 1, SmitherScreen.class), SmithingScreen.class);
      TabRegistry.registerOtherTab(
         new GrindstoneTab(Component.translatable("container.grindstone_title"), GRINDSTONE_TAB_ICON, 0, GrindstoneScreen.class), GrindstoneScreen.class
      );
      TabRegistry.registerOtherTab(new GrinderTab(Component.translatable("container.unlink"), GRINDER_TAB_ICON, 1, GrinderScreen.class), GrindstoneScreen.class);
      ClientTooltipComponentCallback.EVENT
         .register((ClientTooltipComponentCallback)data -> data instanceof SmitherTooltipData ? new SmitherTooltipComponent((SmitherTooltipData)data) : null);
   }
}
