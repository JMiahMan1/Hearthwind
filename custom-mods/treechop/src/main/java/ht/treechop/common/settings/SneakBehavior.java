package ht.treechop.common.settings;

import java.util.Arrays;
import java.util.function.Predicate;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.Entity;

public enum SneakBehavior implements net.minecraft.util.StringRepresentable {
   NONE("treechop.sneak_behavior.none", agent -> false, agent -> false),
   INVERT_CHOPPING("treechop.sneak_behavior.invert_chopping", Entity::isShiftKeyDown, agent -> false);

   public static final int maxNameLength = Arrays.stream(values()).map(Enum::name).map(String::length).max(Integer::compareTo).orElse(0);
   private final Predicate<net.minecraft.world.entity.Entity> chopBehavior;
   private final Predicate<net.minecraft.world.entity.Entity> fellBehavior;
   private final String langKey;

   private SneakBehavior(
      String langKey, Predicate<net.minecraft.world.entity.Entity> chopBehavior, Predicate<net.minecraft.world.entity.Entity> fellBehavior
   ) {
      this.chopBehavior = chopBehavior;
      this.fellBehavior = fellBehavior;
      this.langKey = langKey;
   }

   public SneakBehavior cycle() {
      return values()[Math.floorMod(this.ordinal() + 1, values().length)];
   }

   @Override
   public String toString() {
      return this.name();
   }

   public String getSerializedName() {
      return this.name();
   }

   public boolean shouldChangeChopBehavior(Entity agent) {
      return this.chopBehavior.test(agent);
   }

   public boolean shouldChangeFellBehavior(Entity agent) {
      return this.fellBehavior.test(agent);
   }

   public String getFancyText() {
      return I18n.get(this.langKey, new Object[0]);
   }
}
