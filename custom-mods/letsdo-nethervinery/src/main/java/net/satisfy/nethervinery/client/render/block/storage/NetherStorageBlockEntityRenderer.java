package net.satisfy.nethervinery.client.render.block.storage;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.satisfy.nethervinery.core.block.NetherStorageBlock;
import net.satisfy.nethervinery.core.block.entity.NetherStorageBlockEntity;

import java.util.HashMap;

// 26.2: same storage-type dispatch and facing transform as before; drawing
// goes through the collector with resolvers from the render context.
public class NetherStorageBlockEntityRenderer implements BlockEntityRenderer<NetherStorageBlockEntity, NetherStorageBlockEntityRenderer.State> {
    private static final HashMap<Identifier, NetherStorageTypeRenderer> STORAGE_TYPES = new HashMap<>();
    private final ItemModelResolver itemResolver;
    private final BlockModelResolver blockResolver;

    public static class State extends BlockEntityRenderState {
        public Identifier type;
        public NonNullList<ItemStack> items = NonNullList.create();
        public Level level;
    }

    public static void registerStorageType(Identifier name, NetherStorageTypeRenderer renderer) {
        STORAGE_TYPES.put(name, renderer);
    }

    public static NetherStorageTypeRenderer getRendererForId(Identifier name) {
        return STORAGE_TYPES.get(name);
    }

    public NetherStorageBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemResolver = context.itemModelResolver();
        this.blockResolver = context.blockModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(NetherStorageBlockEntity entity, State state, float partialTick, Vec3 cameraPos,
            ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(entity, state, crumbling);
        state.level = entity.hasLevel() ? entity.getLevel() : null;
        if (state.level == null) return;
        BlockState blockState = entity.getBlockState();
        Block block = blockState.getBlock();
        if (block instanceof NetherStorageBlock sB) {
            state.type = sB.type();
            state.items = NonNullList.create();
            for (ItemStack stack : entity.getInventory()) state.items.add(stack.copy());
        }
    }

    @Override
    public void submit(State state, PoseStack matrices, SubmitNodeCollector collector, CameraRenderState cameraState) {
        if (state.level == null || state.type == null) return;
        matrices.pushPose();
        BlockState blockState = state.level.getBlockState(state.blockPos);
        applyBlockAngle(matrices, blockState, 180.0F);
        NetherStorageTypeRenderer renderer = getRendererForId(state.type);
        if (renderer != null) {
            renderer.render(matrices, collector, itemResolver, blockResolver, state.level, state.blockPos,
                    state.blockPos.hashCode(), state.items, state.lightCoords);
        }
        matrices.popPose();
    }

    public static void applyBlockAngle(PoseStack matrices, BlockState state, float angleOffset) {
        float angle = state.getValue(NetherStorageBlock.FACING).toYRot();
        matrices.translate(0.5, 0, 0.5);
        matrices.mulPose(Axis.YP.rotationDegrees(angleOffset - angle));
    }
}
