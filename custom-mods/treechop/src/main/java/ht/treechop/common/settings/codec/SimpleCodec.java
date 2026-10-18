package ht.treechop.common.settings.codec;

import java.util.Optional;
import java.util.Set;
import net.minecraft.network.FriendlyByteBuf;

public interface SimpleCodec<T> {
   T decode(net.minecraft.network.FriendlyByteBuf var1);

   void encode(FriendlyByteBuf var1, Object var2);

   String getLocalizationString(Object var1);

   Optional<T> getValueOf(Object var1);

   Set<T> getValues();
}
