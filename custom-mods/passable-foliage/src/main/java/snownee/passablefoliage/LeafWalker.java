package snownee.passablefoliage;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Unit;

/**
 * Replaces upstream's Kiwi {@code EnchantmentModule} ({@code @KiwiModule} +
 * {@code KiwiGO}). The enchantment effect component type id
 * ({@code passablefoliage:leaf_walker}) is unchanged, so the shipped
 * {@code data/passablefoliage/enchantment/leaf_walker.json} keeps working.
 *
 * <p>Upstream's module was {@code @KiwiModule.Optional} (disableable via Kiwi
 * config); this port has no module system, so the enchantment is always
 * enabled (upstream default). The data-side {@code kiwi:is_loaded} load
 * conditions are therefore dropped from the shipped JSONs.
 */
public final class LeafWalker {
	private LeafWalker() {}

	public static DataComponentType<Unit> TYPE;

	public static void register() {
		TYPE = Registry.register(
				BuiltInRegistries.ENCHANTMENT_EFFECT_COMPONENT_TYPE,
				PassableFoliage.id("leaf_walker"),
				DataComponentType.<Unit>builder().persistent(Unit.CODEC).build());
		PassableFoliage.enchantmentEnabled = true;
	}
}
