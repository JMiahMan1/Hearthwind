package net.satisfy.herbalbrews.core.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.satisfy.herbalbrews.core.registry.ObjectRegistry;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("deprecation")
public class CoffeeCropBlock extends CropBlock {
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 3);

    public CoffeeCropBlock(BlockBehaviour.Properties settings) {
        super(settings);
    }

    @Override
    protected @NotNull ItemLike getBaseSeedId() {
        return ObjectRegistry.COFFEE_BEANS.get();
    }

    @Override
    public int getMaxAge() {
        return 3;
    }

    @Override
    public @NotNull IntegerProperty getAgeProperty() {
        return AGE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    public @NotNull InteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        int i = blockState.getValue(AGE);
        boolean bl = i == 3;
        if (!bl && player.getItemInHand(interactionHand).is(Items.BONE_MEAL)) {
            return InteractionResult.PASS;
        } else if (i > 1) {
            int tomatoCount = level.getRandom().nextInt(2) + (bl ? 1 : 0);
            popResource(level, blockPos, new ItemStack(ObjectRegistry.COFFEE_BEANS.get(), tomatoCount));
            level.playSound(null, blockPos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 0.8F + level.getRandom().nextFloat() * 0.4F);
            level.setBlock(blockPos, blockState.setValue(AGE, 1), 2);
            return (level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME);
        } else {
            return super.useItemOn(itemStack, blockState, level, blockPos, player, interactionHand, blockHitResult);
        }
    }
}
