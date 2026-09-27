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

/**
 * Draws the back-slot item, following upstream's two branches: block items
 * (shields, lanterns...) lean flat against the back, everything else is
 * flipped and tucked into the shoulder.
 */
@Environment(EnvType.CLIENT)
public class BackItemLayer extends RenderLayer<AvatarRenderState, PlayerModel> {

    private final ItemStackRenderState renderState = new ItemStackRenderState();

    public BackItemLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords,
            AvatarRenderState state, float yRot, float xRot) {
        ItemStack stack = ((BackSlotRenderStateHolder) state).backslot$backItem();
        if (stack.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        this.renderState.clear();
        minecraft.getItemModelResolver().updateForTopItem(this.renderState, stack,
                ItemDisplayContext.FIXED, minecraft.level, null, 0);

        poseStack.pushPose();
        if (stack.getItem() instanceof BlockItem) {
            poseStack.mulPose(Axis.YP.rotationDegrees(52));
            poseStack.mulPose(Axis.XP.rotationDegrees(40));
            poseStack.mulPose(Axis.ZP.rotationDegrees(-25));
            poseStack.translate(-0.26, 0, 0);
            if (!state.isCrouching) {
                poseStack.translate(0.05, 0, 0);
            }
            poseStack.scale(1, -1, -1);
        } else {
            float scale = BackSlot.CONFIG.backslotScaling;
            poseStack.translate(0.16, 0, 0);
            poseStack.scale(scale, scale, scale);
            poseStack.mulPose(Axis.ZP.rotationDegrees(180));
            poseStack.translate(-0.3, 0, 0);
            if (state.isCrouching) {
                poseStack.translate(0.06, 0, 0);
            }
        }
        this.renderState.submit(poseStack, submitNodeCollector, lightCoords,
                OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
    }
}
