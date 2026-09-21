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
import net.satisfy.candlelight.client.model.CookingHatModel;
import net.satisfy.candlelight.client.model.FlowerCrownModel;
import net.satisfy.candlelight.core.item.CandlelightHatItem;
import net.satisfy.candlelight.core.registry.ArmorRegistry;

public class CandlelightHatRenderer implements ArmorRenderer {
    @Override
    public void render(PoseStack matrices, SubmitNodeCollector collector, ItemStack stack, HumanoidRenderState renderState, EquipmentSlot slot, int light, HumanoidModel<HumanoidRenderState> contextModel) {
        if (slot != EquipmentSlot.HEAD) return;
        if (!(stack.getItem() instanceof CandlelightHatItem hat)) return;

        Model hatModel = ArmorRegistry.getHatModel(hat, contextModel.head, contextModel);
        if (hatModel instanceof CookingHatModel cookingHatModel) {
            cookingHatModel.copyHead(contextModel.head);
            collector.submitModel(cookingHatModel, renderState, matrices, RenderTypes.entitySolid(hat.getHatTexture()), light, OverlayTexture.NO_OVERLAY, EntityRenderState.NO_OUTLINE, null);
        }

        Model crownModel = ArmorRegistry.getCrownModel(hat, contextModel.head, contextModel);
        if (crownModel instanceof FlowerCrownModel flowerCrownModel) {
            flowerCrownModel.copyHead(contextModel.head);
            collector.submitModel(flowerCrownModel, renderState, matrices, RenderTypes.entitySolid(hat.getHatTexture()), light, OverlayTexture.NO_OVERLAY, EntityRenderState.NO_OUTLINE, null);
        }
    }
}