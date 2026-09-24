package net.fleshz.block;

import org.jetbrains.annotations.Nullable;

import net.fleshz.block.entity.WoodRackEntity;
import net.fleshz.init.BlockInit;
import net.fleshz.init.RecipeInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.server.level.ServerLevel;

public class WoodRack extends Block implements EntityBlock {

    public static final VoxelShape SHAPENORTH = Block.box(0D, 13D, 13D, 16D, 16D, 16D);
    public static final VoxelShape SHAPEWEST = Block.box(13D, 13D, 0D, 16D, 16D, 16D);
    public static final VoxelShape SHAPEEAST = Block.box(0D, 13D, 0D, 3D, 16D, 16D);
    public static final VoxelShape SHAPESOUTH = Block.box(0D, 13D, 0D, 16D, 16D, 3D);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public WoodRack(BlockBehaviour.Properties settings) {
        super(settings);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, false));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WoodRackEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return type == BlockInit.WOOD_RACK_ENTITY
                ? (lvl, pos, st, be) -> WoodRackEntity.serverTick(lvl, pos, st, (WoodRackEntity) be)
                : null;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof WoodRackEntity woodRackEntity)) {
            return InteractionResult.PASS;
        }
        ItemStack rackStack = woodRackEntity.getItem(0);
        if (rackStack.isEmpty()) {
            ItemStack heldItem = player.getMainHandItem();
            if (!heldItem.isEmpty() && RecipeInit.RACK_ITEM_LIST.contains(heldItem.getItem())) {
                if (!level.isClientSide()) {
                    if (player.isCreative()) {
                        woodRackEntity.setItem(0, heldItem.copy());
                    } else {
                        woodRackEntity.setItem(0, heldItem.split(1));
                    }
                }
                return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
            }
            return InteractionResult.CONSUME;
        } else {
            if (!level.isClientSide() && !player.getInventory().add(rackStack.split(1))) {
                player.drop(rackStack.split(1), false);
            }
            woodRackEntity.clearContent();
            return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
        }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> SHAPENORTH;
            case SOUTH -> SHAPESOUTH;
            case WEST -> SHAPEWEST;
            default -> SHAPEEAST;
        };
    }

    private boolean canPlaceOn(BlockGetter world, BlockPos pos, Direction side) {
        BlockState blockState = world.getBlockState(pos);
        return !blockState.isSignalSource() && blockState.isFaceSturdy(world, pos, side);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        Direction direction = state.getValue(FACING);
        return this.canPlaceOn(world, pos.relative(direction.getOpposite()), direction);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess tickAccess,
            BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (direction.getOpposite() == state.getValue(FACING) && !state.canSurvive(world, pos)) {
            return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        }
        if (state.getValue(WATERLOGGED)) {
            tickAccess.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        }
        return super.updateShape(state, world, tickAccess, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState candidate = this.defaultBlockState();
        LevelReader worldView = ctx.getLevel();
        BlockPos blockPos = ctx.getClickedPos();
        FluidState fluidState = ctx.getLevel().getFluidState(ctx.getClickedPos());

        for (Direction direction : ctx.getNearestLookingDirections()) {
            if (!direction.getAxis().isHorizontal()) {
                continue;
            }
            candidate = candidate.setValue(FACING, direction.getOpposite());
            if (candidate.canSurvive(worldView, blockPos)) {
                return candidate.setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
            }
        }
        return null;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, net.minecraft.world.level.block.Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean moved) {
        if (world.getBlockEntity(pos) instanceof net.minecraft.world.Container container) {
            Containers.dropContents(world, pos, container);
            world.updateNeighborsAt(pos, this);
        }
        super.affectNeighborsAfterRemoval(state, world, pos, moved);
    }
}
