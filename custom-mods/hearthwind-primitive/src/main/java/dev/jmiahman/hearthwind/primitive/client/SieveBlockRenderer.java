package dev.jmiahman.hearthwind.primitive.client;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.jmiahman.hearthwind.primitive.SieveBlockEntity;
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
 * Renders the siftable stack INSIDE the sieve basin (below the lattice
 * plate, above the floor), sinking slightly with each tap so the material
 * visibly sits in the sieve instead of floating on top of it.
 */
public class SieveBlockRenderer implements BlockEntityRenderer<SieveBlockEntity, SieveBlockRenderer.SieveRenderState> {

    private final ItemModelResolver itemModelResolver;

    public SieveBlockRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    public static class SieveRenderState extends BlockEntityRenderState {
        public final ItemStackRenderState stackState = new ItemStackRenderState();
        public int sieveCount;
        public boolean hasStack;
    }

    @Override
    public SieveRenderState createRenderState() {
        return new SieveRenderState();
    }

    @Override
    public void extractRenderState(SieveBlockEntity blockEntity, SieveRenderState state,
            float partialTick, net.minecraft.world.phys.Vec3 camera,
            net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay overlay) {
        BlockEntityRenderState.extractBase(blockEntity, state, overlay);
        state.sieveCount = blockEntity.getSieveCount();
        ItemStack stack = blockEntity.getItem(0);
        state.hasStack = !stack.isEmpty();
        if (state.hasStack) {
            Level level = blockEntity.getLevel();
            this.itemModelResolver.updateForTopItem(state.stackState, stack,
                    ItemDisplayContext.NONE, level, null,
                    (int) (level == null ? 0 : level.getGameTime()));
        }
    }

    @Override
    public void submit(SieveRenderState state, PoseStack poseStack,
            SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.hasStack || state.stackState.isEmpty()) {
            return;
        }
        // basin floor at 2px, lattice plate at 14px: keep the item between them
        double height = switch (state.sieveCount) {
            case 0 -> 0.20;
            case 1 -> 0.175;
            case 2 -> 0.15;
            default -> 0.125;
        };
        poseStack.pushPose();
        poseStack.translate(0.5, height, 0.5);
        poseStack.scale(0.62f, 0.62f, 0.62f);
        poseStack.translate(-0.5, 0.0, -0.5);
        state.stackState.submit(poseStack, collector, state.lightCoords,
                OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }
}
