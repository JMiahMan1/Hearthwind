package io.github.phantomloader.library.events;

public interface RegisterParticlesEvent {
   <T extends net.minecraft.core.particles.ParticleOptions> void register(
      net.minecraft.core.particles.ParticleType<T> var1, net.minecraft.client.particle.ParticleProvider<T> var2
   );
}
