package net.satisfy.meadow.core.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.satisfy.meadow.core.registry.ObjectRegistry;

public class WoodenPowderSnowCauldronBlock extends LayeredCauldronBlock {

    public WoodenPowderSnowCauldronBlock(Biome.Precipitation precipitation, CauldronInteraction.Dispatcher dispatcher, Properties properties) {
        super(precipitation, dispatcher, properties);
    }

    @Override
    protected void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity, InsideBlockEffectApplier effectApplier, boolean blunt) {
        if (!level.isClientSide() && entity.isOnFire() && isEntityInsideContent(blockState, blockPos, entity)) {
            entity.clearFire();
            if (level instanceof ServerLevel serverLevel && entity.mayInteract(serverLevel, blockPos)) {
                lowerFillLevel(ObjectRegistry.WOODEN_WATER_CAULDRON.get().defaultBlockState().setValue(LEVEL, blockState.getValue(LEVEL)), level, blockPos);
            }
        }
    }

    private boolean isEntityInsideContent(BlockState state, BlockPos pos, Entity entity) {
        return entity.getBoundingBox().minY < pos.getY() + this.getContentHeight(state);
    }
}
