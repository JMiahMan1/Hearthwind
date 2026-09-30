package dev.jmiahman.hearthwind.survival.hydration;

import java.util.Map;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorageUtil;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Port of Dehydration 1.4.1 {@code AbstractCopperCauldronBlock} (the 1.3.6
 * class of the same name).
 *
 * <p>Shape, comparator signal, dripstone filling and the Fabric fluid
 * storage interaction are shared by every copper cauldron.
 */
public abstract class AbstractCopperCauldronBlock extends Block {
    private static final VoxelShape RAYCAST_SHAPE = Block.box(2.0D, 4.0D, 2.0D, 14.0D, 16.0D, 14.0D);
    protected static final VoxelShape OUTLINE_SHAPE = Shapes.join(Shapes.block(),
            Shapes.or(Block.box(0.0D, 0.0D, 4.0D, 16.0D, 3.0D, 12.0D),
                    Block.box(4.0D, 0.0D, 0.0D, 12.0D, 3.0D, 16.0D),
                    Block.box(2.0D, 0.0D, 2.0D, 14.0D, 3.0D, 14.0D),
                    RAYCAST_SHAPE),
            BooleanOp.ONLY_FIRST);

    private final Map<Item, CopperCauldronBehavior> behaviorMap;

    protected AbstractCopperCauldronBlock(BlockBehaviour.Properties properties,
            Map<Item, CopperCauldronBehavior> behaviorMap) {
        super(properties);
        this.behaviorMap = behaviorMap;
    }

    protected double getContentHeight(BlockState state) {
        return 0.0D;
    }

    protected boolean isEntityTouchingFluid(BlockState state, BlockPos pos, Entity entity) {
        return entity.getY() < (double) pos.getY() + this.getContentHeight(state)
                && entity.getBoundingBox().maxY > (double) pos.getY() + 0.25D;
    }

    /**
     * Test seam: {@link #useItemOn} is protected, and the hydration gametests
     * live in another package. Reflection keeps the production signature
     * protected rather than widening it for a test.
     */
    public InteractionResult hearthwind$useItemOnForTest(ItemStack stack, BlockState state, Level level,
            BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        return useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hitResult) {
        ItemStack itemStack = player.getItemInHand(hand);
        // A poured bottle bypasses the generic transfer API. It is not a fluid
        // container to that API, but it does carry water, so the storage would
        // otherwise swallow it, refund an empty container, and fill the cauldron
        // one level at a time - where the reference fills it straight to the top
        // and hands back a bowl. Only the potion is diverted here, so the
        // bucket, bottle and flask rows keep going through the storage exactly
        // as before.
        if (itemStack.is(net.minecraft.world.item.Items.POTION)) {
            CopperCauldronBehavior pour = this.behaviorMap.get(itemStack.getItem());
            if (pour != null) {
                return pour.interact(state, level, pos, player, hand, itemStack);
            }
        }
        Storage<FluidVariant> storage = FluidStorage.SIDED.find(level, pos,
                hitResult.getDirection().getOpposite());
        if (storage != null && FluidStorageUtil.interactWithFluidStorage(storage, player, hand)) {
            return InteractionResult.SUCCESS;
        }
        CopperCauldronBehavior behavior = this.behaviorMap.get(itemStack.getItem());
        return behavior == null ? InteractionResult.PASS
                : behavior.interact(state, level, pos, player, hand, itemStack);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return OUTLINE_SHAPE;
    }

    @Override
    protected VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return RAYCAST_SHAPE;
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    public abstract boolean isFull(BlockState state);

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
        return false;
    }

    protected void receiveStalactiteDrip(BlockState state, Level level, BlockPos pos, Fluid fluid) {
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state,
            boolean includeData) {
        return new ItemStack(HydrationBlocks.COPPER_CAULDRON);
    }
}
