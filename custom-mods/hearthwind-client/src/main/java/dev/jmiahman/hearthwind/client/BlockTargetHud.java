package dev.jmiahman.hearthwind.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * In-world block targeting HUD (Aged / LevelZ / WTHIT parity):
 * When pointing crosshair at a block, displays its name and required Mining/Use skill level.
 */
@Environment(EnvType.CLIENT)
public final class BlockTargetHud implements HudElement {
    public static final BlockTargetHud INSTANCE = new BlockTargetHud();

    private BlockTargetHud() {}

    public static void register() {
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.CROSSHAIR,
                Identifier.fromNamespaceAndPath("hearthwind", "block_target_hud"),
                INSTANCE);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.hitResult == null || mc.player.isSpectator()) {
            return;
        }

        if (mc.hitResult.getType() != HitResult.Type.BLOCK || !(mc.hitResult instanceof BlockHitResult bhr)) {
            return;
        }

        BlockState state = mc.level.getBlockState(bhr.getBlockPos());
        if (state.isAir()) {
            return;
        }

        ClientSkillGates.Requirement req = ClientSkillGates.getBreakRequirement(state.getBlock());
        if (req == null) {
            req = ClientSkillGates.getUseRequirement(state.getBlock());
        }

        if (req == null) {
            return;
        }

        Font font = mc.font;
        int screenW = graphics.guiWidth();
        int screenH = graphics.guiHeight();

        int playerLvl = ClientSkillData.knownLevels().getOrDefault(req.skill(), 0);
        boolean unlocked = playerLvl >= req.level() || mc.player.getAbilities().instabuild;

        String skillCap = req.skill().substring(0, 1).toUpperCase() + req.skill().substring(1);
        String text = unlocked
                ? "§a✔ " + skillCap + " Level " + req.level()
                : "§c✖ Requires " + skillCap + " Level " + req.level();

        int textW = font.width(text);
        int x = screenW / 2 - textW / 2;
        int y = screenH / 2 + 14; // cleanly below crosshair

        // Subtle dark backdrop
        graphics.fill(x - 3, y - 2, x + textW + 3, y + 10, 0x90000000);
        graphics.text(font, text, x, y, 0xFFFFFFFF, true);
    }
}
