package dev.jmiahman.hearthwind.jobs;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Job XP hooks: block breaks and kills award job XP if the broken/killed id
 * matches the player's current job level track. Reuses the same event buses
 * as SkillEvents but checks JobDefs instead of skill config.
 */
public final class JobEvents {
    private JobEvents() {}

    public static void register() {
        PlayerBlockBreakEvents.AFTER.register(JobEvents::onBlockBroken);
        ServerLivingEntityEvents.AFTER_DEATH.register(JobEvents::onDeath);
    }

    private static void onBlockBroken(Level world, net.minecraft.world.entity.player.Player player, BlockPos pos,
            BlockState state, BlockEntity blockEntity) {
        if (!(player instanceof ServerPlayer sp) || sp.getAbilities().instabuild || state.isAir()) return;
        String id = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
        JobState.awardIfMatch(sp, id);
        sendJobSync(sp);
    }

    private static void onDeath(LivingEntity entity, DamageSource source) {
        if (!(source.getEntity() instanceof ServerPlayer sp)) return;
        String id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
        JobState.awardIfMatch(sp, id);
        sendJobSync(sp);
    }

    private static void sendJobSync(ServerPlayer sp) {
        dev.jmiahman.hearthwind.survival.JobSyncPayload payload =
                new dev.jmiahman.hearthwind.survival.JobSyncPayload(
                        JobState.jobId(sp), JobState.level(sp), JobState.xp(sp),
                        HearthwindJobsConfig.get().pointsPerLevel);
        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(
                sp, payload);
    }
}
