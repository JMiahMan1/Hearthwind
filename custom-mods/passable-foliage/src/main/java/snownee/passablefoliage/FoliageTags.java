package snownee.passablefoliage;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

/**
 * Replaces upstream's Kiwi {@code CoreModule} ({@code @KiwiModule} +
 * {@code AbstractModule.blockTag}). The tag id is unchanged, so the shipped
 * {@code data/passablefoliage/tags/block/passables.json} (whitelist:
 * {@code #minecraft:leaves}) activates exactly as upstream.
 */
public final class FoliageTags {
	private FoliageTags() {}

	public static final TagKey<Block> PASSABLES = TagKey.create(Registries.BLOCK, PassableFoliage.id("passables"));
}
