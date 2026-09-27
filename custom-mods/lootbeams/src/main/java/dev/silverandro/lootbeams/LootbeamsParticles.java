package dev.silverandro.lootbeams;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;

/**
 * Upstream spawns one coloured dust particle per configured count along a
 * short vertical beam (offset + random * height), plus enchant particles
 * for foiled stacks with a small downward drift.
 */
public final class LootbeamsParticles {
    private LootbeamsParticles() {
    }

    public static void generateParticles(ItemEntity item, int rgb, LootbeamsConfig config) {
        Level level = item.level();
        DustParticleOptions dust = new DustParticleOptions(0xFF000000 | (rgb & 0xFFFFFF), 1.0F);
        for (int i = 0; i < config.particleCount; i++) {
            level.addAlwaysVisibleParticle(dust, item.getX(), beamY(item, config), item.getZ(),
                    0.0D, 0.0D, 0.0D);
        }
        if (config.enchantedParticles && item.getItem().hasFoil()) {
            int count = Math.max(1, config.particleCount / 2);
            for (int i = 0; i < count; i++) {
                level.addAlwaysVisibleParticle(ParticleTypes.ENCHANT, item.getX(), beamY(item, config),
                        item.getZ(), 0.0D, -0.3D, 0.0D);
            }
        }
    }

    private static double beamY(ItemEntity item, LootbeamsConfig config) {
        return item.getY() + config.beamOffset + item.getRandom().nextDouble() * config.beamHeight;
    }
}
