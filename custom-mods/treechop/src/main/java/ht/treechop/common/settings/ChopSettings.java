package ht.treechop.common.settings;

import ht.treechop.TreeChop;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;

public class ChopSettings {
   private final EnumMap<SettingsField, Object> fieldValues = new EnumMap<>(SettingsField.class);

   public ChopSettings() {
      for (SettingsField field : SettingsField.values()) {
         this.fieldValues.put(field, field.getDefaultValue());
      }
   }

   public ChopSettings(ChopSettings template) {
      this();
      this.copyFrom(template);
   }

   public boolean getChoppingEnabled() {
      return this.get(SettingsField.CHOPPING, Boolean.class);
   }

   public SneakBehavior getSneakBehavior() {
      return this.get(SettingsField.SNEAK_BEHAVIOR, SneakBehavior.class);
   }

   public boolean getTreesMustHaveLeaves() {
      return this.get(SettingsField.TREES_MUST_HAVE_LEAVES, Boolean.class);
   }

   public boolean getChopInCreativeMode() {
      return this.get(SettingsField.CHOP_IN_CREATIVE_MODE, Boolean.class);
   }

   public ChopSettings setChoppingEnabled(boolean enabled) {
      this.set(SettingsField.CHOPPING, enabled);
      return this;
   }

   public ChopSettings setSneakBehavior(SneakBehavior behavior) {
      this.set(SettingsField.SNEAK_BEHAVIOR, behavior);
      return this;
   }

   public ChopSettings setTreesMustHaveLeaves(boolean enabled) {
      this.set(SettingsField.TREES_MUST_HAVE_LEAVES, enabled);
      return this;
   }

   public ChopSettings setChopInCreativeMode(boolean enabled) {
      this.set(SettingsField.CHOP_IN_CREATIVE_MODE, enabled);
      return this;
   }

   @Override
   public boolean equals(Object other) {
      if (other == this) {
         return true;
      } else {
         return other instanceof ChopSettings otherSettings
            ? Arrays.stream(SettingsField.VALUES).allMatch(field -> this.get(field).equals(otherSettings.get(field)))
            : false;
      }
   }

   public ChopSettings copyFrom(ChopSettings other) {
      this.fieldValues.putAll(other.fieldValues);
      return this;
   }

   public <T> T get(SettingsField field, Class<T> type) {
      Object value = this.fieldValues.get(field);
      if (!type.isInstance(value)) {
         value = field.getDefaultValue();
         this.fieldValues.put(field, value);
      }

      return type.cast(value);
   }

   public Setting getSetting(SettingsField field) {
      return new Setting(field, this.get(field));
   }

   public Object get(SettingsField field) {
      return this.fieldValues.get(field);
   }

   public void forEach(BiConsumer<SettingsField, Object> consumer) {
      this.fieldValues.forEach(consumer);
   }

   public ChopSettings set(SettingsField field, Object value) {
      if (field.getDefaultValue().getClass().isInstance(value)) {
         this.fieldValues.put(field, value);
      } else {
         TreeChop.LOGGER.warn(String.format("Refusing to set setting %s to illegal value %s (%s)", field, value, value.getClass()));
      }

      return this;
   }

   public ChopSettings set(Pair<SettingsField, Object> fieldValuePair) {
      return this.set((SettingsField)fieldValuePair.getLeft(), fieldValuePair.getRight());
   }

   public ChopSettings set(Setting setting) {
      return this.set(setting.getField(), setting.getValue());
   }

   public List<Setting> getAll() {
      return Arrays.stream(SettingsField.values()).map(field -> new Setting(field, this.get(field))).collect(Collectors.toList());
   }
}
