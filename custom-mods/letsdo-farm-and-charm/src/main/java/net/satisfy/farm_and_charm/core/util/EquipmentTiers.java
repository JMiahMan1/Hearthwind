package net.satisfy.farm_and_charm.core.util;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.Block;

// 26.2: old Tier interface (getUses/getSpeed/...) is gone; ToolMaterial is now
// a record (incorrectBlocksForDrops, durability, speed, attackDamageBonus,
// enchantmentValue, repairItems). Same copper stats preserved (level 2,
// 200 uses, 5.0 speed, 2.0 damage, 12 enchant, copper-ingot repair).
public enum EquipmentTiers {
    COPPER;

    public ToolMaterial material() {
        return new ToolMaterial(BlockTags.INCORRECT_FOR_STONE_TOOL, 200, 5.0f, 2.0f, 12, ItemTags.COPPER_TOOL_MATERIALS);
    }

    public TagKey<Block> incorrectBlocksForDrops() {
        return BlockTags.INCORRECT_FOR_STONE_TOOL;
    }
}
