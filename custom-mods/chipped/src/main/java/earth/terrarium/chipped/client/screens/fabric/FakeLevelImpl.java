package earth.terrarium.chipped.client.screens.fabric;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.block.state.BlockState;

public class FakeLevelImpl {
    public static void renderBatched(BlockPos pos, BlockState state, BlockAndLightGetter level, PoseStack poseStack, RandomSource random) {
        // 26.2: block rendering pipeline reworked; no-op for compile
    }
}
