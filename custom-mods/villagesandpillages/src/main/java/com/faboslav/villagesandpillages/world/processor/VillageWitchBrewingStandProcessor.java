package com.faboslav.villagesandpillages.world.processor;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class VillageWitchBrewingStandProcessor implements StructureProcessor {
    public static final MapCodec<VillageWitchBrewingStandProcessor> CODEC = MapCodec.unit(VillageWitchBrewingStandProcessor::new);

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(
            LevelReader level,
            BlockPos offset,
            BlockPos pivot,
            BlockPos pos,
            StructureTemplate.StructureBlockInfo original,
            StructurePlaceSettings settings
    ) {
        if (!original.state().is(Blocks.BREWING_STAND)) {
            return original;
        }

        RandomSource randomSource = settings.getRandom(original.pos());
        CompoundTag nbt = original.nbt() == null ? new CompoundTag() : original.nbt().copy();
        nbt.putByte("Fuel", (byte) (randomSource.nextInt(10) + 1));
        ListTag itemsListTag = new ListTag();
        nbt.put("Items", itemsListTag);

        float randomFloat = randomSource.nextFloat();
        if (randomFloat < 0.25f) {
            addBrewingRecipe(itemsListTag, "minecraft:fermented_spider_eye", "minecraft:invisibility", randomSource);
        } else if (randomFloat < 0.50f) {
            addBrewingRecipe(itemsListTag, "minecraft:sugar", "minecraft:swiftness", randomSource);
        } else if (randomFloat < 0.75f) {
            addBrewingRecipe(itemsListTag, "minecraft:magma_cream", "minecraft:fire_resistance", randomSource);
        } else {
            addBrewingRecipe(itemsListTag, "minecraft:glistering_melon_slice", "minecraft:healing", randomSource);
        }

        return new StructureTemplate.StructureBlockInfo(original.pos(), original.state(), nbt);
    }

    private void addBrewingRecipe(ListTag itemsListTag, String inputItemId, String outputPotionId, RandomSource randomSource) {
        CompoundTag inputTag = new CompoundTag();
        inputTag.putByte("Slot", (byte) 3);
        inputTag.putString("id", inputItemId);
        inputTag.putByte("Count", (byte) (randomSource.nextInt(3) + 2));
        itemsListTag.add(inputTag);

        putPotionInSlot(itemsListTag, (byte) 1, outputPotionId);
        if (randomSource.nextFloat() < 0.75f) putPotionInSlot(itemsListTag, (byte) 0, outputPotionId);
        if (randomSource.nextFloat() < 0.50f) putPotionInSlot(itemsListTag, (byte) 2, outputPotionId);
    }

    private void putPotionInSlot(ListTag itemsListTag, byte slot, String potionId) {
        CompoundTag potionTag = new CompoundTag();
        potionTag.putByte("Slot", slot);
        potionTag.putString("id", "minecraft:potion");
        potionTag.putByte("Count", (byte) 1);
        CompoundTag tag = new CompoundTag();
        tag.putString("Potion", potionId);
        potionTag.put("tag", tag);
        itemsListTag.add(potionTag);
    }

    @Override
    public MapCodec<VillageWitchBrewingStandProcessor> codec() {
        return CODEC;
    }
}
