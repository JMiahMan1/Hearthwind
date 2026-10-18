package ht.treechop.common.settings.codec;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.network.FriendlyByteBuf;

public class EnumCodec<T extends Enum<T>> extends AbstractSimpleCodec<T> {
   private final Class<T> enumType;
   public final Set<T> values;

   public EnumCodec(Class<T> enumType) {
      this.enumType = enumType;
      this.values = Collections.unmodifiableSet(Arrays.stream(enumType.getEnumConstants()).collect(Collectors.toSet()));
   }

   @Override
   public Set<T> getValues() {
      return this.values;
   }

   public T decode(net.minecraft.network.FriendlyByteBuf buffer) {
      try {
         return Enum.valueOf(this.enumType, buffer.readUtf(64));
      } catch (IllegalArgumentException var3) {
         return this.enumType.getEnumConstants()[0];
      }
   }

   @Override
   public void encode(FriendlyByteBuf buffer, Object object) {
      buffer.writeUtf(this.getValueOf(object).map(Enum::name).orElse(""));
   }

   @Override
   public Class<T> getTypeClass() {
      return this.enumType;
   }
}
