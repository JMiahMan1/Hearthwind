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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class BrewstationBlockEntity extends BlockEntity {
    private final NonNullList<ItemStack> ingredients = NonNullList.withSize(4, ItemStack.EMPTY);
    private int brewingProgress = 0;
    private static final int MAX_PROGRESS = 140;

    public BrewstationBlockEntity(BlockPos pos, BlockState state) {
        super(FloraBlockEntities.BREWSTATION, pos, state);
    }

    private static Item getItem(String ns, String path) {
        return BuiltInRegistries.ITEM.getOptional(Identifier.fromNamespaceAndPath(ns, path)).orElse(Items.AIR);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BrewstationBlockEntity be) {
        if (level.isClientSide()) {
            if (be.isBrewing()) {
                if (level.getRandom().nextFloat() < 0.35f) {
                    level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                            pos.getX() + 0.3 + level.getRandom().nextDouble() * 0.4,
                            pos.getY() + 0.9,
                            pos.getZ() + 0.3 + level.getRandom().nextDouble() * 0.4,
                            0.0, 0.02, 0.0);
                }
            }
            return;
        }

        if (be.canBrew()) {
            be.brewingProgress++;
            if (be.brewingProgress >= MAX_PROGRESS) {
                be.finishBrewing();
                be.brewingProgress = 0;
            }
            be.setChanged();
        } else {
            if (be.brewingProgress > 0) {
                be.brewingProgress = 0;
                be.setChanged();
            }
        }
    }

    public boolean isBrewing() {
        return canBrew();
    }

    private boolean canBrew() {
        ItemStack grain = ingredients.get(0);
        ItemStack mug = ingredients.get(2);
        ItemStack output = ingredients.get(3);
        boolean hasGrain = !grain.isEmpty() && (grain.is(Items.WHEAT) || grain.is(getItem("farm_and_charm", "barley")));
        boolean hasMug = !mug.isEmpty() && (mug.is(Items.GLASS_BOTTLE) || mug.is(getItem("brewery", "beer_mug")));
        return hasGrain && hasMug && output.isEmpty();
    }

    private void finishBrewing() {
        ingredients.get(0).shrink(1);
        ingredients.get(2).shrink(1);
        ingredients.set(3, new ItemStack(getItem("brewery", "beer_wheat"), 1));
        if (level != null) {
            level.playSound(null, worldPosition, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0f, 1.0f);
        }
    }

    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);

        // 1. Extract finished beer
        ItemStack out = ingredients.get(3);
        if (!out.isEmpty() && (held.isEmpty() || player.isShiftKeyDown())) {
            if (!player.getInventory().add(out.copy())) player.drop(out.copy(), false);
            ingredients.set(3, ItemStack.EMPTY);
            setChanged();
            if (level != null) level.playSound(null, worldPosition, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.8f, 1.2f);
            return InteractionResult.SUCCESS;
        }

        // 2. Insert mug / bottle
        if (held.is(Items.GLASS_BOTTLE) || held.is(getItem("brewery", "beer_mug"))) {
            if (ingredients.get(2).isEmpty()) {
                ingredients.set(2, held.split(1));
                setChanged();
                if (level != null) level.playSound(null, worldPosition, SoundEvents.DISPENSER_DISPENSE, SoundSource.BLOCKS, 0.8f, 1.3f);
                return InteractionResult.SUCCESS;
            }
        }

        // 3. Insert grains (wheat / barley)
        if (held.is(Items.WHEAT) || held.is(getItem("farm_and_charm", "barley"))) {
            if (ingredients.get(0).isEmpty()) {
                ingredients.set(0, held.split(1));
                setChanged();
                if (level != null) level.playSound(null, worldPosition, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 0.8f, 1.1f);
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("Progress", brewingProgress);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        brewingProgress = input.getIntOr("Progress", 0);
    }
}
