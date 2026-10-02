package dev.jmiahman.hearthwind.survival.hydration;

import com.mojang.serialization.MapCodec;
import dev.jmiahman.hearthwind.survival.FlaskItems;
import dev.jmiahman.hearthwind.survival.HearthwindSurvivalConfig;
import dev.jmiahman.hearthwind.survival.LeatherFlaskItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Clean-room reimplementation of Dehydration's bamboo pump (upstream is
 * GPLv3, so the behaviour was decoded from the shipped bytecode and the art
 * is our own).
 *
 * <p>Right-clicking attaches the pump to the block below, stores a bucket,
 * bottle or leather flask in it, and squeezes water into it. Four pumps
 * purify a bucket, one purifies a bottle and a flask gains two units; the
 * pump then rests for {@code hydration.pumpCooldown} ticks. A pending
 * cooldown rides on the item so it survives breaking and re-placing.
 */
public class BambooPumpBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final MapCodec<BambooPumpBlock> CODEC = simpleCodec(BambooPumpBlock::new);
    /** Custom-data key the resting cooldown travels under. */
    public static final String COOLDOWN_TAG = "Cooldown";

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty EXTENDED = BlockStateProperties.EXTENDED;
    public static final BooleanProperty ATTACHED = BlockStateProperties.ATTACHED;
    private static final VoxelShape SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0);

    public BambooPumpBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(WATERLOGGED, false)
                .setValue(EXTENDED, false)
                .setValue(ATTACHED, true));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED, EXTENDED, ATTACHED);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
            Direction direction, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        if (direction == Direction.DOWN && !neighbourState.isFaceSturdy(level, neighbourPos, Direction.UP)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, ticks, pos, direction, neighbourPos, neighbourState, random);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof BambooPumpBlockEntity pump)) {
            return InteractionResult.PASS;
        }
        // The first use attaches the pump to its base.
        if (!state.getValue(ATTACHED)) {
            level.setBlock(pos, state.setValue(ATTACHED, true), 2);
            return InteractionResult.SUCCESS;
        }
        // Sneak with an empty hand to take the stored container back.
        if (player.isShiftKeyDown() && player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty() && !pump.isEmpty()) {
            player.setItemInHand(InteractionHand.MAIN_HAND, pump.takeStored());
            level.setBlock(pos, state.setValue(ATTACHED, false).setValue(EXTENDED, false), 2);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        if (!isContainer(stack) || !(level.getBlockEntity(pos) instanceof BambooPumpBlockEntity pump)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        // Refuse to work when no water is in sight and the option is on.
        if (HearthwindSurvivalConfig.get().hydration.pumpRequiresWater && !hasWaterAbove(level, pos)) {
            overlay(player, Component.translatable("block.dehydration.bamboo_pump.no_water"));
            return InteractionResult.SUCCESS_SERVER;
        }
        if (pump.getCooldown() > 0) {
            overlay(player, Component.translatable("block.dehydration.bamboo_pump.cooldown", pump.getCooldown() / 20));
            return InteractionResult.SUCCESS_SERVER;
        }
        if (pump.isEmpty()) {
            pump.setItem(0, stack.copyWithCount(1));
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return InteractionResult.SUCCESS_SERVER;
        }
        // Squeeze: the pump only runs while its spout is extended.
        boolean extended = state.getValue(EXTENDED);
        if (extended) {
            pump.increasePumpCount(1);
        }
        level.setBlock(pos, state.setValue(EXTENDED, !extended), 2);
        if (extended) {
            level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    /** Upstream sends these as overlay text; 26.2 uses the action bar packet. */
    private static void overlay(Player player, Component message) {
        if (player instanceof ServerPlayer serverPlayer && serverPlayer.connection != null) {
            serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(message));
        } else {
            player.sendSystemMessage(message);
        }
    }

    private static boolean isContainer(ItemStack stack) {
        if (stack.is(Items.BUCKET) || stack.is(Items.GLASS_BOTTLE)) {
            return true;
        }
        if (stack.getItem() instanceof LeatherFlaskItem flask) {
            var data = stack.get(FlaskItems.FLASK_DATA);
            return data == null || data.fillLevel() < flask.capacity();
        }
        return false;
    }

    /**
     * The reference scans {@code pos.add(0, i, 0)} for i = 0…49 - blocks
     * 0 through 49 straight up, counting the pump's own block - not blocks 10
     * to 59 like we did (0.1.49).
     */
    private static boolean hasWaterAbove(Level level, BlockPos pos) {
        for (int i = 0; i < 50; i++) {
            if (level.getFluidState(pos.above(i)).is(FluidTags.WATER)) {
                return true;
            }
        }
        return false;
    }

    // The reference keeps pump_cooldown on the block entity alone. We used to
    // ride a pending cooldown on the item NBT so it survived breaking and
    // replacing the pump; that is our own invention and the reference loses the
    // rest with the block, so it goes (0.1.49).

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        return new ItemStack(this);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof BambooPumpBlockEntity pump
                && !pump.isEmpty()) {
            ItemStack stored = pump.takeStored();
            if (!player.addItem(stored)) {
                player.drop(stored, false);
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(type, HydrationBlocks.BAMBOO_PUMP_ENTITY, BambooPumpBlockEntity::tick);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BambooPumpBlockEntity(pos, state);
    }
}
