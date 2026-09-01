package dev.jmiahman.hearthwind.flora.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
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

public class CheeseRackBlockEntity extends BlockEntity {
    private final NonNullList<ItemStack> cheeses = NonNullList.withSize(4, ItemStack.EMPTY);
    private int agingTicks = 0;
    private static final int AGING_TIME = 2400; // 2 minutes to fully age cheese wheel

    public CheeseRackBlockEntity(BlockPos pos, BlockState state) {
        super(FloraBlockEntities.CHEESE_RACK, pos, state);
    }

    private static Item getItem(String ns, String path) {
        return BuiltInRegistries.ITEM.getOptional(Identifier.fromNamespaceAndPath(ns, path)).orElse(Items.AIR);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CheeseRackBlockEntity be) {
        if (level.isClientSide()) return;

        boolean hasCurd = false;
        for (ItemStack st : be.cheeses) {
            if (st.is(getItem("meadow", "raw_cheese")) || st.is(getItem("meadow", "cheese_form"))) {
                hasCurd = true;
                break;
            }
        }

        if (hasCurd) {
            be.agingTicks++;
            if (be.agingTicks >= AGING_TIME) {
                be.ageCheese();
                be.agingTicks = 0;
            }
            be.setChanged();
        }
    }

    private void ageCheese() {
        for (int i = 0; i < cheeses.size(); i++) {
            ItemStack st = cheeses.get(i);
            if (st.is(getItem("meadow", "raw_cheese")) || st.is(getItem("meadow", "cheese_form"))) {
                cheeses.set(i, new ItemStack(getItem("meadow", "cheese_wheel"), 1));
            }
        }
        if (level != null) {
            level.playSound(null, worldPosition, SoundEvents.HONEY_BLOCK_PLACE, SoundSource.BLOCKS, 1.0f, 1.0f);
        }
    }

    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);

        // 1. Extract aged cheese
        for (int i = 0; i < cheeses.size(); i++) {
            if (cheeses.get(i).is(getItem("meadow", "cheese_wheel")) && (held.isEmpty() || player.isShiftKeyDown())) {
                ItemStack aged = cheeses.get(i).copy();
                cheeses.set(i, ItemStack.EMPTY);
                if (!player.getInventory().add(aged)) player.drop(aged, false);
                setChanged();
                if (level != null) level.playSound(null, worldPosition, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.8f, 1.2f);
                return InteractionResult.SUCCESS;
            }
        }

        // 2. Place fresh cheese curd to age
        if (held.is(getItem("meadow", "raw_cheese")) || held.is(getItem("meadow", "cheese_form")) || held.is(Items.MILK_BUCKET)) {
            for (int i = 0; i < cheeses.size(); i++) {
                if (cheeses.get(i).isEmpty()) {
                    if (held.is(Items.MILK_BUCKET)) {
                        cheeses.set(i, new ItemStack(getItem("meadow", "raw_cheese"), 1));
                        held.shrink(1);
                        if (!player.getInventory().add(new ItemStack(Items.BUCKET))) player.drop(new ItemStack(Items.BUCKET), false);
                    } else {
                        cheeses.set(i, held.split(1));
                    }
                    setChanged();
                    if (level != null) level.playSound(null, worldPosition, SoundEvents.SLIME_BLOCK_PLACE, SoundSource.BLOCKS, 0.8f, 1.1f);
                    return InteractionResult.SUCCESS;
                }
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("Aging", agingTicks);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        agingTicks = input.getIntOr("Aging", 0);
    }
}
