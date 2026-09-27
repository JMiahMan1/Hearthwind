package net.backslot.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.backslot.BackSlot;
import net.backslot.network.SwitchSlotPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

/**
 * G swaps the held item with the back slot; holding Shift swaps the belt
 * (upstream uses Amecs' G / G+Shift bindings - we read the modifier
 * ourselves so no extra key-mapping library is needed).
 */
@Environment(EnvType.CLIENT)
public class BackSlotClient implements ClientModInitializer {

    public static final KeyMapping SWITCH_KEY = new KeyMapping(
            "key.backslot.switch",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            new KeyMapping.Category(Identifier.fromNamespaceAndPath(BackSlot.MOD_ID, "main")));

    @Override
    public void onInitializeClient() {
        KeyMappingHelper.registerKeyMapping(SWITCH_KEY);
        BackSlotHud.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (SWITCH_KEY.consumeClick()) {
                if (client.player == null || client.getWindow() == null) {
                    continue;
                }
                boolean shift = InputConstants.isKeyDown(client.getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT)
                        || InputConstants.isKeyDown(client.getWindow(), GLFW.GLFW_KEY_RIGHT_SHIFT);
                ClientPlayNetworking.send(new SwitchSlotPayload(
                        shift ? BackSlot.BELT_SLOT : BackSlot.BACK_SLOT));
            }
        });
    }
}
