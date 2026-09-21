package net.satisfy.farm_and_charm.core.util;

import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public final class StreamCodecUtil {
    public static <T, B extends FriendlyByteBuf> StreamCodec<B, NonNullList<T>> nonNullList(StreamCodec<B, T> elementCodec, T defaultElement) {
        return nonNullList(elementCodec);
    }

    // 26.2: filler elements are immediately overwritten on decode, and
    // Ingredient no longer has an AIR-tolerant EMPTY constant, so decode
    // builds the list element-by-element instead.
    public static <T, B extends FriendlyByteBuf> StreamCodec<B, NonNullList<T>> nonNullList(StreamCodec<B, T> elementCodec) {
        return StreamCodec.of((buf, value) -> {
            buf.writeVarInt(value.size());

            for (T element : value) {
                elementCodec.encode(buf, element);
            }
        }, buf -> {
            int size = buf.readVarInt();
            NonNullList<T> list = NonNullList.create();
            for (int i = 0; i < size; i++) {
                list.add(elementCodec.decode(buf));
            }

            return list;
        });
    }
}