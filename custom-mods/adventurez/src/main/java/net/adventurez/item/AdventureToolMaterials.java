package net.adventurez.item;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.Block;

public final class AdventureToolMaterials {
    public static final ToolMaterial GILDED_NETHERITE = new ToolMaterial(
            BlockTags.INCORRECT_FOR_WOODEN_TOOL,
            2331,
            9.0F,
            4.0F,
            15,
            TagKey.create(net.minecraft.core.registries.Registries.ITEM,
                    net.minecraft.resources.Identifier.fromNamespaceAndPath("adventurez", "gilded_netherite_fragment")));

    private AdventureToolMaterials() {
    }
}
