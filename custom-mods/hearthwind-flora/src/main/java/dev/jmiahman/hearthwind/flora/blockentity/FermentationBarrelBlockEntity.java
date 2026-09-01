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

public class FermentationBarrelBlockEntity extends BlockEntity {
    private final NonNullList<ItemStack> items = NonNullList.withSize(6, ItemStack.EMPTY);
    private int fermentationProgress = 0;
    private static final int MAX_PROGRESS = 160; // 8 seconds per batch

    public FermentationBarrelBlockEntity(BlockPos pos, BlockState state) {
        super(FloraBlockEntities.FERMENTATION_BARREL, pos, state);
    }

    private static Item getItem(String ns, String path) {
        return BuiltInRegistries.ITEM.getOptional(Identifier.fromNamespaceAndPath(ns, path)).orElse(Items.AIR);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, FermentationBarrelBlockEntity be) {
        if (level.isClientSide()) {
            if (be.isFermenting()) {
                if (level.getRandom().nextFloat() < 0.25f) {
                    level.addParticle(ParticleTypes.BUBBLE_POP,
                            pos.getX() + 0.3 + level.getRandom().nextDouble() * 0.4,
                            pos.getY() + 0.85,
                            pos.getZ() + 0.3 + level.getRandom().nextDouble() * 0.4,
                            0.0, 0.02, 0.0);
                }
            }
            return;
        }

        if (be.canFerment()) {
            be.fermentationProgress++;
            if (be.fermentationProgress >= MAX_PROGRESS) {
                be.completeFermentation();
                be.fermentationProgress = 0;
            }
            be.setChanged();
        } else {
            if (be.fermentationProgress > 0) {
                be.fermentationProgress = 0;
                be.setChanged();
            }
        }
    }

    public boolean isFermenting() {
        return canFerment();
    }

    private boolean canFerment() {
        ItemStack juice = items.get(0);
        ItemStack bottle = items.get(4);
        ItemStack output = items.get(5);

        boolean hasJuice = !juice.isEmpty() && (juice.is(getItem("vinery", "red_grape"))
                || juice.is(getItem("vinery", "white_grape"))
                || juice.is(getItem("vinery", "grape_juice"))
                || juice.is(getItem("vinery", "apple_juice"))
                || juice.is(getItem("vinery", "cherry")));

        boolean hasBottle = !bottle.isEmpty() && (bottle.is(Items.GLASS_BOTTLE) || bottle.is(getItem("vinery", "wine_bottle")));

        return hasJuice && hasBottle && (output.isEmpty() || output.getCount() < output.getMaxStackSize());
    }

    private void completeFermentation() {
        ItemStack juice = items.get(0);
        ItemStack bottle = items.get(4);
        ItemStack output = items.get(5);

        String resultWine = "red_wine";
        if (juice.is(getItem("vinery", "white_grape"))) {
            resultWine = "solaris_wine";
        } else if (juice.is(getItem("vinery", "cherry"))) {
            resultWine = "cherry_wine";
        } else if (juice.is(getItem("vinery", "apple_juice"))) {
            resultWine = "apple_wine";
        }

        ItemStack wineStack = new ItemStack(getItem("vinery", resultWine), 1);

        juice.shrink(1);
        bottle.shrink(1);

        if (output.isEmpty()) {
            items.set(5, wineStack);
        } else if (output.is(wineStack.getItem())) {
            output.grow(1);
        }

        if (level != null) {
            level.playSound(null, worldPosition, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);
        }
    }

    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);

        // 1. Extract output
        ItemStack output = items.get(5);
        if (!output.isEmpty() && (held.isEmpty() || player.isShiftKeyDown())) {
            if (!player.getInventory().add(output.copy())) {
                player.drop(output.copy(), false);
            }
            items.set(5, ItemStack.EMPTY);
            setChanged();
            if (level != null) level.playSound(null, worldPosition, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.8f, 1.2f);
            return InteractionResult.SUCCESS;
        }

        // 2. Insert bottles
        if (held.is(Items.GLASS_BOTTLE) || held.is(getItem("vinery", "wine_bottle"))) {
            ItemStack bottleSlot = items.get(4);
            if (bottleSlot.isEmpty()) {
                items.set(4, held.split(held.getCount()));
            } else if (bottleSlot.is(held.getItem())) {
                int add = Math.min(held.getCount(), bottleSlot.getMaxStackSize() - bottleSlot.getCount());
                bottleSlot.grow(add);
                held.shrink(add);
            }
            setChanged();
            if (level != null) level.playSound(null, worldPosition, SoundEvents.DISPENSER_DISPENSE, SoundSource.BLOCKS, 0.8f, 1.5f);
            return InteractionResult.SUCCESS;
        }

        // 3. Insert juice / grapes
        if (held.is(getItem("vinery", "red_grape"))
                || held.is(getItem("vinery", "white_grape"))
                || held.is(getItem("vinery", "grape_juice"))
                || held.is(getItem("vinery", "apple_juice"))
                || held.is(getItem("vinery", "cherry"))) {
            ItemStack juiceSlot = items.get(0);
            if (juiceSlot.isEmpty()) {
                items.set(0, held.split(held.getCount()));
            } else if (juiceSlot.is(held.getItem())) {
                int add = Math.min(held.getCount(), juiceSlot.getMaxStackSize() - juiceSlot.getCount());
                juiceSlot.grow(add);
                held.shrink(add);
            }
            setChanged();
            if (level != null) level.playSound(null, worldPosition, SoundEvents.SLIME_BLOCK_PLACE, SoundSource.BLOCKS, 0.8f, 1.1f);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("Progress", fermentationProgress);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        fermentationProgress = input.getIntOr("Progress", 0);
    }
}
