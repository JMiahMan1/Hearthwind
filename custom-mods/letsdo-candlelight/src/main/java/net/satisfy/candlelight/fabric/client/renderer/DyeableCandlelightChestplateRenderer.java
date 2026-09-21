package net.satisfy.candlelight.fabric.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.satisfy.candlelight.core.item.DyeableCandlelightArmorItem;
import net.satisfy.candlelight.core.registry.ArmorRegistry;

public class DyeableCandlelightChestplateRenderer implements ArmorRenderer {
    @Override
    public void render(PoseStack matrices, SubmitNodeCollector collector, ItemStack stack, HumanoidRenderState renderState, EquipmentSlot slot, int light, HumanoidModel<HumanoidRenderState> contextModel) {
        if (slot != EquipmentSlot.CHEST) return;
        if (!(stack.getItem() instanceof DyeableCandlelightArmorItem item)) return;

        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.contains("Visible") && !tag.getBooleanOr("Visible", true)) return;

        Model model = ArmorRegistry.getDressModel(item, contextModel.body, contextModel.leftArm, contextModel.rightArm, contextModel.leftLeg, contextModel.rightLeg, contextModel);
        if (model == null) return;

        final Identifier base = item.getTexture();
        final int packedColor = 0xFF000000 | item.getColor(stack);
        final Identifier overlay = item.getOverlayTexture();
        final boolean hasOverlay = overlay != null;

        collector.submitCustomGeometry(matrices, RenderTypes.entitySolid(base), new SubmitNodeCollector.CustomGeometryRenderer() {
            @Override
            public void render(PoseStack.Pose pose, VertexConsumer consumer) {
                model.renderToBuffer(matrices, consumer, light, OverlayTexture.NO_OVERLAY, packedColor);
                if (hasOverlay) {
                    model.renderToBuffer(matrices, consumer, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
                }
            }
        });
    }
}