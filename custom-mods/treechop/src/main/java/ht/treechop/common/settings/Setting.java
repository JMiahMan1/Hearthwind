package ht.treechop.common.settings;

import net.minecraft.network.FriendlyByteBuf;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Setting {
   private final SettingsField field;
   private Object value;

   Setting(SettingsField field) {
      this.field = field;
      this.value = field.getDefaultValue();
   }

   public Setting(SettingsField field, Object value) {
      this.field = field;
      this.value = value;
   }

   public SettingsField getField() {
      return this.field;
   }

   public Object getValue() {
      return this.value;
   }

   public void set(Object newValue) {
      if (newValue.getClass().isInstance(this.value)) {
         this.value = newValue;
      } else {
         throw new IllegalArgumentException(String.format("SettingsField %s cannot accept value %s (%s)", this.field, newValue, newValue.getClass()));
      }
   }

   public void encode(FriendlyByteBuf buffer) {
      this.field.encode(buffer, this.value);
   }

   public static Setting decode(FriendlyByteBuf buffer) {
      return SettingsField.decode(buffer);
   }

   @Override
   public boolean equals(Object other) {
      if (other == this) {
         return true;
      } else {
         return !(other instanceof Setting otherSetting) ? false : this.field == otherSetting.field && this.value.equals(otherSetting.value);
      }
   }

   @Override
   public int hashCode() {
      return new HashCodeBuilder(17, 31).append(this.field).append(this.value).toHashCode();
   }
}
