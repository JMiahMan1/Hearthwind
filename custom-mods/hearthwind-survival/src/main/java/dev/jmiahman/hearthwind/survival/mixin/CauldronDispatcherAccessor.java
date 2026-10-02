package dev.jmiahman.hearthwind.survival.mixin;

import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.item.Item;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * {@code CauldronInteraction.Dispatcher#put} is package-private in 26.2, so an
 * outside mod cannot register a new bucket/fluid row. Reference
 * {@code CauldronBehaviorMixin} achieves the same thing from inside vanilla's
 * own package with an {@code @Inject} at the tail of
 * {@code registerBucketBehavior}; the 26.2 equivalent is this invoker.
 */
@Mixin(CauldronInteraction.Dispatcher.class)
public interface CauldronDispatcherAccessor {
    @Invoker("put")
    void hearthwind$put(Item item, CauldronInteraction interaction);
}
