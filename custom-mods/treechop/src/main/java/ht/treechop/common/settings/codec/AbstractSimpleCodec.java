package ht.treechop.common.settings.codec;

import java.util.Optional;

public abstract class AbstractSimpleCodec<T> implements SimpleCodec<T> {
   @Override
   public final String getLocalizationString(Object object) {
      Optional<T> value = this.getValueOf(object);
      return value.isPresent() ? this.localizeSafe(value.get()) : "treechop.codec.bad_value";
   }

   protected String localizeSafe(T value) {
      return value.toString();
   }

   protected abstract Class<T> getTypeClass();

   @Override
   public Optional<T> getValueOf(Object object) {
      return this.getTypeClass().isInstance(object) ? Optional.of(this.getTypeClass().cast(object)) : Optional.empty();
   }
}
