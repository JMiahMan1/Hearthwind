package dev.jmiahman.hearthwind.primitive;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 26.2 port of earlystage's FlintBlock (MIT, Globox_Z): small flint shards
 * on the ground with a size variant property and facing. Right-clicking
 * with a shovel cycles the variant.
 */
public class HearthwindFlintBlock extends Block {

    public enum FlintVariant implements StringRepresentable {
        SMALL("small"), MEDIUM("medium");

        private final String name;

        FlintVariant(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    public static final EnumProperty<FlintVariant> FLINT_TYPE = EnumProperty.create("type", FlintVariant.class);
    public static final EnumProperty<Direction> FACING_PROPERTY = BlockStateProperties.HORIZONTAL_FACING;
    public static final net.minecraft.world.level.block.state.properties.BooleanProperty SNOWY = BlockStateProperties.SNOWY;
    private static final VoxelShape SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 3.0, 12.0);
    private static final List<Item> SHOVELS = List.of(Items.WOODEN_SHOVEL, Items.STONE_SHOVEL, Items.IRON_SHOVEL,
            Items.GOLDEN_SHOVEL, Items.DIAMOND_SHOVEL, Items.NETHERITE_SHOVEL);

    public HearthwindFlintBlock(Properties props) {
        super(props);
        registerDefaultState(this.stateDefinition.any()
                .setValue(FACING_PROPERTY, Direction.NORTH)
                .setValue(FLINT_TYPE, FlintVariant.MEDIUM)
                .setValue(SNOWY, false));
    }

    @Override
    public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        boolean snowy = isSnowy(level, pos);
        return this.defaultBlockState().setValue(FACING_PROPERTY, facing).setValue(SNOWY, snowy);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, net.minecraft.world.level.ScheduledTickAccess tickAccess,
            BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, net.minecraft.util.RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        }
        if (direction == Direction.UP || direction.getAxis().isHorizontal()) {
            boolean snowy = isSnowy(level, pos);
            if (state.getValue(SNOWY) != snowy) {
                state = state.setValue(SNOWY, snowy);
            }
        }
        return super.updateShape(state, level, tickAccess, pos, direction, neighborPos, neighborState, random);
    }

    private static boolean isSnowy(LevelReader level, BlockPos pos) {
        BlockState above = level.getBlockState(pos.above());
        if (above.is(net.minecraft.world.level.block.Blocks.SNOW)
                || above.is(net.minecraft.world.level.block.Blocks.SNOW_BLOCK)
                || above.is(net.minecraft.world.level.block.Blocks.POWDER_SNOW)) {
            return true;
        }
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockState side = level.getBlockState(pos.relative(dir));
            if (side.is(net.minecraft.world.level.block.Blocks.SNOW)
                    || side.is(net.minecraft.world.level.block.Blocks.SNOW_BLOCK)
                    || side.is(net.minecraft.world.level.block.Blocks.POWDER_SNOW)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected BlockState rotate(BlockState state, net.minecraft.world.level.block.Rotation rotation) {
        return state.setValue(FACING_PROPERTY, rotation.rotate(state.getValue(FACING_PROPERTY)));
    }

    @Override
    protected BlockState mirror(BlockState state, net.minecraft.world.level.block.Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING_PROPERTY)));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING_PROPERTY, FLINT_TYPE, SNOWY);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hitResult) {
        if (!SHOVELS.contains(stack.getItem()) && !stack.is(HearthwindPrimitiveItems.FLINT_SHOVEL)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            level.setBlock(pos, state.cycle(FLINT_TYPE), Block.UPDATE_ALL);
            if (player instanceof ServerPlayer sp && !sp.getAbilities().instabuild) {
                stack.hurtAndBreak(1, sp, hand == net.minecraft.world.InteractionHand.MAIN_HAND
                        ? net.minecraft.world.entity.EquipmentSlot.MAINHAND
                        : net.minecraft.world.entity.EquipmentSlot.OFFHAND);
            }
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    public void cycleState(BlockState state, LevelAccessor level, BlockPos pos) {
        level.setBlock(pos, state.cycle(FLINT_TYPE), Block.UPDATE_ALL);
    }
}
