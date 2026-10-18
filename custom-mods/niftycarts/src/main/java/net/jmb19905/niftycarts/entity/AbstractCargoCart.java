package net.jmb19905.niftycarts.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import net.minecraft.core.NonNullList;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public abstract class AbstractCargoCart extends AbstractDrawnInventoryEntity {
   private static final ImmutableList<net.minecraft.network.syncher.EntityDataAccessor<net.minecraft.world.item.ItemStack>> CARGO = ImmutableList.of(
      SynchedEntityData.defineId(AbstractCargoCart.class, EntityDataSerializers.ITEM_STACK),
      SynchedEntityData.defineId(AbstractCargoCart.class, EntityDataSerializers.ITEM_STACK),
      SynchedEntityData.defineId(AbstractCargoCart.class, EntityDataSerializers.ITEM_STACK),
      SynchedEntityData.defineId(AbstractCargoCart.class, EntityDataSerializers.ITEM_STACK)
   );

   public AbstractCargoCart(
      net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.Entity> entityTypeIn,
      net.minecraft.world.level.Level worldIn,
      int inventorySize
   ) {
      super(entityTypeIn, worldIn, inventorySize);
   }

   @Override
   protected void defineSynchedData(Builder builder) {
      super.defineSynchedData(builder);
      UnmodifiableIterator var2 = CARGO.iterator();

      while (var2.hasNext()) {
         net.minecraft.network.syncher.EntityDataAccessor<net.minecraft.world.item.ItemStack> parameter = (EntityDataAccessor)var2.next();
         builder.define(parameter, ItemStack.EMPTY);
      }
   }

   public net.minecraft.core.NonNullList<net.minecraft.world.item.ItemStack> getCargo() {
      net.minecraft.core.NonNullList<net.minecraft.world.item.ItemStack> cargo = NonNullList.withSize(CARGO.size(), ItemStack.EMPTY);

      for (int i = 0; i < CARGO.size(); i++) {
         cargo.set(i, (ItemStack)this.entityData.get((EntityDataAccessor)CARGO.get(i)));
      }

      return cargo;
   }

   @Override
   protected void onContentsChanged(int slot) {
      Object2IntMap<net.minecraft.world.item.Item> totals = new Object2IntLinkedOpenHashMap();
      Object2ObjectMap<net.minecraft.world.item.Item, net.minecraft.world.item.ItemStack> stacks = new Object2ObjectOpenHashMap();

      for (int i = 0; i < this.getItemStacks().size(); i++) {
         ItemStack stack = (ItemStack)this.getItemStacks().get(i);
         if (!stack.isEmpty()) {
            totals.mergeInt(stack.getItem(), 1, Integer::sum);
            stacks.putIfAbsent(stack.getItem(), stack);
         }
      }

      Iterator<Entry<net.minecraft.world.item.Item>> topTotals = totals.object2IntEntrySet()
         .stream()
         .sorted(
            Comparator.<Entry<net.minecraft.world.item.Item>>comparingInt(e -> e.getKey() instanceof BlockItem ? 0 : 1)
               .thenComparingInt(e -> -e.getIntValue())
         )
         .limit(CARGO.size())
         .iterator();
      ItemStack[] items = new ItemStack[CARGO.size()];
      Arrays.fill(items, ItemStack.EMPTY);
      int forth = this.getItemStacks().size() / CARGO.size();
      int pos = 0;

      while (topTotals.hasNext() && pos < CARGO.size()) {
         Entry<net.minecraft.world.item.Item> entry = topTotals.next();
         int count = Math.max(1, (entry.getIntValue() + forth / 2) / forth);

         for (int n = 1; n <= count && pos < CARGO.size(); n++) {
            ItemStack stack = ((ItemStack)stacks.getOrDefault(entry.getKey(), ItemStack.EMPTY)).copy();
            stack.setCount(Math.min(stack.getMaxStackSize(), entry.getIntValue() / n));
            items[pos++] = stack;
         }
      }

      for (int ix = 0; ix < CARGO.size(); ix++) {
         this.getEntityData().set((EntityDataAccessor)CARGO.get(ix), items[ix]);
      }
   }

   @Override
   protected void saveInventory(ValueOutput output) {
      this.addChestVehicleSaveData(output);
   }

   @Override
   protected void readInventory(ValueInput input) {
      this.readChestVehicleSaveData(input);
   }
}
