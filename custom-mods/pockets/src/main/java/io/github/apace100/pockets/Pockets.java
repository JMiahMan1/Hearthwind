package io.github.apace100.pockets;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class Pockets implements ModInitializer {

	public static final TagKey<Item> HAS_POCKET_TAG = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("pockets", "has_pockets"));
	public static final TagKey<Item> BLACKLIST_TAG = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("pockets", "pocket_blacklist"));

	@Override
	public void onInitialize() {
	}
}
