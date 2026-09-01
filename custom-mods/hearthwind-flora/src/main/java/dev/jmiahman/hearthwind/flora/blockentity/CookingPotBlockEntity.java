package dev.jmiahman.hearthwind.flora.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class CookingPotBlockEntity extends BlockEntity {
    private final NonNullList<ItemStack> ingredients = NonNullList.withSize(6, ItemStack.EMPTY);
    private ItemStack finishedMeal = ItemStack.EMPTY;
    private int cookingTimer = 0;
    private static final int COOK_TIME = 100; // 5 seconds on heat

    public CookingPotBlockEntity(BlockPos pos, BlockState state) {
        super(FloraBlockEntities.COOKING_POT, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CookingPotBlockEntity be) {
        boolean heated = isHeated(level, pos);

        if (level.isClientSide()) {
            if (heated && be.hasIngredients()) {
                if (level.getRandom().nextFloat() < 0.4f) {
                    level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                            pos.getX() + 0.3 + level.getRandom().nextDouble() * 0.4,
                            pos.getY() + 0.7,
                            pos.getZ() + 0.3 + level.getRandom().nextDouble() * 0.4,
                            0.0, 0.03, 0.0);
                    level.addParticle(ParticleTypes.SPLASH,
                            pos.getX() + 0.3 + level.getRandom().nextDouble() * 0.4,
                            pos.getY() + 0.5,
                            pos.getZ() + 0.3 + level.getRandom().nextDouble() * 0.4,
                            0.0, 0.01, 0.0);
                }
            }
            return;
        }

        if (heated && be.hasIngredients() && be.finishedMeal.isEmpty()) {
            be.cookingTimer++;
            if (be.cookingTimer >= COOK_TIME) {
                be.finishCooking();
                be.cookingTimer = 0;
            }
            be.setChanged();
        } else {
            if (be.cookingTimer > 0 && !be.hasIngredients()) {
                be.cookingTimer = 0;
                be.setChanged();
            }
        }
    }

    private static boolean isHeated(Level level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        if (below.is(Blocks.MAGMA_BLOCK) || below.is(Blocks.LAVA) || below.is(Blocks.FIRE) || below.is(Blocks.SOUL_FIRE)) {
            return true;
        }
        if (below.hasProperty(BlockStateProperties.LIT) && below.getValue(BlockStateProperties.LIT)) {
            return true;
        }
        return false;
    }

    private boolean hasIngredients() {
        int count = 0;
        for (ItemStack st : ingredients) {
            if (!st.isEmpty()) count++;
        }
        return count >= 2;
    }

    private void finishCooking() {
        for (int i = 0; i < ingredients.size(); i++) {
            ingredients.set(i, ItemStack.EMPTY);
        }
        finishedMeal = new ItemStack(Items.MUSHROOM_STEW, 1);
        if (level != null) {
            level.playSound(null, worldPosition, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0f, 1.0f);
        }
    }

    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);

        // 1. Scoop finished meal with bowl
        if (!finishedMeal.isEmpty() && held.is(Items.BOWL)) {
            held.shrink(1);
            ItemStack meal = finishedMeal.copy();
            finishedMeal = ItemStack.EMPTY;
            if (!player.getInventory().add(meal)) {
                player.drop(meal, false);
            }
            setChanged();
            if (level != null) level.playSound(null, worldPosition, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0f, 1.2f);
            return InteractionResult.SUCCESS;
        }

        // 2. Add raw ingredients
        if (!held.isEmpty() && (held.getItem().components().has(net.minecraft.core.component.DataComponents.FOOD) || held.is(Items.WHEAT) || held.is(Items.EGG))) {
            for (int i = 0; i < ingredients.size(); i++) {
                if (ingredients.get(i).isEmpty()) {
                    ingredients.set(i, held.split(1));
                    setChanged();
                    if (level != null) level.playSound(null, worldPosition, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.8f, 1.4f);
                    return InteractionResult.SUCCESS;
                }
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("Cooking", cookingTimer);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        cookingTimer = input.getIntOr("Cooking", 0);
    }
}
