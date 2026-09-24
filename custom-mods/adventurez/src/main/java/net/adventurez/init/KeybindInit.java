package net.adventurez.init;

import org.lwjgl.glfw.GLFW;

import net.adventurez.AdventureMain;
import net.adventurez.entity.DragonEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;

@Environment(EnvType.CLIENT)
public class KeybindInit {

    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(AdventureMain.identifierOf("keybind"));

    public static KeyMapping dragonFlyDownKeyBind;
    public static KeyMapping dragonFireBreathBind;

    public static void init() {
        // Keybinds
        dragonFlyDownKeyBind = new KeyMapping("key.adventurez.dragonflydown", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT,
                CATEGORY);
        dragonFireBreathBind = new KeyMapping("key.adventurez.dragonfirebreath", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_GRAVE_ACCENT,
                CATEGORY);
        // Callback
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (dragonFlyDownKeyBind.isDown()) {
                DragonEntity.flyDragonDown(client.player, dragonFlyDownKeyBind.saveString());
                return;
            } else if (dragonFireBreathBind.consumeClick()) {
                DragonEntity.dragonFireBreath(client.player);
                return;
            }
        });
    }

}
