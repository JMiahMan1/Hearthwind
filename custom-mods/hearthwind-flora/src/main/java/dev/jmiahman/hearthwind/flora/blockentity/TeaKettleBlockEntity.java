package dev.jmiahman.hearthwind.flora.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TeaKettleBlockEntity extends BlockEntity {
    private final NonNullList<ItemStack> slots = NonNullList.withSize(3, ItemStack.EMPTY);
    private int simmerTime = 0;
    private static final int BREW_TIME = 100;

    public TeaKettleBlockEntity(BlockPos pos, BlockState state) {
        super(FloraBlockEntities.TEA_KETTLE, pos, state);
    }

    private static Item getItem(String ns, String path) {
        return BuiltInRegistries.ITEM.getOptional(Identifier.fromNamespaceAndPath(ns, path)).orElse(Items.AIR);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TeaKettleBlockEntity be) {
        boolean heated = isHeated(level, pos);

        if (level.isClientSide()) {
            if (heated && be.isSimmering()) {
                if (level.getRandom().nextFloat() < 0.4f) {
                    level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                            pos.getX() + 0.4 + level.getRandom().nextDouble() * 0.2,
                            pos.getY() + 0.5,
                            pos.getZ() + 0.4 + level.getRandom().nextDouble() * 0.2,
                            0.0, 0.02, 0.0);
                }
            }
            return;
        }

        if (heated && be.canBrew()) {
            be.simmerTime++;
            if (be.simmerTime >= BREW_TIME) {
                be.finishBrew();
                be.simmerTime = 0;
            }
            be.setChanged();
        } else {
            if (be.simmerTime > 0 && !be.canBrew()) {
                be.simmerTime = 0;
                be.setChanged();
            }
        }
    }

    private static boolean isHeated(Level level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        return below.is(Blocks.MAGMA_BLOCK) || below.is(Blocks.LAVA) || below.is(Blocks.FIRE)
                || (below.hasProperty(BlockStateProperties.LIT) && below.getValue(BlockStateProperties.LIT));
    }

    private boolean isSimmering() {
        return canBrew();
    }

    private boolean canBrew() {
        return !slots.get(0).isEmpty() && !slots.get(1).isEmpty() && slots.get(2).isEmpty();
    }

    private void finishBrew() {
        slots.get(0).shrink(1);
        slots.get(1).shrink(1);
        slots.set(2, new ItemStack(getItem("herbalbrews", "green_tea"), 1));
        if (level != null) {
            level.playSound(null, worldPosition, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0f, 1.3f);
        }
    }

    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);

        // 1. Extract brewed tea
        ItemStack out = slots.get(2);
        if (!out.isEmpty() && (held.isEmpty() || player.isShiftKeyDown())) {
            if (!player.getInventory().add(out.copy())) player.drop(out.copy(), false);
            slots.set(2, ItemStack.EMPTY);
            setChanged();
            if (level != null) level.playSound(null, worldPosition, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.8f, 1.2f);
            return InteractionResult.SUCCESS;
        }

        // 2. Insert water cup / bottle
        if (held.is(Items.GLASS_BOTTLE) || held.is(Items.POTION)) {
            if (slots.get(1).isEmpty()) {
                slots.set(1, held.split(1));
                setChanged();
                if (level != null) level.playSound(null, worldPosition, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 0.8f, 1.2f);
                return InteractionResult.SUCCESS;
            }
        }

        // 3. Insert tea leaves / herbs / flowers
        if (held.is(getItem("herbalbrews", "tea_blossom")) || held.is(getItem("herbalbrews", "lavender_blossom")) || held.is(Items.SWEET_BERRIES)) {
            if (slots.get(0).isEmpty()) {
                slots.set(0, held.split(1));
                setChanged();
                if (level != null) level.playSound(null, worldPosition, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 0.8f, 1.4f);
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("Simmer", simmerTime);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        simmerTime = input.getIntOr("Simmer", 0);
    }
}
