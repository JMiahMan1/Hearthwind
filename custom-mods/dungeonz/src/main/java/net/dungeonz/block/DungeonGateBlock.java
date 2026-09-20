package net.dungeonz.block;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import net.dungeonz.block.entity.DungeonGateEntity;
import net.dungeonz.init.BlockInit;
import net.dungeonz.init.ConfigInit;
import net.dungeonz.network.DungeonServerPacket;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.util.RandomSource;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionResult;

public class DungeonGateBlock extends BaseEntityBlock {

    public static BooleanProperty ENABLED = BlockStateProperties.ENABLED;
    public static final MapCodec<DungeonGateBlock> CODEC = DungeonGateBlock.simpleCodec(DungeonGateBlock::new);

    public DungeonGateBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.stateDefinition.any().setValue(ENABLED, true));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DungeonGateEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return DungeonGateBlock.createTickerHelper(type, BlockInit.DUNGEON_GATE_ENTITY, world.isClientSide() ? null : DungeonGateEntity::serverTick);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.level().getBlockEntity(pos) != null && player.level().getBlockEntity(pos) instanceof DungeonGateEntity) {
            DungeonGateEntity dungeonGateEntity = (DungeonGateEntity) player.level().getBlockEntity(pos);
            if (player.canUseGameMasterBlocks()) {
                if (!player.getItemInHand(hand).isEmpty() && player.getItemInHand(hand).getItem() instanceof BlockItem) {
                    dungeonGateEntity.setBlockId(BuiltInRegistries.BLOCK.getKey(((BlockItem) player.getItemInHand(hand).getItem()).getBlock()));
                    dungeonGateEntity.setChanged();
                } else if (player.isShiftKeyDown()) {
                    if (!world.isClientSide()) {
                        DungeonServerPacket.writeS2COpenOpScreenPacket((ServerPlayer) player, null, dungeonGateEntity);
                    }
                }
                return (world.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME);
            } else if (dungeonGateEntity.getUnlockItem() != null && player.getItemInHand(hand).is(dungeonGateEntity.getUnlockItem())) {
                if (!world.isClientSide()) {
                    if (!player.isCreative()) {
                        player.getItemInHand(hand).shrink(1);
                    }
                    dungeonGateEntity.unlockGate(pos);
                }
                return (world.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME);
            }

        }
        return super.useItemOn(stack, state, world, pos, player, hand, hit);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ENABLED);
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        if (state.getValue(ENABLED) && world.getBlockEntity(pos) != null && world.getBlockEntity(pos) instanceof DungeonGateEntity
                && ((DungeonGateEntity) world.getBlockEntity(pos)).getParticleEffect() != null) {
            ParticleUtils.spawnParticlesOnBlockFaces(world, pos, ((DungeonGateEntity) world.getBlockEntity(pos)).getParticleEffect(), UniformInt.of(0, 1));
        }
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        if (!state.getValue(ENABLED)) {
            return true;
        }
        return super.propagatesSkylightDown(state);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        if (state.getValue(ENABLED)) {
            return false;
        }
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        if (!state.getValue(ENABLED) && !ConfigInit.CONFIG.devMode) {
            return Shapes.empty();
        }
        return super.getShape(state, world, pos, context);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        if (!state.getValue(ENABLED)) {
            return Shapes.empty();
        }
        return super.getCollisionShape(state, world, pos, context);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

}
