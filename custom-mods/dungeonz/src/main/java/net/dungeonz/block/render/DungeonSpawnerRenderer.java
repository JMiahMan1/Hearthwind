package net.dungeonz.block.render;

import net.dungeonz.block.entity.DungeonSpawnerEntity;
import net.dungeonz.block.logic.DungeonSpawnerLogic;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_5614;
import net.minecraft.class_7833;
import net.minecraft.class_827;
import net.minecraft.class_898;

@Environment(EnvType.CLIENT)
public class DungeonSpawnerRenderer implements class_827<DungeonSpawnerEntity> {
    private final class_898 entityRenderDispatcher;

    public DungeonSpawnerRenderer(class_5614.class_5615 ctx) {
        this.entityRenderDispatcher = ctx.method_43334();
    }

    @Override
    public void render(DungeonSpawnerEntity dungeonSpawnerEntity, float f, class_4587 matrixStack, class_4597 vertexConsumerProvider, int i, int j) {
        matrixStack.method_22903();
        matrixStack.method_22904(0.5, 0.0, 0.5);
        DungeonSpawnerLogic dungeonSpawnerLogic = dungeonSpawnerEntity.getLogic();
        class_1297 entity = dungeonSpawnerLogic.getRenderedEntity(dungeonSpawnerEntity.method_10997());
        if (entity != null) {
            float g = 0.53125f;
            float h = Math.max(entity.method_17681(), entity.method_17682());
            if ((double) h > 1.0) {
                g /= h;
            }
            matrixStack.method_22904(0.0, 0.4f, 0.0);
            matrixStack.method_22907(
                    class_7833.field_40716.rotationDegrees((float) class_3532.method_16436((double) f, dungeonSpawnerLogic.randomParticleValueTwo(), dungeonSpawnerLogic.randomParticleValueOne()) * 10.0f));
            matrixStack.method_22904(0.0, -0.2f, 0.0);
            matrixStack.method_22907(class_7833.field_40714.rotationDegrees(-30.0f));
            matrixStack.method_22905(g, g, g);
            this.entityRenderDispatcher.method_3954(entity, 0.0, 0.0, 0.0, 0.0f, f, matrixStack, vertexConsumerProvider, i);
        }
        matrixStack.method_22909();
    }
}
