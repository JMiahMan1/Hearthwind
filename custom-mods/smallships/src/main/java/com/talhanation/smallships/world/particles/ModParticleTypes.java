package com.talhanation.smallships.world.particles;

import com.talhanation.smallships.world.particles.cannon.DyedCannonShootOptions;
import com.talhanation.smallships.world.particles.custom.CustomPoofParticleOptions;
import com.talhanation.smallships.world.particles.fabric.ModParticleTypesImpl;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;

import java.util.function.Supplier;

public class ModParticleTypes {
    public static final Supplier<SimpleParticleType> CANNON_SHOOT = ModParticleTypesImpl.registerSimple("cannon_shoot");
    public static final Supplier<ParticleType<DyedCannonShootOptions>> DYED_CANNON_SHOOT = ModParticleTypesImpl.register("dyed_cannon_shoot", new DyedCannonShootOptions.Type());
    public static final Supplier<ParticleType<CustomPoofParticleOptions>> COLORED_POOF = ModParticleTypesImpl.register("colored_poof", new CustomPoofParticleOptions.Type());
    public static final Supplier<SimpleParticleType> CANNON_BALL_SHOOT = ModParticleTypesImpl.registerSimple("cannon_ball_shoot");
}
