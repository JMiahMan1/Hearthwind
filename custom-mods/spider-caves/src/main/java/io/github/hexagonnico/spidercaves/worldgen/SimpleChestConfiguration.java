package io.github.hexagonnico.spidercaves.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.storage.loot.LootTable;

public record SimpleChestConfiguration(BlockStateProvider chest, ResourceKey<LootTable> lootTable) implements FeatureConfiguration {
	public static final Codec<SimpleChestConfiguration> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
							BlockStateProvider.CODEC.fieldOf("chest").forGetter(SimpleChestConfiguration::chest),
							ResourceKey.codec(Registries.LOOT_TABLE).fieldOf("lootTable").forGetter(SimpleChestConfiguration::lootTable)
					)
					.apply(instance, SimpleChestConfiguration::new)
	);
}
