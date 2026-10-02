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
        if (!player.getMainHandItem().isEmpty() || player.isSpectator() || player.isCreative()
                || !player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        BlockPos water = findWater(player, level);
        if (water == null) {
            return InteractionResult.PASS;
        }
        return trySipAt(player, level, water);
    }

    /**
     * The sip itself, with the source position already resolved.
     *
     * <p>{@link #findWater} is the only part of this that needs the player to
     * be able to raycast, and a gametest mock player cannot: its eye stays at
     * the world origin and {@code pick} misses even with a water source
     * directly below it (measured - the diagnostic in
     * bareHandHoldCompletesSipAndConsumesSource prints eye=(0.0, 1.62, 0.0)
     * with the source at (0, 0, 0)). So the tests call this with a position
     * and the reach stays pinned by {@link #SIP_REACH} instead.
     */
    public static InteractionResult trySipAt(Player player, Level level, BlockPos water) {

        if (HearthwindSurvivalThirst.level(player) >= HearthwindSurvivalThirst.MAX_LEVEL) {
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

    /**
     * The reference's sip target: {@code player.raycast(1.5, 0.0, 1.0)} - a
     * fixed 1.5-block reach, no hits, fluids included - and then ONE test: the
     * hit block's fluid must be in {@code FluidTags.WATER}.
     *
     * <p>We added four fallbacks (water above the hit, waterlogged blocks, a
     * full water cauldron, and the player's own block so a submerged player can
     * sip) to make the sip reachable in more situations. None of them exist in
     * Dehydration 1.3.6 and each one lets you drink where the reference cannot
     * (0.1.49).
     */
    public static BlockPos findWater(Player player, Level level) {
        // withFluids = FALSE, deliberately. The reference raycasts
        // (1.5, 0.0, 1.0), but 26.2 moved fluid handling: LiquidBlock is no
        // longer a LiquidBlockContainer, so Entity.pick's withLiquids branch
        // does not report a water block at all - measured, the eye directly
        // above a source returns a miss. A water source is still a full block
        // for Level.clip, so a block raycast finds it and the FluidTags.WATER
        // test below is the reference's own check.
        HitResult ray = player.pick(SIP_REACH, 0.0F, false);
        if (ray == null || ray.getType() != HitResult.Type.BLOCK) {
            return null;
        }
        BlockPos pos = ((BlockHitResult) ray).getBlockPos();
        return level.getFluidState(pos).is(FluidTags.WATER) ? pos : null;
    }

    /** The reference's fixed sip reach, in blocks (0.1.49). */
    public static final double SIP_REACH = 1.5;

    private static void completeSip(ServerPlayer sp, ServerLevel level, BlockPos pos, boolean still) {
        HearthwindSurvivalConfig.BareHand cfg = HearthwindSurvivalConfig.get().bareHand;
        HearthwindSurvivalThirst.addThirst(sp, Math.max(1, cfg.waterSourceQuench));
        // Drink hook parity: the hand is empty by rule, so this is a no-op
        // for unlisted stacks but keeps the same code path as flask drinks.
        HearthwindSurvivalDiet.onDrink(sp, sp.getMainHandItem());

        float chance = (float) cfg.waterSipThirstChance;
        if (level.getFluidState(pos).is(PurifiedWater.PURIFIED_TAG)) {
            chance = 0f;
        } else if (level.getBiome(pos).is(net.minecraft.tags.BiomeTags.IS_RIVER)) {
            chance = chance / 2f;
        }
        if (chance > 0f && sp.getRandom().nextFloat() <= chance) {
            sp.addEffect(new MobEffectInstance(
                    ThirstMobEffect.HOLDER, cfg.waterSipThirstDuration, 1,
                    false, false, true));
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
