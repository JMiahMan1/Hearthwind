package ht.treechop.common.settings;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Permissions {
   private Set<Setting> permittedSettings = new HashSet<>();

   public Permissions() {
   }

   public Permissions(Collection<Setting> permittedSettings) {
      permittedSettings.forEach(this::permit);
   }

   public void permit(Setting setting) {
      this.permittedSettings.add(setting);
   }

   public void forbid(Setting setting) {
      this.permittedSettings.remove(setting);
   }

   public void set(Setting setting, boolean permitted) {
      if (permitted) {
         this.permit(setting);
      } else {
         this.forbid(setting);
      }
   }

   public boolean isPermitted(SettingsField field, Object value) {
      return this.permittedSettings.contains(new Setting(field, value));
   }

   public boolean isPermitted(Setting setting) {
      return this.permittedSettings.contains(setting);
   }

   public Set<Setting> getPermittedSettings() {
      return Collections.unmodifiableSet(this.permittedSettings);
   }

   public Set<Object> getPermittedValues(SettingsField field) {
      return field.getValues().stream().filter(value -> this.permittedSettings.contains(new Setting(field, value))).collect(Collectors.toSet());
   }

   public void copy(Permissions permissions) {
      this.permittedSettings.clear();
      permissions.getPermittedSettings().forEach(this::permit);
   }
}
