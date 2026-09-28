package com.teamremastered.tlc.processors;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Turns the template's yellow-concrete markers into random stone so castle
 * foundations blend into the terrain.
 *
 * <p>Ported to 26.2: {@code StructureProcessor} is an interface there, its
 * {@code processBlock} takes an extra {@link BlockPos} after the two jigsaw
 * positions, and the old {@code getType()} is replaced by an abstract
 * {@code codec()}.  The block logic is upstream's, including its
 * bounds check as written.
 */
public class FoundationProcessor implements StructureProcessor {
    public static final FoundationProcessor INSTANCE = new FoundationProcessor();
    public static final MapCodec<FoundationProcessor> CODEC = MapCodec.unit(() -> INSTANCE);

    /**
     * 26.2 collapsed the 26.1 local/global block-info pair into a single
     * world-space {@code blockInfo}; {@code pos} is the piece-relative
     * position.  Upstream only ever read the global one.
     */
    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelReader,
                                                             BlockPos jigsawPiecePos,
                                                             BlockPos jigsawPieceBottomCenterPos,
                                                             BlockPos pos,
                                                             StructureTemplate.StructureBlockInfo blockInfo,
                                                             StructurePlaceSettings structurePlacementData) {
        StructureTemplate.StructureBlockInfo blockInfoGlobal = blockInfo;
        if (blockInfoGlobal.state().is(Blocks.CONCRETE.yellow())) {
            if (levelReader instanceof WorldGenRegion worldGenRegion
                    && !worldGenRegion.getCenter().equals(ChunkPos.containing(blockInfoGlobal.pos()))) {
                return blockInfoGlobal;
            }
            BlockState[] foundationBlocks = new BlockState[]{
                    Blocks.STONE.defaultBlockState(),
                    Blocks.STONE_BRICKS.defaultBlockState(),
                    Blocks.CRACKED_STONE_BRICKS.defaultBlockState(),
                    Blocks.CRACKED_STONE_BRICKS.defaultBlockState(),
                    Blocks.MOSSY_STONE_BRICKS.defaultBlockState(),
                    Blocks.POLISHED_ANDESITE.defaultBlockState(),
                    Blocks.POLISHED_ANDESITE.defaultBlockState(),
            };
            blockInfoGlobal = new StructureTemplate.StructureBlockInfo(
                    blockInfoGlobal.pos(), randomBlocks(foundationBlocks), blockInfoGlobal.nbt());
            BlockPos.MutableBlockPos mutable = blockInfoGlobal.pos().mutable().move(Direction.DOWN);
            BlockState currBlockState = levelReader.getBlockState(mutable);
            while (mutable.getY() > levelReader.getMaxY() && mutable.getY() < levelReader.getMinY()
                    && (currBlockState.isAir() || !levelReader.getFluidState(mutable).isEmpty())) {
                levelReader.getChunk(mutable).setBlockState(mutable, randomBlocks(foundationBlocks));
                mutable.move(Direction.DOWN);
                currBlockState = levelReader.getBlockState(mutable);
            }
        }
        return blockInfoGlobal;
    }

    public BlockState randomBlocks(BlockState[] randomBlocks) {
        return randomBlocks[ThreadLocalRandom.current().nextInt(0, randomBlocks.length)];
    }

    @Override
    public MapCodec<? extends StructureProcessor> codec() {
        return CODEC;
    }
}
