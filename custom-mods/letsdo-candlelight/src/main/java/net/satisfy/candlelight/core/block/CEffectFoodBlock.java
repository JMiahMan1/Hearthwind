package net.satisfy.candlelight.core.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.satisfy.candlelight.core.registry.EntityTypeRegistry;
import net.satisfy.farm_and_charm.core.block.EffectFoodBlock;
import net.satisfy.farm_and_charm.core.block.entity.EffectFoodBlockEntity;
import net.satisfy.farm_and_charm.core.item.food.EffectFoodHelper;

public class CEffectFoodBlock extends EffectFoodBlock {
    public CEffectFoodBlock(Properties properties, int maxBites, FoodProperties food) {
        super(properties, maxBites, food);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof EffectFoodBlockEntity e) {
            e.addEffects(EffectFoodHelper.getEffects(stack));
            e.setChanged();
        } else {
            super.setPlacedBy(level, pos, state, placer, stack);
        }
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EffectFoodBlockEntity(EntityTypeRegistry.EFFECT_FOOD_BLOCK_ENTITY.get(), pos, state);
    }
}
