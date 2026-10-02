package dev.jmiahman.hearthwind.survival.hydration;

import com.mojang.serialization.MapCodec;

import dev.jmiahman.hearthwind.survival.HearthwindSurvivalConfig;
import dev.jmiahman.hearthwind.survival.PurifiedWater;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorageUtil;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
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

    /**
     * The reference declares no {@code VoxelShape} for the campfire cauldron,
     * so vanilla's full-cube outline applies and nothing spans the block
     * below. We modelled a stand with legs that reached 15 blocks down
     * (0.1.43); that was our own art idea, not the reference's, and it is
     * gone (0.1.49).
     */
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return Shapes.block();
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).is(BlockTags.CAMPFIRES);
    }

    /**
     * Dehydration's {@code Block.onPlace} breaks a campfire cauldron when
     * something is placed directly above it. Without this the cauldron would
     * hang there with a block over its mouth, which reads as a bug: the water
     * inside still boils, and the boil sound plays from inside a solid block.
     */
    @Override
    protected BlockState updateShape(BlockState state, LevelReader level,
            ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos,
            BlockState neighbourState, RandomSource random) {
        if (directionToNeighbour == Direction.UP && !neighbourState.isAir()) {
            return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState,
                random);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!player.getItemInHand(hand).isEmpty()) {
            // Aged's potion pour: a water or purified-water bottle tops the
            // cauldron up one level and leaves an empty bowl, and pouring plain
            // water re-arms the boil so it has to be boiled again. Dehydration
            // plays the bucket-empty sound here, oddly, but we keep it.
            if (pourPotion(state, level, pos, player, hand, stack)) {
                return InteractionResult.SUCCESS;
            }
            if (pourBucket(state, level, pos, player, hand, stack)) {
                return InteractionResult.SUCCESS;
            }
            Storage<FluidVariant> storage = FluidStorage.SIDED.find(level, pos,
                    hitResult.getDirection().getOpposite());
            if (storage != null && FluidStorageUtil.interactWithFluidStorage(storage, player, hand)) {
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    /**
     * The reference's bucket pair, read out of {@code CampfireCauldronBlock#method_9534}
     * at offsets 53-136 and 137-271.
     *
     * <p>It is NOT the "a bucket is three bottles" model we used to assume. A
     * WATER BUCKET held against a cauldron below LEVEL 4 is swapped for an
     * empty BUCKET, re-arms the boil and fills it straight to LEVEL 4 in one
     * go, whatever level it was at. Symmetrically, an empty BUCKET held against
     * a full LEVEL 4 cauldron is consumed and hands back a WATER BUCKET as the
     * cauldron drops to 0.
     *
     * <p>Both rows run before the fluid-transfer path, so a bucket never
     * reaches the transfer API and is never counted as three bottles.
     *
     * @return true when the held stack was a bucket and the pour happened.
     */
    private boolean pourBucket(BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, ItemStack stack) {
        int current = state.getValue(LEVEL);
        boolean filling = stack.is(Items.WATER_BUCKET) && current < 4;
        boolean draining = stack.is(Items.BUCKET) && current == 4;
        if (!filling && !draining) {
            return false;
        }
        if (!level.isClientSide()) {
            if (filling) {
                // Offsets 74-126: refund an empty bucket, re-arm, fill to 4,
                // then ITEM_BUCKET_EMPTY at 1.0/1.0 on BLOCKS.
                if (!player.getAbilities().instabuild) {
                    player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                }
                if (level.getBlockEntity(pos) instanceof CampfireCauldronBlockEntity entity) {
                    entity.onFillingCauldron();
                }
                setLevel(level, pos, state, 4);
                level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
            } else {
                // Offsets 158-260: shrink, hand back a water bucket, drop to 0,
                // then ITEM_BUCKET_FILL at 1.0/1.0 on BLOCKS.
                stack.shrink(1);
                if (stack.isEmpty()) {
                    player.setItemInHand(hand, new ItemStack(Items.WATER_BUCKET));
                } else if (!player.getInventory().add(stack)) {
                    player.drop(stack, false);
                }
                setLevel(level, pos, state, 0);
                level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        }
        return true;
    }

    /**
     * @return true when the held stack was a water potion and the pour happened.
     */
    /**
     * Test seam for {@link #useItemOn}, which is protected and reached from
     * another package by the hydration gametests.
     */
    public InteractionResult hearthwind$useItemOnForTest(ItemStack stack, BlockState state, Level level,
            BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        return useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    private boolean pourPotion(BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, ItemStack stack) {
        if (stack.getCount() != 1 || state.getValue(LEVEL) >= 4) {
            return false;
        }
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        Holder<Potion> potion = contents == null ? null : contents.potion().orElse(null);
        if (potion == null) {
            return false;
        }
        boolean purified = potion.is(PurifiedWater.PURIFIED_POTION);
        if (!purified && !potion.is(Potions.WATER)) {
            return false;
        }
        player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.BOWL)));
        setLevel(level, pos, state, state.getValue(LEVEL) + 1);
        if (!purified && level.getBlockEntity(pos) instanceof CampfireCauldronBlockEntity entity) {
            entity.onFillingCauldron();
        }
        level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
        return true;
    }

    public void setLevel(Level level, BlockPos pos, BlockState state, int fluidLevel) {
        level.setBlock(pos, state.setValue(LEVEL, Mth.clamp(fluidLevel, 0, 4)), 2);
        level.updateNeighbourForOutputSignal(pos, this);
    }

    @Override
    public void handlePrecipitation(BlockState state, Level level, BlockPos pos,
            Biome.Precipitation precipitation) {
        // The reference's precipitationTick gates on FOUR things: the
        // precipitation is rain, world.random.nextFloat() < 0.2, world.isSkyLit
        // (so an overhang keeps the cauldron dry) and the biome is warm enough
        // (getTemperature() >= 0.15). We only had the first two (0.1.49 fixes
        // the other two).
        if (precipitation == Biome.Precipitation.RAIN
                && level.isRainingAt(pos)
                && level.canSeeSky(pos)
                && level.getBiome(pos).value().getBaseTemperature() >= 0.15F
                && level.getRandom().nextFloat()
                        < HearthwindSurvivalConfig.get().hydration.campfireRainFillChance
                && state.getValue(LEVEL) < 4) {
            setLevel(level, pos, state, state.getValue(LEVEL) + 1);
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
