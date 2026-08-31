package dev.jmiahman.hearthwind.survival.revive;

import java.util.Optional;
import java.util.UUID;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public final class ReviveManager {
    public static final int BLEEDOUT_TICKS = 1200; // 60 seconds
    public static final int REVIVE_REQUIRED_TICKS = 60; // 3 seconds
    public static final float REVIVE_HEALTH = 6.0f; // 3 hearts

    private ReviveManager() {}

    public static void register() {
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, damageAmount) -> {
            if (entity instanceof ServerPlayer player) {
                return onFatalDamage(player, damageSource);
            }
            return true;
        });

        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClientSide() && entity instanceof ServerPlayer target && player instanceof ServerPlayer reviver) {
                return onInteract(reviver, target, hand);
            }
            return InteractionResult.PASS;
        });
    }

    public static boolean onFatalDamage(ServerPlayer player, DamageSource damageSource) {
        if (player.getAbilities().invulnerable) {
            return true;
        }

        MinecraftServer server = player.level() != null ? player.level().getServer() : null;
        // If in singleplayer or no other players are online to revive, allow instant normal death
        if (server == null || server.getPlayerList().getPlayerCount() <= 1) {
            return true;
        }

        DownedState.Data current = DownedState.get(player);
        if (current.isDowned()) {
            // Already downed - second fatal blow confirms true death
            DownedState.clear(player);
            sync(player, false, 0, 0);
            return true;
        }

        // Intercept death and transition to Downed state
        DownedState.set(player, new DownedState.Data(true, BLEEDOUT_TICKS, 0, Optional.empty()));
        player.setHealth(2.0f);

        // Apply immobilization & visual cues
        player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, BLEEDOUT_TICKS, 4, false, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, BLEEDOUT_TICKS, 4, false, false, false));

        if (player.level() != null) {
            player.level().playSound(null, player.blockPosition(),
                    SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, 1.0f, 0.7f);
        }

        player.sendSystemMessage(Component.literal(
                "§c§lDOWNED! §7Bleeding out in 60s - An ally must crouch and use on you to revive!"));

        sync(player, true, BLEEDOUT_TICKS / 20, 0);
        return false; // Cancel death and enter downed state
    }

    public static InteractionResult onInteract(ServerPlayer reviver, ServerPlayer target, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (!DownedState.isDowned(target)) {
            return InteractionResult.PASS;
        }
        if (reviver.getUUID().equals(target.getUUID())) {
            return InteractionResult.PASS;
        }

        // Start reviving target
        DownedState.Data data = DownedState.get(target);
        DownedState.set(target, new DownedState.Data(true, data.bleedoutTicks(), 0, Optional.of(reviver.getUUID())));

        reviver.sendSystemMessage(Component.literal("§eChanneling revive on " + target.getName().getString() + "... Hold sneak!"));
        return InteractionResult.SUCCESS_SERVER;
    }

    public static void tickPlayer(ServerPlayer player) {
        DownedState.Data data = DownedState.get(player);
        if (!data.isDowned()) {
            return;
        }

        MinecraftServer server = player.level() != null ? player.level().getServer() : null;
        if (server != null && server.getPlayerList().getPlayerCount() <= 1) {
            // No other players online to help -> confirm death immediately
            DownedState.clear(player);
            sync(player, false, 0, 0);
            if (player.level() instanceof ServerLevel sl) {
                player.kill(sl);
            }
            return;
        }

        int remainingTicks = data.bleedoutTicks() - 1;
        if (remainingTicks <= 0) {
            // Bleedout expired -> kill player
            DownedState.clear(player);
            sync(player, false, 0, 0);
            if (player.level() instanceof ServerLevel sl) {
                player.kill(sl);
            }
            return;
        }

        int reviveTicks = data.reviveTicks();
        Optional<UUID> reviverId = data.reviverUuid();

        if (reviverId.isPresent()) {
            ServerPlayer reviver = server != null ? server.getPlayerList().getPlayer(reviverId.get()) : null;

            if (reviver != null && reviver.isCrouching() && reviver.distanceToSqr(player) <= 3.5 * 3.5) {
                reviveTicks++;
                int percent = (int) ((reviveTicks / (float) REVIVE_REQUIRED_TICKS) * 100);

                if (player.tickCount % 5 == 0) {
                    player.level().playSound(null, player.blockPosition(),
                            SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.4f, 1.0f + (percent / 100f));
                }

                if (reviveTicks >= REVIVE_REQUIRED_TICKS) {
                    // Revive Complete!
                    completeRevive(player, reviver);
                    return;
                }
            } else {
                // Reviver moved away or stopped sneaking
                reviveTicks = 0;
                reviverId = Optional.empty();
            }
        }

        DownedState.set(player, new DownedState.Data(true, remainingTicks, reviveTicks, reviverId));

        if (player.tickCount % 20 == 0) {
            int secondsLeft = remainingTicks / 20;
            int percent = (int) ((reviveTicks / (float) REVIVE_REQUIRED_TICKS) * 100);
            sync(player, true, secondsLeft, percent);
        }
    }

    public static void completeRevive(ServerPlayer downed, ServerPlayer reviver) {
        DownedState.clear(downed);
        downed.removeAllEffects();
        downed.setHealth(REVIVE_HEALTH);

        if (downed.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.HEART,
                    downed.getX(), downed.getY() + 1.0, downed.getZ(),
                    10, 0.5, 0.5, 0.5, 0.1);
            level.playSound(null, downed.blockPosition(),
                    SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8f, 1.2f);
        }

        downed.sendSystemMessage(Component.literal("§aYou were revived by §e" + reviver.getName().getString() + "§a!"));
        reviver.sendSystemMessage(Component.literal("§aSuccessfully revived §e" + downed.getName().getString() + "§a!"));

        sync(downed, false, 0, 0);
    }

    public static void sync(ServerPlayer player, boolean isDowned, int remainingSeconds, int progressPercent) {
        if (player != null && player.connection != null) {
            try {
                if (ServerPlayNetworking.canSend(player, DownedSyncPayload.TYPE)) {
                    ServerPlayNetworking.send(player, new DownedSyncPayload(isDowned, remainingSeconds, progressPercent));
                }
            } catch (Exception ignored) {}
        }
    }
}
