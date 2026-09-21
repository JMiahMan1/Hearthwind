package net.satisfy.candlelight.fabric.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.satisfy.candlelight.core.item.CandlelightChestItem;
import net.satisfy.candlelight.core.registry.ArmorRegistry;

public class CandlelightChestplateRenderer implements ArmorRenderer {
    @Override
    public void render(PoseStack matrices, SubmitNodeCollector collector, ItemStack stack, HumanoidRenderState renderState, EquipmentSlot slot, int light, HumanoidModel<HumanoidRenderState> contextModel) {
        if (slot != EquipmentSlot.CHEST) return;
        if (!(stack.getItem() instanceof CandlelightChestItem candlelightChestItem)) return;

        Model model = ArmorRegistry.getChestplateModel(candlelightChestItem, contextModel.body, contextModel.leftArm, contextModel.rightArm, contextModel.leftLeg, contextModel.rightLeg, contextModel);

        collector.submitModel(model, renderState, matrices, RenderTypes.entitySolid(candlelightChestItem.getChestplateTexture()), light, OverlayTexture.NO_OVERLAY, EntityRenderState.NO_OUTLINE, null);
    }
}