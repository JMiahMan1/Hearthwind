package dev.jmiahman.hearthwind.survival;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Bare-hand drinking (Aged / Dehydration parity):
 * Sneak + empty main hand + hold right-click looking at still water.
 * ~21 use-events (~4s) with a drink sound every 3rd; completion restores
 * hydration, may inflict thirst (halved in rivers), and consumes a still
 * source block. Audio-only feedback, no chat messages.
 */
public final class BareHandDrinkHandler {
    private static final Map<UUID, Integer> drinkTime = new ConcurrentHashMap<>();

    private BareHandDrinkHandler() {}

    public static void register() {
        UseBlockCallback.EVENT.register(BareHandDrinkHandler::trySipBlock);
        UseItemCallback.EVENT.register(BareHandDrinkHandler::trySipItem);
    }

    private static InteractionResult trySipItem(Player player, Level level, InteractionHand hand) {
        return trySip(player, level);
    }

    private static InteractionResult trySipBlock(Player player, Level level, InteractionHand hand,
            BlockHitResult clicked) {
        return trySip(player, level);
    }

    public static InteractionResult trySip(Player player, Level level) {
        if (!player.getMainHandItem().isEmpty() || player.isSpectator() || !player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        BlockPos water = findWater(player, level);
        if (water == null) {
            return InteractionResult.PASS;
        }

        if (HearthwindSurvivalThirst.hydration(player) >= HearthwindSurvivalThirst.MAX_HYDRATION) {
            return InteractionResult.PASS;
        }

        HearthwindSurvivalConfig.BareHand cfg = HearthwindSurvivalConfig.get().bareHand;
        FluidState fluid = level.getFluidState(water);
        boolean still = fluid.isSource();
        if (!still && !cfg.allowNonFlowingWaterSip) {
            return InteractionResult.PASS;
        }

        if (player instanceof ServerPlayer sp && level instanceof ServerLevel server) {
            int time = drinkTime.getOrDefault(player.getUUID(), 0);
            if (time % 3 == 0) {
                level.playSound(null, sp.getX(), sp.getY(), sp.getZ(),
                        SoundEvents.GENERIC_DRINK.value(), SoundSource.PLAYERS, 0.5f,
                        0.9f + sp.getRandom().nextFloat() * 0.2f);
            }
            if (time > 20) {
                drinkTime.put(player.getUUID(), 0);
                completeSip(sp, server, water, still);
            } else {
                drinkTime.put(player.getUUID(), time + 1);
            }
        }

        player.swing(InteractionHand.MAIN_HAND, true);
        return InteractionResult.SUCCESS;
    }

    /** Fluid-including eye raycast + proximity check; returns the water position or null. */
    public static BlockPos findWater(Player player, Level level) {
        HitResult ray = player.pick(player.blockInteractionRange(), 0.0f, true);
        if (ray != null && ray.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = ((BlockHitResult) ray).getBlockPos();
            if (level.getFluidState(pos).is(FluidTags.WATER) || level.getBlockState(pos).is(Blocks.WATER)) {
                return pos;
            }
            // Check if block above or clicked block is water / waterlogged / water cauldron
            if (level.getFluidState(pos.above()).is(FluidTags.WATER) || level.getBlockState(pos.above()).is(Blocks.WATER)) {
                return pos.above();
            }
            BlockState state = level.getBlockState(pos);
            if (state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED)) {
                return pos;
            }
            if (state.is(Blocks.WATER_CAULDRON) && state.getValue(net.minecraft.world.level.block.LayeredCauldronBlock.LEVEL) > 0) {
                return pos;
            }
        }

        // Proximity fallback: check within 1 block of player (feet, below, around)
        BlockPos center = BlockPos.containing(player.getX(), player.getY(), player.getZ());
        for (BlockPos check : BlockPos.betweenClosed(center.offset(-1, -1, -1), center.offset(1, 1, 1))) {
            if (level.getFluidState(check).is(FluidTags.WATER) || level.getBlockState(check).is(Blocks.WATER)) {
                return check.immutable();
            }
            BlockState state = level.getBlockState(check);
            if (state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED)) {
                return check.immutable();
            }
            if (state.is(Blocks.WATER_CAULDRON) && state.getValue(net.minecraft.world.level.block.LayeredCauldronBlock.LEVEL) > 0) {
                return check.immutable();
            }
        }
        return null;
    }

    private static void completeSip(ServerPlayer sp, ServerLevel level, BlockPos pos, boolean still) {
        HearthwindSurvivalConfig.BareHand cfg = HearthwindSurvivalConfig.get().bareHand;
        HearthwindSurvivalThirst.addHydration(sp, cfg.sipQuench > 0 ? cfg.sipQuench : 1.0);
        // Drink hook parity: the hand is empty by rule, so this is a no-op
        // for unlisted stacks but keeps the same code path as flask drinks.
        HearthwindSurvivalDiet.onDrink(sp, sp.getMainHandItem());

        float chance = (float) cfg.sipThirstChance;
        if (level.getFluidState(pos).is(PurifiedWater.PURIFIED_TAG)) {
            chance = 0f;
        } else if (level.getBiome(pos).is(net.minecraft.tags.BiomeTags.IS_RIVER)) {
            chance = chance / 2f;
        }
        if (chance > 0f && sp.getRandom().nextFloat() <= chance) {
            sp.addEffect(new MobEffectInstance(
                    ThirstMobEffect.HOLDER, cfg.sipThirstDuration, 1));
        }

        if (cfg.consumeStillSource && still) {
            BlockState state = level.getBlockState(pos);
            if (state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED)) {
                level.setBlock(pos, state.setValue(BlockStateProperties.WATERLOGGED, false), 3);
            } else if (level.getBlockState(pos).is(Blocks.WATER)) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
        }

        level.playSound(null, sp.getX(), sp.getY(), sp.getZ(),
                SoundEvents.GENERIC_DRINK.value(), SoundSource.PLAYERS, 0.9f,
                0.9f + sp.getRandom().nextFloat() * 0.2f);
    }

    /** Kept for cauldron boil checks shared with flask filling. */
    public static boolean isHeatedCauldron(Level lvl, BlockPos cauldronPos) {
        BlockPos below = cauldronPos.below();
        BlockState state = lvl.getBlockState(below);
        if (state.is(Blocks.CAMPFIRE) && state.getValue(net.minecraft.world.level.block.CampfireBlock.LIT)) {
            return true;
        }
        if (state.is(Blocks.SOUL_CAMPFIRE) && state.getValue(net.minecraft.world.level.block.CampfireBlock.LIT)) {
            return true;
        }
        return state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE) || state.is(Blocks.LAVA);
    }
}
