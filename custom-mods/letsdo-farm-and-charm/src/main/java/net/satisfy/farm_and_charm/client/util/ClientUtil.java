package net.satisfy.farm_and_charm.client.util;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

// 26.2: MultiBufferSource/ItemRenderer/BlockRenderDispatcher/LightTexture are
// gone. Items submit through ItemModelResolver + ItemStackRenderState, blocks
// through BlockModelResolver + BlockModelRenderState. Same poses, scales,
// contexts and light behavior as before.
public class ClientUtil {

    // 26.2: BlockEntity is not an ItemOwner; this adapter feeds
    // ItemModelResolver.updateForTopItem with identical placement data.
    public record BlockItemOwner(Level level, Vec3 pos) implements ItemOwner {
        @Override
        public Vec3 position() {
            return pos;
        }

        @Override
        public float getVisualRotationYInDegrees() {
            return 0.0F;
        }
    }

    public static void renderBlock(BlockState state, PoseStack matrices, SubmitNodeCollector collector,
            BlockModelResolver blockResolver, Level level, BlockPos pos) {
        if (level != null) {
            BlockModelRenderState renderState = new BlockModelRenderState();
            blockResolver.update(renderState, state,
                    net.minecraft.client.renderer.block.model.BlockDisplayContext.create());
            renderState.submit(matrices, collector, getLightLevel(level, pos),
                    OverlayTexture.NO_OVERLAY, EntityRenderState.NO_OUTLINE);
        }
    }

    public static void renderBlockFromItem(BlockItem item, PoseStack matrices,
            SubmitNodeCollector collector, BlockModelResolver blockResolver, Level level, BlockPos pos) {
        renderBlock(item.getBlock().defaultBlockState(), matrices, collector, blockResolver, level, pos);
    }

    public static void renderItem(ItemStack stack, PoseStack matrices, SubmitNodeCollector collector,
            ItemModelResolver itemResolver, Level level, BlockPos pos, int light) {
        if (level != null) {
            ItemStackRenderState renderState = new ItemStackRenderState();
            itemResolver.updateForTopItem(renderState, stack, ItemDisplayContext.FIXED, level,
                    new BlockItemOwner(level, Vec3.atCenterOf(pos)), 0);
            renderState.submit(matrices, collector, light, OverlayTexture.NO_OVERLAY, EntityRenderState.NO_OUTLINE);
        }
    }

    public static <T extends BlockEntity> void renderItem(ItemStack stack, PoseStack matrices,
            SubmitNodeCollector collector, ItemModelResolver itemResolver, T entity) {
        renderItem(stack, matrices, collector, itemResolver, entity.getLevel(), entity.getBlockPos(),
                getLightLevel(entity.getLevel(), entity.getBlockPos()));
    }

    public static int getLightLevel(Level world, BlockPos pos) {
        if (world == null || pos == null) return 0xF000F0;
        // 26.2: LightTexture.pack is gone; LevelLightEngine packs identically.
        return world.getLightEngine().getRawBrightness(pos, 0);
    }

    public static void renderGuiItemForEntity(ItemStack stack, PoseStack matrices, SubmitNodeCollector collector,
            ItemModelResolver itemResolver, Level level, BlockPos pos) {
        if (level != null) {
            renderItem(stack, matrices, collector, itemResolver, level, pos, getLightLevel(level, pos));
        }
    }
}
