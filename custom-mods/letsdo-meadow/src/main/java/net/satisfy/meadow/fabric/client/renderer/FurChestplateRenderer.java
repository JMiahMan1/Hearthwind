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
import net.satisfy.meadow.client.model.FurChestplateModel;
import net.satisfy.meadow.core.item.FurChestItem;
import net.satisfy.meadow.core.registry.ArmorRegistry;

public class FurChestplateRenderer implements ArmorRenderer {
    @Override
    public void render(PoseStack matrices, SubmitNodeCollector collector, ItemStack stack, HumanoidRenderState renderState, EquipmentSlot slot, int light, HumanoidModel<HumanoidRenderState> contextModel) {
        if (slot != EquipmentSlot.CHEST) return;
        if (!(stack.getItem() instanceof FurChestItem furChestItem)) return;

        FurChestplateModel model = ArmorRegistry.getChestplateModel(furChestItem, contextModel.body, contextModel.leftArm, contextModel.rightArm, contextModel.leftLeg, contextModel.rightLeg);

        Identifier baseTexture = furChestItem.getChestplateTexture();
        String texturePath = baseTexture.getPath();
        if (!texturePath.startsWith("textures/")) texturePath = "textures/" + texturePath;
        if (!texturePath.endsWith(".png")) texturePath = texturePath + ".png";
        Identifier texture = Identifier.fromNamespaceAndPath(baseTexture.getNamespace(), texturePath);

        matrices.pushPose();
        matrices.scale(1.075F, 1.075F, 1.075F);
        collector.submitModel(model, renderState, matrices, RenderTypes.entitySolid(texture), light, OverlayTexture.NO_OVERLAY, EntityRenderState.NO_OUTLINE, null);
        matrices.popPose();
    }
}
