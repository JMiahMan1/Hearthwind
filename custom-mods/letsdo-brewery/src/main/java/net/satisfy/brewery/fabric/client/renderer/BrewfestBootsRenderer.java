package net.satisfy.brewery.fabric.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.satisfy.brewery.client.model.BrewfestBootsModel;
import net.satisfy.brewery.core.item.BrewfestBootsItem;
import net.satisfy.brewery.core.registry.ArmorRegistry;

public class BrewfestBootsRenderer implements ArmorRenderer {
    @Override
    public void render(PoseStack matrices, SubmitNodeCollector collector, ItemStack stack, HumanoidRenderState renderState, EquipmentSlot slot, int light, HumanoidModel<HumanoidRenderState> contextModel) {
        if (slot != EquipmentSlot.FEET) return;
        if (stack.getItem() instanceof BrewfestBootsItem boots
                && ArmorRegistry.getBootsModel(boots, contextModel.rightLeg, contextModel.leftLeg, contextModel) instanceof BrewfestBootsModel model) {
            matrices.pushPose();
            matrices.scale(1.08F, 1.08F, 1.08F);
            matrices.translate(0F, -0.125F, 0F);
            collector.submitModel(model.asModel(), renderState, matrices, RenderTypes.entitySolid(boots.getBootsTexture()), light, OverlayTexture.NO_OVERLAY, EntityRenderState.NO_OUTLINE, null);
            matrices.popPose();
        }
    }
}