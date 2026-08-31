package dev.jmiahman.hearthwind.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public final class NutrientsKey {
    private static final KeyMapping.Category CATEGORY =
            new KeyMapping.Category(Identifier.fromNamespaceAndPath("hearthwind", "main"));

    public static final KeyMapping openNutrients = new KeyMapping(
            "key.hearthwind.nutrients", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_N,
            CATEGORY);

    public static final KeyMapping openSkills = new KeyMapping(
            "key.hearthwind.skills", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K,
            CATEGORY);

    public static final KeyMapping openJobs = new KeyMapping(
            "key.hearthwind.jobs", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J,
            CATEGORY);

    public static final KeyMapping openParty = new KeyMapping(
            "key.hearthwind.party", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_P,
            CATEGORY);

    private NutrientsKey() {}

    public static void init() {
        KeyMappingHelper.registerKeyMapping(openNutrients);
        KeyMappingHelper.registerKeyMapping(openSkills);
        KeyMappingHelper.registerKeyMapping(openJobs);
        KeyMappingHelper.registerKeyMapping(openParty);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openNutrients.consumeClick()) {
                if (client.player != null) {
                    Minecraft.getInstance().setScreenAndShow(new NutrientsScreen());
                }
            }
            while (openSkills.consumeClick()) {
                if (client.player != null) {
                    Minecraft.getInstance().setScreenAndShow(
                            new SurvivalInfoScreen(SurvivalInfoScreen.Kind.SKILLS));
                }
            }
            while (openJobs.consumeClick()) {
                if (client.player != null) {
                    Minecraft.getInstance().setScreenAndShow(
                            new SurvivalInfoScreen(SurvivalInfoScreen.Kind.JOBS));
                }
            }
            while (openParty.consumeClick()) {
                if (client.player != null) {
                    Minecraft.getInstance().setScreenAndShow(
                            new SurvivalInfoScreen(SurvivalInfoScreen.Kind.PARTY));
                }
            }
        });
    }
}
