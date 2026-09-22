package io.wispforest.lavender.client;

import io.wispforest.lavender.Lavender;
import io.wispforest.lavender.structure.BlockStatePredicate;
import io.wispforest.lavender.structure.LavenderStructures;
import io.wispforest.lavender.structure.StructureTemplate;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.hud.Hud;
import io.wispforest.owo.ui.util.Delta;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.BlockHitResult;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * v1 26.2: HUD completion bars + pending placement only.
 * World-space ghost-block overlay disabled (MultiBufferSource / old framebuffer blit removed).
 */
public class StructureOverlayRenderer {

    private static final Map<BlockPos, OverlayEntry> ACTIVE_OVERLAYS = new HashMap<>();
    private static @Nullable OverlayEntry PENDING_OVERLAY = null;

    private static final Identifier HUD_COMPONENT_ID = Lavender.id("structure_overlay");
    private static final Identifier BARS_TEXTURE = Lavender.id("textures/gui/structure_overlay_bars.png");

    public static void addPendingOverlay(Identifier structure) {
        PENDING_OVERLAY = new OverlayEntry(structure, Rotation.NONE);
    }

    public static void addOverlay(BlockPos anchorPoint, Identifier structure, Rotation rotation) {
        ACTIVE_OVERLAYS.put(anchorPoint, new OverlayEntry(structure, rotation));
    }

    public static boolean isShowingOverlay(Identifier structure) {
        if (PENDING_OVERLAY != null && structure.equals(PENDING_OVERLAY.structureId)) return true;

        for (var entry : ACTIVE_OVERLAYS.values()) {
            if (structure.equals(entry.structureId)) return true;
        }

        return false;
    }

    public static void removeAllOverlays(Identifier structure) {
        if (PENDING_OVERLAY != null && structure.equals(PENDING_OVERLAY.structureId)) {
            PENDING_OVERLAY = null;
        }

        ACTIVE_OVERLAYS.values().removeIf(entry -> structure.equals(entry.structureId));
    }

    public static int getLayerRestriction(Identifier structure) {
        for (var entry : ACTIVE_OVERLAYS.values()) {
            if (entry.visibleLayer == -1) continue;
            return entry.visibleLayer;
        }

        return -1;
    }

    public static void restrictVisibleLayer(Identifier structure, int visibleLayer) {
        if (PENDING_OVERLAY != null && structure.equals(PENDING_OVERLAY.structureId)) {
            PENDING_OVERLAY.visibleLayer = visibleLayer;
        }

        for (var entry : ACTIVE_OVERLAYS.values()) {
            if (!structure.equals(entry.structureId)) continue;
            entry.visibleLayer = visibleLayer;
        }
    }

    public static void clearOverlays() {
        ACTIVE_OVERLAYS.clear();
    }

    public static void rotatePending(boolean clockwise) {
        if (PENDING_OVERLAY == null) return;
        PENDING_OVERLAY.rotation = PENDING_OVERLAY.rotation.getRotated(Rotation.CLOCKWISE_90);
    }

    public static boolean hasPending() {
        return PENDING_OVERLAY != null;
    }

    public static void initialize() {
        Hud.add(HUD_COMPONENT_ID, () -> UIContainers.verticalFlow(Sizing.content(), Sizing.content()).gap(15).positioning(Positioning.relative(5, 100)));

        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!(Hud.getComponent(HUD_COMPONENT_ID) instanceof FlowLayout hudComponent)) return;
            if (client.level == null || client.player == null) return;

