package net.dungeonz.util;

import net.dungeonz.access.InGameHudAccess;
import net.dungeonz.init.ConfigInit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.DeltaTracker;

@Environment(EnvType.CLIENT)
public class RenderHelper {

    public static void renderDungeonCountdown(GuiGraphicsExtractor context, DeltaTracker tickDelta) {
        Minecraft client = Minecraft.getInstance();
        if (((InGameHudAccess) client.gui).getDungeonCountdownRemainingTicks() > 0) {
            // 26.x: no global blend state (RenderSystem.enableBlend is gone) and no setColor on
            // the extractor - text alpha is baked into the ARGB color int instead.
            Component text = Component.translatable("hud.dungeonz.dungeon_countdown", ((InGameHudAccess) client.gui).getDungeonCountdownTicks() / 20);
            context.pose().pushMatrix();
            context.pose().translate(context.guiWidth() / 2 - client.font.width(text) / 2 + ConfigInit.CONFIG.countdownX,
                    context.guiHeight() / 2 + ConfigInit.CONFIG.countdownY);
            context.pose().scale(ConfigInit.CONFIG.countdownSize, ConfigInit.CONFIG.countdownSize);

            int alpha = (int) (255 * ((InGameHudAccess) client.gui).getDungeonCountdownRemainingTicks() / 18.0f);
            context.text(client.font, text, 0, 0, (Math.min(255, Math.max(0, alpha)) << 24) | 0xFFFFFF);
            context.pose().popMatrix();
        }
    }

}
