package net.dungeonz.block.render;

import net.dungeonz.block.entity.DungeonPortalEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_5614;
import net.minecraft.class_840;

@Environment(EnvType.CLIENT)
public class DungeonPortalRenderer extends class_840<DungeonPortalEntity> {

    public DungeonPortalRenderer(class_5614.class_5615 ctx) {
        super(ctx);
    }

    @Override
    public void render(DungeonPortalEntity endPortalBlockEntity, float f, class_4587 matrixStack, class_4597 vertexConsumerProvider, int i, int j) {
        super.method_3591(endPortalBlockEntity, f, matrixStack, vertexConsumerProvider, i, j);
    }

    @Override
    protected float method_3594() {
        return 1.0f;
    }

    @Override
    protected float method_35793() {
        return 0.0f;
    }

    @Override
    public int method_33893() {
        return 256;
    }

}
