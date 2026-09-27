package net.backslot.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.backslot.BackSlot;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Draws the belt-slot item on the player's right hip. */
@Environment(EnvType.CLIENT)
public class BeltItemLayer extends RenderLayer<AvatarRenderState, PlayerModel> {

    private final ItemStackRenderState renderState = new ItemStackRenderState();

    public BeltItemLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords,
            AvatarRenderState state, float yRot, float xRot) {
        ItemStack stack = ((BackSlotRenderStateHolder) state).backslot$beltItem();
        if (stack.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        this.renderState.clear();
        minecraft.getItemModelResolver().updateForTopItem(this.renderState, stack,
                ItemDisplayContext.FIXED, minecraft.level, null, 0);

        poseStack.pushPose();
        poseStack.translate(0.29, 0, 0);
        if (stack.getItem() instanceof BlockItem) {
            poseStack.translate(0.01, -0.1, 0);
        }
        poseStack.mulPose(Axis.YP.rotationDegrees(-90));
        float scale = stack.getItem() instanceof BlockItem ? 0.65f : BackSlot.CONFIG.beltslotScaling;
        poseStack.scale(scale, scale, scale);
        if (state.isCrouching) {
            poseStack.translate(0, 0.015, 0);
        }
        this.renderState.submit(poseStack, submitNodeCollector, lightCoords,
                OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
    }
}
