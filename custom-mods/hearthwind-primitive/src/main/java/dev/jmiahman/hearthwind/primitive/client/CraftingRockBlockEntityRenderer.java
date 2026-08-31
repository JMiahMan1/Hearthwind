package dev.jmiahman.hearthwind.primitive.client;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.jmiahman.hearthwind.primitive.CraftingRockBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Renders every placed item floating over its 3x3 compartment on the
 * crafting rock, so the player can see which slot a hit will affect
 * (the rock itself is a plain slab with no GUI).
 *
 * Slot geometry mirrors the server's mirrored getSlot(x,z) mapping:
 * slot s covers the third starting at x = (2 - s/3) * 0.33,
 * z = (2 - s%3) * 0.33.
 */
public class CraftingRockBlockEntityRenderer
        implements BlockEntityRenderer<CraftingRockBlockEntity, CraftingRockBlockEntityRenderer.RockRenderState> {

    private static final double TOP = 0.505;
    private static final float SCALE = 0.38f;

    private final ItemModelResolver itemModelResolver;

    public CraftingRockBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    public static class RockRenderState extends BlockEntityRenderState {
        public final ItemStackRenderState[] slots = new ItemStackRenderState[9];
        public final boolean[] present = new boolean[9];

        public RockRenderState() {
            for (int i = 0; i < 9; i++) {
                this.slots[i] = new ItemStackRenderState();
            }
        }
    }

    @Override
    public RockRenderState createRenderState() {
        return new RockRenderState();
    }

    @Override
    public void extractRenderState(CraftingRockBlockEntity blockEntity, RockRenderState state,
            float partialTick, net.minecraft.world.phys.Vec3 camera,
            net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay overlay) {
        BlockEntityRenderState.extractBase(blockEntity, state, overlay);
        Level level = blockEntity.getLevel();
        int seed = (int) (level == null ? 0 : level.getGameTime());
        for (int i = 0; i < 9; i++) {
            ItemStack stack = blockEntity.getItem(i);
            state.present[i] = !stack.isEmpty();
            if (state.present[i]) {
                this.itemModelResolver.updateForTopItem(state.slots[i], stack,
                        ItemDisplayContext.FIXED, level, null, seed + i);
            }
        }
    }

    @Override
    public void submit(RockRenderState state, PoseStack poseStack,
            SubmitNodeCollector collector, CameraRenderState camera) {
        for (int i = 0; i < 9; i++) {
            if (!state.present[i] || state.slots[i].isEmpty()) {
                continue;
            }
            // center of compartment (s/3, s%3) in the mirrored mapping
            double cx = (2.5 - (i / 3)) / 3.0;
            double cz = (2.5 - (i % 3)) / 3.0;
            poseStack.pushPose();
            poseStack.translate(cx, TOP, cz);
            poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90.0f));
            poseStack.scale(SCALE, SCALE, SCALE);
            state.slots[i].submit(poseStack, collector, state.lightCoords,
                    OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }
}
