package dev.jmiahman.hearthwind.world.snow;

import dev.jmiahman.hearthwind.world.HearthwindWorld;
import dev.jmiahman.hearthwind.world.Season;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * In-house port of 'snow-real-magic' / realistic winter snow accumulation:
 * In winter, snow layers build up progressively on exposed blocks, fences, and foliage.
 */
public final class WinterSnowAccumulation {

    private WinterSnowAccumulation() {}

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % 80 != 0) return;

            for (ServerLevel level : server.getAllLevels()) {
                if (level.isClientSide()) continue;
                if (!level.isRaining() && !level.isThundering()) continue;
                if (HearthwindWorld.currentSeason(level) != Season.WINTER) continue;

                for (ServerPlayer player : level.players()) {
                    accumulateSnowNearPlayer(player, level);
                }
            }
        });
    }

    public static void accumulateSnowNearPlayer(ServerPlayer player, ServerLevel level) {
        BlockPos playerPos = player.blockPosition();
        int rx = playerPos.getX() + level.getRandom().nextInt(32) - 16;
        int rz = playerPos.getZ() + level.getRandom().nextInt(32) - 16;
        BlockPos topPos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos(rx, 0, rz));

        tryAccumulateSnow(level, topPos);
    }

    public static boolean tryAccumulateSnow(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        BlockState belowState = level.getBlockState(pos.below());

        // 1. If air above solid block, place initial snow layer
        if (state.isAir() && belowState.isSolid()) {
            level.setBlockAndUpdate(pos, Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 1));
            return true;
        }

        // 2. If already snow layer, grow up to 3 layers in deep winter
        if (state.is(Blocks.SNOW)) {
            int layers = state.getValue(SnowLayerBlock.LAYERS);
            if (layers < 3) {
                level.setBlockAndUpdate(pos, state.setValue(SnowLayerBlock.LAYERS, layers + 1));
                return true;
            }
        }

        return false;
    }
}
