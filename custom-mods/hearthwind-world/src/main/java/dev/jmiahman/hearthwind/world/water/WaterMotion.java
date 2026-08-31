package dev.jmiahman.hearthwind.world.water;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

/**
 * Water Dynamics & River Currents (Aged parity, ideas/rivers-and-waves.md):
 * 1. Swimming against current: Realistic water resistance reduces swim speed by ~25% without stopping the player.
 * 2. Swimming with current: Gentle ~15% speed boost.
 * 3. Idling / Drifting: Passive drift along the current vector.
 * 4. High-Visibility Current Particles: Directional splash and bubble trails indicating flow vector.
 */
public final class WaterMotion {

    private WaterMotion() {}

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            long time = server.overworld().getGameTime();

            for (ServerLevel level : server.getAllLevels()) {
                if (level.isClientSide()) continue;

                for (ServerPlayer player : level.players()) {
                    if (player.isInWater() || isSubmerged(player, level)) {
                        applyWaterMotion(player, level, time);
                    }
                }
            }
        });
    }

    public static boolean isSubmerged(Entity entity, ServerLevel level) {
        BlockPos pos = entity.blockPosition();
        return level.getFluidState(pos).is(FluidTags.WATER)
                || level.getFluidState(pos.above()).is(FluidTags.WATER)
                || level.getFluidState(pos.below()).is(FluidTags.WATER);
    }

    public static void applyWaterMotion(Entity entity, ServerLevel level, long gameTime) {
        BlockPos pos = entity.blockPosition();
        FluidState fluid = level.getFluidState(pos);
        if (!fluid.is(FluidTags.WATER)) {
            fluid = level.getFluidState(pos.below());
            if (!fluid.is(FluidTags.WATER)) {
                fluid = level.getFluidState(pos.above());
            }
        }

        boolean inRiver = level.getBiome(pos).is(BiomeTags.IS_RIVER);
        boolean inOcean = level.getBiome(pos).is(BiomeTags.IS_OCEAN);

        // 1. Flow vector from fluid dynamics
        Vec3 fluidFlow = fluid.getFlow(level, pos);
        double flowX = fluidFlow.x * 0.006;
        double flowZ = fluidFlow.z * 0.006;

        // 2. River Current: Downstream gradient in river biomes
        if (inRiver || (Math.abs(flowX) < 0.0001 && Math.abs(flowZ) < 0.0001 && !inOcean)) {
            int northY = level.getHeight(Heightmap.Types.OCEAN_FLOOR, pos.getX(), pos.getZ() - 8);
            int southY = level.getHeight(Heightmap.Types.OCEAN_FLOOR, pos.getX(), pos.getZ() + 8);
            int westY = level.getHeight(Heightmap.Types.OCEAN_FLOOR, pos.getX() - 8, pos.getZ());
            int eastY = level.getHeight(Heightmap.Types.OCEAN_FLOOR, pos.getX() + 8, pos.getZ());

            double gradX = Math.max(-0.003, Math.min(0.003, (westY - eastY) * 0.0004));
            double gradZ = Math.max(-0.003, Math.min(0.003, (northY - southY) * 0.0004));

            if (inRiver && Math.abs(gradX) < 0.0004 && Math.abs(gradZ) < 0.0004) {
                gradX = 0.0025;
                gradZ = 0.0015;
            }
            flowX += gradX;
            flowZ += gradZ;
        }

        // 3. Ocean Swell: Rhythmic sinusoidal pulse
        if (inOcean) {
            double swellPhase = (gameTime % 400) / 400.0 * 2.0 * Math.PI;
            double swell = Math.sin(swellPhase) * 0.0015;
            flowX += swell * 0.5;
            flowZ += swell * 0.5;
        }

        double currentMagSq = flowX * flowX + flowZ * flowZ;
        if (currentMagSq > 0.0000005) {
            Vec3 motion = entity.getDeltaMovement();
            double speedSq = motion.x * motion.x + motion.z * motion.z;

            if (speedSq > 0.0004) {
                // Entity is actively moving / swimming
                double dot = motion.x * flowX + motion.z * flowZ;

                if (dot < 0) {
                    // Moving AGAINST current: apply drag (~25% speed penalty) without overpowering or halting
                    double drag = 0.78;
                    entity.setDeltaMovement(motion.x * drag, motion.y, motion.z * drag);
                } else {
                    // Moving WITH current: apply gentle boost (~12% bonus speed)
                    double boost = 1.12;
                    entity.setDeltaMovement(motion.x * boost, motion.y, motion.z * boost);
                }
            } else {
                // Entity is idling / floating: apply gentle passive drift
                double boatMult = entity instanceof AbstractBoat ? 1.4 : 0.5;
                entity.setDeltaMovement(motion.x + flowX * boatMult, motion.y, motion.z + flowZ * boatMult);
            }

            // 4. Directional Particle Trails
            double px = entity.getX() + (level.getRandom().nextDouble() - 0.5) * 1.5;
            double py = entity.getY() + 0.1 + (level.getRandom().nextDouble() * 0.4);
            double pz = entity.getZ() + (level.getRandom().nextDouble() - 0.5) * 1.5;

            double dirX = flowX * 15.0;
            double dirZ = flowZ * 15.0;

            level.sendParticles(ParticleTypes.SPLASH, px, py, pz, 0, dirX, 0.03, dirZ, 0.25);
            level.sendParticles(ParticleTypes.BUBBLE_POP, px, py, pz, 0, dirX * 0.7, 0.02, dirZ * 0.7, 0.2);
            if (level.getRandom().nextInt(3) == 0) {
                level.sendParticles(ParticleTypes.BUBBLE, px, py - 0.2, pz, 2, 0.2, 0.1, 0.2, 0.02);
            }
        }
    }
}
