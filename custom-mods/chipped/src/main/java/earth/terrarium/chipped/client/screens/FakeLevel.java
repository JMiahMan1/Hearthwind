package earth.terrarium.chipped.client.screens;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Objects;
import java.util.Set;

public class FakeLevel implements BlockAndLightGetter {
    @Nullable
    private BlockState state;
    @Nullable
    private Set<BlockPos> positions;

    public static final Vector3f SCENE_LIGHT_1 = new Vector3f(1, 0, 1);
    public static final Vector3f SCENE_LIGHT_2 = new Vector3f(-1, 1, -1);

    @Override
    public int getBrightness(LightLayer lightType, BlockPos blockPos) {
        return 15;
    }

    @Override
    public int getRawBrightness(BlockPos blockPos, int amount) {
        return 15;
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        return null;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return (state != null && positions != null && positions.contains(pos)) ? state : Blocks.AIR.defaultBlockState();
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return Blocks.AIR.defaultBlockState().getFluidState();
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public int getMinY() {
        return 0;
    }

    public void setState(BlockState state) {
        this.state = state;
    }

    public void setPositions(Set<BlockPos> positions) {
        this.positions = positions;
    }

    @Override
    public LevelLightEngine getLightEngine() { return null; }

    public void renderBlock(PoseStack poseStack) {
        // 26.2: block rendering pipeline reworked; no-op
    }

    public static void renderBatched(BlockPos pos, BlockState state, BlockAndLightGetter level, PoseStack poseStack, VertexConsumer consumer, boolean checkSides, RandomSource random) {
        // 26.2: block rendering pipeline reworked; no-op
    }
}
