package ht.treechop.common.config;

import net.minecraft.world.item.Item;

class QualifiedItem<T> {
   final Item item;
   final T qualifier;

   public QualifiedItem(net.minecraft.world.item.Item item, T qualifier) {
      this.item = item;
      this.qualifier = qualifier;
   }

   public Item getItem() {
      return this.item;
   }

   public T getQualifier() {
      return this.qualifier;
   }
}
