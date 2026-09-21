package net.satisfy.meadow.core.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteractions;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.satisfy.meadow.core.registry.ObjectRegistry;
import net.satisfy.meadow.core.util.WoodenCauldronBehavior;
import org.jetbrains.annotations.NotNull;

public class WoodenWaterCauldronBlock extends LayeredCauldronBlock {
    public static final MapCodec<WoodenWaterCauldronBlock> CODEC = simpleCodec(WoodenWaterCauldronBlock::new);

    public WoodenWaterCauldronBlock(Biome.Precipitation precipitation, Properties properties) {
        super(precipitation, CauldronInteractions.WATER, properties);
    }

    public WoodenWaterCauldronBlock(Properties properties) {
        this(Biome.Precipitation.RAIN, properties);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull MapCodec<LayeredCauldronBlock> codec() {
        return (MapCodec<LayeredCauldronBlock>) (MapCodec<?>) CODEC;
    }

    @Override
    protected @NotNull InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.is(ObjectRegistry.WOODEN_BUCKET.get())) {
            return WoodenCauldronBehavior.takeWoodenWater(state, level, pos, player, hand, stack);
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }
}
