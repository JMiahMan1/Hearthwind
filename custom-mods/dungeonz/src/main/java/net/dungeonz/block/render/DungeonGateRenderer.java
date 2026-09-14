package net.dungeonz.block.render;

import net.dungeonz.block.DungeonGateBlock;
import net.dungeonz.block.entity.DungeonGateEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1921;
import net.minecraft.class_1937;
import net.minecraft.class_2680;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_4608;
import net.minecraft.class_4696;
import net.minecraft.class_5614;
import net.minecraft.class_5819;
import net.minecraft.class_776;
import net.minecraft.class_827;

@Environment(EnvType.CLIENT)
public class DungeonGateRenderer<T extends DungeonGateEntity> implements class_827<T> {
    private final class_776 manager;

    public DungeonGateRenderer(class_5614.class_5615 ctx) {
        this.manager = ctx.method_32141();
    }

    @Override
    public void render(T gateBlockEntity, float f, class_4587 matrixStack, class_4597 vertexConsumerProvider, int i, int j) {
        class_1937 world = gateBlockEntity.method_10997();
        if (world == null) {
            return;
        }
        if (gateBlockEntity.method_11010().method_11654(DungeonGateBlock.ENABLED)) {
            class_2680 state = gateBlockEntity.getBlockState();
            class_1921 renderLayer = class_4696.method_23679(state);
            class_4588 vertexConsumer = vertexConsumerProvider.getBuffer(renderLayer);

            this.manager.method_3350().method_3374(world, this.manager.method_3349(state), state, gateBlockEntity.method_11016(), matrixStack, vertexConsumer, false, class_5819.method_43047(),
                    state.method_26190(gateBlockEntity.method_11016()), class_4608.field_21444);
        }
    }

}
