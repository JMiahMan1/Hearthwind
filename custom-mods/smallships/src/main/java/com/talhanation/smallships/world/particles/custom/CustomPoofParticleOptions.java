package com.talhanation.smallships.world.particles.custom;

import com.mojang.serialization.MapCodec;
import com.talhanation.smallships.world.particles.ModParticleTypes;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import org.joml.Vector3f;

import io.netty.buffer.ByteBuf;

public record CustomPoofParticleOptions(Vector3f color) implements ParticleOptions {
    public static final StreamCodec<ByteBuf, CustomPoofParticleOptions> STREAM_CODEC;
    public static final MapCodec<CustomPoofParticleOptions> MAP_CODEC;

    static {
        STREAM_CODEC = StreamCodec.of(
                (buf, options) -> ByteBufCodecs.VECTOR3F.encode(buf, options.color),
                buf -> new CustomPoofParticleOptions(new Vector3f(ByteBufCodecs.VECTOR3F.decode(buf))));
        MAP_CODEC = ExtraCodecs.VECTOR3F
                .xmap(vector3fc -> new CustomPoofParticleOptions(new Vector3f(vector3fc)), CustomPoofParticleOptions::color)
                .fieldOf("color");
    }

    @Override
    public ParticleType<?> getType() {
        return ModParticleTypes.COLORED_POOF.get();
    }

    public static final class Type extends ParticleType<CustomPoofParticleOptions> {
        public Type() {
            super(false);
        }

        @Override
        public MapCodec<CustomPoofParticleOptions> codec() {
            return MAP_CODEC;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, CustomPoofParticleOptions> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
