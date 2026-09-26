package dev.jmiahman.hearthwind.survival.hydration;

import java.util.function.Consumer;

import dev.jmiahman.hearthwind.survival.FlaskItems;
import dev.jmiahman.hearthwind.survival.PurifiedWater;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.EmptyItemFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;

/**
 * Fabric fluid transfer storages for the hydration blocks and vessels,
 * ported from Dehydration 1.4.1 {@code FluidInit}:
 *
 * <ul>
 *   <li>empty {@code minecraft:bowl} ↔ {@code water_bowl} /
 *       {@code purified_water_bowl},</li>
 *   <li>empty glass bottle → purified water bottle, purified bottle →
 *       glass bottle,</li>
 *   <li>flasks on cauldrons,</li>
 *   <li>campfire cauldron and the copper cauldron family.</li>
 * </ul>
 */
public final class HydrationStorages {
    private HydrationStorages() {}

    public static void registerAll(Consumer<String> log) {
        FluidStorage.ITEM.registerForItems(BowlFluidStorage::new,
                Items.BOWL, HydrationItems.WATER_BOWL, HydrationItems.PURIFIED_WATER_BOWL);
        FluidStorage.ITEM.registerForItems(FlaskFluidStorage::new,
                FlaskItems.LEATHER_FLASK, FlaskItems.IRON_LEATHER_FLASK,
                FlaskItems.GOLDEN_LEATHER_FLASK, FlaskItems.DIAMOND_LEATHER_FLASK,
                FlaskItems.NETHERITE_LEATHER_FLASK);

        FluidStorage.combinedItemApiProvider(Items.GLASS_BOTTLE).register(context ->
                new EmptyItemFluidStorage(context, emptyBottle -> {
                    ItemStack newStack = emptyBottle.toStack();
                    newStack.set(DataComponents.POTION_CONTENTS,
                            new PotionContents(PurifiedWater.PURIFIED_POTION));
                    return ItemVariant.of(Items.POTION, newStack.getComponentsPatch());
                }, PurifiedWater.STILL, FluidConstants.BOTTLE));
        FluidStorage.combinedItemApiProvider(Items.POTION)
                .register(PurifiedWaterPotionStorage::find);

        FluidStorage.SIDED.registerForBlocks((world, pos, state, blockEntity, direction) ->
                new CampfireCauldronFluidStorage(world, pos, state,
                        (CampfireCauldronBlockEntity) blockEntity),
                HydrationBlocks.CAMPFIRE_CAULDRON);
        FluidStorage.SIDED.registerForBlocks((world, pos, state, blockEntity, direction) ->
                new CopperCauldronFluidStorage(world, pos),
                HydrationBlocks.COPPER_CAULDRON, HydrationBlocks.COPPER_WATER_CAULDRON,
                HydrationBlocks.COPPER_PURIFIED_WATER_CAULDRON);

        log.accept("dehydration hydration fluid storages registered");
    }
}
