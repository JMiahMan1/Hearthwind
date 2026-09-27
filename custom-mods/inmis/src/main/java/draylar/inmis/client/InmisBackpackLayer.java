package draylar.inmis.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import draylar.inmis.item.BackpackItem;
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
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Draws a chest-slot backpack on the player's back, ported from Inmis's
 * {@code BackpackFeature}: flip the item over, nudge it onto the back and
 * add a lean while sneaking.
 */
@Environment(EnvType.CLIENT)
public class InmisBackpackLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    private final ItemStackRenderState renderState = new ItemStackRenderState();

    public InmisBackpackLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords,
            AvatarRenderState state, float yRot, float xRot) {
        ItemStack stack = ((InmisRenderStateHolder) state).inmis$chestItem();
        if (!(stack.getItem() instanceof BackpackItem)) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        this.renderState.clear();
        minecraft.getItemModelResolver().updateForTopItem(this.renderState, stack,
                ItemDisplayContext.FIXED, minecraft.level, null, 0);

        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(180));
        poseStack.translate(0, -0.2, -0.25);
        if (state.isCrouching) {
            poseStack.mulPose(Axis.XP.rotationDegrees(25));
            poseStack.translate(0, -0.2, 0);
        }
        this.renderState.submit(poseStack, submitNodeCollector, lightCoords,
                OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
    }
}
