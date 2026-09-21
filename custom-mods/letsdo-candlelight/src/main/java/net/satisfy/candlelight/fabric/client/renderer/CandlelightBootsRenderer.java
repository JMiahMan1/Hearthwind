package net.satisfy.candlelight.fabric.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.satisfy.candlelight.core.item.CandlelightBootsItem;
import net.satisfy.candlelight.core.registry.ArmorRegistry;

public class CandlelightBootsRenderer implements ArmorRenderer {
    @Override
    public void render(PoseStack matrices, SubmitNodeCollector collector, ItemStack stack, HumanoidRenderState renderState, EquipmentSlot slot, int light, HumanoidModel<HumanoidRenderState> contextModel) {
        if (slot != EquipmentSlot.FEET) return;
        if (!(stack.getItem() instanceof CandlelightBootsItem candlelightBootsItem)) return;

        Model model = ArmorRegistry.getBootsModel(candlelightBootsItem, contextModel.rightLeg, contextModel.leftLeg, contextModel);

        Identifier baseTexture = candlelightBootsItem.getBootsTexture();
        String texturePath = baseTexture.getPath();
        if (!texturePath.startsWith("textures/")) texturePath = "textures/" + texturePath;
        if (!texturePath.endsWith(".png")) texturePath = texturePath + ".png";
        Identifier texture = Identifier.fromNamespaceAndPath(baseTexture.getNamespace(), texturePath);

        collector.submitModel(model, renderState, matrices, RenderTypes.entitySolid(texture), light, OverlayTexture.NO_OVERLAY, EntityRenderState.NO_OUTLINE, null);
    }
}