package dev.jmiahman.hearthwind.world.water;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

/**
 * Enhanced Water Dynamics, River Currents, Waterfall Mist & Ocean Swells (Minecraft 26.2).
 *
 * 1. Directional River Currents: Pushes players, swimmers, boats, and floating items downstream.
 * 2. Swimming Physics: Swimming against current gives realistic drag (~25%); swimming with current gives a gentle boost (~15%).
 * 3. Waterfall Mist & Spray: Falling water cascades emit dense mist, splash clouds, and bubbles when hitting lower pools.
 * 4. Ocean Swell & Wave Foam: Gentle rhythmic pulse near shores with whitecaps and splash particles.
 */
public final class WaterMotion {

    private WaterMotion() {}

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            long time = server.overworld().getGameTime();

            for (ServerLevel level : server.getAllLevels()) {
                if (level.isClientSide()) continue;

                // 1. Process players
                for (ServerPlayer player : level.players()) {
                    if (player.isInWater() || isSubmerged(player, level)) {
                        applyWaterMotion(player, level, time);
                    }
                }

                // 2. Process boats & floating entities in loaded chunks every 2 ticks
                if (time % 2 == 0) {
                    java.util.Set<Entity> processed = new java.util.HashSet<>();
                    for (ServerPlayer player : level.players()) {
                        BlockPos pPos = player.blockPosition();
                        net.minecraft.world.phys.AABB box = new net.minecraft.world.phys.AABB(pPos).inflate(32.0);
                        for (Entity entity : level.getEntities((Entity) null, box, e -> (e instanceof AbstractBoat || e instanceof ItemEntity || (e instanceof LivingEntity && !(e instanceof ServerPlayer))))) {
                            if (processed.add(entity) && (entity.isInWater() || isSubmerged(entity, level))) {
                                applyWaterMotion(entity, level, time);
                            }
                        }
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
        double flowX = fluidFlow.x * 0.008;
        double flowZ = fluidFlow.z * 0.008;

        // 2. Waterfall detection (falling water column)
        boolean isFallingWater = !fluid.isSource() && fluid.getAmount() >= 8;
        if (isFallingWater) {
            // Waterfall spray & mist particles
            double px = entity.getX() + (level.getRandom().nextDouble() - 0.5) * 1.2;
            double py = entity.getY() + level.getRandom().nextDouble() * 0.5;
            double pz = entity.getZ() + (level.getRandom().nextDouble() - 0.5) * 1.2;
            level.sendParticles(ParticleTypes.SPLASH, px, py, pz, 4, 0.2, 0.2, 0.2, 0.15);
            level.sendParticles(ParticleTypes.CLOUD, px, py + 0.1, pz, 1, 0.1, 0.05, 0.1, 0.02);
            level.sendParticles(ParticleTypes.BUBBLE_POP, px, py, pz, 2, 0.1, 0.1, 0.1, 0.05);
        }

        // 3. River Current: Downstream gradient in river biomes
        if (inRiver || (Math.abs(flowX) < 0.0001 && Math.abs(flowZ) < 0.0001 && !inOcean)) {
            int northY = level.getHeight(Heightmap.Types.OCEAN_FLOOR, pos.getX(), pos.getZ() - 8);
            int southY = level.getHeight(Heightmap.Types.OCEAN_FLOOR, pos.getX(), pos.getZ() + 8);
            int westY = level.getHeight(Heightmap.Types.OCEAN_FLOOR, pos.getX() - 8, pos.getZ());
            int eastY = level.getHeight(Heightmap.Types.OCEAN_FLOOR, pos.getX() + 8, pos.getZ());

            double gradX = Math.max(-0.004, Math.min(0.004, (westY - eastY) * 0.0005));
            double gradZ = Math.max(-0.004, Math.min(0.004, (northY - southY) * 0.0005));

            if (inRiver && Math.abs(gradX) < 0.0004 && Math.abs(gradZ) < 0.0004) {
                gradX = 0.003;
                gradZ = 0.002;
            }
            flowX += gradX;
            flowZ += gradZ;
        }

        // 4. Ocean Swell: Rhythmic sinusoidal pulse
        if (inOcean) {
            double swellPhase = (gameTime % 300) / 300.0 * 2.0 * Math.PI;
            double swell = Math.sin(swellPhase) * 0.0025;
            flowX += swell * 0.6;
            flowZ += swell * 0.6;
        }

        double currentMagSq = flowX * flowX + flowZ * flowZ;
        if (currentMagSq > 0.0000005) {
            Vec3 motion = entity.getDeltaMovement();
            double speedSq = motion.x * motion.x + motion.z * motion.z;

            if (entity instanceof AbstractBoat) {
                // Boats drift with realistic river momentum, capped to avoid velocity runaway
                double newBx = motion.x + flowX * 1.2;
                double newBz = motion.z + flowZ * 1.2;
                double maxBoatSpeed = 0.5;
                double bSpeed = Math.sqrt(newBx * newBx + newBz * newBz);
                if (bSpeed > maxBoatSpeed) {
                    double scale = maxBoatSpeed / bSpeed;
                    newBx *= scale;
                    newBz *= scale;
                }
                entity.setDeltaMovement(newBx, motion.y, newBz);
            } else if (entity instanceof ItemEntity) {
                // Items float downstream
                entity.setDeltaMovement(motion.x * 0.9 + flowX * 1.5, motion.y, motion.z * 0.9 + flowZ * 1.5);
            } else if (speedSq > 0.0004) {
                // Entity is actively swimming
                double dot = motion.x * flowX + motion.z * flowZ;

                if (dot < 0) {
                    // Moving AGAINST current: apply drag (~15% speed penalty)
                    double drag = 0.85;
                    entity.setDeltaMovement(motion.x * drag, motion.y, motion.z * drag);
                } else {
                    // Moving WITH current: apply gentle additive flow push (capped)
                    double newMx = motion.x + flowX * 0.5;
                    double newMz = motion.z + flowZ * 0.5;
                    double maxSpeed = 0.35;
                    double currentSpeed = Math.sqrt(newMx * newMx + newMz * newMz);
                    if (currentSpeed > maxSpeed) {
                        double scale = maxSpeed / currentSpeed;
                        newMx *= scale;
                        newMz *= scale;
                    }
                    entity.setDeltaMovement(newMx, motion.y, newMz);
                }
            } else {
                // Entity is idling / floating: apply gentle passive drift
                entity.setDeltaMovement(motion.x + flowX * 0.8, motion.y, motion.z + flowZ * 0.8);
            }

            // 5. Directional Particle Trails & Surface Wake
            if (level.getRandom().nextInt(2) == 0) {
                double px = entity.getX() + (level.getRandom().nextDouble() - 0.5) * 1.2;
                double py = entity.getY() + 0.05 + (level.getRandom().nextDouble() * 0.3);
                double pz = entity.getZ() + (level.getRandom().nextDouble() - 0.5) * 1.2;

                double dirX = flowX * 18.0;
                double dirZ = flowZ * 18.0;

                level.sendParticles(ParticleTypes.SPLASH, px, py, pz, 1, dirX, 0.04, dirZ, 0.25);
                level.sendParticles(ParticleTypes.BUBBLE_POP, px, py, pz, 1, dirX * 0.7, 0.02, dirZ * 0.7, 0.2);
                level.sendParticles(ParticleTypes.BUBBLE, px, py - 0.2, pz, 2, 0.2, 0.1, 0.2, 0.02);
            }
        }
    }
}
