package dev.jmiahman.hearthwind.primitive;

import java.util.HashMap;
import java.util.Map;

import dev.jmiahman.hearthwind.primitive.mixin.AxeItemAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * Tree bark (earlystage parity): stripping a log with an axe yields the bark
 * as an item drop, and right-clicking the stripped variant with the bark
 * restores the log (preserving axis) while consuming the bark.
 */
public class BarkItem extends Item {

    public static final Map<Block, Item> BARK_ITEMS = new HashMap<>();
    private final Block logBlock;
    private final Block woodBlock;

    public BarkItem(Properties props, Block logBlock, Block woodBlock) {
        super(props);
        this.logBlock = logBlock;
        this.woodBlock = woodBlock;
        BARK_ITEMS.put(this.logBlock, this);
        if (this.woodBlock != null) {
            BARK_ITEMS.put(this.woodBlock, this);
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        Map<Block, Block> strippedBlocks = AxeItemAccessor.getStrippedBlocks();
        if (state.is(net.minecraft.tags.BlockTags.LOGS)
                && strippedBlocks.containsValue(block)
                && (strippedBlocks.get(this.logBlock) == block
                        || (this.woodBlock != null && strippedBlocks.get(this.woodBlock) == block))) {
            Player player = context.getPlayer();
            ItemStack stack = context.getItemInHand();
            if (player != null) {
                player.awardStat(Stats.ITEM_USED.get(this));
            }
            level.playSound(null, pos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0f, 1.0f);
            if (!level.isClientSide()) {
                BlockState restored = strippedBlocks.get(this.logBlock) == block
                        ? this.logBlock.defaultBlockState().setValue(
                                BlockStateProperties.AXIS, state.getValue(BlockStateProperties.AXIS))
                        : this.woodBlock.defaultBlockState();
                level.setBlock(pos, restored, 3);
                if (player != null && !player.hasInfiniteMaterials()) {
                    stack.shrink(1);
                }
            }
            return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
        }
        return super.useOn(context);
    }
}
