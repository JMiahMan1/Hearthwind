package net.dungeonz.init;

import net.dungeonz.block.render.DungeonGateRenderer;
import net.dungeonz.block.render.DungeonPortalRenderer;
import net.dungeonz.block.render.DungeonSpawnerRenderer;
import net.dungeonz.block.screen.DungeonPortalScreen;
import net.dungeonz.util.RenderHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class RenderInit {

    public static void init() {
        // 26.x: chunk section layers are automatic (texture alpha drives CUTOUT; opaque blocks
        // stay SOLID), so no runtime layer registration exists or is needed. dungeon_spawner's
        // texture has transparent pixels (auto-CUTOUT); dungeon_gate is opaque (SOLID).
        // (BlockRenderLayerMap was removed from fabric-api; RenderType is now ChunkSectionLayer.)

        BlockEntityRenderers.register(BlockInit.DUNGEON_PORTAL_ENTITY, DungeonPortalRenderer::new);
        BlockEntityRenderers.register(BlockInit.DUNGEON_SPAWNER_ENTITY, DungeonSpawnerRenderer::new);
        BlockEntityRenderers.register(BlockInit.DUNGEON_GATE_ENTITY, DungeonGateRenderer::new);

        MenuScreens.register(BlockInit.PORTAL, DungeonPortalScreen::new);

        // 26.x: the dungeon compass needle is data-driven (assets/dungeonz/items/dungeon_compass.json
        // uses the vanilla minecraft:compass property with the lodestone target); DungeonCompassItem
        // mirrors the tracked dungeon pos into the lodestone_tracker component, so no client
        // ItemProperties registration is needed (CompassItemPropertyFunction is gone in 26.x).
        // 26.x: HudRenderCallback is gone, replaced by HUD elements. The dungeon countdown
        // overlay registers last so it draws over everything, matching the old callback timing.
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("dungeonz", "dungeon_countdown"), RenderHelper::renderDungeonCountdown);
    }

}
