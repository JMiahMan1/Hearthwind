package net.satisfy.herbalbrews.core.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.satisfy.herbalbrews.core.blocks.entity.DrinkBlockEntity;
import net.satisfy.herbalbrews.core.registry.EntityTypeRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TeaCupBlock extends Block implements EntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape SHAPE = Shapes.box(0.125, 0, 0.125, 0.875, 0.875, 0.875);

    public TeaCupBlock(Properties settings) {
        super(settings);
        registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(world, pos, state, placer, stack);
        if (stack.has(DataComponents.POTION_CONTENTS)) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof DrinkBlockEntity drink) {
                PotionContents data = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
                drink.setComponents(DataComponentMap.builder().set(DataComponents.POTION_CONTENTS, data).build());
                be.setChanged();
            }
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return EntityTypeRegistry.DRINK_BLOCK_ENTITY.get().create(pos, state);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity be, ItemStack tool) {
        if (be instanceof DrinkBlockEntity drink && !player.getAbilities().instabuild) {
            ItemStack stack = new ItemStack(this.asItem());
            drink.components();
            if (!drink.components().isEmpty()) {
                stack.set(DataComponents.POTION_CONTENTS, drink.components().getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY));
            }
            popResource(level, pos, stack);
        }
        super.playerDestroy(level, player, pos, state, be, tool);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moved) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof DrinkBlockEntity) {
            level.removeBlockEntity(pos);
        }
        super.affectNeighborsAfterRemoval(state, level, pos, moved);
    }
}
