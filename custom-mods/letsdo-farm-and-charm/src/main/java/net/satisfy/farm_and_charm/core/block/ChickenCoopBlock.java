package net.satisfy.farm_and_charm.core.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.satisfy.farm_and_charm.core.block.entity.ChickenCoopBlockEntity;
import net.satisfy.farm_and_charm.core.registry.ObjectRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ChickenCoopBlock extends BaseEntityBlock {
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final IntegerProperty EGGS = IntegerProperty.create("eggs", 0, 3);
    public static final MapCodec<ChickenCoopBlock> CODEC = simpleCodec(ChickenCoopBlock::new);

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public ChickenCoopBlock(BlockBehaviour.Properties settings) {
        super(settings);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(EGGS, 0));
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite()).setValue(EGGS, 0);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, EGGS);
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new ChickenCoopBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : (lvl, pos, st, be) -> {
            if (be instanceof ChickenCoopBlockEntity coop) {
                ChickenCoopBlockEntity.tick(lvl, pos, coop);
                int eggCount = coop.getEggCount();
                int stage = eggCount >= 7 ? 3 : eggCount >= 4 ? 2 : eggCount >= 1 ? 1 : 0;
                BlockState blockState = lvl.getBlockState(pos);
                if (blockState.getBlock() instanceof ChickenCoopBlock && blockState.getValue(EGGS) != stage) {
                    lvl.setBlock(pos, blockState.setValue(EGGS, stage), Block.UPDATE_CLIENTS);
                }
            }
        };
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, net.minecraft.server.level.ServerLevel level, BlockPos pos, boolean moved) {
        {
            super.affectNeighborsAfterRemoval(state, level, pos, moved);
        }
    }

    @Override
    public @NotNull BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof ChickenCoopBlockEntity coop) {
                boolean hasData = !coop.getStoredChickens().isEmpty() || coop.getEggCount() > 0;
                if (hasData || !player.isCreative()) {
                    ItemStack stack = new ItemStack(ObjectRegistry.CHICKEN_COOP_ITEM.get());

                    stack.remove(DataComponents.ENTITY_DATA);

                    if (hasData) {
                        var _out = net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING, level.registryAccess());
                        coop.saveTo(_out);
                        CompoundTag tag = _out.buildResult();
                        tag.putString("id", Objects.requireNonNull(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(coop.getType())).toString());
                        stack.set(DataComponents.BLOCK_ENTITY_DATA, net.minecraft.world.item.component.TypedEntityData.of(coop.getType(), tag));
                    }

                    stack.remove(DataComponents.ENTITY_DATA);
                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
                }
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected @NotNull InteractionResult useItemOn(ItemStack heldItem, BlockState state, Level level, BlockPos pos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ChickenCoopBlockEntity coop) {
                if (heldItem.is(ObjectRegistry.PITCHFORK.get()) && !coop.getStoredChickens().isEmpty()) {
                    coop.releaseAllChickens();
                    level.playSound(null, pos, SoundEvents.ANVIL_FALL, player.getSoundSource(), 1.0F, 1.1F);
                    level.playSound(null, pos, SoundEvents.GENERIC_HURT, player.getSoundSource(), 0.325F, 0.825F);
                    level.playSound(null, pos, SoundEvents.BEEHIVE_EXIT, player.getSoundSource(), 0.7F, 1.1F);
                    return InteractionResult.SUCCESS;
                }

                int eggCount = coop.getEggCount();
                if (eggCount > 0) {
                    player.addItem(Items.EGG.getDefaultInstance().copyWithCount(eggCount));
                    coop.clearEggs();
                    coop.setChanged();
                    level.setBlock(pos, state.setValue(EGGS, 0), Block.UPDATE_CLIENTS);
                    return InteractionResult.SUCCESS;
                }

                if (coop.hasSpaceForChicken()) {
                    for (Chicken chicken : level.getEntitiesOfClass(Chicken.class, new AABB(pos).inflate(7.0))) {
                        if (chicken.isLeashed() && chicken.getLeashHolder() == player && chicken.isAlive()) {
                            coop.addChicken(chicken);
                            chicken.dropLeash();
                            level.playSound(null, pos, SoundEvents.BEEHIVE_ENTER, player.getSoundSource(), 1.0F, 1.0F);
                            return InteractionResult.SUCCESS;
                        }
                    }
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.or(Block.box(1, 0, 1, 15, 12, 15), Block.box(0, 12, 0, 16, 14, 16));
    }
}
