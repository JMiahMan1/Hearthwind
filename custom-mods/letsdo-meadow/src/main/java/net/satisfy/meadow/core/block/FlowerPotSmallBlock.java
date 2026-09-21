package net.satisfy.meadow.core.block;

import net.satisfy.meadow.core.registry.TagRegistry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.satisfy.meadow.core.registry.StorageTypeRegistry;
import org.jetbrains.annotations.NotNull;

public class FlowerPotSmallBlock extends StorageBlock {
    protected static final VoxelShape SHAPE = Block.box(5.0, 0.0, 5.0, 11.0, 6.0, 11.0);

    public FlowerPotSmallBlock(Properties settings) {
        super(settings);
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public Identifier type() {
        return StorageTypeRegistry.FLOWER_POT_SMALL;
    }

    @Override
    public Direction[] unAllowedDirections() {
        return new Direction[0];
    }

    @Override
    public boolean canInsertStack(ItemStack itemStack) {
        return (itemStack.getItem() instanceof net.minecraft.world.item.BlockItem blockItem && blockItem.getBlock().defaultBlockState().is(net.minecraft.tags.BlockTags.SMALL_FLOWERS));
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public int getSection(Float aFloat, Float aFloat1) {
        return 0;
    }
}
