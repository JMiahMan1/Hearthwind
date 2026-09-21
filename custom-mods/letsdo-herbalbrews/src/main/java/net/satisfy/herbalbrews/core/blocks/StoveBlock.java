package net.satisfy.herbalbrews.core.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SmokerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.satisfy.herbalbrews.core.blocks.entity.StoveBlockEntity;
import net.satisfy.herbalbrews.core.registry.EntityTypeRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StoveBlock extends SmokerBlock {

    public StoveBlock(Properties settings) {
        super(settings);
    }

    @Override
    public void stepOn(Level world, BlockPos pos, BlockState state, Entity entity) {
        boolean isLit = world.getBlockState(pos).getValue(LIT);
        if (isLit && !entity.fireImmune() && entity instanceof LivingEntity livingEntity) {
            livingEntity.hurt(world.damageSources().inFire(), 1.f);
        }

        super.stepOn(world, pos, state, entity);
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) return;

        double d = pos.getX() + 0.5;
        double e = pos.getY() + 0.24;
        double f = pos.getZ() + 0.5;

        if (random.nextDouble() < 0.4)
            world.playLocalSound(d, e, f, SoundEvents.SMOKER_SMOKE, SoundSource.BLOCKS, 0.25f, 1.0f, false);

        Direction direction = state.getValue(FACING);
        Direction.Axis axis = direction.getAxis();
        double h = random.nextDouble() * 0.6 - 0.3;
        double i = axis == Direction.Axis.X ? direction.getStepX() * 0.52 : h;
        double j = random.nextDouble() * 0.375;
        double k = axis == Direction.Axis.Z ? direction.getStepZ() * 0.52 : h;

        world.addParticle(ParticleTypes.SMOKE, d + i, e + j, f + k, 0.0, 0.0, 0.0);
        world.addParticle(ParticleTypes.FLAME, d + i, e + j, f + k, 0.0, 0.0, 0.0);
        world.addParticle(ParticleTypes.SMOKE, d, pos.getY() + 1.5, f, 0.0, 0.0, 0.0);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof MenuProvider provider) {
            player.openMenu(provider);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new StoveBlockEntity(pos, state);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createFurnaceTicker(level, blockEntityType, EntityTypeRegistry.STOVE_BLOCK_ENTITY.get());
    }
}



