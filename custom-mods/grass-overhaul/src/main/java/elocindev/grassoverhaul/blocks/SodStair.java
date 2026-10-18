package elocindev.grassoverhaul.blocks;

import elocindev.grassoverhaul.GOBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StairBlock;

public class SodStair extends StairBlock {
   public SodStair(Block blockType, String path) {
      super(blockType.defaultBlockState(), GOBlocks.props(blockType, path));
   }
}
