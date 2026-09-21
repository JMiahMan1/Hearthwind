package net.satisfy.meadow.core.item;

import net.minecraft.world.item.Item;

/**
 * 26.2: vanilla {@code MilkBucketItem} is gone - {@code Items.MILK_BUCKET} is a
 * plain {@link Item} with the {@code Consumables.MILK_BUCKET} consumable
 * component (drink anim + sound, {@code ClearAllStatusEffectsConsumeEffect},
 * stats/criteria/particles/game event via {@code Consumable#onConsume}, empty
 * bucket return via craft remainder). This class is just a marker subclass so
 * the 7 wooden milk variants register as plain items; behavior comes from the
 * component + craft remainder wired in {@code ObjectRegistry}.
 */
public class WoodenMilkBucket extends Item {
    public WoodenMilkBucket(Properties settings) {
        super(settings);
    }
}
