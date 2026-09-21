package net.satisfy.meadow.fabric.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.satisfy.meadow.client.model.FurLeggingsModel;
import net.satisfy.meadow.core.item.FurLegsItem;
import net.satisfy.meadow.core.registry.ArmorRegistry;

public class FurLeggingsRenderer implements ArmorRenderer {

    @Override
    public void render(PoseStack matrices, SubmitNodeCollector collector, ItemStack stack, HumanoidRenderState renderState, EquipmentSlot slot, int light, HumanoidModel<HumanoidRenderState> contextModel) {
        if (slot != EquipmentSlot.LEGS) return;
        if (!(stack.getItem() instanceof FurLegsItem furLegsItem)) return;

        FurLeggingsModel model = ArmorRegistry.getLeggingsModel(furLegsItem, contextModel.rightLeg, contextModel.leftLeg);

        Identifier baseTexture = furLegsItem.getLeggingsTexture();
        String texturePath = baseTexture.getPath();
        if (!texturePath.startsWith("textures/")) texturePath = "textures/" + texturePath;
        if (!texturePath.endsWith(".png")) texturePath = texturePath + ".png";
        Identifier texture = Identifier.fromNamespaceAndPath(baseTexture.getNamespace(), texturePath);

        collector.submitModel(model, renderState, matrices, RenderTypes.entitySolid(texture), light, OverlayTexture.NO_OVERLAY, EntityRenderState.NO_OUTLINE, null);
    }
}
