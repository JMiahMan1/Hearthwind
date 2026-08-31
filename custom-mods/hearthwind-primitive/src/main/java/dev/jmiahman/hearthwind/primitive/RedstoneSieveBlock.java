package dev.jmiahman.hearthwind.primitive;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Redstone sieve: sieve with a powered input - while powered it sifts on its
 * own every {@code redstoneSieveTicks} game ticks. Right-clicking takes the
 * inserted stack back out instead of tapping (earlystage parity).
 */
public class RedstoneSieveBlock extends SieveBlock {

    public static final net.minecraft.world.level.block.state.properties.BooleanProperty POWERED =
            net.minecraft.world.level.block.state.properties.BlockStateProperties.POWERED;

    public RedstoneSieveBlock(BlockBehaviour.Properties props) {
        super(props);
        registerDefaultState(defaultBlockState()
                .setValue(POWERED, Boolean.FALSE)
                .setValue(FACING, net.minecraft.core.Direction.NORTH));
    }

    @Override
    public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext ctx) {
        return defaultBlockState()
                .setValue(POWERED, ctx.getLevel().hasNeighborSignal(ctx.getClickedPos()))
                .setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    public <T extends net.minecraft.world.level.block.entity.BlockEntity> BlockEntityTicker<T> getTicker(
            net.minecraft.world.level.Level level, BlockState state,
            net.minecraft.world.level.block.entity.BlockEntityType<T> type) {
        return type == HearthwindPrimitiveBlocks.SIEVE_ENTITY
                ? (world, pos, blockState, blockEntity) -> ((SieveBlockEntity) blockEntity).tickUpdate()
                : null;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos,
            Block neighborBlock, net.minecraft.world.level.redstone.Orientation orientation,
            boolean notify) {
        if (!level.isClientSide()) {
            boolean bl = state.getValue(POWERED);
            if (bl != level.hasNeighborSignal(pos)) {
                if (bl) {
                    level.scheduleTick(pos, this, 4);
                } else {
                    level.setBlock(pos, state.cycle(POWERED), 3);
                }
            }
        }
        super.neighborChanged(state, level, pos, neighborBlock, orientation, notify);
    }

    @Override
    protected void tick(BlockState state, net.minecraft.server.level.ServerLevel level,
            BlockPos pos, net.minecraft.util.RandomSource random) {
        if (state.getValue(POWERED) && !level.hasNeighborSignal(pos)) {
            level.setBlock(pos, state.setValue(POWERED, Boolean.FALSE), 3);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public InteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
            BlockPos pos, net.minecraft.world.entity.player.Player player,
            net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof SieveBlockEntity sieve) {
            ItemStack blockStack = sieve.getItem(0);
            if (blockStack.isEmpty()) {
                if (sieve.canPlaceItem(0, player.getItemInHand(hand))) {
                    sieve.refreshSieveCount();
                    if (!level.isClientSide()) {
                        sieve.setItem(0, new ItemStack(player.getItemInHand(hand).getItem(), 1));
                        if (!player.hasInfiniteMaterials()) {
                            player.getItemInHand(hand).shrink(1);
                        }
                    }
                    return InteractionResult.CONSUME;
                }
                return InteractionResult.PASS;
            } else {
                if (player.getItemInHand(hand).isEmpty()) {
                    if (!level.isClientSide()) {
                        player.setItemInHand(hand, blockStack);
                        sieve.clearContent();
                    }
                    return InteractionResult.CONSUME;
                }
            }
        }
        return InteractionResult.PASS;
    }
}
