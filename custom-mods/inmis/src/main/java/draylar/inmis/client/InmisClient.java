package draylar.inmis.client;

import com.mojang.blaze3d.platform.InputConstants;
import draylar.inmis.Inmis;
import draylar.inmis.network.BackpackOpenPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class InmisClient implements ClientModInitializer {

    public static final KeyMapping OPEN_BACKPACK = new KeyMapping(
            "key.inmis.open_backpack",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            new KeyMapping.Category(Identifier.fromNamespaceAndPath(Inmis.MOD_ID, "main")));

    @Override
    public void onInitializeClient() {
        MenuScreens.register(Inmis.BACKPACK_MENU, BackpackScreen::new);
        KeyMappingHelper.registerKeyMapping(OPEN_BACKPACK);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_BACKPACK.consumeClick()) {
                if (client.player != null) {
                    ClientPlayNetworking.send(new BackpackOpenPacket());
                }
            }
        });
    }
}
