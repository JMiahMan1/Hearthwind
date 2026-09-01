package com.talhanation.smallships.world.particles.fabric;

import com.talhanation.smallships.world.particles.ModParticleProviders;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;

import java.util.function.Function;

@SuppressWarnings("unused")
public class ModParticleProvidersImpl extends ModParticleProviders {
    public static <T extends ParticleOptions> void register(ParticleType<T> type, ParticleProvider<T> provider) {
        ParticleProviderRegistry.getInstance().register(type, provider);
    }

    public static <T extends ParticleOptions> void register(ParticleType<T> type, Function<SpriteSet, ParticleProvider<T>> provider) {
        ParticleProviderRegistry.getInstance().register(type, provider::apply);
    }
}
