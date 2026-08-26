package dev.jmiahman.hearthwind.survival;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;

/**
 * Early-game water: right-click a water source with an empty {@code minecraft:bowl}
 * to obtain {@code dehydration:water_bowl}. This mirrors the original
 * dehydration mod's bowl-filling and makes thirst solvable in the first minutes
 * without a bucket (which is iron-gated). Also allows bare-hand drinking
 * (small sip, dirty) for the truly stranded - deliberately tedious to push
 * players toward bowls/campfires.
 *
 * Server-authoritative, runs on both logical sides but only mutates on server.
 */
public final class BowlWaterFillHandler {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger("hearthwind_survival/bowl");
    private static final double BARE_HAND_HYDRATION = 1.0; // tiny, vs 6 per bowl
    private static final int BARE_HAND_THIRST_DURATION = 400; // 20s, vs 15s for bowl
    private static final float BARE_HAND_THIRST_CHANCE = 0.90f;
    private static final long BARE_HAND_COOLDOWN_TICKS = 60; // 3s - spam is slow
    private static final java.util.Map<java.util.UUID, Long> bareHandCooldowns =
            new java.util.concurrent.ConcurrentHashMap<>();

    private BowlWaterFillHandler() {}

    private static boolean isHeatedCauldron(Level lvl, BlockPos cauldronPos) {
        BlockPos below = cauldronPos.below();
        var state = lvl.getBlockState(below);
        // lit campfire directly below is the classic 'boiling' heater
        if (state.is(Blocks.CAMPFIRE) && state.getValue(net.minecraft.world.level.block.CampfireBlock.LIT)) {
            return true;
        }
        if (state.is(Blocks.SOUL_CAMPFIRE) && state.getValue(net.minecraft.world.level.block.CampfireBlock.LIT)) {
            return true;
        }
        // also consider fire/lava directly below as heated
        return state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE) || state.is(Blocks.LAVA);
    }

    private static boolean isHotWaterSource(ServerPlayer sp) {
        // Hot biome or extreme heat makes standing water warm
        double target = HearthwindSurvivalTemperature.targetFor(sp);
        if (target >= 7.0) return true;
        // also check current temperature already hot
        if (HearthwindSurvivalTemperature.get(sp) >= 7.0) return true;
        // or if it's noon in a hot biome (simple: desert/badlands at 6000t)
        var biome = sp.level().getBiome(sp.blockPosition()).value();
        float base = biome.getBaseTemperature();
        long time;
        try {
            time = (Long) sp.level().getClass().getMethod("getDayTime").invoke(sp.level()) % 24000;
        } catch (Exception e) {
            time = sp.level().getGameTime() % 24000;
        }
        boolean isNoon = time > 5000 && time < 8000;
        return base >= 1.5f && isNoon;
    }

    private static boolean isColdWaterSource(ServerPlayer sp) {
        double target = HearthwindSurvivalTemperature.targetFor(sp);
        if (target <= -4.0) return true;
        if (HearthwindSurvivalTemperature.get(sp) <= -4.0) return true;
        var biome = sp.level().getBiome(sp.blockPosition()).value();
        float base = biome.getBaseTemperature();
        // snowy / frozen biomes are naturally cold
        if (base <= 0.15f) return true;
        // carrying ice also chills the source
        return sp.getInventory().hasAnyMatching(s -> !s.isEmpty() && s.is(EnvironmentzItems.ICE_ITEMS));
    }

    private static boolean isNearWater(Level lvl, BlockPos center, int radius) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos p = center.offset(dx, dy, dz);
                    if (lvl.getFluidState(p).is(Fluids.WATER) || lvl.getBlockState(p).is(Blocks.WATER) || lvl.getBlockState(p).is(Blocks.WATER_CAULDRON)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static void register() {
        // Right-click water block / water cauldron with bowl/empty hand
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            ItemStack held = player.getItemInHand(hand);
            BlockPos pos = hitResult.getBlockPos();
            boolean isWater = world.getFluidState(pos).is(Fluids.WATER)
                    || world.getBlockState(pos).is(Blocks.WATER);
            boolean isCauldron = world.getBlockState(pos).is(Blocks.WATER_CAULDRON);
            // Debug log every interaction near water
            if (player instanceof ServerPlayer sp) {
                LOGGER.info("UseBlock hit {} isWater={} isCauldron={} held={} player={} pos={}", pos, isWater, isCauldron, held, sp.getName().getString(), sp.blockPosition());
            }

            // Bare-hand sip should be very forgiving: if empty hand and near water (2 blocks) or in water, allow it even if not directly hitting water
            if (held.isEmpty() && world instanceof Level lvl) {
                boolean nearWater = isWater || isCauldron || isNearWater(lvl, pos, 2) || isNearWater(lvl, player.blockPosition(), 2) || player.isInWater();
                LOGGER.info("Bare-hand check nearWater={} isWater={} pos={} playerPos={} heldEmpty={}", nearWater, isWater, pos, player.blockPosition(), held.isEmpty());
                if (nearWater) {
                    if (!lvl.isClientSide() && player instanceof ServerPlayer sp) {
                        long now = lvl.getGameTime();
                        long last = bareHandCooldowns.getOrDefault(sp.getUUID(), 0L);
                        if (now - last < BARE_HAND_COOLDOWN_TICKS) {
                            long left = (BARE_HAND_COOLDOWN_TICKS - (now - last)) / 20 + 1;
                            sp.sendOverlayMessage(net.minecraft.network.chat.Component.literal(
                                    "Cupping water is slow... craft a bowl (3 planks) for a proper drink. (" + left + "s)")
                                    .withStyle(net.minecraft.ChatFormatting.GRAY));
                            return InteractionResult.SUCCESS_SERVER;
                        }
                        bareHandCooldowns.put(sp.getUUID(), now);
                        HearthwindSurvivalThirst.addHydration(sp, BARE_HAND_HYDRATION);
                        if (sp.getRandom().nextFloat() < BARE_HAND_THIRST_CHANCE) {
                            sp.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                    ThirstMobEffect.HOLDER, BARE_HAND_THIRST_DURATION, 0));
                        }
                        sp.getFoodData().addExhaustion(0.6f);
                        sp.sendOverlayMessage(net.minecraft.network.chat.Component.literal(
                                "You cup water in your hands and sip - barely helps.").withStyle(net.minecraft.ChatFormatting.BLUE));
                        lvl.playSound(null, pos, SoundEvents.GENERIC_DRINK.value(), SoundSource.PLAYERS, 0.5f, 0.9f);
                    }
                    return InteractionResult.SUCCESS_SERVER;
                }
            }

            if (!isWater && !isCauldron) {
                return InteractionResult.PASS;
            }

            // Cauldron handling - bowl on cauldron
            if (isCauldron && held.is(Items.BOWL)) {
                if (world instanceof Level lvl && !lvl.isClientSide() && player instanceof ServerPlayer sp) {
                    var state = lvl.getBlockState(pos);
                    int level = state.getValue(net.minecraft.world.level.block.LayeredCauldronBlock.LEVEL);
                    if (level <= 0) return InteractionResult.PASS;
                    boolean heated = isHeatedCauldron(lvl, pos);
                    boolean coldSource = !heated && isColdWaterSource(sp);
                    ItemStack filled;
                    if (heated) {
                        // boiling cauldron gives hot purified (even though source was dirty, heat purifies but scalds)
                        filled = HotWaterBowlItem.createHotStack(DehydrationItems.HOT_PURIFIED_WATER_BOWL, lvl);
                        sp.sendOverlayMessage(net.minecraft.network.chat.Component.literal(
                                "You scoop steaming hot purified water - let it cool!").withStyle(net.minecraft.ChatFormatting.RED));
                        lvl.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 0.7f, 1.3f);
                    } else if (coldSource) {
                        filled = new ItemStack(DehydrationItems.COLD_PURIFIED_WATER_BOWL);
                        sp.sendOverlayMessage(net.minecraft.network.chat.Component.literal(
                                "You scoop icy cold purified water!").withStyle(net.minecraft.ChatFormatting.AQUA));
                        lvl.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 0.9f, 1.3f);
                    } else {
                        filled = new ItemStack(DehydrationItems.PURIFIED_WATER_BOWL);
                        lvl.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 0.9f, 1.0f);
                    }
                    if (!sp.getAbilities().instabuild) held.shrink(1);
                    if (!sp.getInventory().add(filled)) sp.drop(filled, false);
                    // decrement cauldron
                    int newLevel = level - 1;
                    if (newLevel <= 0) {
                        lvl.setBlock(pos, Blocks.CAULDRON.defaultBlockState(), 3);
                    } else {
                        lvl.setBlock(pos, state.setValue(net.minecraft.world.level.block.LayeredCauldronBlock.LEVEL, newLevel), 3);
                    }
                }
                return InteractionResult.SUCCESS_SERVER;
            }

            // 1) Bowl -> water_bowl (or hot/cold variant). Check heated cauldron first,
            // then hot/cold-biome source. Hot scalds, cold cools overheating.
            if (held.is(Items.BOWL)) {
                if (world instanceof Level lvl && !lvl.isClientSide() && player instanceof ServerPlayer sp) {
                    // cauldron case is handled in the isWater==false branch below
                    // (water_cauldron is not WATER fluid), so this is source water
                    boolean isHotSource = isHotWaterSource(sp);
                    boolean isColdSource = !isHotSource && isColdWaterSource(sp);
                    ItemStack filled;
                    if (isHotSource) {
                        filled = HotWaterBowlItem.createHotStack(DehydrationItems.HOT_WATER_BOWL, lvl);
                        sp.sendOverlayMessage(net.minecraft.network.chat.Component.literal(
                                "The water is warm/hot from the heat - it will need to cool!").withStyle(net.minecraft.ChatFormatting.GOLD));
                    } else if (isColdSource) {
                        filled = new ItemStack(DehydrationItems.COLD_WATER_BOWL);
                        sp.sendOverlayMessage(net.minecraft.network.chat.Component.literal(
                                "The water is icy cold - refreshing!").withStyle(net.minecraft.ChatFormatting.AQUA));
                    } else {
                        filled = new ItemStack(DehydrationItems.WATER_BOWL);
                    }
                    if (!sp.getAbilities().instabuild) {
                        held.shrink(1);
                    }
                    if (!sp.getInventory().add(filled)) {
                        sp.drop(filled, false);
                    }
                    lvl.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 0.9f, 1.0f);
                }
                return InteractionResult.SUCCESS_SERVER;
            }

            return InteractionResult.PASS;
        });

        // Right-click air with empty hand - also allow sip if near water or in water (very forgiving)
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack held = player.getItemInHand(hand);
            if (!held.isEmpty()) {
                return InteractionResult.PASS;
            }
            boolean nearWater = false;
            if (world instanceof Level lvl) {
                nearWater = player.isInWater() || isNearWater(lvl, player.blockPosition(), 3);
                LOGGER.info("UseItem bare-hand nearWater={} pos={} heldEmpty={}", nearWater, player.blockPosition(), held.isEmpty());
            }
            if (!nearWater) {
                return InteractionResult.PASS;
            }
            if (world instanceof Level lvl && !lvl.isClientSide() && player instanceof ServerPlayer sp) {
                long now = lvl.getGameTime();
                long last = bareHandCooldowns.getOrDefault(sp.getUUID(), 0L);
                if (now - last < BARE_HAND_COOLDOWN_TICKS) {
                    long left = (BARE_HAND_COOLDOWN_TICKS - (now - last)) / 20 + 1;
                    sp.sendOverlayMessage(net.minecraft.network.chat.Component.literal(
                            "Cupping water is slow... craft a bowl (3 planks) for a proper drink. (" + left + "s)")
                            .withStyle(net.minecraft.ChatFormatting.GRAY));
                    return InteractionResult.SUCCESS_SERVER;
                }
                bareHandCooldowns.put(sp.getUUID(), now);
                HearthwindSurvivalThirst.addHydration(sp, BARE_HAND_HYDRATION);
                if (sp.getRandom().nextFloat() < BARE_HAND_THIRST_CHANCE) {
                    sp.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                            ThirstMobEffect.HOLDER, BARE_HAND_THIRST_DURATION, 0));
                }
                sp.getFoodData().addExhaustion(0.6f);
                sp.sendOverlayMessage(net.minecraft.network.chat.Component.literal(
                        "You cup water in your hands and sip - barely helps.").withStyle(net.minecraft.ChatFormatting.BLUE));
                lvl.playSound(null, sp.blockPosition(), SoundEvents.GENERIC_DRINK.value(), SoundSource.PLAYERS, 0.5f, 0.9f);
            }
            return InteractionResult.SUCCESS_SERVER;
        });
    }
}
