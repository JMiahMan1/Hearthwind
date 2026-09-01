package com.talhanation.smallships.client.gui.screens.inventory;

import com.talhanation.smallships.SmallShipsMod;
import com.talhanation.smallships.world.entity.ship.ContainerShip;
import com.talhanation.smallships.world.inventory.ShipContainerMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class ShipContainerScreen extends AbstractContainerScreen<ShipContainerMenu> {
    private static final Identifier RESOURCE_LOCATION = Identifier.fromNamespaceAndPath(SmallShipsMod.MOD_ID, "textures/gui/ship_inventory.png");
    private final ContainerShip containerShip;

    public ShipContainerScreen(ShipContainerMenu shipContainerMenu, Inventory inventory, Component component) {
        super(shipContainerMenu, inventory, component, 256, 114 + shipContainerMenu.getRowCount() * 18);
        this.inventoryLabelY = this.imageHeight - 94;
        this.containerShip = shipContainerMenu.getContainerShip();
    }

    @Override
    public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractContents(guiGraphics, mouseX, mouseY, partialTick);
    }
}
