package net.satisfy.herbalbrews.fabric.core.client;

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
import net.satisfy.herbalbrews.client.model.HatModel;
import net.satisfy.herbalbrews.core.items.HatItem;
import net.satisfy.herbalbrews.core.registry.ArmorRegistry;

public class HatArmorRenderer implements ArmorRenderer {
    @Override
    public void render(PoseStack matrices, SubmitNodeCollector collector, ItemStack stack, HumanoidRenderState renderState, EquipmentSlot slot, int light, HumanoidModel<HumanoidRenderState> contextModel) {
        if (slot != EquipmentSlot.HEAD) return;
        if (!(stack.getItem() instanceof HatItem hat)) return;
        if (!(ArmorRegistry.getHatModel(hat, contextModel.getHead()) instanceof HatModel model)) return;
        matrices.pushPose();
        matrices.scale(1.05F, 1.05F, 1.05F);
        collector.submitModel(model.asModel(), renderState, matrices, RenderTypes.entitySolid(hat.getHatTexture()), light, OverlayTexture.NO_OVERLAY, EntityRenderState.NO_OUTLINE, null);
        matrices.popPose();
    }
}
