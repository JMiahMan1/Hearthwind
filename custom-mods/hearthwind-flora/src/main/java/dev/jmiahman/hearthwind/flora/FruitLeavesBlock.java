package dev.jmiahman.hearthwind.flora;

import java.util.function.Supplier;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 26.2 Port of Let's Do: Vinery FruitLeavesBlock (Apple & Dark Cherry Leaves).
 * Features 4 growth stages (0..3). When fully ripe (stage 3 displaying fruits),
 * right-clicking harvests 1-2 fruits (cherries or apples), plays a picking sound,
 * and resets the leaves back to stage 0 so they can grow and fruit again.
 * Supports bone meal fertilization and random tick growth.
 */
public class FruitLeavesBlock extends LeavesBlock implements BonemealableBlock {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
    public static final MapCodec<FruitLeavesBlock> CODEC = simpleCodec(props -> new FruitLeavesBlock(props, () -> Items.APPLE));

    private final Supplier<Item> fruitSupplier;

    public FruitLeavesBlock(BlockBehaviour.Properties properties, Supplier<Item> fruitSupplier) {
        super(0.01f, properties);
        this.fruitSupplier = fruitSupplier;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(DISTANCE, 7)
                .setValue(PERSISTENT, false)
                .setValue(WATERLOGGED, false)
                .setValue(AGE, 0));
    }

    @Override
    public MapCodec<? extends LeavesBlock> codec() {
        return CODEC;
    }

    @Override
    protected void spawnFallingLeavesParticle(Level level, BlockPos pos, RandomSource random) {
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return state.getValue(DISTANCE) == 7 && !state.getValue(PERSISTENT) || state.getValue(AGE) < 3;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);
        if (!level.getBlockState(pos).is(this)) {
            return;
        }
        int age = state.getValue(AGE);
        if (age < 3 && level.getRawBrightness(pos.above(), 0) >= 9 && random.nextInt(5) == 0) {
            BlockState next = state.setValue(AGE, age + 1);
            level.setBlock(pos, next, Block.UPDATE_CLIENTS);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(next));
        }
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hitResult) {
        int age = state.getValue(AGE);
        if (age == 3) {
            if (level.isClientSide()) {
                return InteractionResult.SUCCESS;
            }
            int count = 1 + level.getRandom().nextInt(2);
            popResource(level, pos, new ItemStack(this.fruitSupplier.get(), count));
            level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 0.8F + level.getRandom().nextFloat() * 0.4F);
            BlockState resetState = state.setValue(AGE, 0);
            level.setBlock(pos, resetState, Block.UPDATE_ALL);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, resetState));
            return InteractionResult.SUCCESS_SERVER;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return state.getValue(AGE) < 3;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        int nextAge = Math.min(3, state.getValue(AGE) + 1);
        level.setBlock(pos, state.setValue(AGE, nextAge), Block.UPDATE_ALL);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(AGE);
    }
}
