package dev.jmiahman.hearthwind.survival.hydration;

import com.mojang.serialization.MapCodec;

import dev.jmiahman.hearthwind.survival.HearthwindSurvivalConfig;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorageUtil;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.item.context.BlockPlaceContext;

/**
 * Port of Dehydration 1.3.6 {@code CampfireCauldronBlock}: a cauldron hung
 * over a campfire. Stores 0..4 bottle-units, boils into purified water after
 * {@code water_boiling_time} ticks on a lit campfire, takes rain, dripstone
 * and bucket/bottle/bowl/flask transfers (Fabric fluid storage), and drives
 * a comparator from its level.
 */
public class CampfireCauldronBlock extends BaseEntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 4);

    private static final VoxelShape CAULDRON_SHAPE = Shapes.or(
            Block.box(11, 1, 4, 12, 6, 5), Block.box(4, 0, 4, 12, 1, 12),
            Block.box(3, 1, 4, 4, 6, 12), Block.box(12, 1, 4, 13, 6, 12),
            Block.box(4, 1, 12, 12, 6, 13), Block.box(4, 1, 3, 12, 6, 4),
            Block.box(4, 1, 4, 5, 6, 5), Block.box(11, 1, 11, 12, 6, 12),
            Block.box(4, 1, 11, 5, 6, 12));
    private static final VoxelShape Z_BASE_SHAPE = Shapes.or(CAULDRON_SHAPE,
            Block.box(7, -15, 0, 9, 14, 1), Block.box(7, 14, -1, 9, 16, 1),
            Block.box(7, 14, 15, 9, 16, 17), Block.box(7, -15, 15, 9, 14, 16),
            Block.box(7, 14, 1, 9, 15, 15));
    private static final VoxelShape X_BASE_SHAPE = Shapes.or(CAULDRON_SHAPE,
            Block.box(15, -15, 7, 16, 14, 9), Block.box(15, 14, 7, 17, 16, 9),
            Block.box(-1, 14, 7, 1, 16, 9), Block.box(0, -15, 7, 1, 14, 9),
            Block.box(1, 14, 7, 15, 15, 9));

    public CampfireCauldronBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, 0));
    }

    @Override
    protected MapCodec<CampfireCauldronBlock> codec() {
        return simpleCodec(CampfireCauldronBlock::new);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CampfireCauldronBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        BlockEntityTicker<CampfireCauldronBlockEntity> ticker = level.isClientSide()
                ? CampfireCauldronBlockEntity::clientTick
                : CampfireCauldronBlockEntity::serverTick;
        return createTickerHelper(type, HydrationBlocks.CAMPFIRE_CAULDRON_ENTITY, ticker);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getClockWise());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return state.getValue(FACING).getAxis() == Direction.Axis.X ? X_BASE_SHAPE : Z_BASE_SHAPE;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).is(BlockTags.CAMPFIRES);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!player.getItemInHand(hand).isEmpty()) {
            Storage<FluidVariant> storage = FluidStorage.SIDED.find(level, pos,
                    hitResult.getDirection().getOpposite());
            if (storage != null && FluidStorageUtil.interactWithFluidStorage(storage, player, hand)) {
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    public void setLevel(Level level, BlockPos pos, BlockState state, int fluidLevel) {
        level.setBlock(pos, state.setValue(LEVEL, Mth.clamp(fluidLevel, 0, 4)), 2);
        level.updateNeighbourForOutputSignal(pos, this);
    }

    @Override
    public void handlePrecipitation(BlockState state, Level level, BlockPos pos,
            Biome.Precipitation precipitation) {
        if (precipitation == Biome.Precipitation.RAIN
                && level.isRainingAt(pos)
                && level.getRandom().nextFloat()
                        < HearthwindSurvivalConfig.get().hydration.campfireRainFillChance
                && state.getValue(LEVEL) < 4) {
            this.setLevel(level, pos, state, state.getValue(LEVEL) + 1);
            level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
        }
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos,
            Direction direction) {
        return state.getValue(LEVEL);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL);
        builder.add(FACING);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    public boolean isFull(BlockState state) {
        return state.getValue(LEVEL) == 4;
    }

    public boolean isFireBurning(Level level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        return below.getBlock() instanceof CampfireBlock && CampfireBlock.isLitCampfire(below);
    }

    public boolean isPurifiedWater(Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof CampfireCauldronBlockEntity entity
                && entity.isBoiled;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockPos stalactitePos = PointedDripstoneBlock.findStalactiteTipAboveCauldron(level, pos);
        if (stalactitePos != null) {
            Fluid fluid = PointedDripstoneBlock.getCauldronFillFluidType(level, stalactitePos);
            if (fluid != Fluids.EMPTY && this.canReceiveStalactiteDrip(fluid)) {
                this.receiveStalactiteDrip(state, level, pos, fluid);
            }
        }
    }

    protected boolean canReceiveStalactiteDrip(Fluid fluid) {
        return fluid == Fluids.WATER;
    }

    protected void receiveStalactiteDrip(BlockState state, Level level, BlockPos pos, Fluid fluid) {
        if (!this.isFull(state)) {
            level.setBlockAndUpdate(pos, state.setValue(LEVEL, state.getValue(LEVEL) + 1));
            level.levelEvent(1047, pos, 0);
        }
    }

}
