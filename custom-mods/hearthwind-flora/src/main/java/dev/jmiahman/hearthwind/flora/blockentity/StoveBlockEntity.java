package dev.jmiahman.hearthwind.flora.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class StoveBlockEntity extends BlockEntity {
    private int burnTime = 0;

    public StoveBlockEntity(BlockPos pos, BlockState state) {
        super(FloraBlockEntities.STOVE, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, StoveBlockEntity be) {
        if (be.burnTime > 0) {
            be.burnTime--;
            if (level.isClientSide() && level.getRandom().nextFloat() < 0.35f) {
                level.addParticle(ParticleTypes.FLAME,
                        pos.getX() + 0.4 + level.getRandom().nextDouble() * 0.2,
                        pos.getY() + 0.3,
                        pos.getZ() + 0.4 + level.getRandom().nextDouble() * 0.2,
                        0.0, 0.01, 0.0);
            }
            if (be.burnTime == 0 && !level.isClientSide()) {
                if (state.hasProperty(BlockStateProperties.LIT)) {
                    level.setBlock(pos, state.setValue(BlockStateProperties.LIT, false), 3);
                }
            }
            be.setChanged();
        }
    }

    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);

        // Add fuel (coal, charcoal, stick)
        if (held.is(Items.COAL) || held.is(Items.CHARCOAL) || held.is(Items.STICK)) {
            int addedTime = held.is(Items.STICK) ? 200 : 1600;
            burnTime += addedTime;
            held.shrink(1);
            if (level != null && !level.isClientSide()) {
                BlockState state = getBlockState();
                if (state.hasProperty(BlockStateProperties.LIT)) {
                    level.setBlock(worldPosition, state.setValue(BlockStateProperties.LIT, true), 3);
                }
                level.playSound(null, worldPosition, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.8f, 1.0f);
            }
            setChanged();
            return InteractionResult.SUCCESS;
        }

        // Light with flint and steel
        if (held.is(Items.FLINT_AND_STEEL) && burnTime <= 0) {
            burnTime = 1200; // 60s starter burn
            EquipmentSlot slot = (hand == InteractionHand.MAIN_HAND) ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
            held.hurtAndBreak(1, player, slot);
            if (level != null && !level.isClientSide()) {
                BlockState state = getBlockState();
                if (state.hasProperty(BlockStateProperties.LIT)) {
                    level.setBlock(worldPosition, state.setValue(BlockStateProperties.LIT, true), 3);
                }
                level.playSound(null, worldPosition, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
            }
            setChanged();
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("BurnTime", burnTime);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        burnTime = input.getIntOr("BurnTime", 0);
    }
}
