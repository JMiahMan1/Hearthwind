package net.dungeonz.init;

import net.dungeonz.block.render.DungeonGateRenderer;
import net.dungeonz.block.render.DungeonPortalRenderer;
import net.dungeonz.block.render.DungeonSpawnerRenderer;
import net.dungeonz.block.screen.DungeonPortalScreen;
import net.dungeonz.item.DungeonCompassItem;
import net.dungeonz.util.RenderHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import net.minecraft.class_3929;
import net.minecraft.class_5272;
import net.minecraft.class_5616;
import net.minecraft.class_7391;

@Environment(EnvType.CLIENT)
public class RenderInit {

    public static void init() {
        BlockRenderLayerMap.INSTANCE.putBlock(BlockInit.DUNGEON_SPAWNER, class_1921.method_23581());
        BlockRenderLayerMap.INSTANCE.putBlock(BlockInit.DUNGEON_GATE, class_1921.method_23581());

        class_5616.method_32144(BlockInit.DUNGEON_PORTAL_ENTITY, DungeonPortalRenderer::new);
        class_5616.method_32144(BlockInit.DUNGEON_SPAWNER_ENTITY, DungeonSpawnerRenderer::new);
        class_5616.method_32144(BlockInit.DUNGEON_GATE_ENTITY, DungeonGateRenderer::new);

        class_3929.method_17542(BlockInit.PORTAL, DungeonPortalScreen::new);

        class_5272.method_27879(ItemInit.DUNGEON_COMPASS, class_2960.method_60654("angle"), new class_7391((world, stack, entity) -> {
            return DungeonCompassItem.createGlobalDungeonStructurePos(world, stack);
        }));
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            RenderHelper.renderDungeonCountdown(drawContext, tickDelta);
        });
    }

}
