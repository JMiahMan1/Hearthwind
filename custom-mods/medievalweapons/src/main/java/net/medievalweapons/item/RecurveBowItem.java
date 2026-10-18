package net.medievalweapons.item;

import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item.Properties;

public class RecurveBowItem extends BowItem {
   public RecurveBowItem(Properties settings) {
      super(settings);
   }

   public int getDefaultProjectileRange() {
      return 12;
   }
}
