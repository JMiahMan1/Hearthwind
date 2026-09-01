package com.talhanation.smallships.world.inventory;

import com.talhanation.smallships.world.entity.ship.ContainerShip;
import com.talhanation.smallships.world.inventory.fabric.ModMenuTypesImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ModMenuTypes {
    public static final MenuType<ShipContainerMenu> SHIP_CONTAINER = getMenuType("ship_container");

    public static <T extends AbstractContainerMenu> MenuType<T> getMenuType(String id) {
        return ModMenuTypesImpl.getMenuType(id);
    }

    public static @Nullable ShipContainerMenu extendedShipContainerMenuTypeSupplier(int syncId, Inventory inventory, UUID shipUUID) {
        ContainerShip containerShip = inventory.player.level().getEntitiesOfClass(ContainerShip.class, new AABB(inventory.player.blockPosition()).inflate(16.0D), ship -> ship.getUUID().equals(shipUUID))
                .stream()
                .filter(Entity::isAlive)
                .findAny().orElse(null);
        if (containerShip == null) return null;

        if (containerShip.getContainerSize() != containerShip.getItemStacks().size()) containerShip.resizeContainer(containerShip.getContainerSize());

        return new ShipContainerMenu(ModMenuTypes.SHIP_CONTAINER, syncId, inventory, containerShip);
    }
}