            hudComponent.<FlowLayout>configure(layout -> {
                layout.clearChildren().padding(Insets.bottom((client.getWindow().getGuiScaledWidth() - 182) / 2 < 200 ? 50 : 5));

                ACTIVE_OVERLAYS.keySet().removeIf(anchor -> {
                    var entry = ACTIVE_OVERLAYS.get(anchor);
                    var structure = entry.fetchStructure();
                    if (structure == null) return true;

                    var hasInvalidBlock = new MutableBoolean();

                    var valid = structure.countValidStates(client.level, anchor, entry.rotation, BlockStatePredicate.MatchCategory.NON_AIR);
                    var total = structure.predicatesOfType(BlockStatePredicate.MatchCategory.NON_AIR);
                    var complete = structure.validate(client.level, anchor, entry.rotation);

                    if (entry.decayTime >= 0) valid = total;

                    int barTextureOffset = 0;
                    if (hasInvalidBlock.booleanValue()) barTextureOffset = 20;
                    if (complete) barTextureOffset = 10;

                    var partialTick = 0f;
                    var lastFrame = 1f;

                    entry.visualCompleteness += Delta.compute(entry.visualCompleteness, valid / (float) total, lastFrame);
                    layout.child(UIContainers.verticalFlow(Sizing.content(), Sizing.content())
                        .child(UIComponents.label(Component.translatable("text.lavender.structure_hud.completion", Component.translatable("structure." + entry.structureId.toString().replace(':', '.')), valid, total)).shadow(true))
                        .child(UIContainers.verticalFlow(Sizing.content(), Sizing.content())
                            .child(UIComponents.texture(BARS_TEXTURE, 0, barTextureOffset, 182, 5, 256, 48))
                            .child(UIComponents.texture(BARS_TEXTURE, 0, barTextureOffset + 5, Math.round(182 * entry.visualCompleteness), 5, 256, 48).positioning(Positioning.absolute(0, 0)))
                            .child(UIComponents.texture(BARS_TEXTURE, 0, 30, 182, 5, 256, 48).blend(true).positioning(Positioning.absolute(0, 0))))
                        .gap(2)
                        .horizontalAlignment(HorizontalAlignment.CENTER)
                        .margins(Insets.bottom((int) (Easing.CUBIC.apply((Math.max(0, entry.decayTime - 30) + partialTick) / 20f) * -32))));

                    if (entry.decayTime < 0 && complete) {
                        entry.decayTime = 0;
                        client.player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1f, 1f);
                    } else if (entry.decayTime >= 0) {
                        entry.decayTime += lastFrame;
                    }

                    return entry.decayTime >= 50;
                });
            });
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (PENDING_OVERLAY == null) return InteractionResult.PASS;

            var structure = PENDING_OVERLAY.fetchStructure();
            if (structure == null) {
                PENDING_OVERLAY = null;
                return InteractionResult.PASS;
            }

            var targetPos = hitResult.getBlockPos().offset(getPendingOffset(structure));
            if (!player.isShiftKeyDown()) targetPos = targetPos.relative(hitResult.getDirection());

            ACTIVE_OVERLAYS.put(targetPos, PENDING_OVERLAY);
            PENDING_OVERLAY = null;

            player.swing(hand);
            return InteractionResult.FAIL;
        });
    }

    private static Vec3i getPendingOffset(StructureTemplate structure) {
        if (PENDING_OVERLAY == null) return Vec3i.ZERO;

        return switch (PENDING_OVERLAY.rotation) {
            case NONE -> new Vec3i(-structure.anchor().getX(), -structure.anchor().getY(), -structure.anchor().getZ());
            case CLOCKWISE_90 -> new Vec3i(-structure.anchor().getZ(), -structure.anchor().getY(), -structure.anchor().getX());
            case CLOCKWISE_180 -> new Vec3i(-structure.xSize + structure.anchor().getX() + 1, -structure.anchor().getY(), -structure.zSize + structure.anchor().getZ() + 1);
            case COUNTERCLOCKWISE_90 -> new Vec3i(-structure.zSize + structure.anchor().getZ() + 1, -structure.anchor().getY(), -structure.xSize + structure.anchor().getX() + 1);
        };
    }

    private static class OverlayEntry {

        public final Identifier structureId;

        public Rotation rotation;
        public int visibleLayer = -1;

        public float decayTime = -1;
        public float visualCompleteness = 0f;

        public OverlayEntry(Identifier structureId, Rotation rotation) {
            this.structureId = structureId;
            this.rotation = rotation;
        }

        public @Nullable StructureTemplate fetchStructure() {
            return LavenderStructures.get(this.structureId);
        }
    }
}
