package dev.jmiahman.hearthwind.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.resources.Identifier;

/**
 * Skill level-up toast: displays centered near the top of the screen for 3 seconds
 * when a skill levels up. Styled like an RPG achievement banner.
 */
public final class SkillToast implements HudElement {
    public static final SkillToast INSTANCE = new SkillToast();

    private SkillToast() {}

    public static void register() {
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.HOTBAR,
                Identifier.fromNamespaceAndPath("hearthwind", "skilltoast"),
                INSTANCE);
    }

    private static Item getSkillIcon(String skill) {
        if (skill == null) return Items.EXPERIENCE_BOTTLE;
        return switch (skill.toLowerCase()) {
            case "farming" -> Items.WHEAT;
            case "mining" -> Items.IRON_PICKAXE;
            case "smithing" -> Items.ANVIL;
            case "strength" -> Items.IRON_SWORD;
            case "agility" -> Items.LEATHER_BOOTS;
            case "defense" -> Items.IRON_CHESTPLATE;
            case "health" -> Items.GOLDEN_APPLE;
            case "stamina" -> Items.FEATHER;
            case "luck" -> Items.RABBIT_FOOT;
            case "archery" -> Items.BOW;
            case "alchemy" -> Items.BREWING_STAND;
            case "trade" -> Items.EMERALD;
            default -> Items.EXPERIENCE_BOTTLE;
        };
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !ClientSkillData.isActive()) {
            return;
        }

        String skillId = ClientSkillData.skillId();
        String skillName = formatSkillName(skillId);
        int lvl = ClientSkillData.level();
        String header = "★ Skill Level Up! ★";
        String mainText = skillName + "  Level " + lvl;
        float alpha = ClientSkillData.alpha();
        if (alpha <= 0.01f) {
            return;
        }

        Font font = mc.font;
        int width = graphics.guiWidth();
        int mainW = font.width(mainText);
        int headerW = font.width(header);
        int boxW = Math.max(headerW, mainW + 24) + 24;
        int boxH = 30;
        int x = (width - boxW) / 2;
        int y = 24;

        int alphaInt = ((int) (alpha * 255f)) & 0xFF;
        if (alphaInt == 0) return;

        int border = (alphaInt << 24) | 0xD4AF37; // Gold
        int bg = (Math.min(alphaInt, 0xD0) << 24) | 0x1A1A1A;
        int headerColor = (alphaInt << 24) | 0xFFAA00;
        int textColor = (alphaInt << 24) | 0xFFFFFF;

        // Banner box
        graphics.fill(x, y, x + boxW, y + boxH, border);
        graphics.fill(x + 1, y + 1, x + boxW - 1, y + boxH - 1, bg);

        // Skill icon
        graphics.item(new ItemStack(getSkillIcon(skillId)), x + 6, y + 7);

        // Text
        graphics.text(font, header, x + (boxW - headerW) / 2, y + 4, headerColor, true);
        graphics.text(font, mainText, x + 26, y + 16, textColor, true);
    }

    private static String formatSkillName(String id) {
        if (id == null || id.isEmpty()) return "Skill";
        return id.substring(0, 1).toUpperCase() + id.substring(1).toLowerCase();
    }
}
