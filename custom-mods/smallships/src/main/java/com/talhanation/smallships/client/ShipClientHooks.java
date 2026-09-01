package com.talhanation.smallships.client;

import com.talhanation.smallships.config.SmallShipsConfig;
import com.talhanation.smallships.world.entity.ship.Ship;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/**
 * Client-only hooks used by common entity code. This class must never be
 * loaded on a dedicated server - every caller guards behind
 * level().isClientSide() so the JVM only resolves these references client-side.
 */
public final class ShipClientHooks {
    private static final Map<Ship, CameraType> PREVIOUS_CAMERA = new WeakHashMap<>();

    private ShipClientHooks() {}

    public static void onAddPassenger(Ship ship, Entity entity) {
        if (SmallShipsConfig.Client.shipGeneralCameraAutoThirdPerson.get() && Objects.equals(Minecraft.getInstance().player, entity)) {
            PREVIOUS_CAMERA.put(ship, Minecraft.getInstance().options.getCameraType());
            Minecraft.getInstance().options.setCameraType(CameraType.THIRD_PERSON_BACK);
        }
    }

    public static void onRemovePassenger(Ship ship, Entity entity) {
        if (SmallShipsConfig.Client.shipGeneralCameraAutoThirdPerson.get() && Objects.equals(Minecraft.getInstance().player, entity)) {
            CameraType previous = PREVIOUS_CAMERA.remove(ship);
            if (previous != null) Minecraft.getInstance().options.setCameraType(previous);
        }
    }

    public static boolean isClientDriver(Player player) {
        return player.equals(Minecraft.getInstance().player);
    }
}
