package dev.jmiahman.hearthwind.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.jmiahman.hearthwind.flora.block.StorageBlock;
import dev.jmiahman.hearthwind.flora.blockentity.StorageBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class StorageBlockEntityRenderer implements BlockEntityRenderer<StorageBlockEntity, StorageRenderState> {
    private static final Map<Identifier, StorageTypeRenderer> STORAGE_TYPES = new HashMap<>();
    private final ItemModelResolver itemModelResolver;

    public static void registerStorageType(Identifier name, StorageTypeRenderer renderer) {
        STORAGE_TYPES.put(name, renderer);
    }

    public static StorageTypeRenderer getRendererForId(Identifier name) {
        return STORAGE_TYPES.get(name);
    }

    public StorageBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        this.itemModelResolver = ctx.itemModelResolver();
    }

    @Override
    public StorageRenderState createRenderState() {
        return new StorageRenderState();
    }

    @Override
    public void extractRenderState(StorageBlockEntity entity, StorageRenderState state, float tickDelta, Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay overlay) {
        BlockEntityRenderState.extractBase(entity, state, overlay);
        if (entity.getBlockState().getBlock() instanceof StorageBlock sB) {
            state.storageType = sB.type();
            state.facing = entity.getBlockState().hasProperty(StorageBlock.FACING)
                    ? entity.getBlockState().getValue(StorageBlock.FACING)
                    : Direction.NORTH;
        }

        NonNullList<ItemStack> inv = entity.getInventory();
        state.itemStates.clear();
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.get(i);
            ItemStackRenderState itemState = new ItemStackRenderState();
            if (!stack.isEmpty()) {
                this.itemModelResolver.updateForTopItem(itemState, stack, ItemDisplayContext.FIXED, entity.getLevel(), null, 0);
            }
            state.itemStates.add(itemState);
        }
    }

    @Override
    public void submit(StorageRenderState state, PoseStack matrices, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        if (state.storageType == null) return;

        matrices.pushPose();
        float angle = state.facing.toYRot();
        matrices.translate(0.5, 0, 0.5);
        matrices.mulPose(Axis.YP.rotationDegrees(180f - angle));

        StorageTypeRenderer renderer = getRendererForId(state.storageType);
        if (renderer != null) {
            renderer.render(state, matrices, submitNodeCollector);
        }

        matrices.popPose();
    }
}
