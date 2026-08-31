package dev.jmiahman.hearthwind.survival;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Water carried by a leather flask. Quality levels mirror the original
 * dehydration mod semantics: 0 = purified (boiled), 1 = impure (natural
 * still water), 2 = dirty (river or unheated cauldron).
 */
public record FlaskData(int fillLevel, int qualityLevel) {
    public static final int PURIFIED = 0;
    public static final int IMPURIFIED = 1;
    public static final int DIRTY = 2;

    public static final Codec<FlaskData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("fill").forGetter(FlaskData::fillLevel),
            Codec.INT.fieldOf("quality").forGetter(FlaskData::qualityLevel)).apply(instance, FlaskData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FlaskData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, FlaskData::fillLevel,
            ByteBufCodecs.VAR_INT, FlaskData::qualityLevel,
            FlaskData::new);
}
