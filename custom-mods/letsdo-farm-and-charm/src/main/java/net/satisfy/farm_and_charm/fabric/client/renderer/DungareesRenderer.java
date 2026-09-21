package net.satisfy.farm_and_charm.fabric.client.renderer;

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
import net.satisfy.farm_and_charm.client.model.DungareesLeggingsModel;
import net.satisfy.farm_and_charm.core.item.DungareesItem;
import net.satisfy.farm_and_charm.core.registry.ArmorRegistry;

// 26.2: armor submits through ArmorRenderer.submitTransformCopyingModel,
// which copies the wearer's pose onto the dungarees model. Same texture,
// slot gate and overlay behavior as before.
public class DungareesRenderer implements ArmorRenderer {
    @Override
    public void render(PoseStack matrices, SubmitNodeCollector collector, ItemStack stack,
            HumanoidRenderState state, EquipmentSlot slot, int light,
            HumanoidModel<HumanoidRenderState> contextModel) {
        if (slot != EquipmentSlot.LEGS) return;
        if (!(stack.getItem() instanceof DungareesItem leggings)) return;

        DungareesLeggingsModel model = ArmorRegistry.getLeggingsModel(leggings);
        if (model == null) return;

        Identifier base = leggings.getLeggingsTexture();
        String path = base.getPath();
        if (!path.startsWith("textures/")) path = "textures/" + path;
        if (!path.endsWith(".png")) path = path + ".png";
        Identifier texture = Identifier.fromNamespaceAndPath(base.getNamespace(), path);

        ArmorRenderer.submitTransformCopyingModel(model, state, contextModel, state, true, collector,
                matrices, RenderTypes.armorCutoutNoCull(texture), light, OverlayTexture.NO_OVERLAY,
                EntityRenderState.NO_OUTLINE, null);
    }
}
