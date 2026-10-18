package ht.treechop.common.config;

import java.util.function.Supplier;

public class Lazy<T> implements Supplier<T> {
   private final Supplier<T> supplier;
   private T cache;
   private boolean available = false;
   private boolean locked = false;

   public Lazy(Supplier<T> supplier) {
      this.supplier = supplier;
   }

   public Lazy(Signal<Lazy<?>> resetSignal, Supplier<T> supplier) {
      this(supplier);
      resetSignal.add(this);
   }

   @Override
   public T get() {
      if (!this.available) {
         this.cache = this.supplier.get();
         this.available = true;
      }

      return this.cache;
   }

   public void reset() {
      if (!this.locked) {
         this.available = false;
      }
   }

   public void override(T override) {
      this.cache = override;
      this.available = true;
      this.locked = true;
   }
}
