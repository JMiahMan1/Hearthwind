package net.satisfy.vinery.core.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.satisfy.vinery.core.util.GrapeType;

public class GrapeBushSeedItem extends BlockItem {
    private final GrapeType type;

    public GrapeBushSeedItem(Block block, Properties settings, GrapeType type) {
        super(block, settings);
        this.type = type;
    }

    public GrapeType getType() {
        return this.type;
    }

}