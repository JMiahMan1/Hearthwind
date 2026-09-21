package net.satisfy.brewery.core.block;

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
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.util.RandomSource;
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
import net.satisfy.brewery.Brewery;
import net.satisfy.brewery.core.block.entity.CompletionistBannerEntity;
import net.satisfy.brewery.core.registry.EntityTypeRegistry;
import net.satisfy.brewery.core.registry.ObjectRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

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
        BlockState belowBlockState = levelReader.getBlockState(blockPos.below());
        return belowBlockState.isSolid();
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
            if (this == ObjectRegistry.BREWERY_BANNER.get()) {
                return ObjectRegistry.BREWERY_WALL_BANNER.get().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, clickedFace.getOpposite());
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
        return level.isClientSide() ? null : createTickerHelper(type, EntityTypeRegistry.BREWERY_BANNER.get(), (level1, pos, state1, entity) -> CompletionistBannerEntity.tick(level1, pos));
    }

    @Override
    public @NotNull BlockState updateShape(@NotNull BlockState blockState, @NotNull LevelReader level, @NotNull ScheduledTickAccess tickAccess, @NotNull BlockPos blockPos, @NotNull Direction direction, @NotNull BlockPos blockPos2, @NotNull BlockState blockState2, @NotNull RandomSource random) {
        if (direction == Direction.DOWN && !blockState.canSurvive(level, blockPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(blockState, level, tickAccess, blockPos, direction, blockPos2, blockState2, random);
    }

    public Identifier getRenderTexture() {
        return Brewery.identifier("textures/banner/brewery_banner.png");
    }

    public void appendBlockHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, net.minecraft.world.item.component.TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag tooltipFlag) {
        tooltip.accept(Component.translatable("tooltip.brewery.banner.thankyou_1").withStyle(style -> style.withColor(TextColor.fromRgb(0xa5b485))));
        tooltip.accept(Component.empty());
        tooltip.accept(Component.translatable("tooltip.brewery.banner.thankyou_2").withStyle(style -> style.withColor(TextColor.fromRgb(0xa5b485))));
        tooltip.accept(Component.translatable("tooltip.brewery.banner.thankyou_4").withStyle(style -> style.withColor(TextColor.fromRgb(0xa5b485))));
        tooltip.accept(Component.empty());
        tooltip.accept(Component.translatable("tooltip.brewery.banner.thankyou_3").withStyle(style -> style.withColor(TextColor.fromRgb(0xa5b485))));
    }

}
