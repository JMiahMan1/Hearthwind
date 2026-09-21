package net.satisfy.meadow.core.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteractions;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.satisfy.meadow.core.registry.ObjectRegistry;
import net.satisfy.meadow.core.util.WoodenCauldronBehavior;
import org.jetbrains.annotations.NotNull;

public class WoodenCauldronBlock extends AbstractCauldronBlock {
    public static final MapCodec<WoodenCauldronBlock> CODEC = simpleCodec(WoodenCauldronBlock::new);

    public WoodenCauldronBlock(Properties properties) {
        super(properties, CauldronInteractions.EMPTY);
    }

    @Override
    protected @NotNull InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.is(ObjectRegistry.WOODEN_WATER_BUCKET.get())) {
            return WoodenCauldronBehavior.fillCauldron(level, pos, player, hand, stack,
                    ObjectRegistry.WOODEN_WATER_CAULDRON.get().defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3),
                    SoundEvents.BUCKET_EMPTY, ObjectRegistry.WOODEN_BUCKET.get());
        }
        if (stack.is(Items.POWDER_SNOW_BUCKET)) {
            return WoodenCauldronBehavior.fillCauldron(level, pos, player, hand, stack,
                    ObjectRegistry.WOODEN_POWDER_SNOW_CAULDRON.get().defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3),
                    SoundEvents.BUCKET_EMPTY_POWDER_SNOW, Items.BUCKET);
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override
    protected @NotNull MapCodec<? extends AbstractCauldronBlock> codec() {
        return CODEC;
    }

    @Override
    public boolean isFull(BlockState state) {
        return false;
    }

    protected static boolean canFillWithPrecipitation(Level world, Biome.Precipitation precipitation) {
        if (precipitation == Biome.Precipitation.RAIN) {
            return world.getRandom().nextFloat() < 0.05f;
        }
        if (precipitation == Biome.Precipitation.SNOW) {
            return world.getRandom().nextFloat() < 0.1f;
        }
        return false;
    }

    @Override
    public void handlePrecipitation(BlockState state, Level world, BlockPos pos, Biome.Precipitation precipitation) {
        if (!WoodenCauldronBlock.canFillWithPrecipitation(world, precipitation)) {
            return;
        }
        if (precipitation == Biome.Precipitation.RAIN) {
            world.setBlockAndUpdate(pos, ObjectRegistry.WOODEN_WATER_CAULDRON.get().defaultBlockState());
            world.gameEvent(null, GameEvent.BLOCK_CHANGE, pos);
        } else if (precipitation == Biome.Precipitation.SNOW) {
            world.setBlockAndUpdate(pos, ObjectRegistry.WOODEN_POWDER_SNOW_CAULDRON.get().defaultBlockState());
            world.gameEvent(null, GameEvent.BLOCK_CHANGE, pos);
        }
    }

    @Override
    protected boolean canReceiveStalactiteDrip(Fluid fluid) {
        return true;
    }

    @Override
    protected void receiveStalactiteDrip(BlockState state, Level world, BlockPos pos, Fluid fluid) {
        if (fluid == Fluids.WATER) {
            BlockState blockState = ObjectRegistry.WOODEN_WATER_CAULDRON.get().defaultBlockState();
            world.setBlockAndUpdate(pos, blockState);
            world.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(blockState));
            world.levelEvent(LevelEvent.SOUND_DRIP_WATER_INTO_CAULDRON, pos, 0);
        }
    }
}
