package net.jmb19905.niftycarts.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NiftyInventory extends net.minecraft.core.NonNullList<net.minecraft.world.item.ItemStack> {
   private Consumer<Integer> onContentsChanged;

   public static NiftyInventory create() {
      return new NiftyInventory(new ArrayList<>(), null);
   }

   public static NiftyInventory createWithCapacity(int i) {
      return new NiftyInventory(new ArrayList<>(i), null);
   }

   public static NiftyInventory withSize(int i, ItemStack object) {
      if (object == null) {
         object = ItemStack.EMPTY;
      }

      List<net.minecraft.world.item.ItemStack> list = new ArrayList<>();

      for (int j = 0; j < i; j++) {
         list.add(object);
      }

      return new NiftyInventory(list, object);
   }

   public NiftyInventory(List<net.minecraft.world.item.ItemStack> list, @Nullable net.minecraft.world.item.ItemStack object) {
      super(list, object);
   }

   public void setOnContentsChanged(Consumer<Integer> onContentsChanged) {
      this.onContentsChanged = onContentsChanged;
   }

   @NotNull
   public ItemStack set(int i, ItemStack object) {
      ItemStack out = (ItemStack)super.set(i, object);
      this.onContentsChanged.accept(i);
      return out;
   }

   public void add(int i, ItemStack object) {
      super.add(i, object);
      this.onContentsChanged.accept(i);
   }

   public ItemStack remove(int i) {
      ItemStack out = (ItemStack)super.remove(i);
      this.onContentsChanged.accept(i);
      return out;
   }

   public void clear() {
      int size = this.size();
      super.clear();

      for (int i = 0; i < size; i++) {
         this.onContentsChanged.accept(i);
      }
   }
}
