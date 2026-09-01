package com.talhanation.smallships.world.particles.fabric;

import com.talhanation.smallships.SmallShipsMod;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

@SuppressWarnings("unused")
public class ModParticleTypesImpl {
    public static @NotNull <T extends ParticleOptions> Supplier<ParticleType<T>> register(String string, ParticleType<T> particleType) {
        ParticleType<T> type = Registry.register(BuiltInRegistries.PARTICLE_TYPE, Identifier.fromNamespaceAndPath(SmallShipsMod.MOD_ID, string), particleType);
        return () -> type;
    }

    public static @NotNull Supplier<SimpleParticleType> registerSimple(String string) {
        SimpleParticleType type = new SimpleParticleTypeImpl();
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, Identifier.fromNamespaceAndPath(SmallShipsMod.MOD_ID, string), type);
        return () -> type;
    }

    public static final class SimpleParticleTypeImpl extends SimpleParticleType {
        public SimpleParticleTypeImpl() {
            super(false);
        }
    }
}
