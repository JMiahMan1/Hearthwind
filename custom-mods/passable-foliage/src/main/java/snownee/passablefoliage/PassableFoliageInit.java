package snownee.passablefoliage;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;

/**
 * Replaces upstream's Kiwi-driven {@code util.CommonProxy} ({@code @Mod} +
 * Kiwi module scan) with a plain Fabric initializer. Same runtime wiring:
 * resource-condition type registration, config load, enchantment effect
 * component registration, and passable-flag computation on tags load.
 */
public class PassableFoliageInit implements ModInitializer {

	@Override
	public void onInitialize() {
		PassableFoliageCommonConfig.load();
		LeafWalker.register();
		ResourceConditions.register(AlwaysLeafWalkingCondition.TYPE);
		CommonLifecycleEvents.TAGS_LOADED.register((registryAccess, client) -> {
			PassableFoliage.tagsLoaded();
		});
	}
}
