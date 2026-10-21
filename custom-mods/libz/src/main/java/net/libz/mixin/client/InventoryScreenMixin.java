package net.libz.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.libz.api.Tab;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Marks the vanilla inventory as a LibZ tab host so {@code DrawTabHelper} can
 * resolve {@code instanceof Tab} and use the inventory tab list. The tab strip
 * itself is drawn and clicked by {@link HandledScreenMixin}, which applies to
 * every {@code AbstractContainerScreen}; 26.2's {@code InventoryScreen} no
 * longer declares {@code mouseClicked}, so this mixin must not inject there.
 */
@Environment(EnvType.CLIENT)
@Mixin({InventoryScreen.class})
public interface InventoryScreenMixin extends Tab {
}
