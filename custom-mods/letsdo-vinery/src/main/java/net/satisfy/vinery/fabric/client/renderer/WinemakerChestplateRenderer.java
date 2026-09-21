package net.satisfy.vinery.fabric.client.renderer;

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
import net.satisfy.vinery.core.registry.ArmorRegistryClient;
import net.satisfy.vinery.client.model.WinemakerChestplateModel;
import net.satisfy.vinery.core.item.WinemakerChestItem;
public class WinemakerChestplateRenderer implements ArmorRenderer {
    @Override
    public void render(PoseStack matrices, SubmitNodeCollector collector, ItemStack stack, HumanoidRenderState renderState, EquipmentSlot slot, int light, HumanoidModel<HumanoidRenderState> contextModel) {
        if (stack.getItem() instanceof WinemakerChestItem chestplate) {
            WinemakerChestplateModel model = ArmorRegistryClient.getChestplateModel(chestplate, contextModel.body, contextModel.leftArm, contextModel.rightArm, contextModel.leftLeg, contextModel.rightLeg);
            if (model == null) return;
            matrices.pushPose();
            matrices.scale(1.075F, 1.075F, 1.075F);
            collector.submitModel(model.asModel(), renderState, matrices, RenderTypes.entitySolid(chestplate.getChestplateTexture()), light, OverlayTexture.NO_OVERLAY, EntityRenderState.NO_OUTLINE, null);
            matrices.popPose();
        }
    }
}
