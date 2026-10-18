package net.medievalweapons.item;

import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item.Properties;

public class LongBowItem extends BowItem {
   public LongBowItem(Properties settings) {
      super(settings);
   }

   public int getDefaultProjectileRange() {
      return 20;
   }
}
