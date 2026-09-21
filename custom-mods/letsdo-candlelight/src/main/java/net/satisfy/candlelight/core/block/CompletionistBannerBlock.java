package net.satisfy.candlelight.core.block;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ScheduledTickAccess;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.satisfy.candlelight.Candlelight;
import net.satisfy.candlelight.core.block.entity.CompletionistBannerEntity;
import net.satisfy.candlelight.core.registry.EntityTypeRegistry;
import net.satisfy.candlelight.core.registry.ObjectRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("deprecation")
public class CompletionistBannerBlock extends BaseEntityBlock {
    public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;
    private static final VoxelShape SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D);

    public CompletionistBannerBlock(Properties properties) {
        super(properties);
        makeDefaultState();
    }

    public static final MapCodec<CompletionistBannerBlock> CODEC = simpleCodec(CompletionistBannerBlock::new);

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return new CompletionistBannerEntity(blockPos, blockState);
    }

    protected void makeDefaultState() {
        registerDefaultState(this.stateDefinition.any().setValue(ROTATION, 0));
    }

    @Override
    public boolean canSurvive(@NotNull BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        if (blockState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return levelReader.getBlockState(blockPos.below()).isSolid() ||
                    levelReader.getBlockState(blockPos.relative(blockState.getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite())).isSolid();
        } else {
            return levelReader.getBlockState(blockPos.below()).isSolid();
        }
    }

    @Override
    public boolean isPossibleToRespawnInThis(@NotNull BlockState blockState) {
        return true;
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState blockState, @NotNull BlockGetter blockGetter, @NotNull BlockPos blockPos, @NotNull CollisionContext collisionContext) {
        return SHAPE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clickedFace = context.getClickedFace();
        if (clickedFace == Direction.UP || clickedFace == Direction.DOWN) {
            return this.defaultBlockState().setValue(ROTATION, Mth.floor((double) ((180.0f + context.getRotation()) * 16.0f / 360.0f) + 0.5) & 0xF);
        } else {
            if (this == ObjectRegistry.CANDLELIGHT_BANNER.get()) {
                return ObjectRegistry.CANDLELIGHT_WALL_BANNER.get().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, clickedFace.getOpposite());
            }
        }
        return this.defaultBlockState();
    }


    @Override
    public @NotNull BlockState rotate(BlockState blockState, Rotation rotation) {
        return blockState.setValue(ROTATION, rotation.rotate(blockState.getValue(ROTATION), 16));
    }

    @Override
    public @NotNull BlockState mirror(BlockState blockState, Mirror mirror) {
        return blockState.setValue(ROTATION, mirror.mirror(blockState.getValue(ROTATION), 16));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ROTATION);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, EntityTypeRegistry.CANDLELIGHT_BANNER_ENTITY.get(), (level1, pos, state1, entity) -> CompletionistBannerEntity.tick(level1, pos));
    }


    public Identifier getRenderTexture() {
        return Candlelight.identifier("textures/banner/candlelight_banner.png");
    }

}
