package dev.jmiahman.hearthwind.client;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * Dehydration 1.3.6 HUD parity (upstream Globox1997/Dehydration
 * ThirstHudRender, GPL-3.0 studied, clean-room reimplementation):
 * - 10 droplets, 9x9, pitch 8, right-aligned at x = width/2 + 91,
 *   y = height - 49.
 * - Droplet N: empty background; full when 2N+1 < thirst; half when
 *   equal; murky-green set while the dehydration:thirst effect is active.
 * - Wobble: random y jitter on a thirst-scaled period (upstream uses its
 *   internal dehydration counter for the faster cadence; we scale the
 *   cadence off thirst only).
 * - Ice overlay: full/half droplets tinted by freezing scale while the
 *   player has frozen ticks, like upstream's frozen sprite.
 * - Hidden while riding a living vehicle (vanilla draws its hearts in the
 *   same band), in creative/spectator/invulnerable, and with the HUD off.
 * No flask icon and no underwater shift - upstream has neither.
 */
public final class ThirstHud implements HudElement {
    private static final Identifier EMPTY =
            Identifier.fromNamespaceAndPath("hearthwind", "hud/thirst_empty");
    private static final Identifier FULL_BLUE =
            Identifier.fromNamespaceAndPath("hearthwind", "hud/thirst_full");
    private static final Identifier HALF_BLUE =
            Identifier.fromNamespaceAndPath("hearthwind", "hud/thirst_half");
    private static final Identifier FULL_GREEN =
            Identifier.fromNamespaceAndPath("hearthwind", "hud/thirst_dirty_full");
    private static final Identifier HALF_GREEN =
            Identifier.fromNamespaceAndPath("hearthwind", "hud/thirst_dirty_half");
    private static final RandomSource RANDOM = RandomSource.create();

    public static final ThirstHud INSTANCE = new ThirstHud();

    private ThirstHud() {}

    public static void register() {
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.FOOD_BAR,
                Identifier.fromNamespaceAndPath("hearthwind", "thirst"),
                INSTANCE);
    }

    private static int heartCount(Entity entity) {
        if (!(entity instanceof LivingEntity living) || !entity.isAlive()) {
            return 0;
        }
        int i = (int) (living.getMaxHealth() + 0.5f) / 2;
        return Math.min(i, 30);
    }

    private static boolean isThirstEffectActive(Minecraft mc) {
        if (mc.player == null) {
            return false;
        }
        return mc.player.getActiveEffects().stream().anyMatch(e -> {
            var key = BuiltInRegistries.MOB_EFFECT.getKey(e.getEffect().value());
            return key != null && "dehydration".equals(key.getNamespace()) && "thirst".equals(key.getPath());
        });
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.player.isInvulnerable() || mc.player.isCreative()
                || mc.player.isSpectator() || mc.gui.hud.isHidden()) {
            return;
        }
        // Upstream skips the row entirely while riding a living vehicle.
        if (heartCount(mc.player.getVehicle()) != 0) {
            return;
        }

        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        int left = width / 2 + 91;
        int top = height - 49;

        int thirst = ClientThirstData.level(); // 0..20
        boolean isDirty = isThirstEffectActive(mc);
        Identifier fullSprite = isDirty ? FULL_GREEN : FULL_BLUE;
        Identifier halfSprite = isDirty ? HALF_GREEN : HALF_BLUE;

        int ticks = mc.player.tickCount;
        float freezeScale = mc.player.getPercentFrozen();
        boolean freezing = mc.player.getTicksFrozen() > 0 && freezeScale > 0.01f;
        int frozenTint = ((int) (freezeScale * 255f) << 24) | 0xB8E8FF;

        for (int i = 0; i < 10; i++) {
            int y = top;
            if (ticks % (thirst * 3 + 1) == 0 || ticks % (thirst * 8 + 3) == 0) {
                y = top + (RANDOM.nextInt(3) - 1);
            }
            int x = left - i * 8 - 9;

            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, EMPTY, x, y, 9, 9);
            boolean full = i * 2 + 1 < thirst;
            boolean half = i * 2 + 1 == thirst;
            if (full) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, fullSprite, x, y, 9, 9);
            } else if (half) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, halfSprite, x, y, 9, 9);
            }
            if (freezing && (full || half)) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                        full ? fullSprite : halfSprite, x, y, 9, 9, frozenTint);
            }
        }
    }
}
