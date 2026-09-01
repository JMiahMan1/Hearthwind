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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class ApplePressBlockEntity extends BlockEntity {
    private final NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);
    private int mashingProgress = 0; // 0..4 presses

    public ApplePressBlockEntity(BlockPos pos, BlockState state) {
        super(FloraBlockEntities.APPLE_PRESS, pos, state);
    }

    private static Item getItem(String ns, String path) {
        return BuiltInRegistries.ITEM.getOptional(Identifier.fromNamespaceAndPath(ns, path)).orElse(Items.AIR);
    }

    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);

        // 1. Extract output
        ItemStack output = items.get(2);
        if (!output.isEmpty() && (held.isEmpty() || player.isShiftKeyDown())) {
            if (!player.getInventory().add(output.copy())) {
                player.drop(output.copy(), false);
            }
            items.set(2, ItemStack.EMPTY);
            setChanged();
            if (level != null) level.playSound(null, worldPosition, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.8f, 1.2f);
            return InteractionResult.SUCCESS;
        }

        // 2. Insert fruit
        if (held.is(Items.APPLE) || held.is(getItem("vinery", "red_grape")) || held.is(getItem("vinery", "white_grape"))) {
            ItemStack inputSlot = items.get(0);
            if (inputSlot.isEmpty()) {
                items.set(0, held.split(1));
                mashingProgress = 0;
                setChanged();
                if (level != null) level.playSound(null, worldPosition, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0f, 1.2f);
                return InteractionResult.SUCCESS;
            }
        }

        // 3. Crank press lever
        ItemStack input = items.get(0);
        if (!input.isEmpty() && output.isEmpty()) {
            mashingProgress++;
            if (level != null) {
                level.playSound(null, worldPosition, SoundEvents.WOODEN_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 0.8f, 0.8f + (mashingProgress * 0.1f));
                level.playSound(null, worldPosition, SoundEvents.HONEY_BLOCK_SLIDE, SoundSource.BLOCKS, 0.7f, 1.3f);
            }

            if (mashingProgress >= 4) {
                String juice = input.is(Items.APPLE) ? "apple_juice" : "grape_juice";
                items.set(2, new ItemStack(getItem("vinery", juice), 1));
                input.shrink(1);
                mashingProgress = 0;
                if (level != null) level.playSound(null, worldPosition, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);
            }
            setChanged();
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("Mashing", mashingProgress);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        mashingProgress = input.getIntOr("Mashing", 0);
    }
}
