package net.mehvahdjukaar.smarterfarmers.mixins;

import java.util.List;
import net.minecraft.world.entity.ai.behavior.WorkAtComposter;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WorkAtComposter.class)
public interface WorkAtComposterAccessor {
   @Mutable
   @Accessor("COMPOSTABLE_ITEMS")
   static void setCompostableItems(List<Item> items) {
      throw new AssertionError();
   }

   @Accessor("COMPOSTABLE_ITEMS")
   static List<Item> getCompostableItems() {
      throw new AssertionError();
   }
}
