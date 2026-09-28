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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Draws the belt-slot item on the player's right hip. */
@Environment(EnvType.CLIENT)
public class BeltItemLayer extends RenderLayer<AvatarRenderState, PlayerModel> {

    /** Upstream {@code belt_lantern_items}: lanterns that hang on the belt. */
    public static final net.minecraft.tags.TagKey<Item> LANTERN_ITEMS =
            net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
                    BackSlot.id("belt_lantern_items"));

    /** 26.2 has no SwordItem class; swords live in the vanilla item tag. */
    public static final net.minecraft.tags.TagKey<Item> SWORDS =
            net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
                    net.minecraft.resources.Identifier.withDefaultNamespace("swords"));

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
        ItemStack back = ((BackSlotRenderStateHolder) state).backslot$backItem();
        Minecraft minecraft = Minecraft.getInstance();
        boolean mainHandEmpty = minecraft.player == null || minecraft.player.getMainHandItem().isEmpty();
        float backScale = BackSlot.CONFIG.backslotScaling;

        // BackSlot Addon: a sword on the belt rides on the back when the back
        // slot is empty, or beside a shield that already covers the back.
        if (BackSlot.CONFIG.doubleBackSword && back.is(SWORDS) && stack.is(SWORDS)) {
            this.renderState.clear();
            minecraft.getItemModelResolver().updateForTopItem(this.renderState, stack,
                    ItemDisplayContext.FIXED, minecraft.level, null, 0);
            poseStack.pushPose();
            poseStack.mulPose(Axis.ZP.rotationDegrees(-270));
            poseStack.translate(0.23, -0.25, 0.28);
            poseStack.scale(backScale, backScale, backScale);
            if (mainHandEmpty) {
                poseStack.translate(0, 0, -0.04);
            }
            this.renderState.submit(poseStack, submitNodeCollector, lightCoords,
                    OverlayTexture.NO_OVERLAY, state.outlineColor);
            poseStack.popPose();
            return;
        }
        if (back.getItem() instanceof net.minecraft.world.item.ShieldItem && stack.is(SWORDS)) {
            this.renderState.clear();
            minecraft.getItemModelResolver().updateForTopItem(this.renderState, stack,
                    ItemDisplayContext.FIXED, minecraft.level, null, 0);
            poseStack.pushPose();
            poseStack.translate(-0.07, -0.05, 0.23);
            poseStack.scale(backScale, backScale, backScale);
            if (!mainHandEmpty) {
                poseStack.translate(0, 0, 0.02);
            }
            if (!BackSlot.CONFIG.shieldClipping) {
                poseStack.translate(0, 0, -0.06);
            }
            this.renderState.submit(poseStack, submitNodeCollector, lightCoords,
                    OverlayTexture.NO_OVERLAY, state.outlineColor);
            poseStack.popPose();
            return;
        }
        // BackSlot Addon: a lantern on the belt hangs behind the shoulder.
        if (BackSlot.CONFIG.allowLanternOnBelt && stack.is(BeltItemLayer.LANTERN_ITEMS)) {
            this.renderState.clear();
            minecraft.getItemModelResolver().updateForTopItem(this.renderState, stack,
                    ItemDisplayContext.FIXED, minecraft.level, null, 0);
            float lanternXRot = 20.0f;
            double ty = -0.1;
            double y2 = -1.0;
            double z = -0.28;
            if (BackSlot.CONFIG.lanternOnBack) {
                lanternXRot = -32.0f;
                ty = -0.15;
                y2 = -0.95;
                z = -0.5;
            }
            poseStack.pushPose();
            poseStack.mulPose(Axis.ZP.rotationDegrees(180));
            poseStack.mulPose(Axis.XP.rotationDegrees(lanternXRot));
            poseStack.translate(ty, y2, z);
            float s = BackSlot.CONFIG.beltslotScaling * 0.6f;
            poseStack.scale(s, s, s);
            this.renderState.submit(poseStack, submitNodeCollector, lightCoords,
                    OverlayTexture.NO_OVERLAY, state.outlineColor);
            poseStack.popPose();
            return;
        }

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
