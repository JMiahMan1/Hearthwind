package dev.jmiahman.hearthwind.primitive;

import dev.jmiahman.hearthwind.primitive.extra.ExtraBlastingRecipes;
import dev.jmiahman.hearthwind.primitive.tiered.ReforgeRegistry;
import dev.jmiahman.hearthwind.primitive.tiered.TierRegistry;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HearthwindPrimitive implements ModInitializer {
	public static final String MOD_ID = "hearthwind_primitive";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		HearthwindPrimitiveBlocks.init();
		HearthwindPrimitiveItems.init();
		HearthwindPrimitiveWorldgen.init();
		ExtraBlastingRecipes.init();
		TreeFelling.register();
		TierRegistry.init();
		ReforgeRegistry.init();
		TieredAffixes.init();
		SieveBlock.loadDrops();
		LeavesStickLoot.register();
		BeginnerForgiveness.register();
		HearthwindPrimitive.LOGGER.info("hearthwind-primitive: config loaded (beginnerDeathCount={}, craftRockCraftHits={}, oreSmeltingRemoval={})",
				HearthwindPrimitiveConfig.get().beginnerDeathCount,
				HearthwindPrimitiveConfig.get().craftRockCraftHits,
				HearthwindPrimitiveConfig.get().removeOreSmeltingRecipes);
	}
}
