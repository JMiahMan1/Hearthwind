package dev.jmiahman.hearthwind.world.endrem;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockPattern;

public class AncientEyeItem extends Item {
    public AncientEyeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);

        if (state.is(Blocks.END_PORTAL_FRAME) && !state.getValue(EndPortalFrameBlock.HAS_EYE)) {
            if (level.isClientSide()) {
                return InteractionResult.SUCCESS;
            }

            BlockState updated = state.setValue(EndPortalFrameBlock.HAS_EYE, true);
            level.setBlock(pos, updated, 2);
            level.updateNeighbourForOutputSignal(pos, Blocks.END_PORTAL_FRAME);

            context.getItemInHand().shrink(1);
            level.levelEvent(1503, pos, 0); // End portal frame filled sound & smoke

            // Check if portal ring is complete
            BlockPattern.BlockPatternMatch patternMatch = EndPortalFrameBlock.getOrCreatePortalShape().find(level, pos);
            if (patternMatch != null) {
                BlockPos portalCenter = patternMatch.getFrontTopLeft().offset(-3, 0, -3);
                for (int x = 0; x < 3; ++x) {
                    for (int z = 0; z < 3; ++z) {
                        level.setBlock(portalCenter.offset(x, 0, z), Blocks.END_PORTAL.defaultBlockState(), 2);
                    }
                }
                level.globalLevelEvent(1038, portalCenter.offset(1, 0, 1), 0); // End portal opened global sound
            }

            return InteractionResult.CONSUME;
        }

        return super.useOn(context);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (level instanceof ServerLevel serverLevel) {
            // Locate nearest Stronghold
            BlockPos strongholdPos = serverLevel.findNearestMapStructure(
                    net.minecraft.tags.StructureTags.EYE_OF_ENDER_LOCATED,
                    player.blockPosition(),
                    100,
                    false);

            if (strongholdPos != null) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ENDER_EYE_LAUNCH, SoundSource.NEUTRAL, 0.5f, 0.4f);
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
                return InteractionResult.SUCCESS_SERVER;
            }
        }
        return InteractionResult.FAIL;
    }
}
