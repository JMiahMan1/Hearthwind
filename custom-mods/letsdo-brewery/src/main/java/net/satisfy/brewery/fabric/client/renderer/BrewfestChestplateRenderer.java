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
import net.satisfy.brewery.client.model.BrewfestChestplateModel;
import net.satisfy.brewery.core.item.BrewfestChestItem;
import net.satisfy.brewery.core.registry.ArmorRegistry;

public class BrewfestChestplateRenderer implements ArmorRenderer {
    @Override
    public void render(PoseStack matrices, SubmitNodeCollector collector, ItemStack stack, HumanoidRenderState renderState, EquipmentSlot slot, int light, HumanoidModel<HumanoidRenderState> contextModel) {
        if (slot != EquipmentSlot.CHEST) return;
        if (stack.getItem() instanceof BrewfestChestItem chestplate
                && ArmorRegistry.getChestplateModel(chestplate, contextModel.body, contextModel.leftArm, contextModel.rightArm, contextModel.leftLeg, contextModel.rightLeg, contextModel) instanceof BrewfestChestplateModel model) {
            matrices.pushPose();
            matrices.scale(1.075F, 1.075F, 1.075F);
            collector.submitModel(model.asModel(), renderState, matrices, RenderTypes.entitySolid(chestplate.getChestplateTexture()), light, OverlayTexture.NO_OVERLAY, EntityRenderState.NO_OUTLINE, null);
            matrices.popPose();
        }
    }
}