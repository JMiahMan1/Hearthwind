package net.dungeonz.util;

import com.mojang.blaze3d.systems.RenderSystem;

import net.dungeonz.access.InGameHudAccess;
import net.dungeonz.init.ConfigInit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_9779;

@Environment(EnvType.CLIENT)
public class RenderHelper {

    public static void renderDungeonCountdown(class_332 context, class_9779 tickDelta) {
        class_310 client = class_310.method_1551();
        if (((InGameHudAccess) client.field_1705).getDungeonCountdownRemainingTicks() > 0) {
            RenderSystem.enableBlend();

            class_2561 text = class_2561.method_43469("hud.dungeonz.dungeon_countdown", ((InGameHudAccess) client.field_1705).getDungeonCountdownTicks() / 20);
            context.method_51448().method_22903();
            context.method_51448().method_46416(context.method_51421() / 2 - client.field_1772.method_27525(text) / 2 + ConfigInit.CONFIG.countdownX,
                    context.method_51443() / 2 + ConfigInit.CONFIG.countdownY, 0.0f);
            context.method_51448().method_22905(ConfigInit.CONFIG.countdownSize, ConfigInit.CONFIG.countdownSize, ConfigInit.CONFIG.countdownSize);

            context.method_51422(1.0f, 1.0f, 1.0f, (float) ((InGameHudAccess) client.field_1705).getDungeonCountdownRemainingTicks() / 18.0f);
            context.method_27535(client.field_1772, text, 0, 0, 0xFFFFFF);
            context.method_51422(1.0f, 1.0f, 1.0f, 1.0f);
            context.method_51448().method_22909();
            RenderSystem.disableBlend();
        }
    }

}
