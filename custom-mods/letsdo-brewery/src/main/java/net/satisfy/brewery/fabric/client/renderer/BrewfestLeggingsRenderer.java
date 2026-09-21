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
import net.satisfy.brewery.client.model.BrewfestLeggingsModel;
import net.satisfy.brewery.core.item.BrewfestLegsItem;
import net.satisfy.brewery.core.registry.ArmorRegistry;

public class BrewfestLeggingsRenderer implements ArmorRenderer {
    @Override
    public void render(PoseStack matrices, SubmitNodeCollector collector, ItemStack stack, HumanoidRenderState renderState, EquipmentSlot slot, int light, HumanoidModel<HumanoidRenderState> contextModel) {
        if (slot != EquipmentSlot.LEGS) return;
        if (stack.getItem() instanceof BrewfestLegsItem leggings
                && ArmorRegistry.getLeggingsModel(leggings, contextModel.rightLeg, contextModel.leftLeg, contextModel) instanceof BrewfestLeggingsModel model) {
            matrices.pushPose();
            matrices.scale(1.08F, 1.08F, 1.08F);
            matrices.translate(0F, -0.095F, 0F);
            collector.submitModel(model.asModel(), renderState, matrices, RenderTypes.entitySolid(leggings.getLeggingsTexture()), light, OverlayTexture.NO_OVERLAY, EntityRenderState.NO_OUTLINE, null);
            matrices.popPose();
        }
    }
}