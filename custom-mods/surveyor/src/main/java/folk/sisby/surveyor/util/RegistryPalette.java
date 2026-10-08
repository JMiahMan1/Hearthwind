package folk.sisby.surveyor.util;

import folk.sisby.surveyor.Surveyor;
import it.unimi.dsi.fastutil.ints.IntIterable;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntIterators;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.IdMap;
import net.minecraft.core.Registry;
import org.jetbrains.annotations.NotNull;

public class RegistryPalette<T> implements IntIterable {
   private final Registry<T> registry;
   private final int[] raw;
   private final int[] inverse;
   private final RegistryPalette<T>.ValueView valueView;
   private int size;

   public RegistryPalette(Registry<T> registry) {
      this.registry = registry;
      this.raw = ArrayUtil.ofSingle(-1, registry.size());
      this.inverse = ArrayUtil.ofSingle(-1, registry.size());
      this.size = 0;
      this.valueView = new RegistryPalette.ValueView();
   }

   public int find(int value) {
      return this.inverse[value];
   }

   private int add(int value) {
      this.raw[this.size] = value;
      this.inverse[value] = this.size;
      T object = (T)this.registry.byId(value);
      this.valueView.values.add(object);
      this.size++;
      return this.size - 1;
   }

   public int findOrAdd(int value) {
      int index = this.find(value);
      return index == -1 ? this.add(value) : index;
   }

   public int findOrAdd(T value) {
      return this.findOrAdd(this.registry.getId(value));
   }

   public int get(int index) {
      return this.raw[index];
   }

   @NotNull
   public IntIterator iterator() {
      return IntIterators.wrap(this.raw, 0, this.size);
   }

   public RegistryPalette<T>.ValueView view() {
      return this.valueView;
   }

   public class ValueView implements IdMap<T> {
      private final T defaultValue;
      private final List<T> values;
      private boolean errored;

      public ValueView() {
         Objects.requireNonNull(RegistryPalette.this);
         super();
         this.defaultValue = (T)(RegistryPalette.this.registry instanceof DefaultedRegistry<T> defreg
            ? defreg.getValue(defreg.getDefaultKey())
            : RegistryPalette.this.registry.byId(0));
         this.values = new ArrayList<>();
         this.errored = false;
      }

      public Registry<T> registry() {
         return RegistryPalette.this.registry;
      }

      public T byId(int index) {
         if (index >= this.values.size()) {
            if (!this.errored) {
               Surveyor.LOGGER
                  .error(
                     "[Surveyor] Palette view access at index {} for palette size {}! Returning garbage!",
                     new Object[]{index, this.values.size(), new IllegalStateException("garbage palette")}
                  );
               this.errored = true;
            }

            return this.defaultValue;
         } else {
            return this.values.get(index);
         }
      }

      public int getId(T value) {
         return this.values.indexOf(value);
      }

      @NotNull
      public Iterator<T> iterator() {
         return this.values.iterator();
      }

      public int size() {
         return RegistryPalette.this.size;
      }
   }
}
